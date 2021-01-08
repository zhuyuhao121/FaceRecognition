package com.zhilai.face.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhilai.face.R;
import com.zhilai.face.bean.FaceBean;
import com.zhilai.facelibrary.zlfacerecog.bll.FaceBll;
import com.zhilai.facelibrary.zlfacerecog.faceutil.Base64Util;
import com.zhilai.facelibrary.zlfacerecog.faceutil.camera.CameraView;
import com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.FaceRecogRecord;
import com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.FaceRecogUser;
import com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.ModuleDataHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FaceRecognitionActivity extends BaseActivity {

    private static final String TAG = "FaceActivity";
    @BindView(R.id.camera)
    CameraView camera;
    @BindView(R.id.bt)
    Button bt;
    @BindView(R.id.face_iv)
    ImageView faceIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face2);
        ButterKnife.bind(this);
    }

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
                FaceBean faceBean = new FaceBean(faceRecogUser.getId()
                        , Base64Util.encode(faceRecogUser.getFaceData())
                        , faceRecogUser.getFacePicPath()
                        , getDateStr(faceRecogUser.getCreateTime()));
                mFaceBean = faceBean;
                String msg = "onFaceRecogSuccess     id===" + id + "   识别到的图片路径===" + faceRecogRecord.getFacePicPath();
                Log.d(TAG, msg);
                Log.d(TAG, "mFaceBean==" + mFaceBean);
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

    @Override
    protected void onResume() {
        super.onResume();
        bt.setText("识别");
        /**
         * 初始化cameraView
         */
        FaceBll.getInstance().initCamera(camera);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /**
         * 释放cameraView
         */
        FaceBll.getInstance().releaseCamera(camera);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.bt)
    public void onViewClicked() {
        faceRecognition();
    }
}
