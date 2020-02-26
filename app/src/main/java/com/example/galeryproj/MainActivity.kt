package com.example.galeryproj

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), PhotoOptionDialogFragment.PhotoOptionDialogListener {

    private lateinit var button: Button
    private var photoFile: File? = null
    private lateinit var photo: ImageView

    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.button = findViewById(R.id.takePictureButton)
        this.photo = findViewById(R.id.imageView)
        this.button.setOnClickListener {
            replaceImage()
        }
    }

    override fun onCaptureClick() {
        photoFile = null
        try {
            photoFile = ImageUtils.createUniqueImageFile(this)
            if (photoFile == null) {
                return
            }
        } catch (ex: java.io.IOException) {
            return
        }
        val captureIntent =
            Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val photoUri = FileProvider.getUriForFile(this,
            "com.example.galeryproj.fileprovider",
            photoFile!!)

        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
            photoUri)

        val intentActivities = packageManager.queryIntentActivities(
            captureIntent, PackageManager.MATCH_DEFAULT_ONLY)

        intentActivities.map { it.activityInfo.packageName }
            .forEach { grantUriPermission(it, photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
        startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)

    }

    override fun onPickClick() {
        val pickIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
    }

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == android.app.Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAPTURE_IMAGE -> {
                    val photoFile = photoFile ?: return
                    val uri = FileProvider.getUriForFile(this,
                        "com.example.galeryproj.fileprovider",
                        photoFile)
                    revokeUriPermission(uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    val image = getImageWithPath(photoFile.absolutePath)
                    image?.let { imageView.setImageBitmap(it) }
                }

                REQUEST_GALLERY_IMAGE -> if (data != null && data.data != null) {
                    val imageUri = data.data
                    val image = getImageWithAuthority(imageUri!!)
                    image?.let { imageView.setImageBitmap(it) }
                }
            }
        }
    }


    private fun getImageWithPath(filePath: String): Bitmap? {
        return ImageUtils.decodeFileToSize(filePath,
            resources.getDimensionPixelSize(
                R.dimen.default_image_width),
            resources.getDimensionPixelSize(
                R.dimen.default_image_height))
    }

    private fun getImageWithAuthority(uri: Uri): Bitmap? {
        return ImageUtils.decodeUriStreamToSize(uri,
            resources.getDimensionPixelSize(
                R.dimen.default_image_width),
            resources.getDimensionPixelSize(
                R.dimen.default_image_height),
            this)
    }
}
