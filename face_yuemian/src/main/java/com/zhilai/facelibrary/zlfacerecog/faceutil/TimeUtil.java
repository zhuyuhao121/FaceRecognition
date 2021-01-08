package com.zhilai.facelibrary.zlfacerecog.faceutil;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {

    private TimeUtil() {
    }

    public enum DateTimePattern {
        ALL_TIME {
            public String getValue() {
                return "yyyy-MM-dd HH:mm:ss.SSS";
            }
        },
        ALL_TIME_INFILE {
            public String getValue() {
                return "yyyy_MM_dd__HH_mm_ss_SSS";
            }
        },
        STANDARD_TIME {
            public String getValue() {
                return "yyyy-MM-dd HH:mm:ss";
            }
        },
        ONLY_DAY {
            public String getValue() {
                return "yyyy-MM-dd";
            }
        },
        ONLY_TIME {
            public String getValue() {
                return "HH:mm:ss";
            }
        };

        public abstract String getValue();
    }

    public static String getNowDate(DateTimePattern pattern) {
        Calendar calendar = Calendar.getInstance();
        Date dateNow = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(pattern.getValue(), Locale.CHINA);
        return sdf.format(dateNow);
    }
}
