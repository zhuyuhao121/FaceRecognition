# 人脸识别框架

目前只集成了阅面人脸识别模块。

## 依赖添加
在你的项目根目录下的build.gradle文件中加入依赖
``` java
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```
添加依赖
``` java
dependencies {
    implementation 'com.github.zhuyuhao121:FaceRecognition:1.0.0'
}
```

## 初始化
``` java
 FaceBll.getInstance().init(MainActivity.this, (faceAddr, lightDev) -> {

                        });

``` 

## 人脸注册

``` java

/**
 * 人脸注册
 *
 * @param faceId             人脸id
 * @param registerThreshold  注册时的阈值
 */
private void faceReg() {
    FaceBll.getInstance().faceReg(faceId, 0.7f, 1, new ModuleDataHelper.FaceRegistCallback() {
        @Override
        public void onFaceRegistSuccess(long id, FaceRecogRecord faceRecogRecord, FaceRecogUser faceRecogUser) {
            String msg = "onFaceRecogSuccess     id===" + id + "   识别到的图片路径===" + faceRecogRecord.getFacePicPath();
            Log.d(TAG, msg);
            runOnUiThread(() -> {
                Toast.makeText(FaceRegActivity.this, msg, Toast.LENGTH_LONG).show();
                Log.e(TAG, "faceRecogRecord.getFacePicPath()==" + faceRecogRecord.getFacePicPath());
                Log.e(TAG, "faceRecogUser.getFacePicPath()==" + faceRecogUser.getFacePicPath());
                if (faceIv != null && !TextUtils.isEmpty(faceRecogRecord.getFacePicPath())) {
//                        Glide.with(FaceRegActivity.this)
//                                .load(faceRecogRecord.getFacePicPath())
//                                .into(faceIv);
                }
            });
        }

        @Override
        public void onError(int i, String errorMsg) {
            String msg = "onError     i===" + i + "    " + "errorMsg===" + errorMsg;
            Log.d(TAG, msg);
            runOnUiThread(() -> Toast.makeText(FaceRegActivity.this, msg, Toast.LENGTH_LONG).show());
            if (i == -1) {
                finish();
            }
        }
    });
}

```

## 人脸识别

``` java

/**
 * 人脸识别
 *
 * @param threshold         识别时的阈值
 */
private void faceRecognition() {
    FaceBll.getInstance().faceRecognition(0.6f, 1, new ModuleDataHelper.FaceRecogCallback() {
        @Override
        public void onFaceResult(long id, float likelyhood) {
            String msg = "onFaceResult     id===" + id + "    " + "likelyhood===" + likelyhood;
            Log.d(TAG, msg);
            runOnUiThread(() -> Toast.makeText(FaceRecognitionActivity.this, msg, Toast.LENGTH_LONG).show());
        }

        @Override
        public void onFaceRecogSuccess(long id, FaceRecogRecord faceRecogRecord, FaceRecogUser faceRecogUser) {
            String msg = "onFaceRecogSuccess     id===" + id + "   识别到的图片路径===" + faceRecogRecord.getFacePicPath();
            Log.d(TAG, msg);
            runOnUiThread(() -> {
                Toast.makeText(FaceRecognitionActivity.this, msg, Toast.LENGTH_LONG).show();
                if (faceIv != null && !TextUtils.isEmpty(faceRecogRecord.getFacePicPath())) {
//                        Glide.with(FaceRecognitionActivity.this)
//                                .load(faceRecogRecord.getFacePicPath())
//                                .into(faceIv);
                }
            });
        }

        @Override
        public void onError(int i, String errorMsg) {
            String msg = "onError     i===" + i + "    " + "errorMsg===" + errorMsg;
            Log.d(TAG, msg);
            runOnUiThread(() -> Toast.makeText(FaceRecognitionActivity.this, msg, Toast.LENGTH_LONG).show());
            if (i == -1) {
                finish();
            }
        }
    });
}
    
```
## 释放cameraView

``` java
/**
 * 释放cameraView
 */
FaceBll.getInstance().releaseCamera(camera);
```

## 根据faceId删除人脸信息

``` java
FaceBll.getInstance().deleteUser(Long.parseLong(id), new ModuleDataHelper.FaceDeleteCallback() {
    @Override
    public void onError(int i, String errorMsg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onFaceDeleteSuccess(long id) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_LONG).show());
    }
});
```

## 删除所有人脸数据

``` java
FaceBll.getInstance().clearAllFaceData();
```

## 摄像头反向

``` java
FaceBll.getInstance().rotate180();
```

## 摄像头正向

``` java
FaceBll.getInstance().rotate0();
```

## 彩色模式

``` java
FaceBll.getInstance().blackToColor();
```

## 黑白模式

``` java
FaceBll.getInstance().colorToBlack();
```

## 摄像头区域重置

``` java
FaceBll.getInstance().setArea();
```

## 设置摄像头区域

``` java
/**
 * 设置摄像头区域
 * if (x1 < 0 || y1 < 0 || x2 > 640 || y2 > 480) 提示请输入正确的区域值
 */
FaceBll.getInstance().setArea(int x1, int x2, int y1, int y2);
```

## 设置人眼距离

``` java
/**
 * 设置人眼距离
 * faceth 人脸阈值
 * distance 人眼距离
 */
FaceBll.getInstance().setEyedistance(float distance);
```

## 设置补光灯亮度

``` java
FaceBll.getInstance().setLightLevelSerial(int level);
```

## 关闭补光灯

``` java
FaceBll.getInstance().closeInfraredLight();
```

## 延时3秒关闭补光灯

``` java
FaceBll.getInstance().closeInfraredLightDelay();
```

## 保存人脸数据

``` java
/**
 * 保存人脸数据
 *
 * @param id       faceId
 * @param faceData 人脸数据
 * @param date     时间
 * @return
 */
FaceBll.getInstance().saveUser(long id, byte[] faceData, String facePicPath, Date date);
```

## 获取人脸数据记录

``` java
FaceBll.getInstance().getFaceRecogRecords();
```

## 获取所有人脸数据

``` java
FaceBll.getInstance().getAllFaceRecogUsers();
```

## 获取faceId对应的人脸数据

``` java
FaceBll.getInstance().getFaceRecogUser(long faceId);
```