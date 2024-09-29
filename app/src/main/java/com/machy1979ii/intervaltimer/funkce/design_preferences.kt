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
    try {//nejdříve zjistí, zda už tam aplikace byla nainstalovaná, v tom případě by se lišily údaje first a last...
        val firstInstallTime =
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        val lastUpdateTime =
            context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
        if(firstInstallTime == lastUpdateTime) {
            //jde o první instalaci, tak se nastaví design na 3, pokud při dalším spuštění si to uživatel nepřenastavil
            vybranyDesign = sharedPrefs.getInt( "vybrany_design" , 3 )
            Log.d("vybranyDesign setting: ", "vybrany design 3 - prvni instalace")
        }
        else {
            //nejde o první instalalaci, tak se design načte, pokud se jedná o v. 4.3 a nižší, kdy tam tato funkce ještě nebyla a nebyla tak možnost výběru designu,
            //automaticky se nastaví vybranyDesign na 1, v opačném případě se načte vybraný design, který tam je
            vybranyDesign = sharedPrefs.getInt( "vybrany_design" , 1 )
            Log.d("vybranyDesign setting: ", "vybrany design 1 - je to update")
        }


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
