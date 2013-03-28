package com.serotonin.m2m2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.directwebremoting.Container;
import org.directwebremoting.create.NewCreator;
import org.directwebremoting.extend.Converter;
import org.directwebremoting.extend.ConverterManager;
import org.directwebremoting.extend.CreatorManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;//nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.joda.time.DateTimeZone;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.Controller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.db.DatabaseProxy;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.DwrConversionDefinition;
import com.serotonin.m2m2.module.DwrDefinition;
import com.serotonin.m2m2.module.EventManagerListenerDefinition;
import com.serotonin.m2m2.module.HandlerInterceptorDefinition;
import com.serotonin.m2m2.module.LicenseDefinition;
import com.serotonin.m2m2.module.Module;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.ServletDefinition;
import com.serotonin.m2m2.module.UriMappingDefinition;
import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.rt.EventManager;
import com.serotonin.m2m2.rt.RuntimeManager;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.rt.event.type.EventType;
import com.serotonin.m2m2.rt.event.type.EventTypeResolver;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.rt.maint.BackgroundProcessing;
import com.serotonin.m2m2.rt.maint.DataPurge;
import com.serotonin.m2m2.rt.maint.WorkItemMonitor;
import com.serotonin.m2m2.shared.LicenseUtils;
import com.serotonin.m2m2.util.BackgroundContext;
import com.serotonin.m2m2.util.license.InstanceLicense;
import com.serotonin.m2m2.view.DynamicImage;
import com.serotonin.m2m2.view.ImageSet;
import com.serotonin.m2m2.view.ViewGraphic;
import com.serotonin.m2m2.view.ViewGraphicLoader;
import com.serotonin.m2m2.view.chart.BaseChartRenderer;
import com.serotonin.m2m2.view.chart.ChartRenderer;
import com.serotonin.m2m2.view.text.BaseTextRenderer;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.mailingList.EmailRecipient;
import com.serotonin.m2m2.vo.mailingList.EmailRecipientResolver;
import com.serotonin.m2m2.web.OverridingWebAppContext;
import com.serotonin.m2m2.web.dwr.BaseDwr;
import com.serotonin.m2m2.web.dwr.util.BlabberBeanConverter;
import com.serotonin.m2m2.web.dwr.util.BlabberConverterManager;
import com.serotonin.m2m2.web.dwr.util.DwrClassConversion;
import com.serotonin.m2m2.web.dwr.util.ModuleDwrCreator;
import com.serotonin.m2m2.web.mvc.BlabberUrlHandlerMapping;
import com.serotonin.m2m2.web.mvc.UrlHandler;
import com.serotonin.m2m2.web.mvc.UrlHandlerController;
import com.serotonin.provider.InputStreamEPollProvider;
import com.serotonin.provider.ProcessEPollProvider;
import com.serotonin.provider.Providers;
import com.serotonin.provider.TimerProvider;
import com.serotonin.provider.impl.InputStreamEPollProviderImpl;
import com.serotonin.provider.impl.ProcessEPollProviderImpl;
import com.serotonin.timer.AbstractTimer;
import com.serotonin.timer.sync.Synchronizer;
import com.serotonin.util.StringEncrypter;
import com.serotonin.util.XmlUtilsTS;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

public class Lifecycle
  implements ILifecycle
{
  private final Log LOG = LogFactory.getLog(Lifecycle.class);
  private Server SERVER;
  private final List<Runnable> STARTUP_TASKS = new ArrayList();
  private final List<Runnable> SHUTDOWN_TASKS = new ArrayList();
  private boolean terminated;

  public synchronized void initialize(ClassLoader classLoader)
  {
    for (Module module : ModuleRegistry.getModules()) {
      module.preInitialize();
    }

    String tzId = Common.envProps.getString("timezone");
    if (!StringUtils.isEmpty(tzId)) {
      TimeZone tz = TimeZone.getTimeZone(tzId);
      if ((tz == null) || (!tz.getID().equals(tzId)))
        throw new RuntimeException("Time zone id '" + tzId + "' in env properties is not valid");
      this.LOG.info("Setting default time zone to " + tz.getID());
      TimeZone.setDefault(tz);
      DateTimeZone.setDefault(DateTimeZone.forID(tzId));
    }

    Common.timer.init(new ThreadPoolExecutor(0, 1000, 30L, TimeUnit.SECONDS, new SynchronousQueue()));

    Providers.add(TimerProvider.class, new TimerProvider()
    {
      public AbstractTimer getTimer() {
        return Common.timer;
      }
    });
    Common.JSON_CONTEXT.addResolver(new EventTypeResolver(), new Class[] { EventType.class });
    Common.JSON_CONTEXT.addResolver(new BaseChartRenderer.Resolver(), new Class[] { ChartRenderer.class });
    Common.JSON_CONTEXT.addResolver(new BaseTextRenderer.Resolver(), new Class[] { TextRenderer.class });
    Common.JSON_CONTEXT.addResolver(new EmailRecipientResolver(), new Class[] { EmailRecipient.class });

    Providers.add(InputStreamEPollProvider.class, new InputStreamEPollProviderImpl());
    Providers.add(ProcessEPollProvider.class, new ProcessEPollProviderImpl());

//    lic();
    freemarkerInitialize();
    databaseInitialize(classLoader);

    for (Module module : ModuleRegistry.getModules()) {
      module.postDatabase();
    }
    utilitiesInitialize();
    eventManagerInitialize();
    runtimeManagerInitialize();
    maintenanceInitialize();
    imageSetInitialize();
    webServerInitialize(classLoader);

    for (Module module : ModuleRegistry.getModules()) {
      module.postInitialize();
    }

    SystemEventType.raiseEvent(new SystemEventType("SYSTEM_STARTUP"), System.currentTimeMillis(), false, new TranslatableMessage("event.system.startup"));

    for (Runnable task : this.STARTUP_TASKS)
      Common.timer.execute(task);
  }

  public boolean isTerminated()
  {
    return this.terminated;
  }

  public synchronized void terminate()
  {
    if (this.terminated)
      return;
    this.terminated = true;

    for (Module module : ModuleRegistry.getModules()) {
      try {
        module.preTerminate();
      }
      catch (RuntimeException e) {
        this.LOG.error("Error in preTerminate of module '" + module.getName() + "'", e);
      }

    }

    Synchronizer sync = new Synchronizer();
    for (Runnable task : this.SHUTDOWN_TASKS)
      sync.addTask(task);
    sync.executeAndWait(Common.timer);

    if (Common.eventManager != null)
    {
      SystemEventType.raiseEvent(new SystemEventType("SYSTEM_SHUTDOWN"), System.currentTimeMillis(), false, new TranslatableMessage("event.system.shutdown"));
    }

    webServerTerminate();
    runtimeManagerTerminate();

    for (Module module : ModuleRegistry.getModules()) {
      if (module.isMarkedForDeletion()) {
        module.uninstall();
        this.LOG.info("Uninstalled module " + module.getName());

        File deleteFlag = new File(Common.MA_HOME + module.getDirectoryPath(), "DELETE");
        if (!deleteFlag.exists()) {
          try {
            FileWriter fw = new FileWriter(deleteFlag);
            fw.write("delete");
            fw.close();
          }
          catch (IOException e) {
            this.LOG.error("Unabled to create delete flag file", e);
          }
        }
      }
    }

    eventManagerTerminate();
    databaseTerminate();
    utilitiesTerminate();

    ((InputStreamEPollProvider)Providers.get(InputStreamEPollProvider.class)).terminate();
    ((ProcessEPollProvider)Providers.get(ProcessEPollProvider.class)).terminate(false);

    if (Common.timer.isInitialized()) {
      Common.timer.cancel();
      Common.timer.getExecutorService().shutdown();
    }

    for (Module module : ModuleRegistry.getModules())
      try {
        module.postTerminate();
      }
      catch (RuntimeException e) {
        this.LOG.error("Error in postTerminate of module '" + module.getName() + "'", e);
      }
  }

  public void addStartupTask(Runnable task)
  {
    this.STARTUP_TASKS.add(task);
  }

  public void addShutdownTask(Runnable task)
  {
    this.SHUTDOWN_TASKS.add(task);
  }

  private void lic() {
    StringEncrypter se = new StringEncrypter();

    System.out.println(se.decodeToString("Q2hlY2tpbmcgbGljZW5zZS4uLg=="));
    try
    {
      loadLic(se);
      ((ICoreLicense)Providers.get(ICoreLicense.class)).licenseCheck(true);
    }
    catch (RuntimeException e)
    {
      System.err.println(e.getMessage());

      throw new RuntimeException(e.getMessage());
    }

    for (Module module : ModuleRegistry.getModules())
      for (LicenseDefinition def : module.getDefinitions(LicenseDefinition.class))
        try {
          def.licenseCheck(true);
        }
        catch (Throwable e) {
          System.err.println(e.getMessage());
        }
  }

  private void loadLic(StringEncrypter se)
  {
    try
    {
      File file = new File(Common.MA_HOME, se.decodeToString("bTJtMi5saWNlbnNlLnhtbA=="));

      if (file.exists()) {
        Document doc = XmlUtilsTS.parse(file);

        InstanceLicense instanceLicense = new InstanceLicense(doc);
        if (!instanceLicense.versionMatches(Common.getMajorVersion()))
        {
          throw new RuntimeException(se.decodeToString("TGljZW5zZSBmaWxlIGNvcmUgdmVyc2lvbiBkb2VzIG5vdCBtYXRjaCBpbnN0YW5jZQ=="));
        }

        Element license = doc.getDocumentElement();

        byte[] licenseHash = LicenseUtils.calculateLicenseHash(license);

        Cipher cipher = Main.cipher();
        String signature = XmlUtilsTS.getChildElementText(license, "signature");
        if (signature != null)
          signature = signature.replaceAll("\\s+", "");
        byte[] signedBytes = Base64.decodeBase64(signature.getBytes(Common.ASCII_CS));
        byte[] signatureHash = cipher.doFinal(signedBytes);

        if (!Arrays.equals(licenseHash, signatureHash))
        {
          throw new RuntimeException(se.decodeToString("U2lnbmF0dXJlIGNoZWNrIGZhaWx1cmU="));
        }
        Common.license = instanceLicense;
      }
      else
      {
        System.out.println(se.decodeToString("TGljZW5zZSBmaWxlIG5vdCBmb3VuZA=="));
      }
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e)
    {
      throw new RuntimeException(se.decodeToString("RXJyb3IgbG9hZGluZyBsaWNlbnNlIGZpbGU="));
    }
  }

  private void freemarkerInitialize()
  {
    Configuration cfg = new Configuration();
    try
    {
      List loaders = new ArrayList();

      File override = new File(Common.MA_HOME, "overrides/ftl");
      if (override.exists()) {
        loaders.add(new FileTemplateLoader(override));
      }

      loaders.add(new FileTemplateLoader(new File(Common.MA_HOME, "ftl")));

      String path = Common.MA_HOME + "/" + "web";
      for (Module module : ModuleRegistry.getModules()) {
        if (module.getEmailTemplatesDir() != null) {
          loaders.add(0, new FileTemplateLoader(new File(path + module.getWebPath(), module.getEmailTemplatesDir())));
        }
      }

      cfg.setTemplateLoader(new MultiTemplateLoader((TemplateLoader[])loaders.toArray(new TemplateLoader[loaders.size()])));
    }
    catch (IOException e) {
      this.LOG.error("Exception defining Freemarker template directory", e);
    }

    cfg.setObjectWrapper(new DefaultObjectWrapper());
    Common.freemarkerConfiguration = cfg;
  }

  private void databaseInitialize(ClassLoader classLoader)
  {
    Common.databaseProxy = DatabaseProxy.createDatabaseProxy();
    Common.databaseProxy.initialize(classLoader);
  }

  private void databaseTerminate() {
    if (Common.databaseProxy != null)
      Common.databaseProxy.terminate();
  }

  private void utilitiesInitialize()
  {
    Common.backgroundProcessing = new BackgroundProcessing();
    Common.backgroundProcessing.initialize();

    BaseDwr.initialize();
    EventType.initialize();
    SystemEventType.initialize();
    AuditEventType.initialize();
  }

  private void utilitiesTerminate() {
    if (Common.backgroundProcessing != null) {
      Common.backgroundProcessing.terminate();
      Common.backgroundProcessing.joinTermination();
      Common.backgroundProcessing = null;
    }
  }

  private void eventManagerInitialize()
  {
    Common.eventManager = new EventManager();
    Common.eventManager.initialize();
    for (EventManagerListenerDefinition def : ModuleRegistry.getDefinitions(EventManagerListenerDefinition.class))
      Common.eventManager.addListener(def);
  }

  private void eventManagerTerminate() {
    if (Common.eventManager != null) {
      Common.eventManager.terminate();
      Common.eventManager.joinTermination();
      Common.eventManager = null;
    }
  }

  private void runtimeManagerInitialize()
  {
    Common.runtimeManager = new RuntimeManager();

    File safeFile = new File(Common.MA_HOME, "SAFE");
    boolean safe = false;
    if ((safeFile.exists()) && (safeFile.isFile()))
    {
      StringBuilder sb = new StringBuilder();
      sb.append("\r\n");
      sb.append("**********************************************************\r\n");
      sb.append("*                     NOTE                               *\r\n");
      sb.append("**********************************************************\r\n");
      sb.append("* EazyScada System is starting in safe mode. All data    *\r\n");
      sb.append("* sources, publishers, and elements applicable from      *\r\n");
      sb.append("* modules will be disabled. To disable safe mode, remove *\r\n");
      sb.append("* the SAFE file from the EazyScada System *\r\n");
      sb.append("* directory.                                             *\r\n");
      sb.append("*                                                        *\r\n");
      sb.append("* To find all objects that were automatically disabled,  *\r\n");
      sb.append("* search for Audit Events on the alarms page.            *\r\n");
      sb.append("**********************************************************");
      this.LOG.warn(sb.toString());
      safe = true;
    }
    try
    {
      if (safe)
        BackgroundContext.set("common.safeMode");
      Common.runtimeManager.initialize(safe);
    }
    catch (Exception e) {
      this.LOG.error("RuntimeManager initialization failure", e);
    }
    finally {
      if (safe)
        BackgroundContext.remove();
    }
  }

  private void runtimeManagerTerminate() {
    if (Common.runtimeManager != null) {
      Common.runtimeManager.terminate();
      Common.runtimeManager.joinTermination();
      Common.runtimeManager = null;
    }
  }

  private void maintenanceInitialize()
  {
    DataPurge.schedule();

    WorkItemMonitor.start();
    LicMonitor.start();
  }

  private void webServerInitialize(ClassLoader classLoader)
  {
    this.SERVER = new Server();

      ServerConnector conn = new ServerConnector(this.SERVER);
//    SelectChannelConnector conn = new SelectChannelConnector();
    conn.setPort(Common.envProps.getInt("web.port", 8080));
    this.SERVER.addConnector(conn);

    WebAppContext context = new OverridingWebAppContext(classLoader);
    this.SERVER.setHandler(context);

    registerServlets(context);
    try
    {
      this.SERVER.start();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    configureDwr(context);
    configureUrls(context);

    ServletHolder sh = new ServletHolder(new HttpServlet()
    {
      private static final long serialVersionUID = 1L;

      protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(403);
      }
    });
    for (Module module : ModuleRegistry.getModules())
      context.addServlet(sh, module.getWebPath() + "/classes/*");
  }

  private void registerServlets(WebAppContext context)
  {
    for (ServletDefinition def : ModuleRegistry.getDefinitions(ServletDefinition.class)) {
      ServletHolder servletHolder = new ServletHolder(def.getServlet());
      servletHolder.setInitParameters(def.getInitParameters());
      servletHolder.setInitOrder(def.getInitOrder());
      for (String pathSpec : def.getUriPatterns())
        context.addServlet(servletHolder, pathSpec);
    }
  }

  private void configureDwr(WebAppContext context)
  {
    this.LOG.debug(context + " context");
    ServletContext sc = context.getServletContext();
    this.LOG.debug(sc + "  sc  name: " + Container.class.getName());
    Container container = (Container)sc.getAttribute(Container.class.getName());
    CreatorManager creatorManager = (CreatorManager)container.getBean(CreatorManager.class.getName());

    for (String type : ModuleRegistry.getDataSourceDefinitionTypes()) {
      Class clazz = ModuleRegistry.getDataSourceDefinition(type).getDwrClass();
      if (clazz != null) {
        String js = clazz.getSimpleName();

        if (creatorManager.getCreatorNames().contains(js)) {
          this.LOG.info("Duplicate definition of DWR class ignored: " + clazz.getName());
        } else {
          NewCreator c = new NewCreator();
          c.setClass(clazz.getName());
          c.setScope("application");
          c.setJavascript(js);
          creatorManager.addCreator(js, c);
        }
      }
    }

    for (String type : ModuleRegistry.getPublisherDefinitionTypes()) {
      Class clazz = ModuleRegistry.getPublisherDefinition(type).getDwrClass();
      if (clazz != null) {
        String js = clazz.getSimpleName();

        if (creatorManager.getCreatorNames().contains(js)) {
          this.LOG.info("Duplicate definition of DWR class ignored: " + clazz.getName());
        } else {
          NewCreator c = new NewCreator();
          c.setClass(clazz.getName());
          c.setScope("application");
          c.setJavascript(js);
          creatorManager.addCreator(js, c);
        }
      }
    }

    for (DwrDefinition def : ModuleRegistry.getDefinitions(DwrDefinition.class)) {
      Class clazz = def.getDwrClass();
      if (clazz != null) {
        String js = clazz.getSimpleName();

        if (creatorManager.getCreatorNames().contains(js)) {
          this.LOG.info("Duplicate definition of DWR class ignored: " + clazz.getName());
        } else {
          ModuleDwrCreator c = new ModuleDwrCreator(def.getModule());
          c.setClass(clazz.getName());
          c.setScope("application");
          c.setJavascript(js);
          creatorManager.addCreator(js, c);
        }
      }
    }

    BlabberConverterManager converterManager = (BlabberConverterManager)container.getBean(ConverterManager.class.getName());

    for (DwrConversionDefinition def : ModuleRegistry.getDefinitions(DwrConversionDefinition.class))
      for (DwrClassConversion conversion : def.getConversions())
        try {
          Map params = new HashMap();
          String converterType = conversion.getConverterType();

          if ("bean".equals(converterType)) {
            String paramKey = null;
            List cludes = new ArrayList();

            Converter converter = converterManager.getConverterAssignableFromNoAdd(conversion.getClazz());

            if ((converter instanceof BlabberBeanConverter)) {
              converterType = "blabberBean";
              BlabberBeanConverter blab = (BlabberBeanConverter)converter;

              if ((!org.apache.commons.collections.CollectionUtils.isEmpty(blab.getExclusions())) && (conversion.getIncludes() != null)) {
                throw new RuntimeException("Class conversion '" + conversion.getClazz().getName() + "' cannot have inclusions because the overriden converter has exclusions");
              }

              if ((!org.apache.commons.collections.CollectionUtils.isEmpty(blab.getInclusions())) && (conversion.getExcludes() != null)) {
                throw new RuntimeException("Class conversion '" + conversion.getClazz().getName() + "' cannot have exclusions because the overriden converter has inclusions");
              }

              if (!org.apache.commons.collections.CollectionUtils.isEmpty(blab.getInclusions())) {
                paramKey = "include";
                cludes.addAll(blab.getInclusions());
              }
              else if (!org.apache.commons.collections.CollectionUtils.isEmpty(blab.getExclusions())) {
                paramKey = "exclude";
                cludes.addAll(blab.getExclusions());
              }
            }

            if (conversion.getIncludes() != null) {
              paramKey = "include";
              cludes.addAll(conversion.getIncludes());
            }
            else if (conversion.getExcludes() != null) {
              paramKey = "exclude";
              cludes.addAll(conversion.getExcludes());
            }

            if (paramKey != null) {
              params.put(paramKey, com.serotonin.util.CollectionUtils.implode(cludes, ","));
            }
          }
          converterManager.addConverter(conversion.getClazz().getName(), converterType, params);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
  }

  private void configureUrls(WebAppContext context)
  {
    try
    {
      ServletHolder sh = context.getServletHandler().getServlet("springDispatcher");
      DispatcherServlet servlet = (DispatcherServlet)sh.getServlet();
      BlabberUrlHandlerMapping urlMap = (BlabberUrlHandlerMapping)servlet.getWebApplicationContext().getBean("mappings");

      for (HandlerInterceptorDefinition def : ModuleRegistry.getDefinitions(HandlerInterceptorDefinition.class))
        urlMap.addInterceptor(def.getInterceptor());
      urlMap.initInterceptors();

      for (UriMappingDefinition def : ModuleRegistry.getDefinitions(UriMappingDefinition.class)) {
        String modulePath = "/modules/" + def.getModule().getName();
        String viewName = null;
        if (def.getJspPath() != null) {
          viewName = modulePath + "/" + def.getJspPath();
        }
        UrlHandler handler = def.getHandler();
        Controller controller = new UrlHandlerController(handler, modulePath, viewName);

        urlMap.registerHandler(def.getPath(), controller);
      }

      for (UrlMappingDefinition def : ModuleRegistry.getDefinitions(UrlMappingDefinition.class)) {
        String modulePath = "/modules/" + def.getModule().getName();
        String viewName = null;
        if (def.getJspPath() != null) {
          viewName = modulePath + "/" + def.getJspPath();
        }
        UrlHandler handler = def.getHandler();
        Controller controller = new UrlHandlerController(handler, modulePath, viewName);

        urlMap.registerHandler(def.getUrlPath(), controller);
      }
    }
    catch (Exception e)
    {
      BlabberUrlHandlerMapping urlMap;
      throw new RuntimeException(e);
    }
  }

  private void webServerTerminate() {
    try {
      if (this.SERVER != null)
        this.SERVER.stop();
    }
    catch (Exception e) {
      this.LOG.warn("Exception while stopping web server", e);
    }
  }

  private void imageSetInitialize()
  {
    ViewGraphicLoader loader = new ViewGraphicLoader();

    for (ViewGraphic g : loader.loadViewGraphics())
      if (g.isImageSet())
        Common.imageSets.add((ImageSet)g);
      else if (g.isDynamicImage())
        Common.dynamicImages.add((DynamicImage)g);
      else
        throw new ShouldNeverHappenException("Unknown view graphic type");
  }
}