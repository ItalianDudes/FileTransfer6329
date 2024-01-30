package it.italiandudes.filetransfer6329.modules.configuration;

import it.italiandudes.filetransfer6329.modules.BaseModule;
import it.italiandudes.filetransfer6329.modules.ModuleState;
import it.italiandudes.filetransfer6329.throwables.errors.ModuleError;
import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;
import it.italiandudes.filetransfer6329.throwables.exceptions.module.configuration.ConfigurationModuleException;
import it.italiandudes.filetransfer6329.utils.Defs;
import it.italiandudes.filetransfer6329.utils.JSONManager;
import it.italiandudes.idl.common.JarHandler;
import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public final class ModuleConfiguration extends BaseModule {

    // Attributes
    private JSONObject configuration = null;

    // Module Management Methods
    @Override
    protected synchronized void loadModule(final boolean isReloading) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Load: Started!");
        moduleLoadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.LOADING);

        File configurationFilePath = new File(Defs.Resources.JSON.JSON_CONFIGURATION);
        if (!configurationFilePath.exists() || !configurationFilePath.isFile()) {
            try {
                JarHandler.copyFileFromJar(new File(Defs.JAR_POSITION), Defs.Resources.JSON.DEFAULT_JSON_CONFIGURATION, configurationFilePath, true);
            } catch (IOException e) {
                setModuleState(ModuleState.ERROR);
                throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: default configuration file copy failed into jar directory)");
            }
        }

        try {
            configuration = JSONManager.readJSON(configurationFilePath);
            validateAndFixConfiguration();
        } catch (FileNotFoundException fileNotFoundException) {
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: configuration file not found, this shouldn't happen)", fileNotFoundException);
        } catch (JSONException jsonException) {
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: the configuration file syntax is invalid)", jsonException);
        }

        if (!isReloading) setModuleState(ModuleState.LOADED);
        Logger.log(MODULE_NAME + " Module Load: Successful!");
    }
    @Override
    protected synchronized void unloadModule(final boolean isReloading, final boolean bypassPreliminaryChecks) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Unload: Started!");
        if (!bypassPreliminaryChecks) moduleUnloadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.UNLOADING);

        configuration = null;

        if (!isReloading) setModuleState(ModuleState.NOT_LOADED);
        Logger.log(MODULE_NAME + " Module Unload: Successful!");
    }

    // Module Methods
    private void validateAndFixConfiguration()throws ModuleException {
        try {
            configuration.getInt(ConfigurationMap.Keys.SERVER_PORT);
        } catch (JSONException e) {
            ConfigurationMap.fixEntry(configuration, ConfigurationMap.Keys.SERVER_PORT);
        }
        try {
            configuration.getString(ConfigurationMap.Keys.SERVER_ROOT_DIRECTORY);
        } catch (JSONException e) {
            ConfigurationMap.fixEntry(configuration, ConfigurationMap.Keys.SERVER_ROOT_DIRECTORY);
        }
        try {
            if (configuration.getInt(ConfigurationMap.Keys.SERVER_DOWNLOAD_SPEED_KB) <= 0) {
                ConfigurationMap.fixEntry(configuration, ConfigurationMap.Keys.SERVER_DOWNLOAD_SPEED_KB);
            }
        } catch (JSONException e) {
            ConfigurationMap.fixEntry(configuration, ConfigurationMap.Keys.SERVER_DOWNLOAD_SPEED_KB);
        }
        try {
            JSONManager.writeJSON(configuration, new File(Defs.Resources.JSON.JSON_CONFIGURATION));
        } catch (IOException e) {
            throw new ModuleException(MODULE_NAME + " Module Load: Failed! (Reason: Failed to write json configuration file)", e);
        }
    }
    @Nullable
    public Object getConfigValue(@NotNull final String key) throws ConfigurationModuleException {
        if (getModuleState() != ModuleState.LOADED) throw new ConfigurationModuleException("The configuration module is not loaded");
        if (configuration.isNull(key)) return null;
        else return configuration.get(key);
    }

    // Instance
    private static ModuleConfiguration instance = null;
    private ModuleConfiguration() {
        super("Configuration");
    }
    @NotNull
    public static ModuleConfiguration getInstance() {
        if (instance == null) instance = new ModuleConfiguration();
        return instance;
    }
}
