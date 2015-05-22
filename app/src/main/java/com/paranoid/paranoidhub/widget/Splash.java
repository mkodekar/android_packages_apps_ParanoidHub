package com.paranoid.paranoidhub.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.paranoid.paranoidhub.R;

/**
 * Created by willi on 09.03.15.
 */
public class Splash extends View {

    private final Paint mPaintCircle;
    private final float density;
    private int radius = 0;
    private int rotate = 0;
    private final Bitmap icon;
    private final Matrix matrix;
    private boolean finished = false;

    public Splash(Context context) {
        this(context, null);
    }

    public Splash(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Splash(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setBackgroundColor(getResources().getColor(R.color.white));
        density = getResources().getDisplayMetrics().density;

        mPaintCircle = new Paint();
        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setStyle(Paint.Style.FILL);
        mPaintCircle.setStrokeCap(Paint.Cap.ROUND);
        mPaintCircle.setColor(Color.WHITE);

        matrix = new Matrix();
        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10);
                        rotate++;
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                invalidate();
                            }
                        });
                        if (finished) break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void finish() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    for (int i = 1; i <= getHeight() / 2; i += 15) {
                        radius = i;
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                invalidate();
                            }
                        });
                        Thread.sleep(15);
                    }
                    ((Activity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setVisibility(GONE);
                            startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
                        }
                    });
                    finished = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        draw(canvas, getWidth(), getHeight(), (int) (radius * density));
    }

    private void draw(Canvas canvas, int x, int y, int radius) {
        if (radius > 0) canvas.drawCircle(x / 2, y / 2, radius, mPaintCircle);
        matrix.postRotate(rotate);
        Bitmap iconRotate = Bitmap.createBitmap(icon, 0, 0, icon.getWidth(), icon.getHeight(), matrix, false);
        canvas.drawBitmap(iconRotate, x / 2 - iconRotate.getWidth() / 2, y / 2 - iconRotate.getHeight() / 2, mPaintCircle);
    }


}
