package com.example.tyaathome.mosaic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.tyaathome.mosaic.R;

/**
 * Created by tyaathome on 2016/8/18.
 */
public class MosaicView extends View {

    private int mImageWidth;
    private int mImageHeight;

    private int mPadding = 6*3;
    private int mGridWidth = 20*3;

    private Rect mImageRect = new Rect();

    private Bitmap bmSourceLayout;
    private Bitmap bmMosaicLayout;
    private Bitmap mBitmap;

    private Paint mPaint = new Paint();
    private Paint mPaintMosaic = new Paint();
    private Paint mPaintClear = new Paint();
    private Paint mPaintDSTIN = new Paint();
    private Path mPath = new Path();

    private float mX;
    private float mY;

    private Canvas mCanvas;

    public MosaicView(Context context) {
        super(context);
    }

    public MosaicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MosaicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context) {
        bmSourceLayout = BitmapFactory.decodeResource(context.getResources(), R.mipmap.source);
        mImageWidth = bmSourceLayout.getWidth();
        mImageHeight = bmSourceLayout.getHeight();
        bmMosaicLayout = getGridMosaic();
        requestLayout();
        initPaint();
    }

    private void initPaint() {

        mPaintMosaic = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMosaic.setStyle(Paint.Style.STROKE);
        mPaintMosaic.setAntiAlias(true);
        mPaintMosaic.setStrokeJoin(Paint.Join.ROUND);
        mPaintMosaic.setStrokeCap(Paint.Cap.ROUND);
        mPaintMosaic.setPathEffect(new CornerPathEffect(10));
        mPaintMosaic.setStrokeWidth(20*3);

        mPaintClear = new Paint(mPaintMosaic);
        mPaintClear.setColor(Color.TRANSPARENT);
        mPaintClear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mPaintDSTIN = new Paint(mPaintMosaic);
        mPaintDSTIN.setColor(Color.TRANSPARENT);
        mPaintDSTIN.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

    }

    private Bitmap getGridMosaic() {
        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int horCount = (int) Math.ceil(mImageWidth / (float) mGridWidth);
        int verCount = (int) Math.ceil(mImageHeight / (float) mGridWidth);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        for (int horIndex = 0; horIndex < horCount; ++horIndex) {
            for (int verIndex = 0; verIndex < verCount; ++verIndex) {
                int l = mGridWidth * horIndex;
                int t = mGridWidth * verIndex;
                int r = l + mGridWidth;
                if (r > mImageWidth) {
                    r = mImageWidth;
                }
                int b = t + mGridWidth;
                if (b > mImageHeight) {
                    b = mImageHeight;
                }
                int color = bmSourceLayout.getPixel(l, t);
                Rect rect = new Rect(l, t, r, b);
                paint.setColor(color);
                canvas.drawRect(rect, paint);
            }
        }
        canvas.save();
        return bitmap;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return;
        }

        int contentWidth = right - left;
        int contentHeight = bottom - top;
        int viewWidth = contentWidth - mPadding * 2;
        int viewHeight = contentHeight - mPadding * 2;
        float widthRatio = viewWidth / ((float) mImageWidth);
        float heightRatio = viewHeight / ((float) mImageHeight);
        float ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
        int realWidth = (int) (mImageWidth * ratio);
        int realHeight = (int) (mImageHeight * ratio);

        int imageLeft = (contentWidth - realWidth) / 2;
        int imageTop = (contentHeight - realHeight) / 2;
        int imageRight = imageLeft + realWidth;
        int imageBottom = imageTop + realHeight;
        mImageRect.set(imageLeft, imageTop, imageRight, imageBottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.moveTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(event.getX(), event.getY());
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制马赛克图片(底图为马赛克图)
        canvas.drawBitmap(bmMosaicLayout, null, mImageRect, null);
        // 保存
        int layerId = canvas.saveLayer(0, 0, mImageWidth, mImageHeight, null, Canvas.ALL_SAVE_FLAG);
        canvas.drawBitmap(bmSourceLayout, null, mImageRect, null);
        canvas.drawPath(mPath, mPaintClear);
        canvas.restoreToCount(layerId);

    }
}
