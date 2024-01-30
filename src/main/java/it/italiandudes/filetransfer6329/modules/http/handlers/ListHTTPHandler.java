package it.italiandudes.filetransfer6329.modules.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import it.italiandudes.filetransfer6329.modules.http.ModuleHTTP;
import it.italiandudes.filetransfer6329.utils.Defs;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Scanner;

public class ListHTTPHandler implements HttpHandler {

    // Context
    public static final String CONTEXT = "/list";

    // Attributes
    private static String listHTML = null;

    // Methods
    private static String convertByteToString(long size) {
        if(size <= 0) return "0B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + units[digitGroups];
    }
    private static void readHTML() {
        try (InputStream stream = Defs.Resources.getAsStream("/html/list.html")) {
            StringBuilder builder = new StringBuilder();
            Scanner reader = new Scanner(stream, "UTF-8");
            while (reader.hasNext()) {
                builder.append(reader.nextLine());
            }
            reader.close();
            listHTML = builder.toString();
        } catch (IOException e) {
            listHTML = null;
        }
    }

    // Handler
    @Override
    public void handle(@NotNull final HttpExchange exchange) throws IOException {
        if (listHTML == null) readHTML();
        exchange.getResponseHeaders().set("Connection", "close");
        File[] files = ModuleHTTP.getInstance().getRootDirectory().listFiles();
        StringBuilder builder = new StringBuilder();
        if (files == null || files.length == 0) {
            builder.append("No file available.");
        } else {
            String base = (listHTML==null?"<title>Server6329</title>\n<h1>FILE LIST</h1>":listHTML);
            builder.append(base).append('\n');
            builder.append("<ul>").append('\n');
            for (File file : files) {
                builder.append("<li><b>[").append(convertByteToString(file.length())).append("]</b> <a href=\"").append("/download/").append(file.getName()).append("\">").append(file.getName()).append("</a></li>").append('\n');
            }
            builder.append("</ul>");
        }
        byte[] response = builder.toString().getBytes(StandardCharsets.ISO_8859_1);
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response, 0, response.length);
        exchange.getResponseBody().flush();
        exchange.close();
    }

}
