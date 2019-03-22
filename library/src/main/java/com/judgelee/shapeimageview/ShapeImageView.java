package com.judgelee.shapeimageview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;
import com.judgelee.library.R;
import com.judgelee.utils.UIUtils;

/**
 * Author: judgelee
 * Date: 2019/3/21
 * Desc: 支持指定Shape的图片
 * 方式有三种：
 *    1.透明图覆盖 （绘制效率低，且需要制作图片，不推荐）
 *    2.BitmapShader （操作灵活，此处使用该方式）
 *    3.Xfermode （图层叠加，效率低）
 *    4.v4包 RoundBitmapDrawable （只能实现圆角矩形图片，内部使用BitmapShader）
 *    5.v7包 CardView （功能强大，必须5.0系统以上显示圆角）
 */
public class ShapeImageView extends AppCompatImageView {

  public static final int SHAPE_CIRCLE = 0x01;
  public static final int SHAPE_OVAL = 0x02;
  public static final int SHAPE_RECTANGLE = 0x03;
  public static final int SHAPE_ROUNDRECTANGLE = 0x04;

  private static final int DEFAULT_IMAGE_SHAPE = SHAPE_RECTANGLE;
  private static final int DEFAULT_SHAPE_RADIUSX = 5;
  private static final int DEFAULT_SHAPE_RADIUSY = 5;
  private static final int DEFAULT_SHAPE_BORDER_WIDTH = 0;
  private static final int DEFAULT_SHAPE_BORDER_COLOR = Color.BLACK;
  private static final int DEFAULT_IMAGE_TEXT_SIZE = 14;
  private static final int DEFAULT_IMAGE_TEXT_COLOR = Color.BLACK;

  private Paint mPaint;
  private Paint mBorderPaint;
  private Paint mTextPaint;

  private int mImageShape;
  /**
   * 圆角矩形 X方向半径
   */
  private int mRadiusX;
  /**
   * 圆角矩形 Y方向半径
   */
  private int mRadiusY;
  private int mBorderWidth;
  private int mBorderColor;
  private String mText;
  private int mTextSize;
  private int mTextColor;

  public ShapeImageView(Context context) {
    this(context, null);
  }

  public ShapeImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ShapeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShapeImageView);
    mImageShape = a.getInt(R.styleable.ShapeImageView_imageShape, DEFAULT_IMAGE_SHAPE);
    mRadiusX = a.getDimensionPixelSize(R.styleable.ShapeImageView_shapeRadiusX, DEFAULT_SHAPE_RADIUSX);
    mRadiusY = a.getDimensionPixelSize(R.styleable.ShapeImageView_shapeRadiusX, DEFAULT_SHAPE_RADIUSY);
    mBorderWidth = a.getDimensionPixelSize(R.styleable.ShapeImageView_shapeBorderWidth, DEFAULT_SHAPE_BORDER_WIDTH);
    mBorderColor = a.getInt(R.styleable.ShapeImageView_shapeBorderColor, DEFAULT_SHAPE_BORDER_COLOR);
    mText = a.getString(R.styleable.ShapeImageView_imageText);
    mTextSize = a.getDimensionPixelSize(R.styleable.ShapeImageView_imageTextSize,
        UIUtils.sp2px(context, DEFAULT_IMAGE_TEXT_SIZE));
    mTextColor = a.getColor(R.styleable.ShapeImageView_imageTextColor, DEFAULT_IMAGE_TEXT_COLOR);
    a.recycle();

    initPaint();
  }

  private void initPaint() {
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mBorderPaint.setStyle(Paint.Style.STROKE);
    mBorderPaint.setColor(mBorderColor);
    mBorderPaint.setStrokeWidth(mBorderWidth);

    mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mTextPaint.setColor(mTextColor);
    mTextPaint.setTextSize(mTextSize);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // TODO: 2019/3/22 没有考虑padding
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int desiredWidth = getMeasuredWidth();
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    if (widthMode != MeasureSpec.EXACTLY) {
      desiredWidth = (int) ((mBorderWidth << 1) + Math.max(getMeasuredWidth(), getTextWidth()) + 0.5f);
    }

    int desiredHeight = getMeasuredHeight();
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    if (heightMode != MeasureSpec.EXACTLY) {
      desiredHeight = (int) ((mBorderWidth << 1) + Math.max(getMeasuredHeight(), getTextHeight()) + 0.5f);
    }

    int width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0);
    int height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0);

    // 圆形强制宽高一致
    if (mImageShape == SHAPE_CIRCLE) {
      width = Math.min(width, height);
      height = width;
    }
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    // TODO: 2019/3/22 没有考虑Padding
    // TODO: 2019/3/22 没有考虑ScaleType 目前默认使用fitXY模式
    Bitmap bitmap = UIUtils.drawableToBitmap(getDrawable(), getWidth(), getHeight());
    float widthScale = getWidth() * 1.0f / bitmap.getWidth();
    float heightScale = getHeight() * 1.0f / bitmap.getHeight();
    Matrix matrix = new Matrix();
    matrix.setScale(widthScale, heightScale);
    BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
    bitmapShader.setLocalMatrix(matrix);
    mPaint.setShader(bitmapShader);

    switch (mImageShape) {
      case SHAPE_CIRCLE:
        drawCircle(canvas);
        break;
      case SHAPE_OVAL:
        drawOval(canvas);
        break;
      case SHAPE_RECTANGLE:
        drawRectangle(canvas);
        break;
      case SHAPE_ROUNDRECTANGLE:
        drawRoundRectangle(canvas);
        break;
      default:
        drawRectangle(canvas);
        break;
    }

    drawText(canvas);
  }

  private void drawCircle(Canvas canvas) {
    float radius = (getWidth() - ((mBorderWidth << 1))) / 2.0f;
    canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, radius, mPaint);
    canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, radius - mBorderWidth / 2.0f, mBorderPaint);
  }

  private void drawOval(Canvas canvas) {
    canvas.drawOval(mBorderWidth, mBorderWidth,
        getWidth() - mBorderWidth, getHeight() - mBorderWidth, mPaint);
    float offset = mBorderWidth / 2.0f;
    canvas.drawOval(offset, offset, getWidth() - offset, getHeight() - offset, mBorderPaint);
  }

  private void drawRectangle(Canvas canvas) {
    canvas.drawRect(mBorderWidth, mBorderWidth,
        getWidth() - mBorderWidth, getHeight() - mBorderWidth, mPaint);
    float offset = mBorderWidth / 2.0f;
    canvas.drawRect(offset, offset, getWidth() - offset, getHeight() - offset, mBorderPaint);
  }

  private void drawRoundRectangle(Canvas canvas) {
    canvas.drawRoundRect(mBorderWidth, mBorderWidth, getWidth() - mBorderWidth,
        getHeight() - mBorderWidth, mRadiusX, mRadiusY, mPaint);
    float offset = mBorderWidth / 2.0f;
    canvas.drawRoundRect(offset, offset, getWidth() - offset, getHeight() - offset,
        mRadiusX, mRadiusY, mBorderPaint);
  }

  private void drawText(Canvas canvas) {
    if (TextUtils.isEmpty(mText)) {
      return;
    }
    Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
    float baseline = getHeight() / 2.0f - (fontMetrics.bottom - fontMetrics.top) / 2.0f - fontMetrics.top;
    canvas.drawText(mText, (getWidth() - getTextWidth()) / 2.0f, baseline, mTextPaint);
  }

  public Paint getPaint() {
    return mPaint;
  }

  public void setPaint(Paint paint) {
    mPaint = paint;
    invalidate();
  }

  public Paint getBorderPaint() {
    return mBorderPaint;
  }

  public void setBorderPaint(Paint borderPaint) {
    mBorderPaint = borderPaint;
    invalidate();
  }

  public int getImageShape() {
    return mImageShape;
  }

  public void setImageShape(int imageShape) {
    mImageShape = imageShape;
    invalidate();
  }

  public int getRadiusX() {
    return mRadiusX;
  }

  public void setRadiusX(int radiusX) {
    mRadiusX = radiusX;
    invalidate();
  }

  public int getRadiusY() {
    return mRadiusY;
  }

  public void setRadiusY(int radiusY) {
    mRadiusY = radiusY;
    invalidate();
  }

  public int getBorderWidth() {
    return mBorderWidth;
  }

  public void setBorderWidth(int borderWidth) {
    mBorderWidth = borderWidth;
    invalidate();
  }

  public int getBorderColor() {
    return mBorderColor;
  }

  public void setBorderColor(int borderColor) {
    mBorderColor = borderColor;
    invalidate();
  }

  public String getText() {
    return mText;
  }

  public void setText(String text) {
    mText = text;
    invalidate();
  }

  public int getTextSize() {
    return mTextSize;
  }

  public void setTextSize(int textSize) {
    mTextSize = textSize;
    invalidate();
  }

  public int getTextColor() {
    return mTextColor;
  }

  public void setTextColor(int textColor) {
    mTextColor = textColor;
    invalidate();
  }

  public float getTextWidth() {
    if (TextUtils.isEmpty(mText)) {
      return 0;
    }
    return mTextPaint.measureText(mText);
  }

  public float getTextHeight() {
    if (TextUtils.isEmpty(mText)) {
      return 0;
    }
    Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
    return fontMetrics.bottom - fontMetrics.top;
  }
}
