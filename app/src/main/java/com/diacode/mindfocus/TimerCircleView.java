package com.diacode.mindfocus;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class TimerCircleView extends View {

    private Paint trackPaint;
    private Paint progressPaint;
    private RectF oval;
    private float progress = 0.77f; // 19:24 of 25:00 = ~77%
    private float strokeWidth;

    public TimerCircleView(Context context) {
        super(context);
        init();
    }

    public TimerCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimerCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        strokeWidth = dpToPx(14);

        // Background track (light purple)
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setColor(0xFFE8E5FF);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        // Progress arc (purple)
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(0xFF6C5CE7);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        oval = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - strokeWidth / 2f - dpToPx(4);

        oval.set(cx - radius, cy - radius, cx + radius, cy + radius);

        // Draw full track
        canvas.drawCircle(cx, cy, radius, trackPaint);

        // Draw progress arc — start at top (-90°), sweep clockwise
        float sweepAngle = 360f * progress;
        canvas.drawArc(oval, -90f, sweepAngle, false, progressPaint);
    }

    /**
     * Set progress (0.0 to 1.0) and redraw.
     */
    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
