package com.machy1979ii.intervaltimer.services

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics

object TimerAnalytics {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    fun getInstance(context: Context): TimerAnalytics {
        try {
            if (mFirebaseAnalytics == null) {
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(context)
                Log.d("IntervalT Analytics:", "Inicializace analytiky 1")
            }
            Log.d("IntervalT Analytics:", "Inicializace analytiky 2")
        } catch (e: Exception) {
            // Zalogujte chybu
            Log.e("IntervalT Analytics:", "Error initializing Firebase Analytics: ${e.message}")
            // Oznamujte uživateli (volitelně)

            Log.d("IntervalT Analytics:", "Došlo k chybě při inicializaci analytiky.")
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
        Log.d("IntervalT Analytics:", "1:"+activityName)
        mFirebaseAnalytics?.logEvent(activityName+"_opened", bundle)
        Log.d("IntervalT Analytics:", "2:"+activityName)
    }
}