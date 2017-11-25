package com.nielsmasdorp.speculum.models;

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
public class Configuration {

    private boolean celsius;
    private String subreddit;
    private boolean rememberConfig;
    private boolean voiceCommands;
    private boolean simpleLayout;

    public static class Builder {

        private boolean celsius = true;
        private String subreddit = "oakland+bayarea+california";
        private boolean rememberConfig;
        private boolean voiceCommands;
        private boolean simpleLayout = false;

        public Builder celsius(boolean celsius) {
            this.celsius = celsius;
            return this;
        }

        public Builder subreddit(String subreddit) {
            this.subreddit = subreddit;
            return this;
        }

        public Builder rememberConfig(boolean rememberConfig) {
            this.rememberConfig = rememberConfig;
            return this;
        }

        public Builder voiceCommands(boolean voiceCommands) {
            this.voiceCommands = voiceCommands;
            return this;
        }

        public Builder simpleLayout(boolean simpleLayout) {
            this.simpleLayout = simpleLayout;
            return this;
        }

        public Configuration build() {

            return new Configuration(this);
        }
    }

    private Configuration(Builder builder) {

        this.celsius = builder.celsius;
        this.subreddit = builder.subreddit;
        this.rememberConfig = builder.rememberConfig;
        this.voiceCommands = builder.voiceCommands;
        this.simpleLayout = builder.simpleLayout;
    }

    public boolean isCelsius() {
        return celsius;
    }

    public void setCelsius(boolean celsius) {
        this.celsius = celsius;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public boolean isVoiceCommands() {
        return voiceCommands;
    }

    public void setVoiceCommands(boolean voiceCommands) {
        this.voiceCommands = voiceCommands;
    }

    public boolean isSimpleLayout() {
        return simpleLayout;
    }

    public void setSimpleLayout(boolean simpleLayout) {
        this.simpleLayout = simpleLayout;
    }

    public boolean isRememberConfig() {
        return rememberConfig;
    }

    public void setRememberConfig(boolean rememberConfig) {
        this.rememberConfig = rememberConfig;
    }
}
