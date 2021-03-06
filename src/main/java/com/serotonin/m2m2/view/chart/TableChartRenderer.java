
package com.serotonin.m2m2.view.chart;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.m2m2.vo.DataPointVO;

public class TableChartRenderer extends BaseChartRenderer {
    private static ImplDefinition definition = new ImplDefinition("chartRendererTable", "TABLE", "chartRenderer.table",
            new int[] { DataTypes.ALPHANUMERIC, DataTypes.BINARY, DataTypes.MULTISTATE, DataTypes.NUMERIC });

    public static ImplDefinition getDefinition() {
        return definition;
    }

    public String getTypeName() {
        return definition.getName();
    }

    @JsonProperty
    private int limit;

    public TableChartRenderer() {
        // no op
    }

    public TableChartRenderer(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void addDataToModel(Map<String, Object> model, DataPointVO point) {
        DataPointRT rt = Common.runtimeManager.getDataPoint(point.getId());
        if (rt != null)
            model.put("chartData", rt.getLatestPointValues(limit));
    }

    public ImplDefinition getDef() {
        return definition;
    }

    public String getChartSnippetFilename() {
        return "tableChart.jsp";
    }

    //
    // /
    // / Serialization
    // /
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(limit);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            limit = in.readInt();
        }
    }
}
