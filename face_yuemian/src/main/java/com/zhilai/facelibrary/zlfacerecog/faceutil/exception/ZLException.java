package com.zhilai.facelibrary.zlfacerecog.faceutil.exception;


public class ZLException extends Exception {

    private ZLErrorEnum errorEnum;

    public ZLException(ZLErrorEnum errorEnum) {
        this.errorEnum = errorEnum;
    }

    public ZLException(ZLErrorEnum errorEnum, Throwable cause) {
        super(cause);
        this.errorEnum = errorEnum;
    }

    public int getErrorCode() {
        return errorEnum.getErrorCode();
    }

    public String getErrorMessage() {
        return errorEnum.getErrorMessage();
    }

    @Override
    public String toString() {
        return "ZLException{" +
                "errorEnum=" + errorEnum +
                '}';
    }
}
