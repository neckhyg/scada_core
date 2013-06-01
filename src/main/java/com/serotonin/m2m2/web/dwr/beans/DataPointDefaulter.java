
package com.serotonin.m2m2.web.dwr.beans;

import com.serotonin.m2m2.vo.DataPointVO;


public interface DataPointDefaulter {
    // Called on a newly created point to default values as necessary.
    void setDefaultValues(DataPointVO dp);

    // Called on an existing point to update values as necessary.
    void updateDefaultValues(DataPointVO dp);

    // Called on a point following a save update values as necessary.
    void postSave(DataPointVO dp);
}
