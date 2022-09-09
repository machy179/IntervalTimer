package com.machy1979ii.intervaltimer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class PolozkaCasuKola implements Parcelable, Cloneable {

    private MyTime time;
    private int colorDlazdice;
    private int zvuk;
    private boolean posledniKoloPreskocitCas = false;
    private String nazevCasu = "Routine";
    private int poradiVCyklu = 1;

    public int getPoradiVCyklu() {
        return poradiVCyklu;
    }

    public void setPoradiVCyklu(int poradiVCyklu) {
        this.poradiVCyklu = poradiVCyklu;
    }

    public MyTime getTime() {
        return time;
    }

    public PolozkaCasuKola(MyTime time, int colorDlazdice, int zvuk, boolean posledniKoloPreskocitCas, String nazevCasu) {
        this.time = time;
        this.colorDlazdice = colorDlazdice;
        this.zvuk = zvuk;
        this.posledniKoloPreskocitCas = posledniKoloPreskocitCas;
        this.nazevCasu = nazevCasu;
    }

    public PolozkaCasuKola() {
    }


    public void setTime(MyTime time) {
        this.time = time;
    }

    public int getColorDlazdice() {
        return colorDlazdice;
    }

    public void setColorDlazdice(int colorDlazdice) {
        this.colorDlazdice = colorDlazdice;
    }

    public int getZvuk() {
        return zvuk;
    }

    public void setZvuk(int zvuk) {
        this.zvuk = zvuk;
    }

    public boolean isPosledniKoloPreskocitCas() {
        return posledniKoloPreskocitCas;
    }

    public void setPosledniKoloPreskocitCas(boolean posledniKoloPreskocitCas) {
        this.posledniKoloPreskocitCas = posledniKoloPreskocitCas;
    }

    public String getNazevCasu() {
        return nazevCasu;
    }

    public void setNazevCasu(String nazevCasu) {
        this.nazevCasu = nazevCasu;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object clonedMyClass = null;
        try {
            clonedMyClass = (PolozkaCasuKola) super.clone();
        } catch (CloneNotSupportedException e) {
            clonedMyClass = new PolozkaCasuKola();
        }

        ((PolozkaCasuKola) clonedMyClass).time = (MyTime) this.time.clone();
        return clonedMyClass;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(time, flags);
        dest.writeInt(colorDlazdice);
        dest.writeInt(zvuk);
        dest.writeByte((byte) (posledniKoloPreskocitCas ? 1 : 0)); //if posledniKoloPreskocitCas == true, byte == 1
        dest.writeString(nazevCasu);


    }

    protected PolozkaCasuKola(Parcel in) {
        time = in.readParcelable(MyTime.class.getClassLoader());
        colorDlazdice = in.readInt();
        zvuk = in.readInt();
        posledniKoloPreskocitCas = in.readByte() != 0; //myBoolean == true if byte != 0
        nazevCasu = in.readString();
    }

    public static final Creator<PolozkaCasuKola> CREATOR = new Creator<PolozkaCasuKola>() {
        @Override
        public PolozkaCasuKola createFromParcel(Parcel in) {
            return new PolozkaCasuKola(in);
        }

        @Override
        public PolozkaCasuKola[] newArray(int size) {
            return new PolozkaCasuKola[size];
        }
    };
}
