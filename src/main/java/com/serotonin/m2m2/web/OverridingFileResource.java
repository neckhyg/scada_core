package com.serotonin.m2m2.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jetty.util.resource.Resource;

/**
 * Used by the web app context to override resources using the overrides directory.
 * 
 * @author Matthew
 */
public class OverridingFileResource extends Resource {
    private final Resource override;
    private final Resource base;

    public OverridingFileResource(Resource override, Resource base) {
        this.override = override;
        this.base = base;
    }

    private OverridingFileResource(Resource override, Resource base, String path) throws MalformedURLException,
            IOException {
        this.override = override.addPath(path);
        this.base = base.addPath(path);
    }

    @Override
    public Resource addPath(String path) throws IOException, MalformedURLException {
        return new OverridingFileResource(override, base, path);
    }

    @Override
    public boolean isContainedIn(Resource r) throws MalformedURLException {
        if (override.exists())
            return override.isContainedIn(r);
        return base.isContainedIn(r);
    }

    @Override
    public void release() {
        override.release();
        base.release();
    }

    @Override
    public boolean exists() {
        if (override.exists())
            return true;
        return base.exists();
    }

    @Override
    public boolean isDirectory() {
        if (override.exists())
            return override.isDirectory();
        return base.isDirectory();
    }

    @Override
    public long lastModified() {
        if (override.exists())
            return override.lastModified();
        return base.lastModified();
    }

    @Override
    public long length() {
        if (override.exists())
            return override.length();
        return base.length();
    }

    @Override
    public URL getURL() {
        if (override.exists())
            return override.getURL();
        return base.getURL();
    }

    @Override
    public File getFile() throws IOException {
        if (override.exists())
            return override.getFile();
        return base.getFile();
    }

    @Override
    public String getName() {
        return base.getName();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (override.exists())
            return override.getInputStream();
        return base.getInputStream();
    }

    public ReadableByteChannel getReadableByteChannel() throws IOException {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException, SecurityException {
        if (override.exists())
            return override.getOutputStream();
        return base.getOutputStream();
    }

    @Override
    public boolean delete() throws SecurityException {
        if (override.exists())
            return override.delete();
        return base.delete();
    }

    @Override
    public boolean renameTo(Resource dest) throws SecurityException {
        if (override.exists())
            return override.renameTo(dest);
        return base.renameTo(dest);
    }

    @Override
    public String[] list() {
        String[] baseList = base.list();
        String[] overrideList = override.list();

        if (baseList == null || baseList.length == 0)
            return overrideList;
        if (overrideList == null || overrideList.length == 0)
            return baseList;

        Set<String> merge = new HashSet<String>();
        CollectionUtils.addAll(merge, baseList);
        CollectionUtils.addAll(merge, overrideList);

        return merge.toArray(new String[merge.size()]);
    }

    @Override
    public String toString() {
        return base.toString();
    }
}
