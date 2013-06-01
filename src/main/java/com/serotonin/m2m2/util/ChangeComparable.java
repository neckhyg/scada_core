
package com.serotonin.m2m2.util;

import java.util.List;

import com.serotonin.m2m2.i18n.TranslatableMessage;


public interface ChangeComparable<T> {
    int getId();

    String getTypeKey();

    void addProperties(List<TranslatableMessage> list);

    void addPropertyChanges(List<TranslatableMessage> list, T from);
}
