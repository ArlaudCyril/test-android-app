package com.Lyber.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WebViewActivity : BaseActivity<ActivityWebViewBinding>() {
    override fun bind() = ActivityWebViewBinding.inflate(layoutInflater)
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mImagePath = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequest()
        val webSettings = binding.webView!!.settings
        webSettings.domStorageEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.javaScriptEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.builtInZoomControls = false
        webSettings.allowFileAccess = true
//        webSettings.setSupportZoom(true)
        webSettings.setSupportMultipleWindows(true)
        webSettings.setAllowFileAccess(true)
        webSettings.setAllowContentAccess(true)
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
        webSettings.mixedContentMode = 0
        binding.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        binding.webView?.webChromeClient = MyWebChromeClient();
        binding.webView?.webViewClient = MyBrowser()

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

                    } else if (permissions[android.Manifest.permission.MODIFY_AUDIO_SETTINGS] == false) {

                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                android.Manifest.permission.MODIFY_AUDIO_SETTINGS
                            )
                        ) {
                        } else permissionsDenied()

                    } else if (permissions[android.Manifest.permission.RECORD_AUDIO] == false) {

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
                    } else if (permissions[android.Manifest.permission.MODIFY_AUDIO_SETTINGS] == false
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
            Log.d("Url", url!!)
            if (PageURL!!.equals("https://www.lyber.com/kyc-finished")) {
                setResult(Activity.RESULT_OK)
                finish()
            } else if (PageURL!!.contains("https://www.lyber.com/sign-finished")) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (ImageUri != null)
            deleteImageFromMediaStore(ImageUri!!)
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
//            redirectWebView.settings.setSupportZoom(true)
            redirectWebView.settings.builtInZoomControls = false
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
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            var f: File? = null
            try {
                f = setUpPhotoFile(this@WebViewActivity)
                mImagePath = f!!.absolutePath
                val imageUri = FileProvider.getUriForFile(
                    this@WebViewActivity,
                    this@WebViewActivity.packageName.toString() + ".provider",  //(use your app signature + ".provider" )
                    f
                )
                intent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    imageUri
                )
            } catch (e: IOException) {
                e.printStackTrace()
                f = null
                mImagePath = null!!
            }
            resultLauncher.launch(intent)
            return true
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onPermissionRequest(request: PermissionRequest?) {
            request?.grant(request.resources)
        }

    }

    private var ImageUri: Uri? = null

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            ImageUri = null
            var results: Array<Uri>? = null
            if (result.resultCode == RESULT_OK) {
                    ImageUri = saveImageToMediaStore(
                        getFile(mImagePath)!!, this
                    )
                    Log.d("imageFi", ImageUri.toString())
                    results = arrayOf(ImageUri!!)

            }
            mFilePathCallback!!.onReceiveValue(results)
            mFilePathCallback = null
            }
    

  private  fun deleteImageFromMediaStore(imageUri: Uri) {
        try {
            val contentResolver: ContentResolver = contentResolver
            contentResolver.delete(imageUri, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpPhotoFile(mContext: Context): File? {

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

    private fun getFile(imgPath: String?): Bitmap? {
        val mOrientation: Int
        var bMapRotate: Bitmap? = null
        try {
            if (imgPath != null) {
                val exif = ExifInterface(imgPath)
                mOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)

                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(imgPath, options)
                options.inSampleSize = calculateInSampleSize(options, 400, 400)
                options.inJustDecodeBounds = false
                bMapRotate = BitmapFactory.decodeFile(imgPath, options)
                if (mOrientation == 6) {
                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    bMapRotate = Bitmap.createBitmap(
                        bMapRotate!!, 0, 0,
                        bMapRotate.width, bMapRotate.height,
                        matrix, true
                    )
                } else if (mOrientation == 8) {
                    val matrix = Matrix()
                    matrix.postRotate(270f)
                    bMapRotate = Bitmap.createBitmap(
                        bMapRotate!!, 0, 0,
                        bMapRotate.width, bMapRotate.height,
                        matrix, true
                    )
                } else if (mOrientation == 3) {
                    val matrix = Matrix()
                    matrix.postRotate(180f)
                    bMapRotate = Bitmap.createBitmap(
                        bMapRotate!!, 0, 0,
                        bMapRotate.width, bMapRotate.height,
                        matrix, true
                    )
                }
            }
        } catch (e: OutOfMemoryError) {
            bMapRotate = null
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            bMapRotate = null
            e.printStackTrace()
        }
        return bMapRotate
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int
    ): Int {
        try {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

   private fun saveImageToMediaStore(finalBitmap: Bitmap, context: Context): Uri? {
        val contentResolver: ContentResolver = context.contentResolver

        // Create a ContentValues object to store metadata about the image
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.IS_PENDING,
                    1
                ) // Mark the image as pending to allow writing
            }
        }

        // Get the collection Uri based on the content type (Images in this case)
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        // Insert the new image into MediaStore
        val imageUri = contentResolver.insert(collection, contentValues)

        // Open an OutputStream to write the bitmap data to the content Uri
        imageUri?.let { uri ->
            contentResolver.openOutputStream(uri).use { outputStream ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream!!)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Mark the image as not pending to make it visible to other apps
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }

            return uri
        }
        // If something goes wrong, return null
        return null
    }
}