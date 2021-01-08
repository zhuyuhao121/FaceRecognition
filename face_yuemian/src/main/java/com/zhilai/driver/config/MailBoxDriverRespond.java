package com.zhilai.driver.config;

public class MailBoxDriverRespond {

    /**
     * errorCode : 0
     * errorMessage : 成功
     * data : {"boxID":"1","boxStatus":1}
     */

    private int errorCode;
    private String errorMessage;
    private DataBean data;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * boxID : 1
         * boxStatus : 1
         */

        private String boxID;
        private int boxStatus;

        public DataBean(String boxID, int boxStatus) {
            this.boxID = boxID;
            this.boxStatus = boxStatus;
        }

        public String getBoxID() {
            return boxID;
        }

        public void setBoxID(String boxID) {
            this.boxID = boxID;
        }

        public int getBoxStatus() {
            return boxStatus;
        }

        public void setBoxStatus(int boxStatus) {
            this.boxStatus = boxStatus;
        }
    }
}
