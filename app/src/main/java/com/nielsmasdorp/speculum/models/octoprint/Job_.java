
package com.nielsmasdorp.speculum.models.octoprint;

import java.util.HashMap;
import java.util.Map;

public class Job_ {

    private Object estimatedPrintTime;
    private Filament filament;
    private File file;
    private Object lastPrintTime;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Job_() {
    }

    /**
     * 
     * @param estimatedPrintTime
     * @param file
     * @param filament
     * @param lastPrintTime
     */
    public Job_(Object estimatedPrintTime, Filament filament, File file, Object lastPrintTime) {
        super();
        this.estimatedPrintTime = estimatedPrintTime;
        this.filament = filament;
        this.file = file;
        this.lastPrintTime = lastPrintTime;
    }

    public Object getEstimatedPrintTime() {
        return estimatedPrintTime;
    }

    public void setEstimatedPrintTime(Object estimatedPrintTime) {
        this.estimatedPrintTime = estimatedPrintTime;
    }

    public Filament getFilament() {
        return filament;
    }

    public void setFilament(Filament filament) {
        this.filament = filament;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Object getLastPrintTime() {
        return lastPrintTime;
    }

    public void setLastPrintTime(Object lastPrintTime) {
        this.lastPrintTime = lastPrintTime;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
