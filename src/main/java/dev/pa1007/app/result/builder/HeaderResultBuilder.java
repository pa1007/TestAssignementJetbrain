package dev.pa1007.app.result.builder;

import dev.pa1007.app.result.HeaderResult;

public class HeaderResultBuilder {

    private double libAccNb;
    private String libAccFrac;
    private double projAccNb;
    private String projAccFrac;
    private String missing;

    public HeaderResultBuilder setLibAccNb(double libAccNb) {
        this.libAccNb = libAccNb;
        return this;
    }

    public HeaderResultBuilder setLibAccFrac(String libAccFrac) {
        this.libAccFrac = libAccFrac;
        return this;
    }

    public HeaderResultBuilder setProjAccNb(double projAccNb) {
        this.projAccNb = projAccNb;
        return this;
    }

    public HeaderResultBuilder setProjAccFrac(String projAccFrac) {
        this.projAccFrac = projAccFrac;
        return this;
    }

    public HeaderResultBuilder setMissing(String missing) {
        this.missing = missing;
        return this;
    }

    public HeaderResult createHeaderResult() {
        return new HeaderResult(libAccNb, libAccFrac, projAccNb, projAccFrac, missing);
    }
}