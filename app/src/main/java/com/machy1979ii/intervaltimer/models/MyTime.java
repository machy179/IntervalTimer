package com.machy1979ii.intervaltimer.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Simonka Patlokova on 12. 5. 2017.
 */

//tuto třídu jsem udělal za použití "parcelable mackeru" na http://www.parcelabler.com
    //dal jsem tam kód třídy (bez parcelablových nutných implemetnací) a ono mi to vyhodilo tohle
    //parcelable jsem použil, abych mezi aktivitami mohl posílat třídy
    //jde to buď Parcelable, Serializable nebo Evetn Bus (ten je pro větší třídy nejlepší prý)
    //Serializable je normální Java, Parcelable je spešl pro Android, ale je třeba tam dát do té
    //tříd kupa kódu navíc, proto jsem použil generátor viz. výše

public class MyTime implements Parcelable, Cloneable {
    private int sec;
    private int min;
    private int hour;

    public MyTime() {
    }

    public MyTime(int hour, int min, int sec) {
        this.sec = sec;
        this.min = min;
        this.hour = hour;
    }

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    protected MyTime(Parcel in) {
        sec = in.readInt();
        min = in.readInt();
        hour = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sec);
        dest.writeInt(min);
        dest.writeInt(hour);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object clonedMyClass;
        try {
            clonedMyClass = (MyTime) super.clone();
        } catch (CloneNotSupportedException e) {
            clonedMyClass = new MyTime();
        }

            return clonedMyClass;



    }

    @SuppressWarnings("unused")
    public static final Creator<MyTime> CREATOR = new Creator<MyTime>() {
        @Override
        public MyTime createFromParcel(Parcel in) {
            return new MyTime(in);
        }

        @Override
        public MyTime[] newArray(int size) {
            return new MyTime[size];
        }
    };
}
