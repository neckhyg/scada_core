/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.util.test;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;

/**
 * @author Matthew Lohbihler
 */
public class DummyServletContext implements ServletContext {
    public Map<String, Object> attributes = new HashMap<String, Object>();

    @Override
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public ServletContext getContext(String arg0) {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getInitParameter(String arg0) {
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return null;
    }

    public boolean setInitParameter(String s, String s2) {
        return false;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String arg0) {
        return null;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    public int getEffectiveMajorVersion() {
        return 0;
    }

    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String arg0) {
        return null;
    }

    @Override
    public String getRealPath(String arg0) {
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return null;
    }

    @Override
    public URL getResource(String arg0) {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String arg0) {
        return null;
    }

    @Override
    public Set<String> getResourcePaths(String arg0) {
        return null;
    }

    @Override
    public String getServerInfo() {
        return null;
    }

    @Override
    public Servlet getServlet(String arg0) {
        return null;
    }

    @Override
    public String getServletContextName() {
        return null;
    }

    public ServletRegistration.Dynamic addServlet(String s, String s2) {
        return null;
    }

    public ServletRegistration.Dynamic addServlet(String s, Servlet servlet) {
        return null;
    }

    public ServletRegistration.Dynamic addServlet(String s, Class<? extends Servlet> aClass) {
        return null;
    }

    public <T extends Servlet> T createServlet(Class<T> tClass) throws ServletException {
        return null;
    }

    public ServletRegistration getServletRegistration(String s) {
        return null;
    }

    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }

    public FilterRegistration.Dynamic addFilter(String s, String s2) {
        return null;
    }

    public FilterRegistration.Dynamic addFilter(String s, Filter filter) {
        return null;
    }

    public FilterRegistration.Dynamic addFilter(String s, Class<? extends Filter> aClass) {
        return null;
    }

    public <T extends Filter> T createFilter(Class<T> tClass) throws ServletException {
        return null;
    }

    public FilterRegistration getFilterRegistration(String s) {
        return null;
    }

    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }

    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
    }

    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return null;
    }

    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return null;
    }

    public void addListener(String s) {
    }

    public <T extends EventListener> void addListener(T t) {
    }

    public void addListener(Class<? extends EventListener> aClass) {
    }

    public <T extends EventListener> T createListener(Class<T> tClass) throws ServletException {
        return null;
    }

    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    public ClassLoader getClassLoader() {
        return null;
    }

    public void declareRoles(String... strings) {
    }

    @Override
    public Enumeration<String> getServletNames() {
        return null;
    }

    @Override
    public Enumeration<Servlet> getServlets() {
        return null;
    }

    @Override
    public void log(Exception arg0, String arg1) {

    }

    @Override
    public void log(String arg0, Throwable arg1) {

    }

    @Override
    public void log(String arg0) {

    }
}
