package com.serotonin.m2m2;

import com.serotonin.m2m2.module.LicenseDefinition;
import com.serotonin.m2m2.module.Module;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.provider.Providers;
import com.serotonin.timer.FixedRateTrigger;
import com.serotonin.timer.RealTimeTimer;
import com.serotonin.timer.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LicMonitor extends TimerTask {
    private static final Log LOG = LogFactory.getLog(LicMonitor.class);
    private static final long TIMEOUT = Common.getMillis(2, 15);

    public static void start() {
        Common.timer.schedule(new LicMonitor());
    }

    private LicMonitor() {
        super(new FixedRateTrigger(TIMEOUT, TIMEOUT));
    }

    public void run(long fireTime) {
        ((ICoreLicense) Providers.get(ICoreLicense.class)).licenseCheck(false);

        for (Module module : ModuleRegistry.getModules())
            for (LicenseDefinition def : module.getDefinitions(LicenseDefinition.class))
                try {
                    def.licenseCheck(false);
                } catch (Throwable e) {
                    LOG.error(e.getMessage());
                }
    }
}