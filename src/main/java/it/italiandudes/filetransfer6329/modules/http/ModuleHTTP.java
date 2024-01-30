package it.italiandudes.filetransfer6329.modules.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import it.italiandudes.filetransfer6329.modules.BaseModule;
import it.italiandudes.filetransfer6329.modules.ModuleState;
import it.italiandudes.filetransfer6329.modules.configuration.ConfigurationMap;
import it.italiandudes.filetransfer6329.modules.configuration.ModuleConfiguration;
import it.italiandudes.filetransfer6329.modules.http.handlers.DownloadHTTPHandler;
import it.italiandudes.filetransfer6329.modules.http.handlers.ListHTTPHandler;
import it.italiandudes.filetransfer6329.throwables.errors.ModuleError;
import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;
import it.italiandudes.filetransfer6329.utils.Defs;
import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class ModuleHTTP extends BaseModule {

    // Attributes
    private HttpServer httpServer = null;
    private Integer port = null;
    private File rootDirectory = null;
    private Integer maxDownloadSpeedKB = null;

    // Module Management Methods
    @Override
    protected synchronized void loadModule(final boolean isReloading) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Load: Started!");
        moduleLoadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.LOADING);

        port = (Integer) ModuleConfiguration.getInstance().getConfigValue(ConfigurationMap.Keys.SERVER_PORT);
        if (port == null) {
            httpServer = null;
            rootDirectory = null;
            maxDownloadSpeedKB = null;
            setModuleState(ModuleState.NOT_LOADED);
            throw new ModuleException(MODULE_NAME + " Module Load: Canceled! (Reason: the port is null)");
        }
        String rootDirectoryStr = (String) ModuleConfiguration.getInstance().getConfigValue(ConfigurationMap.Keys.SERVER_ROOT_DIRECTORY);
        if (rootDirectoryStr == null) {
            httpServer = null;
            rootDirectory = null;
            port = null;
            maxDownloadSpeedKB = null;
            setModuleState(ModuleState.NOT_LOADED);
            throw new ModuleException(MODULE_NAME + " Module Load: Canceled! (Reason: the root directory is null)");
        }

        rootDirectory = new File(rootDirectoryStr);
        if (!rootDirectory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            rootDirectory.mkdirs();
        }

        if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            httpServer = null;
            rootDirectory = null;
            port = null;
            maxDownloadSpeedKB = null;
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: can't create the server root directory)");
        }

        maxDownloadSpeedKB = (Integer) ModuleConfiguration.getInstance().getConfigValue(ConfigurationMap.Keys.SERVER_DOWNLOAD_SPEED_KB);
        if (maxDownloadSpeedKB == null || maxDownloadSpeedKB <= 0) {
            httpServer = null;
            rootDirectory = null;
            port = null;
            maxDownloadSpeedKB = null;
            setModuleState(ModuleState.NOT_LOADED);
            throw new ModuleException(MODULE_NAME + " Module Load: Canceled! (Reason: the root directory is null)");
        }

        try {
            InetSocketAddress socketAddress = new InetSocketAddress(port);
            httpServer = HttpServer.create(socketAddress, Defs.HTTPSERVER_BACKLOG);
            httpServer.createContext(ListHTTPHandler.CONTEXT, new ListHTTPHandler());
            httpServer.createContext(DownloadHTTPHandler.CONTEXT, new DownloadHTTPHandler());
            httpServer.start();
        } catch (IllegalArgumentException iae) {
            httpServer = null;
            rootDirectory = null;
            port = null;
            maxDownloadSpeedKB = null;
            setModuleState(ModuleState.ERROR);
            throw new ModuleException(MODULE_NAME + " Module Load: Failed! (Reason: the port is outside the range 0~65535)", iae);
        } catch (IOException ioe) {
            httpServer = null;
            rootDirectory = null;
            port = null;
            maxDownloadSpeedKB = null;
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: an IOException has been raised)", ioe);
        }

        if (!isReloading) setModuleState(ModuleState.LOADED);
        Logger.log(MODULE_NAME + " Module Load: Successful!");
    }
    @Override
    protected synchronized void unloadModule(final boolean isReloading, final boolean bypassPreliminaryChecks) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Unload: Started!");
        if (!bypassPreliminaryChecks) moduleUnloadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.UNLOADING);

        if (httpServer != null) {
            httpServer.stop(Defs.HTTPSERVER_STOP_DELAY);
            httpServer = null;
            rootDirectory = null;
            port = null;
            maxDownloadSpeedKB = null;
        }

        if (!isReloading) setModuleState(ModuleState.NOT_LOADED);
        Logger.log(MODULE_NAME + " Module Unload: Successful!");
    }

    // Module Methods
    public static final class CommonResponse {
        private static void sendHTTPResponse(@NotNull final HttpExchange exchange, int returnCode, @NotNull final String response) throws IOException {
            byte[] responseBodyBytes = response.getBytes(StandardCharsets.ISO_8859_1);
            exchange.sendResponseHeaders(returnCode, responseBodyBytes.length);
            exchange.getResponseBody().write(responseBodyBytes, 0, responseBodyBytes.length);
            exchange.getResponseBody().flush();
        }
        public static void sendBadRequest(@NotNull final HttpExchange exchange) throws IOException {
            sendHTTPResponse(exchange, 400, "Bad Request");
        }
        public static void sendUnauthorized(@NotNull final HttpExchange exchange) throws IOException {
            sendHTTPResponse(exchange, 401, "Unauthorized");
        }
        public static void sendForbidden(@NotNull final HttpExchange exchange) throws IOException {
            sendHTTPResponse(exchange, 403, "Forbidden");
        }
        public static void sendNotFound(@NotNull final HttpExchange exchange) throws IOException {
            sendHTTPResponse(exchange, 404, "Not Found");
        }
        public static void sendInternalServerError(@NotNull final HttpExchange exchange) throws IOException {
            sendHTTPResponse(exchange, 500, "Internal Server Error");
        }
    }

    // Instance
    private static ModuleHTTP instance = null;
    private ModuleHTTP() {
        super("HTTP");
    }
    @NotNull
    public static ModuleHTTP getInstance() {
        if (instance == null) instance = new ModuleHTTP();
        return instance;
    }
    public File getRootDirectory() {
        return rootDirectory;
    }
    public int getMaxDownloadSpeedKB() {
        return maxDownloadSpeedKB>=0?maxDownloadSpeedKB:(64 * 1024);
    }
}
