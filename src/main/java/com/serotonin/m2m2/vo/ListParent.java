
package com.serotonin.m2m2.vo;

import java.util.List;


public class ListParent<T, E> {
    private T parent;
    private List<E> list;

    public List<E> getList() {
        return list;
    }

    public void setList(List<E> list) {
        this.list = list;
    }

    public T getParent() {
        return parent;
    }

    public void setParent(T parent) {
        this.parent = parent;
    }
}
