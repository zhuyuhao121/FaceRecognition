package com.zhilai.facelibrary.zlfacerecog.faceutil.yuemian;

import com.zhilai.facelibrary.zlfacerecog.faceutil.ByteUtil;
import java.util.Arrays;

public class CmdWrapper {
    String mCmd;
    public byte mCmdCode;
    /**
     * -1 error
     * 0 ready
     * 1 in use
     */
    public int mResultCode;
    public byte[] mResponse;
    public boolean mShouldNotify = true;
    public Callback mCallback;

    public static int WRAPPER_BYTE_COUNT = 20;

    public String getmCmd() {
        return mCmd;
    }

    public void setmCmd(String mCmd) {
        this.mCmd = mCmd;
    }

    public byte[] getmResponse() {
        return mResponse;
    }

    public void setmResponse(byte[] mResponse) {
        this.mResponse = mResponse;
    }

    public boolean ismShouldNotify() {
        return mShouldNotify;
    }

    public void setmShouldNotify(boolean mShouldNotify) {
        this.mShouldNotify = mShouldNotify;
    }

    public Callback getmCallback() {
        return mCallback;
    }

    public void setmCallback(Callback mCallback) {
        this.mCallback = mCallback;
    }

    public CmdWrapper(byte mCmd, String method) {
        this.mCmd = ByteUtil.bytesToHexString(new byte[]{mCmd}, "") + "-" + method;
        mCmdCode = mCmd;
    }

    public CmdWrapper() {
    }

    public CmdWrapper(byte mCmd) {
        this.mCmd = ByteUtil.bytesToHexString(new byte[]{mCmd}, "");
        mCmdCode = mCmd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CmdWrapper that = (CmdWrapper) o;
        return (mCmd == that.mCmd) || (mCmd != null && mCmd.equals(that.mCmd));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new String[]{mCmd});
    }

    @Override
    public String toString() {
        return "CmdWrapper{" +
                "mCmd='" + mCmd + '\'' +
                ", mCmdCode=" + mCmdCode +
                ", mResultCode=" + mResultCode +
                ", mResponse=" + ByteUtil.bytesToHexStringLimit(mResponse, " ", WRAPPER_BYTE_COUNT)  +
                ", mShouldNotify=" + mShouldNotify +
                ", mCallback=" + mCallback +
                '}';
    }

    public interface Callback {
        void onData(byte[] data);
    }
}
