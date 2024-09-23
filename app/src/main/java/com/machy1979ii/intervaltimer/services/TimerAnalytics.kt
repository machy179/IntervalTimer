package com.machy1979ii.intervaltimer.services

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

object TimerAnalytics {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    fun getInstance(context: Context): TimerAnalytics {
        try {
            if (mFirebaseAnalytics == null) {
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
            }
        } catch (e: Exception) {
            // Zalogujte chybu
            Log.e("Boxing Analytics:", "Error initializing Firebase Analytics: ${e.message}")
            // Oznamujte uživateli (volitelně)
            // Toast.makeText(context, "Došlo k chybě při inicializaci analytiky.", Toast.LENGTH_SHORT).show()
        }
/*        synchronized(this) {
            if (mFirebaseAnalytics == null) {
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
            }
        }*/
        return this
    }

    fun logActivityStart(activityName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_NAME, activityName)
        }
        Log.d("Boxing Analytics:", "1:"+activityName)
        mFirebaseAnalytics?.logEvent(activityName+"_opened", bundle)
    }
}