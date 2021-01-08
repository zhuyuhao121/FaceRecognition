package com.zhilai.face.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.zhilai.face.R;
import com.zhilai.face.permission.Acp;
import com.zhilai.face.permission.AcpListener;
import com.zhilai.face.permission.AcpOptions;
import com.zhilai.facelibrary.zlfacerecog.bll.FaceBll;
import com.zhilai.facelibrary.zlfacerecog.faceutil.Base64Util;
import com.zhilai.facelibrary.zlfacerecog.faceutil.ByteUtil;
import com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.ModuleDataHelper;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@SuppressLint("NonConstantResourceId")
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.face_id_et)
    EditText faceIdEt;
    @BindView(R.id.bt_del_face)
    Button btDelFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        verifyStoragePermissions();
    }

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void verifyStoragePermissions() {
        Acp.getInstance(this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.CAMERA
                                , Manifest.permission.READ_PHONE_STATE
                                , Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        /*以下为自定义提示语、按钮文字
                        .setDeniedMessage()
                        .setDeniedCloseBtn()
                        .setDeniedSettingBtn()
                        .setRationalMessage()
                        .setRationalBtn()*/
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {
                        Log.d(TAG, "已申请到权限");
                        FaceBll.getInstance().init(MainActivity.this, (faceAddr, lightDev) -> {

                        });
//                        FaceBll.getInstance().init("/dev/ttyS1", "/dev/ttyUSBFAC1");
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        Log.d(TAG, "权限已被拒绝");
                    }
                });
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.bt_input_face, R.id.bt_auth_face, R.id.bt_del_face, R.id.clear_face, R.id.add_face})
    public void onViewClicked(View view) {
//        if (FaceBll.getInstance().rst == 2) {
        String id = faceIdEt.getText().toString();
        switch (view.getId()) {
            case R.id.bt_input_face:
                if (TextUtils.isEmpty(id)) {
                    Toast.makeText(MainActivity.this, "请先输入人脸ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, FaceRegActivity.class);
                intent.putExtra("faceId", Long.parseLong(id));
                startActivity(intent);
                break;
            case R.id.bt_auth_face:
                startActivity(new Intent(MainActivity.this, FaceRecognitionActivity.class));
                break;
            case R.id.bt_del_face:
                if (TextUtils.isEmpty(id)) {
                    Toast.makeText(MainActivity.this, "请先输入人脸ID", Toast.LENGTH_SHORT).show();
                    return;
                }
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
                break;
            case R.id.clear_face:
                FaceBll.getInstance().clearAllFaceData();
                break;
            case R.id.add_face:
                try {
                    if (mFaceBean != null) {
                        boolean isOk = FaceBll.getInstance().saveUser(mFaceBean.getId()
                                , Base64Util.decode(mFaceBean.getFaceData())
                                , mFaceBean.getFacePicPath()
                                , parseServerTime(mFaceBean.getCreateTime()));
                        Log.d(TAG, "isOk===" + isOk);
                    } else {
                        Toast.makeText(this, "请先注册人脸", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
//        } else {
//            Toast.makeText(this, "正在获取串口号，请稍后...", Toast.LENGTH_SHORT).show();
//        }
    }
}
