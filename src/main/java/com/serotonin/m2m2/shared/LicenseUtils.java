package com.serotonin.m2m2.shared;

import com.serotonin.util.StringEncrypter;
import com.serotonin.util.XmlUtilsTS;

import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

public class LicenseUtils {
    public static byte[] calculateLicenseHash(Element license) {
        StringBuilder toHash = new StringBuilder();
        Element core = XmlUtilsTS.getChildElement(license, "core");
        addAttributesToSign(toHash, core);
        toHash.append("|");

        Element modulesNode = XmlUtilsTS.getChildElement(license, "modules");
        List<Element> modules = XmlUtilsTS.getChildElements(modulesNode);
        Collections.sort(modules, XmlUtilsTS.TAG_NAME_COMPARATOR);

        for (Element module : modules) {
            toHash.append(module.getTagName());
            toHash.append(":");
            addAttributesToSign(toHash, module);
            toHash.append("|");
        }

        byte[] sha = StringEncrypter.hashSHA512(toHash.toString());

        return sha;
    }

    private static void addAttributesToSign(StringBuilder tosign, Element element) {
        List<Element> children = XmlUtilsTS.getChildElements(element);
        Collections.sort(children, XmlUtilsTS.TAG_NAME_COMPARATOR);

        for (Element child : children) {
            tosign.append(child.getTagName());
            tosign.append("=");

            if (XmlUtilsTS.getChildElements(child).isEmpty()) {
                tosign.append(XmlUtilsTS.getElementText(child, ""));
            } else {
                tosign.append("(");
                addAttributesToSign(tosign, child);
                tosign.append(")");
            }
            tosign.append(",");
        }
    }
}