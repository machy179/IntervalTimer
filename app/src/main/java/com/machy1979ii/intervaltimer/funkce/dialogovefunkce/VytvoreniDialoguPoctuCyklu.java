package com.machy1979ii.intervaltimer.funkce.dialogovefunkce;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.machy1979ii.intervaltimer.R;
import com.machy1979ii.intervaltimer.funkce.VypocetCelkovehoCasuAZobrezni;
import com.machy1979ii.intervaltimer.models.MyTime;
import com.machy1979ii.intervaltimer.models.SouborPolozekCasuKola;

import java.util.ArrayList;

public class VytvoreniDialoguPoctuCyklu {

    private Context context;
    private TextView hodnotaCelkovyCasTextView;
    private ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol;
    private VypocetCelkovehoCasuAZobrezni vypocetCelkovehoCasuAZobrezni;

    public VytvoreniDialoguPoctuCyklu(Context context, TextView hodnotaCelkovyCasTextView) {
        this.context = context;
        this.hodnotaCelkovyCasTextView = hodnotaCelkovyCasTextView;
        vypocetCelkovehoCasuAZobrezni = new VypocetCelkovehoCasuAZobrezni(context, hodnotaCelkovyCasTextView);

    }

    public void vytvorDialogOpakovani(final Dialog dialog, final TextView textView, final SouborPolozekCasuKola souborPolozekCasuKola, ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //pokud budu chtít nadpisy, tak tohle oddělat a tyhle zakomentované řádky pod tím naopak odkomentovat (od comment 1 do comment2
        dialog.setContentView(R.layout.set_numbers);
        this.vsechnyPolozkyCasyKol = vsechnyPolozkyCasyKol;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.backgroundpocetcykludialog));//  (R.drawable.background);
        } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorButtonsDlazdiceCasu);

        final NumberPicker pocitacDesitky = (NumberPicker) dialog.findViewById(R.id.npDesitky);
        setDividerColor(pocitacDesitky);
        pocitacDesitky.setMinValue(0);
        pocitacDesitky.setMaxValue(9);
        final NumberPicker pocitacJednotky = (NumberPicker) dialog.findViewById(R.id.npJednotky);
        setDividerColor(pocitacJednotky);
        pocitacJednotky.setMinValue(0);
        pocitacJednotky.setMaxValue(9);
        //
        int pocetCyklu = souborPolozekCasuKola.getPocetCyklu();;
            pocitacDesitky.setValue((pocetCyklu - pocetCyklu%10)/10);
            pocitacJednotky.setValue(pocetCyklu%10);
        Button uloz = (Button) dialog.findViewById(R.id.buttonUlozPocet);
        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String pocet = String.valueOf(String.valueOf(pocitacDesitky.getValue()) + String.valueOf(pocitacJednotky.getValue()));
                textView.setText(pocet);

                    souborPolozekCasuKola.setPocetCyklu((pocitacDesitky.getValue()) * 10 + pocitacJednotky.getValue());
                    if(souborPolozekCasuKola.getPocetCyklu()==0) {
                        souborPolozekCasuKola.setPocetCyklu(1);
                        textView.setText("01");
                    }

                    //            pocitacDesitky.setValue((pocetCyklu - pocetCyklu%10)/10);
                    //            pocitacJednotky.setValue(pocetCyklu%10);


                dialog.dismiss(); //nebo cancel() ???
                vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);
            }
        });
    }
    private void setDividerColor(NumberPicker picker) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //pf.set(picker, getResources().getColor(R.color.my_orange));
                    //Log.v(TAG,"here");
                    //    pf.set(picker, getResources().getDrawable(R.color.colorPisma));
                    pf.set(picker, ContextCompat.getDrawable(context, R.color.colorPisma));

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


    private String vratDeseticisla(int cislo) {
        if (cislo < 10) {
            return ("0" + String.valueOf(cislo));
        } else
            return String.valueOf(cislo);
    }

}
