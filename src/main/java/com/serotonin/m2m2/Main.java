package com.serotonin.m2m2;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.crypto.Cipher;

import com.serotonin.m2m2.i18n.Translations;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.epoll.BufferingHandler;
import com.serotonin.epoll.ProcessEPoll;
import com.serotonin.epoll.ProcessHandler;
import com.serotonin.io.StreamUtils;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.Module;
import com.serotonin.m2m2.module.ModuleElementDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.shared.DependencyData;
import com.serotonin.m2m2.shared.ModuleUtils;
import com.serotonin.m2m2.shared.VersionData;
import com.serotonin.m2m2.util.HostUtils;
import com.serotonin.provider.Providers;
import com.serotonin.util.SerializationHelper;
import com.serotonin.util.StringEncrypter;
import com.serotonin.util.properties.ReloadingProperties;
import com.serotonin.util.queue.ByteQueue;

public class Main
{
  static final Log LOG = LogFactory.getLog(Main.class);
  static final StringEncrypter SE = new StringEncrypter();

    /**
     *
     * @param args
     * @throws Exception
     */
  public static void main(String[] args) throws Exception {
    Providers.add(ICoreLicense.class, new CoreLicenseDefinition());

    Common.MA_HOME = System.getProperty("ma.home");
    Common.M2M2_HOME = Common.MA_HOME;

    new File(Common.MA_HOME, "RESTART").delete();

    Common.envProps = new ReloadingProperties("env");

    openZipFiles();
    ClassLoader moduleClassLoader = loadModules();

    Lifecycle lifecycle = new Lifecycle();
    Providers.add(ILifecycle.class, lifecycle);

    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run() {
        ((ILifecycle)Providers.get(ILifecycle.class)).terminate();
      }
    });
    try {
      lifecycle.initialize(moduleClassLoader);
      if ((!GraphicsEnvironment.isHeadless()) && (Desktop.isDesktopSupported()) && (Common.envProps.getBoolean("web.openBrowserOnStartup")))
      {
        Desktop.getDesktop().browse(new URI(new StringBuilder().append("http://localhost:").append(Common.envProps.getInt("web.port", 8080)).toString()));
      }
    } catch (Exception e) {
      LOG.error("Error during initialization", e);
      lifecycle.terminate();
    }
  }

  private static void openZipFiles() throws Exception {
    ProcessEPoll pep = new ProcessEPoll();
    try {
      new Thread(pep).start();

      File[] zipFiles = new File(new StringBuilder().append(Common.MA_HOME).append("/").append("web").append("/").append("modules").toString()).listFiles(new FilenameFilter()
      {
        public boolean accept(File dir, String name)
        {
          return name.endsWith(".zip");
        }
      });
      if ((zipFiles == null) || (zipFiles.length == 0))
        return;
      for (File file : zipFiles) {
        if (!file.isFile())
        {
          continue;
        }
        ZipFile zip = new ZipFile(file);
        try
        {
          Properties props = getProperties(zip);

          String moduleName = props.getProperty("name");
          if (moduleName == null) {
            throw new RuntimeException("name not defined in module properties");
          }
          if (!ModuleUtils.validateName(moduleName)) {
            throw new RuntimeException(new StringBuilder().append("Module name '").append(moduleName).append("' is invalid").toString());
          }
          File moduleDir = new File(new StringBuilder().append(Common.MA_HOME).append("/").append("web").append("/").append("modules").toString(), moduleName);

          if (moduleDir.exists())
            LOG.info(new StringBuilder().append("Upgrading module ").append(moduleName).toString());
          else {
            LOG.info(new StringBuilder().append("Installing module ").append(moduleName).toString());
          }
          String persistDirs = props.getProperty("persistPaths");
          File moduleSaveDir = new File(new StringBuilder().append(moduleDir).append(".save").toString());

          if (!org.apache.commons.lang3.StringUtils.isBlank(persistDirs))
          {
            String[] paths = persistDirs.split(",");
            for (String path : paths) {
              path = path.trim();
              if (!org.apache.commons.lang3.StringUtils.isBlank(path)) {
                File from = new File(moduleDir, path);
                File to = new File(moduleSaveDir, path);
                if (from.exists()) {
                  if (from.isDirectory())
                    moveDir(from, to);
                  else {
                    FileUtils.moveFile(from, to);
                  }
                }
              }
            }
          }

          deleteDir(moduleDir);

          if (moduleSaveDir.exists())
            moveDir(moduleSaveDir, moduleDir);
          else {
            moduleDir.mkdirs();
          }
          Enumeration entries = zip.entries();
          while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry)entries.nextElement();
            String name = entry.getName();
            File entryFile = new File(moduleDir, name);

            if (entry.isDirectory())
              entryFile.mkdirs();
            else {
              writeFile(entryFile, zip.getInputStream(entry));
            }
          }

          File moduleWorkDir = new File(moduleDir, "work");
          if (moduleWorkDir.exists()) {
            moveDir(moduleWorkDir, new File(Common.MA_HOME, "work"));
          }

          if (HostUtils.isLinux()) {
            String permissions = props.getProperty("permissions");
            if (!org.apache.commons.lang3.StringUtils.isBlank(permissions)) {
              String[] s = permissions.split(",");
              for (String permission : s) {
                setPermission(pep, moduleName, moduleDir, permission);
              }

            }

          }

          zip.close();
        }
        catch (Exception e)
        {
          LOG.warn(new StringBuilder().append("Error while opening zip file ").append(file.getName()).append(". Is this module built for the core version that you are using? Module ignored.").toString(), e);
        }
        finally
        {
          zip.close();
        }
      }

    }
    finally
    {
      pep.waitForAll();
      pep.terminate();
    }
  }

  private static void setPermission(ProcessEPoll pep, String moduleName, File moduleDir, String permStr)
  {
    int pipe = permStr.indexOf(124);
    if (pipe == -1) {
      LOG.warn(new StringBuilder().append("Malformed permission string in module ").append(moduleName).append(": ").append(permStr).toString());
    } else {
      String mode = permStr.substring(0, pipe).trim();
      final String fileStr = permStr.substring(pipe + 1).trim();

      File file = new File(moduleDir, fileStr);
      if (!file.exists())
        LOG.warn(new StringBuilder().append("Can't set permissions in module ").append(moduleName).append(" on file ").append(fileStr).append(", file does not exist").toString());
      else
        try
        {
        	//moduleName, fileStr
        	final String _moduleName = moduleName;
          pep.add(new ProcessBuilder(new String[] { "chmod", mode, file.getPath() }), 3000L, new BufferingHandler()
          {
            public void done(ProcessHandler.DoneCause cause, int exitValue, Exception e) {
              if (cause != ProcessHandler.DoneCause.FINISHED) {
                Main.LOG.warn("Error setting permissions in module " + _moduleName + " on file " + fileStr + ", bad finish: " + cause, e);
              }
              if (exitValue != 0) {
                Main.LOG.warn("Error setting permissions in module " + _moduleName + " on file " + fileStr + ", bad exit value: " + exitValue);
              }
              if (!org.apache.commons.lang3.StringUtils.isBlank(getInput())) {
                Main.LOG.warn("Error setting permissions in module " + _moduleName + " on file " + fileStr + ", " + getInput());
              }
              if (!org.apache.commons.lang3.StringUtils.isBlank(getError()))
                Main.LOG.warn("Error setting permissions in module " + _moduleName + " on file " + fileStr + ", " + getError());
            }
          });
        }
        catch (IOException e) {
          LOG.warn(new StringBuilder().append("Error seting permissions in module ").append(moduleName).append(" on file ").append(fileStr).toString(), e);
        }
    }
  }

  private static ClassLoader loadModules()
    throws Exception
  {
    Common.documentationManifest.parseManifestFile("web/WEB-INF/dox");

    File modulesPath = new File(new StringBuilder().append(Common.MA_HOME).append("/").append("web").append("/").append("modules").toString());
    File[] modules = modulesPath.listFiles();
    if (modules == null) {
      modules = new File[0];
    }
    List classLoaderUrls = new ArrayList();
    Map<Module, List<String>> moduleClasses = new HashMap<Module, List<String>>();

    VersionData coreVersion = Common.getVersion();

    List<ModuleWrapper> moduleWrappers = new ArrayList();
    for (File moduleDir : modules) {
      if (!moduleDir.isDirectory()) {
        continue;
      }
      if (new File(moduleDir, "DELETE").exists())
      {
        deleteDir(moduleDir);

        LOG.info(new StringBuilder().append("Deleted module directory ").append(moduleDir).toString());
      }
      else
      {
        Properties props = null;
        try {
          props = getProperties(moduleDir);
        }
        catch (ModulePropertiesException e) {
          LOG.warn(new StringBuilder().append("Error loading properties for module ").append(moduleDir.getPath()).toString(), e.getCause());
        }
        if (props == null) {
          continue;
        }
        String moduleName = props.getProperty("name");
        if (moduleName == null) {
          throw new RuntimeException(new StringBuilder().append("Module ").append(moduleDir.getPath()).append(": ").append("name").append(" not defined in module properties").toString());
        }
        if (!ModuleUtils.validateName(moduleName)) {
          throw new RuntimeException(new StringBuilder().append("Module ").append(moduleDir.getPath()).append(": ").append("name").append(" has an invalid name: ").append(moduleName).toString());
        }

        String version = props.getProperty("version");
        if (version == null) {
          throw new RuntimeException(new StringBuilder().append("Module ").append(moduleName).append(": ").append("version").append(" not defined in module properties").toString());
        }

        String moduleCoreVersion = props.getProperty("coreVersion");
        if (moduleCoreVersion == null) {
          throw new RuntimeException(new StringBuilder().append("Module ").append(moduleName).append(": ").append("coreVersion").append(" not defined in module properties").toString());
        }

        DependencyData moduleCoreDependency = new DependencyData(moduleCoreVersion);
        if (!moduleCoreDependency.matches(coreVersion)) {
          LOG.warn(new StringBuilder().append("Module ").append(moduleName).append(": this module requires a core version of ").append(moduleCoreVersion).append(", which does not match the current core version of ").append(coreVersion.getFullString()).append(". Module not loaded.").toString());
        }
        else
        {
          String descriptionKey = props.getProperty("descriptionKey");
          TranslatableMessage description = null;
          if (org.apache.commons.lang3.StringUtils.isBlank(descriptionKey)) {
            String desc = props.getProperty("description");
            if (!org.apache.commons.lang3.StringUtils.isBlank(desc))
              description = new TranslatableMessage("common.default", new Object[] { desc });
          }
          else {
            description = new TranslatableMessage(descriptionKey);
          }
          String vendor = props.getProperty("vendor");
          String vendorUrl = props.getProperty("vendorUrl");
          String dependencies = props.getProperty("dependencies");
          String loadOrderStr = props.getProperty("loadOrder");

          int loadOrder = 50;
          if (!org.apache.commons.lang3.StringUtils.isBlank(loadOrderStr)) {
            try {
              loadOrder = Integer.parseInt(loadOrderStr);
            }
            catch (Exception e) {
              loadOrder = -1;
            }

            if ((loadOrder < 1) || (loadOrder > 100)) {
              LOG.warn(new StringBuilder().append("Module ").append(moduleName).append(": bad loadOrder value '").append(loadOrderStr).append("', must be a number between 1 and 100. Defaulting to 50").toString());

              loadOrder = 50;
            }
          }

          Module module = new Module(moduleName, version, description, vendor, vendorUrl, dependencies, loadOrder);
          moduleWrappers.add(new ModuleWrapper(module, props, moduleDir));
        }
      }
    }
    Collections.sort(moduleWrappers, new Comparator<ModuleWrapper>()
    {
      public int compare(ModuleWrapper m1, ModuleWrapper m2) {
        return m1.module.getLoadOrder() - m2.module.getLoadOrder();
      }
    });
    for (ModuleWrapper moduleWrapper : moduleWrappers) {
      Module module = moduleWrapper.module;
      String moduleName = moduleWrapper.module.getName();
      String version = moduleWrapper.module.getVersion();
      String vendor = moduleWrapper.module.getVendor();
      Properties props = moduleWrapper.props;
      File moduleDir = moduleWrapper.moduleDir;

      ModuleRegistry.addModule(module);

      LOG.info(new StringBuilder().append("Loading module '").append(moduleName).append("', v").append(version).append(" by ").append(vendor == null ? "(unknown vendor)" : vendor).toString());

      String classes = props.getProperty("classes");
      if (!org.apache.commons.lang3.StringUtils.isBlank(classes)) {
        String[] parts = classes.split(",");
        for (String className : parts) {
          if (!org.apache.commons.lang3.StringUtils.isBlank(className)) {
            className = className.trim();
            List classNames = (List)moduleClasses.get(module);
            if (classNames == null) {
              classNames = new ArrayList();
              moduleClasses.put(module, classNames);
            }
            classNames.add(className);
          }
        }

      }

      File classesDir = new File(moduleDir, "classes");
      if (classesDir.exists()) {
        classLoaderUrls.add(classesDir.toURI().toURL());
      }

      loadLib(moduleDir, classLoaderUrls);

      String logo = props.getProperty("logo");
      if (!org.apache.commons.lang3.StringUtils.isBlank(logo)) {
        Common.applicationLogo = new StringBuilder().append("/modules/").append(moduleName).append("/").append(logo).toString();
      }

      String favicon = props.getProperty("favicon");
      if (!org.apache.commons.lang3.StringUtils.isBlank(favicon)) {
        Common.applicationFavicon = new StringBuilder().append("/modules/").append(moduleName).append("/").append(favicon).toString();
      }

      String styles = props.getProperty("styles");
      if (!org.apache.commons.lang3.StringUtils.isBlank(styles)) {
        for (String style : styles.split(",")) {
          style = com.serotonin.util.StringUtils.trimWhitespace(style);
          if (!org.apache.commons.lang3.StringUtils.isBlank(style)) {
            Common.moduleStyles.add(new StringBuilder().append("modules/").append(moduleName).append("/").append(style).toString());
          }
        }
      }

      String scripts = props.getProperty("scripts");
      if (!org.apache.commons.lang3.StringUtils.isBlank(scripts)) {
        for (String script : scripts.split(",")) {
          script = com.serotonin.util.StringUtils.trimWhitespace(script);
          if (!org.apache.commons.lang3.StringUtils.isBlank(script)) {
            Common.moduleScripts.add(new StringBuilder().append("modules/").append(moduleName).append("/").append(script).toString());
          }
        }
      }

      String jspfs = props.getProperty("jspfs");
      if (!org.apache.commons.lang3.StringUtils.isBlank(jspfs)) {
        for (String jspf : jspfs.split(",")) {
          jspf = com.serotonin.util.StringUtils.trimWhitespace(jspf);
          if (!org.apache.commons.lang3.StringUtils.isBlank(jspf)) {
            Common.moduleJspfs.add(new StringBuilder().append("/modules/").append(moduleName).append("/").append(jspf).toString());
          }
        }
      }

      String dox = props.getProperty("documentation");
      if (!org.apache.commons.lang3.StringUtils.isBlank(dox)) {
        Common.documentationManifest.parseManifestFile(new StringBuilder().append("web/modules/").append(moduleName).append("/").append(dox).toString());
      }

      String locales = props.getProperty("locales");
      if (!org.apache.commons.lang3.StringUtils.isBlank(locales)) {
        String[] s = locales.split(",");
        for (String locale : s) {
          module.addLocaleDefinition(locale.trim());
        }
      }

      String tagdir = props.getProperty("tagdir");
      if (!org.apache.commons.lang3.StringUtils.isBlank(tagdir)) {
        File from = new File(moduleDir, tagdir);
        File to = new File(new StringBuilder().append(Common.MA_HOME).append("/web/WEB-INF/tags/").append(moduleName).toString());
        deleteDir(to);

        if (from.exists()) {
          FileUtils.copyDirectory(from, to);
        }

      }

      String graphics = props.getProperty("graphics");
      if (!org.apache.commons.lang3.StringUtils.isBlank(graphics)) {
        graphics = com.serotonin.util.StringUtils.trimWhitespace(graphics);
        if (!org.apache.commons.lang3.StringUtils.isBlank(graphics)) {
          module.setGraphicsDir(graphics);
        }
      }

      String emailTemplates = props.getProperty("emailTemplates");
      if (!org.apache.commons.lang3.StringUtils.isBlank(emailTemplates)) {
        emailTemplates = com.serotonin.util.StringUtils.trimWhitespace(emailTemplates);
        if (!org.apache.commons.lang3.StringUtils.isBlank(emailTemplates)) {
          module.setEmailTemplatesDir(emailTemplates);
        }
      }
    }

    for (Module module : ModuleRegistry.getModules()) {
      String dependenciesStr = module.getDependencies();
      if (!org.apache.commons.lang3.StringUtils.isBlank(dependenciesStr)) {
        String[] parts = dependenciesStr.split(",");

        for (String dependencyStr : parts)
        {
          DependencyData depVer = null;

          dependencyStr = dependencyStr.trim();
          int pos = dependencyStr.lastIndexOf(45);
          String depName;
          if (pos == -1)
          {
            depName = dependencyStr;
          } else {
            depName = dependencyStr.substring(0, pos);

            String ver = dependenciesStr.substring(pos + 1);
            try {
              depVer = new DependencyData(ver);
            }
            catch (Exception e) {
              throw new RuntimeException(new StringBuilder().append("Invalid dependency version in '").append(dependencyStr).append("'").toString(), e);
            }

          }

          Module depModule = ModuleRegistry.getModule(depName);
          if (depModule == null) {
            throw new RuntimeException(new StringBuilder().append("Module '").append(depName).append("' not found, but required by '").append(module.getName()).append("'").toString());
          }

          if ((depVer != null) && (!depVer.matches(new VersionData(depModule.getVersion())))) {
            throw new RuntimeException(new StringBuilder().append("Module '").append(depName).append("' has version '").append(depModule.getVersion()).append("' but module '").append(module.getName()).append("' requires version '").append(depVer.getFullString()).append("'").toString());
          }
        }
      }

    }

    URL[] arr = (URL[])classLoaderUrls.toArray(new URL[0]);
    URLClassLoader cl = new URLClassLoader(arr, Main.class.getClassLoader());
    Thread.currentThread().setContextClassLoader(cl);

    for (Map.Entry mod : moduleClasses.entrySet()) {
      try {
        for (String className : (List<String>)mod.getValue()) {
          Class clazz = cl.loadClass(className);
          boolean used = false;

          if (ModuleElementDefinition.class.isAssignableFrom(clazz)) {
            ModuleElementDefinition def = (ModuleElementDefinition)clazz.newInstance();
            ((Module)mod.getKey()).addDefinition(def);
            used = true;
          }

          if (!used)
            LOG.warn(new StringBuilder().append("Unused classes entry: ").append(className).toString());
        }
      }
      catch (Exception e) {
        throw new Exception(new StringBuilder().append("Exception loading classes in module ").append(((Module)mod.getKey()).getName()).toString(), e);
      }
    }

    return cl;
  }

  private static void loadLib(File module, List<URL> urls)
    throws Exception
  {
    File[] jarFiles = new File(module, "lib").listFiles(new FilenameFilter()
    {
      public boolean accept(File dir, String name) {
        return name.endsWith(".jar");
      }
    });
    if ((jarFiles == null) || (jarFiles.length == 0)) {
      return;
    }
    for (File file : jarFiles) {
      if (!file.isFile())
        continue;
      urls.add(file.toURI().toURL());
    }
  }

  private static void writeFile(File file, InputStream in) throws Exception {
    FileOutputStream out = new FileOutputStream(file);
    StreamUtils.transfer(in, out);
    out.close();
  }

  private static Properties getProperties(ZipFile zip) throws IOException, Main.ModulePropertiesException {
    ZipEntry propFile = zip.getEntry("module.signed");
    if (propFile != null) {
      return getProperties(zip.getInputStream(propFile), true);
    }
    propFile = zip.getEntry("module.properties");
    if (propFile == null) {
      throw new RuntimeException("module.properties not found in module zip file");
    }
    return getProperties(zip.getInputStream(propFile), false);
  }

  private static Properties getProperties(File moduleDir) throws IOException, Main.ModulePropertiesException
  {
    File signed = new File(moduleDir, "module.signed");
    if (signed.exists()) {
      FileInputStream fis = new FileInputStream(signed);
      Properties props = getProperties(fis, true);
//        File newFile = new File(moduleDir,"module.properties");
//        FileOutputStream fos = new FileOutputStream(newFile);
//        props.store(fos,"");
//        fos.close();
      fis.close();
      return props;
    }

    File propFile = new File(moduleDir, "module.properties");
    if (!propFile.exists()) {
      LOG.warn(new StringBuilder().append("Module ").append(moduleDir.getPath()).append(": ").append("module.properties").append(" not found in module directory. Module not loaded.").toString());

      return null;
    }

    FileInputStream fis = new FileInputStream(propFile);
    Properties props = getProperties(fis, false);
    fis.close();
    return props;
  }

  private static Properties getProperties(InputStream in, boolean signed) throws IOException, Main.ModulePropertiesException
  {
    if (signed) {
      try {
        Cipher cipher = cipher();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        ByteQueue queue = new ByteQueue();
        String morsel;
        while ((morsel = reader.readLine()) != null) {
          byte[] b = Base64.decodeBase64(morsel.getBytes(Common.ASCII_CS));
          b = cipher.doFinal(b);
          queue.push(b);
        }

        in = new ByteArrayInputStream(queue.popAll());
      }
      catch (Exception e) {
        throw new ModulePropertiesException(e);
      }
    }

    Properties props = new Properties();
    props.load(in);

    return props;
  }

  static Cipher cipher()
    throws Exception
  {
    String s = "rO0ABXNyABRqYXZhLnNlY3VyaXR5LktleVJlcL35T7OImqVDAgAETAAJYWxnb3JpdGhtdAASTGphdmEvbGFuZy9TdHJpbmc7WwAHZW5jb2RlZHQAAltCTAAGZm9ybWF0cQB+AAFMAAR0eXBldAAbTGphdmEvc2VjdXJpdHkvS2V5UmVwJFR5cGU7eHB0AANSU0F1cgACW0Ks8xf4BghU4AIAAHhwAAACJjCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAITXG2VdD46nNDJjDXHdh9IHL8nXU48MLTHKE5o5JBHqrG3nBHE4QTEIJLCYF4wa8cFK4lgL815PB5/QsBeCKE8JKJGIFsGczoC8D3S2jGAevlHyUhrB+j0Lu1IpYAkGc/CoUNv7KVpobB2DP8ORjt9NYoAtF2uopHQjesxLwjaDfiqeMmvBlxVaFi5IdegDU/7hoMaJo0ody9fJoNV1KrNnaKQ2V33AQAQia/rpf/pMyZwJ5ifA7s9TojyLWNsrzLuym+05Hi1gxhASRZbZE/s22rQSduc+8FVkwjxiSeM21aVKZ8bf7RrZrxjcpUBES7WvSQpJKhpxKvNkoqvSgE1B5FVgHlvg5akHT36Df1H7o1Qalmaig6LTaPv2/8lp6oP7kNmK6ywWlgm24764pOc4e+UR2cVNOJAv2HsU8wJpbHA4rjLsSMYE5O4I/smkU4INSP9URrXG3uomr/TM+jRrBCEsw4WW0KyAjGrdkBwExV2910iTajxEKUPbu12E25NfRmM82Fyo1EFXIQK505tRhxVGxiAunZHnWvKNlQ7IcUZzLAhMbz3Seb6LhpTcT629m4IzEwjyfxFQNiA8BAk6pv3UY/J58ZsH2ha8f34TLYyeQXK7E+8tgrvcIP9SwEylX0j/svzoZ5FBxsDmM/DPcoLV6inEff10XOQyB6VnAgMBAAF0AAVYLjUwOX5yABlqYXZhLnNlY3VyaXR5LktleVJlcCRUeXBlAAAAAAAAAAASAAB4cgAOamF2YS5sYW5nLkVudW0AAAAAAAAAABIAAHhwdAAGUFVCTElD";
    byte[] keyBytes = Base64.decodeBase64(s.getBytes(Common.ASCII_CS));
    Key key = (Key)SerializationHelper.readObject(new ByteArrayInputStream(keyBytes));
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(2, key);
    return cipher;
  }

  private static void deleteDir(File dir) throws IOException {
    String message = null;
//    while (true)
      try {
        FileUtils.deleteDirectory(dir);
      }
      catch (IOException e)
      {
        if (org.apache.commons.lang3.StringUtils.equals(message, e.getMessage()))
          throw e;
        message = e.getMessage();
      }
  }

  private static void moveDir(File from, File to) throws IOException
  {
    FileUtils.copyDirectory(from, to);
    deleteDir(from);
  }

  static class ModulePropertiesException extends Exception
  {
    private static final long serialVersionUID = 1L;

    public ModulePropertiesException(Throwable cause)
    {
      super();
    }
  }

  static class ModuleWrapper
  {
    final Module module;
    final Properties props;
    final File moduleDir;

    public ModuleWrapper(Module module, Properties props, File moduleDir)
    {
      this.module = module;
      this.props = props;
      this.moduleDir = moduleDir;
    }
  }
}