package com.example.myapplication
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

class MyFingerView : AppCompatImageView {
    //设置绘制模式是“画笔”还是“橡皮擦”
    var mode = Pen
    private lateinit var mBitmap: Bitmap
    private lateinit var mCanvas: Canvas
    private lateinit var mEraserPaint: Paint
    lateinit var mPaint: Paint
    private lateinit var mPath: Path
    private var mX = 0f
    private var mY = 0f

    constructor(context: Context?) : super(context!!) {
        initPaint()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        initPaint()
    }

    private fun initPaint() {
        //画笔
        mPaint = Paint()
        mPaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = Color.BLACK
            strokeWidth = 10f
        }

        //橡皮擦
        mEraserPaint = Paint()
        mEraserPaint.apply {
            alpha = 0
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN) //这个属性是设置paint为橡皮擦
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeWidth = 30f
        }
        mPath = Path()
    }

    fun reset() {
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!this::mBitmap.isInitialized) {
            mBitmap = Bitmap.createBitmap(right - left, bottom - top, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(mBitmap)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mBitmap, 0f, 0f, mPaint)
    }

    private fun drawPath() {
        if (mode == Pen) { //如果是“画笔”模式就用mPaint画笔进行绘制
            mCanvas.drawPath(mPath, mPaint)
        } else if (mode == Eraser) { //如果是“橡皮擦”模式就用mEraserPaint画笔进行绘制
            mCanvas.drawPath(mPath, mEraserPaint)
        }
    }

    private fun touchStart(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
        drawPath()
    }

    private fun touchMove(x: Float, y: Float) {
        mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
        mX = x
        mY = y
        drawPath()
    }

    private fun touchUp() {
        mPath.lineTo(mX, mY)
        drawPath()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    companion object {
        var Pen = 1
        var Eraser = 2
    }
}