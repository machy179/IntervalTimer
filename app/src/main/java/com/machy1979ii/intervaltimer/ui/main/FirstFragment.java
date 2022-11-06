package com.machy1979ii.intervaltimer.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.machy1979ii.intervaltimer.ClassicActivity;
import com.machy1979ii.intervaltimer.R;
import com.machy1979ii.intervaltimer.SetSoundClassicActivity;
import com.machy1979ii.intervaltimer.funkce.PraceSeSouborem;
import com.machy1979ii.intervaltimer.funkce.PraceSeSouboremTabata;
import com.machy1979ii.intervaltimer.models.MyTime;

import java.io.IOException;

import petrov.kristiyan.colorpicker.ColorPicker;

import static android.app.Activity.RESULT_OK;

public class FirstFragment extends Fragment {

    //polozka celkový čas
    private MyTime casCelkovy = new MyTime(0, 60, 0);
    //    private Dialog dialogPriprava;
    private TextView hodnotaCelkovyCasTextView;

    //polozka příprava
    private MyTime casPripravy = new MyTime(0, 0, 20);
    private Dialog dialogPriprava;
    private TextView hodnotaPripravyTextView;

    private LinearLayout dlazdiceCasPripravy; //color
    private int colorDlazdiceCasPripravy; //color

    //polozka cvičení
    private MyTime casCviceni = new MyTime(0, 2, 0);
    private Dialog dialogCviceni;
    private TextView hodnotaCviceniTextView;

    private LinearLayout dlazdiceCasCviceni; //color
    private int colorDlazdiceCasCviceni; //color

    //polozka pauza
    private MyTime casPauzy = new MyTime(0, 1, 0);
    private Dialog dialogPauzy;
    private TextView hodnotaPauzyTextView;

    private LinearLayout dlazdiceCasPauzy; //color
    private int colorDlazdiceCasPauzy; //color




    //polozka počet cyklů
    private int pocetCyklu = 8;
    private Dialog dialogPocetCyklu;
    private TextView pocetCykluTextView;
    private LinearLayout dlazdiceCykly; //kola

    private int colorDlazdicePocetCyklu;


    //uložení a načtení dat ze souboru
    private PraceSeSouborem soubor;
    private String dataSoubor;
    private StringBuilder nactenyTextZeSouboru;
    private boolean posledniHodnotaNacteni = false;

    //nastavení zvuků
    private int zvukStart = 1;
    private int zvukStop = 1;
    private int zvukCelkovyKonec = 1;
    private int zvukCountdown= 1;
    private int zvukPulkaKola = 33; //33 je když není zvuk nastaven, to znamená, že zvuk v půlce cvičení není žádný
    private int zvukPredKoncemKola = 33; //33 je když není zvuk nastaven, to znamená, že zvuk není žádný
    private int casZvukuPredKoncemKola = 20;

    private String dataSouborTime;
    private StringBuilder nactenyTextZeSouboruTime;
    private int hlasitost = 100;

    //nastavení color ale jen ze souboru, ostatní data jsou viz výše přímo u svých times
    private String dataSouborColor;
    private StringBuilder nactenyTextZeSouboruColor;

    // timhle udělám to, že načtu tuto instanci MainActivituy nedostanu se sem tlačítkem zpět
    //protože kdyby někdo furt dával zpět, tak by šel např. na tu první MainActivitu, ale v té první není ještě uložený zvuk
    //druhá část tohoto je je v SetSoundAdctivity, kdy tuto původní MainActivitu zničí
    public static Activity self_intent;;

    private LinearLayout showTimePickerDialogNastavSetupZvukuLayout;
    private LinearLayout showTimePickerDialogNastavPripravuLayout;
    private LinearLayout showTimePickerDialogNastavCviceniLayout;
    private LinearLayout showTimePickerDialogNastavPauzuLayout;
    private LinearLayout showTimePickerDialogNastavPocetCykluLayout;
    private LinearLayout zmacknutyStartLayout;


    private LinearLayout showPickerDialogNastavPripravuColor;
    private LinearLayout showPickerDialogNastavCviceniColor;
    private LinearLayout showPickerDialogNastavPauzuColor;
    private LinearLayout showTimePickerDialogNastavPauzuMeziTabatami;
    private LinearLayout showPickerDialogNastavPauzuMeziTabatamiColor;
    private LinearLayout showTimePickerDialogNastavCoolDown;
    private LinearLayout showPickerDialogNastavCoolDownColor;

    private LinearLayout showPickerDialogNastavTabatyColor;

    private View rootView;

    private FrameLayout frameLayout;

    private ActivityResultLauncher<Intent> activityResultLaunch;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        frameLayout = new FrameLayout(getActivity());
        LayoutInflater inflater2 = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater2.inflate(R.layout.fragment_first, null);
        frameLayout.addView(rootView);

        //       rootView = inflater.inflate(R.layout.fragment_first, container, false);

        activityResultLaunch = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent intent = result.getData();


                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.i("getResult","OK");
                            nactiZvukyZActivitySetSound(intent);
                        } else {
                            Log.i("getResult","CANCELED");
                        }
                    }
                });


        vytvorView();
        udelejLayout(rootView);
        nactiColors();
        vytvorClickaciLayouty();


        return frameLayout;
      //  return rootView;

    }

    private void nactiZvukyZActivitySetSound(Intent intent) {
        zvukStart = intent.getIntExtra("zvukstartClassic", 0);
        zvukStop = intent.getIntExtra("zvukstopClassic", 0);
        zvukCelkovyKonec = intent.getIntExtra("zvukcelkovykonecClassic", 0);
        zvukCountdown = intent.getIntExtra("zvukcountdownClassic", 0);
        zvukPulkaKola = intent.getIntExtra("zvukpulkakolaClassic", 0);
        zvukPredKoncemKola = intent.getIntExtra("zvukpredkoncemkolaClassic", 0);
        casZvukuPredKoncemKola = intent.getIntExtra("caszvukupupredkoncemkolaClassic", 20);
        hlasitost = intent.getIntExtra("hlasitost", 100);

        ulozDataZvukDoSouboru();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);

      //  vytvorView();
    }

    private void vytvorClickaciLayouty() {
        showTimePickerDialogNastavPripravuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPriprava.show();
            }
        });

        showTimePickerDialogNastavSetupZvukuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialogNastavSetupZvuku();
            }
        });
        showTimePickerDialogNastavCviceniLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCviceni.show();
            }
        });
        showTimePickerDialogNastavPauzuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPauzy.show();
            }
        });

        dlazdiceCykly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPocetCyklu.show();
            }
        });

        zmacknutyStartLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zmacknutyStart();
            }
        });

        //colors
        showPickerDialogNastavPripravuColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zobrazNastaveniColoruDlazdice("colorDlazdiceCasPripravy",dlazdiceCasPripravy);
            }
        });

        showPickerDialogNastavCviceniColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zobrazNastaveniColoruDlazdice("colorDlazdiceCasCviceni",dlazdiceCasCviceni);
            }
        });

        showPickerDialogNastavPauzuColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zobrazNastaveniColoruDlazdice("colorDlazdiceCasPauzy",dlazdiceCasPauzy);
            }
        });

    }
    public void showPickerDialogNastavCviceniColor(View v) {
        // custom dialog

        //    nastavColorDlazdice(colorDlazdiceCasCviceni, dlazdiceCasCviceni);
        //    nastavColor3();
        zobrazNastaveniColoruDlazdice("colorDlazdiceCasCviceni",dlazdiceCasCviceni);


    }

    public void showPickerDialogNastavPauzuColor(View v) {

        zobrazNastaveniColoruDlazdice("colorDlazdiceCasPauzy",dlazdiceCasPauzy);


    }

    public void vytvorView(){
        nactiHodnoty();
        nactiZvuky();
    }

    private void nactiZvuky() {

       //pokud se spustí aplikace, tak je potřba níže uvedené proměnné načíst ze souboru
        //a raději než se ze souboru načtou, tak jim dám předem hodnotu 1, kdyby se něco s načtením ze souboru podělalo
        //nebo kdyby to bylo první načtení, kdy ještě neexistuje soubor "datatime.txt"

            zvukStart = 1;
            zvukStop = 1;
            zvukCelkovyKonec = 1;
            zvukCountdown =1;
            zvukPulkaKola = 33; //33 znamená, že zvuk půlky kola není
            zvukPredKoncemKola = 33; //33 znamená, že zvuk není

            //načtení hodnot pro zvuky ze souboru
            soubor = new PraceSeSouborem();
            String nactenyZvuk = "";

            try {
                nactenyZvuk = soubor.readFromInternalFileZvuk(getActivity().getApplicationContext());
                prevedNactenyTextDoPromennychTime(nactenyZvuk);
            } catch (IOException e) {
                e.printStackTrace();
            }




    }

    private void nactiHodnoty() {
        soubor = new PraceSeSouborem();

        String nacteny = "";
        try {
            nacteny = soubor.readFromInternalFile(getActivity().getApplicationContext());
            prevedNactenyTextDoPromennych(nacteny);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //    Toast.makeText(this, nacteny, Toast.LENGTH_SHORT).show();
    }

    private void prevedNactenyTextDoPromennychTime(String nactenyTime) {

        try {
            nactenyTextZeSouboruTime = new StringBuilder(nactenyTime);
            for (int i = 1; i <= 8; i++) {
                if (nactenyTextZeSouboruTime.charAt(1) == ';') {
                    if (i == 1) {
                        zvukStart = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    } else if (i == 2) {
                        zvukStop = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    } else if(i==3) {
                        zvukCelkovyKonec = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    } else if(i==4) {
                        zvukCountdown = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    } else if(i==5) {
                        zvukPulkaKola = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    } else if(i==6) {
                        zvukPredKoncemKola = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    } else if(i==7) {
                        casZvukuPredKoncemKola = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    }  else if(i==8){
                        hlasitost = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                        Log.i("hlasitost-prevedene1",String.valueOf(hlasitost));
                    }
                    nactenyTextZeSouboruTime.delete(0, 2);
                } else if (nactenyTextZeSouboruTime.charAt(2) == ';') {
                    if (i == 1) {
                        zvukStart = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if (i == 2) {
                        zvukStop = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if(i==3) {
                        zvukCelkovyKonec = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if(i==4){
                        zvukCountdown = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if(i==5){
                        zvukPulkaKola = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if(i==6){
                        zvukPredKoncemKola = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if(i==7){
                        casZvukuPredKoncemKola = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    }  else if(i==8){
                        hlasitost = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                        Log.i("hlasitost-prevedene3",String.valueOf(hlasitost));
                    }
                    nactenyTextZeSouboruTime.delete(0, 3);

                } else if (nactenyTextZeSouboruTime.charAt(3) == ';') {
                    if (i == 8) {
                        Log.i("hlasitost-prevedene4",String.valueOf(nactenyTextZeSouboruTime));
                        hlasitost = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 3));
                        Log.i("hlasitost-prevedene4",String.valueOf(hlasitost));
                    }
                    nactenyTextZeSouboruTime.delete(0, 4);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void prevedNactenyTextDoPromennych(String nacteny) {
        try {
            Log.d("myTag", "načítááááááááááá to");
            nactenyTextZeSouboru = new StringBuilder(nacteny);
            casPripravy.setMin(nactiHodnotuOdepisJiZNactenehoTextu());
            casPripravy.setSec(nactiHodnotuOdepisJiZNactenehoTextu());
            casCviceni.setMin(nactiHodnotuOdepisJiZNactenehoTextu());
            casCviceni.setSec(nactiHodnotuOdepisJiZNactenehoTextu());
            casPauzy.setMin(nactiHodnotuOdepisJiZNactenehoTextu());
            casPauzy.setSec(nactiHodnotuOdepisJiZNactenehoTextu());
            pocetCyklu = nactiHodnotuOdepisJiZNactenehoTextu();
        } catch (Exception e) {
            Log.d("myTag", "nenačetlo se to");
            e.printStackTrace();
        }


    }

    private int nactiHodnotuOdepisJiZNactenehoTextu() {
        int vracenaHodnota = 1;
        try { //na google play to házelo někomu chybu v této metodě - StringIndexOutOfBoundsException, mně na mých přístrojích jsem nic nenašel
            //tak jsem to raději za try-catchoval
            if (nactenyTextZeSouboru.charAt(1) == ';') {
                vracenaHodnota = Integer.parseInt(nactenyTextZeSouboru.substring(0, 1));
                nactenyTextZeSouboru.delete(0, 2);
            } else {
                vracenaHodnota = Integer.parseInt(nactenyTextZeSouboru.substring(0, 2));
                nactenyTextZeSouboru.delete(0, 3);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vracenaHodnota;
    }


    private void vytvorDialogCasu(final Dialog dialog, final String nadpis, final TextView textView) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //pokud budu chtít nadpisy, tak tohle oddělat a tyhle zakomentované řádky pod tím naopak odkomentovat (od comment 1 do comment2

        dialog.setContentView(R.layout.set_time_layout_dialog);
//comment1       dialog.setTitle(nadpis);
//        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            //ve verzi LOLLIPOP my tohle házelo chybu, nevyřešil jsem ji, tak tohle...:-(
//            int divierId = dialog.getActivity().getApplicationContext().getResources()
//                    .getIdentifier("android:id/titleDivider", null, null);
//            View divider = dialog.findViewById(divierId);
//            divider.setVisibility(View.GONE);
//comment2        }


        // divider.setBackgroundColor(getResources().getColor(R.color.colorPisma));
        //tady     divider.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPisma));
        if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPripravy))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //    dialog.getWindow().setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
                dialog.getWindow().setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasPripravy);

        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCviceni))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasCviceni);

        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPauzy))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzy));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasPauzy);
        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPauzyMeziTabatami))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzymezitabatami));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasPauzyMeziTabatami);
        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCoolDown))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                dialog.getWindow().setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.backgroundcolorcascooldown));//  (R.drawable.background);
            } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorCasCoolDown);
        }


        final NumberPicker pocitacMin = (NumberPicker) dialog.findViewById(R.id.npMin);
        setDividerColor(pocitacMin);
        pocitacMin.setMinValue(0);
        pocitacMin.setMaxValue(99);
        final NumberPicker pocitacSec = (NumberPicker) dialog.findViewById(R.id.npSec);
        setDividerColor(pocitacSec);
        pocitacSec.setMinValue(0);
        pocitacSec.setMaxValue(59);
        //nastavím v dialogu hodnotu, která je načtená ze souboru
        if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPripravy))) {
            pocitacMin.setValue(casPripravy.getMin());
            pocitacSec.setValue(casPripravy.getSec());
        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCviceni))) {
            pocitacMin.setValue(casCviceni.getMin());
            pocitacSec.setValue(casCviceni.getSec());
        } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPauzy))) {
            pocitacMin.setValue(casPauzy.getMin());
            pocitacSec.setValue(casPauzy.getSec());
        }

        Button uloz = (Button) dialog.findViewById(R.id.buttonUloz);
        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPripravy))) {
                    casPripravy = new MyTime(0, pocitacMin.getValue(), pocitacSec.getValue());
                    textView.setText(vratStringCasUpraveny(casPripravy));
                    //             pocitacMin.setValue(casPripravy.getMin());
                    //             pocitacSec.setValue(casPripravy.getSec());
                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCviceni))) {
                    casCviceni = new MyTime(0, pocitacMin.getValue(), pocitacSec.getValue());
                    textView.setText(vratStringCasUpraveny(casCviceni));
                    //             pocitacMin.setValue(casCviceni.getMin());
                    //             pocitacSec.setValue(casCviceni.getSec());
                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasPauzy))) {
                    casPauzy = new MyTime(0, pocitacMin.getValue(), pocitacSec.getValue());
                    textView.setText(vratStringCasUpraveny(casPauzy));
                    //           pocitacMin.setValue(casPauzy.getMin());
                    //           pocitacSec.setValue(casPauzy.getSec());
                }


                dialog.dismiss(); //nebo cancel() ???
                spoctiCelkovyCasAZobraz();
                ulozDataDoSouboru();
            }
        });
    }



    private void vytvorDialogOpakovani(final Dialog dialog, final String nadpis, final TextView textView) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //pokud budu chtít nadpisy, tak tohle oddělat a tyhle zakomentované řádky pod tím naopak odkomentovat (od comment 1 do comment2

        dialog.setContentView(R.layout.set_numbers);


        //comment1      dialog.setTitle(nadpis);
        //     if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        //         int divierId = dialog.getActivity().getApplicationContext().getResources()
        //                 .getIdentifier("android:id/titleDivider", null, null);
        //         View divider = dialog.findViewById(divierId);
        //         divider.setVisibility(View.GONE);
        //comment2     }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dialog.getWindow().setBackgroundDrawable(requireContext().getResources().getDrawable(R.drawable.backgroundcolorpocetkol));//  (R.drawable.background);
        } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorPocetTabat);
        final NumberPicker pocitacDesitky = (NumberPicker) dialog.findViewById(R.id.npDesitky);
        setDividerColor(pocitacDesitky);
        pocitacDesitky.setMinValue(0);
        pocitacDesitky.setMaxValue(9);
        final NumberPicker pocitacJednotky = (NumberPicker) dialog.findViewById(R.id.npJednotky);
        setDividerColor(pocitacJednotky);
        pocitacJednotky.setMinValue(0);
        pocitacJednotky.setMaxValue(9);
        //
        if (nadpis.equals(getResources().getString(R.string.nadpisNastavPocetCyklu))) {
            pocitacDesitky.setValue((pocetCyklu - pocetCyklu%10)/10);
            pocitacJednotky.setValue(pocetCyklu%10);

        }
        Button uloz = (Button) dialog.findViewById(R.id.buttonUlozPocet);
        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pocet = String.valueOf(String.valueOf(pocitacDesitky.getValue()) + String.valueOf(pocitacJednotky.getValue()));
                textView.setText(pocet);
                if (nadpis.equals(getResources().getString(R.string.nadpisNastavPocetCyklu))) {
                    pocetCyklu = (pocitacDesitky.getValue()) * 10 + pocitacJednotky.getValue();
                    if(pocetCyklu==0) {
                        pocetCyklu = 1;
                        textView.setText("01");
                    }

                    //            pocitacDesitky.setValue((pocetCyklu - pocetCyklu%10)/10);
                    //            pocitacJednotky.setValue(pocetCyklu%10);

                }
                dialog.dismiss(); //nebo cancel() ???
                spoctiCelkovyCasAZobraz();
                ulozDataDoSouboru();
            }
        });
    }

    public void showTimePickerDialogNastavSetupZvuku() {
        // až tam dám třídu SetSoundActivity, tak vše odcomentovat
        Intent nastavZvukActivity = new Intent(getActivity().getApplicationContext(), SetSoundClassicActivity.class);
        nastavZvukActivity.putExtra("zvukstartsetsoundClassic", zvukStart);
        nastavZvukActivity.putExtra("zvukstopsetsoundClassic", zvukStop);
        nastavZvukActivity.putExtra("zvukcelkovykonecsetsoundClassic", zvukCelkovyKonec);
        nastavZvukActivity.putExtra("zvukcountdownsetsoundClassic", zvukCountdown);
        nastavZvukActivity.putExtra("zvukpulkakolaClassic", zvukPulkaKola);
        nastavZvukActivity.putExtra("zvukpupredkoncemkolaClassic", zvukPredKoncemKola);
        nastavZvukActivity.putExtra("caszvukupupredkoncemkolaClassic", casZvukuPredKoncemKola);
        nastavZvukActivity.putExtra("hlasitost",hlasitost);

        Log.i("1zvukstartsetsound", String.valueOf(zvukStart));
        Log.i("1zvukstopsetsound", String.valueOf(zvukStop));
        Log.i("1zvukcelkovykonec", String.valueOf(zvukCelkovyKonec));
        Log.i("1zvukcountdownsetsound", String.valueOf(zvukCountdown));
        Log.i("1zvukpulkakola", String.valueOf(zvukPulkaKola));
        Log.i("1zvukpupredkoncemkola", String.valueOf(zvukPredKoncemKola));
        Log.i("1zvukupupredkoncemkola", String.valueOf(casZvukuPredKoncemKola));

        //color
        nastavZvukActivity.putExtra("barvaPripravy", colorDlazdiceCasPripravy); //color
        nastavZvukActivity.putExtra("barvaCviceni", colorDlazdiceCasCviceni); //color
        nastavZvukActivity.putExtra("barvaPauzy", colorDlazdiceCasPauzy); //color


        ulozDataDoSouboru();
       // startActivity(nastavZvukActivity);
        activityResultLaunch.launch(nastavZvukActivity);

    }

    public void showTimePickerDialogNastavPripravu() {
        // custom dialog
        dialogPriprava.show();

    }

    public void showTimePickerDialogNastavCviceni() {
        // custom dialog
        dialogCviceni.show();

    }

    public void showTimePickerDialogNastavPauzu() {
        // custom dialog
        dialogPauzy.show();

    }





    public void showTimePickerDialogNastavPocetCyklu() {
        // custom dialog
        //       Toast.makeText(getApplicationContext(), "počet tabat",
        //             Toast.LENGTH_LONG).show();
        dialogPocetCyklu.show();

    }

    public void zmacknutyStart() {
        // až tam dám activityu TabataActivity, tak to dole vše odkomentovat, ale asi se to nebude jmenovat TabataActivity, ale ClassicActivity

        Intent classicActivity = new Intent(getActivity().getApplicationContext(), ClassicActivity.class);
   //     classicActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        classicActivity.putExtra("caspripavy", casPripravy);
        classicActivity.putExtra("barvaPripravy", colorDlazdiceCasPripravy); //color

        classicActivity.putExtra("cascviceni", casCviceni);
        classicActivity.putExtra("barvaCviceni", colorDlazdiceCasCviceni); //color

        classicActivity.putExtra("caspauzy", casPauzy);
        classicActivity.putExtra("barvaPauzy", colorDlazdiceCasPauzy); //color



        classicActivity.putExtra("cascelkovy", casCelkovy);

        classicActivity.putExtra("zvukstart", zvukStart);
        classicActivity.putExtra("zvukstop", zvukStop);
        classicActivity.putExtra("zvukcelkovykonec", zvukCelkovyKonec);
        classicActivity.putExtra("zvukcountdown", zvukCountdown);
        classicActivity.putExtra("zvukpulkakola", zvukPulkaKola);
        classicActivity.putExtra("zvukpredkoncemkola", zvukPredKoncemKola);
        classicActivity.putExtra("caszvukupupredkoncemkola", casZvukuPredKoncemKola);

        classicActivity.putExtra("hlasitost",hlasitost);

        if (pocetCyklu == 0) {
            pocetCyklu = 1;
        }
        classicActivity.putExtra("pocetcyklu", pocetCyklu);
        classicActivity.putExtra("barvaPocetCyklu", colorDlazdicePocetCyklu); //color


        ulozDataDoSouboru();

        startActivity(classicActivity);




    }


    //metoda, která u numperpickeru udělá ty dvě čárečky jinou barvou, než defaultní - jinak to nešlo
    private void setDividerColor(NumberPicker picker) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //pf.set(picker, getResources().getColor(R.color.my_orange));
                    //Log.v(TAG,"here");
                    //    pf.set(picker, getResources().getDrawable(R.color.colorPisma));
                    pf.set(picker, ContextCompat.getDrawable(getActivity().getApplicationContext(), R.color.colorPisma));

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

    private String vratStringCasUpravenySHodinama(MyTime casClass) {
        return getResources().getString(R.string.celkovyCas)+vratHodiny(casClass.getHour()) + vratDeseticisla(casClass.getMin()) + ":" + vratDeseticisla(casClass.getSec());
    }

    private String vratHodiny(int cislo) {
        if (cislo > 9) {
            return String.valueOf(cislo) + ":";

        } else
        {if (cislo == 0) {
            return "";
        } else
            return ("0" + String.valueOf(cislo))+ ":";
        }

    }



    private String vratDeseticisla(int cislo) {
        if (cislo < 10) {
            return ("0" + String.valueOf(cislo));
        } else
            return String.valueOf(cislo);
    }

    //metoda, která zjistí otočení displaye a potom se znovu nastaví celý layout
    //jinak mi to nešlo udělat, aby po otočení displaye se activita znovu nerestartovala a tím pádem by nebyly uloženy hodnoty
    //k tomu ještě je třeba dát do manifestu:
    //android:configChanges="orientation|screenSize" (do activity)
    public void onConfigurationChanged(Configuration newConfig) {


        super.onConfigurationChanged(newConfig);
        frameLayout.removeAllViews();
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_first, null);
        frameLayout.addView(rootView);

        // Checks the orientation of the screen
        //      if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        //          Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        vytvorView();
        udelejLayout(rootView);
        nactiColors();
        vytvorClickaciLayouty();

        //    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        //        Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        //       udelejLayout();
        //    }
    }

    private void udelejLayout(View rootView) {


        String idAplikace = "ca-app-pub-6701702247641250~7047640994";
        MobileAds.initialize(requireContext(), idAplikace);
        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        showTimePickerDialogNastavSetupZvukuLayout = rootView.findViewById(R.id.showTimePickerDialogNastavSetupZvuku);
        showTimePickerDialogNastavPripravuLayout = rootView.findViewById(R.id.showTimePickerDialogNastavPripravu);
        showTimePickerDialogNastavCviceniLayout = rootView.findViewById(R.id.showTimePickerDialogNastavCviceni);
        showTimePickerDialogNastavPauzuLayout = rootView.findViewById(R.id.showTimePickerDialogNastavPauzu);
        showTimePickerDialogNastavPocetCykluLayout = rootView.findViewById(R.id.dlazdiceCykly);
        zmacknutyStartLayout = rootView.findViewById(R.id.zmacknutyStart);

        showPickerDialogNastavPripravuColor = rootView.findViewById(R.id.showPickerDialogNastavPripravuColor);
        showPickerDialogNastavCviceniColor = rootView.findViewById(R.id.showPickerDialogNastavCviceniColor);
        showPickerDialogNastavPauzuColor = rootView.findViewById(R.id.showPickerDialogNastavPauzuColor);

        //vytvoření dialogu příprava
        hodnotaPripravyTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaPriprava);
        hodnotaPripravyTextView.setText(vratStringCasUpraveny(casPripravy));
        dialogPriprava = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
        vytvorDialogCasu(dialogPriprava, getResources().getString(R.string.nadpisNastavCasPripravy), hodnotaPripravyTextView);

        dlazdiceCasPripravy = rootView.findViewById(R.id.dlazdiceCasPripravy);
        colorDlazdiceCasPripravy = getResources().getColor(R.color.colorCasPripravy);

        //vytvoření dialogu cvičení
        hodnotaCviceniTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaCviceni);
        hodnotaCviceniTextView.setText(vratStringCasUpraveny(casCviceni));
        dialogCviceni = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
        vytvorDialogCasu(dialogCviceni, getResources().getString(R.string.nadpisNastavCasCviceni), hodnotaCviceniTextView);

        dlazdiceCasCviceni = rootView.findViewById(R.id.dlazdiceCasCviceni);
        colorDlazdiceCasCviceni = getResources().getColor(R.color.colorCasCviceni);

        //vytvoření dialogu pauzy
        hodnotaPauzyTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaPauza);
        hodnotaPauzyTextView.setText(vratStringCasUpraveny(casPauzy));
        dialogPauzy = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
        vytvorDialogCasu(dialogPauzy, getResources().getString(R.string.nadpisNastavCasPauzy), hodnotaPauzyTextView);

        dlazdiceCasPauzy = rootView.findViewById(R.id.dlazdiceCasPauzy);
        colorDlazdiceCasPauzy = getResources().getColor(R.color.colorCasPauzy);

        //vytvoření dialogu počet cyklů
        pocetCykluTextView = (TextView) rootView.findViewById(R.id.textViewPocetCyklu);
        pocetCykluTextView.setText(String.valueOf(pocetCyklu));
//        dialogPocetCyklu = new Dialog(MainActivity.this);
        dialogPocetCyklu = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
        vytvorDialogOpakovani(dialogPocetCyklu, getResources().getString(R.string.nadpisNastavPocetCyklu), pocetCykluTextView);
        dlazdiceCykly = rootView.findViewById(R.id.dlazdiceCykly);
        colorDlazdicePocetCyklu = getResources().getColor(R.color.colorCerna);

        //vytvoření nápisu celkový čas
        hodnotaCelkovyCasTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaCelkovyCasNadpis);
        spoctiCelkovyCasAZobraz();
        //      hodnotaCelkovyCasTextView.setText(vratStringCasUpravenySHodinama(casCelkovy));
        nastavColorDlazdiceKola();
    }

    private void nastavColorDlazdiceKola() {
        int[] colors1 = {colorDlazdiceCasCviceni, colorDlazdiceCasPauzy};
        GradientDrawable shadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors1);
        shadow.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
        dlazdiceCykly.setBackground(shadow);

        GradientDrawable shadow2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors1);
        shadow2.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
        dialogPocetCyklu.getWindow().setBackgroundDrawable(shadow2);
    }

    private void spoctiCelkovyCasAZobraz() {
        int pomocnyCasCviceni = casCviceni.getHour()*3600 + casCviceni.getMin()*60 + casCviceni.getSec();
        int pomocnyCasPauzy = casPauzy.getHour()*3600 + casPauzy.getMin()*60 + casPauzy.getSec();
        int pomocnyCelkovyCas = (casPripravy.getHour()*3600 + casPripravy.getMin()*60 + casPripravy.getSec())
                + (pomocnyCasCviceni * pocetCyklu )
                + (pomocnyCasPauzy * (pocetCyklu-1));

        int pomocnyCelkovyCasHodiny = pomocnyCelkovyCas/3600;
        int pomocnyCelkovyCasHodinyZbytekPoDeleni = pomocnyCelkovyCas%3600;


        Log.d("AAA celkem sekund:",String.valueOf(pomocnyCelkovyCas));
        Log.d("AAA hodiny:",String.valueOf(pomocnyCelkovyCasHodiny));
        Log.d("AAA zbytek po d hodin:",String.valueOf(pomocnyCelkovyCasHodinyZbytekPoDeleni));
        System.out.print(pomocnyCelkovyCasHodiny);
        System.out.print(pomocnyCelkovyCasHodinyZbytekPoDeleni);

        int pomocnyCelkovyCasMinuty = pomocnyCelkovyCasHodinyZbytekPoDeleni/60;
        int  pomocnyCelkovyCasSekundy =  pomocnyCelkovyCasHodinyZbytekPoDeleni%60;

        Log.d("AAA minuty:",String.valueOf(pomocnyCelkovyCasMinuty));
        Log.d("AAA sekundy:",String.valueOf(pomocnyCelkovyCasSekundy));

        casCelkovy.setHour(pomocnyCelkovyCasHodiny);
        casCelkovy.setMin(pomocnyCelkovyCasMinuty);
        casCelkovy.setSec(pomocnyCelkovyCasSekundy);

        Log.d("AAA hodiny ulozene:",String.valueOf(casCelkovy.getHour()));

        Log.d("AAA minuty ulozene:",String.valueOf(casCelkovy.getMin()));

        Log.d("AAA sekundy ulozene:",String.valueOf(casCelkovy.getSec()));

        System.out.print(pomocnyCelkovyCasMinuty);
        System.out.print(pomocnyCelkovyCasSekundy);

        System.out.print("Pokus");


        hodnotaCelkovyCasTextView.setText(vratStringCasUpravenySHodinama(casCelkovy));


    }


    //souborové funkce
    private void ulozDataDoSouboru() {
        dataSoubor = String.valueOf(casPripravy.getMin()) + ";" + String.valueOf(casPripravy.getSec()) + ";" +
                String.valueOf(casCviceni.getMin()) + ";" + String.valueOf(casCviceni.getSec()) + ";" +
                String.valueOf(casPauzy.getMin()) + ";" + String.valueOf(casPauzy.getSec()) + ";" +
                String.valueOf(pocetCyklu)+";";

        soubor.writeToFileInternal(dataSoubor, getActivity().getApplicationContext());

    }

    private void ulozDataZvukDoSouboru() {
        dataSouborTime = String.valueOf(zvukStart) + ";" + String.valueOf(zvukStop) + ";" +
                String.valueOf(zvukCelkovyKonec) + ";" + String.valueOf(zvukCountdown)+ ";" +
                String.valueOf(zvukPulkaKola) + ";" +String.valueOf(zvukPredKoncemKola) + ";" +
                String.valueOf(casZvukuPredKoncemKola) +";"
                + String.valueOf(hlasitost)+";"
        ;

        Log.i("hlasitost-ulozeni:", dataSouborTime);
        soubor.writeToFileInternalZvuk(dataSouborTime, getActivity().getApplicationContext());

    }

    private void ulozDataColorDoSouboru() {
        Log.i("asdf:", "3");
        dataSouborColor = String.valueOf(colorDlazdiceCasPripravy) + ";" + String.valueOf(colorDlazdiceCasCviceni) + ";" +
                String.valueOf(colorDlazdiceCasPauzy) + ";"
        ;
        Log.i("colorDlazdicePripravy:", String.valueOf(colorDlazdiceCasPripravy));
        Log.i("colorDlazdiceCviceni:", String.valueOf(colorDlazdiceCasCviceni));
        Log.i("colorDlazdiceCasPauzy:", String.valueOf(colorDlazdiceCasPauzy));

        soubor.writeToFileInternalColor(dataSouborColor, getActivity().getApplicationContext());

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();


    }

    //   private void log(String msg) {
    //       Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    //       Log.d("AdinCube", msg);
    //   }

    //   private final AdinCubeConsentEventListener askEventListener = new AdinCubeConsentEventListener() {
    //       @Override public void onComplete(AdinCube.UserConsent.Answer answer) {
    //           log("ask - onComplete: " + answer);
    //       }
    //       @Override public void onError(String errorCode) {
    //           log("ask - onError: " + errorCode);
    //       }
    //   };
    private void nactiColors() {
        //v tuto dobu budou načteny colors defaultně, protože tato metoda se volá déle, než metoda udelejLayout
        // ve které se defaultní hodnoty do colors načítají


        Log.i("asdf:", "11");
        //načtení hodnot pro colors ze souboru, pokud už existuje soubor datacolor.txt
        soubor = new PraceSeSouborem();
        String nactenyColors = "";
        Log.i("asdf:", "12");
        try {
            Log.i("asdf:", "13");
            nactenyColors = soubor.readFromInternalFileColor(getActivity().getApplicationContext());
            Log.i("nactenyColors:", nactenyColors);
            prevedNactenyTextDoPromennychColor(nactenyColors);
            Log.i("asdf:", "15");

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private void prevedNactenyTextDoPromennychColor(String nactenyColor) {
        Log.i("asdf:", nactenyColor);
        try {
            Log.i("asdf:", "16");
            nactenyTextZeSouboruColor = new StringBuilder(nactenyColor);
            Log.i("asdf:", "17");
            for (int i = 1; i <= 3; i++) {
                if (nactenyTextZeSouboruColor.charAt(8) == ';') {
                    Log.i("Colors:", "charAt8First");
                    if (i == 1) {
                        colorDlazdiceCasPripravy = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 8));
                        Log.i("pripravaColors:", String.valueOf(colorDlazdiceCasPripravy));
                        nastavColorDlazdice("colorDlazdiceCasPripravy", colorDlazdiceCasPripravy,dlazdiceCasPripravy);
                    } else if (i == 2) {
                        colorDlazdiceCasCviceni = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 8));
                        Log.i("pauzaColors:", String.valueOf(colorDlazdiceCasCviceni));
                        nastavColorDlazdice("colorDlazdiceCasCviceni", colorDlazdiceCasCviceni,dlazdiceCasCviceni);
                    } else {
                        colorDlazdiceCasPauzy = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 8));
                        Log.i("pauzaColors:", String.valueOf(colorDlazdiceCasPauzy));
                        nastavColorDlazdice("colorDlazdiceCasPauzy", colorDlazdiceCasPauzy,dlazdiceCasPauzy);
                    }
                    nactenyTextZeSouboruColor.delete(0, 9);
                } else if (nactenyTextZeSouboruColor.charAt(9) == ';') {
                    Log.i("Colors:", "charAt9First");

                    if (i == 1) {
                        colorDlazdiceCasPripravy = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 9));
                        Log.i("pripravaColors:", String.valueOf(colorDlazdiceCasPripravy));
                        nastavColorDlazdice("colorDlazdiceCasPripravy", colorDlazdiceCasPripravy,dlazdiceCasPripravy);
                    } else if (i == 2) {
                        colorDlazdiceCasCviceni = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 9));
                        Log.i("cviceniColors:", String.valueOf(colorDlazdiceCasCviceni));
                        nastavColorDlazdice("colorDlazdiceCasCviceni", colorDlazdiceCasCviceni,dlazdiceCasCviceni);
                    } else {
                        colorDlazdiceCasPauzy = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 9));
                        Log.i("pauzaColors:", String.valueOf(colorDlazdiceCasPauzy));
                        nastavColorDlazdice("colorDlazdiceCasPauzy", colorDlazdiceCasPauzy,dlazdiceCasPauzy);
                    }
                    nactenyTextZeSouboruColor.delete(0, 10);
                } else if (nactenyTextZeSouboruColor.charAt(10) == ';') {
                    Log.i("Colors:", "charAt10First");
                    if (i == 1) {
                        colorDlazdiceCasPripravy = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 10));
                        Log.i("pripravaColors:", String.valueOf(colorDlazdiceCasPripravy));
                        nastavColorDlazdice("colorDlazdiceCasPripravy", colorDlazdiceCasPripravy,dlazdiceCasPripravy);
                    } else if (i == 2) {
                        colorDlazdiceCasCviceni = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 10));
                        Log.i("cviceniColors:", String.valueOf(colorDlazdiceCasCviceni));
                        nastavColorDlazdice("colorDlazdiceCasCviceni", colorDlazdiceCasCviceni,dlazdiceCasCviceni);
                    } else {
                        colorDlazdiceCasPauzy = Integer.parseInt(nactenyTextZeSouboruColor.substring(0, 10));
                        Log.i("pauzaColors:", String.valueOf(colorDlazdiceCasPauzy));
                        nastavColorDlazdice("colorDlazdiceCasPauzy", colorDlazdiceCasPauzy,dlazdiceCasPauzy);
                    }
                    nactenyTextZeSouboruColor.delete(0, 11);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }


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
            colorDlazdiceCasCviceni = color;
            dialogCviceni.getWindow().setBackgroundDrawable(shape2);
            nastavColorDlazdiceKola();
        } else if (colorDlazdiceString.equals("colorDlazdiceCasPauzy")) {
            colorDlazdiceCasPauzy = color;
            dialogPauzy.getWindow().setBackgroundDrawable(shape2);
            nastavColorDlazdiceKola();
        }    else if (colorDlazdiceString.equals("colorDlazdiceCasPripravy")) {
            colorDlazdiceCasPripravy = color;
            dialogPriprava.getWindow().setBackgroundDrawable(shape2);
        }

    }
    private void zobrazNastaveniColoruDlazdice(String colorDlazdiceString, LinearLayout dlazdice) { //tady pokračovat, tady to asi udělalo chybu
        final ColorPicker colorPicker = new ColorPicker(getActivity()); //tady nevím, jestli je to OK, původně to bylo MainActivity.this, tady je chyba, vyřešit to
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                Log.i("asdf:", "1");
                nastavColorDlazdice(colorDlazdiceString, color,dlazdice);
                Log.i("asdf:", "2");
                ulozDataColorDoSouboru();

            }

            @Override
            public void onCancel(){
                // put code
            }
        }).setColumns(5)
                .setRoundColorButton(true)
                .setTitle(getResources().getString(R.string.nadpisNastavBarvu))
                .setColors(R.array.default_colors_color_picker)
        ;


        colorPicker.getDialogViewLayout().setBackgroundResource(R.drawable.background);
        //     colorPicker.getDialogViewLayout().setBackgroundDrawableResource(backgroundcolorpicker);

        colorPicker.show();
    }



}