package com.zhilai.facelibrary.zlfacerecog.faceutil.camera;

import android.app.Activity;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;

import java.util.List;

public class CameraUtil {
    private static String TAG = "CameraUtil";

    public static Camera.Size getOptimalPreviewSize(Activity currentActivity,
                                                    List<Camera.Size> sizes, double targetRatio) {

        Point[] points = new Point[sizes.size()];

        int index = 0;
        for (Camera.Size s : sizes) {
            points[index++] = new Point(s.width, s.height);
        }

        int optimalPickIndex = getOptimalPreviewSize(currentActivity, points, targetRatio);
        return (optimalPickIndex == -1) ? null : sizes.get(optimalPickIndex);
    }

    public static int getOptimalPreviewSize(Activity currentActivity,
                                            Point[] sizes, double targetRatio) {
        // Use a very small tolerance because we want an exact match.
        final double ASPECT_TOLERANCE = 0.01;
        if (sizes == null) return -1;

        int optimalSizeIndex = -1;
        double minDiff = Double.MAX_VALUE;

        // Because of bugs of overlay and layout, we sometimes will try to
        // layout the viewfinder in the portrait orientation and thus get the
        // wrong size of preview surface. When we change the preview size, the
        // new overlay will be created before the old one closed, which causes
        // an exception. For now, just get the screen size.
        Point point = getDefaultDisplaySize(currentActivity, new Point());
        int targetHeight = Math.min(point.x, point.y);
        // Try to find an size match aspect ratio and size
        for (int i = 0; i < sizes.length; i++) {
            Point size = sizes[i];
            double ratio = (double) size.x / size.y;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.y - targetHeight) < minDiff) {
                optimalSizeIndex = i;
                minDiff = Math.abs(size.y - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio. This should not happen.
        // Ignore the requirement.
        if (optimalSizeIndex == -1) {
            Log.w(TAG, "No preview size match the aspect ratio");
            minDiff = Double.MAX_VALUE;
            for (int i = 0; i < sizes.length; i++) {
                Point size = sizes[i];
                if (Math.abs(size.y - targetHeight) < minDiff) {
                    optimalSizeIndex = i;
                    minDiff = Math.abs(size.y - targetHeight);
                }
            }
        }
        return optimalSizeIndex;
    }

    private static Point getDefaultDisplaySize(Activity activity, Point size) {
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return size;
    }
}
