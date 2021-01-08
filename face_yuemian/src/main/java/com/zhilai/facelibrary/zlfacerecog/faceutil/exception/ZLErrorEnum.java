package com.zhilai.facelibrary.zlfacerecog.faceutil.exception;


public enum ZLErrorEnum {

    /**
     * 京东售卖自提相关
     */
    NO_GOODS_DETECT(-8001, "未取到物品"),
    MOTOR_ERROR(-8002, "电机异常"),
    MOTOR_SENSE_ERROR(-8003, "电机反馈信号异常"),
    JD_CLOSE_ERROR(-8004, "关门夹手"),
    JD_LOCK_ERROR(-8005, "门锁异常"),
    JD_OPEN_ERROR(-8006, "开门异常"),
    JD_PLATFORM_OCCUPY(-8007, "平台有物"),
    JD_PLATFORM_CALI_ERROR(-8008, "平台校准失败"),
    JD_INTAKEPORT_OPENED(-8009, "取货口未关闭"),
    JD_INTAKEPORT_UNLOCK(-8010, "取货口未上锁"),
    JD_CAN_NOT_LIFT(-8011, "请先降至底部"),

    ILLEGAL_ARGUMENT_ERROR(-1000, "参数不合法"),
    /**
     * 关门超时回调
     */
    SOCKET_READ_TIMEOUT_ERROR(-1001, "关门超时"),

    WAIT_ERROR(-1002, "等待错误"),
    RECEIVE_DATA_ERROR(-1003, "命令与pcb不匹配"),
    UPS_REVERSED(-1004, "ups版本过低"),
    ADD_WAIT_ERROR(-1005, "添加锁错误"),
    RETURN_ERROR(-1006, "返回数据错误"),

    /**
     * rfid
     */
    RFID_CONNECT_FAIL(-10001, "rfid卡连接异常"),
    RFID_WRITE_FAIL(-10002, "rfid写数据异常"),
    RFID_READ_FAIL(-10003, "rfid读数据异常"),
    RFID_INVENT_FAIL(-10004, "rfid盘存异常"),

    /**
     * 串口相关
     */
    SERIAL_OPEN_CLOSE_ERROR(-2000, "串口初始化异常"),
    SERIAL_IO_ERROR(-2001, "串口IO流异常"),
    SERIAL_TIMEOUT_ERROR(-2002, "串口超时异常"),
    SERIAL_SECURITY_ERROR(-2003, "串口权限异常"),

    /**
     * 触发模式，扫描仪，读取超时
     */
    SERIAL_READ_TIMEOUT_ERROR(-2004, "读取超时"),

    /**
     * 网口相关
     */
    SOCKET_TIMEOUT_ERROR(-3000, "socket 超时"),
    SOCKET_IO_ERROR(-3001, "socket IO 异常"),


    /**
     * 通讯通用异常
     */
    OUTPUT_ERROR(-9001, "输出流异常"),
    INPUT_ERROR(-9002, "输入流异常"),
    IO_ERROR(-9003, "IO异常"),
    TIMEOUT_ERROR(-9004, "超时异常"),
    WAIT_O_ERROR(-9005, "异常停止"),
    RESPONSE_BYTES_ERROR(-9006, "返回数据异常"),
    LOOP_ERROR(-9007, "接收loop异常"),
    OCCUPY_ERROR(-9010, "占用中"),


    /**
     * 开门板
     */
    LOCKER_CONFIG_NOT_FOUND(-4000, "未找到开门板配置"),

    /**
     * 扫码器
     */
    SCANNER_OCCUPY_ERROR(-5000, "扫码器占用中"),

    /**
     * 打印机
     */
    PRINTER_OCCUPY_ERROR(-6000, "打印机占用中");


    private int errorCode;
    private String errorMessage;

    ZLErrorEnum(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "ZLErrorEnum{" +
                "errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
