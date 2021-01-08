package com.zhilai.driver.log;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.zhilai.facelibrary.zlfacerecog.faceutil.ThreadUtil;
import com.zhilai.facelibrary.zlfacerecog.faceutil.TimeUtil;
import com.zhilai.facelibrary.zlfacerecog.faceutil.ZipUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ZLog {

    private static LogConfiguration configuration = LogConfiguration.defaultBuilder().build();
    private static ReentrantLock logLock = new ReentrantLock(true);

    public static LogConfiguration getConfiguration() {
        return configuration;
    }

    public static ReentrantLock getLogLock() {
        return logLock;
    }

    public static void setLogConfiguration(LogConfiguration zlLogConfiguration) {
        configuration = zlLogConfiguration;

        init();
    }

    static {
        init();
    }

    private static void init() {

        File dir = new File(configuration.logPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        pool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                clearOverdueLog();
            }
        }, 0, 1, TimeUnit.DAYS);

    }

    public static String getTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return "\r\n" + sw.toString() + "\r\n";
    }

    public static void v(String message) {

        if (configuration.logLevel > LogLevel.VERBOSE) {
            return;
        }

        Log.v(configuration.tag, message);
        printFile(createHead(LogLevel.VERBOSE).concat("\r\n").concat(message).concat("\r\n\r\n"));
    }

    public static void v(String message, Throwable throwable) {

        if (configuration.logLevel > LogLevel.VERBOSE) {
            return;
        }

        Log.v(configuration.tag, message, throwable);

        String msg = createHead(LogLevel.VERBOSE)
                .concat("\r\n")
                .concat(message)
                .concat("\r\n")
                .concat(getTrace(throwable))
                .concat("\r\n\r\n");

        printFile(msg);
    }

    public static void d(String message) {

        if (configuration.logLevel > LogLevel.DEBUG) {
            return;
        }


        Log.d(configuration.tag, message);
        printFile(createHead(LogLevel.DEBUG).concat("\r\n").concat(message).concat("\r\n\r\n"));
    }

    public static void d(String message, Throwable throwable) {

        if (configuration.logLevel > LogLevel.DEBUG) {
            return;
        }

        Log.d(configuration.tag, message, throwable);

        String msg = createHead(LogLevel.DEBUG)
                .concat("\r\n")
                .concat(message)
                .concat("\r\n")
                .concat(getTrace(throwable))
                .concat("\r\n\r\n");

        printFile(msg);
    }

    public static void i(String message) {

        if (configuration.logLevel > LogLevel.INFO) {
            return;
        }

        Log.i(configuration.tag, message);
        printFile(createHead(LogLevel.INFO).concat("\r\n").concat(message).concat("\r\n\r\n"));
    }

    public static void i(boolean callbackout, String message) {

        if (configuration.logLevel > LogLevel.INFO) {
            return;
        }

        Log.i(configuration.tag, message);
        printFile(callbackout, createHead(LogLevel.INFO).concat("\r\n").concat(message).concat("\r\n\r\n"));
    }

    public static void i(String message, Throwable throwable) {

        if (configuration.logLevel > LogLevel.INFO) {
            return;
        }
        Log.i(configuration.tag, message, throwable);

        String msg = createHead(LogLevel.INFO)
                .concat("\r\n")
                .concat(message)
                .concat("\r\n")
                .concat(getTrace(throwable))
                .concat("\r\n\r\n");

        printFile(msg);
    }

    public static void e(String message) {

        if (configuration.logLevel > LogLevel.ERROR) {
            return;
        }

        Log.e(configuration.tag, message);
        printFile(createHead(LogLevel.ERROR).concat("\r\n").concat(message).concat("\r\n\r\n"));
    }

    public static void e(Exception e) {
        e(getTrace(e));
    }

    public static void e(String message, Throwable throwable) {

        if (configuration.logLevel > LogLevel.ERROR) {
            return;
        }

        Log.e(configuration.tag, message, throwable);

        String msg = createHead(LogLevel.ERROR)
                .concat("\r\n")
                .concat(message)
                .concat("\r\n")
                .concat(getTrace(throwable))
                .concat("\r\n\r\n");
        printFile(msg);
    }

    private static String createHead(int logLevel) {

        StackTraceElement element = Thread.currentThread().getStackTrace()[4];
        String className = element.getClassName();
        String methodName = element.getMethodName();
        int lineNumber = element.getLineNumber();

        String ThreadId;
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            ThreadId = "Main";
        } else {
            ThreadId = "" + Thread.currentThread().getId();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[ ")
                .append(TimeUtil.getNowDate(TimeUtil.DateTimePattern.ALL_TIME))
                .append("  ")
                .append(className.substring(className.lastIndexOf(".") + 1) + "." + methodName)
                .append("  Line-")
                .append(lineNumber)
                .append("  thread=" + ThreadId)
                .append("  " + LogLevel.getName(logLevel))
                .append(" ]");
        return sb.toString();
    }

    static long lastTimeStamp;

    public static void i(String log, boolean enableTimeStamp) {
        if (enableTimeStamp) {
            ZLog.i(log + "[take time:" + (System.currentTimeMillis() - lastTimeStamp) + "]");
            lastTimeStamp = System.currentTimeMillis();
        } else {
            ZLog.i(log);
            lastTimeStamp = System.currentTimeMillis();
        }
    }

    public interface LogCallback {
        void onData(String msg);
    }

    static LogCallback logCallback;

    public static void setLogCallback(LogCallback logCallback) {
        ZLog.logCallback = logCallback;
    }

    private static void printFile(final String message) {

        ThreadUtil.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                logLock.lock();
                try {
                    File log = new File(configuration.logPath.concat(File.separator).concat(TimeUtil.getNowDate(TimeUtil.DateTimePattern.ONLY_DAY)).concat(configuration.logSuffix));
                    FileWriter fw = new FileWriter(log, true);
                    fw.write(message);
                    fw.write("\r\n");
                    fw.flush();
                    fw.close();
//                    if (logCallback != null) {
//                        logCallback.onData(message);
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    logLock.unlock();
                }
            }
        });
    }

    private static void printFile(final boolean callbackout, final String message) {

        ThreadUtil.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                logLock.lock();
                try {
                    File log = new File(configuration.logPath.concat(File.separator).concat(TimeUtil.getNowDate(TimeUtil.DateTimePattern.ONLY_DAY)).concat(configuration.logSuffix));
                    FileWriter fw = new FileWriter(log, true);
                    fw.write(message);
                    fw.write("\r\n");
                    fw.flush();
                    fw.close();
                    if (logCallback != null && callbackout) {
                        logCallback.onData(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    logLock.unlock();
                }
            }
        });
    }

    private static boolean isExpired(String date) {

        if (TextUtils.isEmpty(date)) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1 * configuration.cacheDays);
        Date expiredDate = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat(TimeUtil.DateTimePattern.ONLY_DAY.getValue(), Locale.CHINA);

        try {
            Date d = sdf.parse(date);
            return d.before(expiredDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static void clearOverdueLog() {
        File dir = new File(configuration.logPath);
        if (dir.isDirectory()) {
            File[] allFiles = dir.listFiles();
            for (File logFile : allFiles) {
                String fileName = logFile.getName();
                if (!"log".equals(fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()))) {
                    ZLog.d("not log file:" + fileName);
                    continue;
                }
                if (isExpired(fileName.substring(0, fileName.indexOf(".")))) {
                    logFile.delete();
                    ZLog.d("delete expired log success,the log path is:" + logFile.getAbsolutePath());
                }
            }
        }
    }

    public synchronized static boolean compressToUDisk() {

        File file = new File("/storage");
        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.startsWith("udisk")) {
                    File dest = new File(dir, name);
                    String[] allFileName = dest.list();
                    if (Arrays.binarySearch(allFileName, "LOST.DIR") != -1) {
                        return true;
                    }
                }

                return false;
            }
        });


        if (files.length == 1) {
            return compress(files[0].getAbsolutePath() + "/log.zip");
        }

        return false;
    }

    synchronized static boolean compress(String destPath) {

        try {
            FileOutputStream out = new FileOutputStream(destPath);
            ZipUtil.toZip(configuration.logPath, out, true);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

}
