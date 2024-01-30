package it.italiandudes.filetransfer6329.modules.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import it.italiandudes.filetransfer6329.modules.configuration.ConfigurationMap;
import it.italiandudes.filetransfer6329.modules.configuration.ModuleConfiguration;
import it.italiandudes.filetransfer6329.modules.http.ModuleHTTP;
import it.italiandudes.filetransfer6329.throwables.exceptions.module.configuration.ConfigurationModuleException;
import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;

public class DownloadHTTPHandler implements HttpHandler {

    // Context
    public static final String CONTEXT = "/download";

    // Attributes
    private static Boolean logSendForDownload = null;

    // Methods
    private static String convertByteSecondToString(int speed) {
        if(speed <= 0) return "0B/s";
        final String[] units = new String[] { "B/s", "KB/s", "MB/s", "GB/s" };
        int digitGroups = (int) (Math.log10(speed)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(speed/Math.pow(1024, digitGroups)) + units[digitGroups];
    }

    // Handler
    @Override
    public void handle(@NotNull final HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Connection", "close");
        if (logSendForDownload == null) {
            try {
                logSendForDownload = (Boolean) ModuleConfiguration.getInstance().getConfigValue(ConfigurationMap.Keys.LOG_SEND_FOR_DOWNLOAD);
            } catch (ConfigurationModuleException e) {
                logSendForDownload = false;
            }
        }
        String[] uriPath = exchange.getRequestURI().getPath().split("/");
        String filename;
        if (uriPath.length <= 2) {
            ModuleHTTP.CommonResponse.sendBadRequest(exchange);
            exchange.close();
            return;
        }
        filename = uriPath[2];
        File filepath = new File(ModuleHTTP.getInstance().getRootDirectory().getAbsolutePath() + File.separator + filename);
        if (!filepath.exists() || !filepath.isFile()) {
            ModuleHTTP.CommonResponse.sendNotFound(exchange);
            exchange.close();
            return;
        }
        File resolvedFilePath = filepath.getAbsoluteFile();
        File[] fileList = ModuleHTTP.getInstance().getRootDirectory().listFiles();
        if (fileList == null) {
            ModuleHTTP.CommonResponse.sendInternalServerError(exchange);
        } else {
            boolean found = false;
            for (File f : fileList) {
                if (f.getAbsolutePath().equals(resolvedFilePath.getAbsolutePath())) {
                    found = true;
                    break;
                }
            }
            if (found) {
                Logger.log(exchange.getRemoteAddress().getHostName() + " --> " + resolvedFilePath.getAbsolutePath());
                exchange.getResponseHeaders().add("Content-Disposition", "attachment;filename=\"" + filename + "\"");
                exchange.sendResponseHeaders(200, 0); // Chunked Mode
                try (FileInputStream inputStream = new FileInputStream(resolvedFilePath)) {
                    final byte[] buffer = new byte[ModuleHTTP.getInstance().getMaxDownloadSpeedKB() * 1024];
                    int count;
                    while ((count = inputStream.read(buffer)) >= 0) {
                        if (logSendForDownload) Logger.log(exchange.getRemoteAddress().getHostName() + " --> " + resolvedFilePath.getAbsolutePath() + " [" + convertByteSecondToString(count) + "]");
                        exchange.getResponseBody().write(buffer, 0, count);
                    }
                } catch (FileNotFoundException e) {
                    ModuleHTTP.CommonResponse.sendNotFound(exchange);
                }
            } else {
                ModuleHTTP.CommonResponse.sendForbidden(exchange);
            }
        }
        exchange.close();
    }
}
