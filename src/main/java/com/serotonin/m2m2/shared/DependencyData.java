package com.serotonin.m2m2.shared;

public class DependencyData {
    private final int major;
    private final int minor;
    private final boolean forwardCompatible;

    public DependencyData(String text) {
        if (text.endsWith("+")) {
            this.forwardCompatible = true;
            text = text.substring(0, text.length() - 1);
        } else {
            this.forwardCompatible = false;
        }
        VersionData ver = new VersionData(text);
        this.major = ver.getMajor();
        this.minor = ver.getMinor();
    }

    public DependencyData(int major, int minor, boolean forwardCompatible) {
        this.major = major;
        this.minor = minor;
        this.forwardCompatible = forwardCompatible;
    }

    public String getFullString() {
        StringBuilder sb = new StringBuilder();

        if (this.major != -1) {
            sb.append(this.major);

            if (this.minor != -1) {
                sb.append('.').append(this.minor);
            }
            if (this.forwardCompatible) {
                sb.append('+');
            }
        }
        return sb.toString();
    }

    public boolean matches(VersionData version) {
        if (this.major == -1) {
            return true;
        }
        if (this.major > version.getMajor()) {
            return false;
        }
        if (this.major < version.getMajor()) {
            return (this.minor == -1) && (this.forwardCompatible);
        }

        if (this.minor == -1) {
            return true;
        }
        if (this.minor > version.getMinor()) {
            return false;
        }
        if (this.minor < version.getMinor()) {
            return this.forwardCompatible;
        }
        return true;
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public boolean isForwardCompatible() {
        return this.forwardCompatible;
    }
}