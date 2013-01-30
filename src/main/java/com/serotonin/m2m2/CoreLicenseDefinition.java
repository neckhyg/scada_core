package com.serotonin.m2m2;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.LicenseDefinition;
import com.serotonin.m2m2.rt.EventManager;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.util.license.InstanceLicense;
import com.serotonin.m2m2.util.timeout.TimeoutClient;
import com.serotonin.m2m2.util.timeout.TimeoutTask;
import com.serotonin.provider.Providers;
import com.serotonin.timer.TimerTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

public final class CoreLicenseDefinition extends LicenseDefinition
        implements ICoreLicense {
    private static final int TIMEOUT_TYPE = 2;
    private static final int TIMEOUT_PERIODS = 5;
    private String guid;
    private boolean freeMode;
    private TimerTask shutdownTask;

    public final void addLicenseErrors(List<TranslatableMessage> errors) {
        if (this.shutdownTask != null) {
            errors.add(new TranslatableMessage("modules.expired"));
        }
    }

    public final void addLicenseWarnings(List<TranslatableMessage> warnings) {
        Integer majorVersion = Common.license() == null ? null : Integer.valueOf(Common.license().getVersion());

        if ((majorVersion != null) && (majorVersion.intValue() < Common.getMajorVersion())) {
            warnings.add(new TranslatableMessage("modules.core.old", new Object[]{Integer.valueOf(Common.getMajorVersion()), majorVersion}));
        }

        if ((this.shutdownTask == null) &&
                (this.freeMode))
            warnings.add(new TranslatableMessage("modules.freeMode"));
    }

    public void licenseCheck(boolean initialization) {
        if (initialization)
            initializeLic();
        else
            check();
    }

    public String getGuid() {
        return this.guid;
    }

    public boolean isFreeMode() {
        return this.freeMode;
    }

    private void initializeLic() {
        try {
            this.guid = guid();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (this.guid == null) {
            throw new RuntimeException("Unable to determine the machine id");
        }
        String freeReason = null;

        if (Common.license() == null)
            freeReason = "modules.event.freeMode.reason.missingLicense";
        else if (!this.guid.equals(Common.license().getGuid()))
            freeReason = "modules.event.freeMode.reason.guidMismatch";
        else if (Common.getMajorVersion() != Common.license().getVersion()) {
            freeReason = "modules.event.freeMode.reason.versionMismatch";
        }

        if (freeReason != null) {
            this.freeMode = true;

            final String finalReason = freeReason;
            ((ILifecycle) Providers.get(ILifecycle.class)).addStartupTask(new Runnable() {
                public void run() {
                    SystemEventType.raiseEvent(CoreLicenseDefinition.this.getEventType(),
                            System.currentTimeMillis(),
                            true,
                            new TranslatableMessage("modules.event.freeMode", new Object[]{new TranslatableMessage(finalReason)}));
                }
            });
            ((ILifecycle) Providers.get(ILifecycle.class)).addShutdownTask(new Runnable() {
                public void run() {
                    Common.eventManager.returnToNormal(CoreLicenseDefinition.this.getEventType(), System.currentTimeMillis(), 4);
                }
            });
        }
    }

    private void check() {
    }

    void startTimer(String messageKey) {
        long timeout = Common.getMillis(2, 5);
        this.shutdownTask = new TimeoutTask(timeout, new TimeoutClient() {
            public void scheduleTimeout(long fireTime) {
                ((ILifecycle) Providers.get(ILifecycle.class)).terminate();
            }
        });
        TranslatableMessage m = new TranslatableMessage(messageKey, new Object[]{Common.getPeriodDescription(2, 5)});

        SystemEventType.raiseEvent(getEventType(), System.currentTimeMillis(), true, m);
    }

    SystemEventType getEventType() {
        return new SystemEventType("LICENSE_CHECK", 0, 3);
    }

    private String guid() throws IOException {
        File maFile = new File(System.getProperty("user.home"), ".ma");
        String id = null;

        Properties ids = new Properties();
        if (maFile.exists()) {
            FileReader reader;
            try {
                reader = new FileReader(maFile);
            } catch (FileNotFoundException e) {
                throw new ShouldNeverHappenException(e);
            }
            ids.load(reader);
            reader.close();

            String userDir = System.getProperty("user.dir");
            if (!StringUtils.equals(userDir, Common.M2M2_HOME)) {
                String guid = (String) ids.remove(userDir);
                if (guid != null) {
                    ids.put(Common.M2M2_HOME, guid);

                    FileWriter writer = new FileWriter(maFile);
                    ids.store(writer, null);
                    writer.close();
                }
            }

            id = ids.getProperty(Common.M2M2_HOME);
        }

        if (StringUtils.isBlank(id)) {
            id = "2-" + UUID.randomUUID().toString();

            ids.setProperty(Common.M2M2_HOME, id);

            FileWriter writer = new FileWriter(maFile);
            ids.store(writer, null);
            writer.close();
        }

        return id;
    }
}