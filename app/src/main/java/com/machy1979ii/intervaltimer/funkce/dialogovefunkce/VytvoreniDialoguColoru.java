package com.machy1979ii.intervaltimer.funkce.dialogovefunkce;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.widget.LinearLayout;

import com.machy1979ii.intervaltimer.R;
import com.machy1979ii.intervaltimer.funkce.PraceSeSouboremCustom;
import com.machy1979ii.intervaltimer.models.PolozkaCasuKola;
import com.machy1979ii.intervaltimer.models.SouborPolozekCasuKola;

import java.util.ArrayList;

import petrov.kristiyan.colorpicker.ColorPicker;

public class VytvoreniDialoguColoru {

    private Context context;
    private ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol;
    private PraceSeSouboremCustom praceSeSouborem = new PraceSeSouboremCustom();

    public VytvoreniDialoguColoru(Context context) {
        this.context = context;
    }

    public void zobrazNastaveniColoruDlazdice(LinearLayout dlazdice, PolozkaCasuKola polozkaCasu, Dialog predanyDialog, ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol) { //tady pokračovat, tady to asi udělalo chybu
        this.vsechnyPolozkyCasyKol = vsechnyPolozkyCasyKol;

        final ColorPicker colorPicker = new ColorPicker((Activity) context); //tady nevím, jestli je to OK, původně to bylo MainActivity.this, tady je chyba, vyřešit to
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                Log.i("asdf:", "1");
                polozkaCasu.setColorDlazdice(color);
                nastavColorDlazdice(color,dlazdice, predanyDialog);
                Log.i("asdf:", "2");
                //        ulozDataColorDoSouboru(); tady to odkomentovat a dodělat to

            }

            @Override
            public void onCancel(){
                // put code
            }
        }).setColumns(5)
                .setRoundColorButton(true)
                .setTitle(context.getResources().getString(R.string.nadpisNastavBarvu))
                .setColors(R.array.default_colors_color_picker)
        ;


        colorPicker.getDialogViewLayout().setBackgroundResource(R.drawable.background);
        //     colorPicker.getDialogViewLayout().setBackgroundDrawableResource(backgroundcolorpicker);

        colorPicker.show();
    }

    public void nastavColorDlazdice(int color, LinearLayout dlazdice, Dialog predanyDialog) {

        //nejdříve se změní vybraná dlaždice
        GradientDrawable shape1 =  new GradientDrawable();
        shape1.setCornerRadius(context.getResources().getDimension(R.dimen.kulate_rohy));
        shape1.setColor(color);
        dlazdice.setBackground(shape1);

        GradientDrawable shape2 =  new GradientDrawable();
        shape2.setCornerRadius(context.getResources().getDimension(R.dimen.kulate_rohy));
        shape2.setColor(color);
        //poté se nastaví barva do proměnné a nastaví se i nová barva pro daný dialog
        //musel jsem použít 2 shapy (shape1 a shape2), když jsem měl jen jeden a ten jsem použil
        //viz výše a také do dialogu (viz níže), tak to dělalo neplechu ve zobrazování


            predanyDialog.getWindow().setBackgroundDrawable(shape2);
            if (!(vsechnyPolozkyCasyKol == null)) {
                Log.d("nacteniSouboru", "---------------uloženo------------");
                praceSeSouborem.writeToFileInternal(vsechnyPolozkyCasyKol, context);
            } else Log.d("nacteniSouboru", "---------------NENENEuloženo------------");



    }

    public void nastavColorDlazdiceBezDialogu(int color, LinearLayout dlazdice) {

        //tahle metoda obarví jen dlaždici
        GradientDrawable shape1 =  new GradientDrawable();
        shape1.setCornerRadius(context.getResources().getDimension(R.dimen.kulate_rohy));
        shape1.setColor(color);
        dlazdice.setBackground(shape1);



    }



}
