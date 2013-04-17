package com.serotonin.m2m2.host;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.util.HostUtils;
import com.serotonin.util.queue.ByteQueue;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

public abstract class Host
{
  public abstract Specificity getSpecificity();

  public abstract boolean matches();

  public abstract String guid()
    throws IOException;

  protected String createGuidFromNics()
  {
    List<NI> nis = new ArrayList<NI>();
    try
    {
      Enumeration eni = NetworkInterface.getNetworkInterfaces();
      while (eni.hasMoreElements()) {
        NetworkInterface netint = (NetworkInterface)eni.nextElement();
        NI ni = new NI();
        ni.name = netint.getName();
        try {
          ni.hwAddress = netint.getHardwareAddress();
        }
        catch (SocketException e)
        {
        }
        if ((ni.name != null) && (ni.hwAddress != null))
        {
          nis.add(ni);
        }
      }
    }
    catch (SocketException e)
    {
    }
    if (nis.isEmpty()) {
      return null;
    }

    Collections.sort(nis, new Comparator<NI>()
    {
      public int compare(NI ni1, NI ni2) {
        return ni1.name.compareTo(ni2.name);
      }
    });
    ByteQueue queue = new ByteQueue();
    for (NI ni : nis) {
      queue.push(ni.name.getBytes(Common.UTF8_CS));
      queue.push(ni.hwAddress);
    }

    UUID uuid = UUID.nameUUIDFromBytes(queue.popAll());
    return "1-" + uuid.toString();
  }

  protected boolean isWindows() {
    return HostUtils.isWindows();
  }

  protected boolean isLinux() {
    return HostUtils.isLinux();
  }

  protected String createGuidFromNixMacs(String[] ids)
    throws IOException
  {
    if (isWindows()) {
      return "";
    }
    ByteQueue queue = new ByteQueue();
    for (String id : ids) {
      queue.push(HostUtils.getHwaddr(id).getBytes(Common.UTF8_CS));
    }
    UUID uuid = UUID.nameUUIDFromBytes(queue.popAll());
    return "1-" + uuid.toString();
  }

  class NI
  {
    String name;
    byte[] hwAddress;

    NI()
    {
    }
  }

  public static enum Specificity
  {
    Device, Distro, OS, All;
  }
}