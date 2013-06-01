
package com.serotonin.m2m2.vo.permission;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.DataPointVO;


public class DataPointAccess implements JsonSerializable {
    public static final int READ = 1;
    public static final int SET = 2;

    private static final ExportCodes ACCESS_CODES = new ExportCodes();
    static {
        ACCESS_CODES.addElement(READ, "READ", "common.access.read");
        ACCESS_CODES.addElement(SET, "SET", "common.access.set");
    }

    private int dataPointId;
    private int permission;

    public int getDataPointId() {
        return dataPointId;
    }

    public void setDataPointId(int dataPointId) {
        this.dataPointId = dataPointId;
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("dataPointXid", new DataPointDao().getDataPoint(dataPointId).getXid());
        writer.writeEntry("permission", ACCESS_CODES.getCode(permission));
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        String text = jsonObject.getString("dataPointXid");
        if (StringUtils.isBlank(text))
            throw new TranslatableJsonException("emport.error.permission.missing", "dataPointXid");

        DataPointVO dp = new DataPointDao().getDataPoint(text);
        if (dp == null)
            throw new TranslatableJsonException("emport.error.missingPoint", text);
        dataPointId = dp.getId();

        text = jsonObject.getString("permission");
        if (StringUtils.isBlank(text))
            throw new TranslatableJsonException("emport.error.missing", "permission", ACCESS_CODES.getCodeList());
        permission = ACCESS_CODES.getId(text);
        if (permission == -1)
            throw new TranslatableJsonException("emport.error.invalid", "permission", text, ACCESS_CODES.getCodeList());
    }
}
