package com.offbeat.apps.tasks.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.offbeat.apps.tasks.R;

public class Utils {
    Context mContext;

    public Utils(Context mContext) {
        this.mContext = mContext;
    }

    public static void setTypeFace(Context context) {
        FontsOverride.setDefaultFont(context, "DEFAULT", "font/prod_sans_regular.ttf");
        FontsOverride.setDefaultFont(context, "MONOSPACE", "font/prod_sans_regular.ttf");
        FontsOverride.setDefaultFont(context, "SERIF", "font/prod_sans_regular.ttf");
        FontsOverride.setDefaultFont(context, "SANS_SERIF", "font/prod_sans_regular.ttf");
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public static void setLightStatusBar(Context context, View view, Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(context.getColor(R.color.colorPrimary));
        }
    }
}
