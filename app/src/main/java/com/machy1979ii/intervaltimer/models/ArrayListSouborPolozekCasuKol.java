package com.machy1979ii.intervaltimer.models;

import java.util.ArrayList;

public class ArrayListSouborPolozekCasuKol {
//tahle metoda je proto, aby se arraylist souboru položek časů kol mohl uložit a pak načíst pomocí Gson (knihovna pro Jsobn)
    //protože při načetení gson.fromJson...je potřeba jako druhý atribut dát název objektlu.class viz PraceSeSouboremCustom.vratZJsonuVsechnyPolozkyCasyKol
    ArrayList<SouborPolozekCasuKola> SouborPolozekCasuKola;

    public ArrayListSouborPolozekCasuKol(ArrayList<com.machy1979ii.intervaltimer.models.SouborPolozekCasuKola> souborPolozekCasuKola) {
        SouborPolozekCasuKola = souborPolozekCasuKola;
    }

    public ArrayList<com.machy1979ii.intervaltimer.models.SouborPolozekCasuKola> getSouborPolozekCasuKola() {
        return SouborPolozekCasuKola;
    }

    public void setSouborPolozekCasuKola(ArrayList<com.machy1979ii.intervaltimer.models.SouborPolozekCasuKola> souborPolozekCasuKola) {
        SouborPolozekCasuKola = souborPolozekCasuKola;
    }
}
