package com.serotonin.m2m2.web.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;


abstract public class BaseInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected int getIntRequestParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String value = request.getParameter(paramName);
        try {
            if (value != null)
                return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            // no op
        }
        return defaultValue;
    }
}
