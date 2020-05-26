package training.udemy.kiddrawingapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import kotlinx.android.synthetic.main.settings_fragment.*

class SettingsFragment(controller : MainActivity.FragmentController) :
  ControllableFragment(controller)
{
  private val LOGTAG : String = "in_class_${SettingsFragment::class.java.simpleName}"

  private lateinit var btnUndo : ImageButton
  private lateinit var btnRedo : ImageButton
  private lateinit var btnReset : ImageButton
  private lateinit var btnBrushSettings : ImageButton

  companion object
  {
    enum class ButtonType
    {
      BUTTON_UNDO, BUTTON_REDO, BUTTON_RESET
    }

    enum class ButtonState(val state : Boolean)
    {
      BUTTON_ENABLED(true), BUTTON_DISABLED(false)
    }
  }

  override fun onCreateView(
    inflater : LayoutInflater,
    container : ViewGroup?,
    savedInstanceState : Bundle?
  ) : View?
  {
    return inflater.inflate(R.layout.settings_fragment, container, false)
  }

  override fun onActivityCreated(savedInstanceState : Bundle?)
  {
    super.onActivityCreated(savedInstanceState)

    btnUndo = btn_undo
    btnRedo = btn_redo
    btnReset = btn_reset
    btnBrushSettings = btn_brush_settings

    btn_undo.setOnClickListener {
      controller.drawingUndo()
    }
    btn_redo.setOnClickListener {
      controller.drawingRedo()
    }
    btn_reset.setOnClickListener {
      controller.drawingReset()
      controller.resetBackgroundImage(true)
    }
    btn_brush_settings.setOnClickListener {
      controller.showFragment(MainActivity.Companion.FragmentType.BRUSH_SETTINGS)
    }
    btn_hide_settings.setOnClickListener {
      controller.hideFragment(this)
    }

    btn_add_image.setOnClickListener {
      controller.importBackgroundImage()
    }

    btn_save.setOnClickListener {
      controller.exportImage()
    }

    btn_share.setOnClickListener {
      controller.share()
    }

    controller.settingsUpdateButtons()
    controller.hideFragment(this)
  }

  fun updateButton(type : ButtonType, state : Boolean)
  {
    if (state) updateButton(type, ButtonState.BUTTON_ENABLED)
    else updateButton(type, ButtonState.BUTTON_DISABLED)
  }

  fun updateButton(type : ButtonType, state : ButtonState)
  {
    val btn = when (type)
    {
      ButtonType.BUTTON_UNDO  -> btnUndo
      ButtonType.BUTTON_REDO  -> btnRedo
      ButtonType.BUTTON_RESET -> btnReset
    }
    when (state)
    {
      ButtonState.BUTTON_ENABLED  ->
      {
        btn.isEnabled = true
        btn.setColorFilter(Color.WHITE)
      }
      ButtonState.BUTTON_DISABLED ->
      {
        btn.isEnabled = false
        btn.setColorFilter(Color.GRAY)
      }
    }
  }
}