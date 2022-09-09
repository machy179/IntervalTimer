package com.machy1979ii.intervaltimer.funkce;

import android.util.Log;

import com.machy1979ii.intervaltimer.models.PolozkaCasuKola;
import com.machy1979ii.intervaltimer.models.SouborPolozekCasuKola;

import java.util.ArrayList;

public class PrevodVsechPolozekCasyKolToArrayListPolozkyCasu {

    private PrevodVsechPolozekCasyKolToArrayListPolozkyCasu() {
    }

    public static ArrayList<PolozkaCasuKola> vratArrayPolozekCasyKol(ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol) {
        ArrayList<PolozkaCasuKola> polozkyCasyKol = new ArrayList<PolozkaCasuKola>();

        for(SouborPolozekCasuKola soubor : vsechnyPolozkyCasyKol) {
            for(int i = 0; i < soubor.getPocetCyklu(); i++) {
                for(PolozkaCasuKola polozka : soubor.getPolozkyCasyKol()) {
                    if(!(i==(soubor.getPocetCyklu()-1) && polozka.isPosledniKoloPreskocitCas())) {
                    //vloží to do ArrayListu jen pokud to nebude poslední kolo a nebude mít ta položka času nastaven příznak přeskočit poslední kolo
                   //     polozka.setPoradiVCyklu(String.valueOf(i+1));
                     //   Log.i("pocitac",polozka.getPoradiVCyklu());
                    //    polozkyCasyKol.add(polozka);

                        PolozkaCasuKola pol = new PolozkaCasuKola(polozka.getTime(), polozka.getColorDlazdice(), polozka.getZvuk(), polozka.isPosledniKoloPreskocitCas(), polozka.getNazevCasu());
                        pol.setPoradiVCyklu(i+1);
                        polozkyCasyKol.add(pol);

                    }
                }
          }
        }

        for(PolozkaCasuKola pol : polozkyCasyKol) {
            Log.i("pocitac2",String.valueOf(pol.getPoradiVCyklu()));
        }

        return polozkyCasyKol;
    }



}
