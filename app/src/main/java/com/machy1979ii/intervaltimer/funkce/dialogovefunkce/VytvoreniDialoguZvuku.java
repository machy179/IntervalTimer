package com.machy1979ii.intervaltimer.funkce.dialogovefunkce;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.machy1979ii.intervaltimer.R;
import com.machy1979ii.intervaltimer.funkce.PraceSeSouboremCustom;
import com.machy1979ii.intervaltimer.funkce.PraceSeZvukem;
import com.machy1979ii.intervaltimer.models.PolozkaCasuKola;
import com.machy1979ii.intervaltimer.models.SouborPolozekCasuKola;

import java.util.ArrayList;

public class VytvoreniDialoguZvuku {

    private Context context;
    private String nazevZvukuPolozka = "sound";
    private MediaPlayer mediaPlayer = null;
    private ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol;
    private PraceSeSouboremCustom praceSeSouborem = new PraceSeSouboremCustom();

    public VytvoreniDialoguZvuku(Context context) {
        this.context = context;
    }

    public void vytvorDialogNastaveniZvukuCasu(final Dialog dialog, final PolozkaCasuKola polozkaCasuKola, ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol) {
        this.vsechnyPolozkyCasyKol = vsechnyPolozkyCasyKol;
        nazevZvukuPolozka = context.getResources().getString(R.string.napisZvuk);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //pokud budu chtít nadpisy, tak tohle oddělat a tyhle zakomentované řádky pod tím naopak odkomentovat (od comment 1 do comment2

        dialog.setContentView(R.layout.set_sound_layout_dialog);

        final NumberPicker pocitacVybranehoZvuku = (NumberPicker) dialog.findViewById(R.id.npVybranyZvuk);
        setDividerColor(pocitacVybranehoZvuku);
        pocitacVybranehoZvuku.setMinValue(1);

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
        } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasCviceni);

        GradientDrawable shape2 =  new GradientDrawable();
        shape2.setCornerRadius(context.getResources().getDimension(R.dimen.kulate_rohy));
        shape2.setColor(polozkaCasuKola.getColorDlazdice());
        dialog.getWindow().setBackgroundDrawable(shape2);

        pocitacVybranehoZvuku.setMaxValue(PraceSeZvukem.vratPocetZvukuPulkaCviceni());
        pocitacVybranehoZvuku.setDisplayedValues(PraceSeZvukem.vratNazvyZvukuPulkaCviceni(nazevZvukuPolozka));

        pocitacVybranehoZvuku.setValue(polozkaCasuKola.getZvuk());




        Button uloz = (Button) dialog.findViewById(R.id.buttonUloz);
        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // jinak než jak to mám, to nešlo...
                int pomocny = pocitacVybranehoZvuku.getValue();
         //       textView.setText(pocitacVybranehoZvuku.getDisplayedValues()[pomocny - 1]);

                    polozkaCasuKola.setZvuk(pomocny);
                praceSeSouborem.writeToFileInternal(vsechnyPolozkyCasyKol, context);
                dialog.dismiss(); //nebo cancel() ???
            }
        });

        Button prehraj = (Button) dialog.findViewById(R.id.buttonPrehraj);
        prehraj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pomocny2 = pocitacVybranehoZvuku.getValue();
                mediaPlayer = MediaPlayer.create(context, PraceSeZvukem.vratZvukStartStopPodlePozice(pomocny2));

           //     final float volume = (float) (1 - (Math.log(maxHlasitost - hlasitost) / Math.log(maxHlasitost)));
           //     mediaPlayer.setVolume(volume,volume);
                mediaPlayer.start();

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

}
