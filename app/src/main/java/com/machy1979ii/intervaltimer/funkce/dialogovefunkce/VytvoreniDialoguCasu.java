package com.machy1979ii.intervaltimer.funkce.dialogovefunkce;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.machy1979ii.intervaltimer.R;
import com.machy1979ii.intervaltimer.funkce.VypocetCelkovehoCasuAZobrezni;
import com.machy1979ii.intervaltimer.models.MyTime;
import com.machy1979ii.intervaltimer.models.PolozkaCasuKola;
import com.machy1979ii.intervaltimer.models.SouborPolozekCasuKola;

import java.util.ArrayList;

public class VytvoreniDialoguCasu {

    private Context context;
    private ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol;
    private TextView hodnotaCelkovyCasTextView;
    private MyTime casCelkovy = new MyTime(0, 60, 0);
    private VypocetCelkovehoCasuAZobrezni vypocetCelkovehoCasuAZobrezni;

    public VytvoreniDialoguCasu(Context context, final TextView hodnotaCelkovyCasTextView) {
        this.context = context;

        this.hodnotaCelkovyCasTextView = hodnotaCelkovyCasTextView;
        vypocetCelkovehoCasuAZobrezni = new VypocetCelkovehoCasuAZobrezni(context, hodnotaCelkovyCasTextView);
    }

    public void vytvorDialogCasu(final Dialog dialog, final String nadpis, final TextView textViewCelkovyCas, final MyTime casPredany, final PolozkaCasuKola polozkaCasuKola, ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol) {

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //pokud budu chtít nadpisy, tak tohle oddělat a tyhle zakomentované řádky pod tím naopak odkomentovat (od comment 1 do comment2

        if (nadpis.equals(context.getResources().getString(R.string.nadpisNastavCasPripravy))) {
            dialog.setContentView(R.layout.set_time_layout_dialog);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasPripravy);
        } else {
            dialog.setContentView(R.layout.set_time_layout_dialog_custom);
            SwitchCompat switchPreskocPosledniKolo = (SwitchCompat) dialog.findViewById(R.id.switchPreskocPosledniKolo);
            switchPreskocPosledniKolo.setChecked(polozkaCasuKola.isPosledniKoloPreskocitCas());
            switchPreskocPosledniKolo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        polozkaCasuKola.setPosledniKoloPreskocitCas(true);

                    } else {
                        polozkaCasuKola.setPosledniKoloPreskocitCas(false);
                    }
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasCviceni);
        }


        final NumberPicker pocitacMin = (NumberPicker) dialog.findViewById(R.id.npMin);
        setDividerColor(pocitacMin);
        pocitacMin.setMinValue(0);
        pocitacMin.setMaxValue(99);
        final NumberPicker pocitacSec = (NumberPicker) dialog.findViewById(R.id.npSec);
        setDividerColor(pocitacSec);
        pocitacSec.setMinValue(0);
        pocitacSec.setMaxValue(59);

            pocitacMin.setValue(casPredany.getMin());
            pocitacSec.setValue(casPredany.getSec());



        Button uloz = (Button) dialog.findViewById(R.id.buttonUloz);
        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    casPredany.setMin(pocitacMin.getValue());
                    casPredany.setSec(pocitacSec.getValue());
                    //     casPredany = new MyTime(0, pocitacMin.getValue(), pocitacSec.getValue());
                textViewCelkovyCas.setText(vratStringCasUpraveny(casPredany));
                hodnotaCelkovyCasTextView.setText(vratStringCasUpraveny(casPredany));

                dialog.dismiss(); //nebo cancel() ???
                //spoctiCelkovyCasAZobraz();
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
