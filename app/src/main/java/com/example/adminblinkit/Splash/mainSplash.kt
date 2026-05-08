package com.example.adminblinkit.Splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.example.adminblinkit.Authentication.Signup
import com.example.adminblinkit.R

class mainSplash : AppCompatActivity() {

    private lateinit var chefAnimation: LottieAnimationView
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var simpleLoader: View
    private lateinit var appNameContainer: View
    private lateinit var tagline: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_splash)

        chefAnimation = findViewById(R.id.admin28)
        simpleLoader = findViewById(R.id.simpleLoader)
        appNameContainer = findViewById(R.id.appNameContainer)
        tagline = findViewById(R.id.tagline)

        // Start me sirf loader show hoga
        simpleLoader.visibility = View.VISIBLE
        chefAnimation.visibility = View.INVISIBLE
        appNameContainer.visibility = View.INVISIBLE
        tagline.visibility = View.INVISIBLE

        chefAnimation.repeatCount = LottieDrawable.INFINITE

        /*
         Important:
         root.post se pehle loader screen par draw ho jayega.
         Uske baad hum Lottie load karenge.
        */
        findViewById<View>(R.id.main88).post {

            chefAnimation.addLottieOnCompositionLoadedListener {
                showMainSplashUi()
            }

            // XML se rawRes hata kar yaha se animation load karo
            chefAnimation.setAnimation(R.raw.chef)
        }

        // Safety fallback
        handler.postDelayed({
            if (chefAnimation.visibility != View.VISIBLE) {
                showMainSplashUi()
            }
        }, 1500)

        handler.postDelayed({
            startActivity(Intent(this, Signup::class.java))
            finish()
        }, 4000)
    }

    private fun showMainSplashUi() {
        simpleLoader.visibility = View.GONE

        chefAnimation.visibility = View.VISIBLE
        appNameContainer.visibility = View.VISIBLE
        tagline.visibility = View.VISIBLE

        chefAnimation.playAnimation()
    }

    override fun onResume() {
        super.onResume()

        if (::chefAnimation.isInitialized && chefAnimation.visibility == View.VISIBLE) {
            chefAnimation.playAnimation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}