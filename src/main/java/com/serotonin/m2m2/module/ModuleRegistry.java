
package com.serotonin.m2m2.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.NotImplementedException;
import com.serotonin.m2m2.module.MenuItemDefinition.Visibility;
import com.serotonin.m2m2.module.license.LicenseEnforcement;
import com.serotonin.m2m2.vo.User;

/**
 * The registry of all modules in an MA instance.
 * 
 */
public class ModuleRegistry {
    private static final Object LOCK = new Object();
    private static final Map<String, Module> MODULES = new LinkedHashMap<String, Module>();

    private static Map<String, DataSourceDefinition> DATA_SOURCE_DEFINITIONS;
    private static Map<String, PublisherDefinition> PUBLISHER_DEFINITIONS;
    private static Map<String, EventTypeDefinition> EVENT_TYPE_DEFINITIONS;
    private static Map<String, SystemEventTypeDefinition> SYSTEM_EVENT_TYPE_DEFINITIONS;
    private static Map<String, AuditEventTypeDefinition> AUDIT_EVENT_TYPE_DEFINITIONS;

    private static Map<MenuItemDefinition.Visibility, List<MenuItemDefinition>> MENU_ITEMS;

    private static final List<LicenseEnforcement> licenseEnforcements = new ArrayList<LicenseEnforcement>();
    private static final List<ModuleElementDefinition> preDefaults = new ArrayList<ModuleElementDefinition>();
    private static final List<ModuleElementDefinition> postDefaults = new ArrayList<ModuleElementDefinition>();

    /**
     * @return a list of all available modules in the instance.
     */
    public static List<Module> getModules() {
        return new ArrayList<Module>(MODULES.values());
    }

    /**
     * Returns the instance of the module or null if not found for the given module name.
     * 
     * @param name
     *            the name of the module
     * @return the module instance or null if not found.
     */
    public static Module getModule(String name) {
        return MODULES.get(name);
    }

    public static boolean hasModule(String name) {
        return MODULES.containsKey(name);
    }

    /**
     * Should not be used by client code.
     */
    public static void addModule(Module module) {
        MODULES.put(module.getName(), module);
    }

    //
    //
    // Data source special handling
    //
    public static DataSourceDefinition getDataSourceDefinition(String type) {
        ensureDataSourceDefinitions();
        return DATA_SOURCE_DEFINITIONS.get(type);
    }

    public static Set<String> getDataSourceDefinitionTypes() {
        ensureDataSourceDefinitions();
        return DATA_SOURCE_DEFINITIONS.keySet();
    }

    private static void ensureDataSourceDefinitions() {
        if (DATA_SOURCE_DEFINITIONS == null) {
            synchronized (LOCK) {
                if (DATA_SOURCE_DEFINITIONS == null) {
                    Map<String, DataSourceDefinition> map = new HashMap<String, DataSourceDefinition>();
                    for (Module module : MODULES.values()) {
                        for (DataSourceDefinition def : module.getDefinitions(DataSourceDefinition.class))
                            map.put(def.getDataSourceTypeName(), def);
                    }
                    DATA_SOURCE_DEFINITIONS = map;
                }
            }
        }
    }

    //
    //
    // Publisher special handling
    //
    public static PublisherDefinition getPublisherDefinition(String type) {
        ensurePublisherDefinitions();
        return PUBLISHER_DEFINITIONS.get(type);
    }

    public static Set<String> getPublisherDefinitionTypes() {
        ensurePublisherDefinitions();
        return PUBLISHER_DEFINITIONS.keySet();
    }

    private static void ensurePublisherDefinitions() {
        if (PUBLISHER_DEFINITIONS == null) {
            synchronized (LOCK) {
                if (PUBLISHER_DEFINITIONS == null) {
                    Map<String, PublisherDefinition> map = new HashMap<String, PublisherDefinition>();
                    for (Module module : MODULES.values()) {
                        for (PublisherDefinition def : module.getDefinitions(PublisherDefinition.class))
                            map.put(def.getPublisherTypeName(), def);
                    }
                    PUBLISHER_DEFINITIONS = map;
                }
            }
        }
    }

    //
    //
    // System event type special handling
    //
    public static SystemEventTypeDefinition getSystemEventTypeDefinition(String typeName) {
        ensureSystemEventTypeDefinitions();
        return SYSTEM_EVENT_TYPE_DEFINITIONS.get(typeName);
    }

    private static void ensureSystemEventTypeDefinitions() {
        if (SYSTEM_EVENT_TYPE_DEFINITIONS == null) {
            synchronized (LOCK) {
                if (SYSTEM_EVENT_TYPE_DEFINITIONS == null) {
                    Map<String, SystemEventTypeDefinition> map = new HashMap<String, SystemEventTypeDefinition>();
                    for (Module module : MODULES.values()) {
                        for (SystemEventTypeDefinition def : module.getDefinitions(SystemEventTypeDefinition.class))
                            map.put(def.getTypeName(), def);
                    }
                    SYSTEM_EVENT_TYPE_DEFINITIONS = map;
                }
            }
        }
    }

    //
    //
    // Module event type special handling
    //
    public static EventTypeDefinition getEventTypeDefinition(String eventTypeName) {
        ensureEventTypeDefinitions();
        return EVENT_TYPE_DEFINITIONS.get(eventTypeName);
    }

    private static void ensureEventTypeDefinitions() {
        if (EVENT_TYPE_DEFINITIONS == null) {
            synchronized (LOCK) {
                if (EVENT_TYPE_DEFINITIONS == null) {
                    Map<String, EventTypeDefinition> map = new HashMap<String, EventTypeDefinition>();
                    for (Module module : MODULES.values()) {
                        for (EventTypeDefinition def : module.getDefinitions(EventTypeDefinition.class))
                            map.put(def.getTypeName(), def);
                    }
                    EVENT_TYPE_DEFINITIONS = map;
                }
            }
        }
    }

    //
    //
    // Audit event type special handling
    //
    public static AuditEventTypeDefinition getAuditEventTypeDefinition(String typeName) {
        ensureAuditEventTypeDefinitions();
        return AUDIT_EVENT_TYPE_DEFINITIONS.get(typeName);
    }

    private static void ensureAuditEventTypeDefinitions() {
        if (AUDIT_EVENT_TYPE_DEFINITIONS == null) {
            synchronized (LOCK) {
                if (AUDIT_EVENT_TYPE_DEFINITIONS == null) {
                    Map<String, AuditEventTypeDefinition> map = new HashMap<String, AuditEventTypeDefinition>();
                    for (Module module : MODULES.values()) {
                        for (AuditEventTypeDefinition def : module.getDefinitions(AuditEventTypeDefinition.class))
                            map.put(def.getTypeName(), def);
                    }
                    AUDIT_EVENT_TYPE_DEFINITIONS = map;
                }
            }
        }
    }

    //
    //
    // Generic handling
    //
    public static <T extends ModuleElementDefinition> List<T> getDefinitions(Class<T> clazz) {
        List<T> defs = new ArrayList<T>();
        defs.addAll(Module.getDefinitions(preDefaults, clazz));
        for (Module module : MODULES.values())
            defs.addAll(module.getDefinitions(clazz));
        defs.addAll(Module.getDefinitions(postDefaults, clazz));
        return defs;
    }

    public static <T extends ModuleElementDefinition> T getDefinition(Class<T> clazz, boolean first) {
        List<T> defs = getDefinitions(clazz);
        if (defs.isEmpty())
            return null;
        if (first)
            return defs.get(0);
        return defs.get(defs.size() - 1);
    }

    /**
     * @return a list of all available locale names in this instance.
     */
    public static Set<String> getLocales() {
        Set<String> locales = new HashSet<String>();
        for (Module module : MODULES.values())
            locales.addAll(module.getLocales());
        return locales;
    }

    /**
     * @return a map by permissions type of all available menu items in this instance.
     */
    public static Map<MenuItemDefinition.Visibility, List<MenuItemDefinition>> getMenuItems() {
        if (MENU_ITEMS == null) {
            synchronized (LOCK) {
                if (MENU_ITEMS == null) {
                    Map<MenuItemDefinition.Visibility, List<MenuItemDefinition>> map = new HashMap<MenuItemDefinition.Visibility, List<MenuItemDefinition>>();

                    for (MenuItemDefinition mi : getDefinitions(MenuItemDefinition.class)) {
                        boolean add = true;
                        // Special handling of url mapping definitions
                        if (mi instanceof UrlMappingDefinition)
                            add = !StringUtils.isBlank(((UrlMappingDefinition) mi).getMenuKey());

                        if (add) {
                            List<MenuItemDefinition> permList = map.get(mi.getVisibility());

                            if (permList == null) {
                                permList = new ArrayList<MenuItemDefinition>();
                                map.put(mi.getVisibility(), permList);
                            }

                            permList.add(mi);
                        }
                    }

                    for (List<MenuItemDefinition> list : map.values()) {
                        Collections.sort(list, new Comparator<MenuItemDefinition>() {
                            public int compare(MenuItemDefinition m1, MenuItemDefinition m2) {
                                return m1.getOrder() - m2.getOrder();
                            }
                        });
                    }

                    MENU_ITEMS = map;
                }
            }
        }

        return MENU_ITEMS;
    }

    public static synchronized void addLicenseEnforcement(LicenseEnforcement licenseEnforcement) {
        licenseEnforcements.add(licenseEnforcement);
    }

    @SuppressWarnings("unchecked")
    public static <T extends LicenseEnforcement> List<T> getLicenseEnforcements(Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        for (LicenseEnforcement le : licenseEnforcements) {
            if (clazz.isAssignableFrom(le.getClass()))
                result.add((T) le);
        }
        return result;
    }

    static {
        // Add default definitions
        postDefaults.add(new DefaultPagesDefinition() {
            @Override
            public String getLoginPageUri(HttpServletRequest request, HttpServletResponse response) {
                return "/login.htm";
            }

            @Override
            public String getLoggedInPageUri(HttpServletRequest request, HttpServletResponse response, User user) {
//                return "/data_point_details.shtm";
                return "/index.shtm";
            }

            @Override
            public String getFirstUserLoginPageUri(HttpServletRequest request, HttpServletResponse response, User user) {
                return "/help.shtm";
            }
        });

        preDefaults.add(createMenuItemDefinition(Visibility.USER, "header.dataPoints", "green_dot_32",
                "/data_point_details.shtm"));
        preDefaults.add(createMenuItemDefinition(Visibility.USER, "header.alarms", "alarm_32", "/events.shtm"));

        preDefaults.add(createMenuItemDefinition(Visibility.DATA_SOURCE, "header.eventHandlers", "event_32",
                "/event_handlers.shtm"));
        preDefaults.add(createMenuItemDefinition(Visibility.DATA_SOURCE, "header.dataSources", "pencil_32",
                "/data_sources.shtm"));

        preDefaults.add(createMenuItemDefinition(Visibility.ADMINISTRATOR, "header.pointHierarchy", "folder_brick",
                "/point_hierarchy.shtm"));
        preDefaults.add(createMenuItemDefinition(Visibility.ADMINISTRATOR, "header.mailingLists", "mail_32",
                "/mailing_lists.shtm"));
        preDefaults.add(createMenuItemDefinition(Visibility.ADMINISTRATOR, "header.publishers", "transmit",
                "/publishers.shtm"));
        preDefaults.add(createMenuItemDefinition(Visibility.ADMINISTRATOR, "header.systemSettings", "settings_32",
                "/system_settings.shtm"));
        preDefaults
                .add(createMenuItemDefinition(Visibility.ADMINISTRATOR, "header.modules", "modules", "/modules.shtm"));
        preDefaults.add(createMenuItemDefinition(Visibility.ADMINISTRATOR, "header.emport", "emport_32", "/emport.shtm"));

        preDefaults.add(createMenuItemDefinition(Visibility.ANONYMOUS, "header.help", "help", "/help.shtm"));
    }

    static MenuItemDefinition createMenuItemDefinition(final Visibility visibility, final String textKey,
            final String png, final String href) {
        return new MenuItemDefinition() {
            @Override
            public Visibility getVisibility() {
                return visibility;
            }

            @Override
            public String getTextKey(HttpServletRequest request, HttpServletResponse response) {
                return textKey;
            }

            @Override
            public String getImagePath(HttpServletRequest request, HttpServletResponse response) {
                return "/images/icons/" + png + ".png";
            }

            @Override
            public String getImage(HttpServletRequest request, HttpServletResponse response) {
                throw new NotImplementedException();
            }

            @Override
            public String getHref(HttpServletRequest request, HttpServletResponse response) {
                return href;
            }
        };
    }
}
