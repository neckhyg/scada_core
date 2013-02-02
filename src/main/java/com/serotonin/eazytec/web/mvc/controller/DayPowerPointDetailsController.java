package com.serotonin.eazytec.web.mvc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

public class DayPowerPointDetailsController extends ParameterizableViewController{
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

    	
   //     Map<String, Object> model = new HashMap<String, Object>();
        
	
        return new ModelAndView(getViewName());
    }
}
