package com.example.rempahrasa

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.rempahrasa.databinding.FragmentScanBinding
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_IMAGE_PICK = 2
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
        }

        binding.btnPickImage.setOnClickListener {
            dispatchPickPictureIntent()
        }

        binding.ivClose.setOnClickListener {
            requireActivity().onBackPressed()
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun dispatchPickPictureIntent() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { pickPictureIntent ->
            startActivityForResult(pickPictureIntent, REQUEST_IMAGE_PICK)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(requireContext(), "Failed to start camera.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(requireContext().cacheDir, "scanned_image.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Failed to capture image.", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    uploadImage(photoFile)
                }
            })
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
                        Toast.makeText(requireContext(), "Classification successful!", Toast.LENGTH_LONG).show()
                        val intent = Intent(requireContext(), ResultsActivity::class.java).apply {
                            putExtra("spiceName", classificationResponse.spices.firstOrNull())
                            putStringArrayListExtra("recipes", ArrayList(classificationResponse.recipes))
                            putExtra("spiceImageRes", R.drawable.ic_launcher_background)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), classificationResponse?.message ?: "Classification failed", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.message()}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    binding.ivScannedImage.setImageURI(imageUri)
                    val filePath = getPathFromUri(imageUri)
                    if (filePath != null) {
                        uploadImage(File(filePath))
                    }
                }
            }
        }
    }

    private fun getPathFromUri(uri: Uri?): String? {
        var path: String? = null
        uri?.let {
            val cursor = requireContext().contentResolver.query(it, null, null, null, null)
            cursor?.moveToFirst()?.let {
                val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                path = cursor.getString(idx)
                cursor.close()
            }
        }
        return path
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
