package com.qiniu.pili.droid.shortvideo.demo.seeshion.sxve.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.qiniu.pili.droid.shortvideo.demo.seeshion.sxve.model.GroupModel;
import com.qiniu.pili.droid.shortvideo.demo.seeshion.sxve.util.RotationGestureDetector;
import com.qiniu.pili.droid.shortvideo.demo.seeshion.sxve.util.Size;

import java.lang.reflect.Field;

public class TemplateView extends View {
    private static final String                  TAG = "TemplateView";
    private              float                   mMidPntX;
    private              float                   mMidPntY;
    private              GestureDetector         mGestureDetector;
    private              ScaleGestureDetector    mScaleDetector;
    private              RotationGestureDetector mRotationDetector;
    private              GroupModel              mGroup;
    private              float                   mOverallScale;

    public TemplateView(Context context) {
        super(context);
        init();
    }

    public TemplateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TemplateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setupGestureListeners();
    }

    private void setupGestureListeners() {
        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        mRotationDetector = new RotationGestureDetector(new RotationListener());

        try {
            Field minSpan = mScaleDetector.getClass().getDeclaredField("mMinSpan");
            minSpan.setAccessible(true);
            minSpan.set(mScaleDetector, 20);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            mMidPntX = (event.getX(0) + event.getX(1)) / 2 / mOverallScale;
            mMidPntY = (event.getY(0) + event.getY(1)) / 2 / mOverallScale;
        }

        mGestureDetector.onTouchEvent(event);

        mScaleDetector.onTouchEvent(event);

        mRotationDetector.onTouchEvent(event);

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            mGroup.allFingerUp();
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mGroup != null) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);

            Size size = mGroup.getSize();
            if (width * size.getHeight() < height * size.getWidth()) {
                height = width * size.getHeight() / size.getWidth();
                mOverallScale = (float) width / size.getWidth();
            } else {
                width = height * size.getWidth() / size.getHeight();
                mOverallScale = (float) height / size.getHeight();
            }
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.scale(mOverallScale, mOverallScale);
        mGroup.draw(canvas);
        canvas.restore();
    }

    public void setAssetGroup(GroupModel group) {
        mGroup = group;
        group.setTemplateTarget(this);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            mGroup.singleTap(new PointF(e.getX() / mOverallScale, e.getY() / mOverallScale));
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mGroup.down(new PointF(e.getX() / mOverallScale, e.getY() / mOverallScale));
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mGroup.scroll(distanceX / mOverallScale, distanceY / mOverallScale);
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float deltaScale = detector.getScaleFactor();
            mGroup.scale(deltaScale, deltaScale, mMidPntX, mMidPntY);
            return true;
        }
    }

    private class RotationListener extends RotationGestureDetector.SimpleOnRotationGestureListener {
        @Override
        public boolean onRotation(RotationGestureDetector detector) {
            mGroup.rotate(detector.getAngle(), mMidPntX, mMidPntY);
            return true;
        }
    }

}
