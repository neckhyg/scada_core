/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;

/**
 * @author Matthew Lohbihler
 */
abstract public class BasePooledProxy extends DatabaseProxy {
    private final Log log = LogFactory.getLog(BasePooledProxy.class);
    private BasicDataSource dataSource;

    @Override
    protected void initializeImpl(String propertyPrefix) {
        log.info("Initializing pooled connection manager");
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName(getDriverClassName());
        dataSource.setUrl(getUrl(propertyPrefix));
        dataSource.setUsername(Common.envProps.getString(propertyPrefix + "db.username"));
        dataSource.setPassword(getDatabasePassword(propertyPrefix));
        dataSource.setMaxActive(Common.envProps.getInt(propertyPrefix + "db.pool.maxActive", 10));
        dataSource.setMaxIdle(Common.envProps.getInt(propertyPrefix + "db.pool.maxIdle", 10));
    }

    protected String getUrl(String propertyPrefix) {
        return Common.envProps.getString(propertyPrefix + "db.url");
    }

    abstract protected String getDriverClassName();

    @Override
    public void runScript(String[] script, OutputStream out) {
        ExtendedJdbcTemplate ejt = new ExtendedJdbcTemplate();
        ejt.setDataSource(dataSource);

        StringBuilder statement = new StringBuilder();

        for (String line : script) {
            // Trim whitespace
            line = line.trim();

            // Skip comments
            if (line.startsWith("--"))
                continue;

            statement.append(line);
            statement.append(" ");
            if (line.endsWith(";")) {
                // Execute the statement
                ejt.execute(statement.toString());
                statement.delete(0, statement.length() - 1);
            }
        }
    }

    @Override
    public void runScript(InputStream input, OutputStream out) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(input));

            List<String> lines = new ArrayList<String>();
            String line;
            while ((line = in.readLine()) != null)
                lines.add(line);

            String[] script = new String[lines.size()];
            lines.toArray(script);
            runScript(script, out);
        }
        catch (IOException ioe) {
            throw new ShouldNeverHappenException(ioe);
        }
        finally {
            try {
                if (in != null)
                    in.close();
            }
            catch (IOException ioe) {
                log.warn("", ioe);
            }
        }
    }

    @Override
    public void terminate() {
        log.info("Stopping database");
        try {
            dataSource.close();
        }
        catch (SQLException e) {
            log.warn("", e);
        }
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public File getDataDirectory() {
        return null;
    }

    @Override
    public int getActiveConnections() {
        return dataSource.getNumActive();
    }

    @Override
    public int getIdleConnections() {
        return dataSource.getNumIdle();
    }
}
