package io.anyline.examples.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by hanna on 10.03.17.
 */

public class InactiveCutoutView extends View {
    int x = 0;
    int y = 0;
    int width = 0;
    int height = 0;
    int cornerRadius = 0;
    int strokeWidth = 0;
    RectF rect = new RectF(0, 0, 0, 0);


    private Paint paint = new Paint();

    public InactiveCutoutView(Context context) {
        super(context);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);

        this.setWillNotDraw(false);
    }

    public InactiveCutoutView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);

        this.setWillNotDraw(false);
    }

    public void setDimensions(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rect = new RectF(x, y, x + width, y + height);

    }

    public void setCornerRadius(int cornerRadiusInDp) {
        this.cornerRadius = dpToPx(cornerRadiusInDp);
    }

    public void setStrokeWidth(int strokeWidthInDp) {
        this.strokeWidth = dpToPx(strokeWidthInDp);
        paint.setStrokeWidth(this.strokeWidth);
    }

    public void setColor(int color) {
        paint.setColor(color);
        paint.setAlpha(150);
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}
