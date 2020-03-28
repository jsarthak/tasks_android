package com.offbeat.apps.tasks.Utils;

import android.content.Context;
import android.graphics.Typeface;

import java.lang.reflect.Field;

public class FontsOverride {
    public static void setDefaultFont(Context context, String typfaceName, String fontAssetName) {
        final Typeface regular = Typeface.createFromAsset(context.getAssets(), fontAssetName);
        replaceFont(typfaceName, regular);
    }

    protected static void replaceFont(String typfaceName, final Typeface newTypeFace) {
        try {
            final Field staticField = Typeface.class.getDeclaredField(typfaceName);
            staticField.setAccessible(true);
            staticField.set(null, newTypeFace);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
