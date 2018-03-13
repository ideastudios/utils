package tech.oom.julian.utils;

import android.app.Activity;
import android.view.View;

/**
 * Created by issuser on 2018/3/13 0013.
 */

public class StatusBarNavigationUtils {
    /**
     * 全屏隐藏状态栏和导航栏 可以在Activity中的 onResume 中调用
     *
     * @param activity
     */
    public static void hideStatusBarAndNavigation(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
