/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.serotonin.db.DaoUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.TranslatableMessageParseException;

public class BaseDao extends DaoUtils {
    /**
     * Public constructor for code that needs to get stuff from the database.
     */
    public BaseDao() {
        super(Common.databaseProxy.getDataSource());
    }

    //
    // Convenience methods for storage of booleans.
    //
    public static String boolToChar(boolean b) {
        return b ? "Y" : "N";
    }

    public static boolean charToBool(String s) {
        return "Y".equals(s);
    }

    protected void deleteInChunks(String sql, List<Integer> ids) {
        int chunk = 1000;
        for (int i = 0; i < ids.size(); i += chunk) {
            String idStr = createDelimitedList(ids, i, i + chunk, ",", null);
            ejt.update(sql + " (" + idStr + ")");
        }
    }

    //
    // XID convenience methods
    //
    protected String generateUniqueXid(String prefix, String tableName) {
        String xid = Common.generateXid(prefix);
        while (!isXidUnique(xid, -1, tableName))
            xid = Common.generateXid(prefix);
        return xid;
    }

    protected boolean isXidUnique(String xid, int excludeId, String tableName) {
        return ejt.queryForInt("select count(*) from " + tableName + " where xid=? and id<>?", new Object[] { xid,
                excludeId }) == 0;
    }

    //
    // Convenience methods for translatable messages
    //
    public static String writeTranslatableMessage(TranslatableMessage tm) {
        if (tm == null)
            return null;
        return tm.serialize();
    }

    public static TranslatableMessage readTranslatableMessage(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        if (s == null)
            return null;

        try {
            return TranslatableMessage.deserialize(s);
        }
        catch (TranslatableMessageParseException e) {
            return new TranslatableMessage("common.default", rs.getString(columnIndex));
        }
    }
}
