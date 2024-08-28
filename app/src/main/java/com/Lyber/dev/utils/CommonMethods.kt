package com.Lyber.dev.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources.getSystem
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.Navigation.findNavController
import com.Lyber.dev.R
import com.Lyber.dev.databinding.DocumentBeingVerifiedBinding
import com.Lyber.dev.databinding.ProgressBarBinding
import com.Lyber.dev.models.AssetBaseData
import com.Lyber.dev.models.Balance
import com.Lyber.dev.models.ErrorResponse
import com.Lyber.dev.models.MonthsList
import com.Lyber.dev.models.Network
import com.Lyber.dev.models.PriceServiceResume
import com.Lyber.dev.network.RestClient
import com.Lyber.dev.utils.App.Companion.prefsManager
import com.Lyber.dev.utils.Constants.CAP_RANGE
import com.Lyber.dev.utils.Constants.SMALL_RANGE
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.mikephil.charting.data.Entry
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import okhttp3.ResponseBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.RoundingMode
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.roundToInt


@Suppress("INTEGER_OVERFLOW")
class CommonMethods {

    companion object {

        private var dialog: Dialog? = null
        private var alertDialog: AlertDialog? = null

        private var dialogVerification: Dialog? = null

        fun isProgressShown(): Boolean {
            dialog?.let {
                return it.isShowing
            }
            return false
        }

        @JvmStatic
//        fun getScreenWidth(activity: Context?): Int {
//            val w = activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//            val size = Point()
//            w.defaultDisplay.getSize(size)
//            return size.x
//        }


        fun showProgressDialog(context: Context) {

            if (dialog == null) {
                dialog = Dialog(context)
                dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog!!.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                dialog!!.window!!.setDimAmount(0.2F)
                dialog!!.setCancelable(false)
                dialog!!.setContentView(ProgressBarBinding.inflate(LayoutInflater.from(context)).root)
            }
            try {
                dialog?.findViewById<ImageView>(R.id.progressImage)?.animation =
                    AnimationUtils.loadAnimation(context, R.anim.rotate_drawable)
                dialog!!.show()
                AppLifeCycleObserver.progressDialogVisible = true
            } catch (e: WindowManager.BadTokenException) {
                Log.d("Exception", "showProgressDialog: ${e.message}")
                dialog?.dismiss()
                dialog = null
                showProgressDialog(context)
            } catch (e: Exception) {
                Log.d("Exception", "showProgressDialog: ${e.message}")
            }

        }

        fun String.toFormat(format: String, convertFormat: String): String {
            try {
                var result = ""
                val formatter: DateFormat = SimpleDateFormat(format, Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("UTC")
                val outputFormatter: DateFormat =
                    SimpleDateFormat(convertFormat, Locale.getDefault())
                outputFormatter.timeZone = TimeZone.getDefault()
                formatter.parse(this)?.let { result = outputFormatter.format(it) }

                return result
            } catch (e: Exception) {
                return this
            }
        }

        fun dismissProgressDialog() {
            dialog?.let {
                try {
                    it.findViewById<ImageView>(R.id.progressImage).clearAnimation()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                it.dismiss()
                AppLifeCycleObserver.progressDialogVisible = false

            }
        }

        @SuppressLint("SimpleDateFormat")
        fun Long.toDateFormat(): String {
            SimpleDateFormat("dd MMM yyyy").let {
                return it.format(Date(this))
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun Long.toDateFormatTwo(): String {
            SimpleDateFormat("dd/MM/yyyy").let {
                return it.format(Date(this))
            }
        }

        //        fun Long.toGraphTime(): String {
//            SimpleDateFormat("MMM dd, HH:mm").let {
//                return it.format(Date(this))
//            }
//        }
        fun Long.toGraphTime(): String {
            val date = Date(this)
            val dateFormat = if (SimpleDateFormat("HH:mm").format(date) == "00:00") {
                SimpleDateFormat("MMM dd")
            } else {
                SimpleDateFormat("MMM dd, HH:mm")
            }
            return dateFormat.format(date)
        }

        fun List<Double>.toTimeSeries(): List<List<Double>> {

            val list = mutableListOf<List<Double>>()
            for (i in 0 until this.count())
                list.add(listOf(System.currentTimeMillis().toDouble(), this[i]))
            return list
        }

        fun String.decimalPoint(): String {
            val decimalFormat = DecimalFormat("#.########", DecimalFormatSymbols(Locale.ENGLISH))
            return decimalFormat.format(toDouble())
        }

        fun String.decimalPoints(points: Int): String {
            val string = StringBuilder()
            string.append("#.##")
//            for (i in 1..points) string.append("#")
            return DecimalFormat(string.toString()).format(toDouble())
        }


        fun String.roundFloat(): String {
            try {
                var value = this
                if (value.contains(","))
                    value = this.replace(",", "")
                return String.format(Locale.US, "%.2f", value.toFloat())
            } catch (ex: NumberFormatException) {
                return ""
            }
        }

        fun isBiometricReady(context: Context) =
            hasBiometricCapability(context) == BiometricManager.BIOMETRIC_SUCCESS

        private fun hasBiometricCapability(context: Context): Int {
            val biometricManager = BiometricManager.from(context)
            return biometricManager.canAuthenticate(BIOMETRIC_WEAK)
        }

        fun setBiometricPromptInfo(
            title: String,
            subtitle: String,
            description: String,
            allowDeviceCredential: Boolean
        ): BiometricPrompt.PromptInfo {
            val builder = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)

            // Use Device Credentials if allowed, otherwise show Cancel Button
            builder.apply {
                if (allowDeviceCredential)
                    setAllowedAuthenticators(BIOMETRIC_WEAK)
                else setNegativeButtonText("Cancel")
            }

            return builder.build()
        }


        fun ImageView.loadImage(any: Any) {
            when (any) {
                is String -> {


                    if (any.startsWith("http"))
                        Glide.with(this).load(any).apply(RequestOptions.fitCenterTransform())
                            .into(this)
                    else
                        Glide.with(this).load(Constants.BASE_IMAGE_URL_ORIGINAL + any)
                            .apply(RequestOptions.fitCenterTransform()).into(this)

                }

            }
        }

        fun ImageView.loadCircleCrop(any: Any, placeHolderRes: Int? = -1) {
            if (any.toString().contains("btc")) {
                this.setImageResource(R.drawable.ic_bitcoin)
            } else if (any.toString().contains("pepe")) {
                this.setImageResource(R.drawable.ic_pepe)
            } else {
//                var res = any
                val requestBuilder: RequestBuilder<PictureDrawable> =
                    Glide.with(this).`as`(PictureDrawable::class.java)
                        .placeholder(placeHolderRes!!)
                        .error(placeHolderRes)
                        .listener(com.Lyber.dev.utils.SvgSoftwareLayerSetter())
                when (any) {
                    is String -> {
//                        if (!any.contains("http"))
//                            res = Constants.BASE_URL + any

                        if (placeHolderRes != -1)
                            Glide.with(this).load(any)
                                .placeholder(placeHolderRes)
                                .apply(RequestOptions.circleCropTransform())
                                .diskCacheStrategy(
                                    DiskCacheStrategy.RESOURCE
                                ).into(this)
                        else requestBuilder.load(any).into(this)
                        /*Glide.with(this)
                        .`as`(PictureDrawable::class.java)
                        .load(any).apply(RequestOptions.circleCropTransform())
                        .diskCacheStrategy(
                            DiskCacheStrategy.RESOURCE
                        )
                        .placeholder(R.drawable.ic_sol)
                        .dontAnimate().into(this)*/


                    }

                    else -> {
                        if (placeHolderRes != -1)
                            Glide.with(this).load(any).apply(RequestOptions.circleCropTransform())
                                .diskCacheStrategy(
                                    DiskCacheStrategy.RESOURCE
                                ).into(this)
                        else Glide.with(this).load(any).apply(RequestOptions.circleCropTransform())
                            .diskCacheStrategy(
                                DiskCacheStrategy.RESOURCE
                            )
                            .placeholder(placeHolderRes)
                            .into(this)
                    }
                }
            }

        }

        fun ImageView.load(any: Any) {
            Glide.with(this).load(any).into(this)
        }

        fun checkInternet(view:View,context: Context, handle: () -> Unit) {
            if (isNetworkConnected(context)) handle()
            else "Please check your internet connection".showToast(view,context)
        }

        @SuppressLint("NewApi", "MissingPermission")
        private fun isNetworkConnected(context: Context): Boolean {
            var result = false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
            return result
        }

        @SuppressLint("HardwareIds")
        fun getDeviceId(contentResolver: ContentResolver): String {
            return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        }

        fun showErrorMessage(context: Context, responseBody: ResponseBody?, root: View): Int {

            val errorConverter = RestClient.getRetrofitInstance()
                .responseBodyConverter<ErrorResponse>(
                    ErrorResponse::class.java,
                    arrayOfNulls<Annotation>(0)
                )

            val errorRes: ErrorResponse? = errorConverter.convert(responseBody!!)
            if (errorRes?.code == 19006 || errorRes?.code == 19007 || errorRes?.code == 19004) {
                logOut(context)
//                return errorRes.code
            } else if (errorRes?.code == 19002 || errorRes?.code == 19003) {
                findNavController(root).navigate(R.id.underMaintenanceFragment)
            } else if (errorRes?.code == 7023 || errorRes?.code == 10041 || errorRes?.code == 7025 || errorRes?.code == 10043) {
                return errorRes.code
            } else if (errorRes?.code == 7024 || errorRes?.code == 10042) {
                showSnack(root, context, null)
                return errorRes.code
            } else
                if ((errorRes?.error ?: "").isNotEmpty()) {
                    when (errorRes?.error) {
                        "Invalid UpdateExpression: Syntax error; token: \"0\", near: \", 0)\"" ->
                            "Invalid OTP".showToast(root,context)

                        "Email already verified" -> "Email already exists".showToast(root,context)
                        "Bad client credentials" -> "Invalid credentials".showToast(root,context)
                        "No user registerd with this email" -> "Invalid credentials".showToast(
                            root,context
                        )

                        else -> {
                            if (errorRes?.error!!.length <= 60)
                                errorRes?.error?.showToast(root,context)
                            else
                                showSnack(root, context, errorRes?.error)
                        }
                    }
                }
            if (errorRes?.error == "UNAUTHORIZED" || errorRes?.error == "Unauthorized") {

                prefsManager.logout()
                val intent =
                    Intent(context, com.Lyber.dev.ui.activities.SplashActivity::class.java).apply {
                        putExtra(Constants.IS_LOGOUT, true).flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                    Intent.FLAG_ACTIVITY_NEW_TASK
                    }

                ContextCompat.startActivity(context, intent, null)
            }
            return 0
        }

        fun Long.is1DayOld(): Boolean {
            return abs((System.currentTimeMillis() - absoluteValue)) > abs((24 * 3600000))
        }

        fun Long.is30DaysOld(): Boolean {
            return abs((System.currentTimeMillis() - absoluteValue)) > abs(30 * (24 * 3600000))
        }

        val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()

        fun TextView.addTypeface(typeface: Int) {
            setTypeface(this.typeface, typeface)
        }

        fun View.visibleFromRight() {
            slideToRight(this, View.VISIBLE)
        }

        fun View.goneToRight() {
            slideToRight(this, View.GONE)
        }

        fun View.visibleFromLeft() {
            slideToLeft(this, View.VISIBLE)
        }

        fun View.goneToLeft() {
            slideToLeft(this, View.GONE)
        }

        fun View.goneToTop() {
            slideToTop(this, View.GONE)
        }

        fun View.goneToBottom() {
            slideToBottom(this, View.GONE)
        }


        fun View.fadeIn() {
            val animation = AlphaAnimation(0F, 1F)
            animation.duration = 300
            startAnimation(animation)
            visible()
        }

        fun View.fadeOut() {
            val animation = AlphaAnimation(1F, 0.0F)
            animation.duration = 300
            startAnimation(animation)
            gone()
        }

        fun View.focusOut() {
            val animation = AlphaAnimation(0.0f, 0.5f)
            animation.duration = 300
            startAnimation(animation)
            gone()
        }

        fun zoomIn(view: View) {
            val animation = ScaleAnimation(0F, 1F, 0F, 1F, 0.5F, 0.5F)
            animation.duration = 300
            view.startAnimation(animation)
            view.visible()
        }

        fun zoomOut(view: View) {
            val animation = ScaleAnimation(1F, 0F, 1F, 0F, 0.5F, 0.5F)
            animation.duration = 300
            view.startAnimation(animation)
            view.gone()
        }

        // To animate view slide out from left to right
        private fun slideToRight(view: View, visibility: Int) {
            val animate = if (visibility == View.VISIBLE) TranslateAnimation(
                view.width.toFloat(),
                0F,
                0F,
                0F
            ) else TranslateAnimation(0F, view.width.toFloat(), 0F, 0F)
            animate.duration = 300
//            animate.fillAfter = true
            view.startAnimation(animate)
            animate.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    view.visibility = visibility
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }

        // To animate view slide out from right to left
        private fun slideToLeft(view: View, visibility: Int) {

            val animate = if (visibility == View.VISIBLE) TranslateAnimation(
                (-view.width).toFloat(),
                0F,
                0F,
                0F
            ) else TranslateAnimation(0F, (-view.width).toFloat(), 0F, 0F)
            animate.duration = 300
//            animate.fillAfter = true
            animate.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    view.visibility = visibility
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
            view.startAnimation(animate)

        }

        // To animate view slide out from top to bottom
        private fun slideToBottom(view: View, visibility: Int) {

            val animate = if (visibility == View.VISIBLE) TranslateAnimation(
                0F,
                0F,
                view.height.toFloat(),
                0F
            ) else TranslateAnimation(0F, 0F, 0F, view.height.toFloat())
            animate.duration = 300
//            animate.fillAfter = true
            view.startAnimation(animate)
            view.visibility = visibility
        }

        // To animate view slide out from bottom to top
        private fun slideToTop(view: View, visibility: Int) {

            val animate = if (visibility == View.VISIBLE) TranslateAnimation(
                0F,
                0F,
                (-view.height).toFloat(),
                0F
            ) else TranslateAnimation(0F, 0F, 0F, (-view.height).toFloat())
            animate.duration = 300
//            animate.fillAfter = true
            view.startAnimation(animate)
            view.visibility = visibility
        }

        fun List<Double>.formLineData(): List<Entry> {
            val list = mutableListOf<Entry>()
            for (i in 0 until count() step 10) {
                list.add(Entry((i + 1).toFloat(), this[i].toFloat()))
            }
            return list
        }

        fun getRedLineData(): List<Entry> {
            val lineValues = ArrayList<Entry>()
            lineValues.add(Entry(20f, 50.0F))
            lineValues.add(Entry(30f, 40.0F))
            lineValues.add(Entry(40f, 20.0F))
            lineValues.add(Entry(50f, 1.0F))
            lineValues.add(Entry(60f, 5.0F))
            lineValues.add(Entry(70f, 10.0F))
            lineValues.add(Entry(80f, 15.0F))
            lineValues.add(Entry(90f, 20.0F))
            lineValues.add(Entry(100f, 10.0F))
            lineValues.add(Entry(110f, 24.0F))
            lineValues.add(Entry(120f, 20.0F))
            lineValues.add(Entry(130f, 16.0F))
            lineValues.add(Entry(140f, 8.0F))
            return lineValues
        }

        fun getLineData(): List<Entry> {
            val lineValues = ArrayList<Entry>()
            lineValues.add(Entry(20f, 0.0F, "$46434"))
            lineValues.add(Entry(30f, 0.0F))
            lineValues.add(Entry(40f, 0.0F))
            lineValues.add(Entry(50f, 0.0F))
            lineValues.add(Entry(60f, 0.0F))
            lineValues.add(Entry(70f, 0.0F))
            lineValues.add(Entry(80f, 0.0F))
            lineValues.add(Entry(90f, 0.0F))
            lineValues.add(Entry(100f, 0.0F))
            lineValues.add(Entry(110f, 0.0F))
            lineValues.add(Entry(120f, 0.0F))
            lineValues.add(Entry(130f, 0.0F))
            lineValues.add(Entry(140f, 0.0F, "$2246"))
            return lineValues
        }

        fun Float.toPx(context: Context): Int {
            val r = context.resources
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                this,
                r.displayMetrics
            ).roundToInt()
        }

        fun FragmentActivity.shouldShowPermission(permission: String): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                permission
            )
        }

        fun FragmentActivity.checkPermission(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun Activity.checkPermission(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }


        fun List<View>.gone() {
            forEach { it.gone() }
        }

        fun List<View>.visible() {
            forEach { it.visible() }
        }

        fun View.gone() {
            visibility = View.GONE
        }

        fun View.visible() {
            visibility = View.VISIBLE
        }

        fun View.invisible() {
            visibility = View.INVISIBLE
        }

        fun TextView.strikeText() {
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        fun View.setBackgroundTint(color: Int) {
            (background as Drawable).setTint(context.getColor(color))
        }

        fun isValidEmail(string: String): Boolean {
            return string.contains(Regex("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+\$"))
        }

        fun FragmentActivity.clearBackStack() {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }


        inline fun <reified T : ViewModel> getViewModel(owner: ViewModelStoreOwner): T {
            return ViewModelProvider(owner)[T::class.java]
        }

        fun <T> T.showToast(view: View,context: Context) {
//            Toast.makeText(context, toString(), Toast.LENGTH_SHORT).show()
            showSnackBar(view,context, toString())
//            val customToastLayout = LayoutInflater.from(context)
//                .inflate(R.layout.custom_toast, null)
//            val textView = customToastLayout.findViewById<TextView>(R.id.tvToast)
//            textView.text = toString()
//            val customToast = Toast(context)
//            customToast.view = customToastLayout
//            customToast.setGravity(Gravity.TOP, 0, 152)
//            customToast.duration = Toast.LENGTH_SHORT
//            customToast.show()
        }

        private fun showSnackBar(view: View, context: Context, text: String) {
            val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT)
            val params = snackbar.view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            params.setMargins(24, 158, 24, 0)
            snackbar.view.layoutParams = params
            snackbar.view.background =
                context.getDrawable(R.drawable.curved_background_toast)

            snackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
            val layout = snackbar.view as Snackbar.SnackbarLayout
            val textView =
                layout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.visibility = View.INVISIBLE
            val snackView =
                LayoutInflater.from(context).inflate(R.layout.custom_toast, null)
            val textViewMsg = snackView.findViewById<TextView>(R.id.tvToast)
            val ivIcon = snackView.findViewById<ImageView>(R.id.ivIcon)
            ivIcon.visible()
            layout.setPadding(0, 0, 0, 0)
            layout.addView(snackView, 0)
            textViewMsg.text = text
//                getString(R.string.new_pass_cannot_be_same_as_old) + getString(R.string.new_pass_cannot_be_same_as_old)
            snackbar.show()
        }

        fun Fragment.showToast(string: String) {
            Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
        }

        fun getTextLineCount(context: Context, text: String, textSizeInSp: Float): Int {
            // Dynamically calculate the width (e.g., screen width minus padding)
            val defaultPaddingDp = 16
            val density = context.resources.displayMetrics.density
            val padding = (defaultPaddingDp * density).toInt()
            val textViewWidth =
                getScreenWidth(context) - 2 * padding

            // Create and measure the TextView as before
            val textView = TextView(context)
            textView.text = text
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp)

            val widthMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(textViewWidth, View.MeasureSpec.AT_MOST)
            val heightMeasureSpec =
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            textView.measure(widthMeasureSpec, heightMeasureSpec)

            val totalHeight = textView.measuredHeight
            val lineHeight = textView.lineHeight
            return totalHeight / lineHeight
        }

        fun getScreenWidth(context: Context): Int {
            val displayMetrics = context.resources.displayMetrics
            return displayMetrics.widthPixels
        }

        fun EditText.requestKeyboard() {
            requestFocus()
            val inputMethodManager: InputMethodManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            postDelayed({
                if (!inputMethodManager.showSoftInput(this, 0))
                    if (!inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT))
                        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_FORCED)
            }, 100)


        }

        fun EditText.isKeyboardVisible(): Boolean {
            return (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).isActive(
                this
            )
        }

        fun Fragment.postDelay(time: Long, delay: () -> Unit) {
            android.os.Handler(Looper.getMainLooper()).postDelayed({ delay() }, time)
        }


        fun Fragment.add(id: Int, fragment: Fragment) {
            childFragmentManager.beginTransaction()
                .add(id, fragment, fragment::class.java.simpleName).commit()
        }


        fun Fragment.replace(
            id: Int,
            fragment: Fragment,
            isBackStack: Boolean = true,
            topBottom: Boolean = false
        ) {
            childFragmentManager.beginTransaction().let {
                if (topBottom)
                    it.setCustomAnimations(
                        R.anim.slide_up,
                        0,
                        0,
                        R.anim.slide_down,
                    )
                else
                    it.setCustomAnimations(
                        R.anim.enter_from_right,
                        R.anim.exit_from_left,
                        R.anim.enter_from_left,
                        R.anim.exit_from_right,
                    )
                if (isBackStack)
                    it.replace(id, fragment, fragment::class.java.simpleName)
                        .addToBackStack(fragment::class.java.simpleName).commit()
                else
                    it.replace(id, fragment, fragment::class.java.simpleName).commit()
            }
        }


        fun FragmentActivity.addFragment(id: Int, fragment: Fragment) {
            supportFragmentManager.beginTransaction()
                .add(id, fragment, fragment::class.java.simpleName).commit()
        }

        fun FragmentActivity.replaceFragment(
            id: Int,
            fragment: Fragment,
            isBackStack: Boolean = true,
            topBottom: Boolean = false
        ) {
            supportFragmentManager.beginTransaction().let {
                if (topBottom)
                    it.setCustomAnimations(
                        R.anim.slide_up,
                        0,
                        0,
                        R.anim.slide_down,
                    )
//                    it.setCustomAnimations(
//                        R.anim.enter_from_bottom,
//                        R.anim.exit_from_top,
//                        R.anim.enter_from_top,
//                        R.anim.exit_from_bottom,
//                    )
                else
                    it.setCustomAnimations(
                        R.anim.enter_from_right,
                        R.anim.exit_from_left,
                        R.anim.enter_from_left,
                        R.anim.exit_from_right,
                    )
                if (isBackStack)
                    it.replace(id, fragment, fragment::class.java.simpleName)
                        .addToBackStack(fragment::class.java.simpleName).commit()
                else
                    it.replace(id, fragment, fragment::class.java.simpleName).commit()
            }

        }


        val String.pointFormatted
            get() = kotlin.run {

                if (contains(".")) {
                    if (split(".")[1].toInt() > 0)
                        trimCurrency.replace(",", "", true)
                    else split(".")[0].replace(",", "", true)
                } else
                    replace(",", "", true)
            }

        private val String.trimCurrency
            get() = kotlin.run {
                java.lang.StringBuilder().let {
                    for (i in this) {
                        if (i in '0'..'9' || i == ',' || i == '.')
                            it.append(i)
                        else break
                    }
                    return@run it.toString()
                }
            }


        private fun Number.pointFormatted(): String {
            return when (this) {
                is Float, is Double -> String.format("%,.2f", this)
                is Long, is Int -> String.format("%,d", this)
                else -> "$this"
            }
        }

        //        val <T> T.commaFormatted: String
//            get() = when (this) {
//                is Number -> this.commaFormatted
//                is String -> toDoubleOrNull().commaFormatted
//                is Char -> this.toString()
//                else -> "0"
//            }
        val <T> T.commaFormatted: String
            get() = when (this) {
                is Int -> String.format(Locale.US, "%d", this.toLong())
                is Number -> String.format(Locale.US, "%.2f", this)
                is String -> toDoubleOrNull()?.let { String.format(Locale.US, "%.2f", it) } ?: "0"
                is Char -> this.toString()
                else -> "0"
            }
        private val Number.commaFormatted: String
            get() = when (this) {

                is Float, is Double -> {

                    if (toString().contains(".")) {
                        val split = toString().split(".")[1]
                        if (split.toFloat() > 0)
                            when (split.length) {
                                1 -> String.format(Locale.US, "%,.1f", this)
                                else -> String.format(Locale.US, "%,.2f", this)
                            }
                        else String.format(
                            Locale.US,
                            "%,d",
                            toString().split(".")[0].toInt()
                        ) //+ ".00"

                    } else String.format(Locale.US, "%,2f", this) //+ ".00"

                }

                is Long, is Int -> String.format(Locale.US, "%,d", this) //+ ".00"

                else -> toString()

            }

        val String.currencyFormatted: SpannableString
            get() = kotlin.run {
                if (this.isEmpty()) {
                    SpannableString("0.00" + Constants.EURO)
                } else {
                    try {
                        var value: Double = 0.0
                        if (this.contains(","))
                            value = this.replace(",", "").toDouble()
                        else
                            value = this.toDouble()
                        var numberZerosLeft = 0 // we count also the coma
                        var formatter = DecimalFormat()
                        if (value > 10000) {
                            formatter.maximumFractionDigits = 0
                            formatter.minimumFractionDigits = 0
                        } else if (value > 1000) {
                            formatter.maximumFractionDigits = 1
                            formatter.minimumFractionDigits = 1
                        } else if (value > 1) {
                            formatter.maximumFractionDigits = 2
                            formatter.minimumFractionDigits = 2
                        } else if (value > 0.1) {
                            numberZerosLeft = 2
                            formatter.maximumFractionDigits = 3
                            formatter.minimumFractionDigits = 3
                        } else if (value > 0.01) {
                            numberZerosLeft = 3
                            formatter.maximumFractionDigits = 4
                            formatter.minimumFractionDigits = 4
                        } else if (value > 0.001) {
                            numberZerosLeft = 4
                            formatter.maximumFractionDigits = 5
                            formatter.minimumFractionDigits = 5
                        } else if (value > 0.0001) {
                            numberZerosLeft = 5
                            formatter.maximumFractionDigits = 6
                            formatter.minimumFractionDigits = 6
                        } else if (value > 0.00001) {
                            numberZerosLeft = 6
                            formatter.maximumFractionDigits = 7
                            formatter.minimumFractionDigits = 7
                        } else if (value > 0.000001) {
                            numberZerosLeft = 7
                            formatter.maximumFractionDigits = 8
                            formatter.minimumFractionDigits = 8
                        } else if (value > 0.0000001) {
                            numberZerosLeft = 8
                            formatter.maximumFractionDigits = 9
                            formatter.minimumFractionDigits = 9
                        } else if (value > 0.00000001) {
                            numberZerosLeft = 9
                            formatter.maximumFractionDigits = 10
                            formatter.minimumFractionDigits = 10
                        }

                        val symbols = DecimalFormatSymbols(Locale.US)
                        formatter.decimalFormatSymbols = symbols
                        val stringFormatted = formatter.format(value) + Constants.EURO
                        val ss1 = SpannableString(stringFormatted)
                        ss1.setSpan(RelativeSizeSpan(0.8f), 0, numberZerosLeft, 0) // set size
                        ss1
                    } catch (ex: NumberFormatException) {
                        SpannableString("0.00" + Constants.EURO)
                    }
                }
            }


        fun String.formattedAsset(
            price: Double?,
            rounding: RoundingMode,
            roundValue: Int = 0
        ): String {
            var priceFinal = price
            if (this == "" || priceFinal == null || priceFinal == 0.0 || priceFinal.isNaN()) {
                priceFinal = 1.026
            }

            val formatter = DecimalFormat("#.##########")

            // Pour trouver la précision, ici X
            // Prix * 10e-X >= 0.01 (centimes)
            // => X >= -log(0,01/Prix)
            val precision = ceil(-log10(0.01 / priceFinal)).toInt()
            if (precision > 0) {
                formatter.maximumFractionDigits = precision
                formatter.minimumFractionDigits = precision
            } else {
                formatter.maximumFractionDigits = 0
                formatter.minimumFractionDigits = 0
            }
            if (roundValue != 0) {
                formatter.maximumFractionDigits = roundValue
                formatter.minimumFractionDigits = roundValue
            }

            formatter.roundingMode = rounding

            val symbols = DecimalFormatSymbols(Locale.US)
            formatter.decimalFormatSymbols = symbols
            val valueFormatted = formatter.format(this.toDouble() ?: 0.0)
//            return valueFormatted.toString()
            return trimTrailingZeros(valueFormatted.toDouble())
        }

        fun trimTrailingZeros(number: Double): String {
            val formatted = String.format(
                Locale.US,
                "%.15f",
                number
            ) // Use a high precision for double to string conversion
            val trimmed = formatted.trimEnd('0').trimEnd('.')
            return if (trimmed == "-0") "0" else trimmed // Handle negative zero case
        }

        fun TextView.expandWith(string: String) {

            measureLayout()
            val startHeight = layout.height + paddingBottom + paddingTop

            if (string.length > 80) {
                text = string.substring(0..60)
                setSpan(true) {

                    val endHeight = layout.height + paddingBottom + paddingTop
                    text = string.trim()
                    ValueAnimator.ofInt(startHeight, endHeight).apply {
                        duration = 600
                        addUpdateListener {

                            if ((animatedValue as Int) < endHeight) {
                                val lp = layoutParams
                                lp.height = animatedValue as Int
                                layoutParams = lp
                            } else collapseWith(string)
                        }

                    }.start()

                }

            } else text = string
        }

        fun TextView.collapseWith(string: String) {
            measureLayout()
            val startHeight = layout.height + paddingBottom + paddingTop
            text = string.trim()
            setSpan(false) {
                val endHeight = layout.height + paddingBottom + paddingTop

                ValueAnimator.ofInt(startHeight, endHeight).apply {
                    duration = 600
                    addUpdateListener {
                        val lp = layoutParams
                        lp.height = animatedValue as Int
                        layoutParams = lp
                    }
                }.start()
            }
        }

        fun TextView.setSpan(viewMore: Boolean, clickAction: () -> Unit) {

            val string = text.trim().toString()
            val optionText = if (viewMore) "... View More" else "... View Less"

            val finalString = "$string$optionText"

            SpannableString(finalString).let {

                it.setSpan(
                    ForegroundColorSpan(getColor(context, R.color.purple_500)),
                    string.length,
                    finalString.length,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )

                it.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            clickAction()
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            ds.isUnderlineText = true
                            super.updateDrawState(ds)
                        }
                    },
                    string.length,
                    finalString.length,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )

                text = it
                movementMethod = LinkMovementMethod()
                measureLayout()

            }

        }

        fun TextView.setSpannable(viewMore: Boolean, clickAction: () -> Unit) {

            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            if (text.trim().toString().length > 90) {
                val spanText = if (viewMore) "View More" else "Learn More"
                val string = text.trim().toString().substring(0..78) + "... " + spanText
                SpannableString(string).apply {

                    /* color span */
                    setSpan(
                        ForegroundColorSpan(resources.getColor(R.color.purple_500, context.theme)),
                        (string.length - spanText.length),
                        string.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    setSpan(
                        object : ClickableSpan() {

                            override fun onClick(widget: View) {
                                clickAction()
                            }

                            override fun updateDrawState(ds: TextPaint) {
                                super.updateDrawState(ds)
                                highlightColor = Color.TRANSPARENT
                                ds.color = resources.getColor(R.color.purple_500, context.theme)
                                ds.typeface = Typeface.DEFAULT_BOLD
                                ds.isUnderlineText = true
                            }

                        },
                        (string.length - spanText.length),
                        string.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    movementMethod = LinkMovementMethod.getInstance()

                    text = this
                }
            }
        }

        fun saveImageToExternalStorage(finalBitmap: Bitmap, context: Context): File {
            val file: File

//            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                .toString()
            val root1 = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
            val myDir = File(root1 + "/PICS")
            myDir.mkdirs()
            val fname = (Constants.JPEG_FILE_PREFIX + System.currentTimeMillis()
                    + Constants.JPEG_FILE_SUFFIX)
            file = File(myDir, fname)
            if (file.exists())
                file.delete()
            try {
                val out = FileOutputStream(file)
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
                return file
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return file
        }

        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
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

        fun Bitmap.toFile(name: String): File? {
            return try {
                val file = File(
                    Environment.getExternalStorageDirectory().toString() + File.separator + name
                )
                file.createNewFile()

                //Convert bitmap to byte array
                val bos = ByteArrayOutputStream()
                compress(Bitmap.CompressFormat.PNG, 100, bos) // YOU can also save it in JPEG
                val bitmapdata = bos.toByteArray()

                //write the bytes in file
                val fos = FileOutputStream(file)
                fos.write(bitmapdata)
                fos.flush()
                fos.close()
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun View.measureLayout() {

            val widthSpecification = View.MeasureSpec.makeMeasureSpec(
                width,
                View.MeasureSpec.EXACTLY
            )
            val heightSpecification = View.MeasureSpec.makeMeasureSpec(
                height,
                View.MeasureSpec.EXACTLY
            )

            measure(widthSpecification, heightSpecification)
        }

        fun EditText.takesAlphabetOnly() {
            doAfterTextChanged {
                when {
                    text.trim().toString().isNotEmpty() -> {
                        if (text.trim().toString()
                                .last() !in context.getString(R.string.alphabets)
                        ) {
                            setText(text.trim().toString().dropLast(1))
                            setSelection(text.trim().toString().length)
                        }
                    }
                }
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun String.toMilli(): Long {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC") // Set time zone to UTC
                val date = dateFormat.parse(this)

                // Convert date to milliseconds in the default time zone
                return date.time
            } catch (ex: java.lang.Exception) {
                return System.currentTimeMillis()
            }
        }

        val ImageView.setProfile: Unit
            get() = kotlin.run {
                val matchingAvatar =
                    Constants.defaults.find { it.avatar_name == prefsManager.defaultImage }
                matchingAvatar?.let {
                    val drawableResId = it.avatar_is
                    setImageResource(drawableResId)
                }
//                setImageResource(Constants.defaults[prefsManager.defaultImage])
            }

        val List<List<Double>>.lineData: MutableList<Float>
            get() = kotlin.run {
                val list = mutableListOf<Float>()
                for (i in 0 until count() step 1) list.add(get(i)[1].toFloat())
                return@run list
            }


        private fun String.isHexadecimalFormat(): Boolean {

            val string: String

            try {
                string = when {
                    startsWith('1') -> split('1')[1]
                    startsWith('3') -> split('3')[1]
                    startsWith("bc1") -> split("bc1")[1]
                    startsWith("0x") -> split("0x")[1]
                    else -> ""
                }
            } catch (e: Exception) {
                Log.d("isHexadecimalFormat", ": ${e.message}")
                return false
            }

            for (i in string) {
                when (i) {
                    in SMALL_RANGE -> {
                        Log.d("isHexadecimalFormat", ": $i")
                        return false
                    }

                    in CAP_RANGE -> {
                        Log.d("isHexadecimalFormat", ": $i")
                        return false
                    }

                    else -> {}
                }
            }

            return true
        }

        fun String.addressMatched(format: String): Boolean {
            return this.matches(format.toRegex())
        }

        fun String.checkFormat(type: String): Boolean {

            return when (type) {

                "BTC", "BITCOIN", "bitoin", "Bitcoin" -> {
                    val lengthConstraint: Boolean = length == 40
                    val prefixConstraint: Boolean =
                        (startsWith('1') || startsWith('3') || startsWith("bc1"))
                    lengthConstraint && prefixConstraint
                }

                else -> {
                    val lengthConstraint: Boolean = length in 27..37
                    val prefixConstraint: Boolean = startsWith("0x")
                    lengthConstraint && prefixConstraint && this.isHexadecimalFormat()
                }

            }

        }

        fun String.checkAddressFormat() {

            when (this) {

                "BTC" ->
                    ((length in 27..37) &&
                            (startsWith('1') || startsWith('3') || startsWith("bc1")))

                "BCH" ->
                    ((length in 27..34) && (startsWith('q') || startsWith("bitcoincash")))

                "ETH" -> ((length == 42) && (startsWith("0x")))

                "EOS" -> (length == 12)

                "AVAX" -> (endsWith("avax1") &&
                        (startsWith('C') || startsWith('X') || startsWith('P')))

                "DOGE" -> startsWith("D")

                "LTC" -> startsWith("M")

                "XRP" -> startsWith("r")

                "ADA" -> startsWith("addr")

                "XLM" -> startsWith("G")

                "TRX" -> startsWith("T")

                "NEO" -> startsWith("N")

                "DASH" -> startsWith("X")

                "XTZ" -> startsWith("tz")

                "DOT" -> startsWith("1")

                "NEM", "XEM" -> startsWith("N")

                "WAVES" -> startsWith("3P")

                "ATOM" -> startsWith("cosmos")

            }
        }

        fun getSpan(
            fullString: String,
            spanString: String,
            color: Int,
            function: () -> Unit
        ): SpannableString {
            return SpannableString(fullString).apply {

                val clickSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        function.invoke()
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = color
                        ds.isUnderlineText = true
                        super.updateDrawState(ds)
                    }
                }

                setSpan(
                    clickSpan,
                    fullString.length - spanString.length,
                    fullString.length,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )

            }
        }

        fun getAsset(id: String): AssetBaseData {
            return com.Lyber.dev.ui.activities.BaseActivity.assets.first { it.id == id }
        }

        fun getBalance(id: String): Balance? {
            return com.Lyber.dev.ui.activities.BaseActivity.balances.firstOrNull { it.id == id }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getMonthsAndYearsBetweenWithApi26(
            startDate: String,
            endDate: String
        ): List<MonthsList> {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val startDateTime = LocalDate.parse(startDate, formatter)
            val endDateTime = LocalDate.parse(endDate, formatter)


            val period = Period.between(startDateTime, endDateTime)
            val months = period.toTotalMonths().toInt()

            val days = period.days

            val result = mutableListOf<MonthsList>()

            var currentDate = startDateTime
            for (i in 0..days) {
                result.add(
                    MonthsList(
                        "${
                            currentDate.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                                .toString().lowercase().replaceFirstChar { it.uppercase() }
                        } ${currentDate.year}", "${
                            currentDate.month.getDisplayName(TextStyle.FULL, Locale.FRENCH)
                                .toString().lowercase().replaceFirstChar { it.uppercase() }
                        } ${currentDate.year}"
                    ))
                currentDate = currentDate.plusDays(1)
            }

            return result.distinct()
        }

        fun getMonthsAndYearsBetween(startDate: String, endDate: String): List<MonthsList> {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val startDateTime = Calendar.getInstance()
            startDateTime.time = sdf.parse(startDate) ?: Date()

            val endDateTime = Calendar.getInstance()
            endDateTime.time = sdf.parse(endDate) ?: Date()

            val result = mutableListOf<MonthsList>()

            while (startDateTime.before(endDateTime) || startDateTime == endDateTime) {
                result.add(
                    MonthsList(
                        "${
                            startDateTime.getDisplayName(
                                Calendar.MONTH,
                                Calendar.LONG,
                                Locale.ENGLISH
                            )!!.lowercase().replaceFirstChar { it.uppercase() }
                        } ${startDateTime.get(Calendar.YEAR)}",
                        "${
                            startDateTime.getDisplayName(
                                Calendar.MONTH,
                                Calendar.LONG,
                                Locale.FRANCE
                            )!!.lowercase().replaceFirstChar { it.uppercase() }
                        } ${startDateTime.get(Calendar.YEAR)}"))

                startDateTime.add(Calendar.MONTH, 1)
            }
            return result
        }

        fun getCurrentDateTime(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone =
                TimeZone.getTimeZone("UTC") // Set the time zone to UTC to get the desired format

            val currentDate = Calendar.getInstance().time
            return sdf.format(currentDate)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun convertToYearMonthVersion26(inputDate: String): String {
            val inputFormat = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

            try {
                val temporalAccessor = inputFormat.parse(inputDate)
                val yearMonth = YearMonth.from(temporalAccessor)

                return yearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM", Locale.ENGLISH))
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle parsing exception if needed
                return ""
            }
        }

        fun convertToYearMonth(inputDate: String): String {
            val inputFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("yyyy-MM", Locale.ENGLISH)

            try {
                val date = inputFormat.parse(inputDate)
                return outputFormat.format(date)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle parsing exception if needed
                return ""
            }
        }

        fun encodeToBase64(input: String): String {
            val bytes = input.toByteArray(Charsets.UTF_8)
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            return base64
        }

        fun isValidPassword(password: String): Boolean {
            val passwordRegex = Regex("(?=.*[a-z])(?=.*[A-Z])(?=.*[^\\w]).{10,}")
            return passwordRegex.matches(password)
        }


        fun String.formattedAssetForInverseRatio(price: Double?, rounding: RoundingMode): String {
            var priceFinal = price
            if (this == "" || priceFinal == null || priceFinal == 0.0 || priceFinal.isNaN()) {
                priceFinal = 1.026
            }

            val formatter = DecimalFormat()

            // Pour trouver la précision, ici X
            // Prix * 10e-X >= 0.01 (centimes)
            // => X >= -log(0,01/Prix)
            //changed it for showing the required result
            val precision = 8 //ceil(-log10(0.01 / priceFinal)).toInt()
            if (precision > 0) {
                formatter.maximumFractionDigits = precision
                formatter.minimumFractionDigits = precision
            } else {
                formatter.maximumFractionDigits = 0
                formatter.minimumFractionDigits = 0
            }

            formatter.roundingMode = rounding

            val symbols = DecimalFormatSymbols(Locale.US)
            formatter.decimalFormatSymbols = symbols
            val valueFormatted = formatter.format(this.toDouble() ?: 0.0)

            return valueFormatted.toString()
        }

        fun String.decimalPointUptoTwoPlaces(): String {
            val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.ENGLISH))
            return decimalFormat.format(toDouble())
        }

        fun getDeviceLocale(context: Context): String {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return context.resources.configuration.locales[0].country
            } else {
                @Suppress("DEPRECATION")
                return ""
            }
        }

        fun getfile(imgPath: String?): Bitmap? {
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

        fun showSnack(root: View, context: Context, textMsg: String?) {
            val snackbar = Snackbar.make(root, "", Snackbar.LENGTH_SHORT)
            val params = snackbar.view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            params.setMargins(24, 158, 24, 0)
            snackbar.view.layoutParams = params
            snackbar.view.background =
                context.getDrawable(R.drawable.curved_background_toast)

            snackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
            val layout = snackbar.view as Snackbar.SnackbarLayout
            val textView =
                layout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.visibility = View.INVISIBLE
            val snackView =
                LayoutInflater.from(context).inflate(R.layout.custom_toast, null)
            val textViewMsg = snackView.findViewById<TextView>(R.id.tvToast)
            if (textMsg == null)
                textViewMsg.text = context.getString(R.string.kyc_under_verification)
            else
                textViewMsg.text = textMsg

            layout.setPadding(0, 0, 0, 0)
            layout.addView(snackView, 0)
            snackbar.show()
        }

        fun isFaceIdAvail(context: Context): Boolean {
            val packageManager = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return packageManager?.hasSystemFeature(PackageManager.FEATURE_FACE) == true
            } else
                return false
        }

        fun setProgressDialogAlert(context: Context): AlertDialog? {

            if (alertDialog == null) {

                // Inflate the custom progress bar layout
                val customProgressBarView =
                    LayoutInflater.from(context).inflate(R.layout.progress_bar, null)
                val llPadding = 480
                customProgressBarView.setPadding(llPadding, llPadding, llPadding, llPadding)

                // Create a dialog builder
                val builder = AlertDialog.Builder(context)
                builder.setCancelable(false)
                builder.setView(customProgressBarView) // Set the custom progress bar view as the view

                // Create the dialog
                alertDialog = builder.create()

                // Set a listener to start animation when the dialog is shown
                alertDialog!!.setOnShowListener {
                    try {
                        alertDialog!!.findViewById<ImageView>(R.id.progressImage)?.startAnimation(
                            AnimationUtils.loadAnimation(context, R.anim.rotate_drawable)
                        )
                    } catch (e: Exception) {
                        Log.d("Exception", "DialogAlert: ${e.message}")
                    }
                }

                // Modify the window attributes
                val window = alertDialog!!.window
                if (window != null) {
                    val layoutParams = WindowManager.LayoutParams().apply {
                        copyFrom(alertDialog!!.window?.attributes)
                        width = LinearLayout.LayoutParams.WRAP_CONTENT
                        height = LinearLayout.LayoutParams.WRAP_CONTENT

                        alertDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dimAmount = 0.0f
                    }
                    alertDialog!!.window?.attributes = layoutParams
                }

            }


            try {
                alertDialog!!.show()
            } catch (e: WindowManager.BadTokenException) {
                Log.d("Exception", "DialogAlert: ${e.message}")
                alertDialog!!.dismiss()
                setProgressDialogAlert(context)
            } catch (e: Exception) {
                Log.d("Exception", "DialogAlert: ${e.message}")
            }

            return alertDialog
        }

        fun dismissAlertDialog() {
            alertDialog?.let {
                try {
                    it.findViewById<ImageView>(R.id.progressImage)!!.clearAnimation()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                it.dismiss()
            }
        }

        fun logOut(context: Context) {
            com.Lyber.dev.ui.activities.BaseActivity.balances = ArrayList<Balance>()
            com.Lyber.dev.ui.activities.BaseActivity.assets = ArrayList<AssetBaseData>()
            com.Lyber.dev.ui.activities.BaseActivity.networkAddress = ArrayList<Network>()
            com.Lyber.dev.ui.activities.BaseActivity.balances = ArrayList<Balance>()
            com.Lyber.dev.ui.activities.BaseActivity.balanceResume = ArrayList<PriceServiceResume>()

            App.prefsManager.logout()
            context.startActivity(
                Intent(
                    context,
                    com.Lyber.dev.ui.activities.SplashActivity::class.java
                ).apply {
                    putExtra("fromLogout", "fromLogout")
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            )
        }

        fun showDocumentDialog(context: Context, typeOfLoader: Int, tt: Boolean = false) {
            try {
                if (dialogVerification == null && typeOfLoader != Constants.LOADING_DISMISS) {
                    dialogVerification = Dialog(context)
                    dialogVerification!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialogVerification!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialogVerification!!.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    dialogVerification!!.window!!.setDimAmount(0.3F)
                    dialogVerification!!.setCancelable(false)
                    dialogVerification!!.setContentView(
                        DocumentBeingVerifiedBinding.inflate(
                            LayoutInflater.from(
                                context
                            )
                        ).root
                    )
                }
                if (dialogVerification != null && typeOfLoader == Constants.LOADING_DISMISS) {
                    val viewImage =
                        dialogVerification!!.findViewById<LottieAnimationView>(R.id.animationView)
                    val tvDocVerified =
                        dialogVerification!!.findViewById<TextView>(R.id.tvDocVerified)
                    val imageView = dialogVerification!!.findViewById<ImageView>(R.id.ivCorrect)!!

                    viewImage.clearAnimation()
                    tvDocVerified.gone()
                    viewImage.gone()
                    imageView.gone()
                    AppLifeCycleObserver.documentDialogVisible = false
                    dialogVerification!!.dismiss()
                } else {
                    try {
                        val viewImage =
                            dialogVerification!!.findViewById<LottieAnimationView>(R.id.animationView)
                        viewImage.visible()
                        val tvDocVerified =
                            dialogVerification!!.findViewById<TextView>(R.id.tvDocVerified)
                        if (!tt)
                            tvDocVerified.text =
                                context.getString(R.string.kyc_under_verification_text)
                        else
                            tvDocVerified.text = context.getString(R.string.doc_being_verified)
                        tvDocVerified.visible()
                        val imageView =
                            dialogVerification!!.findViewById<ImageView>(R.id.ivCorrect)!!
                        when (typeOfLoader) {


                            Constants.LOADING -> {
                                imageView.gone()
                                viewImage!!.playAnimation()
                                viewImage.setMinAndMaxProgress(0f, .32f)
                            }

                            Constants.LOADING_SUCCESS -> {
                                viewImage.clearAnimation()
                                tvDocVerified.gone()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    imageView.visible()
                                    imageView.setImageResource(R.drawable.baseline_done_24)
                                }, 50)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    tvDocVerified.gone()
                                    dialogVerification!!.dismiss()
                                    AppLifeCycleObserver.documentDialogVisible = false
                                }, 400)
                            }

                            Constants.LOADING_FAILURE -> {
                                viewImage.clearAnimation()
                                tvDocVerified.text = context.getString(R.string.kyc_refused_text)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    imageView.visible()
                                    imageView.setImageResource(R.drawable.baseline_clear_24)
                                }, 50)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    tvDocVerified.gone()
                                    dialogVerification!!.dismiss()
                                    AppLifeCycleObserver.documentDialogVisible = false
                                }, 400)
                            }

                        }


                        /*(0f,.32f) for loader
                        * (0f,.84f) for success
                        * (0.84f,1f) for failure*/


                        dialogVerification!!.show()
                    } catch (e: WindowManager.BadTokenException) {
                        Log.d("Exception", "showProgressDialog: ${e.message}")
                        dialogVerification!!.dismiss()
                    } catch (e: Exception) {
                        Log.d("Exception", "showProgressDialog: ${e.message}")
                    }
                }
            } catch (_: Exception) {

            }


        }

        fun dismissDocumentDialog() {
            try {
                if (dialogVerification == null)
                    dialogVerification?.dismiss()
            } catch (_: Exception) {

            }
        }

        fun <T> T.commaFormattedDecimal(decimalPlaces: Int): String {
            val formatString = "%.${decimalPlaces}f"
            return when (this) {
                is Int -> String.format(Locale.US, "%d", this.toDouble())
                is Number -> String.format(Locale.US, formatString, this)
                is String -> toDoubleOrNull()?.let { String.format(Locale.US, formatString, it) }
                    ?: "0"

                is Char -> this.toString()
                else -> "0"
            }
        }

        fun getTruncatedText(text: String, maxLength: Int): String {
            try {
                if (text.length <= maxLength) {
                    return text
                }
                val startLength = (maxLength - 3) / 2
                val endLength = 6
                return text.substring(
                    0,
                    startLength
                ) + "..." + text.substring(text.length - endLength)

            } catch (_: Exception) {
                return text
            }
        }
    }
}


