package it.italiandudes.bot6329.modules.database;

import it.italiandudes.bot6329.modules.BaseModule;
import it.italiandudes.bot6329.modules.ModuleState;
import it.italiandudes.bot6329.modules.configuration.ConfigurationMap;
import it.italiandudes.bot6329.modules.configuration.ModuleConfiguration;
import it.italiandudes.bot6329.modules.database.entries.DatabaseKeyParameters;
import it.italiandudes.bot6329.throwables.errors.ModuleError;
import it.italiandudes.bot6329.throwables.exceptions.ModuleException;
import it.italiandudes.bot6329.utils.Resource;
import it.italiandudes.idl.common.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Scanner;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class ModuleDatabase extends BaseModule {

    // Attributes
    private Connection dbConnection = null;
    private static final String DB_PREFIX = "jdbc:sqlite:";
    public static final String SUPPORTED_DATABASE_VERSION = "1.0";

    // Module Management Methods
    @Override
    protected synchronized void loadModule(final boolean isReloading) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Load: Started!");
        moduleLoadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.LOADING);

        try {
            String databasePath = (String) ModuleConfiguration.getInstance().getConfigValue(ConfigurationMap.Keys.DATABASE_PATH);
            if (databasePath == null) {
                setModuleState(ModuleState.ERROR);
                throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: the database path provided in configuration is null)");
            }

            File databasePathFile = new File(databasePath);
            if (databasePathFile.exists() && databasePathFile.isFile()) {
                createSQLiteConnection(databasePathFile.getAbsolutePath());
                String dbVersion = readKeyParameter(DatabaseKeyParameters.KEY_DB_VERSION);
                if (dbVersion == null || !dbVersion.equals(SUPPORTED_DATABASE_VERSION)) {
                    setModuleState(ModuleState.NOT_LOADED);
                    try {
                        if (dbConnection != null) dbConnection.close();
                    } catch (SQLException ignored) {}
                    dbConnection = null;
                    throw new ModuleException(MODULE_NAME + " Module Load: Failed! (Reason: the provided database version is \"" + dbVersion + "\" but the currently supported is \"" + SUPPORTED_DATABASE_VERSION + "\")");
                }
            } else {
                createSQLiteConnection(databasePathFile.getAbsolutePath());
                createDatabaseStructure();
            }
        } catch (ClassCastException e) {
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: the database path provided in configuration is invalid)", e);
        } catch (SQLException e) {
            setModuleState(ModuleState.ERROR);
            throw new ModuleError(MODULE_NAME + " Module Load: Failed! (Reason: an SQLException has been raised)", e);
        }

        if (!isReloading) setModuleState(ModuleState.LOADED);
        Logger.log(MODULE_NAME + " Module Load: Successful!");
    }
    @Override
    protected synchronized void unloadModule(final boolean isReloading, final boolean bypassPreliminaryChecks) throws ModuleException, ModuleError {
        Logger.log(MODULE_NAME + " Module Unload: Started!");
        if (!bypassPreliminaryChecks) moduleUnloadPreliminaryCheck(MODULE_NAME, isReloading);
        if (!isReloading) setModuleState(ModuleState.UNLOADING);

        if (dbConnection != null) {
            try {
                if (!dbConnection.isClosed()) dbConnection.close();
                dbConnection = null;
            } catch (SQLException e) {
                try {
                    if (!dbConnection.isClosed()) dbConnection.close();
                } catch (Exception ignored) {
                }
                dbConnection = null;
                setModuleState(ModuleState.ERROR);
                throw new ModuleError(MODULE_NAME + " Module Unload: Failed! (Reason: an error has occurred during connection close)", e);
            }
        }

        if (!isReloading) setModuleState(ModuleState.NOT_LOADED);
        Logger.log(MODULE_NAME + " Module Unload: Successful!");
    }

    // Internal Module Methods
    private synchronized void createSQLiteConnection(@NotNull final String databaseAbsolutePath) throws SQLException {
        String url = DB_PREFIX + databaseAbsolutePath;
        dbConnection = DriverManager.getConnection(url);
        dbConnection.setAutoCommit(true);
        Statement foreignKeysStatement = dbConnection.createStatement();
        foreignKeysStatement.execute("PRAGMA foreign_keys = ON;");
        foreignKeysStatement.close();
    }
    private synchronized void createDatabaseStructure() throws SQLException, ModuleException {
        if (dbConnection == null || dbConnection.isClosed()) {
            dbConnection = null;
            throw new ModuleException("Can't create database structure: the database connection doesn't exists or it's closed");
        }
        Scanner fileReader = new Scanner(Resource.getAsStream(Resource.SQL.DATABASE_SQL_FILEPATH), StandardCharsets.UTF_8);
        StringBuilder queryBuffer = new StringBuilder();
        String buffer;

        while (fileReader.hasNext()) {
            buffer = fileReader.nextLine();
            queryBuffer.append(buffer);
            if (buffer.endsWith(";")) {
                PreparedStatement ps = dbConnection.prepareStatement(queryBuffer.toString());
                ps.execute();
                ps.close();
                queryBuffer = new StringBuilder();
            } else {
                queryBuffer.append('\n');
            }
        }

        fileReader.close();
        writeKeyParameter(DatabaseKeyParameters.KEY_DB_VERSION, SUPPORTED_DATABASE_VERSION);
    }

    // Module Methods
    public PreparedStatement preparedStatement(@NotNull final String QUERY) throws SQLException {
        if (dbConnection != null) {
            //noinspection SqlSourceToSinkFlow
            return dbConnection.prepareStatement(QUERY);
        }
        return null;
    }
    public boolean isKeyParameterPresent(@NotNull final String KEY) throws SQLException {
        String query = "SELECT * FROM key_parameters WHERE param_key=?;";
        PreparedStatement ps = dbConnection.prepareStatement(query);
        if (ps == null) throw new SQLException("The database connection doesn't exist");
        ps.setString(1, KEY);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            ps.close();
            return true;
        } else {
            ps.close();
            return false;
        }
    }
    public void writeKeyParameter(@NotNull final String KEY, @NotNull final String VALUE) {
        String query;
        PreparedStatement ps = null;
        try {
            if (isKeyParameterPresent(KEY)) { // Update
                query = "UPDATE key_parameters SET param_value=? WHERE param_key=?;";
                ps = preparedStatement(query);
                if (ps == null) throw new SQLException("The database connection doesn't exist");
                ps.setString(1, VALUE);
                ps.setString(2, KEY);
            } else { // Insert
                query = "INSERT OR REPLACE INTO key_parameters (param_key, param_value) VALUES (?, ?);";
                ps = preparedStatement(query);
                if (ps == null) throw new SQLException("The database connection doesn't exist");
                ps.setString(1, KEY);
                ps.setString(2, VALUE);
            }
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            try {
                if (ps != null) ps.close();
            } catch (SQLException ignored) {}
            Logger.log(e);
        }
    }
    public String readKeyParameter(@NotNull final String KEY) {
        PreparedStatement ps = null;
        try {
            String query = "SELECT param_value FROM key_parameters WHERE param_key=?;";
            ps = preparedStatement(query);
            if (ps == null) throw new SQLException("The database connection doesn't exist");
            ps.setString(1, KEY);
            ResultSet result = ps.executeQuery();
            if (result.next()) {
                String value = result.getString("param_value");
                ps.close();
                return value;
            } else {
                ps.close();
                return null;
            }
        } catch (SQLException e) {
            try {
                if (ps != null) ps.close();
            } catch (SQLException ignored) {}
            Logger.log(e);
            return null;
        }
    }

    // Instance
    private static ModuleDatabase instance = null;
    private ModuleDatabase() {
        super("Database");
    }
    @NotNull
    public static ModuleDatabase getInstance() {
        if (instance == null) instance = new ModuleDatabase();
        return instance;
    }
}
