
package com.serotonin.m2m2.vo.dataSource;

import com.serotonin.m2m2.vo.DataPointVO;

/**
 * Used when a point is saved from the data point edit page. Allows data source -specific actions to be completed before
 * the point is actually saved.
 * 
 */
public interface DataPointSaveHandler {
    void handleSave(DataPointVO point);
}
