package com.machy1979ii.intervaltimer.funkce;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.machy1979ii.intervaltimer.R;
import com.machy1979ii.intervaltimer.models.MyTime;
import com.machy1979ii.intervaltimer.models.PolozkaCasuKola;
import com.machy1979ii.intervaltimer.models.SouborPolozekCasuKola;

import java.util.ArrayList;

public class VypocetCelkovehoCasuAZobrezni {

    private Context context;
    private ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol;
    private TextView hodnotaCelkovyCasTextView;
    private MyTime casCelkovy = new MyTime(0, 60, 0);
    private PraceSeSouboremCustom praceSeSouborem = new PraceSeSouboremCustom();

    public VypocetCelkovehoCasuAZobrezni(Context context, TextView hodnotaCelkovyCasTextView) {
        this.context = context;;
        this.hodnotaCelkovyCasTextView = hodnotaCelkovyCasTextView;

    }

    public VypocetCelkovehoCasuAZobrezni(Context context, TextView hodnotaCelkovyCasTextView, MyTime casCelkovy) {
        this.context = context;;
        this.hodnotaCelkovyCasTextView = hodnotaCelkovyCasTextView;
        this.casCelkovy = casCelkovy;

    }


    public void spoctiCelkovyCasAZobraz(ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol) {
        if (!(vsechnyPolozkyCasyKol==null)) {
            this.vsechnyPolozkyCasyKol = vsechnyPolozkyCasyKol;
        }
        //this.vsechnyPolozkyCasyKol = vsechnyPolozkyCasyKol;
        int pomocnyCelkovyCas=0;
        int pocetKolVSouboru=1;
        boolean prvniPolozkaCasu = true;
        for (SouborPolozekCasuKola soubor : vsechnyPolozkyCasyKol) {
            if(!prvniPolozkaCasu) { //první položku času nezapočítá, protože to je čas přípravy
            pocetKolVSouboru = soubor.getPocetCyklu();
            for (PolozkaCasuKola polozka : soubor.getPolozkyCasyKol()) {
                for (int i = 0; i < pocetKolVSouboru; ++ i) {
                    if (!(i==(pocetKolVSouboru-1) && polozka.isPosledniKoloPreskocitCas())) { //nezapočítá ani při posledním kole ty položky času, u kterých je nastaven atrybut přeskočení posledního kola

                        pomocnyCelkovyCas = pomocnyCelkovyCas + polozka.getTime().getHour()*360+
                                polozka.getTime().getMin()*60+
                                polozka.getTime().getSec();
                    }


                }
            }
            }
            prvniPolozkaCasu = false;
        }


        int pomocnyCelkovyCasHodiny = pomocnyCelkovyCas/3600;
        int pomocnyCelkovyCasHodinyZbytekPoDeleni = pomocnyCelkovyCas%3600;


        Log.d("AAA celkem sekund:",String.valueOf(pomocnyCelkovyCas));
        Log.d("AAA hodiny:",String.valueOf(pomocnyCelkovyCasHodiny));
        Log.d("AAA zbytek po d hodin:",String.valueOf(pomocnyCelkovyCasHodinyZbytekPoDeleni));


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

        Log.d("AAA ----","-----------------------------------");

        hodnotaCelkovyCasTextView.setText(vratStringCasUpravenySHodinama(casCelkovy));

        praceSeSouborem.writeToFileInternal(vsechnyPolozkyCasyKol, context);


    }
    private String vratStringCasUpraveny(MyTime casClass) {
        return vratDeseticisla(casClass.getMin()) + ":" + vratDeseticisla(casClass.getSec());
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

    private String vratStringCasUpravenySHodinama(MyTime casClass) {
        return context.getResources().getString(R.string.celkovyCas)+vratHodiny(casClass.getHour()) + vratDeseticisla(casClass.getMin()) + ":" + vratDeseticisla(casClass.getSec());
    }

    private String vratDeseticisla(int cislo) {
        if (cislo < 10) {
            return ("0" + String.valueOf(cislo));
        } else
            return String.valueOf(cislo);
    }

}
