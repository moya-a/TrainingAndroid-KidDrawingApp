package training.udemy.kiddrawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context : Context, attrs : AttributeSet) : View(context, attrs)
{
  private val LOGTAG : String = "in_class_${DrawingView::class.simpleName}"

  companion object Config
  {
    val BITMAP_CONFIG : Bitmap.Config = Bitmap.Config.ARGB_8888
  }

  private val pathList : ArrayList<CustomPath> by lazy {
    ArrayList<CustomPath>()
  }

  // initialized in setUp / init function
  private lateinit var drawPath : CustomPath
  private lateinit var drawPaint : Paint
  private lateinit var canvasPaint : Paint

  // initialized in event
  private var canvas : Canvas? = null
  private var canvasBitmap : Bitmap? = null

  var brushSize : Float = 5f
    private set
  var color : Int = Color.BLACK
    private set

  // used to control the undo and redo features
  private var pathIndex = 0
  private var pathIndexMax = 0

  init
  {
    setUpDrawing()
  }

  private fun setUpDrawing()
  {
    drawPaint = Paint()
    drawPath = CustomPath(color, brushSize)
    // setting up the brush
    drawPaint.color = color
    drawPaint.style = Paint.Style.STROKE
    drawPaint.strokeJoin = Paint.Join.ROUND
    drawPaint.strokeCap = Paint.Cap.ROUND
    // setting up the canvas
    canvasPaint = Paint(Paint.DITHER_FLAG)
  }

  fun undo()
  {
    if (0 < pathIndex) --pathIndex
    invalidate()
  }

  fun redo()
  {
    if (pathIndexMax > pathIndex) ++pathIndex
    invalidate()
  }

  fun reset()
  {
    pathIndexMax = 0
    pathIndex = 0
    pathList.clear()
    drawPath.reset()
    invalidate()
  }

  fun setDrawingBrushSize(size : Float)
  {
    brushSize = size
    drawPaint.strokeWidth = brushSize
  }

  fun setDrawingColor(newColor : Int)
  {
    color = newColor
    drawPaint.color = color
  }

  override fun onSizeChanged(width : Int, height : Int, oldWidth : Int, oldHeight : Int)
  {
    super.onSizeChanged(width, height, oldWidth, oldHeight)
    canvasBitmap = Bitmap.createBitmap(width, height, BITMAP_CONFIG)
    canvas = Canvas(canvasBitmap!!)
  }

  override fun onDraw(canvas : Canvas?)
  {
    canvas?.let { c ->
      c.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)
      if (0 < pathList.size)
      {
        for (i in 0 until pathIndex)
        {
          val path = pathList[i]
          if (path != drawPath && !path.isEmpty)
          {
            drawPaint.strokeWidth = path.thickness
            drawPaint.color = path.color
            c.drawPath(path, drawPaint)
          }
        }
      }
      if (!drawPath.isEmpty)
      {
        drawPaint.strokeWidth = drawPath.thickness
        drawPaint.color = drawPath.color
        c.drawPath(drawPath, drawPaint)
      }
    }
  }

  fun touchedEvent(event : MotionEvent?) : Boolean
  {
    event?.let { e ->
      val x = e.x
      val y = e.y
      when (e.action)
      {
        MotionEvent.ACTION_DOWN ->
        {
          drawPath.reset()
          drawPath.color = color
          drawPath.thickness = brushSize
          drawPath.moveTo(x, y)
          drawPath.lineTo(x, y)
        }
        MotionEvent.ACTION_MOVE ->
        {
          drawPath.lineTo(x, y)
        }
        MotionEvent.ACTION_UP   ->
        {
          pathList.add(pathIndex++, drawPath)
          pathIndexMax = pathIndex
          drawPath = CustomPath(color, brushSize)
        }
        else                    -> return false
      }
    }
    invalidate()
    return true
  }

  fun canUndo() : Boolean
  {
    return 0 < pathIndex
  }

  fun canRedo() : Boolean
  {
    return pathIndexMax > pathIndex
  }

  fun canReset() : Boolean
  {
    return 0 < pathIndexMax
  }

  internal inner class CustomPath(var color : Int, var thickness : Float) : Path()
}