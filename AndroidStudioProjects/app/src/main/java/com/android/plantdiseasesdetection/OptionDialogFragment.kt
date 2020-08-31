package com.android.plantdiseasesdetection

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_option_dialog.*

/**
 * A simple [Fragment] subclass.
 */
class OptionDialogFragment : DialogFragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.fragment_option_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rl_camera.setOnClickListener(this)
        rl_gallery.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            rl_camera -> {
                val moveCameraIntent = Intent(activity, CameraActivity::class.java)
                startActivity(moveCameraIntent)
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                dismiss()
            }
            rl_gallery -> {
                val moveDetectFromGallery = Intent(activity, DetectWithImageActivity::class.java)
                startActivity(moveDetectFromGallery)
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                dismiss()
            }
        }
    }
}
