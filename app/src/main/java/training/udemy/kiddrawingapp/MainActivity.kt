package training.udemy.kiddrawingapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawing_fragment.*
import java.io.File
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity()
{

  /**
   ** Defining "static" constants
   */
  companion object
  {
    private const val OPERATION_IMPORT_BACKGROUND_PICTURE : Int = 1
    private const val OPERATION_TAKE_BACKGROUND_PICTURE : Int = 2

    private const val WRITE_EXTERNAL_STORAGE_PERMISSION : Int = 1

    enum class FragmentType
    {
      SETTINGS, DRAWING, BRUSH_SETTINGS
    }
  }

  /**
   ** Inner class controlling all fragments
   ** this is the brain of the application
   ** all fragments register to this controller when created
   ** fragments only handle the UI functions and call the controller for more complex tasks
   */
  inner class FragmentController
  {
    private val LOGTAG : String by lazy { "in_class_${this::class.simpleName}" }

    /**
     ** Methods controlling the fragments
     ** add fragments to controller
     ** get fragments
     ** show / hide fragments
     */
    fun addFragment(
      fragment : ControllableFragment
    )
    {
      when (fragment)
      {
        is SettingsFragment           -> settingsFragment = fragment
        is DrawingFragment            -> drawingFragment = fragment
        is BrushControlDialogFragment -> brushControlDialogFragment = fragment
        else                          -> Log.e(LOGTAG, "unknown type of $fragment")
      }
    }

    private fun getFragment(
      fragmentType : FragmentType
    ) : ControllableFragment
    {
      return when (fragmentType)
      {
        FragmentType.BRUSH_SETTINGS -> brushControlDialogFragment
        FragmentType.DRAWING        -> drawingFragment
        FragmentType.SETTINGS       -> settingsFragment
      }
    }

    fun showFragment(
      fragmentType : FragmentType,
      animIn : Int = R.anim.slide_in_down,
      animOut : Int = R.anim.slide_out_up
    )
    {
      showFragment(getFragment(fragmentType), animIn, animOut)
    }

    fun showFragment(
      f : ControllableFragment,
      animIn : Int = R.anim.slide_in_down,
      animOut : Int = R.anim.slide_out_up
    )
    {
      supportFragmentManager.beginTransaction()
        .setCustomAnimations(animIn, animOut, animIn, animOut)
        .show(f)
        .commit()
    }

    fun hideFragment(
      fragmentType : FragmentType,
      animIn : Int = R.anim.slide_in_down,
      animOut : Int = R.anim.slide_out_up
    )
    {
      hideFragment(getFragment(fragmentType), animIn, animOut)
    }

    fun hideFragment(
      f : ControllableFragment,
      animIn : Int = R.anim.slide_in_down,
      animOut : Int = R.anim.slide_out_up
    )
    {
      supportFragmentManager.beginTransaction()
        .setCustomAnimations(animIn, animOut, animIn, animOut)
        .hide(f)
        .commit()
    }

    /**
     ** Methods controlling size and color of the drawing brush
     */
    fun setDrawingBrushSize(
      size : Float = 10f
    )
    {
      drawing_view.setDrawingBrushSize(size)
    }

    fun setDrawingBrushColor(
      color : Int
    )
    {
      drawing_view.setDrawingColor(color)
    }

    /**
     ** Methods controlling the drawing actions
     ** undo / redo / reset
     */
    fun drawingUndo()
    {
      drawing_view.undo()
      settingsUpdateButtons()
    }

    fun drawingRedo()
    {
      drawing_view.redo()
      settingsUpdateButtons()
    }

    fun drawingReset()
    {
      drawing_view.reset()
      settingsUpdateButtons()
    }

    /**
     ** Methods controlling the background image
     */
    private fun askToRemoveDrawing()
    {
      AlertDialog.Builder(this@MainActivity)
        .setTitle("Remove drawing")
        .setCancelable(false)
        .setMessage("Do you want to clear the drawing ?")
        .setPositiveButton("Yes") { dialog, _ ->
          controller.drawingReset()
          dialog.dismiss()
        }
        .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        .create().show()
    }

    fun setBackGroundImage(
      uri : Uri
    )
    {
      resetBackgroundImage()
      iv_background_image.setImageURI(uri)
      if (drawing_view.canUndo())
      {
        askToRemoveDrawing()
      }
    }

    fun setBackGroundImage(
      bitmap : Bitmap
    )
    {
      resetBackgroundImage()
      iv_background_image.setImageBitmap(bitmap)
      if (drawing_view.canUndo())
      {
        askToRemoveDrawing()
      }
    }

    fun importBackgroundImage()
    {
      AlertDialog.Builder(this@MainActivity)
        .setTitle("Import Image")
        .setCancelable(true)
        .setMessage("You can import an image from gallery or take a picture from the camera")
        .setPositiveButton("Import Image") { dialog, _ ->
          Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .also { importPictureIntent ->
              importPictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(importPictureIntent, OPERATION_IMPORT_BACKGROUND_PICTURE)
              }
              dialog.dismiss()
            }
        }.also { builder ->
          if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY))
          {
            builder.setNegativeButton("Open Camera") { dialog, _ ->
              Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .also { takePictureIntent ->
                  takePictureIntent.resolveActivity(packageManager)?.also {
                    startActivityForResult(takePictureIntent, OPERATION_TAKE_BACKGROUND_PICTURE)
                  }
                  dialog.dismiss()
                }
            }
          }
        }
        .setNeutralButton("Cancel") { dialog, _ ->
          dialog.dismiss()
        }
        .create()
        .show()
    }

    fun resetBackgroundImage(
      showAlert : Boolean = false
    )
    {
      if (showAlert && drawingFragment.hasBackgroundImage())
      {
        AlertDialog.Builder(this@MainActivity)
          .setTitle("Remove Image")
          .setCancelable(false)
          .setMessage("Do you want to clear background image ?")
          .setPositiveButton("Yes") { dialog, _ ->
            resetBackgroundImage()
            dialog.dismiss()
          }
          .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
          .create()
          .show()
      } else // default reset without dialog
      {
        iv_background_image.setImageURI(null)
        iv_background_image.setImageBitmap(null)
      }
    }

    fun exportImage()
    {
      if (PackageManager.PERMISSION_DENIED == ActivityCompat.checkSelfPermission(
          this@MainActivity,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
      )
      {
        ActivityCompat.requestPermissions(
          this@MainActivity,
          arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
          WRITE_EXTERNAL_STORAGE_PERMISSION
        )
      } else
      {
        if (!drawing_view.canUndo())
        {
          AlertDialog.Builder(this@MainActivity)
            .setTitle("Empty Image")
            .setCancelable(false)
            .setMessage("Your image seems empty, are you sure you want to save it ?")
            .setPositiveButton("Yes") { dialog, _ ->
              saveImage()
              dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
        } else
        {
          saveImage()
        }
      }
    }

    fun saveImage()
    {
      if (drawingFragment.hasNotBackgroundImage())
      {
        asyncExport()
      } else
      {
        AlertDialog.Builder(this@MainActivity)
          .setTitle("Background")
          .setCancelable(false)
          .setMessage("Do you want to export the background with the drawing ?")
          .setPositiveButton("Yes") { dialog, _ ->
            asyncExport(true)
            dialog.dismiss()
          }
          .setNegativeButton("No") { dialog, _ ->
            asyncExport()
            dialog.dismiss()
          }
          .create()
          .show()
      }
    }

    private fun asyncExport(withBackGround : Boolean = false)
    {

      if (withBackGround && null != iv_background_image.drawable)
      {
        AsyncBitmapFileWriter(context = this@MainActivity, withBackGround = true)
          .execute()
      } else
      {
        AsyncBitmapFileWriter(context = this@MainActivity, withBackGround = false)
          .execute()
      }
    }

    fun getDimensionedSize(
      size : Int
    ) : Float
    {
      return getDimensionedSize(size.toFloat())
    }

    fun getDimensionedSize(
      size : Float
    ) : Float
    {
      return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        size,
        resources.displayMetrics
      )
    }

    fun settingsUpdateButtons()
    {
      settingsFragment.let { settingsFragment ->
        settingsFragment.updateButton(
          SettingsFragment.Companion.ButtonType.BUTTON_UNDO,
          drawing_view.canUndo()
        )
        settingsFragment.updateButton(
          SettingsFragment.Companion.ButtonType.BUTTON_REDO,
          drawing_view.canRedo()
        )
      }
    }

    fun share()
    {
      Snackbar.make(mainContextView, "Not Implemented...", Snackbar.LENGTH_LONG).show()
    }

    /**
     ** End of FragmentController inner class
     */
  }

  private class AsyncBitmapFileWriter internal constructor(
    context : MainActivity,
    var fileName : String = "image",
    val withBackGround : Boolean = false
  ) :
    AsyncTask<Any, Void, Pair<Boolean, String>>()
  {
    private val mainActivityRef : WeakReference<MainActivity> = WeakReference(context)
    private val LOGTAG : String by lazy { "in_class_${this::class.simpleName}" }
    private lateinit var bitmap : Bitmap
    private lateinit var values : ContentValues
    private lateinit var progressBar : ProgressBar
    private lateinit var snackBar : Snackbar

    override fun onPreExecute()
    {
      super.onPreExecute()
      mainActivityRef.get()?.let { mainActivity ->
        progressBar = ProgressBar(
          mainActivity,
          null,
          android.R.attr.progressBarStyleHorizontal
        ).apply {
          isIndeterminate = false
          progress = 1
          min = 0
          max = 5
        }
        snackBar = Snackbar.make(mainActivity.mainContextView, "", Snackbar.LENGTH_INDEFINITE)
        (snackBar.view as Snackbar.SnackbarLayout).addView(progressBar)
        snackBar.show()

        fileName += "_${System.currentTimeMillis() / 1000}"
        bitmap = mainActivity.drawingFragment.getBitmap(withBackGround)
        values = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.png")
          put(MediaStore.Images.Media.TITLE, fileName)
          put(MediaStore.Images.Media.DESCRIPTION, "drawing : $fileName")
          put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
          put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }
      }
    }

    override fun doInBackground(vararg params : Any?) : Pair<Boolean, String>?
    {
      mainActivityRef.get()?.let { mainActivity ->
        var path : String? = fileName
        mainActivity.contentResolver
          .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
          ?.let { uri ->
            path = uri.path + File.separator + fileName
            publishProgress()
            mainActivity.contentResolver
              .openOutputStream(uri)?.also { output ->
                try
                {
                  publishProgress()
                  bitmap.compress(Bitmap.CompressFormat.PNG, 90, output)
                  Thread.sleep(1000)
                  output.flush()
                } catch (e : Exception)
                {
                  Log.e(LOGTAG, "$e")
                  publishProgress()
                  publishProgress()
                  return Pair(false, "${e.message}")
                }
              }?.run {
                publishProgress()
                close()
              }
          }
        publishProgress()
        return Pair(true, path ?: fileName)
      }
      return Pair(false, "No reference to context")
    }

    override fun onProgressUpdate(vararg values : Void?)
    {
      super.onProgressUpdate(*values)
      progressBar.incrementProgressBy(1)
    }

    override fun onPostExecute(result : Pair<Boolean, String>?)
    {
      mainActivityRef.get()?.let { mainActivity ->
        snackBar.dismiss()
        result?.let {
          if (it.first)
          {
            Snackbar.make(
              mainActivity.mainContextView,
              "File saved : ${it.second}",
              Snackbar.LENGTH_LONG
            ).show()
            Log.d(LOGTAG, it.second)
          } else
          {
            Snackbar.make(
              mainActivity.mainContextView,
              "Error while saving file : ${it.second}",
              Snackbar.LENGTH_LONG
            ).show()
          }
        }
      }
    }
  }

  /**
   ** Start of MainActivity class
   */

  private val LOGTAG : String by lazy { "in_class_${this::class.simpleName}" }

  private val controller : FragmentController = FragmentController()
  private lateinit var settingsFragment : SettingsFragment
  private lateinit var drawingFragment : DrawingFragment
  private lateinit var brushControlDialogFragment : BrushControlDialogFragment

  init
  {
    /* Register fragments to the controller */
    SettingsFragment(controller)
    DrawingFragment(controller)
    BrushControlDialogFragment(controller)
  }

  override fun onCreate(savedInstanceState : Bundle?)
  {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    /* Set up fragments */
    supportFragmentManager.beginTransaction()
      .add(R.id.frame_drawing, drawingFragment)
      .add(R.id.frame_setting, settingsFragment)
      .add(R.id.frame_dialog_brushSize, brushControlDialogFragment)
      .commit()

    btn_show_Settings.setOnClickListener {
      controller.showFragment(settingsFragment)
    }
  }

  override fun onRequestPermissionsResult(
    requestCode : Int,
    permissions : Array<out String>,
    grantResults : IntArray
  )
  {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode)
    {
      WRITE_EXTERNAL_STORAGE_PERMISSION ->
      {
        if (grantResults.isNotEmpty() &&
          PackageManager.PERMISSION_GRANTED == grantResults[0]
        )
        {
          controller.saveImage()
        } else
        {
          Snackbar.make(
            mainContextView,
            "The application needs external storage access to export your image",
            Snackbar.LENGTH_LONG
          )
            .setAction("Retry") {
              ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_PERMISSION
              )
            }
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .show()
        }
      }
    }
  }

  override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
  {
    super.onActivityResult(requestCode, resultCode, data)
    if (Activity.RESULT_OK == resultCode)
    {
      when (requestCode)
      {
        OPERATION_IMPORT_BACKGROUND_PICTURE ->
        {
          data?.data?.let { uri ->
            controller.setBackGroundImage(uri)
          } ?: run {
            Snackbar.make(
              mainContextView,
              "couldn't import image",
              Snackbar.LENGTH_SHORT
            )
              .show()
          }
        }
        OPERATION_TAKE_BACKGROUND_PICTURE   ->
        {
          Log.d(LOGTAG, "$data")
          data?.extras?.let { extras ->
            controller.setBackGroundImage(extras.get("data") as Bitmap)
          } ?: run {
            Snackbar.make(
              mainContextView,
              "couldn't take picture",
              Snackbar.LENGTH_SHORT
            )
              .show()
          }
        }
      }
    }
  }
  /**
   ** End of MainActivity class
   */
}
