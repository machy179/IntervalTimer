package com.machy1979ii.intervaltimer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.machy1979ii.intervaltimer.funkce.PraceSeZvukem;
import com.machy1979ii.intervaltimer.ui.main.FirstFragment;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

public class SetSoundClassicActivity extends AppCompatActivity {

    private Dialog dialogCviceniNastaveniZvuku;
    private TextView vybranyZvukCviceniTextView;
    private int cisloNastavenehoZvukuCviceni = 1;

    private Dialog dialogRestNastaveniZvuku;
    private TextView vybranyZvukRestTextView;
    private int cisloNastavehoZvukuRest = 1;

  //  private Dialog dialogKonecCviceniNastaveniZvuku;
  //  private TextView vybranyZvukKonecCvicenitTextView;
  //  private int cisloNastavenehoZvukuKonec = 1;

    private Dialog dialogCountdownNastaveniZvuku;
    private TextView vybranyZvukCountdownTextView;
    private int cisloNastavenehoZvukuCountdown = 1;

    private Dialog dialogPulkaCviceniNastaveniZvuku;
    private TextView vybranyZvukPulkaCviceniTextView;
    private int cisloNastavenehoZvukuPulkaCviceni = 50; //když bude tohle číslo, tak žádný zvuk není vybrán

    private Dialog dialogPredKoncemKolaNastaveniZvuku;
    private TextView vybranyZvukPredKoncemKolaTextView;
    private int cisloNastavenehoZvukuPredKoncemKola = 50; //když bude tohle číslo, tak žádný zvuk není vybrán

    private Dialog dialogPredKoncemKolaNastaveniCasu;
    private TextView casPredKoncemKolaTextView;
    private int hodnotaCasuZvukuPredKoncemKola = 10;

    private MediaPlayer mediaPlayer = null;

    public static Activity self_intent_setsound;
    ;

    private String nazevZvukuPolozka = "sound";

    private boolean mediaPlayerReleasedANenastavenNovy = false; //když uživatel dal dvakrát za sebou přehrát zvuk X, tedy žádný, tak aplikace havarovala
    //při druhém přehrátí a spadlo to na mediaPlayer.release(); - předpokládám, že to bylo kvůli tomu, že to nemělo co uvolnit, protože při
    //prvním přehrátí X se release už udělalo, ale nenačetl se do mediaPlayeru nový zvuk. tak to neříš přes tuto proměnnou a když to při přehrátí
    //neuloží do mediaPlayeru žádný zvuk, tak tahle proměnná bude TRUE a při dalším načtení ne neudělá mediaPlayer.release()


    //color
    private int colorDlazdiceCasCviceni; //color
    private int colorDlazdiceCasPauzy; //color

    private LinearLayout dlazdiceCasCviceni; //color
    private LinearLayout dlazdiceCasPauzy; //color

    //slider
    private DiscreteSeekBar slider;
    private int hlasitost = 100;
    private int hlasitostPomocna = 100; //do téhle proměnné budu ukládat hlasitost průběžně a když se otočí display, tak se do proměnné "hlasitost" skopíruje
    //tahle hlasitost. Je to proto, neboť když se otočil display, tak se do proměnné "hlasitost" nastavilo 100
    private int maxHlasitost = 100; //pokud tady změním, tak změnit také tohle  app:dsb_max="20" v layoutu activity_set_sound

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_sound_classic);





        self_intent_setsound = this;

        statusBarcolor();

        cisloNastavenehoZvukuCviceni = getIntent().getIntExtra("zvukstartsetsoundClassic", 1);
        cisloNastavehoZvukuRest = getIntent().getIntExtra("zvukstopsetsoundClassic", 1);
  //      cisloNastavenehoZvukuKonec = getIntent().getIntExtra("zvukcelkovykonecsetsound", 1);
        cisloNastavenehoZvukuCountdown = getIntent().getIntExtra("zvukcountdownsetsoundClassic", 1);
        cisloNastavenehoZvukuPulkaCviceni = getIntent().getIntExtra("zvukpulkakolaClassic", 33);
        cisloNastavenehoZvukuPredKoncemKola = getIntent().getIntExtra("zvukpupredkoncemkolaClassic", 33);
        hodnotaCasuZvukuPredKoncemKola = getIntent().getIntExtra("caszvukupupredkoncemkolaClassic", 20);

        hlasitost = getIntent().getIntExtra("hlasitost",hlasitost);

        colorDlazdiceCasCviceni = getIntent().getExtras().getInt("barvaCviceni"); //color
        colorDlazdiceCasPauzy = getIntent().getExtras().getInt("barvaPauzy"); //color
        udelejLayout();


    }

    private void udelejLayout() {

        setContentView(R.layout.activity_set_sound_classic);

        // reklama Google
        String idAplikace = "ca-app-pub-6701702247641250~7047640994";
     //   MobileAds.initialize(getApplicationContext(), idAplikace);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //načte to název zvuku podle jazykové mutace zařízení
        nazevZvukuPolozka = getResources().getString(R.string.napisZvuk);

        //nastavení dialogů nastavení časů
        dialogCviceniNastaveniZvuku = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
        vybranyZvukCviceniTextView = (TextView) findViewById(R.id.textViewHodnotaZvukCviceni);
        vybranyZvukCviceniTextView.setText(PraceSeZvukem.vratNazvyZvukuStartStop(nazevZvukuPolozka)[cisloNastavenehoZvukuCviceni - 1]);
        //musel jsem to takhle obejít, to nahoře mi házelo chybu
        // int cisloPomocne=cisloNastavenehoZvukuCviceni+1;
        // String aString[] = PraceSeZvukem.vratNazvyZvukuCountdown();
        // vybranyZvukCviceniTextView.setText(aString[cisloPomocne]);
        vytvorDialogNastaveniZvukuCasu(dialogCviceniNastaveniZvuku, getResources().getString(R.string.nadpisNastavZvukCviceni), vybranyZvukCviceniTextView);
        dlazdiceCasCviceni = findViewById(R.id.dlazdiceCasCviceniZvuk);
        nastavColorDlazdice("colorDlazdiceCasCviceni", colorDlazdiceCasCviceni, dlazdiceCasCviceni);

        dialogRestNastaveniZvuku = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
        vybranyZvukRestTextView = (TextView) findViewById(R.id.textViewHodnotaZvukPauzy);
        vybranyZvukRestTextView.setText(PraceSeZvukem.vratNazvyZvukuStartStop(nazevZvukuPolozka)[cisloNastavehoZvukuRest - 1]);
        vytvorDialogNastaveniZvukuCasu(dialogRestNastaveniZvuku, getResources().getString(R.string.nadpisNastavZvukRest), vybranyZvukRestTextView);
        dlazdiceCasPauzy = findViewById(R.id.dlazdiceCasPauzyZvuk);
        nastavColorDlazdice("colorDlazdiceCasPauzy", colorDlazdiceCasPauzy, dlazdiceCasPauzy);

        //      dialogKonecCviceniNastaveniZvuku = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
        //      vybranyZvukKonecCvicenitTextView = (TextView) findViewById(R.id.textViewZvukKonecCviceni);
        //      vybranyZvukKonecCvicenitTextView.setText(PraceSeZvukem.vratNazvyZvukuKonec(nazevZvukuPolozka)[cisloNastavenehoZvukuKonec - 1]);
        //      vytvorDialogNastaveniZvukuCasu(dialogKonecCviceniNastaveniZvuku, getResources().getString(R.string.nadpisNastavZvukKonecCviceni), vybranyZvukKonecCvicenitTextView);

        dialogCountdownNastaveniZvuku = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
        vybranyZvukCountdownTextView = (TextView) findViewById(R.id.textViewZvukCountdown);
        vybranyZvukCountdownTextView.setText(PraceSeZvukem.vratNazvyZvukuCountdown(nazevZvukuPolozka)[cisloNastavenehoZvukuCountdown - 1]);
        vytvorDialogNastaveniZvukuCasu(dialogCountdownNastaveniZvuku, getResources().getString(R.string.nadpisNastavZvukCountdown), vybranyZvukCountdownTextView);

        dialogPulkaCviceniNastaveniZvuku = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
        vybranyZvukPulkaCviceniTextView = (TextView) findViewById(R.id.textViewZvukPulkaCviceni);
        vybranyZvukPulkaCviceniTextView.setText(PraceSeZvukem.vratNazvyZvukuPulkaCviceni(nazevZvukuPolozka)[cisloNastavenehoZvukuPulkaCviceni - 1]);
        vytvorDialogNastaveniZvukuCasu(dialogPulkaCviceniNastaveniZvuku, getResources().getString(R.string.nadpisNastavZvukPulkaCviceni), vybranyZvukPulkaCviceniTextView);

        dialogPredKoncemKolaNastaveniZvuku = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
        vybranyZvukPredKoncemKolaTextView = (TextView) findViewById(R.id.textViewZvukPredKoncemKola);
        vybranyZvukPredKoncemKolaTextView.setText(PraceSeZvukem.vratNazvyZvukuPulkaCviceni(nazevZvukuPolozka)[cisloNastavenehoZvukuPredKoncemKola - 1]);
        vytvorDialogNastaveniZvukuCasu(dialogPredKoncemKolaNastaveniZvuku, getResources().getString(R.string.nadpisNastavZvukDoKonceKola), vybranyZvukPredKoncemKolaTextView);

        dialogPredKoncemKolaNastaveniCasu = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
        casPredKoncemKolaTextView = (TextView) findViewById(R.id.textViewHodnotaCasuZvukuPredKoncemKola);
        casPredKoncemKolaTextView.setText(String.valueOf(hodnotaCasuZvukuPredKoncemKola));
        vytvorDialogCasu(dialogPredKoncemKolaNastaveniCasu, getResources().getString(R.string.nadpisNastavSekundyPredProZvukPrekKoncemKola));

        //nastavení slideru zesílení/
        slider = findViewById(R.id.slider);
        slider.setProgress(hlasitost);
        slider.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                hlasitost = value;
                hlasitostPomocna = value;
                //        zobrazHlasitost();
                return value;
            }

        });
    }

    //metoda, která zjistí otočení displaye a potom se znovu nastaví celý layout a zůstanou nastavené všechny údaje - nebudou se načítat znovu

    //jinak mi to nešlo udělat, aby po otočení displaye se activita znovu nerestartovala a tím pádem by nebyly uloženy hodnoty
    //taky to řeší multiscreen
    //k tomu ještě je třeba dát do manifestu:
    //android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation" (do activity v manifestu)
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        Toast.makeText(this, "onConfigurationChanged", Toast.LENGTH_SHORT).show();
        hlasitost = hlasitostPomocna;
        udelejLayout();



    }

    public void showTimePickerDialogNastavZvukCviceni(View v) {
        // custom dialog
        dialogCviceniNastaveniZvuku.show();
    }

    public void showTimePickerDialogNastavCasPauzy(View v) {
        // custom dialog
        dialogRestNastaveniZvuku.show();
    }

    public void showTimePickerDialogNastavZvukPulkaCviceni(View v) {
        // custom dialog
        dialogPulkaCviceniNastaveniZvuku.show();
    }

    public void showTimePickerDialogNastavZvukPrekKoncemKola(View v) {
        // custom dialog
        dialogPredKoncemKolaNastaveniZvuku.show();
    }

    public void showTimePickerDialogNastavCasZvukuDoKonceKola(View v) {
        // custom dialog
        dialogPredKoncemKolaNastaveniCasu.show();
    }


 //   public void showTimePickerDialogNastavZvukKonecCviceni(View v) {
        // custom dialog
 //       dialogKonecCviceniNastaveniZvuku.show();
 //   }

    public void showTimePickerDialogNastavZvukCountdown(View v) {
        // custom dialog
        dialogCountdownNastaveniZvuku.show();
    }


    public void zmacknutyOk(View v) {
        // timhle udělám to, že na tu původní MainActivitu se nedostanu tlačítkem zpět
        //protože kdyby někdo furt dával zpět, tak by šel např. na tu první MainActivitu, ale v té první není ještě uložený zvuk
        try {
            //u Android 4.4 to někomu házelo chybu, tak to obalím try-catch
            FirstFragment.self_intent.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }


        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);

        mainActivity.putExtra("zobrazIntent",0);

        mainActivity.putExtra("zvukstartClassic", cisloNastavenehoZvukuCviceni);

        mainActivity.putExtra("zvukstopClassic", cisloNastavehoZvukuRest);

 //       mainActivity.putExtra("zvukcelkovykonec", cisloNastavenehoZvukuKonec);
        mainActivity.putExtra("zvukcelkovykonecClassic", cisloNastavehoZvukuRest);

        mainActivity.putExtra("zvukcountdownClassic", cisloNastavenehoZvukuCountdown);

        mainActivity.putExtra("zvukpulkakolaClassic", cisloNastavenehoZvukuPulkaCviceni);

        mainActivity.putExtra("zvukpredkoncemkolaClassic", cisloNastavenehoZvukuPredKoncemKola);

        mainActivity.putExtra("caszvukupupredkoncemkolaClassic", hodnotaCasuZvukuPredKoncemKola);
        mainActivity.putExtra("hlasitost",hlasitost);


       // startActivity(mainActivity);
        setResult(Activity.RESULT_OK, mainActivity);
        finish();

    }

    private void vytvorDialogNastaveniZvukuCasu(final Dialog dialog, final String nadpis, final TextView textView) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //pokud budu chtít nadpisy, tak tohle oddělat a tyhle zakomentované řádky pod tím naopak odkomentovat (od comment 1 do comment2

        dialog.setContentView(R.layout.set_sound_layout_dialog);

//comment1        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
        //ve verzi LOLLIPOP my tohle házelo chybu, nevyřešil jsem ji, tak tohle...:-(
//            int divierId = dialog.getContext().getResources()
//                    .getIdentifier("android:id/titleDivider", null, null);
//            View divider = dialog.findViewById(divierId);
//            divider.setVisibility(View.GONE);
//        }


        final NumberPicker pocitacVybranehoZvuku = (NumberPicker) dialog.findViewById(R.id.npVybranyZvuk);
        setDividerColor(pocitacVybranehoZvuku);
        pocitacVybranehoZvuku.setMinValue(1);


        if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPripravy))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasPripravy);

        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukCviceni))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasCviceni);

            pocitacVybranehoZvuku.setMaxValue(PraceSeZvukem.vratPocetZvukuStartStop());
            pocitacVybranehoZvuku.setDisplayedValues(PraceSeZvukem.vratNazvyZvukuStartStop(nazevZvukuPolozka));
            pocitacVybranehoZvuku.setValue(cisloNastavenehoZvukuCviceni);
            //     pocitacVybranehoZvuku.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            //         @Override
            //          public void onValueChange(NumberPicker picker, int oldVal, int newVal){
            //Process the changes here
            //              PraceSeZvukem.spustZvukStartStop(getApplicationContext(),newVal);
            //          }
            //      });

        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukRest))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzy));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasPauzy);

            pocitacVybranehoZvuku.setMaxValue(PraceSeZvukem.vratPocetZvukuStartStop());
            pocitacVybranehoZvuku.setDisplayedValues(PraceSeZvukem.vratNazvyZvukuStartStop(nazevZvukuPolozka));

            pocitacVybranehoZvuku.setValue(cisloNastavehoZvukuRest);

        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukPulkaCviceni))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorzvukpulkykola));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorZvukPulkyKola);

            pocitacVybranehoZvuku.setMaxValue(PraceSeZvukem.vratPocetZvukuPulkaCviceni());
            pocitacVybranehoZvuku.setDisplayedValues(PraceSeZvukem.vratNazvyZvukuPulkaCviceni(nazevZvukuPolozka));

            pocitacVybranehoZvuku.setValue(cisloNastavenehoZvukuPulkaCviceni);

        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukDoKonceKola))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorzvukpulkykola));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorZvukPulkyKola);

            pocitacVybranehoZvuku.setMaxValue(PraceSeZvukem.vratPocetZvukuPulkaCviceni());
            pocitacVybranehoZvuku.setDisplayedValues(PraceSeZvukem.vratNazvyZvukuPulkaCviceni(nazevZvukuPolozka));

            pocitacVybranehoZvuku.setValue(cisloNastavenehoZvukuPredKoncemKola);


        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukRestTabatas))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzymezitabatami));//  (R.drawable.background);
            } else
                dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasPauzyMeziTabatami);

            pocitacVybranehoZvuku.setMaxValue(PraceSeZvukem.vratPocetZvukuStartStop());
            pocitacVybranehoZvuku.setDisplayedValues(PraceSeZvukem.vratNazvyZvukuStartStop(nazevZvukuPolozka));


        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukKonecCviceni))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascooldown));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasCoolDown);

            pocitacVybranehoZvuku.setMaxValue(PraceSeZvukem.vratPocetZvukuKonec());
            pocitacVybranehoZvuku.setDisplayedValues(PraceSeZvukem.vratNazvyZvukuKonec(nazevZvukuPolozka));



  //          pocitacVybranehoZvuku.setValue(cisloNastavenehoZvukuKonec);
        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukCountdown))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasPripravy);

            pocitacVybranehoZvuku.setMaxValue(PraceSeZvukem.vratPocetZvukuCountDown());
            pocitacVybranehoZvuku.setDisplayedValues(PraceSeZvukem.vratNazvyZvukuCountdown(nazevZvukuPolozka));
            //       pocitacVybranehoZvuku.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            //           @Override
            //           public void onValueChange(NumberPicker picker, int oldVal, int newVal){
            //Process the changes here
            //              PraceSeZvukem.spustZvukKonec(getApplicationContext(),newVal);
            //          }
            //      });

            pocitacVybranehoZvuku.setValue(cisloNastavenehoZvukuCountdown);
        }


        Button uloz = (Button) dialog.findViewById(R.id.buttonUloz);
        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // jinak než jak to mám, to nešlo...
                int pomocny = pocitacVybranehoZvuku.getValue();
                textView.setText(pocitacVybranehoZvuku.getDisplayedValues()[pomocny - 1]);


                if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukCviceni))) {
                    cisloNastavenehoZvukuCviceni = pomocny;

                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukRest))) {
                    cisloNastavehoZvukuRest = pomocny;
                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukKonecCviceni))) {
  //                  cisloNastavenehoZvukuKonec = pomocny;
                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukPulkaCviceni))) {
                    cisloNastavenehoZvukuPulkaCviceni = pomocny;
                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukDoKonceKola))) {
                    cisloNastavenehoZvukuPredKoncemKola = pomocny;
                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukCountdown))) {
                    cisloNastavenehoZvukuCountdown = pomocny;
                }


                dialog.dismiss(); //nebo cancel() ???
            }
        });

        Button prehraj = (Button) dialog.findViewById(R.id.buttonPrehraj);
        prehraj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pomocny2 = pocitacVybranehoZvuku.getValue();
                boolean zvukNastaven = false;
                //   Toast.makeText(getApplicationContext(),Integer.toString(pomocny2), Toast.LENGTH_LONG).show();
                //   PraceSeZvukem.spustZvukStartStop(getApplicationContext(),pomocny2);
                if ((mediaPlayer != null) && !mediaPlayerReleasedANenastavenNovy) { //aaa
               //     try {
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayerReleasedANenastavenNovy = false;
               //     } catch (Exception e) {

              //      }
              //      mediaPlayer.reset();
               //     mediaPlayer.release();
                }



                if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukCviceni)) || nadpis.equals(getResources().getString(R.string.nadpisNastavZvukRest)) || nadpis.equals(getResources().getString(R.string.nadpisNastavZvukRestTabatas))) {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(pomocny2));
                 //   mediaPlayer.start();
                    zvukNastaven = true;


                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukPulkaCviceni)) && pomocny2 != PraceSeZvukem.vratPocetZvukuPulkaCviceni()) {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(pomocny2));
                 //   mediaPlayer.start();
                    zvukNastaven = true;

                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukDoKonceKola)) && pomocny2 != PraceSeZvukem.vratPocetZvukuPulkaCviceni()) {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(pomocny2));
                 //   mediaPlayer.start();
                    zvukNastaven = true;

                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukKonecCviceni))) {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukKonecPodlePozice(pomocny2));
               //     mediaPlayer.start();
                    zvukNastaven = true;

                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavZvukCountdown))) {
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(pomocny2));
                 //   mediaPlayer.start();
                    zvukNastaven = true;
                } else {
                    mediaPlayerReleasedANenastavenNovy = true;
                }

                if (zvukNastaven) {
                    Log.i("pomocny2: ","******************");
                    final float volume = (float) (1 - (Math.log(maxHlasitost - hlasitost) / Math.log(maxHlasitost)));
                    mediaPlayer.setVolume(volume,volume);
                    mediaPlayer.start();
                    zvukNastaven = false;
                }
                Log.i("pomocny2: ",String.valueOf(pomocny2));


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
                    pf.set(picker, ContextCompat.getDrawable(getApplicationContext(), R.color.colorPisma));

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

    private void vytvorDialogCasu(final Dialog dialog, final String nadpis) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //pokud budu chtít nadpisy, tak tohle oddělat a tyhle zakomentované řádky pod tím naopak odkomentovat (od comment 1 do comment2

        dialog.setContentView(R.layout.set_time_jen_sec_dialog);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorzvukpulkykola));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorZvukPulkyKola);





        final NumberPicker pocitacSec = (NumberPicker) dialog.findViewById(R.id.npSec);
        setDividerColor(pocitacSec);
        pocitacSec.setMinValue(0);
        pocitacSec.setMaxValue(60);
        //nastavím v dialogu hodnotu, která je načtená ze souboru

            pocitacSec.setValue(hodnotaCasuZvukuPredKoncemKola);


        Button uloz = (Button) dialog.findViewById(R.id.buttonUloz);
        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nadpis.equals(getResources().getString(R.string.nadpisNastavSekundyPredProZvukPrekKoncemKola))) {
                   hodnotaCasuZvukuPredKoncemKola = pocitacSec.getValue();


                    //zapíše nový čas cvičení
                    if (hodnotaCasuZvukuPredKoncemKola < 10) {
                        casPredKoncemKolaTextView.setText("0" + String.valueOf(hodnotaCasuZvukuPredKoncemKola));
                    } else {
                        casPredKoncemKolaTextView.setText(String.valueOf(hodnotaCasuZvukuPredKoncemKola));
                        }
                }


                dialog.dismiss(); //nebo cancel() ???
                //    prepoctiZbyvajiciCasZmenaCasuKola( rozdilMeziCasemCviceniANovouHodnotou);
            }
        });
    }

    private void nastavColorDlazdice(String colorDlazdiceString, int color, LinearLayout dlazdice) {

        //nejdříve se změní vybraná dlaždice
        GradientDrawable shape1 =  new GradientDrawable();
        shape1.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
        shape1.setColor(color);
        dlazdice.setBackground(shape1);

        GradientDrawable shape2 =  new GradientDrawable();
        shape2.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
        shape2.setColor(color);
        //poté se nastaví barva do proměnné a nastaví se i nová barva pro daný dialog
        //musel jsem použít 2 shapy (shape1 a shape2), když jsem měl jen jeden a ten jsem použil
        //viz výše a také do dialogu (viz níže), tak to dělalo neplechu ve zobrazování
        if (colorDlazdiceString.equals("colorDlazdiceCasCviceni")) {
            dialogCviceniNastaveniZvuku.getWindow().setBackgroundDrawable(shape2);
        } else if (colorDlazdiceString.equals("colorDlazdiceCasPauzy")) {
            dialogRestNastaveniZvuku.getWindow().setBackgroundDrawable(shape2);

        }

    }

    public void statusBarcolor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(SetSoundClassicActivity.this,R.color.colorStatusBarColor));
        }
    }

    @Override
    public void onBackPressed(){
        //protože v manifestu mám nastaveno android:noHistory="true", tak při zpětném buttonu by se apka ukončila a neskočila by do mainu,
        //proto jsem tady zpětný button přetypoval, aby při jeho stisknutí to skočilo do mainu
        super.onBackPressed();
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.putExtra("zobrazIntent",0);
     //   startActivity(mainActivity);
        setResult(Activity.RESULT_CANCELED, mainActivity);
        finish();
        //Toast.makeText(this, "zmáčknutý back", Toast.LENGTH_SHORT).show();
    //    finish();
    }

}
