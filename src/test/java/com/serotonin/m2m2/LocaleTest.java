package com.serotonin.m2m2;

import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: walter
 * Date: 13-1-28
 * Time: 下午4:49
 * To change this template use File | Settings | File Templates.
 */
public class LocaleTest {
    public static void main(String[] args){
        System.out.println("Locale default: " + Locale.getDefault().toString());
        System.out.println("Locale language: " + Locale.getDefault().getLanguage());
        System.out.println("Locale country: " + Locale.getDefault().getCountry());
    }
}
