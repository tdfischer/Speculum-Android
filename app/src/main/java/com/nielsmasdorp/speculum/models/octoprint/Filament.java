
package com.nielsmasdorp.speculum.models.octoprint;

import java.util.HashMap;
import java.util.Map;

public class Filament {

    private Object length;
    private Object volume;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Filament() {
    }

    /**
     * 
     * @param volume
     * @param length
     */
    public Filament(Object length, Object volume) {
        super();
        this.length = length;
        this.volume = volume;
    }

    public Object getLength() {
        return length;
    }

    public void setLength(Object length) {
        this.length = length;
    }

    public Object getVolume() {
        return volume;
    }

    public void setVolume(Object volume) {
        this.volume = volume;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
