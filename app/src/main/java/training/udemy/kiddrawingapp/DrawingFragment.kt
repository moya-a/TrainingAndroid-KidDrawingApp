package training.udemy.kiddrawingapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.drawing_fragment.*

class DrawingFragment(controller : MainActivity.FragmentController) :
  ControllableFragment(controller)
{
  private val LOGTAG : String = "in_class_${DrawingFragment::class.simpleName}"

  override fun onCreateView(
    inflater : LayoutInflater,
    container : ViewGroup?,
    savedInstanceState : Bundle?
  ) : View?
  {
    return inflater.inflate(R.layout.drawing_fragment, container, false)
  }

  override fun onActivityCreated(savedInstanceState : Bundle?)
  {
    super.onActivityCreated(savedInstanceState)
    drawing_view.setOnTouchListener(SimpleTouchListener())
  }

  fun hasNotBackgroundImage() : Boolean
  {
    return null == iv_background_image.drawable
  }

  fun hasBackgroundImage() : Boolean
  {
    return null != iv_background_image.drawable
  }

  fun getBitmap(withBackground : Boolean = false) : Bitmap
  {
    return Bitmap.createBitmap(
      drawing_view.width, drawing_view.height,
      DrawingView.BITMAP_CONFIG
    ).apply {
      Canvas(this).let { canvas ->
        if (withBackground)
          iv_background_image.draw(canvas)
        else
          canvas.drawColor(Color.WHITE)
        drawing_view.draw(canvas)
      }
    } // apply returns the bitmap
  }

  private inner class SimpleTouchListener : View.OnTouchListener
  {
    override fun onTouch(v : View?, event : MotionEvent?) : Boolean
    {
      val result = drawing_view.touchedEvent(event)
      controller.settingsUpdateButtons()
      return result
    }
  }
}

