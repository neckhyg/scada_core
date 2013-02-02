/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.eazytec.vo.flexwatchlist;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.json.JsonException;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.module.EmportDefinition;
import com.serotonin.m2m2.web.dwr.emport.ImportContext;

public class FlexWatchListEmportDefinition extends EmportDefinition {
    private FlexWatchListDao watchListDao;

    @Override
    public void postInitialize() {
        super.postInitialize();
        watchListDao = new FlexWatchListDao();
    }

    @Override
    public String getElementId() {
        return "watchLists";
    }

    @Override
    public String getDescriptionKey() {
        return "header.watchLists";
    }

    @Override
    public Object getExportData() {
        List<FlexWatchList> wls = watchListDao.getWatchLists();
        watchListDao.populateWatchlistData(wls);
        return wls;
    }

    @Override
    public void doImport(JsonValue jsonValue, ImportContext importContext) throws JsonException {
        JsonObject watchListJson = jsonValue.toJsonObject();

        String xid = watchListJson.getString("xid");
        if (StringUtils.isBlank(xid))
            xid = watchListDao.generateUniqueXid();

        FlexWatchList watchList = watchListDao.getWatchList(xid);
        if (watchList == null) {
            watchList = new FlexWatchList();
            watchList.setXid(xid);
        }

        try {
            importContext.getReader().readInto(watchList, watchListJson);

            // Now validate it. Use a new response object so we can distinguish errors in this user from other
            // errors.
            ProcessResult watchListResponse = new ProcessResult();
            watchList.validate(watchListResponse);
            if (watchListResponse.getHasMessages())
                // Too bad. Copy the errors into the actual response.
                importContext.copyValidationMessages(watchListResponse, "emport.watchList.prefix", xid);
            else {
                // Sweet. Save it.
                boolean isnew = watchList.getId() == Common.NEW_ID;
                watchListDao.saveWatchList(watchList);
                importContext.addSuccessMessage(isnew, "emport.watchList.prefix", xid);
            }
        }
        catch (TranslatableJsonException e) {
            importContext.getResult().addGenericMessage("emport.watchList.prefix", xid, e.getMsg());
        }
        catch (JsonException e) {
            importContext.getResult().addGenericMessage("emport.watchList.prefix", xid,
                    importContext.getJsonExceptionMessage(e));
        }
    }
}
