package com.zhilai.driver.log;


import com.zhilai.facelibrary.zlfacerecog.MApp;

public class LogConfiguration {

    public int cacheDays;
    public String logPath;
    public String tag;
    public String logSuffix;
    public int logLevel;

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    private LogConfiguration(Builder builder) {
        this.cacheDays = builder.cacheDays;
        this.logPath = builder.logPath;
        this.tag = builder.tag;
        this.logSuffix = builder.logSuffix;
        this.logLevel = builder.logLevel;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder defaultBuilder() {
        Builder builder = new Builder();
        builder.cacheDays(7)
                .logLevel(LogLevel.DEBUG)
                .logPath(MApp.CONFIG_PATH + "/log")
                .logSuffix(".log")
                .tag("ZHILAI")
                .build();
        return builder;
    }

    public static class Builder {
        private int cacheDays;
        private String logPath;
        private String tag;
        private String logSuffix;
        private int logLevel;

        public Builder cacheDays(int cacheDays) {
            this.cacheDays = cacheDays;
            return this;
        }

        public Builder logPath(String logPath) {
            this.logPath = logPath;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder logSuffix(String logSuffix) {
            this.logSuffix = logSuffix;
            return this;
        }

        public Builder logLevel(int logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public LogConfiguration build() {
            return new LogConfiguration(this);
        }


    }
}
