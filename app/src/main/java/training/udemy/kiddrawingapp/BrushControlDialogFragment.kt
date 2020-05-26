package training.udemy.kiddrawingapp

import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import kotlinx.android.synthetic.main.dialog_brush_control_fragment.*

class BrushControlDialogFragment(controller : MainActivity.FragmentController) :
  ControllableFragment(controller)
{
  private val LOGTAG : String = "in_class_${BrushControlDialogFragment::class.java.simpleName}"

  private var brushSize : Float = 0f
  private var brushColorRed : Int = 0
  private var brushColorGreen : Int = 0
  private var brushColorBlue : Int = 0
  private var brushColor : Int = Color.BLACK

  override fun onCreateView(
    inflater : LayoutInflater,
    container : ViewGroup?,
    savedInstanceState : Bundle?
  ) : View?
  {
    return inflater.inflate(R.layout.dialog_brush_control_fragment, container, false)
  }

  override fun onActivityCreated(savedInstanceState : Bundle?)
  {
    super.onActivityCreated(savedInstanceState)

    // setting listeners to the seekbars
    sb_brushSize.setOnSeekBarChangeListener(SimpleSeekBarOnProgressChangedListener())
    sb_brushColorRed.setOnSeekBarChangeListener(SimpleSeekBarOnProgressChangedListener())
    sb_brushColorGreen.setOnSeekBarChangeListener(SimpleSeekBarOnProgressChangedListener())
    sb_brushColorBlue.setOnSeekBarChangeListener(SimpleSeekBarOnProgressChangedListener())
    // initalize seekbar values
    brushSize = controller.getDimensionedSize(5f)
    sb_brushSize.progress = brushSize.toInt()

    tv_sizeTitle.height = controller.getDimensionedSize(50).toInt()

    btn_brushDialog_hide.setOnClickListener {
      controller.hideFragment(this)
    }
    controller.setDrawingBrushSize(brushSize)
    updatePreview()
    controller.hideFragment(this)
  }

  fun updatePreview()
  {
    val oval = OvalShape()
    oval.resize(brushSize, brushSize)
    tv_brushPreview.background = ShapeDrawable(oval).apply {
      intrinsicHeight = brushSize.toInt()
      intrinsicWidth = brushSize.toInt()
      paint.color = this@BrushControlDialogFragment.brushColor
      paint.style = Paint.Style.FILL
    }
    tv_brushPreview.height = brushSize.toInt()
    tv_brushPreview.width = brushSize.toInt()
  }

  fun updateSize(newSize : Int)
  {
    brushSize = controller.getDimensionedSize(newSize)
    controller.setDrawingBrushSize(brushSize)
  }

  fun updateColor()
  {
    brushColor = Color.argb(255, brushColorRed, brushColorGreen, brushColorBlue)
    controller.setDrawingBrushColor(brushColor)
  }

  private inner class SimpleSeekBarOnProgressChangedListener : SeekBar.OnSeekBarChangeListener
  {
    private fun processEvent(seekBar : SeekBar?)
    {
      seekBar?.let {
        when (it.tag)
        {
          "SIZE"        -> updateSize(it.progress)
          "COLOR_RED"   -> brushColorRed = it.progress
          "COLOR_GREEN" -> brushColorGreen = it.progress
          "COLOR_BLUE"  -> brushColorBlue = it.progress
        }
        updateColor()
      }
      updatePreview()
    }

    override fun onProgressChanged(seekBar : SeekBar?, progress : Int, fromUser : Boolean)
    {
      processEvent(seekBar)
    }

    override fun onStartTrackingTouch(seekBar : SeekBar?)
    {
    }

    override fun onStopTrackingTouch(seekBar : SeekBar?)
    {
      processEvent(seekBar)
    }
  }
}