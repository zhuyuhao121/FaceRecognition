package com.zhilai.facelibrary.zlfacerecog.faceutil;

import android.app.Activity;
import android.app.Dialog;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.zhilai.driver.log.ZLog;
import com.zhilai.facelibrary.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Util {
    static Pattern pattern = Pattern.compile("[0-9]*");
    public static void showToast(final String string, final Activity activity) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            Toast.makeText(activity, string, Toast.LENGTH_SHORT).show();
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, string, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static boolean isNum(String string) {
        if (string == null || "".equals(string)) {
            return false;
        }

        if (pattern.matcher(string).matches()) {
            return true;
        } else {
            return false;
        }

    }

    public static ExecutorService executors = Executors.newCachedThreadPool();

    public static void doAndToast(final MCallback mCallback, final Activity activity) {
        executors.execute(new Runnable() {
            @Override
            public void run() {

                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(activity);
                        }
                    });
                }

                String s;
                try {
                    s = mCallback.onDo();
                } catch (Exception e) {
                    s = e.toString();
                    ZLog.e(e);
                }
                final String finalS = s;
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mCallback.onResult(finalS);
                            } catch (Exception e) {
                                showToast("异常：" + e, activity);
                                ZLog.e(e);
                            }
                            if (finalS != null) {
                                showToast("结果：" + finalS, activity);
                            }
                            dismissProgress();
                        }
                    });
                }
            }
        });

    }

    private static Dialog progressDialog;

    public static synchronized void showProgress(Activity activity) {
        ZLog.i("show progress");
        if (progressDialog == null) {
            progressDialog = new Dialog(activity, R.style.progress_dialog);
            progressDialog.setContentView(R.layout.layout_dialog);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//            progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//                    ZLog.i("progress will be null");
//                    dismissProgress();
//                }
//            });
            TextView msg = (TextView) progressDialog.findViewById(R.id.id_tv_loadingmsg);
            msg.setText("加载中");
            ZLog.i("progress will be created");
        }

        if (!progressDialog.isShowing() &&
                activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            ZLog.i("progress will be show");
            try {
                progressDialog.show();
            } catch (Exception e) {
                ZLog.e(e);
            }
        }
    }

    public static synchronized void dismissProgress() {
        ZLog.i("dismiss progress");
        if (progressDialog != null) {
            ZLog.i("progress will be dismiss");
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                ZLog.e(e);
            }
            progressDialog = null;
        }
    }

    private static String getLevelStr(int level) {
        StringBuffer levelStr = new StringBuffer();
        for (int levelI = 0; levelI < level; levelI++) {
            levelStr.append("\t");
        }
        return levelStr.toString();
    }

    public static String JsonFormart(String s) {
        int level = 0;
        //存放格式化的json字符串
        StringBuffer jsonForMatStr = new StringBuffer();
        for (int index = 0; index < s.length(); index++)//将字符串中的字符逐个按行输出
        {
            //获取s中的每个字符
            char c = s.charAt(index);
//          System.out.println(s.charAt(index));

            //level大于0并且jsonForMatStr中的最后一个字符为\n,jsonForMatStr加入\t
            if (level > 0 && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
                jsonForMatStr.append(getLevelStr(level));
//                System.out.println("123"+jsonForMatStr);
            }
            //遇到"{"和"["要增加空格和换行，遇到"}"和"]"要减少空格，以对应，遇到","要换行
            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c + "\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c + "\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
        return jsonForMatStr.toString();
    }
}
