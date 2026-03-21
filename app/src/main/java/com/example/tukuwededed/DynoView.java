package com.example.tukuwededed;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class DynoView extends View {
    private List<Float> dataPoints = new ArrayList<>();
    private Paint linePaint, gridPaint;

    public DynoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#4ADE80")); // racing_green
        linePaint.setStrokeWidth(5);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);

        gridPaint = new Paint();
        gridPaint.setColor(Color.WHITE);
        gridPaint.setAlpha(30);
    }

    public void setData(List<Float> points) {
        this.dataPoints = points;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (dataPoints.size() < 2) return;

        float width = getWidth();
        float height = getHeight();
        float maxVal = 0;
        for (float f : dataPoints) if (f > maxVal) maxVal = f;
        if (maxVal == 0) maxVal = 1;

        Path path = new Path();
        for (int i = 0; i < dataPoints.size(); i++) {
            float x = (float) i / (dataPoints.size() - 1) * width;
            float y = height - (dataPoints.get(i) / maxVal * height);
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        canvas.drawPath(path, linePaint);
    }
}