
package com.nielsmasdorp.speculum.models.octoprint;

import java.util.HashMap;
import java.util.Map;

public class Progress {

    private Object completion;
    private Object filepos;
    private Object printTime;
    private Object printTimeLeft;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Progress() {
    }

    /**
     * 
     * @param printTimeLeft
     * @param printTime
     * @param filepos
     * @param completion
     */
    public Progress(Object completion, Object filepos, Object printTime, Object printTimeLeft) {
        super();
        this.completion = completion;
        this.filepos = filepos;
        this.printTime = printTime;
        this.printTimeLeft = printTimeLeft;
    }

    public Object getCompletion() {
        return completion;
    }

    public void setCompletion(Object completion) {
        this.completion = completion;
    }

    public Object getFilepos() {
        return filepos;
    }

    public void setFilepos(Object filepos) {
        this.filepos = filepos;
    }

    public Object getPrintTime() {
        return printTime;
    }

    public void setPrintTime(Object printTime) {
        this.printTime = printTime;
    }

    public Object getPrintTimeLeft() {
        return printTimeLeft;
    }

    public void setPrintTimeLeft(Object printTimeLeft) {
        this.printTimeLeft = printTimeLeft;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
