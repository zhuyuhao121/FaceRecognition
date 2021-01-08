package com.zhilai.driver.config;

public class ConfigString {


    private ConfigString() {

    }

    public static ConfigString getInstance() {
        return Inner.INSTANCE;
    }

    static class Inner {
        static ConfigString INSTANCE = new ConfigString();
    }

    public String ctr = "ctr";
    public String log = "log";
    public String level = "level";
    public String server = "server";
    public String sqlServer = "sql_server";
    public String port = "port";
    public String address = "address";
    public String user = "user";
    public String password = "password";

    public String app = "app";
    public String face = "face";
    public String finger = "finger";
    public String reserveRecord = "reserve_record";
    public String faceThreshold = "face_threshold";
    public String fingerThreshold = "finger_threshold";
    public String admin = "admin";
    public String faceAddr = "face_addr";
    public String faceLigntAddr = "face_light_addr";
    public String cabId = "cab_id";
    public String doorId = "door_id";
    public String inOrOut = "in_or_out";
    /**
     * 0 gpio
     * 1 send data to face light address dev
     */
    public String infraredType = "infrared_type";


    public String dev = "dev";
    public String type = "type";
    public String baudrate = "baudrate";


    /**
     * [monitor]
     * type = ...
     * dev = ...
     */
    public String monitor = "monitor";

    /**
     * [pcb_...]
     * id = ...
     * order = ...
     * type = ... {"fc24","serial"}
     * dev = ...  {ip address or dial code}
     * count = ...
     * start_no = ...
     * <p>
     * rootDev = ... {serial locker address}
     * baudrate = ...
     */
    public String lockerPcb = "pcb_";
    public String id = "id";
    public String count = "count";
    public String startNo = "start_no";
    public String rootDev = "root_dev";
    public String group = "group";

    /**
     * [scanner]
     * type = ...
     * dev = ...
     * baudrate = ...
     */
    public String scanner = "scanner";
    public String timeout = "timeout";

    /**
     * [ups]
     * dev = ...
     * baudrate = ...
     */
    public String ups = "ups";

    /**
     * [vending_1]
     * dev=/dev/ttyS1
     * baudrate=9600
     * column=5
     * row=4
     */
    public String vending = "vending_";
    public String col = "column";
    public String row = "row";
    /**
     * 0 启用
     * 1 禁用
     */
    public String inspectEnable = "inspectEnable";
    public String getGoodsTimeout = "getGoodsTimeout";
    /**
     * 1-2
     */
    public String pedrailChannel = "pedrailChannel";
    /**
     * 0 有升降台
     * 1 无升降台
     */
    public String hasPlatform = "hasPlatform";


    /**
     * [selftake]
     * dev=/dev/ttyS1
     * baudrate=9600
     */
    public String selftake = "selftake";

    /**
     * [idCard]
     * dev=/dev/ttyS1
     * baudrate=9600
     */
    public String idCard = "idCard";

    /**
     * [rfidRW]
     * dev=/dev/ttyS1
     * baudrate=115200
     */
    public String rfidRW = "rfidRW";
    public String antenna = "antenna";

    public String settingcode = "settingcode";
    public String cansetpedrail = "cansetpedrail";
}