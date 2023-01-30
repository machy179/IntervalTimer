package com.machy1979ii.intervaltimer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.machy1979ii.intervaltimer.funkce.PrevodVsechPolozekCasyKolToArrayListPolozkyCasu;
import com.machy1979ii.intervaltimer.models.MyTime;

import angtrim.com.fivestarslibrary.FiveStarsDialog;
import angtrim.com.fivestarslibrary.NegativeReviewListener;
import angtrim.com.fivestarslibrary.ReviewListener;
import com.machy1979ii.intervaltimer.funkce.PraceSeZvukemTabata;
import com.machy1979ii.intervaltimer.models.PolozkaCasuKola;
import com.machy1979ii.intervaltimer.models.SouborPolozekCasuKola;
import com.machy1979ii.intervaltimer.services.CustomService;

import java.util.ArrayList;

public class CustomActivity extends AppCompatActivity implements NegativeReviewListener, ReviewListener {

    //   ReviewManager manager;
    //   ReviewInfo reviewInfo = null;
    private static final String HOME_ACTIVITY_TAG = TabataActivity.class.getSimpleName();


    private boolean pauzaNeniZmacknuta = true;


    private MyTime casCelkovy;

    private int puvodniPocetCyklu;
    private int pocetCyklu;
    private int aktualniCyklus = 1;

    private TextView textViewCas;
    private TextView textViewCasNadpis;
    private TextView textViewAktualniPocetCyklu;
    private TextView textViewAktualniPocetTabat;
    private LinearLayout linearLayoutPauza;
    private LinearLayout dlazdiceOdpocitavace;
    private TextView textViewCelkovyCas;

    private TextView textViewBeziciCasCisloKola;

    private LinearLayout dlazdicePodHlavnimCasem1;
    private LinearLayout dlazdicePodHlavnimCasem2;
    private LinearLayout dlazdicePodHlavnimCasem3;
    private AdView mAdView;


    private CountDownTimer odpocitavac;
    private int dobaCasovace;
    private long pomocny;
    private int velikostCislic;
    private boolean preskocVypisCasu = false;

    private byte stav = 0; //0-priprava, 1-cviceni, 2-pauza, 3-pauza mezi tabatami

//    private MediaPlayer tikZvuk3;
//    private MediaPlayer tikZvuk2;
//    private MediaPlayer tikZvuk1;
//    private MediaPlayer restZvuk;
    //   private MediaPlayer startZvuk;
//    private MediaPlayer fanfareZvuk;

    //nastavení zvuků

    private int zvukCountdown=1;
    private int zvukCelkovyKonec=1;
    private MediaPlayer mediaPlayer = null;
    private int hlasitost = 100;
    private int maxHlasitost = 100;
    float volume;

    private ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol = new ArrayList<SouborPolozekCasuKola>();
    private ArrayList<PolozkaCasuKola> polozkyCasyKol = new ArrayList<PolozkaCasuKola>();
    private int colorSpodnichDlazdic;
    private PolozkaCasuKola aktualniPolozkaCasu;
    private int pocitadloPolozekCasu = 0;

    private boolean jeKonecOdpocitavani = false;

    //proměnné pro servicu
    private Boolean bound = false;
    private Boolean spustenoZPozadi = false; //příznak, že aplikace byla spuštěna z pozadí a ne z notifikace
    private Intent service;
    private CustomService s;
    private ServiceConnection serviceConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            CustomService.MyBinder b= (CustomService.MyBinder) binder;
            if (s==null) {
                s = b.getService();
            } else {
             }
            bound = true;

            Log.d("Servica1","onServiceConnected ---");

            if((s.getResult()!=0)) {
                //v momentě, kdy se connectne k service, tak se z ní čačte result, pokud je 0, tak je vypnutá, nebo nespuštěná, pokud je nějaké číslo
                //tak se z ní to číslo načte a začne se odpočítávat od toho čísla, sem dám i načtení dalších dat ze servisy
                //načtení ze servisy jsem se pokoušel dávat do onCreata, onResume, onRestart atd., ale připojení k service má asi nějaké nepatrné zpoždění, tak to fungovalo jen tady

                //ZDE SE NAČÍTÁ, KDYŽ UŽIVATEL KLIKNE NA NOTIFIKACI
                //      Toast.makeText(ClassicActivity.this, "from service nacte", Toast.LENGTH_SHORT).show();
                Log.d("ServicaZPozadi","s.getResult()!=0");
                Log.d("Servica1","onServiceConnected ---");
                nactiZeServisy();
            } else Log.d("ServicaZPozadi","s.getResult()==0");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Servica1","onServiceDISconnected ---");
            s = null;
            bound = false;
            Toast.makeText(CustomActivity.this, "onServiceDisconnected", Toast.LENGTH_SHORT).show();
        }

    };


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SSS","called to cancel service-Activity");
            //ve skutečnosti to úplně nekillne aplikaci, ale vypne to jen tuto aktivitu, plus service a notifikaci,
            //a pokud se uživatel mrkne na spuštěné aplikace, tak uvidí tuto aplikaci. Po kliknutí to skočí do MainActivity
            //prý by se nemělo killovat aplikaci programově, takhle podobně to má jiný timer v Google Play
            finish();

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(mMessageReceiver, new IntentFilter("znicCustomActivityACustomService")); //pridat pro service


        //zamezí vypnutí obrazovky do úsporného režimu po nečinnosti, šlo to udělat
        //v XML -  android:keepScreenOn="true", ale to bych to musel dát do všech XML (land...)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //tohle tady je, aby statusbar měl určitou barvu, jako barva pozadí reklamy, nešlo mi to udělat v XML lajoutu, tak to řeším takhle
        statusBarcolor();


        vsechnyPolozkyCasyKol = getIntent().getExtras().getParcelableArrayList("vsechnyPolozkyCasyKol");
        polozkyCasyKol = PrevodVsechPolozekCasyKolToArrayListPolozkyCasu.vratArrayPolozekCasyKol(vsechnyPolozkyCasyKol);
        puvodniPocetCyklu = polozkyCasyKol.size()-1;

        colorSpodnichDlazdic = getIntent().getExtras().getInt("colorSpodnichDlazdic"); //color
        casCelkovy = getIntent().getExtras().getParcelable("casCelkovy");
        zvukCountdown=getIntent().getExtras().getInt("zvukcountdown");
        zvukCelkovyKonec=getIntent().getExtras().getInt("zvukcelkovykonec");

        udelejLayout();

        hlasitost = getIntent().getIntExtra("hlasitost", 100);
        volume = (float) (1 - (Math.log(maxHlasitost - hlasitost) / Math.log(maxHlasitost)));

        mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukCountdownPodlePozice(zvukCountdown));

        spustCasovac();
    }



    private void spustCasovac() {
        pocetCyklu = puvodniPocetCyklu;
        aktualniPolozkaCasu = polozkyCasyKol.get(pocitadloPolozekCasu);


        //nastavení časovače - prostě to bude odpočítávat
         dobaCasovace = 100000; //nebudeme to počítat, ale pojede to tolik sekund
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
            //color
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            shape.setColor(aktualniPolozkaCasu.getColorDlazdice());
            dlazdiceOdpocitavace.setBackground(shape);

        } else  dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasPripravy));

        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus)+"/"+String.valueOf(puvodniPocetCyklu));
        //zapíše čas cvičení
        if (aktualniPolozkaCasu.getTime().getSec() < 10) {
            textViewAktualniPocetTabat.setText(String.valueOf(aktualniPolozkaCasu.getTime().getMin()) + ":0" + String.valueOf(aktualniPolozkaCasu.getTime().getSec()));
        } else {
            textViewAktualniPocetTabat.setText(String.valueOf(aktualniPolozkaCasu.getTime().getMin()) + ":" + String.valueOf(aktualniPolozkaCasu.getTime().getSec()));
        }

        pomocny = aktualniPolozkaCasu.getTime().getSec()+1 + aktualniPolozkaCasu.getTime().getMin()*60 +aktualniPolozkaCasu.getTime().getHour()*3600;
        if (pomocny < 60) {
            textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniDva));
            velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;
        }

        odectiAZobrazCelkovyCas();
        spustOdpocitavac();

    }

    @Override
    public void onNegativeReview(int stars) {

    }

    @Override
    public void onReview(int stars) {

    }


    //odpočítavač
    public class OdpocitavacCasu extends CountDownTimer {
        public OdpocitavacCasu(long startTime, long interval) {
            super(startTime, interval);
        }


        @SuppressLint("ResourceType")
        @Override
        public void onTick(long l) {


            if (pauzaNeniZmacknuta) {


                if (!(aktualniPolozkaCasu == polozkyCasyKol.get(0))) {
                    odectiAZobrazCelkovyCas();

                }
                pomocny = pomocny-1;
                         {
                            nastavCislice(pomocny);
                            switch ((int) pomocny){
                                case 4:
                                    //musel jsem tam dát ještě při odpočítávání falešný tik na 4té sekundě, protože u pomalejších zařízeních mi
                                    //ten třetí tik přehrával pomalu (když několik sekund před tím nebyl zvuk spuštěn) a než se stihnul přehrát, tak už to bylo na dvojce, tak jsem sem dal
                                    //falešný 4 tik, který načtu, spustím a ihned vypnu. U dalších tiků problém nebyl. Nevím proč, ale nějak MediaPlayer jak není několik sekund
                                    //spuštěn, tak pak se načítá pomalu a v pomalejších zařízeních je to znát
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukCountdownPodlePozice(zvukCountdown));
                                    //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukStartStopPodlePozice(zvukStart));
                                    mediaPlayer.setVolume(volume,volume);
                                    mediaPlayer.start();
                                    mediaPlayer.stop();
                                    break;
                                case 3:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukCountdownPodlePozice(zvukCountdown));
                                    //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukStartStopPodlePozice(zvukStart));
                                    mediaPlayer.setVolume(volume,volume);
                                    mediaPlayer.start();
                                    break;
                                case 2:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume,volume);
                                    mediaPlayer.start();
                                    break;
                                case 1:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume,volume);
                                    mediaPlayer.start();
                                    break;

                                case 0: //dojelo to do konce, tak je třeba změnit položku času kola, která se bude nyní provádět
                                    //tady   restZvuk.start();
                                    //  PraceSeZvukemTabata.spustZvukStartStop(getApplicationContext(),zvukStart);
                                    //nakonec jsem to musel vyřešit takhle, chtěl jsem spouštět zvuk přímo ve třídě PraceSeZvukemTabata, ale po přehrání pár zvuků
                                    //to přestalo přehrávat zvuky, tak jsem to udělal takhle, navíc jsem to pořešil reset() a release(), našel jsem to v nějakém návodu
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                        mediaPlayer = null;
                                    }

                                    //je konec odpočítávání předchozí polozkyCasu, tak se musí v tomto vláknu nastavit nová polozkaCasu
                                    pocitadloPolozekCasu ++;
                                    if(!(pocitadloPolozekCasu==polozkyCasyKol.size()))  { //zjistí, zda není celkový konec odpočítávání
                                    aktualniCyklus = pocitadloPolozekCasu;
                                    textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus)+"/"+String.valueOf(puvodniPocetCyklu));
                                    aktualniPolozkaCasu = polozkyCasyKol.get(pocitadloPolozekCasu);

                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukStartStopPodlePozice(aktualniPolozkaCasu.getZvuk()));
                                    mediaPlayer.setVolume(volume,volume);
                                    mediaPlayer.start();


                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                                        //color
                                        GradientDrawable shape =  new GradientDrawable();
                                        shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                        shape.setColor(aktualniPolozkaCasu.getColorDlazdice());
                                        dlazdiceOdpocitavace.setBackground(shape);
                                        //color
                                    } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasCviceni));

                                    //zapíše čas cvičení
                                    if (aktualniPolozkaCasu.getTime().getSec() < 10) {
                                        textViewAktualniPocetTabat.setText(String.valueOf(aktualniPolozkaCasu.getTime().getMin()) + ":0" + String.valueOf(aktualniPolozkaCasu.getTime().getSec()));
                                    } else {
                                        textViewAktualniPocetTabat.setText(String.valueOf(aktualniPolozkaCasu.getTime().getMin()) + ":" + String.valueOf(aktualniPolozkaCasu.getTime().getSec()));
                                    }

                                    textViewCasNadpis.setText(aktualniPolozkaCasu.getNazevCasu());

                                    pomocny=aktualniPolozkaCasu.getTime().getSec() +  aktualniPolozkaCasu.getTime().getMin()*60 +  aktualniPolozkaCasu.getTime().getHour()*3600 + 1;
                                    pomocny = pomocny -1; //protože na začátku už to je jedna sekunda a pak mi to při celkovém času nehrálo

                                        nastavStartKola(aktualniPolozkaCasu.getNazevCasu());
                                  //  nastavCislice(pomocny);
                            } else { //je celkový konec odpočítávání
                                        jeKonecOdpocitavani = true;
                                      nastaveniPozadiKonce();

                                        mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukKonecPodlePozice(zvukCelkovyKonec));
                                        mediaPlayer.setVolume(volume,volume);
                                        mediaPlayer.start();
                                        odpocitavac.cancel();

                                        zavlojejReviewNejake();
                                    }


                                default:
                                    break;
                            }

                        }



            }
        }

        @Override
        public void onFinish() {
            textViewCas.setText("0");
            textViewBeziciCasCisloKola.setText("");
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukCountdownPodlePozice(zvukCelkovyKonec));
            mediaPlayer.setVolume(volume,volume);
            mediaPlayer.start();
        }
    }

    @SuppressLint("ResourceAsColor")
    private void nastaveniPozadiKonce() {
        textViewBeziciCasCisloKola.setText("");
        textViewCas.setText(R.string.konec);
        textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
        textViewCasNadpis.setText("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);

            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            shape.setColor(getBaseContext().getResources().getColor(R.color.colorKonecTabaty));
            dlazdiceOdpocitavace.setBackground(shape);
            Log.i("color", "111");
            //color
        } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorKonecTabaty));
        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus)+"/"+String.valueOf(puvodniPocetCyklu));
        Log.i("color", "222");
        //zapíše čas cvičení - kola
        if (aktualniPolozkaCasu.getTime().getSec() < 10) {
            textViewAktualniPocetTabat.setText(String.valueOf(aktualniPolozkaCasu.getTime().getMin()) + ":0" + String.valueOf(aktualniPolozkaCasu.getTime().getSec()));
        } else {
            textViewAktualniPocetTabat.setText(String.valueOf(aktualniPolozkaCasu.getTime().getMin()) + ":" + String.valueOf(aktualniPolozkaCasu.getTime().getSec()));
        }

        textViewCelkovyCas.setText("00:00");
    }

    private void odectiAZobrazCelkovyCas() {
        zobrazCelkovyCas();
        if (casCelkovy.getHour()==0 & casCelkovy.getMin()==0 & casCelkovy.getSec()==0) {

        } else {
            if (casCelkovy.getSec()==0) {
                casCelkovy.setMin(casCelkovy.getMin()-1);
                casCelkovy.setSec(59);
            } else {
                casCelkovy.setSec(casCelkovy.getSec()-1);
            }
        }

    }

    private void zobrazCelkovyCas() {
        int hodiny = casCelkovy.getHour();
        String hodinyString;
        int minuty= casCelkovy.getMin();
        String minutyString;
        int sekundy= casCelkovy.getSec();
        String sekundyString;

        if (hodiny==0) {
            hodinyString = "";
        }  else {
            hodinyString = String.valueOf(hodiny)+":";
        }

        if (minuty<10) {
            minutyString = "0"+String.valueOf(minuty);
        } else {
            minutyString = String.valueOf(minuty);
        }

        if (sekundy<10) {
            sekundyString = "0"+String.valueOf(sekundy);
        } else {
            sekundyString = String.valueOf(sekundy);
        }

        textViewCelkovyCas.setText(hodinyString+minutyString+":"+sekundyString);
    }


    private void spustOdpocitavac() {
        odpocitavac = new OdpocitavacCasu((dobaCasovace)*1000, 1000);
        odpocitavac.start();
    }

    public void showPauseDialog(View v) {
        if (pauzaNeniZmacknuta)  {
            pauzaNeniZmacknuta = false;
            // textViewPauza.setText(">");
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                linearLayoutPauza.setBackgroundResource(R.mipmap.playlezatotabataactivity);
            } else {
                linearLayoutPauza.setBackgroundResource(R.mipmap.playstojatotabataactivity);
            }
            //       linearLayoutPauza.setBackgroundResource(R.mipmap.playlezatotabataactivity);
            vytvorToast(getResources().getString(R.string.pauza));

        } else {
            pauzaNeniZmacknuta = true;
            //    textViewPauza.setText("||");
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                linearLayoutPauza.setBackgroundResource(R.mipmap.pausestojatotabataactivity);
            } else {
                linearLayoutPauza.setBackgroundResource(R.mipmap.pauselezatotabataactivity);
            }
            //        linearLayoutPauza.setBackgroundResource(R.mipmap.pauselezatotabataactivity);
            vytvorToast(getResources().getString(R.string.pokracovat));
        }


    }

    private void zavlojejReviewNejake() {

        zavolejTretiZpusob();
    }


    //metoda, která u numperpickeru udělá ty dvě čárečky jinou barvou, než defaultní - jinak to nešlo
    private void setDividerColor (NumberPicker picker) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(picker, getResources().getDrawable(R.color.colorPisma));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        //}
    }

    private void nastavCislice (long hodnotaAktualni) {
        if (hodnotaAktualni < 60) {
            if (velikostCislic != (R.dimen.velikostCasuOdpocitavaniDva)) {
                textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniDva));
                velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;

            }

            textViewCas.setText(vratDeseticisla((int) pomocny));
        } else {
            if (velikostCislic != (R.dimen.velikostCasuOdpocitavaniCtyri)) {
                textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniCtyri));
                velikostCislic = R.dimen.velikostCasuOdpocitavaniCtyri;
            }
            int minuty = (int) (hodnotaAktualni%60);
            textViewCas.setText((vratDeseticisla((int) ((pomocny-minuty)/60))) + ":"+vratDeseticisla(minuty));
        }
        textViewBeziciCasCisloKola.setText("");

    }

    private void nastavStartKola (String hodnotaNazvuKola) {


        textViewCas.setText(hodnotaNazvuKola);
        if (aktualniPolozkaCasu.getPoradiVCyklu()>1)  {
            textViewBeziciCasCisloKola.setText(String.valueOf(aktualniPolozkaCasu.getPoradiVCyklu()));
        }


    }

    private String vratDeseticisla (int cislo) {
        if (cislo < 10) {
            return ("0" + String.valueOf(cislo));
        } else
            return String.valueOf(cislo);
    }

    //metoda, která zjistí otočení displaye a potom se znovu nastaví celý layout
    //jinak mi to nešlo udělat, aby po otočení displaye se activita znovu nerestartovala a tím pádem by nebyly uloženy hodnoty
    //taky to řeší multiscreen
    //k tomu ještě je třeba dát do manifestu:
    //android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation" (do activity v manifestu)
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        udelejLayout();
        nastavCislice(pomocny);

                //tady se popasuje s tím, že když je to v pauze, aby to vykreslilo PLAY a čas do spodní prostřední dlaždice
        if (!pauzaNeniZmacknuta)  {

            // textViewPauza.setText(">");
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                linearLayoutPauza.setBackgroundResource(R.mipmap.playlezatotabataactivity);
            } else {
                linearLayoutPauza.setBackgroundResource(R.mipmap.playstojatotabataactivity);
            }
            zobrazCelkovyCas();

        }

        if (jeKonecOdpocitavani) {
            nastaveniPozadiKonce();
        } else {

        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus)+"/"+String.valueOf(puvodniPocetCyklu));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
            //color
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            shape.setColor(aktualniPolozkaCasu.getColorDlazdice());
            dlazdiceOdpocitavace.setBackground(shape);
            //color
        } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasCviceni));
        textViewCasNadpis.setText(aktualniPolozkaCasu.getNazevCasu());
        //zapíše čas cvičení
        if (aktualniPolozkaCasu.getTime().getSec() < 10) {
            textViewAktualniPocetTabat.setText(String.valueOf(aktualniPolozkaCasu.getTime().getMin()) + ":0" + String.valueOf(aktualniPolozkaCasu.getTime().getSec()));
        } else {
            textViewAktualniPocetTabat.setText(String.valueOf(aktualniPolozkaCasu.getTime().getMin()) + ":" + String.valueOf(aktualniPolozkaCasu.getTime().getSec()));
        }

        }


    }


    private void udelejLayout() {

        setContentView(R.layout.activity_custom);

        // reklama Google
        String idAplikace = "ca-app-pub-6701702247641250~7047640994";
        MobileAds.initialize(getApplicationContext(), idAplikace);
 /*       MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });*/
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //   zavolejReview();

        textViewCas  = (TextView) findViewById(R.id.textViewBeziciCas);
        textViewCasNadpis = (TextView) findViewById(R.id.textViewBeziciCasNadpis);
        textViewAktualniPocetCyklu = (TextView) findViewById(R.id.textViewAktualniPocetCyklu);
        textViewAktualniPocetTabat = (TextView) findViewById(R.id.textViewAktualniPocetTabat);
        dlazdiceOdpocitavace = (LinearLayout) findViewById(R.id.dlazdiceHlavniCas);
        textViewBeziciCasCisloKola = (TextView) findViewById(R.id.textViewBeziciCasCisloKola);


        dlazdicePodHlavnimCasem1 = (LinearLayout) findViewById(R.id.dlazdicePodHlavnimCasem1);
        dlazdicePodHlavnimCasem2 = (LinearLayout) findViewById(R.id.dlazdicePodHlavnimCasem2);
        dlazdicePodHlavnimCasem3 = (LinearLayout) findViewById(R.id.dlazdicePodHlavnimCasem3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dlazdicePodHlavnimCasem1.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundmodrykulaterohy));//  (R.drawable.background);
            dlazdicePodHlavnimCasem2.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundmodrykulaterohy));//  (R.drawable.background);
            dlazdicePodHlavnimCasem3.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundmodrykulaterohy));//  (R.drawable.background);

            GradientDrawable pozadi1 =  new GradientDrawable();
            pozadi1.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            pozadi1.setColor(colorSpodnichDlazdic);
            GradientDrawable pozadi2 =  new GradientDrawable();
            pozadi2.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            pozadi2.setColor(colorSpodnichDlazdic);
            GradientDrawable pozadi3 =  new GradientDrawable();
            pozadi3.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            pozadi3.setColor(colorSpodnichDlazdic);
            GradientDrawable pozadi4 =  new GradientDrawable();
            //   pozadi4.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            //   pozadi4.setColor(colorDlazdicePocetCyklu);
            //color
            dlazdicePodHlavnimCasem1.setBackground(pozadi1);
            dlazdicePodHlavnimCasem2.setBackground(pozadi2);
            dlazdicePodHlavnimCasem3.setBackground(pozadi3);

        }

        //      textViewPauza = (TextView) findViewById(R.id.textViewPauza);

        linearLayoutPauza = findViewById(R.id.linearLayoutPauza);
        textViewCelkovyCas = (TextView) findViewById(R.id.textViewCelkovyCas);
        velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;


        //vytvoření dialogu počet tabat
   //     dialogPoctuTabat = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
   //     vytvorDialogOpakovani(dialogPoctuTabat, getResources().getString(R.string.nadpisNastavPocetTabat));

        //vytvoření dialogu počet cyklů
    //    dialogPocetCyklu = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
    //    vytvorDialogOpakovani(dialogPocetCyklu, getResources().getString(R.string.nadpisNastavPocetCyklu));



    }

    private void vytvorToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

     private void zavolejTretiZpusob() {
        FiveStarsDialog fiveStarsDialog = new FiveStarsDialog(this,"machy79@seznam.cz"); //...ale poslání na e-mail mám vypnuté
        //     FiveStarsDialog fiveStarsDialog = new FiveStarsDialog(this);
        fiveStarsDialog.setRateText("Will you review app?...5 stars please:-)?")
                .setTitle("Review app")
// .setContentTextVisibility(View.GONE) // posibility to hide subtitle
                .setForceMode(false)
                .setStarColor(Color.YELLOW)
                //  .setEmailChooserText("Send email...")
                .setPositiveText("OK")
                .setNegativeText("NOT NOW")
                .setNeutralText("NEVER")
                .setNeutralButtonColor(Color.parseColor("#CF0000"))
                .setNegativeButtonColor(Color.parseColor("#E67220"))
                .setPositiveButtonColor(Color.parseColor("#24AD02"))
                .setButtonTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL))
                .setContentTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL))
                .setStarColor(Color.parseColor("#ffee32"))
                .setNoRatingSelectedText("Please select your rating first.")
                .setUpperBound(5) // Market opened if a rating >= 2 is selected
                .setNegativeReviewListener(CustomActivity.this) // OVERRIDE mail intent for negative review
                //            .setReviewListener(this) // Used to listen for reviews (if you want to track them )
                .showAfter(7); //počet spuštění, aby se žádost o review spustila (je to na konci v END)
    }



    @Override

    protected void onPause() {

        super.onPause();//invisible

        Log.d(HOME_ACTIVITY_TAG, "Activity paused");
        onResume();

        CustomActivity.this.onResume();

    }

    public void statusBarcolor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(CustomActivity.this,R.color.colorReklama));
        }
    }

    /** Called when the activity has become visible. */
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override //pridat pro service
    protected void onStart() {
        super.onStart();
        //      Toast.makeText(ClassicActivity.this, "onStart", Toast.LENGTH_SHORT).show();
        //níže uvedené musí být tady, protože když se to sem vrací z onRestart, to znamená, že uživatel se vlrátí na aktivitu, tak kdyby to níže uvedené bylo v onCreate, tak by to dělalo neplechu
        Log.d("ServicaZPozadi","onStart");

        Intent intent = new Intent(this, CustomService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        service = new Intent(getApplicationContext(), CustomService.class);


    }

    @Override //pridat pro service
    protected void onRestart() { //tímhle to projde, když se uživatel vrátí do aplikace z pozadí a klikne na aplikaci nebo když klikne na notifikaci
        super.onRestart();
        //     Toast.makeText(ClassicActivity.this, "onRestart", Toast.LENGTH_SHORT).show();
        Log.d("ServicaZPozadi","onRestart");
        if((s.getResult()!=0)) {
            Log.d("ServicaZPozadi","onRestart2");
            //ZDE SE NAČÍTÁ, KDYŽ UŽIVATEL KLIKNE NA APLIKACI Z POZADÍ
            //      bound = true;
            spustenoZPozadi = true; //tahle proměnná je tady proto, že když se to neověřovalo, zda se apka spouští z pozadí nebo naopak z notifikace,
            //tak se killservice spustilo dvakrát a dělalo to samozřejmě neplechu
            nactiZeServisy();
            if (jeKonecOdpocitavani) {
                nastaveniPozadiKonce();
            } else spustOdpocitavac();
        } else Log.d("ServicaZPozadi","onRestart3");
    }

    @Override //pridat pro service
    protected void onStop() {//když uživatel dá aplikaci do pozadí, tak teprve potom se spustí servica a nastaví se v service odpočítávání, sem dám asi všechny proměnné
        super.onStop();
        //když máme service connection, tak se nemusí startovat servica, ta už je inicializovaná, stačí v ní jen vyvolat metody


        s.nastavHodnoty(aktualniCyklus, puvodniPocetCyklu, casCelkovy,
                stav,colorSpodnichDlazdic, pomocny, pauzaNeniZmacknuta,
                pocetCyklu, polozkyCasyKol, aktualniPolozkaCasu, pocitadloPolozekCasu,
                jeKonecOdpocitavani);
        s.nastavOdpocitavani();

        s.nastavZvuky(zvukCelkovyKonec,
                zvukCountdown, hlasitost, maxHlasitost, volume);

        s.setNotification();


        //nakonec musím spustit startForegroundService, aby se v service mohla spustit metoda onStartCommand, ve které je return START_NOT_STICKY - to tady je proto,
        //aby se po uvedení telefonu po vypnutí tato servica po cca 1 minutě nekillnula
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service);
        } else
            this.startService(service);

        odpocitavac.cancel();

    }


    @Override
    protected void onDestroy() {
        odpocitavac.cancel();
        znicService();
        unregisterReceiver(mMessageReceiver);

        super.onDestroy();
    }

    //metody pro service
    private void nactiZeServisy() {


        aktualniPolozkaCasu = s.getAktualniPolozkaCasu();
        pocitadloPolozekCasu = s.getPocitadloPolozekCasu();
        jeKonecOdpocitavani = s.getJeKonecOdpocitavani();
        preskocVypisCasu = s.getPreskocVypisCasu();
        casCelkovy = s.getCasCelkovy();
        pocetCyklu = s.getPocetCyklu();
        pauzaNeniZmacknuta = s.getPauzaNeniZmacknuta();
        aktualniCyklus = s.getAktualniCyklus();
        puvodniPocetCyklu = s.getPuvodniPocetCyklu();
        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus)+"/"+String.valueOf(puvodniPocetCyklu));


        stav = s.getStav();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
            //color
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            shape.setColor(aktualniPolozkaCasu.getColorDlazdice());
            dlazdiceOdpocitavace.setBackground(shape);
            //color
        } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasCviceni));

        if (aktualniPolozkaCasu.getTime().getSec() < 10) {
            textViewAktualniPocetTabat.setText(String.valueOf(aktualniPolozkaCasu.getTime().getMin()) + ":0" + String.valueOf(aktualniPolozkaCasu.getTime().getSec()));
        } else {
            textViewAktualniPocetTabat.setText(String.valueOf(aktualniPolozkaCasu.getTime().getMin()) + ":" + String.valueOf(aktualniPolozkaCasu.getTime().getSec()));
        }

        if (pauzaNeniZmacknuta)  {
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                linearLayoutPauza.setBackgroundResource(R.mipmap.pausestojatotabataactivity);
            } else {
                linearLayoutPauza.setBackgroundResource(R.mipmap.pauselezatotabataactivity);
            }
        } else {
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                linearLayoutPauza.setBackgroundResource(R.mipmap.playlezatotabataactivity);
            } else {
                linearLayoutPauza.setBackgroundResource(R.mipmap.playstojatotabataactivity);
            }
        }


        pomocny = s.getPomocny();
        nastavCislice(pomocny);

        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus)+"/"+String.valueOf(puvodniPocetCyklu));
        //zapíše čas cvičení
        if (aktualniPolozkaCasu.getTime().getSec() < 10) {
            textViewAktualniPocetTabat.setText(String.valueOf(aktualniPolozkaCasu.getTime().getMin()) + ":0" + String.valueOf(aktualniPolozkaCasu.getTime().getSec()));
        } else {
            textViewAktualniPocetTabat.setText(String.valueOf(aktualniPolozkaCasu.getTime().getMin()) + ":" + String.valueOf(aktualniPolozkaCasu.getTime().getSec()));
        }

        zobrazCelkovyCas();

        if(spustenoZPozadi) { //když to sem projde z pozadí a ne jen z notifikace, tak je vlastně tato activita
            //načtena a je potřebe jen servisu killnout a ne ji zcela zničit, je potřeba si na ni nechat connection, tak proto je zde tato podmínka
            Log.d("ServicaZPozadi","spustenoZPozadi=true");
            s.killService();
            spustenoZPozadi = false;
        } else znicService();
    }

    private void znicService() { //aby šlo servicu zničit, musí se unbindnout a vlákno v ní, tedy počítadlo, zničit také
        if (bound) {
            Log.d("Servica1","2");
            //      Toast.makeText(ClassicActivity.this, "unbind", Toast.LENGTH_SHORT).show();
            unbindService(serviceConnection);
            getApplicationContext().stopService(service);
            bound = false;
            s.killService();
            Log.d("Servica1","3");

        }
    }


/*    @Override
    public void onBackPressed(){
        //protože v manifestu mám nastaveno android:noHistory="true", tak při zpětném buttonu by se apka ukončila a neskočila by do mainu,
        //proto jsem tady zpětný button přetypoval, aby při jeho stisknutí to skočilo do mainu
        super.onBackPressed();
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.putExtra("zobrazIntent",2);
        startActivity(mainActivity);
        finish();
    }*/

}
