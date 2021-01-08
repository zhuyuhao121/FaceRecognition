package com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

@Entity
public class FaceRecogRecord {
    @Id
    Long id;
    /**
     * 0 注册
     * 1 识别
     * 2 删除
     * 3 注册失败，已注册
     * 4 图片注册
     * 5 图片识别
     * 6 图片注册失败，已注册
     */
    int type;
    Date recogTime;
    String facePicPath;
    long mostLikelyId;
    float likelyHood;

    @Generated(hash = 1059313260)
    public FaceRecogRecord(Long id, int type, Date recogTime, String facePicPath,
                           long mostLikelyId, float likelyHood) {
        this.id = id;
        this.type = type;
        this.recogTime = recogTime;
        this.facePicPath = facePicPath;
        this.mostLikelyId = mostLikelyId;
        this.likelyHood = likelyHood;
    }

    @Generated(hash = 2012218059)
    public FaceRecogRecord() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getRecogTime() {
        return this.recogTime;
    }

    public void setRecogTime(Date recogTime) {
        this.recogTime = recogTime;
    }

    public String getFacePicPath() {
        return this.facePicPath;
    }

    public void setFacePicPath(String facePicPath) {
        this.facePicPath = facePicPath;
    }

    public long getMostLikelyId() {
        return this.mostLikelyId;
    }

    public void setMostLikelyId(long mostLikelyId) {
        this.mostLikelyId = mostLikelyId;
    }

    public float getLikelyHood() {
        return this.likelyHood;
    }

    public void setLikelyHood(float likelyHood) {
        this.likelyHood = likelyHood;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "FaceRecogRecord{" +
                "id=" + id +
                ", type=" + type +
                ", recogTime=" + recogTime +
                ", facePicPath='" + facePicPath + '\'' +
                ", mostLikelyId=" + mostLikelyId +
                ", likelyHood=" + likelyHood +
                '}';
    }
}
