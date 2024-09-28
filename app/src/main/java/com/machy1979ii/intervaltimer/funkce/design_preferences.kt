package com.machy1979ii.intervaltimer.funkce

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

//metoda pro načtení zvoleného designu, 1- starý design, 2- nový, 3- nový s progressbarem
public fun getDesignPreferences(context: Context): Int {
    Log.i("vybranyDesign: ", "222")
    var vybranyDesign = 1
    val sharedPrefs = context.getSharedPreferences(
        context.getPackageName() + "_hodnoty_aplikace", Context.MODE_PRIVATE)
    try {
            vybranyDesign = sharedPrefs.getInt( "vybrany_design" , 1 )

    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        vybranyDesign = 1
    }
    return vybranyDesign
}

//metoda pro uložení zvoleného designu, 1- starý design, 2- nový, 3- nový s progressbarem
public fun setDesignPreferences(context: Context, vybranyDesign: Int) {
    val sharedPrefs = context.getSharedPreferences(
        context.getPackageName() + "_hodnoty_aplikace", Context.MODE_PRIVATE)
    val editor = sharedPrefs.edit()
    editor.putInt("vybrany_design", vybranyDesign)
    editor.commit();
    Log.i("vybranyDesign: ", "ulozeno:" + vybranyDesign.toString())

}
@Suppress("DEPRECATION")
fun Context.getPackageInfo(): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        packageManager.getPackageInfo(packageName, 0)
    }
}
