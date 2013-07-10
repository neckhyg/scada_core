
package com.serotonin.m2m2.view.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.serotonin.json.spi.JsonProperty;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.view.ImplDefinition;
import com.serotonin.util.SerializationHelper;

public class TimeRenderer extends BaseTextRenderer {
    private static ImplDefinition definition = new ImplDefinition("textRendererTime", "TIME", "textRenderer.time",
            new int[] { DataTypes.NUMERIC });

    public static ImplDefinition getDefinition() {
        return definition;
    }

    public String getTypeName() {
        return definition.getName();
    }

    public ImplDefinition getDef() {
        return definition;
    }

    @JsonProperty
    private String format;
    @JsonProperty
    private int conversionExponent;

    public TimeRenderer() {
        // no op
    }

    public TimeRenderer(String format, int conversionExponent) {
        setFormat(format);
        this.conversionExponent = conversionExponent;
    }

    @Override
    protected String getTextImpl(DataValue value, int hint) {
        if (!(value instanceof NumericValue))
            return null;
        return getText((long) value.getDoubleValue(), hint);
    }

    @Override
    public String getText(double value, int hint) {
        long l = (long) value;

        if (hint == HINT_RAW || hint == HINT_SPECIFIC)
            return new Long(l).toString();

        l *= (long) Math.pow(10, conversionExponent);

        return new SimpleDateFormat(format).format(new Date(l));
    }

    @Override
    protected String getColourImpl(DataValue value) {
        return null;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getConversionExponent() {
        return conversionExponent;
    }

    public void setConversionExponent(int conversionExponent) {
        this.conversionExponent = conversionExponent;
    }

    public String getChangeSnippetFilename() {
        return "changeContentText.jsp";
    }

    public String getSetPointSnippetFilename() {
        return null;
    }

    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        SerializationHelper.writeSafeUTF(out, format);
        out.writeInt(conversionExponent);
    }

    private void readObject(ObjectInputStream in) throws IOException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            format = SerializationHelper.readSafeUTF(in);
            conversionExponent = in.readInt();
        }
    }
}
