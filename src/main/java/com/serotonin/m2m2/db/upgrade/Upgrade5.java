package com.serotonin.m2m2.db.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.serotonin.m2m2.db.DatabaseProxy;

public class Upgrade5 extends DBUpgrade {
    @Override
    public void upgrade() throws Exception {
        // Run the script.
        Map<String, String[]> scripts = new HashMap<String, String[]>();
        scripts.put(DatabaseProxy.DatabaseType.DERBY.name(), derbyScript);
        scripts.put(DatabaseProxy.DatabaseType.MYSQL.name(), mysqlScript);
        scripts.put(DatabaseProxy.DatabaseType.MSSQL.name(), mssqlScript);
        runScript(scripts);
    }

    @Override
    protected String getNewSchemaVersion() {
        return "6";
    }

    private final String[] derbyScript = { //
    "ALTER TABLE dataPoints ADD COLUMN pointFolderId int;", //

            "CREATE TABLE dataPointHierarchy (", //
            "  id int NOT NULL, ", //
            "  parentId int, ", //
            "  name varchar(100)", //
            ");", //
            "ALTER TABLE dataPointHierarchy ADD CONSTRAINT dataPointHierarchyPk PRIMARY KEY (id);", //
    };

    private final String[] mssqlScript = { //
    "ALTER TABLE dataPoints ADD COLUMN pointFolderId int;", //

            "CREATE TABLE dataPointHierarchy (", //
            "  id int NOT NULL identity, ", //
            "  parentId int, ", //
            "  name nvarchar(100), ", //
            "  PRIMARY KEY (id)", //
            ");", //
    };

    private final String[] mysqlScript = { //
    "ALTER TABLE dataPoints ADD COLUMN pointFolderId int;", //

            "CREATE TABLE dataPointHierarchy (", //
            "  id int NOT NULL auto_increment, ", //
            "  parentId int, ", //
            "  name varchar(100), ", //
            "  PRIMARY KEY (id) ", //
            ") engine=InnoDB;", //
    };
}
