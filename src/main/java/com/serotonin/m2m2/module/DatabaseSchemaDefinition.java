/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.module;

import java.util.List;

import com.serotonin.db.spring.ExtendedJdbcTemplate;

/**
 * A database schema definition allows a module to create an manage database tables and other objects as necessary to
 * perform its functionality.
 * 
 * IMPORTANT: any tables with foreign keys into core tables MUST have an "on delete cascade" clause. FKs that reference
 * non-PK fields MUST also have an "on update cascade" clause. Failure to do this will result in esoteric error messages
 * presented to users, and the final blame for such being assigned to your module. Failure to fix such conditions will
 * result in bad module karma, if not outright module removal.
 * 
 * @author Matthew Lohbihler
 */
abstract public class DatabaseSchemaDefinition extends ModuleElementDefinition {
    /**
     * Provides the module an opportunity to check if it is a new installation (typically by checking if a table that it
     * uses exists or not). Modules should perform any required installation tasks at this time.
     * 
     * @param ejt
     *            the JDBC template that provides access to the database
     */
    abstract public void newInstallationCheck(ExtendedJdbcTemplate ejt);

    /**
     * Modules should add all table names that they manage to the given list. The names are used to perform conversions
     * between one type of database (e.g. Derby) and another (e.g. MySQL).
     * 
     * @param tableNames
     *            the list of table name to add to
     */
    abstract public void addConversionTableNames(List<String> tableNames);

    /**
     * The Java package in which upgrade classes can be found. An upgrade class must be provided whenever the "code
     * version" (see below) changes, and must be named Upgrade&lt;version&gt;, where &lt;version&gt; is the version
     * <b>from which</b> the module is being upgraded. (For example, Upgrade1 will upgrade version 1 to the next version
     * - presumably, but not necessarily, 2.) Upgrade classes extend the DBUpgrade class.
     * 
     * @return the package name where upgrade classes can be found
     */
    abstract public String getUpgradePackage();

    /**
     * The version of the database schema that the current code requires. This is compared with the version stored in
     * the database - which represents the version of the schema - and determines whether the database needs to be
     * upgraded. This is separated from the version of the module because a module upgrade often does not require
     * database changes.
     * 
     * ONLY POSITIVE NUMBERS should be used as version numbers. The recommendation is to start at 1 and increase from
     * there.
     * 
     * @return the database schema version number required by the current code.
     */
    abstract public int getDatabaseSchemaVersion();
}
