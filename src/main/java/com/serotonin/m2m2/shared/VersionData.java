package com.serotonin.m2m2.shared;

public class VersionData
{
  private final int major;
  private final int minor;
  private final int micro;

  public static String getFullString(int major, int minor, int micro)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(major).append('.');

    if (minor == -1) {
      sb.append('x');
    } else {
      sb.append(minor);
      sb.append('.');
      if (micro == -1)
        sb.append('x');
      else {
        sb.append(micro);
      }
    }
    return sb.toString();
  }

  public static int compare(VersionData vd1, VersionData vd2) {
    if (vd1.major != vd2.major) {
      return vd1.major - vd2.major;
    }
    if ((vd1.minor == -1) || (vd2.minor == -1))
      return 0;
    if (vd1.minor != vd2.minor) {
      return vd1.minor - vd2.minor;
    }
    if ((vd1.micro == -1) || (vd2.micro == -1))
      return 0;
    return vd1.micro - vd2.micro;
  }

  public VersionData(String text)
  {
    this(text, false);
  }

  public VersionData(int major, int minor, int micro) {
    this.major = major;
    this.minor = minor;
    this.micro = micro;
  }

  public VersionData(String text, boolean forceFullAndValid) {
    String[] parts = text.split("\\.");

    this.major = Integer.parseInt(parts[0]);

    if (parts.length > 1) {
      if ("x".equals(parts[1]))
        this.minor = -1;
      else
        this.minor = Integer.parseInt(parts[1]);
    }
    else {
      this.minor = -1;
    }
    if (this.minor == -1) {
      this.micro = -1;
    }
    else if (parts.length > 2) {
      if ("x".equals(parts[2]))
        this.micro = -1;
      else
        this.micro = Integer.parseInt(parts[2]);
    }
    else {
      this.micro = -1;
    }

    if ((forceFullAndValid) && (!isFullAndValid()))
      throw new RuntimeException();
  }

  public boolean isFullAndValid() {
    return (this.major >= 0) && (this.minor >= 0) && (this.micro >= 0);
  }

  public int getMajor() {
    return this.major;
  }

  public int getMinor() {
    return this.minor;
  }

  public int getMicro() {
    return this.micro;
  }

  public String getFullString() {
    return getFullString(this.major, this.minor, this.micro);
  }

  public String toString()
  {
    return getFullString();
  }
}