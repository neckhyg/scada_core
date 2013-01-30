/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.m2m2.module.DatabaseSchemaDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;

/**
 * @author Matthew Lohbihler
 */
public class DBConvert {
    private static final Log LOG = LogFactory.getLog(DBConvert.class);

    private DatabaseProxy source;
    private DatabaseProxy target;

    public void setSource(DatabaseProxy source) {
        this.source = source;
    }

    public void setTarget(DatabaseProxy target) {
        this.target = target;
    }

    public void execute() throws SQLException {
        LOG.warn("Running database conversion from " + source.getType().name() + " to " + target.getType().name());

        // Create the connections
        Connection sourceConn = source.getDataSource().getConnection();
        sourceConn.setAutoCommit(true);
        Connection targetConn = target.getDataSource().getConnection();
        targetConn.setAutoCommit(false);

        List<String> tableNames = getCoreTableNames();
        for (DatabaseSchemaDefinition def : ModuleRegistry.getDefinitions(DatabaseSchemaDefinition.class))
            def.addConversionTableNames(tableNames);

        for (String tableName : tableNames)
            copyTable(sourceConn, targetConn, tableName);

        sourceConn.close();
        targetConn.close();

        LOG.warn("Completed database conversion");
    }

    private List<String> getCoreTableNames() {
        List<String> tableNames = new ArrayList<String>();
        tableNames.add("systemSettings");
        tableNames.add("users");
        tableNames.add("userComments");
        tableNames.add("mailingLists");
        tableNames.add("mailingListInactive");
        tableNames.add("mailingListMembers");
        tableNames.add("dataSources");
        tableNames.add("dataSourceUsers");
        tableNames.add("dataPoints");
        tableNames.add("dataPointUsers");
        tableNames.add("pointValues");
        tableNames.add("pointValueAnnotations");
        tableNames.add("pointEventDetectors");
        tableNames.add("events");
        tableNames.add("userEvents");
        tableNames.add("eventHandlers");
        tableNames.add("publishers");
        tableNames.add("dataPointHierarchy");
        return tableNames;
    }

    private void copyTable(Connection sourceConn, Connection targetConn, String tableName) throws SQLException {
        LOG.warn(" --> Converting table " + tableName + "...");

        // Get the source data
        Statement sourceStmt = sourceConn.createStatement();
        ResultSet rs = sourceStmt.executeQuery("select * from " + tableName);

        // Create the insert statement from the meta data of the source.
        StringBuilder sb = new StringBuilder();
        ResultSetMetaData meta = rs.getMetaData();
        int columns = meta.getColumnCount();
        sb.append("insert into ").append(tableName).append(" (");
        for (int i = 1; i <= columns; i++) {
            if (i > 1)
                sb.append(",");
            sb.append(meta.getColumnName(i));
        }
        sb.append(") values (");
        for (int i = 1; i <= columns; i++) {
            if (i > 1)
                sb.append(",");
            sb.append("?");
        }
        sb.append(")");
        String insert = sb.toString();

        // Do the inserts. Commit every now and then so that transaction logs don't get huge.
        int cnt = 0;
        int total = 0;
        int maxCnt = 1000;
        while (rs.next()) {
            PreparedStatement targetStmt = targetConn.prepareStatement(insert);
            for (int i = 1; i <= columns; i++)
                targetStmt.setObject(i, rs.getObject(i), meta.getColumnType(i));
            targetStmt.executeUpdate();

            cnt++;
            total++;
            if (cnt >= maxCnt) {
                targetConn.commit();
                cnt = 0;
            }

            targetStmt.close();
        }
        targetConn.commit();

        rs.close();
        sourceStmt.close();

        LOG.warn(" --> Finished converting table " + tableName + ". " + total + " records copied.");
    }
}
