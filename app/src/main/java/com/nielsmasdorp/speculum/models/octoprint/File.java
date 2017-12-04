
package com.nielsmasdorp.speculum.models.octoprint;

import java.util.HashMap;
import java.util.Map;

public class File {

    private Object date;
    private Object name;
    private Object origin;
    private Object path;
    private Object size;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public File() {
    }

    /**
     * 
     * @param name
     * @param path
     * @param origin
     * @param date
     * @param size
     */
    public File(Object date, Object name, Object origin, Object path, Object size) {
        super();
        this.date = date;
        this.name = name;
        this.origin = origin;
        this.path = path;
        this.size = size;
    }

    public Object getDate() {
        return date;
    }

    public void setDate(Object date) {
        this.date = date;
    }

    public Object getName() {
        return name;
    }

    public void setName(Object name) {
        this.name = name;
    }

    public Object getOrigin() {
        return origin;
    }

    public void setOrigin(Object origin) {
        this.origin = origin;
    }

    public Object getPath() {
        return path;
    }

    public void setPath(Object path) {
        this.path = path;
    }

    public Object getSize() {
        return size;
    }

    public void setSize(Object size) {
        this.size = size;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
