package com.example.myapplication
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var myFingerView: MyFingerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        myFingerView = findViewById(R.id.finger_view)
        val colorBox = findViewById<LinearLayout>(R.id.ll_color_parent)
        for (i in 0 until colorBox.childCount) {
            colorBox.getChildAt(i).setOnClickListener(this@MainActivity)
        }
        val toolBox = findViewById<LinearLayout>(R.id.ll_tool)
        for (i in 0 until toolBox.childCount) {
            toolBox.getChildAt(i).setOnClickListener(this@MainActivity)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_pen -> myFingerView.mode = MyFingerView.Pen
            R.id.iv_erase -> myFingerView.mode = MyFingerView.Eraser
            R.id.iv_gallery -> if (checkPermission()) {
                goGallery()
            }
            R.id.iv_reset -> {
                myFingerView.reset()
                myFingerView.setImageDrawable(null)
            }
            else -> if (myFingerView.mode == MyFingerView.Pen) {
                val tag = view.tag
                val color = Color.parseColor(tag.toString())
                myFingerView.mPaint.color = color
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            val imageUri = data!!.data
            val bitmap = ImagesTool.decodeSampledBitmapFromPath(this@MainActivity, imageUri, myFingerView)
            myFingerView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            return false
        }
        return true
    }

    private fun goGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            goGallery()
        }
    }
}