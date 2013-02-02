package com.serotonin.eazytec.web.mvc.controller;


import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class HourPowerPointDetailsController extends ParameterizableViewController{
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

    	
   //     Map<String, Object> model = new HashMap<String, Object>();
    	Map<String, Object> model = new HashMap<String, Object>();
    	String projectName="web"; 	
	    String nowpath;             //当前tomcat的bin目录的路径 如 D:\java\software\apache-tomcat-6.0.14\bin   
	    String tempdir; 
	    nowpath=System.getProperty("user.dir");   
	    tempdir=nowpath.replace("bin", "webapps");   
	   // tempdir+="\\"+projectName;  
	  
	//  return tempdir;    	
    	 model.put("modulePath", tempdir);	
        return new ModelAndView(getViewName(),model);
    }
}
