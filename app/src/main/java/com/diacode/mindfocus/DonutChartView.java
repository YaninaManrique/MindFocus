package com.diacode.mindfocus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DonutChartView extends View {
    public static class DonutSlice {
        public int color;
        public float percentage;

        public DonutSlice(int color, float percentage) {
            this.color = color;
            this.percentage = percentage;
        }
    }

    private List<DonutSlice> slices = new ArrayList<>();

    private Paint paint;
    private Paint textPaint;
    private RectF oval;

    private float strokeRatio = 0.16f;
    private float gapDegrees = 2f;

    public DonutChartView(Context context) {
        super(context);
        init();
    }

    public DonutChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DonutChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.BUTT);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#8884a0"));
        textPaint.setTextAlign(Paint.Align.CENTER);

        oval = new RectF();
    }

    public void setSlices(List<DonutSlice> data) {
        slices.clear();
        slices.addAll(data);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        float diameter = Math.min(w, h);

        float stroke = diameter * strokeRatio;

        paint.setStrokeWidth(stroke);

        textPaint.setTextSize(diameter * 0.12f);

        float inset = stroke / 2f;

        oval.set(inset, inset, diameter - inset, diameter - inset);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (slices.isEmpty())
            return;

        // Fondo
        paint.setColor(Color.parseColor("#2a2940"));
        canvas.drawOval(oval, paint);

        float total = 0;

        for (DonutSlice s : slices)
            total += s.percentage;

        float startAngle = -90f;

        for (DonutSlice slice : slices) {

            float sweep = (slice.percentage / total) * 360f - gapDegrees;

            if (sweep < 0)
                sweep = 0;

            paint.setColor(slice.color);

            canvas.drawArc(
                    oval,
                    startAngle,
                    sweep,
                    false,
                    paint
            );

            startAngle += sweep + gapDegrees;
        }

        float cx = oval.centerX();

        float cy = oval.centerY() - (textPaint.descent() + textPaint.ascent()) / 2;

        canvas.drawText("totales", cx, cy, textPaint);
    }
}
