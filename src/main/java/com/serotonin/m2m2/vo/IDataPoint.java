package com.serotonin.m2m2.vo;

/**
 * Interface that represents both full data point VOs and summary objects.
 * 
 */
public interface IDataPoint {
    int getId();

    String getXid();

    String getName();

    int getDataSourceId();

    String getDeviceName();

    int getPointFolderId();

    String getExtendedName();
}
