package com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog;

import com.zhilai.facelibrary.zlfacerecog.faceutil.ByteUtil;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

@Entity
public class FaceRecogUser {
    @Id
    Long id;
    byte[] faceData;
    String facePicPath;
    Date createTime;

    @Generated(hash = 622738301)
    public FaceRecogUser(Long id, byte[] faceData, String facePicPath,
                         Date createTime) {
        this.id = id;
        this.faceData = faceData;
        this.facePicPath = facePicPath;
        this.createTime = createTime;
    }

    @Generated(hash = 247272373)
    public FaceRecogUser() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getFaceData() {
        return this.faceData;
    }

    public void setFaceData(byte[] faceData) {
        this.faceData = faceData;
    }

    public String getFacePicPath() {
        return this.facePicPath;
    }

    public void setFacePicPath(String facePicPath) {
        this.facePicPath = facePicPath;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "FaceRecogUser{" +
                "id=" + id +
                ", faceData=" + ByteUtil.bytesToHexStringLimit(faceData, " ", 50) +
                ", facePicPath='" + facePicPath + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
