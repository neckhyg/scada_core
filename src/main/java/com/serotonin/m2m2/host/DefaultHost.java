package com.serotonin.m2m2.host;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public class DefaultHost extends Host
{
  public Host.Specificity getSpecificity()
  {
    return Host.Specificity.All;
  }

  public boolean matches()
  {
    return true;
  }

  public String guid() throws IOException
  {
    File maFile = new File(System.getProperty("user.home"), ".ma");
    String id = null;

    Properties ids = new Properties();
    if (maFile.exists()) {
      FileReader reader;
      try { reader = new FileReader(maFile);
      }
      catch (FileNotFoundException e)
      {
        throw new ShouldNeverHappenException(e);
      }
      ids.load(reader);
      reader.close();

      String userDir = System.getProperty("user.dir");
      if (!StringUtils.equals(userDir, Common.MA_HOME)) {
        String guid = (String)ids.remove(userDir);
        if (guid != null) {
          ids.put(Common.MA_HOME, guid);

          FileWriter writer = new FileWriter(maFile);
          ids.store(writer, null);
          writer.close();
        }
      }

      id = ids.getProperty(Common.MA_HOME);
    }

    if (StringUtils.isBlank(id))
    {
      id = "2-" + UUID.randomUUID().toString();

      ids.setProperty(Common.MA_HOME, id);

      FileWriter writer = new FileWriter(maFile);
      ids.store(writer, null);
      writer.close();
    }

    return id;
  }
}