package com.machy1979ii.intervaltimer.ui.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.machy1979ii.intervaltimer.CustomActivity;
import com.machy1979ii.intervaltimer.R;
import com.machy1979ii.intervaltimer.SetSoundCustomActivity;
import com.machy1979ii.intervaltimer.funkce.PraceSeSouboremCustom;
import com.machy1979ii.intervaltimer.funkce.VypocetCelkovehoCasuAZobrezni;
import com.machy1979ii.intervaltimer.funkce.dialogovefunkce.VytvoreniDialoguCasu;
import com.machy1979ii.intervaltimer.funkce.dialogovefunkce.VytvoreniDialoguColoru;
import com.machy1979ii.intervaltimer.funkce.dialogovefunkce.VytvoreniDialoguPoctuCyklu;
import com.machy1979ii.intervaltimer.funkce.dialogovefunkce.VytvoreniDialoguZvuku;
import com.machy1979ii.intervaltimer.models.MyTime;
import com.machy1979ii.intervaltimer.models.PolozkaCasuKola;
import com.machy1979ii.intervaltimer.models.SouborPolozekCasuKola;

import java.io.IOException;
import java.util.ArrayList;

public class ThirdFragment extends Fragment {

    private View rootView;
    private FrameLayout frameLayout;


    private LinearLayout dlazdiceKontejner;  //kontejner, do kterého bude uživatel naklikávat časy
    private LinearLayout showVlozDalsiCasLayout; //plus, kterým uživatel vloží nový layout s časy

    private LinearLayout showTimePickerDialogNastavSetupZvukuLayout;


    //polozka celkový čas
    private MyTime casCelkovy = new MyTime(0, 1, 0);
    //    private Dialog dialogPriprava;
    private TextView hodnotaCelkovyCasTextView;

    //polozka příprava
    private MyTime casPripravy = new MyTime(0, 1, 0);
    private Dialog dialogPriprava;
    private TextView hodnotaPripravyTextView;

    private LinearLayout showTimePickerDialogNastavPripravuLayout;
    private LinearLayout showPickerDialogNastavPripravuColor;
    private LinearLayout dlazdiceCasPripravy; //color
    private int colorDlazdiceCasPripravy; //color
    private PolozkaCasuKola polozkaCasuPriprava;

    //nastavení zvuků
    private int zvukCelkovyKonec = 1;
    private int zvukCountdown = 1;

    private String dataSouborTime;
    private StringBuilder nactenyTextZeSouboruTime;
    private int hlasitost = 100;

    //funkce
    private VytvoreniDialoguCasu vytvoreniDialoguCasu;
    private VytvoreniDialoguColoru vytvoreniDialoguColoru;
    private VypocetCelkovehoCasuAZobrezni vypocetCelkovehoCasuAZobrezni;
    private VytvoreniDialoguPoctuCyklu VytvoreniDialoguPoctuCyklu;
    private VytvoreniDialoguZvuku vytvoreniDialoguZvuku;

    private LinearLayout zmacknutyStartLayout;

    private ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol = new ArrayList<SouborPolozekCasuKola>();

    private int pocitacDlazdic = 1;
    private PraceSeSouboremCustom praceSeSouborem;
    private boolean dlazdicePripravyVytvorena = false;


    //pro lazy nahrávání ze souboru, protože vkládání velkého počtu layoutů, a jejich nastavování, trvá dlouho, tak se to musí udělat tak, že se ověří, zda je tenhle fragment zobrazen a zároveň se provedlo onCreateView
    //pokud ano, spustí vkládání a nastavování dalších layoutů - takhle se to musí udělat, protože u fragmaentů se preloadují oba layouty sousední, nedá se to vypnout, a když apka byla v druhém fragmentu a po zlomku sekundy
    //uživatel kliknut na třetí layout, tak ten se v té době už načítal a tím pádem to na zlomek sekundy se to zaseklo v mezi, nevypadalo to hezky, tak preload necházme načíst jen základ a zbytek layoutů ze souboru
    //až po zobrazení tohoto fragmentu a zároveň po udělaném onCreateView - viz. např. zde: https://programmer.help/blogs/preloading-and-lazy-loading-of-viewpager-fragment-combination.html

    //Fragment's View loaded tag
    private boolean isViewCreated;

    //Fragment's Visible Markup to Users
    private boolean isUIVisible;

    private ProgressBar progressBar;
    private AsyncTaskNahraniLayoutu asyncTaskNahraniLayoutu;

    //pro nastavení výšky dlaždic
    private int vyskaDlazdicePripravy;

    private ActivityResultLauncher<Intent> activityResultLaunch;




    public ThirdFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        frameLayout = new FrameLayout(getActivity());
        LayoutInflater inflater2 = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater2.inflate(R.layout.fragment_third, null);
        frameLayout.addView(rootView);
      //  rootView = inflater.inflate(R.layout.fragment_third, container, false);

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


        vytvorLayout();
        vytvorClickaciLayouty();
        nactiZvuky();
        progressBar = rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        return frameLayout;
    //    return rootView;


    }

    private void nactiZvukyZActivitySetSound(Intent intent) {
        zvukCelkovyKonec = intent.getIntExtra("zvukcelkovykoneccustom", 0);
        zvukCountdown = intent.getIntExtra("zvukcountdowncustom", 0);
        hlasitost = intent.getIntExtra("hlasitostcustom", 100);

        ulozDataZvukDoSouboru();
    }

    private void nactiZvuky() {
        //pokud se spustí aplikace, tak je potřba níže uvedené proměnné načíst ze souboru
        //a raději než se ze souboru načtou, tak jim dám předem hodnotu 1, kdyby se něco s načtením ze souboru podělalo
        //nebo kdyby to bylo první načtení, kdy ještě neexistuje soubor "datatime.txt"


            zvukCelkovyKonec = 21;
            zvukCountdown =1;

            //načtení hodnot pro zvuky ze souboru
            praceSeSouborem = new PraceSeSouboremCustom();
            String nactenyZvuk = "";
            try {
                nactenyZvuk = praceSeSouborem.readFromInternalFileZvuk(getActivity().getApplicationContext());
                prevedNactenyTextDoPromennychTime(nactenyZvuk);

            } catch (IOException e) {
                e.printStackTrace();
            }


    }

    private void ulozDataZvukDoSouboru() {
        dataSouborTime = String.valueOf(zvukCelkovyKonec) + ";" + String.valueOf(zvukCountdown)+ ";"
                + String.valueOf(hlasitost)+";"
        ;

        Log.i("zvuky:", dataSouborTime);

        praceSeSouborem.writeToFileInternalZvuk(dataSouborTime, getActivity().getApplicationContext());

    }

    private void prevedNactenyTextDoPromennychTime(String nactenyTime) {

        try {
            nactenyTextZeSouboruTime = new StringBuilder(nactenyTime);
            for (int i = 1; i <= 3; i++) {
                if (nactenyTextZeSouboruTime.charAt(1) == ';') {
                    if (i == 1) {
                        zvukCelkovyKonec = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    } else if (i == 2) {
                        zvukCountdown = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    } else if(i==3) {
                        hlasitost = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 1));
                    }
                    nactenyTextZeSouboruTime.delete(0, 2);
                } else if (nactenyTextZeSouboruTime.charAt(2) == ';') {
                    if (i == 1) {
                        zvukCelkovyKonec = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if (i == 2) {
                        zvukCountdown = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    } else if(i==3) {
                        hlasitost = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 2));
                    }
                    nactenyTextZeSouboruTime.delete(0, 3);
                } else if (nactenyTextZeSouboruTime.charAt(3) == ';') {
                    if (i == 3) {
                        hlasitost = Integer.parseInt(nactenyTextZeSouboruTime.substring(0, 3));
                    }
                    nactenyTextZeSouboruTime.delete(0, 4);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void vytvorLayout() {


        zmacknutyStartLayout = rootView.findViewById(R.id.zmacknutyStart);

        dlazdiceKontejner = rootView.findViewById(R.id.dlazdiceKontejner);
        showVlozDalsiCasLayout = rootView.findViewById(R.id.showVlozDalsiCasLayout);

        //textview celkový čas
        hodnotaCelkovyCasTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaCelkovyCasNadpis);

        //vytvoření funkcí pro dialogy a uložení a načtení z/do souboru
        vytvoreniDialoguCasu = new VytvoreniDialoguCasu(getActivity().getApplicationContext(), hodnotaCelkovyCasTextView);
    //    vytvoreniDialoguCasu = new VytvoreniDialoguCasu(requireContext(), hodnotaCelkovyCasTextView);
        vytvoreniDialoguColoru = new VytvoreniDialoguColoru(requireContext());
        vypocetCelkovehoCasuAZobrezni = new VypocetCelkovehoCasuAZobrezni(getActivity().getApplicationContext(), hodnotaCelkovyCasTextView, casCelkovy);
        VytvoreniDialoguPoctuCyklu = new VytvoreniDialoguPoctuCyklu(getActivity().getApplicationContext(), hodnotaCelkovyCasTextView);
        vytvoreniDialoguZvuku = new VytvoreniDialoguZvuku(getActivity().getApplicationContext());
        praceSeSouborem = new PraceSeSouboremCustom();

        //nactiDataZeSouboru();
        //načte data ze souboru
        try {
            vsechnyPolozkyCasyKol = praceSeSouborem.readFromInternalFile(getActivity().getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
            vsechnyPolozkyCasyKol = new ArrayList<SouborPolozekCasuKola>();
        }
        vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);

        //vytvoření přípravy
        showTimePickerDialogNastavPripravuLayout = rootView.findViewById(R.id.showTimePickerDialogNastavPripravu);
        showPickerDialogNastavPripravuColor = rootView.findViewById(R.id.showPickerDialogNastavPripravuColor);
        dlazdiceCasPripravy = rootView.findViewById(R.id.dlazdiceCasPripravy);
        //níže uvedené je tady proto, aby to ocheckovalo, kdy je dlazdicePripravy vykreslana, aby se mohla spočítat
        //její výška a tahle souřadnice se pak použít při výpočtu výšky vložených dlaždic, jinak to nešlo udělat,
        //protože jinak to výšku dlaždice házelo 0, protože ještě nebyla načtena, tak proto je použit níže uvedený listener
        //aby se výška spočítala až v momentě, kdy je dlaždice načtena
        final ViewTreeObserver observer= dlazdiceCasPripravy.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Log.i("++++", "Height: " + dlazdiceCasPripravy.getHeight());
                        Log.i("++++", "layout je dlazdicePripravy je udělán");
                        vyskaDlazdicePripravy = dlazdiceCasPripravy.getHeight();
                        dlazdicePripravyVytvorena = true;

                        lazyLoad();
                    }
                });
        colorDlazdiceCasPripravy = getResources().getColor(R.color.colorCasPripravy);

        //vytvoření dialogu příprava
        hodnotaPripravyTextView = (TextView) rootView.findViewById(R.id.textViewHodnotaPriprava);

        dialogPriprava = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));

        if (vsechnyPolozkyCasyKol.size()==0 || vsechnyPolozkyCasyKol.isEmpty() || vsechnyPolozkyCasyKol == null) { //pokud půjde o první spuštění, tak se položka přípravy nově udělá, dal jsem tam i ošetření proti isEmpty a null, protože po nové aktualizazi mi to zde házelo chybu
            Log.d("AAA AAB:","3");
            hodnotaPripravyTextView.setText(vratStringCasUpraveny(casPripravy));
            polozkaCasuPriprava = new PolozkaCasuKola(casPripravy, colorDlazdiceCasPripravy, 1, false, getResources().getString(R.string.nadpisCasPripravy));
            SouborPolozekCasuKola souborPolozekCasuKolaPripravy = new SouborPolozekCasuKola();
            souborPolozekCasuKolaPripravy.vlozPolozkuCasKola(polozkaCasuPriprava);
            souborPolozekCasuKolaPripravy.setPocetCyklu(1);
            if (vsechnyPolozkyCasyKol.isEmpty() || vsechnyPolozkyCasyKol == null) vsechnyPolozkyCasyKol = new ArrayList<SouborPolozekCasuKola>();
            vsechnyPolozkyCasyKol.add(souborPolozekCasuKolaPripravy);

            vytvoreniDialoguCasu.vytvorDialogCasu(dialogPriprava, getResources().getString(R.string.nadpisNastavCasPripravy), hodnotaPripravyTextView, casPripravy, polozkaCasuPriprava, vsechnyPolozkyCasyKol);

        } else { //pokud jde o další spuštění a položka přípravy se načte ze souboru, tak se tady jen nastaví
            Log.d("AAA AAB:","4");
            hodnotaPripravyTextView.setText(vratStringCasUpraveny(vsechnyPolozkyCasyKol.get(0).getPolozkyCasyKol().get(0).getTime()));
            polozkaCasuPriprava = vsechnyPolozkyCasyKol.get(0).getPolozkyCasyKol().get(0);
            vytvoreniDialoguCasu.vytvorDialogCasu(dialogPriprava, getResources().getString(R.string.nadpisNastavCasPripravy), hodnotaPripravyTextView, polozkaCasuPriprava.getTime(),
                    polozkaCasuPriprava, vsechnyPolozkyCasyKol);
            vytvoreniDialoguColoru.nastavColorDlazdice(polozkaCasuPriprava.getColorDlazdice(), dlazdiceCasPripravy, dialogPriprava);

            obarviNactenouDlazdici(dlazdiceCasPripravy,polozkaCasuPriprava.getColorDlazdice());


        }




        showTimePickerDialogNastavSetupZvukuLayout = rootView.findViewById(R.id.showTimePickerDialogNastavSetupZvuku);

         //vytvoření nápisu celkový čas
        vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);
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

        showVlozDalsiCasLayout.setOnClickListener(new View.OnClickListener() {
            //plus, kterým uživatel vloží nový layout s časy
            @Override
            public void onClick(View v) {
                vlozDalsiSouborPolozek2(null, false);
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
                //    zobrazNastaveniColoruDlazdice("colorDlazdiceCasPripravy",dlazdiceCasPripravy, 0, null);
                vytvoreniDialoguColoru.zobrazNastaveniColoruDlazdice(dlazdiceCasPripravy, polozkaCasuPriprava, dialogPriprava, vsechnyPolozkyCasyKol);

            }
        });
    }




    private void nastavVymazaniDlazdice(View dlazdiceSouboruCasu, SouborPolozekCasuKola souborPolozekCasuKola, PolozkaCasuKola polozkaCasu) {

        LinearLayout showVymazDlazdici = dlazdiceSouboruCasu.findViewById(R.id.showVymazDlazdici);
        //nastaví se výška dlaždic
        ViewGroup.LayoutParams paramsSpodniMenu = showVymazDlazdici.getLayoutParams();
        paramsSpodniMenu.height = vyskaDlazdicePripravy;
        showVymazDlazdici.setLayoutParams(paramsSpodniMenu);

        showVymazDlazdici.setOnClickListener(new View.OnClickListener() {
            ViewGroup parent = (ViewGroup) showVymazDlazdici.getParent();
            @Override
            public void onClick(View v) {
                parent.removeAllViews();
                if (parent.getParent() != null) {
                    ((ViewGroup) parent.getParent()).removeView(parent);
                }

                souborPolozekCasuKola.vymazPolozkuCasKola(polozkaCasu);


                //spoctiCelkovyCasAZobraz();
                vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);
            }
        });


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


    public void showTimePickerDialogNastavSetupZvuku() {
        // custom dialog
        Intent nastavZvukCustomActivity = new Intent(getActivity().getApplicationContext(), SetSoundCustomActivity.class);
        nastavZvukCustomActivity.putExtra("zvukcelkovykoneccustom", zvukCelkovyKonec);
        nastavZvukCustomActivity.putExtra("zvukcountdowncustom", zvukCountdown);
        nastavZvukCustomActivity.putExtra("hlasitostcustom", hlasitost);

        //color
        nastavZvukCustomActivity.putExtra("barvaPripravy", colorDlazdiceCasPripravy); //color

        //  ulozDataDoSouboru(); tuhle metodu musím upravit podle schématu, které vymyslím
       // startActivity(nastavCasActivity);
        activityResultLaunch.launch(nastavZvukCustomActivity);

    }

    private void obarviNactenouDlazdici(LinearLayout dlazdice, int color) {
        //metoda, která obarví dlaždici při načtení dat ze souboru, jinak se dlaždice obarvují ve třídě VytvoreniDialoguColoru
        GradientDrawable shape1 =  new GradientDrawable();
        shape1.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
        shape1.setColor(color);
        dlazdice.setBackground(shape1);
    }

    public void zmacknutyStart() {
        Intent customActivity = new Intent(getActivity().getApplicationContext(), CustomActivity.class);

        customActivity.putParcelableArrayListExtra("vsechnyPolozkyCasyKol",vsechnyPolozkyCasyKol);

        customActivity.putExtra("colorSpodnichDlazdic", getResources().getColor(R.color.colorSpodnichDLazdicCustomActivity));

        vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);
        customActivity.putExtra("caspripavy", casPripravy);
        customActivity.putExtra("barvaPripravy", colorDlazdiceCasPripravy); //color

        customActivity.putExtra("casCelkovy", casCelkovy);

        customActivity.putExtra("zvukcelkovykonec", zvukCelkovyKonec);
        customActivity.putExtra("zvukcountdown", zvukCountdown);

        customActivity.putExtra("hlasitost",hlasitost);
      //  customActivity.putParcelableArrayListExtra("vsechnyPolozkyCasyKol", vsechnyPolozkyCasyKol);


        startActivity(customActivity);

        //pokus
        //      Intent pokus = new Intent(getApplicationContext(), HomeActivity.class);
        //      startActivity(pokus);


    }

/*    private void nactiDataZeSouboru() {
        try {
            Log.i("nacteniSouboru","111");
            vsechnyPolozkyCasyKol = praceSeSouborem.readFromInternalFile(getActivity().getApplicationContext());
            Log.i("nacteniSouboru","222");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("nacteniSouboru","333");
            Log.i("nacteniSouboru",e.toString());
        }
        Log.i("nacteniSouboru","444");
        vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);
        Log.i("nacteniSouboru","555");

    }*/

    //bylo zde jen public class
    public class AsyncTaskNahraniLayoutu extends AsyncTask<Void, Void, Void>{

        boolean isNotCancelled = true;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
         //   vytvorZobrazProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... args) {
            // do background work here
            try {
                Thread.sleep(250); // nejdříve v druhém vláknu počká 250 sec, aby se načetla základní obrazovka
                vyskaDlazdicePripravy = dlazdiceCasPripravy.getMeasuredHeight();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            getActivity().runOnUiThread(new Runnable() { //...a potom se v původním vláknu načtou další layouty, musejí se načíst v původním hlavním vláknu
                //nemůžu to udělat v tom druhém vláknu, protože tam se vytvářejí dialogy a přidávají se layouty a to je třeba dělat v původním vláknu - dle návodů

                @Override
                public void run() {

                    udelejLayoutyCasuZNactenehoSouboru();

/*                    boolean prvniPolozkaCasu = true;
                    for(SouborPolozekCasuKola soubor : vsechnyPolozkyCasyKol) {
                        if(!prvniPolozkaCasu && isNotCancelled) { //první položku času neudělá, protože to je čas přípravy a layout tohoto se už udělal
                            vlozDalsiSouborPolozek(soubor, true);

                        }
                        prvniPolozkaCasu = false;
                    }*/

                }
            });
           return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        //    progressBar.setProgress(progressPosun);
        }

        @Override
        protected void onPostExecute(Void result) {
            // do UI work here
            progressBar.setVisibility(View.GONE);

       //     myProgressDialog.dismiss();
        }

/*        @Override
        protected void onCancelled() {
            Log.i("xxx","111");
            isNotCancelled = false;
            Log.i("xxx","222");
            super.onCancelled();

        }*/

    }


    private void udelejLayoutyCasuZNactenehoSouboru() {

        boolean prvniPolozkaCasu = true;
        for(SouborPolozekCasuKola soubor : vsechnyPolozkyCasyKol) {
            if(!prvniPolozkaCasu) { //první položku času neudělá, protože to je čas přípravy a layout tohoto se už udělal
                vlozDalsiSouborPolozek2(soubor, true);


            }
            prvniPolozkaCasu = false;
        }

    }

    //pro lazy nahrávání ze souboru, protože vkládání velkého počtu layoutů, a jejich nastavování, trvá dlouho, tak se to musí udělat tak, že se ověří, zda je tenhle fragment zobrazen a zároveň se provedlo onCreateView
    //pokud ano, spustí vkládání a nastavování dalších layoutů - takhle se to musí udělat, protože u fragmaentů se preloadují oba layouty sousední, nedá se to vypnout, a když apka byla v druhém fragmentu a po zlomku sekundy
    //uživatel kliknut na třetí layout, tak ten se v té době už načítal a tím pádem to na zlomek sekundy se to zaseklo v mezi, nevypadalo to hezky, tak preload necházme načíst jen základ a zbytek layoutů ze souboru
    //až po zobrazení tohoto fragmentu a zároveň po udělaném onCreateView - viz. např. zde: https://programmer.help/blogs/preloading-and-lazy-loading-of-viewpager-fragment-combination.html


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
        Log.i("++++", "layout je created");
        lazyLoad();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //The boolean value isVisibleToUser indicates whether the UI user of the Fragment is visible or not.
        if (isVisibleToUser) {
            Log.i("++++", "layout je visible");
            isUIVisible = true;

            lazyLoad();
        } else {

            isUIVisible = false;
        }
    }

    private void lazyLoad() {
        //The double tag judgment here is because setUserVisibleHint calls back many times and before onCreateView is executed, you must ensure that onCreateView is loaded and the page is visible before loading data.
        if (isViewCreated && isUIVisible && dlazdicePripravyVytvorena) {
           // vytvorZobrazProgressDialog();
          //  vyskaDlazdicePripravy = dlazdiceCasPripravy.getMeasuredHeight();
            if (vyskaDlazdicePripravy ==0) {
                vyskaDlazdicePripravy = (int) getResources().getDimension(R.dimen.vyskaDlazcie);
            }
            asyncTaskNahraniLayoutu = new AsyncTaskNahraniLayoutu();
            asyncTaskNahraniLayoutu.execute();

            //When the data is loaded, tags are restored to prevent repeated loading.
            isViewCreated = false;
            isUIVisible = false;

        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i("layoutNahran", "onResume");

    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i("layoutNahran", "onStart");

    }

    public void onConfigurationChanged(Configuration newConfig) {


        super.onConfigurationChanged(newConfig);
        frameLayout.removeAllViews();
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_third, null);
        frameLayout.addView(rootView);

        vytvorLayout();
        vytvorClickaciLayouty();
        nactiZvuky();
        progressBar = rootView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        if (vyskaDlazdicePripravy ==0) {
            vyskaDlazdicePripravy = (int) getResources().getDimension(R.dimen.vyskaDlazcie);
        }

        asyncTaskNahraniLayoutu = new AsyncTaskNahraniLayoutu();
        asyncTaskNahraniLayoutu.execute();

        //When the data is loaded, tags are restored to prevent repeated loading.
        isViewCreated = false;
        isUIVisible = false;


    }



    //nová architektura vkládání
    private void vlozDalsiSouborPolozek2(SouborPolozekCasuKola souborPolozekCasuKolaVlozeny, boolean nactenoZeSouboru) { //vloží soubor časů, kdy první dlaždice času je defaultní, dlaší se dají přiklikat
    View dlazdiceSouboruCasu = getLayoutInflater().inflate(R.layout.dlazdice_souboru_casu, null);

        //nastaví se výška dlaždic spodního a horního menu podle výšky dlaždicePřípravy:
    LinearLayout dlazdiceSpodnihoMenu = dlazdiceSouboruCasu.findViewById(R.id.dlazdiceSpodnihoMenu);
        ViewGroup.LayoutParams paramsSpodniMenu = dlazdiceSpodnihoMenu.getLayoutParams();
        paramsSpodniMenu.height = vyskaDlazdicePripravy;
        dlazdiceSpodnihoMenu.setLayoutParams(paramsSpodniMenu);
        Log.i("spodniMenu",String.valueOf(paramsSpodniMenu.height));

        LinearLayout dlazdiceHornihoMenu = dlazdiceSouboruCasu.findViewById(R.id.dlazdiceHornihoMenu);
        ViewGroup.LayoutParams paramsHorniMenu = dlazdiceHornihoMenu.getLayoutParams();
        paramsHorniMenu.height = vyskaDlazdicePripravy/2;
        dlazdiceHornihoMenu.setLayoutParams(paramsHorniMenu);


    dlazdiceKontejner.addView(dlazdiceSouboruCasu);

    SouborPolozekCasuKola souborPolozekCasuKola;
    if (souborPolozekCasuKolaVlozeny == null) {
        //tato dlaždice je nově vytvořena kliknutím na plus
        souborPolozekCasuKola = new SouborPolozekCasuKola();
        //...tak se musí udělat první defaultní PolozkaCasuKola
        MyTime cas = new MyTime(0, 1, 0);
        int colorDlazdice;
        //toto je ternární operátor, jakože IF je podmínka splněna, tedy že je pocitacDlazdice sudé číslo,  potom colorDlazdice= colorCasCviceni, v opacnem případě colorCasCviceni=colorCasPauzy
        colorDlazdice = (pocitacDlazdic % 2 == 0 ) ? getResources().getColor(R.color.colorCasPauzy) : getResources().getColor(R.color.colorCasCviceni);
        PolozkaCasuKola polozkaCasu = new PolozkaCasuKola(cas, colorDlazdice, 1, false, (getResources().getString(R.string.timeNapis)+" "+String.valueOf(pocitacDlazdic)));
        pocitacDlazdic ++;
        souborPolozekCasuKola.vlozPolozkuCasKola(polozkaCasu);
        nastavVlozeneLayouty2(dlazdiceSouboruCasu, cas, polozkaCasu);
    } else {
        //tato dlaždice je vytvořena klonováním dlaždice
        souborPolozekCasuKola = souborPolozekCasuKolaVlozeny;
        nastavVlozeneLayouty2(dlazdiceSouboruCasu, souborPolozekCasuKolaVlozeny.vratPolozkuCasKola(0).getTime(), souborPolozekCasuKolaVlozeny.vratPolozkuCasKola(0));


    }

    //nastavení plusu - přidání jednoho času do této dlaždice
    LinearLayout kontejnerVSouboruCasu = dlazdiceSouboruCasu.findViewById(R.id.dlazdiceSouboruCasu);
    LinearLayout showVlozDalsiCasVSouboruLayout = dlazdiceSouboruCasu.findViewById(R.id.showVlozDalsiCasVSouboruLayout);
    showVlozDalsiCasVSouboruLayout.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View dlazdiceJednohoCasu = getLayoutInflater().inflate(R.layout.dlazdice_jednoho_casu, null);
            //dlaždice přidaná položka
            MyTime cas2 = new MyTime(0, 1, 0);
            int colorDlazdice2;
            //toto je ternární operátor, jakože IF je podmínka splněna, tedy že je pocitacDlazdice sudé číslo,  potom colorDlazdice= colorCasCviceni, v opacnem případě colorCasCviceni=colorCasPauzy
            colorDlazdice2 = (pocitacDlazdic % 2 == 0 ) ? getResources().getColor(R.color.colorCasPauzy) : getResources().getColor(R.color.colorCasCviceni);
            PolozkaCasuKola polozkaCasu2 = new PolozkaCasuKola(cas2, colorDlazdice2, 1, false, (getResources().getString(R.string.timeNapis)+" "+String.valueOf(pocitacDlazdic)));
            pocitacDlazdic ++;
            nastavVlozeneLayouty2(dlazdiceJednohoCasu, cas2, polozkaCasu2); //nastaví layout a klikací dialogy nové dlaždici aneb PolozceCasuKola
            souborPolozekCasuKola.vlozPolozkuCasKola(polozkaCasu2);
            nastavVymazaniDlazdice(dlazdiceJednohoCasu, souborPolozekCasuKola, polozkaCasu2);
            kontejnerVSouboruCasu.addView(dlazdiceJednohoCasu);
            vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);

        }
    });

    //pokud tato metoda byla zavolaná klonováním dlaždice, tak je třeba zobrazit položky času, které jsou v clonovaném SouboruPolozekCasuKola uloženy
    if (souborPolozekCasuKola.getPolozkyCasyKol().size()>1) {
        for (int i=1; i < souborPolozekCasuKola.getPolozkyCasyKol().size(); i++) {
            View dlazdiceJednohoCasu = getLayoutInflater().inflate(R.layout.dlazdice_jednoho_casu, null);
            nastavVlozeneLayouty2(dlazdiceJednohoCasu, souborPolozekCasuKola.getPolozkyCasyKol().get(i).getTime(), souborPolozekCasuKola.getPolozkyCasyKol().get(i));
            nastavVymazaniDlazdice(dlazdiceJednohoCasu, souborPolozekCasuKola, souborPolozekCasuKola.getPolozkyCasyKol().get(i));
            kontejnerVSouboruCasu.addView(dlazdiceJednohoCasu);
        }
    }


    //nastavení tlačítka pro výběr počtu cyklů
    LinearLayout showTimePickerDialogNastavPocetCyklu = dlazdiceSouboruCasu.findViewById(R.id.showTimePickerDialogNastavPocetCyklu);
    TextView textViewPocetCyklu = dlazdiceSouboruCasu.findViewById(R.id.textViewPocetCyklu);
    //nastaví do textView počet cyklů, kdyby to náhodou byla jiná hodnota než 1, protože by to byla zkopírovaná dlaždice
    int pocetCykluPomocny = souborPolozekCasuKola.getPocetCyklu();
    String pocetCykluPomocnyString = "01";
    if (pocetCykluPomocny > 10) {
        pocetCykluPomocnyString = String.valueOf(pocetCykluPomocny);
    } else {
        pocetCykluPomocnyString = "0" + String.valueOf(pocetCykluPomocny);
    }
    textViewPocetCyklu.setText(pocetCykluPomocnyString);

     showTimePickerDialogNastavPocetCyklu.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Dialog dialogNastaveniPoctuCyklu = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));

            VytvoreniDialoguPoctuCyklu.vytvorDialogOpakovani(dialogNastaveniPoctuCyklu, textViewPocetCyklu, souborPolozekCasuKola, vsechnyPolozkyCasyKol);
            dialogNastaveniPoctuCyklu.show();
            vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);
        }
    });

    //nastavení tlačítka kopie celé dlaždice
    LinearLayout showKopirujDlazdiciSouboru = dlazdiceSouboruCasu.findViewById(R.id.showKopirujDlazdiciSouboru);

    showKopirujDlazdiciSouboru.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try {
                SouborPolozekCasuKola kopieSouborPolozekCasuKola = (SouborPolozekCasuKola) souborPolozekCasuKola.clone();
                vlozDalsiSouborPolozek2(kopieSouborPolozekCasuKola, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });


    if (!nactenoZeSouboru) {
        vsechnyPolozkyCasyKol.add(souborPolozekCasuKola); //do hlavní vsechnyPolozkyCasyKol vloží tento soubor časů, ale jen pokud se zrovna nedělá layout z načteného souboru
    }

  //nastavení mínusu - smazání celé této dlaždice
    LinearLayout showVymazDlazdiciSouboru = dlazdiceSouboruCasu.findViewById(R.id.showVymazDlazdiciSouboru);
    showVymazDlazdiciSouboru.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dlazdiceKontejner.removeView(dlazdiceSouboruCasu);
            vsechnyPolozkyCasyKol.remove(souborPolozekCasuKola);
            vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);
        }
    });

    vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);
}

    private void nastavVlozeneLayouty2(View dlazdiceSouboruCasu, MyTime cas, PolozkaCasuKola polozkaCasu) {
        //nastaví layout vložené dlaždice PolozkaCasuKola
        //dlaždice
        LinearLayout showDialogNastavCasDlazdice = dlazdiceSouboruCasu.findViewById(R.id.showDialogNastavCasDlazdice);
        LinearLayout dlazdiceCasu = dlazdiceSouboruCasu.findViewById(R.id.dlazdiceCasu);

        //nastaví se výška dlaždic podle výšky dlaždicePřípravy:
        ViewGroup.LayoutParams params = dlazdiceCasu.getLayoutParams();
        params.height = vyskaDlazdicePripravy;
        dlazdiceCasu.setLayoutParams(params);

        vytvoreniDialoguColoru.nastavColorDlazdiceBezDialogu(polozkaCasu.getColorDlazdice(), dlazdiceCasu);
        Log.i("rozmeryDlazdiceVyska",String.valueOf(params.height));
        Log.i("rozmeryDlazdiceSirka",String.valueOf(params.width));

        Dialog dialogNastaveniCasu = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
        final boolean[] dialogCasuJesteNevytvoren = {true};

        TextView hodnotaCasuTextView;
        hodnotaCasuTextView = (TextView) dlazdiceSouboruCasu.findViewById(R.id.textViewHodnotaCasu);
        hodnotaCasuTextView.setText(vratStringCasUpraveny(cas));

        showDialogNastavCasDlazdice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogCasuJesteNevytvoren[0]) {
                    vytvoreniDialoguCasu.vytvorDialogCasu(dialogNastaveniCasu, getResources().getString(R.string.nadpisNastavCas), hodnotaCasuTextView, cas, polozkaCasu, vsechnyPolozkyCasyKol);
                    dialogCasuJesteNevytvoren[0] = false;
                }

                vytvoreniDialoguColoru.nastavColorDlazdice(polozkaCasu.getColorDlazdice(), dlazdiceCasu, dialogNastaveniCasu);

                dialogNastaveniCasu.show();
            }
        });

        //color dlaždice
        LinearLayout showPickerDialogNastavColorDlazdice = dlazdiceSouboruCasu.findViewById(R.id.showPickerDialogNastavColorDlazdice);

        showPickerDialogNastavColorDlazdice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vytvoreniDialoguColoru.nastavColorDlazdice(polozkaCasu.getColorDlazdice(), dlazdiceCasu, dialogNastaveniCasu);
                vytvoreniDialoguColoru.zobrazNastaveniColoruDlazdice(dlazdiceCasu, polozkaCasu, dialogNastaveniCasu, vsechnyPolozkyCasyKol);
            }
        });

        //zvuk dlaždice
         LinearLayout showPickerDialogNastavZvukDlazdice = dlazdiceSouboruCasu.findViewById(R.id.showPickerDialogNastavZvukDlazdice);
        showPickerDialogNastavZvukDlazdice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //zobrazNastaveniColoruDlazdice("colorDlazdice",dlazdiceCasu, colorDlazdice, dialogNastaveniCasu);
                //    vytvoreniDialoguColoru.zobrazNastaveniColoruDlazdice(dlazdiceCasu, polozkaCasu, dialogNastaveniCasu);
                Dialog dialogNastaveniZvuku;
                dialogNastaveniZvuku = new Dialog(new ContextThemeWrapper(requireContext(), R.style.DialogStyle));
                vytvoreniDialoguZvuku.vytvorDialogNastaveniZvukuCasu(dialogNastaveniZvuku, polozkaCasu, vsechnyPolozkyCasyKol);
                dialogNastaveniZvuku.show();
            }
        });

        //nípis dlaždice
        EditText editTextDlazdice= (EditText) dlazdiceSouboruCasu.findViewById(R.id.editTextNadpisHodnotaCasu);
        editTextDlazdice.setText(polozkaCasu.getNazevCasu());
        editTextDlazdice.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                polozkaCasu.setNazevCasu(editTextDlazdice.getText().toString());
            }
        });
    }

//starý způsob před upravou struktury
/*    private void vlozDalsiSouborPolozek(SouborPolozekCasuKola souborPolozekCasuKolaVlozeny, boolean nactenoZeSouboru) { //vloží soubor časů, kdy první dlaždice času je defaultní, dlaší se dají přiklikat
        View dlazdiceSouboruCasu = getLayoutInflater().inflate(R.layout.dlazdice_souboru_casu, null);

*//*        final ViewStub dlazdiceSouboruCasu = new ViewStub(getActivity().getApplicationContext());
        //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
   //     dlazdiceSouboruCasu.setLayoutParams(layoutParams);
        dlazdiceSouboruCasu.setLayoutResource(R.layout.dlazdice_souboru_casu);*//*

        dlazdiceKontejner.addView(dlazdiceSouboruCasu);
        //      dlazdiceSouboruCasu.inflate();


        SouborPolozekCasuKola souborPolozekCasuKola;
        if (souborPolozekCasuKolaVlozeny == null) {
            //tato dlaždice je nově vytvořena kliknutím na plus
            souborPolozekCasuKola = new SouborPolozekCasuKola();
            //...tak se musí udělat první defaultní PolozkaCasuKola
            MyTime cas = new MyTime(0, 1, 0);
            int colorDlazdice = getResources().getColor(R.color.colorCasCviceni);
            PolozkaCasuKola polozkaCasu = new PolozkaCasuKola(cas, colorDlazdice, 1, false, (getResources().getString(R.string.timeNapis)+" "+String.valueOf(pocitacDlazdic)));
            pocitacDlazdic ++;
            souborPolozekCasuKola.vlozPolozkuCasKola(polozkaCasu);
            nastavVlozeneLayouty(dlazdiceSouboruCasu, cas, polozkaCasu);
        } else {
            //tato dlaždice je vytvořena klonováním dlaždice
            souborPolozekCasuKola = souborPolozekCasuKolaVlozeny;
            nastavVlozeneLayouty(dlazdiceSouboruCasu, souborPolozekCasuKolaVlozeny.vratPolozkuCasKola(0).getTime(), souborPolozekCasuKolaVlozeny.vratPolozkuCasKola(0));


        }

        //nastavení plusu - přidání jednoho času do této dlaždice
        LinearLayout kontejnerVSouboruCasu = dlazdiceSouboruCasu.findViewById(R.id.dlazdiceSouboruCasu);
        LinearLayout showVlozDalsiCasVSouboruLayout = dlazdiceSouboruCasu.findViewById(R.id.showVlozDalsiCasVSouboruLayout);
        showVlozDalsiCasVSouboruLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dlazdiceJednohoCasu = getLayoutInflater().inflate(R.layout.dlazdice_jednoho_casu, null);
                //dlaždice přidaná položka
                MyTime cas2 = new MyTime(0, 1, 0);
                int colorDlazdice2 = getResources().getColor(R.color.colorCasCviceni);
                PolozkaCasuKola polozkaCasu2 = new PolozkaCasuKola(cas2, colorDlazdice2, 1, false, (getResources().getString(R.string.timeNapis)+" "+String.valueOf(pocitacDlazdic)));
                pocitacDlazdic ++;
                nastavVlozeneLayouty(dlazdiceJednohoCasu, cas2, polozkaCasu2); //nastaví layout a klikací dialogy nové dlaždici aneb PolozceCasuKola
                souborPolozekCasuKola.vlozPolozkuCasKola(polozkaCasu2);
                nastavVymazaniDlazdice(dlazdiceJednohoCasu, souborPolozekCasuKola, polozkaCasu2);
                kontejnerVSouboruCasu.addView(dlazdiceJednohoCasu);
                vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);

            }
        });
        //pokud tato metoda byla zavolaná klonováním dlaždice, tak je třeba zobrazit položky času, které jsou v clonovaném SouboruPolozekCasuKola uloženy
        if (souborPolozekCasuKola.getPolozkyCasyKol().size()>1) {
            for (int i=1; i < souborPolozekCasuKola.getPolozkyCasyKol().size(); i++) {
                View dlazdiceJednohoCasu = getLayoutInflater().inflate(R.layout.dlazdice_jednoho_casu, null);
                nastavVlozeneLayouty(dlazdiceJednohoCasu, souborPolozekCasuKola.getPolozkyCasyKol().get(i).getTime(), souborPolozekCasuKola.getPolozkyCasyKol().get(i));
                nastavVymazaniDlazdice(dlazdiceJednohoCasu, souborPolozekCasuKola, souborPolozekCasuKola.getPolozkyCasyKol().get(i));
                kontejnerVSouboruCasu.addView(dlazdiceJednohoCasu);
            }
        }


        //nastavení tlačítka pro výběr počtu cyklů
        LinearLayout showTimePickerDialogNastavPocetCyklu = dlazdiceSouboruCasu.findViewById(R.id.showTimePickerDialogNastavPocetCyklu);
        TextView textViewPocetCyklu = dlazdiceSouboruCasu.findViewById(R.id.textViewPocetCyklu);
        //nastaví do textView počet cyklů, kdyby to náhodou byla jiná hodnota než 1, protože by to byla zkopírovaná dlaždice
        int pocetCykluPomocny = souborPolozekCasuKola.getPocetCyklu();
        String pocetCykluPomocnyString = "01";
        if (pocetCykluPomocny > 10) {
            pocetCykluPomocnyString = String.valueOf(pocetCykluPomocny);
        } else {
            pocetCykluPomocnyString = "0" + String.valueOf(pocetCykluPomocny);
        }
        textViewPocetCyklu.setText(pocetCykluPomocnyString);
        Dialog dialogNastaveniPoctuCyklu = new Dialog(new ContextThemeWrapper(getActivity().getApplicationContext(), R.style.DialogStyle));
        ;
        VytvoreniDialoguPoctuCyklu.vytvorDialogOpakovani(dialogNastaveniPoctuCyklu, textViewPocetCyklu, souborPolozekCasuKola, vsechnyPolozkyCasyKol);
        showTimePickerDialogNastavPocetCyklu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogNastaveniPoctuCyklu.show();
                vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);
            }
        });

        //nastavení tlačítka kopie celé dlaždice
        LinearLayout showKopirujDlazdiciSouboru = dlazdiceSouboruCasu.findViewById(R.id.showKopirujDlazdiciSouboru);

        showKopirujDlazdiciSouboru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("ccc:", "kliknuto");
                try {
                    Log.d("ccc:", "kliknuto2");
                    SouborPolozekCasuKola kopieSouborPolozekCasuKola = (SouborPolozekCasuKola) souborPolozekCasuKola.clone();
                    Log.d("ccc:", "kliknuto3");
                    // vsechnyPolozkyCasyKol.add(kopieSouborPolozekCasuKola);
                    vlozDalsiSouborPolozek(kopieSouborPolozekCasuKola, false);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        if (!nactenoZeSouboru) {
            vsechnyPolozkyCasyKol.add(souborPolozekCasuKola); //do hlavní vsechnyPolozkyCasyKol vloží tento soubor časů, ale jen pokud se zrovna nedělá layout z načteného souboru
        }

        //tady  dlazdiceKontejner.addView(dlazdiceSouboruCasu);


        //nastavení mínusu - smazání celé této dlaždice
        LinearLayout showVymazDlazdiciSouboru = dlazdiceSouboruCasu.findViewById(R.id.showVymazDlazdiciSouboru);
        showVymazDlazdiciSouboru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlazdiceKontejner.removeView(dlazdiceSouboruCasu);
                vsechnyPolozkyCasyKol.remove(souborPolozekCasuKola);
                vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);
            }
        });

        vypocetCelkovehoCasuAZobrezni.spoctiCelkovyCasAZobraz(vsechnyPolozkyCasyKol);
    }

    private void nastavVlozeneLayouty(View dlazdiceSouboruCasu, MyTime cas, PolozkaCasuKola polozkaCasu) {
        //nastaví layout vložené dlaždice PolozkaCasuKola
        //dlaždice
        LinearLayout showDialogNastavCasDlazdice = dlazdiceSouboruCasu.findViewById(R.id.showDialogNastavCasDlazdice);
        LinearLayout dlazdiceCasu = dlazdiceSouboruCasu.findViewById(R.id.dlazdiceCasu);

        Dialog dialogNastaveniCasu;
        dialogNastaveniCasu = new Dialog(new ContextThemeWrapper(getActivity().getApplicationContext(), R.style.DialogStyle));


        TextView hodnotaCasuTextView;
        hodnotaCasuTextView = (TextView) dlazdiceSouboruCasu.findViewById(R.id.textViewHodnotaCasu);
        hodnotaCasuTextView.setText(vratStringCasUpraveny(cas));

        vytvoreniDialoguCasu.vytvorDialogCasu(dialogNastaveniCasu, getResources().getString(R.string.nadpisNastavCas), hodnotaCasuTextView, cas, polozkaCasu, vsechnyPolozkyCasyKol);
        //dlaždice a dialog, kdyby náhodou byla dlaždice předána klonováním, tak bude mít jinou barvu, než dafaultní
        vytvoreniDialoguColoru.nastavColorDlazdice(polozkaCasu.getColorDlazdice(), dlazdiceCasu, dialogNastaveniCasu);

        showDialogNastavCasDlazdice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogNastaveniCasu.show();
            }
        });

        //color dlaždice
        LinearLayout showPickerDialogNastavColorDlazdice = dlazdiceSouboruCasu.findViewById(R.id.showPickerDialogNastavColorDlazdice);

        showPickerDialogNastavColorDlazdice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //zobrazNastaveniColoruDlazdice("colorDlazdice",dlazdiceCasu, colorDlazdice, dialogNastaveniCasu);
                vytvoreniDialoguColoru.zobrazNastaveniColoruDlazdice(dlazdiceCasu, polozkaCasu, dialogNastaveniCasu);
            }
        });

        //zvuk dlaždice
        Dialog dialogNastaveniZvuku;
        dialogNastaveniZvuku = new Dialog(new ContextThemeWrapper(getActivity().getApplicationContext(), R.style.DialogStyle));
        LinearLayout showPickerDialogNastavZvukDlazdice = dlazdiceSouboruCasu.findViewById(R.id.showPickerDialogNastavZvukDlazdice);
        vytvoreniDialoguZvuku.vytvorDialogNastaveniZvukuCasu(dialogNastaveniZvuku, polozkaCasu);

        showPickerDialogNastavZvukDlazdice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //zobrazNastaveniColoruDlazdice("colorDlazdice",dlazdiceCasu, colorDlazdice, dialogNastaveniCasu);
                //    vytvoreniDialoguColoru.zobrazNastaveniColoruDlazdice(dlazdiceCasu, polozkaCasu, dialogNastaveniCasu);
                dialogNastaveniZvuku.show();
            }
        });

        //nípis dlaždice
        EditText editTextDlazdice= (EditText) dlazdiceSouboruCasu.findViewById(R.id.editTextNadpisHodnotaCasu);
        editTextDlazdice.setText(polozkaCasu.getNazevCasu());
        editTextDlazdice.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                polozkaCasu.setNazevCasu(editTextDlazdice.getText().toString());
            }
        });
    }*/





}