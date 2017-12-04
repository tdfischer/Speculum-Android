
package com.nielsmasdorp.speculum.models.octoprint;

import java.util.HashMap;
import java.util.Map;

public class Job {

    private Job_ job;
    private Progress progress;
    private String state;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Job() {
    }

    /**
     * 
     * @param progress
     * @param state
     * @param job
     */
    public Job(Job_ job, Progress progress, String state) {
        super();
        this.job = job;
        this.progress = progress;
        this.state = state;
    }

    public Job_ getJob() {
        return job;
    }

    public void setJob(Job_ job) {
        this.job = job;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
