
package com.serotonin.m2m2.view.quantize;

import java.util.List;

import com.serotonin.m2m2.view.stats.IValueTime;

/**
 * @deprecated use quantize2 classes instead.
 */
@Deprecated
public interface DataQuantizerCallback {
    void quantizedData(List<IValueTime> vts);
}
