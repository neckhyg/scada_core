
package com.serotonin.m2m2.web.dwr.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DwrPermission {
    boolean admin() default false;

    boolean user() default false;

    boolean anonymous() default false;
}
