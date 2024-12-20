package com.machy1979ii.intervaltimer.funkce

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibratorTimer { //singleton, který zajistí vibrování v posuvníku dialogu
    private var vibrator: Vibrator? = null

    fun vibrate(context: Context) {
        if (vibrator == null) {
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(30)
            }
        }
    }
}