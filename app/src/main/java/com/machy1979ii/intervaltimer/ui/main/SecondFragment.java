package com.machy1979ii.intervaltimer.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.machy1979ii.intervaltimer.R;
import com.machy1979ii.intervaltimer.SetSoundTabataActivity;
import com.machy1979ii.intervaltimer.TabataActivity;
import com.machy1979ii.intervaltimer.funkce.PraceSeSouboremTabata;
import com.machy1979ii.intervaltimer.models.MyTime;

import java.io.IOException;

import petrov.kristiyan.colorpicker.ColorPicker;

public class SecondFragment extends Fragment {

    //polozka celkový čas
    private MyTime casCelkovy = new MyTime(0, 60, 0);
    //    private Dialog dialogPriprava;
    private TextView hodnotaCelkovyCasTextView;

    //polozka příprava
    private MyTime casPripravy = new MyTime(0, 1, 0);
    private Dialog dialogPriprava;
    private TextView hodnotaPripravyTextView;

    private LinearLayout dlazdiceCasPripravy; //color
    private int colorDlazdiceCasPripravy; //color

    //polozka cvičení
    private MyTime casCviceni = new MyTime(0, 0, 20);
    private Dialog dialogCviceni;
    private TextView hodnotaCviceniTextView;

    private LinearLayout dlazdiceCasCviceni; //color
    private int colorDlazdiceCasCviceni; //color

    //polozka pauza
    private MyTime casPauzy = new MyTime(0, 0, 20);
    private Dialog dialogPauzy;
    private TextView hodnotaPauzyTextView;

    private LinearLayout dlazdiceCasPauzy; //color
    private int colorDlazdiceCasPauzy; //color

    //polozka čas mezi tabatami
    private MyTime casMeziTabatami = new MyTime(0, 1, 0);
    private Dialog dialogPauzyMeziTabatami;
    private TextView hodnotaPauzyMeziTabatamiTextView;

    private LinearLayout dlazdiceCasPauzyMeziTabatami; //color
    private int colorDlazdiceCasPauzyMeziTabatami; //color

    //polozka čas cool down
    private MyTime casCoolDown = new MyTime(0, 1, 0);
    private Dialog dialogCoolDown;
    private TextView hodnotaCasuCoolDownTextView;

    private LinearLayout dlazdiceCasCoolDown; //color
    private int colorDlazdiceCasCoolDown; //color


    //polozka počet tabat
    private int pocetTabat = 4;
    private Dialog dialogPoctuTabat;
    private TextView pocetTabatTextView;

    private LinearLayout dlazdiceTabaty; //color
    private int colorDlazdiceTabaty; //color


    //polozka počet cyklů
    private int pocetCyklu = 8;
    private Dialog dialogPocetCyklu;
    private TextView pocetCykluTextView;
    private LinearLayout dlazdiceCykly; //kola

    //uložení a načtení dat ze souboru
    private PraceSeSouboremTabata soubor;
    private String dataSoubor;
    private StringBuilder nactenyTextZeSouboru;
    private boolean posledniHodnotaNacteni = false;

    //nastavení zvuků
    private int zvukStart = 1;
    private int zvukStop = 1;
    private int zvukCelkovyKonec = 1;
    private int zvukCountdown= 1;

    private int zvukStopTabatas = 1;
    private String dataSouborTime;
    private StringBuilder nactenyTextZeSouboruTime;
    private int hlasitost = 100;

    //nastavení color ale jen ze souboru, ostatní data jsou viz výše přímo u svých times
    private String dataSouborColor;
    private StringBuilder nactenyTextZeSouboruColor;

    // timhle udělám to, že načtu tuto instanci MainActivituy nedostanu se sem tlačítkem zpět
    //protože kdyby někdo furt dával zpět, tak by šel např. na tu první MainActivitu, ale v té první není ještě uložený zvuk
    //druhá část tohoto je je v SetSoundAdctivity, kdy tuto původní MainActivitu zničí
    public static Activity self_intent;;

    private View rootView;
    private FrameLayout frameLayout;


    private LinearLayout showTimePickerDialogNastavSetupZvukuLayout;
    private LinearLayout showTimePickerDialogNastavPripravuLayout;
    private LinearLayout showPickerDialogNastavPripravuColor;
    private LinearLayout showTimePickerDialogNastavCviceniLayout;
    private LinearLayout showPickerDialogNastavCviceniColor;
    private LinearLayout showTimePickerDialogNastavPauzuLayout;
    private LinearLayout showPickerDialogNastavPauzuColor;
    private LinearLayout showTimePickerDialogNastavPauzuMeziTabatami;
    private LinearLayout showPickerDialogNastavPauzuMeziTabatamiColor;
    private LinearLayout showTimePickerDialogNastavCoolDown;
    private LinearLayout showPickerDialogNastavCoolDownColor;
    private LinearLayout zmacknutyStartLayout;

    private LinearLayout showPickerDialogNastavTabatyColor;

    private ActivityResultLauncher<Intent> activityResultLaunch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        frameLayout = new FrameLayout(getActivity());
        LayoutInflater inflater2 = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater2.inflate(R.layout.fragment_second, null);
        frameLayout.addView(rootView);

     //   rootView =  inflater.inflate(R.layout.fragment_second, container, false);

        activityResultLaunch = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent intent = result.getData();


                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.i("getResult","OK");
                            nactiZvukyZActivitySetSound(intent);
                        } else {
                            Log.i("getResult","CANCELED");
                        }
                    }
                });

        vytvorView();
        udelejLayout();
        nactiColors();
        vytvorClickaciLayouty();

        return frameLayout;
     //   return rootView;
    }

    public void vytvorView(){
        nactiHodnoty();
        nactiZvuky();
    }

    private void nactiZvukyZActivitySetSound(Intent intent) {
        zvukStart = intent.getIntExtra("zvukstartTabata", 0);
        zvukStop = intent.getIntExtra("zvukstopTabata", 0);
        zvukCelkovyKonec = intent.getIntExtra("zvukcelkovykonecTabata", 0);
        zvukCountdown = intent.getIntExtra("zvukcountdownTabata", 0);
        zvukStopTabatas = intent.getIntExtra("zvukstoptabatasTabata", 0);
        hlasitost = intent.getIntExtra("hlasitostTabata", 100);

        ulozDataZvukDoSouboru();
    }



    private void nactiZvuky() {
       //pokud se spustí aplikace, tak je potřba níže uvedené proměnné načíst ze souboru
        //a raději než se ze souboru načtou, tak jim dám předem hodnotu 1, kdyby se něco s načtením ze souboru podělalo
        //nebo kdyby to bylo první načtení, kdy ještě neexistuje soubor "datatime.txt"

            zvukStart = 1;
            zvukStop = 1;
            zvukStopTabatas = 1;
            zvukCelkovyKonec = 21;
            zvukCountdown =1;

            //načtení hodnot pro zvuky ze souboru
            soubor = new PraceSeSouboremTabata();
            String nactenyZvuk = "";
            try {
                nactenyZvuk = soubor.readFromInternalFileZvuk(getActivity().getApplicationContext());
                Log.i("zvukNacteno",String.valueOf(nactenyZvuk));
                prevedNactenyTextDoPromennychTime(nactenyZvuk);

            } catch (IOException e) {
                e.printStackTrace();
            }


    }
    private void nactiColors() {
        //v tuto dobu budou načteny colors defaultně, protože tato metoda se volá déle, než metoda udelejLayout
        // ve které se defaultní hodnoty do colors načítají


        Log.i("asdf:", "11");
        //načtení hodnot pro colors ze souboru, pokud už existuje soubor datacolor.txt
        soubor = new PraceSeSouboremTabata();
        String nactenyColors = "";
        Log.i("asdf:", "12");
        try {
            Log.i("asdf:", "13");
            nactenyColors = soubor.readFromInternalFileColor(getActivity().getApplicationContext());
            Log.i("asdf:", "14");
            prevedNactenyTextDoPromennychColor(nactenyColors);
            Log.i("asdf:", "15");

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private void nactiHodnoty() {
        soubor = new PraceSeSouboremTabata();

        String nacteny = "";
        try {
            nacteny = soubor.readFromInternalFile(getActivity().getApplicationContext());
            prevedNactenyTextDoPromennych(nacteny);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //    Toast.makeText(this, nacteny, Toast.LENGTH_SHORT).show();
    }


    private void prevedNactenyTextDoPromennychTime(String nactenyTime) {

        try {
            nactenyTextZeSouboruTime = new StringBuilder(nactenyTime);
            for (int i = 1; i <= 6; i++) {
                if (nactenyTextZeSouboruTime.charAt(1) == ';') {
                    if (i == 1) {
                        zvukStart = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    } else if (i == 2) {
                        zvukStop = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    } else if(i==3) {
                        zvukCelkovyKonec = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    } else if(i==4) {
                        zvukCountdown = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    } else if(i==5){
                        zvukStopTabatas = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    }  else if(i==6){
                        hlasitost = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    }
                    nactenyTextZeSouboruTime.delete(0, 2);
                } else if (nactenyTextZeSouboruTime.charAt(2) == ';') {
                    if (i == 1) {
                        zvukStart = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if (i == 2) {
                        zvukStop = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if(i==3) {
                        zvukCelkovyKonec = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if(i==4){
                        zvukCountdown = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if(i==5){
                        zvukStopTabatas = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if(i==6){
                        hlasitost = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    }
                    nactenyTextZeSouboruTime.delete(0, 3);
                } else if (nactenyTextZeSouboruTime.charAt(3) == ';') {
                    if (i == 6) {
                        hlasitost = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 3));
                    }
                    nactenyTextZeSouboruTime.delete(0, 4);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (zvukStart==0) { //kdyby náhodou došlo k nějaké chybě při ukládání, nebo nahrávání proměnných ze souboru, tak se to raději otestuje a implicitně to přiřadí hodnoty na 1
            zvukStart = 1;
            zvukStop = 1;
            zvukStopTabatas = 1;
            zvukCelkovyKonec = 1;
            zvukCountdown =1;
        }



    }
    private void prevedNactenyTextDoPromennychColor(String nactenyColor) {
        Log.i("asdf:", nactenyColor);
        try {
            Log.i("asdf:", "16");
            nactenyTextZeSouboruColor = new StringBuilder(nactenyColor);
            Log.i("asdf:", "17");
            for (int i = 1; i <= 6; i++) {
                if (nactenyTextZeSouboruColor.charAt(8) == ';') {
                    Log.i("asdf:", "charAt8");
                    if (i == 1) {
                        colorDlazdiceCasPripravy = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 8));
                        Log.i("asdf:", String.valueOf(colorDlazdiceCasPripravy));
                        nastavColorDlazdice("colorDlazdiceCasPripravy", colorDlazdiceCasPripravy,dlazdiceCasPripravy);
                    } else if (i == 2) {
                        colorDlazdiceCasCviceni = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 8));
                        Log.i("asdf:", String.valueOf(colorDlazdiceCasCviceni));
                        nastavColorDlazdice("colorDlazdiceCasCviceni", colorDlazdiceCasCviceni,dlazdiceCasCviceni);
                    } else if(i==3) {
                        colorDlazdiceCasPauzy = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 8));
                        Log.i("asdf:", String.valueOf(colorDlazdiceCasPauzy));
                        nastavColorDlazdice("colorDlazdiceCasPauzy", colorDlazdiceCasPauzy,dlazdiceCasPauzy);
                    } else if(i==4) {
                        colorDlazdiceTabaty = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 8));
                        nastavColorDlazdice("colorDlazdiceTabaty", colorDlazdiceTabaty,dlazdiceTabaty);
                    } else if(i==5) {
                        colorDlazdiceCasPauzyMeziTabatami = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 8));
                        nastavColorDlazdice("colorDlazdiceCasPauzyMeziTabatami", colorDlazdiceCasPauzyMeziTabatami,dlazdiceCasPauzyMeziTabatami);
                    } else {
                        colorDlazdiceCasCoolDown = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 8));
                        nastavColorDlazdice("colorDlazdiceCasCoolDown", colorDlazdiceCasCoolDown,dlazdiceCasCoolDown);

                    }
                    nactenyTextZeSouboruColor.delete(0, 9);
                } else if (nactenyTextZeSouboruColor.charAt(9) == ';') {
                    Log.i("asdf:", "charAt9");

                    if (i == 1) {
                        colorDlazdiceCasPripravy = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 9));
                        Log.i("asdf:", String.valueOf(colorDlazdiceCasPripravy));
                        nastavColorDlazdice("colorDlazdiceCasPripravy", colorDlazdiceCasPripravy,dlazdiceCasPripravy);
                    } else if (i == 2) {
                        colorDlazdiceCasCviceni = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 9));
                        Log.i("asdf:", String.valueOf(colorDlazdiceCasCviceni));
                        nastavColorDlazdice("colorDlazdiceCasCviceni", colorDlazdiceCasCviceni,dlazdiceCasCviceni);
                    } else if(i==3) {
                        colorDlazdiceCasPauzy = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 9));
                        Log.i("asdf:", String.valueOf(colorDlazdiceCasPauzy));
                        nastavColorDlazdice("colorDlazdiceCasPauzy", colorDlazdiceCasPauzy,dlazdiceCasPauzy);
                    } else if(i==4) {
                        colorDlazdiceTabaty = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 9));
                        nastavColorDlazdice("colorDlazdiceTabaty", colorDlazdiceTabaty,dlazdiceTabaty);
                    } else if(i==5) {
                        colorDlazdiceCasPauzyMeziTabatami = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 9));
                        nastavColorDlazdice("colorDlazdiceCasPauzyMeziTabatami", colorDlazdiceCasPauzyMeziTabatami,dlazdiceCasPauzyMeziTabatami);
                    } else {
                        colorDlazdiceCasCoolDown = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 9));
                        nastavColorDlazdice("colorDlazdiceCasCoolDown", colorDlazdiceCasCoolDown,dlazdiceCasCoolDown);

                    }
                    nactenyTextZeSouboruColor.delete(0, 10);
                } else if (nactenyTextZeSouboruColor.charAt(10) == ';') {
                    Log.i("asdf:", "charAt10");
                    if (i == 1) {
                        colorDlazdiceCasPripravy = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 10));
                        Log.i("asdf:", String.valueOf(colorDlazdiceCasPripravy));
                        nastavColorDlazdice("colorDlazdiceCasPripravy", colorDlazdiceCasPripravy,dlazdiceCasPripravy);
                    } else if (i == 2) {
                        colorDlazdiceCasCviceni = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 10));
                        Log.i("asdf:", String.valueOf(colorDlazdiceCasCviceni));
                        nastavColorDlazdice("colorDlazdiceCasCviceni", colorDlazdiceCasCviceni,dlazdiceCasCviceni);
                    } else if(i==3) {
                        colorDlazdiceCasPauzy = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 10));
                        Log.i("asdf:", String.valueOf(colorDlazdiceCasPauzy));
                        nastavColorDlazdice("colorDlazdiceCasPauzy", colorDlazdiceCasPauzy,dlazdiceCasPauzy);
                    } else if(i==4) {
                        colorDlazdiceTabaty = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 10));
                        nastavColorDlazdice("colorDlazdiceTabaty", colorDlazdiceTabaty,dlazdiceTabaty);
                    } else if(i==5) {
                        colorDlazdiceCasPauzyMeziTabatami = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 10));
                        nastavColorDlazdice("colorDlazdiceCasPauzyMeziTabatami", colorDlazdiceCasPauzyMeziTabatami,dlazdiceCasPauzyMeziTabatami);
                    } else {
                        colorDlazdiceCasCoolDown = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 10));
                        nastavColorDlazdice("colorDlazdiceCasCoolDown", colorDlazdiceCasCoolDown,dlazdiceCasCoolDown);

                    }
                    nactenyTextZeSouboruColor.delete(0, 11);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void prevedNactenyTextDoPromennych(String nacteny) {
        try {
            nactenyTextZeSouboru = new StringBuilder(nacteny);
            casPripravy.setMin(nactiHodnotuOdepisJiZNactenehoTextu());
            casPripravy.setSec(nactiHodnotuOdepisJiZNactenehoTextu());
            casCviceni.setMin(nactiHodnotuOdepisJiZNactenehoTextu());
            casCviceni.setSec(nactiHodnotuOdepisJiZNactenehoTextu());
            casPauzy.setMin(nactiHodnotuOdepisJiZNactenehoTextu());
            casPauzy.setSec(nactiHodnotuOdepisJiZNactenehoTextu());
            casMeziTabatami.setMin(nactiHodnotuOdepisJiZNactenehoTextu());
            casMeziTabatami.setSec(nactiHodnotuOdepisJiZNactenehoTextu());
            casCoolDown.setMin(nactiHodnotuOdepisJiZNactenehoTextu());
            casCoolDown.setSec(nactiHodnotuOdepisJiZNactenehoTextu());
            pocetCyklu = nactiHodnotuOdepisJiZNactenehoTextu();
            pocetTabat = Integer.parseInt(String.valueOf(nactenyTextZeSouboru));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private int nactiHodnotuOdepisJiZNactenehoTextu() {
        int vracenaHodnota = 1;
        try { //na google play to házelo někomu chybu v této metodě - StringIndexOutOfBoundsException, mně na mých přístrojích jsem nic nenašel
            //tak jsem to raději za try-catchoval
            if (nactenyTextZeSouboru.charAt(1) == ';') {
                vracenaHodnota = Integer.parseInt(nactenyTextZeSouboru.substring(0, 1));
                nactenyTextZeSouboru.delete(0, 2);
            } else {
                vracenaHodnota = Integer.parseInt(nactenyTextZeSouboru.substring(0, 2));
                nactenyTextZeSouboru.delete(0, 3);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vracenaHodnota;
    }


    private void vytvorDialogCasu(final Dialog dialog, final String nadpis, final TextView textView) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //pokud budu chtít nadpisy, tak tohle oddělat a tyhle zakomentované řádky pod tím naopak odkomentovat (od comment 1 do comment2

        dialog.setContentView(R.layout.set_time_layout_dialog);
//comment1       dialog.setTitle(nadpis);
//        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            //ve verzi LOLLIPOP my tohle házelo chybu, nevyřešil jsem ji, tak tohle...:-(
//            int divierId = dialog.getActivity().getApplicationContext().getResources()
//                    .getIdentifier("android:id/titleDivider", null, null);
//            View divider = dialog.findViewById(divierId);
//            divider.setVisibility(View.GONE);
//comment2        }


        // divider.setBackgroundColor(getResources().getColor(R.color.colorPisma));
        //tady     divider.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPisma));
        if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPripravy))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasPripravy);

        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCviceni))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasCviceni);

        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPauzy))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzy));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasPauzy);
        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPauzyMeziTabatami))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzymezitabatami));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasPauzyMeziTabatami);
        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCoolDown))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.backgroundcolorcascooldown));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasCoolDown);
        }


        final NumberPicker pocitacMin = (NumberPicker) dialog.findViewById(R.id.npMin);
        setDividerColor(pocitacMin);
        pocitacMin.setMinValue(0);
        pocitacMin.setMaxValue(99);
        final NumberPicker pocitacSec = (NumberPicker) dialog.findViewById(R.id.npSec);
        setDividerColor(pocitacSec);
        pocitacSec.setMinValue(0);
        pocitacSec.setMaxValue(59);
        //nastavím v dialogu hodnotu, která je načtená ze souboru
        if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPripravy))) {
            pocitacMin.setValue(casPripravy.getMin());
            pocitacSec.setValue(casPripravy.getSec());
        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCviceni))) {
            pocitacMin.setValue(casCviceni.getMin());
            pocitacSec.setValue(casCviceni.getSec());
        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPauzy))) {
            pocitacMin.setValue(casPauzy.getMin());
            pocitacSec.setValue(casPauzy.getSec());
        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPauzyMeziTabatami))) {
            pocitacMin.setValue(casMeziTabatami.getMin());
            pocitacSec.setValue(casMeziTabatami.getSec());
        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCoolDown))) {
            pocitacMin.setValue(casCoolDown.getMin());
            pocitacSec.setValue(casCoolDown.getSec());
        }

        Button uloz = (Button) dialog.findViewById(R.id.buttonUloz);
        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPripravy))) {
                    casPripravy = new MyTime(0, pocitacMin.getValue(), pocitacSec.getValue());
                    textView.setText(vratStringCasUpraveny(casPripravy));
                    //             pocitacMin.setValue(casPripravy.getMin());
                    //             pocitacSec.setValue(casPripravy.getSec());
                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCviceni))) {
                    casCviceni = new MyTime(0, pocitacMin.getValue(), pocitacSec.getValue());
                    textView.setText(vratStringCasUpraveny(casCviceni));
                    //             pocitacMin.setValue(casCviceni.getMin());
                    //             pocitacSec.setValue(casCviceni.getSec());
                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPauzy))) {
                    casPauzy = new MyTime(0, pocitacMin.getValue(), pocitacSec.getValue());
                    textView.setText(vratStringCasUpraveny(casPauzy));
                    //           pocitacMin.setValue(casPauzy.getMin());
                    //           pocitacSec.setValue(casPauzy.getSec());
                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPauzyMeziTabatami))) {
                    casMeziTabatami = new MyTime(0, pocitacMin.getValue(), pocitacSec.getValue());
                    textView.setText(vratStringCasUpraveny(casMeziTabatami));
                    //            pocitacMin.setValue(casMeziTabatami.getMin());
                    //           pocitacSec.setValue(casMeziTabatami.getSec());
                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCoolDown))) {
                    casCoolDown = new MyTime(0, pocitacMin.getValue(), pocitacSec.getValue());
                    textView.setText(vratStringCasUpraveny(casCoolDown));
                    //                pocitacMin.setValue(casCoolDown.getMin());
                    //                pocitacSec.setValue(casCoolDown.getSec());
                }


                dialog.dismiss(); //nebo cancel() ???
                spoctiCelkovyCasAZobraz();
            }
        });
    }



    private void vytvorDialogOpakovani(final Dialog dialog, final String nadpis, final TextView textView) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //pokud budu chtít nadpisy, tak tohle oddělat a tyhle zakomentované řádky pod tím naopak odkomentovat (od comment 1 do comment2

        dialog.setContentView(R.layout.set_numbers);


        //comment1      dialog.setTitle(nadpis);
        //     if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        //         int divierId = dialog.getActivity().getApplicationContext().getResources()
        //                 .getIdentifier("android:id/titleDivider", null, null);
        //         View divider = dialog.findViewById(divierId);
        //         divider.setVisibility(View.GONE);
        //comment2     }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dialog.getWindow().setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.backgroundcolorpocettabat));//  (R.drawable.background);
        } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorPocetTabat);
        final NumberPicker pocitacDesitky = (NumberPicker) dialog.findViewById(R.id.npDesitky);
        setDividerColor(pocitacDesitky);
        pocitacDesitky.setMinValue(0);
        pocitacDesitky.setMaxValue(9);
        final NumberPicker pocitacJednotky = (NumberPicker) dialog.findViewById(R.id.npJednotky);
        setDividerColor(pocitacJednotky);
        pocitacJednotky.setMinValue(0);
        pocitacJednotky.setMaxValue(9);
        //
        if (nadpis.equals(getResources().getString(R.string.nadpisNastavPocetCyklu))) {
            pocitacDesitky.setValue((pocetCyklu - pocetCyklu%10)/10);
            pocitacJednotky.setValue(pocetCyklu%10);

        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavPocetTabat))) {
            pocitacDesitky.setValue((pocetTabat - pocetTabat%10)/10);
            pocitacJednotky.setValue(pocetTabat%10);
        }
        Button uloz = (Button) dialog.findViewById(R.id.buttonUlozPocet);
        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pocet = String.valueOf(String.valueOf(pocitacDesitky.getValue()) + String.valueOf(pocitacJednotky.getValue()));
                textView.setText(pocet);
                if (nadpis.equals(getResources().getString(R.string.nadpisNastavPocetCyklu))) {
                    pocetCyklu = (pocitacDesitky.getValue()) * 10 + pocitacJednotky.getValue();
                    if(pocetCyklu==0) {
                        pocetCyklu = 1;
                        textView.setText("01");
                    }

                    //            pocitacDesitky.setValue((pocetCyklu - pocetCyklu%10)/10);
                    //            pocitacJednotky.setValue(pocetCyklu%10);

                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavPocetTabat))) {
                    pocetTabat = (pocitacDesitky.getValue()) * 10 + pocitacJednotky.getValue();
                    if (pocetTabat==0) {
                        pocetTabat=1;
                        textView.setText("01");
                    }
                    //              pocitacDesitky.setValue((pocetTabat - pocetTabat%10)/10);
                    //              pocitacJednotky.setValue(pocetTabat%10);
                }
                dialog.dismiss(); //nebo cancel() ???
                spoctiCelkovyCasAZobraz();
            }
        });
    }

    public void showTimePickerDialogNastavSetupZvuku() {
        // custom dialog
        Intent nastavZvukTabataActivity = new Intent(getActivity().getApplicationContext(), SetSoundTabataActivity.class);
        nastavZvukTabataActivity.putExtra("zvukstartsetsound", zvukStart);
        Log.i("zvuk1",String.valueOf(zvukStart));
        nastavZvukTabataActivity.putExtra("zvukstopsetsound", zvukStop);
        nastavZvukTabataActivity.putExtra("zvukcelkovykonecsetsound", zvukCelkovyKonec);
        nastavZvukTabataActivity.putExtra("zvukcountdownsetsound", zvukCountdown);

        nastavZvukTabataActivity.putExtra("zvukresttabatassetsound", zvukStopTabatas);
        nastavZvukTabataActivity.putExtra("hlasitostTabata",hlasitost);

        //color
        nastavZvukTabataActivity.putExtra("barvaPripravy", colorDlazdiceCasPripravy); //color
        nastavZvukTabataActivity.putExtra("barvaCviceni", colorDlazdiceCasCviceni); //color
        nastavZvukTabataActivity.putExtra("barvaPauzy", colorDlazdiceCasPauzy); //color
        nastavZvukTabataActivity.putExtra("barvaPauzyMeziTabatami", colorDlazdiceCasPauzyMeziTabatami); //color
        nastavZvukTabataActivity.putExtra("barvaCoolDown", colorDlazdiceCasCoolDown); //color

        ulozDataDoSouboru();
     //   startActivity(nastavZvukTabataActivity);
        activityResultLaunch.launch(nastavZvukTabataActivity);

    }

 //   public void showTimePickerDialogNastavPripravu(View v) {
        // custom dialog
    //    dialogPriprava.show();

 //   }
 private void vytvorClickaciLayouty() {

    showTimePickerDialogNastavPripravuLayout.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialogPriprava.show();
        }
    });

     showTimePickerDialogNastavSetupZvukuLayout.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             showTimePickerDialogNastavSetupZvuku();
         }
     });
     showTimePickerDialogNastavCviceniLayout.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             dialogCviceni.show();
         }
     });
     showTimePickerDialogNastavPauzuLayout.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             dialogPauzy.show();
         }
     });
     showTimePickerDialogNastavPauzuMeziTabatami.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             dialogPauzyMeziTabatami.show();
         }
     });

     showTimePickerDialogNastavCoolDown.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             dialogCoolDown.show();
         }
     });

     zmacknutyStartLayout.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             zmacknutyStart();
         }
     });

     dlazdiceCykly.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             dialogPocetCyklu.show();
         }
     });

     dlazdiceTabaty.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             dialogPoctuTabat.show();
         }
     });

     //colors
     showPickerDialogNastavPripravuColor.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             zobrazNastaveniColoruDlazdice("colorDlazdiceCasPripravy",dlazdiceCasPripravy);
         }
     });

     showPickerDialogNastavCviceniColor.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             zobrazNastaveniColoruDlazdice("colorDlazdiceCasCviceni",dlazdiceCasCviceni);
         }
     });

     showPickerDialogNastavPauzuColor.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             zobrazNastaveniColoruDlazdice("colorDlazdiceCasPauzy",dlazdiceCasPauzy);
         }
     });

     showPickerDialogNastavTabatyColor.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             zobrazNastaveniColoruDlazdice("colorDlazdiceTabaty",dlazdiceTabaty);
         }
     });

     showPickerDialogNastavPauzuMeziTabatamiColor.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             zobrazNastaveniColoruDlazdice("colorDlazdiceCasPauzyMeziTabatami",dlazdiceCasPauzyMeziTabatami);
         }
     });

     showPickerDialogNastavCoolDownColor.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             zobrazNastaveniColoruDlazdice("colorDlazdiceCasCoolDown",dlazdiceCasCoolDown);
         }
     });


 }

    public void showPickerDialogNastavCviceniColor(View v) {
        // custom dialog

        //    nastavColorDlazdice(colorDlazdiceCasCviceni, dlazdiceCasCviceni);
        //    nastavColor3();
        zobrazNastaveniColoruDlazdice("colorDlazdiceCasCviceni",dlazdiceCasCviceni);


    }

    public void showPickerDialogNastavPauzuColor(View v) {

        zobrazNastaveniColoruDlazdice("colorDlazdiceCasPauzy",dlazdiceCasPauzy);


    }

    public void showPickerDialogNastavTabatyColor(View v) {
        zobrazNastaveniColoruDlazdice("colorDlazdiceTabaty",dlazdiceTabaty);
    }

 //   public void showPickerDialogNastavPripravuColor(View v) {
 //       zobrazNastaveniColoruDlazdice("colorDlazdiceCasPripravy",dlazdiceCasPripravy);
 //   }

    public void showPickerDialogNastavPauzuMeziTabatamiColor(View v) {
        zobrazNastaveniColoruDlazdice("colorDlazdiceCasPauzyMeziTabatami",dlazdiceCasPauzyMeziTabatami);
    }

    public void showPickerDialogNastavCoolDownColor(View v) {
        zobrazNastaveniColoruDlazdice("colorDlazdiceCasCoolDown",dlazdiceCasCoolDown);
    }

    private void zobrazNastaveniColoruDlazdice(String colorDlazdiceString, LinearLayout dlazdice) { //tady pokračovat, tady to asi udělalo chybu
        final ColorPicker colorPicker = new ColorPicker(getActivity()); //tady nevím, jestli je to OK, původně to bylo MainActivity.this, tady je chyba, vyřešit to
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                Log.i("asdf:", "1");
                nastavColorDlazdice(colorDlazdiceString, color,dlazdice);
                Log.i("asdf:", "2");
                ulozDataColorDoSouboru();

            }

            @Override
            public void onCancel(){
                // put code
            }
        }).setColumns(5)
                .setRoundColorButton(true)
                .setTitle(getResources().getString(R.string.nadpisNastavBarvu))
                .setColors(R.array.default_colors_color_picker)
        ;


        colorPicker.getDialogViewLayout().setBackgroundResource(R.drawable.background);
        //     colorPicker.getDialogViewLayout().setBackgroundDrawableResource(backgroundcolorpicker);

        colorPicker.show();
    }

    private void nastavColorDlazdice(String colorDlazdiceString, int color, LinearLayout dlazdice) {

        //nejdříve se změní vybraná dlaždice
        GradientDrawable shape1 =  new GradientDrawable();
        shape1.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
        shape1.setColor(color);
        dlazdice.setBackground(shape1);

        GradientDrawable shape2 =  new GradientDrawable();
        shape2.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
        shape2.setColor(color);
        //poté se nastaví barva do proměnné a nastaví se i nová barva pro daný dialog
        //musel jsem použít 2 shapy (shape1 a shape2), když jsem měl jen jeden a ten jsem použil
        //viz výše a také do dialogu (viz níže), tak to dělalo neplechu ve zobrazování
        if (colorDlazdiceString.equals("colorDlazdiceCasCviceni")) {
            colorDlazdiceCasCviceni = color;
            dialogCviceni.getWindow().setBackgroundDrawable(shape2);
            nastavColorDlazdiceKola();
        } else if (colorDlazdiceString.equals("colorDlazdiceCasPauzy")) {
            colorDlazdiceCasPauzy = color;
            dialogPauzy.getWindow().setBackgroundDrawable(shape2);
            nastavColorDlazdiceKola();
        }  else if (colorDlazdiceString.equals("colorDlazdiceTabaty")) {
            colorDlazdiceTabaty = color;
            dialogPoctuTabat.getWindow().setBackgroundDrawable(shape2);
        }  else if (colorDlazdiceString.equals("colorDlazdiceCasPripravy")) {
            colorDlazdiceCasPripravy = color;
            dialogPriprava.getWindow().setBackgroundDrawable(shape2);
        }  else if (colorDlazdiceString.equals("colorDlazdiceCasPauzyMeziTabatami")) {
            colorDlazdiceCasPauzyMeziTabatami = color;
            dialogPauzyMeziTabatami.getWindow().setBackgroundDrawable(shape2);
        }  else if (colorDlazdiceString.equals("colorDlazdiceCasCoolDown")) {
            colorDlazdiceCasCoolDown = color;
            dialogCoolDown.getWindow().setBackgroundDrawable(shape2);
        }

    }

    private void nastavColorDlazdiceKola() {
        int[] colors1 = {colorDlazdiceCasCviceni, colorDlazdiceCasPauzy};
        GradientDrawable shadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors1);
        shadow.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
        dlazdiceCykly.setBackground(shadow);

        GradientDrawable shadow2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors1);
        shadow2.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
        dialogPocetCyklu.getWindow().setBackgroundDrawable(shadow2);
    }








    public void zmacknutyStart() {
        // custom dialog
        //        Toast.makeText(getApplicationContext(), "!!!START!!!",
        //             Toast.LENGTH_LONG).show();

        Intent tabataActivity = new Intent(getActivity().getApplicationContext(), TabataActivity.class);
        tabataActivity.putExtra("caspripavy", casPripravy);
        tabataActivity.putExtra("barvaPripravy", colorDlazdiceCasPripravy); //color
        tabataActivity.putExtra("cascviceni", casCviceni);
        tabataActivity.putExtra("barvaCviceni", colorDlazdiceCasCviceni); //color
        Log.i("color dlazdice1:", String.valueOf(colorDlazdiceCasCviceni));
        tabataActivity.putExtra("caspauzy", casPauzy);
        tabataActivity.putExtra("barvaPauzy", colorDlazdiceCasPauzy); //color
        tabataActivity.putExtra("caspauzynezitabatami", casMeziTabatami);
        tabataActivity.putExtra("barvaPauzyMeziTabatami", colorDlazdiceCasPauzyMeziTabatami); //color
        tabataActivity.putExtra("cascooldown", casCoolDown);
        tabataActivity.putExtra("barvaCoolDown", colorDlazdiceCasCoolDown); //color
        tabataActivity.putExtra("casCelkovy", casCelkovy);

        tabataActivity.putExtra("zvukstart", zvukStart);
        tabataActivity.putExtra("zvukstop", zvukStop);
        tabataActivity.putExtra("zvukcelkovykonec", zvukCelkovyKonec);
        tabataActivity.putExtra("zvukcountdown", zvukCountdown);

        tabataActivity.putExtra("zvukstoptabatas", zvukStopTabatas);
        tabataActivity.putExtra("hlasitostTabata",hlasitost);

        if (pocetCyklu == 0) {
            pocetCyklu = 1;
        }
        tabataActivity.putExtra("pocetcyklu", pocetCyklu);
        if (pocetTabat == 0) {
            pocetTabat = 1;
        }

        ulozDataDoSouboru();
        tabataActivity.putExtra("pocettabat", pocetTabat);
        tabataActivity.putExtra("barvaTabaty", colorDlazdiceTabaty); //color nejen dlazdice tabaty, ale všech spodních dlaždic
        startActivity(tabataActivity);

        //pokus
        //      Intent pokus = new Intent(getApplicationContext(), HomeActivity.class);
        //      startActivity(pokus);


    }



    //metoda, která u numperpickeru udělá ty dvě čárečky jinou barvou, než defaultní - jinak to nešlo
    private void setDividerColor(NumberPicker picker) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //pf.set(picker, getResources().getColor(R.color.my_orange));
                    //Log.v(TAG,"here");
                    //    pf.set(picker, getResources().getDrawable(R.color.colorPisma));
                    pf.set(picker, ContextCompat.getDrawable(requireContext(), R.color.colorPisma));

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        //}
    }

    private String vratStringCasUpraveny(MyTime casClass) {
        return vratDeseticisla(casClass.getMin()) + ":" + vratDeseticisla(casClass.getSec());
    }

    private String vratStringCasUpravenySHodinama(MyTime casClass) {
        return getResources().getString(R.string.celkovyCas)+vratHodiny(casClass.getHour()) + vratDeseticisla(casClass.getMin()) + ":" + vratDeseticisla(casClass.getSec());
    }

    private String vratHodiny(int cislo) {
        if (cislo > 9) {
            return String.valueOf(cislo) + ":";

        } else
        {if (cislo == 0) {
            return "";
        } else
            return ("0" + String.valueOf(cislo))+ ":";
        }

    }



    private String vratDeseticisla(int cislo) {
        if (cislo < 10) {
            return ("0" + String.valueOf(cislo));
        } else
            return String.valueOf(cislo);
    }

    //metoda, která zjistí otočení displaye a potom se znovu nastaví celý layout
    //jinak mi to nešlo udělat, aby po otočení displaye se activita znovu nerestartovala a tím pádem by nebyly uloženy hodnoty
    //k tomu ještě je třeba dát do manifestu:
    //android:configChanges="orientation|screenSize" (do activity)
    public void onConfigurationChanged(Configuration newConfig) {


        super.onConfigurationChanged(newConfig);
        frameLayout.removeAllViews();
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_second, null);
        frameLayout.addView(rootView);

        vytvorView();
        udelejLayout();
        nactiColors();
        vytvorClickaciLayouty();

        // Checks the orientation of the screen
        //      if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        //          Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
     //   udelejLayout();
      //  nactiColors();

        //    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        //        Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        //       udelejLayout();
        //    }
    }

    private void udelejLayout() {
   //     setContentView(R.layout.activity_main3);

        String idAplikace = "ca-app-pub-6701702247641250~7047640994";
        MobileAds.initialize(requireContext(), idAplikace);
/*        MobileAds.initialize(getActivity().getApplicationContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });*/
        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //   mAdView.setVisibility(View.GONE);

        //udelejZpravuGDPR();


        showTimePickerDialogNastavPripravuLayout = rootView.findViewById(R.id.showTimePickerDialogNastavPripravu);
        showTimePickerDialogNastavSetupZvukuLayout= rootView.findViewById(R.id.showTimePickerDialogNastavSetupZvuku);
        showTimePickerDialogNastavCviceniLayout= rootView.findViewById(R.id.showTimePickerDialogNastavCviceni);
        showTimePickerDialogNastavPauzuLayout= rootView.findViewById(R.id.showTimePickerDialogNastavPauzu);
        showTimePickerDialogNastavPauzuMeziTabatami = rootView.findViewById(R.id.showTimePickerDialogNastavPauzuMeziTabatami);
        showTimePickerDialogNastavCoolDown = rootView.findViewById(R.id.showTimePickerDialogNastavCoolDown);
        zmacknutyStartLayout= rootView.findViewById(R.id.zmacknutyStart);


        showPickerDialogNastavPripravuColor = rootView.findViewById(R.id.showPickerDialogNastavPripravuColor);
        showPickerDialogNastavCviceniColor = rootView.findViewById(R.id.showPickerDialogNastavCviceniColor);
        showPickerDialogNastavPauzuColor = rootView.findViewById(R.id.showPickerDialogNastavPauzuColor);
        showPickerDialogNastavTabatyColor = rootView.findViewById(R.id.showPickerDialogNastavTabatyColor);
        showPickerDialogNastavPauzuMeziTabatamiColor = rootView.findViewById(R.id.showPickerDialogNastavPauzuMeziTabatamiColor);
        showPickerDialogNastavCoolDownColor = rootView.findViewById(R.id.showPickerDialogNastavCoolDownColor);



        //vytvoření dialogu příprava
        hodnotaPripravyTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaPriprava);
        hodnotaPripravyTextView.setText(vratStringCasUpraveny(casPripravy));
        dialogPriprava = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
        vytvorDialogCasu(dialogPriprava, getResources().getString(R.string.nadpisNastavCasPripravy), hodnotaPripravyTextView);

        dlazdiceCasPripravy = rootView.findViewById(R.id.dlazdiceCasPripravy);
        colorDlazdiceCasPripravy = getResources().getColor(R.color.colorCasPripravy);

        //vytvoření dialogu cvičení
        hodnotaCviceniTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaCviceni);
        hodnotaCviceniTextView.setText(vratStringCasUpraveny(casCviceni));
        dialogCviceni = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
        vytvorDialogCasu(dialogCviceni, getResources().getString(R.string.nadpisNastavCasCviceni), hodnotaCviceniTextView);

        dlazdiceCasCviceni = rootView.findViewById(R.id.dlazdiceCasCviceni);
        colorDlazdiceCasCviceni = getResources().getColor(R.color.colorCasCviceni);

        //vytvoření dialogu pauzy
        hodnotaPauzyTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaPauza);
        hodnotaPauzyTextView.setText(vratStringCasUpraveny(casPauzy));
        dialogPauzy = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
        vytvorDialogCasu(dialogPauzy, getResources().getString(R.string.nadpisNastavCasPauzy), hodnotaPauzyTextView);

        dlazdiceCasPauzy = rootView.findViewById(R.id.dlazdiceCasPauzy);
        colorDlazdiceCasPauzy = getResources().getColor(R.color.colorCasPauzy);

        //vytvoření dialogu pauza mezi tabatami
        hodnotaPauzyMeziTabatamiTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaCasuMeziTabatami);
        hodnotaPauzyMeziTabatamiTextView.setText(vratStringCasUpraveny(casMeziTabatami));
        dialogPauzyMeziTabatami = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
        vytvorDialogCasu(dialogPauzyMeziTabatami, getResources().getString(R.string.nadpisNastavCasPauzyMeziTabatami), hodnotaPauzyMeziTabatamiTextView);

        dlazdiceCasPauzyMeziTabatami = rootView.findViewById(R.id.dlazdiceCasPauzyMeziTabatami);
        colorDlazdiceCasPauzyMeziTabatami = getResources().getColor(R.color.colorCasPauzyMeziTabatami);

        //vytvoření dialogu casu cool down
        hodnotaCasuCoolDownTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaCoolDown);
        hodnotaCasuCoolDownTextView.setText(vratStringCasUpraveny(casCoolDown));
        dialogCoolDown = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
        vytvorDialogCasu(dialogCoolDown, getResources().getString(R.string.nadpisNastavCasCoolDown), hodnotaCasuCoolDownTextView);

        dlazdiceCasCoolDown = rootView.findViewById(R.id.dlazdiceCasCoolDown);
        colorDlazdiceCasCoolDown = getResources().getColor(R.color.colorCasCoolDown);

        //vytvoření dialogu počet tabat
        pocetTabatTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaPocetTabat);
        pocetTabatTextView.setText(String.valueOf(pocetTabat));
        dialogPoctuTabat = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
        vytvorDialogOpakovani(dialogPoctuTabat, getResources().getString(R.string.nadpisNastavPocetTabat), pocetTabatTextView);

        dlazdiceTabaty = rootView.findViewById(R.id.dlazdiceTabaty);
        colorDlazdiceTabaty = getResources().getColor(R.color.colorPocetTabat);

        //vytvoření dialogu počet cyklů
        pocetCykluTextView = (TextView) rootView.findViewById(R.id.textViewPocetCyklu);
        pocetCykluTextView.setText(String.valueOf(pocetCyklu));
//        dialogPocetCyklu = new Dialog(MainActivity.this);
        dialogPocetCyklu = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
        vytvorDialogOpakovani(dialogPocetCyklu, getResources().getString(R.string.nadpisNastavPocetCyklu), pocetCykluTextView);
        dlazdiceCykly = rootView.findViewById(R.id.dlazdiceCykly);

        //vytvoření nápisu celkový čas
        hodnotaCelkovyCasTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaCelkovyCasNadpis);
        spoctiCelkovyCasAZobraz();
        //      hodnotaCelkovyCasTextView.setText(vratStringCasUpravenySHodinama(casCelkovy));
        //nastavení coloru dlazdic po novu:
        nastavColorDlazdiceKola();
    }



    private void spoctiCelkovyCasAZobraz() {
        int pomocnyCasCviceni = casCviceni.getHour()*3600 + casCviceni.getMin()*60 + casCviceni.getSec();
        int pomocnyCasPauzy = casPauzy.getHour()*3600 + casPauzy.getMin()*60 + casPauzy.getSec();
        int pomocnyCasMeziTabatami = casMeziTabatami.getHour()*3600 + casMeziTabatami.getMin()*60 + casMeziTabatami.getSec();
        int pomocnyCelkovyCas = (casPripravy.getHour()*3600 + casPripravy.getMin()*60 + casPripravy.getSec())
                + (pomocnyCasCviceni * pocetCyklu * pocetTabat)
                + (pomocnyCasPauzy * (pocetCyklu-1)*pocetTabat)
                +(pomocnyCasMeziTabatami*(pocetTabat-1));

        int pomocnyCelkovyCasHodiny = pomocnyCelkovyCas/3600;
        int pomocnyCelkovyCasHodinyZbytekPoDeleni = pomocnyCelkovyCas%3600;


        Log.d("AAA celkem sekund:",String.valueOf(pomocnyCelkovyCas));
        Log.d("AAA hodiny:",String.valueOf(pomocnyCelkovyCasHodiny));
        Log.d("AAA zbytek po d hodin:",String.valueOf(pomocnyCelkovyCasHodinyZbytekPoDeleni));
        System.out.print(pomocnyCelkovyCasHodiny);
        System.out.print(pomocnyCelkovyCasHodinyZbytekPoDeleni);

        int pomocnyCelkovyCasMinuty = pomocnyCelkovyCasHodinyZbytekPoDeleni/60;
        int  pomocnyCelkovyCasSekundy =  pomocnyCelkovyCasHodinyZbytekPoDeleni%60;

        Log.d("AAA minuty:",String.valueOf(pomocnyCelkovyCasMinuty));
        Log.d("AAA sekundy:",String.valueOf(pomocnyCelkovyCasSekundy));

        casCelkovy.setHour(pomocnyCelkovyCasHodiny);
        casCelkovy.setMin(pomocnyCelkovyCasMinuty);
        casCelkovy.setSec(pomocnyCelkovyCasSekundy);

        Log.d("AAA hodiny ulozene:",String.valueOf(casCelkovy.getHour()));

        Log.d("AAA minuty ulozene:",String.valueOf(casCelkovy.getMin()));

        Log.d("AAA sekundy ulozene:",String.valueOf(casCelkovy.getSec()));

        System.out.print(pomocnyCelkovyCasMinuty);
        System.out.print(pomocnyCelkovyCasSekundy);

        System.out.print("Pokus");


        hodnotaCelkovyCasTextView.setText(vratStringCasUpravenySHodinama(casCelkovy));


    }


    //souborové funkce
    private void ulozDataDoSouboru() {
        dataSoubor = String.valueOf(casPripravy.getMin()) + ";" + String.valueOf(casPripravy.getSec()) + ";" +
                String.valueOf(casCviceni.getMin()) + ";" + String.valueOf(casCviceni.getSec()) + ";" +
                String.valueOf(casPauzy.getMin()) + ";" + String.valueOf(casPauzy.getSec()) + ";" +
                String.valueOf(casMeziTabatami.getMin()) + ";" + String.valueOf(casMeziTabatami.getSec()) + ";" +
                String.valueOf(casCoolDown.getMin()) + ";" + String.valueOf(casCoolDown.getSec()) + ";" +
                String.valueOf(pocetCyklu) + ";" + String.valueOf(pocetTabat);

        soubor.writeToFileInternal(dataSoubor, getActivity().getApplicationContext());

    }

    private void ulozDataZvukDoSouboru() {
        dataSouborTime = String.valueOf(zvukStart) + ";" + String.valueOf(zvukStop) + ";" +
                String.valueOf(zvukCelkovyKonec) + ";" + String.valueOf(zvukCountdown)+ ";" +
                String.valueOf(zvukStopTabatas) + ";"
                + String.valueOf(hlasitost)+";"
        ;

        Log.i("zvuky:", dataSouborTime);

        soubor.writeToFileInternalZvuk(dataSouborTime, getActivity().getApplicationContext());

    }

    private void ulozDataColorDoSouboru() {
        Log.i("asdf:", "3");
        dataSouborColor = String.valueOf(colorDlazdiceCasPripravy) + ";" + String.valueOf(colorDlazdiceCasCviceni) + ";" +
                String.valueOf(colorDlazdiceCasPauzy) + ";" + String.valueOf(colorDlazdiceTabaty)+ ";" +
                String.valueOf(colorDlazdiceCasPauzyMeziTabatami) + ";" + String.valueOf(colorDlazdiceCasCoolDown) + ";"
        ;
        Log.i("asdf:", "4");
        soubor.writeToFileInternalColor(dataSouborColor, getActivity().getApplicationContext());

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();


    }

}