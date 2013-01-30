/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.web.mvc.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.module.AuthenticationDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.mvc.form.LoginForm;
import com.serotonin.util.ValidationUtils;

@SuppressWarnings("deprecation")
public class LoginController extends SimpleFormController {
    private String successUrl;
    private String newUserUrl;

    public void setSuccessUrl(String url) {
        successUrl = url;
    }

    public void setNewUserUrl(String newUserUrl) {
        this.newUserUrl = newUserUrl;
    }

    @Override
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors,
            @SuppressWarnings("rawtypes") Map controlModel) throws Exception {
        LoginForm loginForm = (LoginForm) errors.getTarget();

        if (!errors.hasErrors())
            checkDomain(request, errors);

        if (!errors.hasErrors()) {
            User user = null;
            for (AuthenticationDefinition def : ModuleRegistry.getDefinitions(AuthenticationDefinition.class)) {
                user = def.preLoginForm(request, response, loginForm, errors);
                if (user != null)
                    break;
            }

            if (user != null)
                return performLogin(request, user);
        }

        return super.showForm(request, response, errors, controlModel);
    }

    @Override
    protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) {
        LoginForm login = (LoginForm) command;

        checkDomain(request, errors);

        // Make sure there is a username
        if (StringUtils.isBlank(login.getUsername()))
            ValidationUtils.rejectValue(errors, "username", "login.validation.noUsername");

        // Make sure there is a password
        if (StringUtils.isBlank(login.getPassword()))
            ValidationUtils.rejectValue(errors, "password", "login.validation.noPassword");
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
            BindException errors) throws Exception {
        LoginForm login = (LoginForm) command;

        // Check if the user exists
        User user = new UserDao().getUser(login.getUsername());
        if (user == null)
            ValidationUtils.rejectValue(errors, "username", "login.validation.noSuchUser");
        else if (user.isDisabled())
            ValidationUtils.reject(errors, "login.validation.accountDisabled");
        else {
            boolean authenticated = false;

            for (AuthenticationDefinition def : ModuleRegistry.getDefinitions(AuthenticationDefinition.class)) {
                authenticated = def.authenticate(request, response, user, login.getPassword(), errors);
                if (authenticated)
                    break;
            }

            if (!authenticated) {
                String passwordHash = Common.encrypt(login.getPassword());

                // Validating the password against the database.
                if (!passwordHash.equals(user.getPassword()))
                    ValidationUtils.reject(errors, "login.validation.invalidLogin");
            }
        }

        if (errors.hasErrors())
            return showForm(request, response, errors);

        return performLogin(request, user);
    }

    private void checkDomain(HttpServletRequest request, BindException errors) {
        if (Common.license() != null && !"localhost".equals(request.getServerName())) {
            if (!ControllerUtils.getDomain(request).equals(Common.license().getDomain()))
                ValidationUtils.reject(errors, "login.validation.wrongDomain", Common.license().getDomain());
        }
    }

    private ModelAndView performLogin(HttpServletRequest request, User user) {
        if (user.isDisabled())
            throw new RuntimeException("User " + user.getUsername() + " is disabled. Aborting login");

        // Update the last login time.
        new UserDao().recordLogin(user.getId());

        // Add the user object to the session. This indicates to the rest of the application whether the user is logged 
        // in or not. Will replace any existing user object.
        Common.setUser(request, user);
        if (logger.isDebugEnabled())
            logger.debug("User object added to session");

        if (user.isFirstLogin())
            return new ModelAndView(new RedirectView(newUserUrl));
        if (!StringUtils.isBlank(user.getHomeUrl()))
            return new ModelAndView(new RedirectView(user.getHomeUrl()));

        for (AuthenticationDefinition def : ModuleRegistry.getDefinitions(AuthenticationDefinition.class))
            def.postLogin(user);

        return new ModelAndView(new RedirectView(successUrl));
    }
}
