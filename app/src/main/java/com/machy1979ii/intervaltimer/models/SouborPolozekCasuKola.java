package com.machy1979ii.intervaltimer.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class SouborPolozekCasuKola implements Parcelable, Cloneable {
    private ArrayList<PolozkaCasuKola> polozkyCasyKol = new ArrayList<PolozkaCasuKola>();
    private int pocetCyklu = 1;

    public SouborPolozekCasuKola() {
    }


    public void setPocetCyklu(int pocetCyklu) {
        this.pocetCyklu = pocetCyklu;
    }

    public ArrayList<PolozkaCasuKola> getPolozkyCasyKol() {
        return polozkyCasyKol;
    }

    public int getPocetCyklu() {
        return pocetCyklu;
    }

    public void vlozPolozkuCasKola(PolozkaCasuKola polozkaCasuKola) {
        polozkyCasyKol.add(polozkaCasuKola);
    }

    public void vymazPolozkuCasKola(PolozkaCasuKola polozkaCasuKola) {
        polozkyCasyKol.remove(polozkaCasuKola);
    }

    public PolozkaCasuKola vratPolozkuCasKola(int index) {
        return polozkyCasyKol.get(index);
    }

    public int vratPocetPolozek() {
        return polozkyCasyKol.size();
    }

    public void vratCelkoveMinuty() { //to je jen pro testování, pak to vymažu
        for (PolozkaCasuKola polozka : polozkyCasyKol) {
            Log.d("polozka",String.valueOf(polozka.getTime().getMin()));

        }
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        //aby tato třída šla klonovat - kopírovat instantci, ale udělt novou instanci, která neodkazuje na tu původní
        //takto lze ale klonovat jen třídu se základními datovými typy, tady tedy pocet cyklů, aby bylo možné klonovat i složitější datové typy
        //jako například polozkyCasyKol, je třeba udělat k tomuto atributu clone() také plus to ošetřit přímo ve třídě PolozkaCasuKola (a tam obdobně pořešit MyTime)
        Object clonedMyClass;
        ArrayList<PolozkaCasuKola> klonovanePolozkyCasyKol = new ArrayList<PolozkaCasuKola>();

        try {
            clonedMyClass = (SouborPolozekCasuKola) super.clone();
        } catch (CloneNotSupportedException e) {
            clonedMyClass = new SouborPolozekCasuKola();
        }

        for (PolozkaCasuKola polozka : polozkyCasyKol) {
         //   Log.d("polozka",String.valueOf(polozka.getTime().getMin()));
            klonovanePolozkyCasyKol.add((PolozkaCasuKola) polozka.clone());
        }
       ((SouborPolozekCasuKola) clonedMyClass).polozkyCasyKol = klonovanePolozkyCasyKol;
       // ((SouborPolozekCasuKola) clonedMyClass).pocetCyklu = 3;
        return clonedMyClass;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(polozkyCasyKol);
        dest.writeInt(pocetCyklu);
    }

    protected SouborPolozekCasuKola(Parcel in) {
        in.readTypedList(polozkyCasyKol, PolozkaCasuKola.CREATOR);
        pocetCyklu = in.readInt();
    }

    public static final Creator<SouborPolozekCasuKola> CREATOR = new Creator<SouborPolozekCasuKola>() {
        @Override
        public SouborPolozekCasuKola createFromParcel(Parcel in) {
            return new SouborPolozekCasuKola(in);
        }

        @Override
        public SouborPolozekCasuKola[] newArray(int size) {
            return new SouborPolozekCasuKola[size];
        }
    };

}
