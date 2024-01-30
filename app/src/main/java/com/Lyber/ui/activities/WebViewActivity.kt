package com.Lyber.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.Lyber.R
import com.Lyber.databinding.ActivityWebViewBinding
import com.Lyber.utils.CommonMethods
import com.Lyber.utils.Constants
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WebViewActivity : BaseActivity<ActivityWebViewBinding>() {
    override fun bind() = ActivityWebViewBinding.inflate(layoutInflater)
    private val TAG = "WebViewActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
checkAndRequest()
        val webSettings = binding.webView!!.settings
        webSettings.domStorageEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.javaScriptEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.builtInZoomControls = true
        webSettings.allowFileAccess = true
        webSettings.setSupportZoom(true)
        webSettings.setSupportMultipleWindows(true)
        webSettings.setAllowFileAccess(true)
        webSettings.setAllowContentAccess(true)
        webSettings.setAllowFileAccessFromFileURLs(true)
        webSettings.setAllowUniversalAccessFromFileURLs(true)
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.allowContentAccess = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.safeBrowsingEnabled = true
        }
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.pluginState = WebSettings.PluginState.ON
        webSettings.setSupportMultipleWindows(true)

        webSettings.loadWithOverviewMode = true
      if (Build.VERSION.SDK_INT >= 21) {
            webSettings.mixedContentMode = 0
            binding.webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else if (Build.VERSION.SDK_INT >= 19) {
            binding.webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else if (Build.VERSION.SDK_INT < 19) {
            binding.webView!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        binding.webView!!.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                setProgress(progress * 100)
            }
        }
        binding.webView!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.endsWith(".pdf")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    return true
                }
                return false
            }
        }
        binding.webView!!.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
            }
        }
        binding.webView?.webChromeClient=MyWebChromeClient();
        binding.webView?.webViewClient=MyBrowser()

        binding.webView.loadUrl(intent.getStringExtra(Constants.URL)!!)
        //dummyUrl
//        binding.webView.loadUrl("https://marcusbelcher.github.io/wasm-asm-camera-webgl-test/index.html")
    }

    private fun checkAndRequest() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.CAMERA
                )
                != PackageManager.PERMISSION_GRANTED

                ||
                ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
                ||
                ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.MODIFY_AUDIO_SETTINGS
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED

            ) {

                requestMultiplePermissions.launch(
                    arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO,
                        android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        android.Manifest.permission.RECORD_AUDIO
                    )
                )

            } else {
                Log.d("TAG2", "Permission Already Granted")
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.CAMERA
                )
                != PackageManager.PERMISSION_GRANTED

                ||
                ActivityCompat.checkSelfPermission(
                    applicationContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED

                ||
                ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
                ||
                ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.MODIFY_AUDIO_SETTINGS
                ) != PackageManager.PERMISSION_GRANTED
                ||
                ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                requestMultiplePermissions.launch(
                    arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        android.Manifest.permission.RECORD_AUDIO
                    )
                )

            } else {
                Log.d("TAG2", "Permission Already Granted")
            }
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            permissions.entries.forEach {
                Log.d("TAG", "${it.key} = ${it.value}")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (permissions[android.Manifest.permission.CAMERA] == true
                    && permissions[android.Manifest.permission.READ_MEDIA_IMAGES] == true
                    && permissions[android.Manifest.permission.READ_MEDIA_VIDEO] == true
                    && permissions[android.Manifest.permission.MODIFY_AUDIO_SETTINGS] == true
                    && permissions[android.Manifest.permission.RECORD_AUDIO] == true
                ) {
                    Log.d("requestMultiplePermissions", "Permission granted")
                    // isPermissionGranted=true
                } else {

                    if (permissions[android.Manifest.permission.CAMERA] == false) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.CAMERA
                            )
                        ) {
                        } else permissionsDenied()

                    } else if (permissions[android.Manifest.permission.READ_MEDIA_VIDEO] == false) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.READ_MEDIA_VIDEO
                            )
                        ) {

                        } else permissionsDenied()
                    } else if (permissions[android.Manifest.permission.READ_MEDIA_IMAGES] == false) {

                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.READ_MEDIA_IMAGES
                            )
                        ) {
                        } else permissionsDenied()

                    }else if (permissions[android.Manifest.permission.MODIFY_AUDIO_SETTINGS] == false) {

                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.MODIFY_AUDIO_SETTINGS
                            )
                        ) {
                        } else permissionsDenied()

                    }else if (permissions[android.Manifest.permission.RECORD_AUDIO] == false) {

                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.RECORD_AUDIO
                            )
                        ) {
                        } else permissionsDenied()

                    }

                    Log.d("requestMultiplePermissions", "Permission not granted")

                }

            } else {

                if (permissions[android.Manifest.permission.CAMERA] == true && permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
                    && permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true
                    && permissions[android.Manifest.permission.MODIFY_AUDIO_SETTINGS] == true
                    && permissions[android.Manifest.permission.RECORD_AUDIO] == true) {
                    Log.d("requestMultiplePermissions", "Permission granted")
                    // isPermissionGranted=true

                } else {
                    if (permissions[android.Manifest.permission.CAMERA] == false) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.CAMERA
                            )
                        ) {

                        } else {
                            permissionsDenied()
                        }

                    } else if (permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] == false
                        && permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == false
                    ) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                            && ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        ) {

                        } else {

                            permissionsDenied()
                        }
                    }else if (permissions[android.Manifest.permission.MODIFY_AUDIO_SETTINGS] == false
                        && permissions[android.Manifest.permission.RECORD_AUDIO] == false
                    ) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.MODIFY_AUDIO_SETTINGS
                            )
                            && ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.RECORD_AUDIO
                            )
                        ) {

                        } else {

                            permissionsDenied()
                        }
                    }

                    Log.d("requestMultiplePermissions", "Permission not granted")

                }

            }

        }

    private var alertDialogRational: AlertDialog? = null
    private fun permissionsDenied() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogRational?.getButton(AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(ContextCompat.getColor(this, R.color.black))

        alertDialogRational = alertDialogBuilder.setTitle("Permissions Required")
            .setMessage(
                "Please open settings, go to permissions and allow them."
            )
            .setPositiveButton(
                "Settings"
            ) { _, _ ->

                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts(
                        "package", packageName,
                        null
                    )
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                activityResult.launch(intent)
            }
            .setNegativeButton(
                "Cancel"
            ) { _, _ -> }
            .setCancelable(false)
            .create()
        alertDialogRational!!.apply {
            show()
            getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this@WebViewActivity, R.color.black))
            getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this@WebViewActivity, R.color.black))
        }
    }

    private inner class MyBrowser : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)

        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            val PageURL = view?.url
            if (PageURL!!.contains("KYCSuccess")) {

            }
        }

    }

    private var mCapturedImageURI: Uri? = null
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mCameraPhotoPath: String? = null
    private var mUploadMessage: ValueCallback<Uri?>? = null
    companion object {
        private const val INPUT_FILE_REQUEST_CODE = 100
        private const val FILECHOOSER_RESULTCODE = 100
    }
    private inner class MyWebChromeClient : WebChromeClient() {
        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message
        ): Boolean {
            val redirectWebView = WebView(this@WebViewActivity)
            redirectWebView.settings.javaScriptEnabled = true
            redirectWebView.settings.allowContentAccess = true
            redirectWebView.settings.setEnableSmoothTransition(true)
            redirectWebView.settings.setSupportZoom(true)
            redirectWebView.settings.builtInZoomControls = true
            redirectWebView.settings.pluginState = WebSettings.PluginState.ON
            redirectWebView.settings.setSupportMultipleWindows(true)
            redirectWebView.settings.mediaPlaybackRequiresUserGesture = false
            redirectWebView.settings.allowFileAccess = true


            val dialog = Dialog(this@WebViewActivity)
            dialog.setContentView(redirectWebView)
            dialog.show()
            val transport = resultMsg.obj as WebView.WebViewTransport
            transport.webView = redirectWebView
            resultMsg.sendToTarget()

            redirectWebView.webViewClient = object : android.webkit.WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return true
                }
            }
            return true
        }


        // For Android 5.0
        override fun onShowFileChooser(
            view: WebView,
            filePath: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            // Double check that we don't have any existing callbacks
            if (mFilePathCallback != null) {
                mFilePathCallback!!.onReceiveValue(null)
            }
            mFilePathCallback = filePath
            var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent!!.resolveActivity(packageManager) != null) {
                // Create the File where the photo should go
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
//                    Log.e(TAG, "Unable to create Image File", ex);
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.absolutePath
                    takePictureIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile)
                    )
                } else {
                    takePictureIntent = null
                }
            }
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = "image/*"
            val intentArray: Array<Intent?>
            intentArray = takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
            var chooserIntent = Intent(Intent.ACTION_CHOOSER)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val data = fileChooserParams.acceptTypes
                if (data.size > 0) {
                    val takePictureIntent2 = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    // Intent intent = fileChooserParams.createIntent();
                    chooserIntent = Intent(Intent.ACTION_CHOOSER)
                    val contentSelectionIntent2 = Intent(Intent.ACTION_GET_CONTENT)
                    contentSelectionIntent2.addCategory(Intent.CATEGORY_OPENABLE)
                    contentSelectionIntent2.type = "*/*"
                    val intentArray2 = arrayOf(takePictureIntent2)
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent2)
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose an action")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray2)
                } else if (data[0].contains("video")) {
                    val takeVideoIntent2 = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    //Intent intent = fileChooserParams.createIntent();
                    chooserIntent = Intent(Intent.ACTION_CHOOSER)
                    val contentSelectionIntent2 = Intent(Intent.ACTION_GET_CONTENT)
                    contentSelectionIntent2.addCategory(Intent.CATEGORY_OPENABLE)
                    contentSelectionIntent2.type = "*/*"
                    val intentArray2 = arrayOf(takeVideoIntent2)
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent2)
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose an action")
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray2)
                }
            }

//            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
//            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
//            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE)
            return true
        }

        @JvmOverloads
        fun openFileChooser(uploadMsg: ValueCallback<Uri?>?, acceptType: String? = "") {
            mUploadMessage = uploadMsg
            // Create AndroidExampleFolder at sdcard
            // Create AndroidExampleFolder at sdcard
            val imageStorageDir = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ), "AndroidExampleFolder"
            )
            if (!imageStorageDir.exists()) {
                // Create AndroidExampleFolder at sdcard
                imageStorageDir.mkdirs()
            }
            // Create camera captured image file path and name
            val file = File(
                imageStorageDir.toString() + File.separator + "IMG_"
                        + System.currentTimeMillis().toString() + ".jpg"
            )
            mCapturedImageURI = Uri.fromFile(file)
            // Camera capture image intent
            val captureIntent = Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
            )
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI)
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "image/*"
            // Create file chooser intent
            val chooserIntent = Intent.createChooser(i, "Image Chooser")
            // Set camera intent to file chooser
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(captureIntent)
            )
            // On select image call onActivityResult method of activity
            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE)
        }

        //openFileChooser for other Android versions
        fun openFileChooser(
            uploadMsg: ValueCallback<Uri?>?,
            acceptType: String?,
            capture: String?
        ) {
            openFileChooser(uploadMsg, acceptType)
        }
        //openFileChooser for other Android versions

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onPermissionRequest(request: PermissionRequest?) {
//            super.onPermissionRequest(request)
            request?.grant(request.resources)
        }

    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
//        super.onActivityResult(requestCode, resultCode, intent)
//        var results: Array<Uri>? = null
//        //Check if response is positive
//        if (resultCode == RESULT_OK) {
//
//            if (requestCode == INPUT_FILE_REQUEST_CODE) {
//                if (null == mFilePathCallback) {
//                    return
//                }
//                //camera selected
//                //Capture Photo if no image available
//                if (mCameraPhotoPath != null) {
//                    val imageFile = CommonMethods.saveImageToExternalStorage(
//                        CommonMethods.getfile(mCameraPhotoPath)!!
//                        ,this@WebViewActivity)
//                    Log.d("imageFi", imageFile!!.absolutePath)
//
//                    results = arrayOf(Uri.parse(mCameraPhotoPath!!))
//                } else {
//                    //gallery selected
//                    val dataString = intent!!.dataString
//                    if (dataString != null) {
//                        results = arrayOf(Uri.parse(dataString))
//                    }
//                }
//            }
//        }
//        mFilePathCallback!!.onReceiveValue(results)
//        mFilePathCallback = null
//    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, intent)
                return
            }
            var results: Array<Uri>? = null
            // Check that the response is a good one
            if (resultCode == RESULT_OK) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = arrayOf(Uri.parse(mCameraPhotoPath))
                    }
            }
            mFilePathCallback!!.onReceiveValue(results)
            mFilePathCallback = null
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
    }
    fun setUpPhotoFile(mContext: Context): File? {

        var imageF: File? = null
        val directoryPath =
            mContext.getExternalFilesDir(null)!!.absolutePath + File.separator + "PICS"
        try {
            if (Environment.MEDIA_MOUNTED == Environment
                    .getExternalStorageState()
            ) {
                val storageDir = File(directoryPath).parentFile

                if (storageDir != null) {
                    if (!storageDir.mkdirs()) {
                        if (!storageDir.exists()) {
                            Log.d("CameraSample", "failed to create directory")
                            return null
                        }
                    }
                }
                imageF = File.createTempFile(
                    Constants.JPEG_FILE_PREFIX
                            + System.currentTimeMillis() + "_",
                    Constants.JPEG_FILE_SUFFIX, storageDir
                )
            } else {
                Log.v(
                    mContext.getString(R.string.app_name),
                    "External storage is not mounted READ/WRITE."
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imageF
    }
}