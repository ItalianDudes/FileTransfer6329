package it.italiandudes.filetransfer6329.server.modules;

import it.italiandudes.filetransfer6329.throwables.errors.ModuleError;
import it.italiandudes.filetransfer6329.throwables.exceptions.ModuleException;
import it.italiandudes.filetransfer6329.throwables.exceptions.module.generic.*;
import it.italiandudes.idl.common.InfoFlags;
import it.italiandudes.idl.common.Logger;
import it.italiandudes.idl.common.StringHandler;
import org.jetbrains.annotations.NotNull;

public abstract class BaseModule {

    // Attributes
    @NotNull private ModuleState moduleState = ModuleState.NOT_LOADED;
    public final String MODULE_NAME;

    // Base Module Constructor
    protected BaseModule(@NotNull final String MODULE_NAME) {
        this.MODULE_NAME = MODULE_NAME;
    }

    // Abstract Methods
    protected abstract void loadModule(final boolean isReloading) throws ModuleException, ModuleError;
    protected abstract void unloadModule(final boolean isReloading, final boolean bypassPreliminaryChecks) throws ModuleException, ModuleError;

    // Implemented Methods
    public final synchronized void loadModule() throws ModuleException, ModuleError {
        loadModule(false);
    }
    public final synchronized void unloadModule() throws ModuleException, ModuleError {
        unloadModule(false, false);
    }
    protected final synchronized void unloadModule(final boolean isReloading) throws ModuleException, ModuleError {
        unloadModule(isReloading, false);
    }
    public final synchronized void emergencyUnloadModule() {
        try {
            unloadModule(false, true);
        } catch (Throwable e) {
            Logger.log("NOTE: THIS IS JUST A MESSAGE, THE ERROR WON'T PROPAGATE\n" + StringHandler.getStackTrace(e), new InfoFlags(e instanceof Exception, e instanceof Error, false, true));
        }
    }
    public final synchronized void reloadModule() throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Reload: Started!");
        moduleReloadPreliminaryCheck(MODULE_NAME);
        setModuleState(ModuleState.RELOADING);

        try {
            unloadModule(true);
            loadModule(true);
        } catch (ModuleException | ModuleError e) {
            unloadModule(false, true);
            throw e;
        }

        setModuleState(ModuleState.LOADED);
        Logger.log(MODULE_NAME + " Module Reload: Successful!");
    }
    protected final synchronized void setModuleState(@NotNull final ModuleState moduleState) {
        this.moduleState = moduleState;
    }
    @NotNull public final ModuleState getModuleState() {
        return moduleState;
    }
    protected final synchronized void moduleLoadPreliminaryCheck(@NotNull final String moduleName, final boolean isReloading) throws ModuleLoadingException, ModuleAlreadyLoadedException, ModuleError {
        switch (getModuleState()) {
            case ERROR:
                throw new ModuleError(moduleName + " Module Load: Canceled! (Reason: This module is in error)");
            case LOADING:
                throw new ModuleLoadingException(moduleName + " Module Load: Canceled! (Reason: Another thread is performing module load)");
            case UNLOADING:
                throw new ModuleLoadingException(moduleName + " Module Load: Canceled! (Reason: Another thread is performing the module unload)");
            case RELOADING:
                if (!isReloading) throw new ModuleLoadingException(moduleName + " Module Load: Canceled! (Reason: Another thread is performing the module reload)");
            case LOADED:
                throw new ModuleAlreadyLoadedException(moduleName + "Module Load: Canceled! (Reason: this module is already loaded)");
        }
    }
    protected final synchronized void moduleUnloadPreliminaryCheck(@NotNull final String moduleName, final boolean isReloading) throws ModuleUnloadingException, ModuleNotLoadedException, ModuleError {
        switch (getModuleState()) {
            case ERROR:
                throw new ModuleError(moduleName + " Module Unload: Canceled! (Reason: This module is in error)");
            case LOADING:
                throw new ModuleUnloadingException(moduleName + " Module Unload: Canceled! (Reason: Another thread is performing module load)");
            case UNLOADING:
                throw new ModuleUnloadingException(moduleName + " Module Unload: Canceled! (Reason: Another thread is performing the module unload)");
            case NOT_LOADED:
                throw new ModuleNotLoadedException(moduleName + " Module Unload: Canceled! (Reason: the module is not loaded)");
            case RELOADING:
                if (!isReloading) throw new ModuleUnloadingException(moduleName + " Module Unload: Canceled! (Reason: Another thread is performing the module reload)");
        }
    }
    protected final synchronized void moduleReloadPreliminaryCheck(@NotNull final String moduleName) throws ModuleReloadingException, ModuleNotLoadedException, ModuleError {
        switch (getModuleState()) {
            case ERROR:
                throw new ModuleError(moduleName + " Module Reload: Canceled! (Reason: This module is in error)");
            case LOADING:
                throw new ModuleReloadingException(moduleName + " Module Reload: Canceled! (Reason: Another thread is performing module load)");
            case UNLOADING:
                throw new ModuleReloadingException(moduleName + " Module Reload: Canceled! (Reason: Another thread is performing the module unload)");
            case RELOADING:
                throw new ModuleReloadingException(moduleName + " Module Reload: Canceled! (Reason: Another thread is performing the module reload)");
            case NOT_LOADED:
                throw new ModuleNotLoadedException(moduleName + " Module Reload: Canceled! (Reason: the module is not loaded)");
        }
    }
}
