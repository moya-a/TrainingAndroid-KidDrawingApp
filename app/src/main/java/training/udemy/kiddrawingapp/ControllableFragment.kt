package training.udemy.kiddrawingapp

import androidx.fragment.app.Fragment

open class ControllableFragment(controller: MainActivity.FragmentController) : Fragment()
{
  protected val controller = controller


  init
  {
    attachToController()
  }

  private fun attachToController()
  {
    controller.addFragment(this)
  }
}