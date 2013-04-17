package com.serotonin.m2m2.host;

import com.serotonin.epoll.ProcessEPoll;
import com.serotonin.epoll.ProcessEpollUtils;
import com.serotonin.provider.ProcessEPollProvider;
import com.serotonin.provider.Providers;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.jfree.util.Log;

public class DreamPlug extends Host
{
  public Host.Specificity getSpecificity()
  {
    return Host.Specificity.Device;
  }

  public boolean matches()
  {
    if (!isLinux()) {
      return false;
    }
    ProcessEPoll pep = ((ProcessEPollProvider)Providers.get(ProcessEPollProvider.class)).getProcessEPoll();
    try {
      String input = ProcessEpollUtils.getProcessInput(pep, 2000L, new String[] { "uname", "-a" });
      if ((StringUtils.contains(input, "dreamplug")) || (StringUtils.contains(input, "dgbox")))
        return true;
    }
    catch (IOException e) {
      Log.debug("Process exception: ", e);
    }

    return false;
  }

  public String guid()
  {
    if (isWindows())
      return "0-dreamplug-but-windows";
    try
    {
      return createGuidFromNixMacs(new String[] { "eth0", "eth1" });
    } catch (IOException e) {
        throw new RuntimeException(e);
    }

  }
}