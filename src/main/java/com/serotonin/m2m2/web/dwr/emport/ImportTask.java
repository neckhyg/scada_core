/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.web.dwr.emport;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.json.JsonReader;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.module.EmportDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.util.BackgroundContext;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.dwr.EmportDwr;
import com.serotonin.m2m2.web.dwr.emport.importers.DataPointImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.DataSourceImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.EventHandlerImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.MailingListImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.PointHierarchyImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.PublisherImporter;
import com.serotonin.m2m2.web.dwr.emport.importers.UserImporter;
import com.serotonin.util.ProgressiveTask;

/**
 * @author Matthew Lohbihler
 */
public class ImportTask extends ProgressiveTask {
    private final ImportContext importContext;
    private final User user;

    private final List<Importer> importers = new ArrayList<Importer>();
    private final List<ImportItem> importItems = new ArrayList<ImportItem>();

    public ImportTask(JsonObject root, Translations translations, User user) {
        JsonReader reader = new JsonReader(Common.JSON_CONTEXT, root);
        this.importContext = new ImportContext(reader, new ProcessResult(), translations);
        this.user = user;

        for (JsonValue jv : nonNullList(root, EmportDwr.USERS))
            addImporter(new UserImporter(jv.toJsonObject()));
        for (JsonValue jv : nonNullList(root, EmportDwr.DATA_SOURCES))
            addImporter(new DataSourceImporter(jv.toJsonObject()));
        for (JsonValue jv : nonNullList(root, EmportDwr.DATA_POINTS))
            addImporter(new DataPointImporter(jv.toJsonObject()));
        addImporter(new PointHierarchyImporter(root.getJsonArray(EmportDwr.POINT_HIERARCHY)));
        for (JsonValue jv : nonNullList(root, EmportDwr.MAILING_LISTS))
            addImporter(new MailingListImporter(jv.toJsonObject()));
        for (JsonValue jv : nonNullList(root, EmportDwr.PUBLISHERS))
            addImporter(new PublisherImporter(jv.toJsonObject()));
        for (JsonValue jv : nonNullList(root, EmportDwr.EVENT_HANDLERS))
            addImporter(new EventHandlerImporter(jv.toJsonObject()));

        for (EmportDefinition def : ModuleRegistry.getDefinitions(EmportDefinition.class)) {
            ImportItem importItem = new ImportItem(def, root.get(def.getElementId()));
            importItems.add(importItem);
        }

        Common.timer.execute(this);
    }

    private List<JsonValue> nonNullList(JsonObject root, String key) {
        JsonArray arr = root.getJsonArray(key);
        if (arr == null)
            arr = new JsonArray();
        return arr;
    }

    private void addImporter(Importer importer) {
        importer.setImportContext(importContext);
        importer.setImporters(importers);
        importers.add(importer);
    }

    public ProcessResult getResponse() {
        return importContext.getResult();
    }

    private int importerIndex;
    private boolean importerSuccess;

    @Override
    protected void runImpl() {
        try {
            BackgroundContext.set(user);

            if (!importers.isEmpty()) {
                if (importerIndex >= importers.size()) {
                    // A run through the importers has been completed.
                    if (importerSuccess) {
                        // If there were successes with the importers and there are still more to do, run through 
                        // them again.
                        importerIndex = 0;
                        importerSuccess = false;
                    }
                    else {
                        // There are importers left in the list, but there were no successful imports in the last run
                        // of the set. So, all that is left is stuff that will always fail. Copy the validation 
                        // messages to the context for each.
                        for (Importer importer : importers)
                            importer.copyMessages();
                        importers.clear();
                        return;
                    }
                }

                // Run the next importer
                Importer importer = importers.get(importerIndex);
                try {
                    importer.doImport();
                    if (importer.success()) {
                        // The import was successful. Note the success and remove the importer from the list.
                        importerSuccess = true;
                        importers.remove(importerIndex);
                    }
                    else
                        // The import failed. Leave it in the list since the run of another importer
                        // may resolved the problem.
                        importerIndex++;
                }
                catch (Exception e) {
                    // Uh oh...
                    addException(e);
                    importers.remove(importerIndex);
                }

                return;
            }

            // Run the import items.
            try {
                for (ImportItem importItem : importItems) {
                    if (!importItem.isComplete()) {
                        importItem.importNext(importContext);
                        return;
                    }
                }

                completed = true;
            }
            catch (Exception e) {
                addException(e);
            }
        }
        finally {
            BackgroundContext.remove();
        }
    }

    private void addException(Exception e) {
        String msg = e.getMessage();
        Throwable t = e;
        while ((t = t.getCause()) != null)
            msg += ", " + importContext.getTranslations().translate("emport.causedBy") + " '" + t.getMessage() + "'";
        importContext.getResult().addGenericMessage("common.default", msg);
    }
}
