package com.zhilai.face.bean;

public class FaceBean {

    private Long id;
    private String faceData;
    private String facePicPath;
    private String createTime;

    public FaceBean(Long id, String faceData, String facePicPath,
                    String createTime) {
        this.id = id;
        this.faceData = faceData;
        this.facePicPath = facePicPath;
        this.createTime = createTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFaceData() {
        return faceData;
    }

    public void setFaceData(String faceData) {
        this.faceData = faceData;
    }

    public String getFacePicPath() {
        return facePicPath;
    }

    public void setFacePicPath(String facePicPath) {
        this.facePicPath = facePicPath;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "FaceRecogUser{" +
                "id=" + id +
                ", faceData=" + faceData +
                ", facePicPath='" + facePicPath + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
