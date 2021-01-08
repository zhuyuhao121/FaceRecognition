package com.zhilai.facelibrary.zlfacerecog.faceutil.yuemian;

import com.zhilai.facelibrary.zlfacerecog.faceutil.ByteUtil;

public class ReadSenseSendPacket {

    public byte cmdType;
    private byte replyType;
    public byte[] dataBody = new byte[0];
    private int reserve;
    private byte[] response;
    private CmdWrapper cmdWrapper;

    @Override
    public String toString() {
        return "ReadSenseSendPacket{" +
                "cmdType=" + ByteUtil.bytesToHexString(new byte[]{cmdType}, "") +
                ", replyType=" + ByteUtil.bytesToHexString(new byte[]{replyType}, "") +
                ", reserve=" + reserve +
                ", response=" + ByteUtil.bytesToHexStringLimit(response, " ", 20) +
                ", dataBody=" + ByteUtil.bytesToHexStringLimit(dataBody, " ", 20) +
                '}';
    }

    public ReadSenseSendPacket(byte cmdType, byte replyType) {
        this.cmdType = cmdType;
        this.replyType = replyType;
        cmdWrapper = new CmdWrapper(replyType);
    }

    public ReadSenseSendPacket(byte cmdType, byte replyType, byte[] dataBody) {
        this.cmdType = cmdType;
        this.replyType = replyType;
        cmdWrapper = new CmdWrapper(replyType);

        this.dataBody = dataBody;
    }
}
