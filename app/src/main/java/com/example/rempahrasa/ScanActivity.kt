package com.example.rempahrasa

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ScanActivity : AppCompatActivity() {

    private lateinit var ivScannedImage: ImageView
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        ivScannedImage = findViewById(R.id.ivScannedImage)
        val btnTakePhoto = findViewById<ImageView>(R.id.btnTakePhoto)
        val btnPickImage = findViewById<ImageView>(R.id.btnPickImage)
        val ivClose = findViewById<ImageView>(R.id.ivClose)

        ivClose.setOnClickListener {
            finish()
        }

        btnTakePhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

        btnPickImage.setOnClickListener {
            dispatchPickPictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun dispatchPickPictureIntent() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { pickPictureIntent ->
            startActivityForResult(pickPictureIntent, REQUEST_IMAGE_PICK)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    ivScannedImage.setImageBitmap(imageBitmap)
                    uploadImage(bitmapToFile(imageBitmap))
                }
                REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    ivScannedImage.setImageURI(imageUri)
                    val filePath = getPathFromUri(imageUri)
                    if (filePath != null) {
                        uploadImage(File(filePath))
                    }
                }
            }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap): File {
        val file = File(cacheDir, "scanned_image.jpg")
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val bitmapData = bos.toByteArray()

        FileOutputStream(file).apply {
            write(bitmapData)
            flush()
            close()
        }

        return file
    }

    private fun getPathFromUri(uri: Uri?): String? {
        var path: String? = null
        uri?.let {
            val cursor = contentResolver.query(it, null, null, null, null)
            cursor?.moveToFirst()?.let {
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }

    private fun uploadImage(file: File) {
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        CoroutineScope(Dispatchers.IO).launch {
            val response: Response<ClassificationResponse> = RetrofitInstance.api.classifySpice(body)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body() != null) {
                    val classificationResponse = response.body()
                    if (classificationResponse?.success == true) {
                        Toast.makeText(this@ScanActivity, "Classification successful!", Toast.LENGTH_LONG).show()
                        // Start ResultsActivity with the results
                        val intent = Intent(this@ScanActivity, ResultsActivity::class.java).apply {
                            putExtra("spiceName", classificationResponse.spices.firstOrNull())
                            putStringArrayListExtra("recipes", ArrayList(classificationResponse.recipes))
                            putExtra("spiceImageRes", R.drawable.ic_launcher_background) // Replace with actual image resource if available
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@ScanActivity, classificationResponse?.message ?: "Classification failed", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@ScanActivity, "Error: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
