
package com.serotonin.m2m2.view.conversion;


public class LinearConversion implements Conversion {
    private final double slope;
    private final double intersect;

    public LinearConversion(double slope, double intersect) {
        this.slope = slope;
        this.intersect = intersect;
    }

    public LinearConversion getInverse() {
        return new LinearConversion(1 / slope, -intersect / slope);
    }

    public double convert(double value) {
        return slope * value + intersect;
    }
}
