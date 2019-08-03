package xyz.trankvila.menteiaalirilo.views

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Region
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import org.joda.time.LocalTime
import xyz.trankvila.menteiaalirilo.fragments.ClockFragment

class PhaseIconView extends View {
    PhaseIconView(Context context, AttributeSet attrs) {
        super(context, attrs)
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final now = LocalTime.now()
        final phase = now.hourOfDay.intdiv(8).intValue()
        final paint = new Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = ContextCompat.getColor(context, ClockFragment.phaseColors[phase])
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 23
        paint.strokeCap = Paint.Cap.ROUND
        final path = new Path()
        switch (phase) {
            case 0:
                path.moveTo(0, height / 2)
                path.lineTo(width, height / 2)
                path.moveTo(width / 2, 0)
                path.lineTo(width / 2, height)
                break
            case 1:
                path.moveTo(0, height)
                path.lineTo(0, height / 2)
                path.lineTo(width, height / 2)
                path.lineTo(width, 0)
                path.moveTo(0, height / 2)
                path.addArc(0, 0, width / 2, height, 180, 180)
                path.addArc(width / 2, 0, width, height, 0, 180)
                break
            case 2:
                path.moveTo(width * 0.35, 0)
                path.lineTo(0, 0)
                path.lineTo(0, height)
                path.lineTo(width * 0.7, height)
                path.moveTo(width, height)
                path.lineTo(width, 0)
                path.lineTo(width * 0.65, 0)
                break
        }

        final shadowPaint = new Paint(0)
        shadowPaint.style = Paint.Style.STROKE
        shadowPaint.strokeWidth = 23
        shadowPaint.strokeCap = Paint.Cap.ROUND
        shadowPaint.color = (int) 0xff101010
        shadowPaint.maskFilter = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL)

        final save = canvas.save()
        try {
            canvas.translate(10, 10)
            canvas.drawPath(path, shadowPaint)
        } finally {
            canvas.restoreToCount(save)
        }

        canvas.drawPath(path, paint)
    }
}
