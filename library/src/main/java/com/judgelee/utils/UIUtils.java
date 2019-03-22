package com.judgelee.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Author: judgelee
 * Date: 2019/3/21
 * Desc:
 */
public class UIUtils {

  public static int dp2px(Context context, float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }

  public static int px2dp(Context context, float pxValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (pxValue / scale + 0.5f);
  }

  public static int sp2px(Context context, float spValue) {
    final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
    return (int) (spValue * fontScale + 0.5f);
  }

  public static Bitmap drawableToBitmap(Drawable drawable, int outWidth, int outHeight) {
    if (drawable == null) {
      return null;
    }
    if (drawable instanceof BitmapDrawable) {
      BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
      return bitmapDrawable.getBitmap();
    }
    int width = drawable.getIntrinsicWidth() <= 0 ? outWidth : drawable.getIntrinsicWidth();
    int height = drawable.getIntrinsicHeight() <= 0 ? outHeight : drawable.getIntrinsicHeight();
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, width, height);
    drawable.draw(canvas);
    return bitmap;
  }
}
