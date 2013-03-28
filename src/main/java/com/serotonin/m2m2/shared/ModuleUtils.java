package com.serotonin.m2m2.shared;

import com.serotonin.validation.StringValidation;
import java.nio.charset.Charset;
import org.apache.commons.lang3.ArrayUtils;

public class ModuleUtils
{
  public static final String ASCII = "ASCII";
  public static final Charset ASCII_CS = Charset.forName("ASCII");
  public static final String UTF8 = "UTF-8";
  public static final Charset UTF8_CS = Charset.forName("UTF-8");
  public static final String DOWNLOAD_DIR = "downloads";

  public static boolean validateName(String name)
  {
    char[] allowedFirst = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    char[] allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_".toCharArray();

    if (!StringValidation.isLengthBetween(name, 3, 30)) {
      return false;
    }
    if (!ArrayUtils.contains(allowedFirst, name.charAt(0))) {
      return false;
    }
    for (int i = 1; i < name.length(); i++) {
      if (!ArrayUtils.contains(allowed, name.charAt(i))) {
        return false;
      }
    }

    return !"core".equalsIgnoreCase(name);
  }

  public static String moduleFilename(String moduleName, String fullVersionDescription)
  {
    return "m2m2-" + moduleName + "-" + fullVersionDescription + ".zip";
  }

  public static String downloadFilename(String moduleName, String fullVersionDescription) {
    return "downloads/" + moduleFilename(moduleName, fullVersionDescription);
  }

  public static abstract interface Constants
  {
    public static final String MODULE_PROPERTIES = "module.properties";
    public static final String MODULE_SIGNED = "module.signed";
    public static final String MODULE_LICENSE_TYPES = "licenseTypes.xml";
    public static final String PROP_NAME = "name";
    public static final String PROP_VERSION = "version";
    public static final String PROP_CORE_VERSION = "coreVersion";
    public static final String PROP_DESCRIPTION = "description";
    public static final String PROP_DESCRIPTION_KEY = "descriptionKey";
    public static final String PROP_VENDOR = "vendor";
    public static final String PROP_VENDOR_URL = "vendorUrl";
    public static final String PROP_CLASSES = "classes";
    public static final String PROP_LOGO = "logo";
    public static final String PROP_FAVICON = "favicon";
    public static final String PROP_STYLES = "styles";
    public static final String PROP_SCRIPTS = "scripts";
    public static final String PROP_JSPFS = "jspfs";
    public static final String PROP_DOCUMENTATION = "documentation";
    public static final String PROP_LOCALES = "locales";
    public static final String PROP_TAGDIR = "tagdir";
    public static final String PROP_GRAPHICS = "graphics";
    public static final String PROP_LOAD_ORDER = "loadOrder";
    public static final String MODULE_NOTES = "RELEASE-NOTES";
    public static final String PROP_DEPENDENCIES = "dependencies";
    public static final String PROP_EMAIL_TEMPLATES = "emailTemplates";
    public static final String PROP_PERMISSIONS = "permissions";
    public static final String PROP_PERSIST = "persistPaths";
  }
}