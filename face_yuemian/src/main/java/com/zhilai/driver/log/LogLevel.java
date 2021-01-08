package com.zhilai.driver.log;


public class LogLevel {

    public static final int VERBOSE = 2;

    public static final int DEBUG = 3;

    public static final int INFO = 4;

    public static final int WARN = 5;

    public static final int ERROR = 6;

    public static String getName(int logLevel) {
        String name;

        switch (logLevel) {
            case VERBOSE:
                name = "VERBOSE";
                break;
            case DEBUG:
                name = "DEBUG";
                break;
            case INFO:
                name = "INFO";
                break;
            case WARN:
                name = "WARN";
                break;
            case ERROR:
                name = "ERROR";
                break;
            default:
                name = "unknown name";
                break;
        }
        return name;
    }
}
