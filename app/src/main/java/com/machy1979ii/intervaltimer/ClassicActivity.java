package com.machy1979ii.intervaltimer;

import static com.machy1979ii.intervaltimer.funkce.Design_preferencesKt.getDesignPreferences;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.machy1979ii.intervaltimer.funkce.AdUtils;
import com.machy1979ii.intervaltimer.funkce.PraceSeZvukem;
import com.machy1979ii.intervaltimer.funkce.VibratorTimer;
import com.machy1979ii.intervaltimer.models.MyTime;
import com.machy1979ii.intervaltimer.services.ClassicService;
import com.machy1979ii.intervaltimer.services.TimerAnalytics;

import angtrim.com.fivestarslibrary.FiveStarsDialog;
import angtrim.com.fivestarslibrary.NegativeReviewListener;
import angtrim.com.fivestarslibrary.ReviewListener;
import pl.pawelkleczkowski.customgauge.CustomGauge;

public class ClassicActivity extends AppCompatActivity implements NegativeReviewListener, ReviewListener {

    //   ReviewManager manager;
    //   ReviewInfo reviewInfo = null;
    //private static final String HOME_ACTIVITY_TAG = ClassicActivity.class.getSimpleName();

    private Dialog dialogNastaveniCasuKola;
    private Dialog dialogPocetCyklu;
    private boolean pauzaNeniZmacknuta = true;

    private MyTime casPripravy;
    private int colorDlazdiceCasPripravy; //color
    private MyTime casCviceni;
    private int colorDlazdiceCasCviceni; //color
    private MyTime casPauzy;
    private int colorDlazdiceCasPauzy; //color
    private MyTime casMezitabatami = new MyTime(0, 0, 0);
    private MyTime casCoolDown = new MyTime(0, 0, 0);
    private MyTime casCelkovy;

    private int pocetTabat = 1;
    private int colorDlazdicePocetCyklu; //color
    private int aktualniTabata = 1;
    private int puvodniPocetTabat = 1;
    private int puvodniPocetCyklu = 8;
    private int aktualniCyklus = 1;
    private int pocetCyklu;
    //private int pauzaMeziTabatami;

    private TextView textViewCas;
    private TextView textViewCasNadpis;
    private TextView textViewAktualniPocetCyklu;
    private TextView textViewAktualniPocetTabat;
    //    private TextView textViewPauza;
    private LinearLayout linearLayoutPauza;
    private LinearLayout dlazdiceOdpocitavace;
    private TextView textViewCelkovyCas;
    private TextView textViewBeziciCasCisloKola;

    private LinearLayout dlazdicePodHlavnimCasem1;
    private LinearLayout dlazdicePodHlavnimCasem2;
    private LinearLayout dlazdicePodHlavnimCasem3;


    private CountDownTimer odpocitavac;
    private int dobaCasovace;
    private long pomocny;  //čas v konkrétním stavu
    private int velikostCislic;
    private boolean preskocVypisCasu = false;

    private byte stav = 0; //0-priprava, 1-cviceni, 2-pauza, 3-pauza mezi tabatami

    //nastavení zvuků
    private int zvukStart = 1;
    private int zvukStop = 1;
    private int zvukCelkovyKonec = 1;
    private int zvukCountdown = 1;
    private int zvukPulkaCviceni = 49; //49 je, když není nastaven zvuk
    private int casPulkyKola = 0; //pokud uživatel nebude chtít, aby v půlce kola byl nějaký zvuk, tak hodnota bude 0
    private int casPulkyKolaAktualni = 0; //je potřeba ještě tuto proměnnou, protože když nastavím jinou délku kola zrovna v kole, tak by to habrovalo
    private int zvukPredkoncemKola = 49; //49 je, když není nastaven zvuk
    private int casZvukuPredKoncemKola = 20; //pokud uživatel nebude chtít, aby v půlce kola byl nějaký zvuk, tak hodnota bude 0


    private MediaPlayer mediaPlayer = null;

    private int hlasitost = 100;
    private int maxHlasitost = 100;
    float volume;

    private boolean buttonBackIsNotPressed = true;

    //proměnné pro servicu
    private Boolean bound = false;
    private Boolean spustenoZPozadi = false; //příznak, že aplikace byla spuštěna z pozadí a ne z notifikace
    private Intent service;
    private ClassicService s;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            ClassicService.MyBinder b = (ClassicService.MyBinder) binder;
            if (s == null) {
                //       Toast.makeText(ClassicActivity.this, "Connected s=null", Toast.LENGTH_SHORT).show();
                s = b.getService();
            } else {
                //     Toast.makeText(ClassicActivity.this, "Connected s!=null", Toast.LENGTH_SHORT).show();
            }


            bound = true;

            Log.d("Servica1", "onServiceConnected ---");

            if ((s.getResult() != 0)) {
                //v momentě, kdy se connectne k service, tak se z ní čačte result, pokud je 0, tak je vypnutá, nebo nespuštěná, pokud je nějaké číslo
                //tak se z ní to číslo načte a začne se odpočítávat od toho čísla, sem dám i načtení dalších dat ze servisy
                //načtení ze servisy jsem se pokoušel dávat do onCreata, onResume, onRestart atd., ale připojení k service má asi nějaké nepatrné zpoždění, tak to fungovalo jen tady

                //ZDE SE NAČÍTÁ, KDYŽ UŽIVATEL KLIKNE NA NOTIFIKACI
                //      Toast.makeText(ClassicActivity.this, "from service nacte", Toast.LENGTH_SHORT).show();
                Log.d("Servica1", "onServiceConnected");
                Log.d("ServicaZPozadi", "s.getResult()!=0");

                nactiZeServisy();
            } else Log.d("ServicaZPozadi", "s.getResult()==0");

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            s = null;
            bound = false;
            Toast.makeText(ClassicActivity.this, "onServiceDisconnected", Toast.LENGTH_SHORT).show();
        }

    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SSS", "called to cancel service-Activity");
            //ve skutečnosti to úplně nekillne aplikaci, ale vypne to jen tuto aktivitu, plus service a notifikaci,
            //a pokud se uživatel mrkne na spuštěné aplikace, tak uvidí tuto aplikaci. Po kliknutí to skočí do MainActivity
            //prý by se nemělo killovat aplikaci programově, takhle podobně to má jiný timer v Google Play
            finish();

        }

    };

    //new design
    private boolean newLayout = true;
    private int vybranyDesign = 3;
    private CustomGauge progressBar;
    private int maxHodnotaProgressBar = 0;
    private boolean nastavProgressBar = false;
    private boolean konecOdpocitavani = false;

    private boolean jeKonecOdpocitavani = false;


    //adaptivní banner
    private static final String AD_UNIT_ID = "ca-app-pub-6701702247641250/5801491018";
    private AdView adView;
    private FrameLayout adContainerView;
    private boolean initialLayoutComplete = false;

    //Google Billing
    private Boolean adsDisabled = false; //když si uživatel koupil aplikaci a reklamy jsou zakázány

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //pro zajištění, aby to bylo full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //pro zajištění, aby to bylo full screen

        Window window = getWindow(); //spodní navigační lišta pořád tmavá
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.setNavigationBarColor(getResources().getColor(R.color.colorCerna, null));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mMessageReceiver, new IntentFilter("znicClassicActivityAClassicService"), null, null, RECEIVER_NOT_EXPORTED); //pridat pro service, protože cílím na Android 14, tak jsem musel přidat ještě , null, null, RECEIVER_NOT_EXPORTED, jakože  žůže přijímat pouze vysílání z této aplikace
            //ještě jsem dal co manifestu <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
        } else  registerReceiver(mMessageReceiver, new IntentFilter("znicClassicActivityAClassicService"));
        //zamezí vypnutí obrazovky do úsporného režimu po nečinnosti, šlo to udělat
        //v XML -  android:keepScreenOn="true", ale to bych to musel dát do všech XML (land...)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            //v androidu 11 když uživatel dal zpět, tak se zobrazila notifikace, tak tato metoda bude checkovat, jestli uživatel dal v aktivitě
            //zpět a pokud ano, notifikace se v onStop nezavolá, protože ale zpět může dát i v otevřeném
            @Override
            public void handleOnBackPressed() {
                if(buttonBackIsNotPressed) {
                    buttonBackIsNotPressed = false;
                    finish();
                }

            }
        });

        //Google Billing
        SharedPreferences sharedPreferences =
                getApplicationContext().getSharedPreferences("boxing_timer_prefers", Context.MODE_PRIVATE);
        adsDisabled = sharedPreferences.getBoolean("ads_disabled", false);

        statusBarcolor();


        if (getIntent().getExtras() != null) {
            //play google mi zde házel chybu, tak ještě ověřuji, zda Bundle extras není null
            //pravděpodobně je to ve chvíli, kdy uživatel killne tuto aktivitu, skočí mu notifikace a on na ni klikne, potom se znovu otevře
            //nová tato aktivita, ale getIntent().getExtras() je null
            if (getIntent().getExtras().getParcelable("caspripavy") == null) {
                casPripravy = new MyTime(0, 0, 20);
            } else {
                casPripravy = getIntent().getExtras().getParcelable("caspripavy");
            }
            if (getIntent().getExtras().getParcelable("cascviceni") == null) {
                casCviceni = new MyTime(0, 2, 0);
            } else {
                casCviceni = getIntent().getExtras().getParcelable("cascviceni");
            }
            if (getIntent().getExtras().getParcelable("caspauzy") == null) {
                casPauzy = new MyTime(0, 1, 0);
            } else {
                casPauzy = getIntent().getExtras().getParcelable("caspauzy");
            }
            if (getIntent().getExtras().getParcelable("cascelkovy") == null) {
                casCelkovy = new MyTime(0, 23, 20);
            } else {
                casCelkovy = getIntent().getExtras().getParcelable("cascelkovy");
            }

            puvodniPocetCyklu = getIntent().getExtras().getInt("pocetcyklu");
            colorDlazdiceCasCviceni = getIntent().getExtras().getInt("barvaCviceni", getResources().getColor(R.color.colorCasCviceni));
            colorDlazdiceCasPauzy = getIntent().getExtras().getInt("barvaPauzy",getResources().getColor(R.color.colorCasPauzy));
            colorDlazdicePocetCyklu = getIntent().getExtras().getInt("barvaPocetCyklu", getResources().getColor(R.color.colorCerna));
            colorDlazdiceCasPripravy = getIntent().getExtras().getInt("barvaPripravy", getResources().getColor(R.color.colorCasPripravy));

            zvukStart = getIntent().getIntExtra("zvukstart", 1);
            zvukStop = getIntent().getIntExtra("zvukstop", 1);
            zvukCelkovyKonec = getIntent().getIntExtra("zvukcelkovykonec", 1);
            zvukCountdown = getIntent().getIntExtra("zvukcountdown", 1);
            zvukPulkaCviceni = getIntent().getIntExtra("zvukpulkakola", 49);

            zvukPredkoncemKola = getIntent().getIntExtra("zvukpredkoncemkola", 49);
            casZvukuPredKoncemKola = getIntent().getIntExtra("caszvukupupredkoncemkola", 20);
            hlasitost = getIntent().getIntExtra("hlasitost", 100);

            if (zvukPulkaCviceni != PraceSeZvukem.vratPocetZvukuPulkaCviceni()) {
                casPulkyKola = (casCviceni.getHour() * 3600 + casCviceni.getMin() * 60 + casCviceni.getSec()) / 2;
                casPulkyKolaAktualni = casPulkyKola;
                Log.d("Cas pulky kola: ", String.valueOf(casPulkyKola));

            }

            volume = (float) (1 - (Math.log(maxHlasitost - hlasitost) / Math.log(maxHlasitost)));


            //countdown zvuk řeším jinak, než ostatní zvuky, předělával jsem to, tak abych se moc nevrtal v kodu, tak to nechám takhle jinak
            //       tikZvuk3 = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
            //       tikZvuk2 = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
            //       tikZvuk1 = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
            mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));

            //new design
            vybranyDesign = getDesignPreferences(getApplicationContext());
            Log.d("vybranyDesign", String.valueOf(vybranyDesign));

            //Firebase Analytics
            TimerAnalytics.INSTANCE.getInstance(this).logActivityStart("TimerClassicActivity_"+String.valueOf(vybranyDesign));
            switch (vybranyDesign) {
                case 1:
                    newLayout = false;
                    break;
                case 2:
                    newLayout = true;
                    break;
                case 3:
                    newLayout = true;
                    nastavProgressBar = true;
                    break;
                default:
                    newLayout = false;
            }
            udelejLayout();

            spustCasovac();
        } else {
            // Bundle je null, takže uživatel nejdříve killnul tuto aktivitu, zobrazila se mu ale notifikace, na tuto kliknul, ale ta
            //už ho neměla kam nasměrovat zpět, tak se tato aktivita otevřela znovu, ale bez dat, tak automaticky skočím do mainActivity
            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivity);
            finish();
        }


   }

    private void spustCasovac() {
        //pauzaMeziTabatami = casMezitabatami.getSec();
        pocetCyklu = puvodniPocetCyklu;
        pocetTabat = puvodniPocetTabat;

        //nastavení časovače - prostě to bude odpočítávat
        //  int pomocnyCelkovyCasMeziTabatami = pauzaMeziTabatami*(pocetTabat-1);
        //   dobaCasovace = casPripravy.getSec() +((casCviceni.getSec()+casPauzy.getSec())*pocetCyklu +pomocnyCelkovyCasMeziTabatami)*pocetTabat;
        dobaCasovace = 100000; //nebudeme to počítat, ale pojede to tolik sekunc
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
            //color
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            shape.setColor(colorDlazdiceCasPripravy);
            dlazdiceOdpocitavace.setBackground(shape);

        } else
            dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPripravy));

        if (newLayout) {
            dlazdiceOdpocitavace.setBackgroundColor(colorDlazdiceCasPripravy);
            zmenNavigationBarColor(colorDlazdiceCasPripravy);

        }

        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus) + "/" + String.valueOf(puvodniPocetCyklu));
        //zapíše čas cvičení
        if (casCviceni.getSec() < 10) {
            textViewAktualniPocetTabat.setText(String.valueOf(casCviceni.getMin()) + ":0" + String.valueOf(casCviceni.getSec()));
        } else {
            textViewAktualniPocetTabat.setText(String.valueOf(casCviceni.getMin()) + ":" + String.valueOf(casCviceni.getSec()));
        }

        pomocny = casPripravy.getSec() + 1 + casPripravy.getMin() * 60 + casPripravy.getHour() * 3600;
        maxHodnotaProgressBar = (int) pomocny;
        if (pomocny < 60) {
            textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniDva));
            velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;
        }
        pocetCyklu = pocetCyklu - 1;
        pocetTabat = pocetTabat - 1;
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

                odectiAZobrazCelkovyCas();

                switch (stav) {
                    case 0:
                        //vlákno příprava
                        pomocny = pomocny - 1;
                        if (pomocny <= 0) {
                            //tady   restZvuk.start();
                            //  PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukStart);
                            //nakonec jsem to musel vyřešit takhle, chtěl jsem spouštět zvuk přímo ve třídě PraceSeZvukem, ale po přehrání pár zvuků
                            //to přestalo přehrávat zvuky, tak jsem to udělal takhle, navíc jsem to pořešil reset() a release(), našel jsem to v nějakém návodu
                            if (mediaPlayer != null) {
                                mediaPlayer.reset();
                                mediaPlayer.release();
                                mediaPlayer = null;
                            }
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
                            mediaPlayer.setVolume(volume, volume);
                            mediaPlayer.start();


                            stav = (byte) (stav + 1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                                //color
                                GradientDrawable shape = new GradientDrawable();
                                shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                shape.setColor(colorDlazdiceCasCviceni);
                                dlazdiceOdpocitavace.setBackground(shape);
                                //color
                            } else
                                dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCviceni));

                            if (newLayout) {
                                dlazdiceOdpocitavace.setBackgroundColor(colorDlazdiceCasCviceni);
                                zmenNavigationBarColor(colorDlazdiceCasCviceni);
                            }

                            textViewCasNadpis.setText(R.string.nadpisCasCviceni);
                            pomocny = casCviceni.getSec() + casCviceni.getMin() * 60 + casCviceni.getHour() * 3600 + 1;
                            maxHodnotaProgressBar = (int) pomocny;
                            pomocny = pomocny - 1; //protože při GO už to je jedna sekunda a pak mi to při celkovém času nehrálo

                            textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniDva));
                            velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;
                            textViewCas.setText(R.string.cvic);
                            if (aktualniCyklus > 1) {
                                textViewBeziciCasCisloKola.setText(String.valueOf(aktualniCyklus));
                            }
                            preskocVypisCasu = true;

                        }

                        if (preskocVypisCasu) {
                            preskocVypisCasu = false;
                        } else {
                            nastavCislice(pomocny);
                            switch ((int) pomocny) {
                                case 4:
                                    //musel jsem tam dát ještě při odpočítávání falešný tik na 4té sekundě, protože u pomalejších zařízeních mi
                                    //ten třetí tik přehrával pomalu (když několik sekund před tím nebyl zvuk spuštěn) a než se stihnul přehrát, tak už to bylo na dvojce, tak jsem sem dal
                                    //falešný 4 tik, který načtu, spustím a ihned vypnu. U dalších tiků problém nebyl. Nevím proč, ale nějak MediaPlayer jak není několik sekund
                                    //spuštěn, tak pak se načítá pomalu a v pomalejších zařízeních je to znát
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    mediaPlayer.stop();
                                    break;
                                case 3:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    break;
                                case 2:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    break;
                                case 1:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    break;
                                default:
                                    break;
                            }

                        }


                        break;
                    case 1:
                        //vlákno cvičení
                        pomocny = pomocny - 1;
                        if (pomocny <= 0) {
                            casPulkyKolaAktualni = casPulkyKola;
                            if (pocetCyklu == 0) {
                                if (pocetTabat == 0) {
                                    //    fanfareZvuk.start();

                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukCelkovyKonec));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();

                                    if (casCoolDown.getSec() == 0 && casCoolDown.getMin() == 0) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorkonectabaty));//  (R.drawable.background);

                                        } else
                                            dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                                        if (newLayout) {
                                            dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                                            zmenNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                                        }

                                        textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                                        velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                                        textViewCas.setText(R.string.konec);
                                        konecOdpocitavani = true;
                                        textViewBeziciCasCisloKola.setText("");
                                        stav = 4;
                                        preskocVypisCasu = true;
                                        Log.d("Jsem na konci: ", "1");


                                    } else {
                                        stav = 5;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascooldown));//  (R.drawable.background);
                                        } else
                                            dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCoolDown));
                                        if (newLayout) {
                                            dlazdiceOdpocitavace.setBackgroundColor(getBaseContext().getResources().getColor(R.color.colorCasCoolDown));
                                            zmenNavigationBarColor(getBaseContext().getResources().getColor(R.color.colorCasCoolDown));
                                        }
                                        textViewCasNadpis.setText(R.string.nadpisCasCoolDown);
                                        pomocny = casCoolDown.getSec() + casCoolDown.getMin() * 60 + casCoolDown.getHour() * 3600 + 1;
                                        maxHodnotaProgressBar = (int) pomocny;

                                        textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniCoolDown));
                                        velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                                        textViewCas.setText(R.string.coolDown);
                                        textViewBeziciCasCisloKola.setText("");
                                        preskocVypisCasu = true;
                                        Log.d("Jsem na konci: ", "2");


                                    }


                                } else {
                                    //tady   restZvuk.start();
                                    // PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukStop);

                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStop));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();

                                    stav = 3;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzymezitabatami));//  (R.drawable.background);
                                    } else
                                        dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPauzyMeziTabatami));
                                    if (newLayout) {
                                        dlazdiceOdpocitavace.setBackgroundColor(getBaseContext().getResources().getColor(R.color.colorCasPauzyMeziTabatami));
                                        zmenNavigationBarColor(getBaseContext().getResources().getColor(R.color.colorCasPauzyMeziTabatami));
                                    }
                                    textViewCasNadpis.setText(R.string.nadpisOdpocinekMeziTabatami);
                                    pomocny = casMezitabatami.getSec() + casMezitabatami.getMin() * 60 + casMezitabatami.getHour() * 3600 + 1;
                                    maxHodnotaProgressBar = (int) pomocny;

                                    pomocny = pomocny - 1; //protože při REST už to je jedna sekunda a pak mi to při celkovém času nehrálo

                                    textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                                    velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                                    textViewCas.setText(R.string.odpocinek);
                                    textViewBeziciCasCisloKola.setText("");
                                    preskocVypisCasu = true;
                                    pocetTabat = pocetTabat - 1;
                                }
                            } else {
                                //tady                            restZvuk.start();
                                //  PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukStop);

                                if (mediaPlayer != null) {
                                    mediaPlayer.reset();
                                    mediaPlayer.release();
                                }
                                mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStop));
                                mediaPlayer.setVolume(volume, volume);
                                mediaPlayer.start();

                                stav = (byte) (stav + 1);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzy));//  (R.drawable.background);
                                    //color
                                    GradientDrawable shape = new GradientDrawable();
                                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                    shape.setColor(colorDlazdiceCasPauzy);
                                    dlazdiceOdpocitavace.setBackground(shape);
                                    //color
                                } else
                                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPauzy));
                                if (newLayout) {
                                    dlazdiceOdpocitavace.setBackgroundColor(colorDlazdiceCasPauzy);
                                    zmenNavigationBarColor(colorDlazdiceCasPauzy);
                                }
                                textViewCasNadpis.setText(R.string.nadpisCasPauzy);
                                pomocny = casPauzy.getSec() + casPauzy.getMin() * 60 + casPauzy.getHour() * 3600 + 1;
                                maxHodnotaProgressBar = (int) pomocny;

                                pomocny = pomocny - 1; //protože při REST už to je jedna sekunda a pak mi to při celkovém času nehrálo

                                textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                                velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                                textViewCas.setText(R.string.odpocinek);
                                textViewBeziciCasCisloKola.setText("");
                                preskocVypisCasu = true;
                            }
                        }

                        if (preskocVypisCasu) {
                            preskocVypisCasu = false;
                        } else {
                            nastavCislice(pomocny);
                            if ((casZvukuPredKoncemKola != 0) && (casZvukuPredKoncemKola == pomocny && zvukPredkoncemKola != PraceSeZvukem.vratPocetZvukuPulkaCviceni())) {
                                //pokud bude nastavený zvuk v půlce kola, tak to odehraje tohle níže, má to větší přednost než countdown, takže pokud by to přicházelo na
                                //stejný čas, tak to odehraje pouze zvuk půlky kola
                                //aaa
                                Log.d("Jsem v půlce kola", "halo");
                                if (mediaPlayer != null) {
                                    mediaPlayer.reset();
                                    mediaPlayer.release();
                                }
                                mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukPredkoncemKola));
                                mediaPlayer.setVolume(volume, volume);
                                mediaPlayer.start();
                            } else if ((casPulkyKolaAktualni != 0) && (casPulkyKolaAktualni == pomocny && zvukPulkaCviceni != PraceSeZvukem.vratPocetZvukuPulkaCviceni())) {
                                //pokud bude nastavený zvuk v půlce kola, tak to odehraje tohle níže, má to větší přednost než countdown, takže pokud by to přicházelo na
                                //stejný čas, tak to odehraje pouze zvuk půlky kola
                                if (mediaPlayer != null) {
                                    mediaPlayer.reset();
                                    mediaPlayer.release();
                                }
                                Log.d("Jsem v půlce kola", "halo");
                                mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukPulkaCviceni));
                                mediaPlayer.setVolume(volume, volume);
                                mediaPlayer.start();
                            } else {
                                Log.d("cas: ", String.valueOf(pomocny));
                                switch ((int) pomocny) {
                                    case 4:
                                        if (mediaPlayer != null) {
                                            mediaPlayer.reset();
                                            mediaPlayer.release();
                                        }
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                        //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
                                        mediaPlayer.setVolume(volume, volume);
                                        mediaPlayer.start();
                                        mediaPlayer.stop();
                                        break;
                                    case 3:
                                        if (mediaPlayer != null) {
                                            mediaPlayer.reset();
                                            mediaPlayer.release();
                                        }
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                        mediaPlayer.setVolume(volume, volume);
                                        mediaPlayer.start();
                                        break;
                                    case 2:
                                        if (mediaPlayer != null) {
                                            mediaPlayer.reset();
                                            mediaPlayer.release();
                                        }
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                        mediaPlayer.setVolume(volume, volume);
                                        mediaPlayer.start();

                                        break;
                                    case 1:
                                        if (mediaPlayer != null) {
                                            mediaPlayer.reset();
                                            mediaPlayer.release();
                                        }
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                        mediaPlayer.setVolume(volume, volume);
                                        mediaPlayer.start();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }


                        break;
                    case 2:
                        //vlákno pauza
                        pomocny = pomocny - 1;
                        if (pomocny <= 0) {
                            if (pocetCyklu == 0) {
                                if (pocetTabat == 0) {
                                    if (casCoolDown.getSec() == 0 && casCoolDown.getMin() == 0) {
                                        //   fanfareZvuk.start();

                                        if (mediaPlayer != null) {
                                            mediaPlayer.reset();
                                            mediaPlayer.release();
                                        }
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukCelkovyKonec));
                                        mediaPlayer.setVolume(volume, volume);
                                        mediaPlayer.start();

                                        stav = 4;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorkonectabaty));//  (R.drawable.background);
                                        } else
                                            dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                                        if (newLayout) {
                                            dlazdiceOdpocitavace.setBackgroundColor(getBaseContext().getResources().getColor(R.color.colorKonecTabaty));
                                            zmenNavigationBarColor(getBaseContext().getResources().getColor(R.color.colorKonecTabaty));
                                        }
                                        textViewCas.setText("");
                                        textViewBeziciCasCisloKola.setText("");
                                        preskocVypisCasu = true;
                                    } else {
                                        //tady                            restZvuk.start();
                                        //   PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukCelkovyKonec);

                                        if (mediaPlayer != null) {
                                            mediaPlayer.reset();
                                            mediaPlayer.release();
                                        }
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukCelkovyKonec));
                                        mediaPlayer.setVolume(volume, volume);
                                        mediaPlayer.start();
                                        stav = 5;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascooldown));//  (R.drawable.background);
                                        } else
                                            dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCoolDown));
                                        if (newLayout) {
                                            dlazdiceOdpocitavace.setBackgroundColor(getBaseContext().getResources().getColor(R.color.colorCasCoolDown));
                                            zmenNavigationBarColor(getBaseContext().getResources().getColor(R.color.colorCasCoolDown));
                                        }
                                        textViewCasNadpis.setText(R.string.nadpisCasCoolDown);
                                        pomocny = casCoolDown.getSec() + casCoolDown.getMin() * 60 + casCoolDown.getHour() * 3600 + 1;
                                        maxHodnotaProgressBar = (int) pomocny;

                                        textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniCoolDown));
                                        velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                                        textViewCas.setText(R.string.coolDown);
                                        textViewBeziciCasCisloKola.setText("");
                                        preskocVypisCasu = true;
                                    }

                                } else {
                                    //tady                            restZvuk.start();
                                    //  PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukStop);

                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStop));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    stav = 3;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzymezitabatami));//  (R.drawable.background);
                                    } else
                                        dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPauzyMeziTabatami));
                                    if (newLayout) {
                                        dlazdiceOdpocitavace.setBackgroundColor(getBaseContext().getResources().getColor(R.color.colorCasPauzyMeziTabatami));
                                        zmenNavigationBarColor(getBaseContext().getResources().getColor(R.color.colorCasPauzyMeziTabatami));
                                    }
                                    textViewCasNadpis.setText(R.string.nadpisCasPauzy);
                                    pomocny = casMezitabatami.getSec() + casMezitabatami.getMin() * 60 + casMezitabatami.getHour() * 3600 + 1;
                                    maxHodnotaProgressBar = (int) pomocny;

                                    pomocny = pomocny - 1; //protože při REST už to je jedna sekunda a pak mi to při celkovém času nehrálo

                                    textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                                    velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                                    textViewCas.setText(R.string.odpocinek);
                                    textViewBeziciCasCisloKola.setText("");
                                    preskocVypisCasu = true;
                                    pocetTabat = pocetTabat - 1;
                                }
                            } else {
                                //tady                            restZvuk.start();
                                //  PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukStart);

                                if (mediaPlayer != null) {
                                    mediaPlayer.reset();
                                    mediaPlayer.release();
                                }
                                mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
                                mediaPlayer.setVolume(volume, volume);
                                mediaPlayer.start();
                                stav = 1;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                                    //color
                                    GradientDrawable shape = new GradientDrawable();
                                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                    shape.setColor(colorDlazdiceCasCviceni);
                                    dlazdiceOdpocitavace.setBackground(shape);
                                    //color
                                } else
                                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCviceni));
                                if (newLayout) {
                                    dlazdiceOdpocitavace.setBackgroundColor(colorDlazdiceCasCviceni);
                                    zmenNavigationBarColor(colorDlazdiceCasCviceni);
                                }
                                textViewCasNadpis.setText(R.string.nadpisCasCviceni);
                                aktualniCyklus = aktualniCyklus + 1;
                                textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus) + "/" + String.valueOf(puvodniPocetCyklu));
                                pomocny = casCviceni.getSec() + casCviceni.getMin() * 60 + casCviceni.getHour() * 3600 + 1;
                                maxHodnotaProgressBar = (int) pomocny;

                                pomocny = pomocny - 1; //protože při GO už to je jedna sekunda a pak mi to při celkovém času nehrálo


                                textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniDva));
                                velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;
                                textViewCas.setText(R.string.cvic);
                                if (aktualniCyklus > 1) {
                                    textViewBeziciCasCisloKola.setText(String.valueOf(aktualniCyklus));
                                }
                                preskocVypisCasu = true;

                                pocetCyklu = pocetCyklu - 1;
                            }

                        }

                        if (preskocVypisCasu) {
                            preskocVypisCasu = false;
                        } else {
                            nastavCislice(pomocny);

                            switch ((int) pomocny) {
                                case 4:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    mediaPlayer.stop();
                                    break;
                                case 3:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    break;
                                case 2:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    break;
                                case 1:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }

                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    break;
                                default:
                                    break;
                            }

                        }

                        break;
                    case 3:
                        //vlákno pauza mezi tabatami
                        pomocny = pomocny - 1;
                        if (pomocny <= 0) {
                            //tady                            restZvuk.start();
                            // PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukStart);

                            if (mediaPlayer != null) {
                                mediaPlayer.reset();
                                mediaPlayer.release();
                            }
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
                            mediaPlayer.setVolume(volume, volume);
                            mediaPlayer.start();

                            stav = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                                //color
                                GradientDrawable shape = new GradientDrawable();
                                shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                shape.setColor(colorDlazdiceCasCviceni);
                                dlazdiceOdpocitavace.setBackground(shape);
                                //color
                            } else
                                dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCviceni));
                            if (newLayout) {
                                dlazdiceOdpocitavace.setBackgroundColor(colorDlazdiceCasCviceni);
                                zmenNavigationBarColor(colorDlazdiceCasCviceni);
                            }
                            textViewCasNadpis.setText(R.string.nadpisCasCviceni);
                            aktualniCyklus = 1;
                            textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus) + "/" + String.valueOf(puvodniPocetCyklu));
                            aktualniTabata = aktualniTabata + 1;
                            //       textViewAktualniPocetTabat.setText(String.valueOf(aktualniTabata)+"/"+String.valueOf(puvodniPocetTabat));
                            pomocny = casCviceni.getSec() + casCviceni.getMin() * 60 + casCviceni.getHour() * 3600 + 1;
                            maxHodnotaProgressBar = (int) pomocny;
                            pomocny = pomocny - 1; //protože při GO už to je jedna sekunda a pak mi to při celkovém času nehrálo

                            textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniDva));
                            velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;
                            textViewCas.setText(R.string.cvic);
                            if (aktualniCyklus > 1) {
                                textViewBeziciCasCisloKola.setText(String.valueOf(aktualniCyklus));
                            }
                            preskocVypisCasu = true;
                            pocetCyklu = puvodniPocetCyklu - 1;


                        }

                        if (preskocVypisCasu) {
                            preskocVypisCasu = false;
                        } else {
                            nastavCislice(pomocny);
                            switch ((int) pomocny) {
                                case 4:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    mediaPlayer.stop();
                                    break;
                                case 3:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    break;
                                case 2:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    break;
                                case 1:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    break;
                                default:
                                    break;
                            }
                        }

                        break;
                    case 4:
                        //konec odpočítávání
                        textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                        textViewCas.setText(R.string.konec);
                        textViewBeziciCasCisloKola.setText("");
                        konecOdpocitavani = true;
                        zavlojejReviewNejake(); //aaa
                        odpocitavac.cancel();
                        Log.d("Jsem na konci: ", "3");
                        break;

                    case 5:
                        //čas cool down, je to tam doděláno dodatečně, proto to má číslo 5 a ne 4
                        pomocny = pomocny - 1;
                        if (pomocny <= 0) {
                            //   restZvuk.start();

                            //  PraceSeZvukem.spustZvukKonec(getApplicationContext(),zvukCelkovyKonec);

                            if (mediaPlayer != null) {
                                mediaPlayer.reset();
                                mediaPlayer.release();
                            }
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukCelkovyKonec));
                            mediaPlayer.setVolume(volume, volume);
                            mediaPlayer.start();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorkonectabaty));//  (R.drawable.background);
                            } else
                                dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                            if (newLayout) {
                                dlazdiceOdpocitavace.setBackgroundColor(getBaseContext().getResources().getColor(R.color.colorKonecTabaty));
                                zmenNavigationBarColor(getBaseContext().getResources().getColor(R.color.colorKonecTabaty));
                            }
                            textViewCasNadpis.setText("");
                            stav = 4;
                            //tady velká změna bylo jen preskocVypisCasu = false;
                            preskocVypisCasu = true;
                            textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                            textViewCas.setText(R.string.konec);
                            konecOdpocitavani = true; //progress bar
                            //konec velké změny

                        }

                        if (preskocVypisCasu) {
                            preskocVypisCasu = false;
                        } else {
                            nastavCislice(pomocny);
                            switch ((int) pomocny) {
                                case 4:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    mediaPlayer.stop();
                                    break;
                                case 3:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    break;
                                case 2:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    break;
                                case 1:
                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown));
                                    mediaPlayer.setVolume(volume, volume);
                                    mediaPlayer.start();
                                    break;
                                default:
                                    break;
                            }

                        }
                        break;

                    default:
                        break;

                }

                if (nastavProgressBar && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Log.d("progress-bar", String.valueOf(maxHodnotaProgressBar - 1));
                    Log.d("progress-bar", String.valueOf(pomocny));
                    Log.d("progress-bar", String.valueOf(konecOdpocitavani));
                    if (((maxHodnotaProgressBar - 1 - (int) pomocny) == 0) || konecOdpocitavani) {
                        progressBar.setVisibility(View.GONE);
                    } else {

                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setEndValue(maxHodnotaProgressBar - 1);
                        progressBar.setValue(maxHodnotaProgressBar - 1 - (int) pomocny);
                    }
                }
            }
        }

        @Override
        public void onFinish() {
            textViewCas.setText("0");
            textViewBeziciCasCisloKola.setText("");
            //      dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPocetTabat));
        }
    }

    private void odectiAZobrazCelkovyCas() {
        zobrazCelkovyCas();
        if (casCelkovy.getHour() == 0 & casCelkovy.getMin() == 0 & casCelkovy.getSec() == 0) {

        } else {
            if (casCelkovy.getSec() == 0) {
                casCelkovy.setMin(casCelkovy.getMin() - 1);
                casCelkovy.setSec(59);
            } else {
                casCelkovy.setSec(casCelkovy.getSec() - 1);
            }
        }

    }

    private void zobrazCelkovyCas() {
        int hodiny = casCelkovy.getHour();
        String hodinyString;
        int minuty = casCelkovy.getMin();
        String minutyString;
        int sekundy = casCelkovy.getSec();
        String sekundyString;

        if (hodiny == 0) {
            hodinyString = "";
        } else {
            hodinyString = String.valueOf(hodiny) + ":";
        }

        if (minuty < 10) {
            minutyString = "0" + String.valueOf(minuty);
        } else {
            minutyString = String.valueOf(minuty);
        }

        if (sekundy < 10) {
            sekundyString = "0" + String.valueOf(sekundy);
        } else {
            sekundyString = String.valueOf(sekundy);
        }

        textViewCelkovyCas.setText(hodinyString + minutyString + ":" + sekundyString);
    }


    private void spustOdpocitavac() {
        odpocitavac = new OdpocitavacCasu((dobaCasovace) * 1000, 1000);
        odpocitavac.start();
    }

    public void showPauseDialog(View v) {
        if (pauzaNeniZmacknuta) {
            pauzaNeniZmacknuta = false;
            // textViewPauza.setText(">");
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && !newLayout) {
                linearLayoutPauza.setBackgroundResource(R.mipmap.playlezatotabataactivity);
            } else {
                linearLayoutPauza.setBackgroundResource(R.mipmap.playstojatotabataactivity);
            }
            //       linearLayoutPauza.setBackgroundResource(R.mipmap.playlezatotabataactivity);
        //    vytvorToast(getResources().getString(R.string.pauza));

        } else {
            pauzaNeniZmacknuta = true;
            //    textViewPauza.setText("||");
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || newLayout) {
                linearLayoutPauza.setBackgroundResource(R.mipmap.pausestojatotabataactivity);
            } else {
                linearLayoutPauza.setBackgroundResource(R.mipmap.pauselezatotabataactivity);
            }
            //        linearLayoutPauza.setBackgroundResource(R.mipmap.pauselezatotabataactivity);
       //     vytvorToast(getResources().getString(R.string.pokracovat));
        }


    }

    public void showTimePickerDialogNastavCasKolaVCasovaci(View v) {
        pauzaNeniZmacknuta = false;
        //  textViewPauza.setText(">");
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && !newLayout) {
            linearLayoutPauza.setBackgroundResource(R.mipmap.playlezatotabataactivity);
        } else {
            linearLayoutPauza.setBackgroundResource(R.mipmap.playstojatotabataactivity);
        }
        dialogNastaveniCasuKola.show();

    }

    public void showPickerNastavPocetCykluVTabate(View v) {
        pauzaNeniZmacknuta = false;
        //   textViewPauza.setText(">");
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE  && !newLayout) {
            linearLayoutPauza.setBackgroundResource(R.mipmap.playlezatotabataactivity);
        } else {
            linearLayoutPauza.setBackgroundResource(R.mipmap.playstojatotabataactivity);
        }
        //    linearLayoutPauza.setBackgroundResource(R.mipmap.playlezatotabataactivity);
        dialogPocetCyklu.show();

    }

    private void vytvorDialogOpakovani(final Dialog dialog, final String nadpis) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //pokud budu chtít nadpisy, tak tohle oddělat a tyhle zakomentované řádky pod tím naopak odkomentovat (od comment 1 do comment2

        dialog.setContentView(R.layout.set_numbers);
//comment1        dialog.setTitle(nadpis);
        //nastavení bílé barvy na linku u dialogu, jinak mi to nešlo, jen takhle
//        int divierId = dialog.getContext().getResources()
//                .getIdentifier("android:id/titleDivider", null, null);
//comment2        View divider = dialog.findViewById(divierId);
        //tady        divider.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPisma));

        //    dialog.getWindow().setBackgroundDrawableResource(R.color.colorPocetTabat);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {


            int[] colors1 = {colorDlazdiceCasCviceni, colorDlazdiceCasPauzy};

            GradientDrawable shadow2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors1);
            shadow2.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            dialog.getWindow().setBackgroundDrawable(shadow2);

        } else dialog.getWindow().setBackgroundDrawableResource(R.color.colorPocetTabat);

        final NumberPicker pocitacDesitky = (NumberPicker) dialog.findViewById(R.id.npDesitky);
        setDividerColor(pocitacDesitky);
        pocitacDesitky.setMinValue(0);
        pocitacDesitky.setMaxValue(9);
        final NumberPicker pocitacJednotky = (NumberPicker) dialog.findViewById(R.id.npJednotky);
        setDividerColor(pocitacJednotky);
        pocitacJednotky.setMinValue(0);
        pocitacJednotky.setMaxValue(9);

        // Set vibration on scroll
        pocitacDesitky.setOnValueChangedListener((picker, oldVal, newVal) -> {
            VibratorTimer.INSTANCE.vibrate(dialog.getContext());
        });

        pocitacJednotky.setOnValueChangedListener((picker, oldVal, newVal) -> {
            VibratorTimer.INSTANCE.vibrate(dialog.getContext());
        });

        Button uloz = (Button) dialog.findViewById(R.id.buttonUlozPocet);
        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pocet = String.valueOf(String.valueOf(pocitacDesitky.getValue()) + String.valueOf(pocitacJednotky.getValue()));


                if (nadpis.equals(getResources().getString(R.string.nadpisNastavPocetCyklu))) {
                    int novyRozdilMeziCykly = ((pocitacDesitky.getValue()) * 10 + pocitacJednotky.getValue()) - puvodniPocetCyklu;
                    if ((novyRozdilMeziCykly + pocetCyklu) > 0) {
                        pocetCyklu = pocetCyklu + novyRozdilMeziCykly;
                        puvodniPocetCyklu = puvodniPocetCyklu + novyRozdilMeziCykly;
                        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus) + "/" + String.valueOf(puvodniPocetCyklu));
                        vytvorToast(getResources().getString(R.string.nadpisPocetCyklu) + " " + pocet);
                        prepoctiZbyvajiciCasCykly(novyRozdilMeziCykly);
                    }

                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavPocetTabat))) {
                    int novyRozdilMeziTabatami = ((pocitacDesitky.getValue()) * 10 + pocitacJednotky.getValue()) - puvodniPocetTabat;
                    if ((novyRozdilMeziTabatami + pocetTabat) > 0) {
                        pocetTabat = pocetTabat + novyRozdilMeziTabatami;
                        puvodniPocetTabat = puvodniPocetTabat + novyRozdilMeziTabatami;
                        textViewAktualniPocetTabat.setText(String.valueOf(casCviceni.getMin()) + ":" + String.valueOf(casCviceni.getSec()));
                        vytvorToast(getResources().getString(R.string.nadpisPocetTabat) + " " + pocet);
                        prepoctiZbyvajiciCasTabaty(novyRozdilMeziTabatami);
                    }
                }
                pauzaNeniZmacknuta = true;
                //textViewPauza.setText("||");
                if (getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    linearLayoutPauza.setBackgroundResource(R.mipmap.pausestojatotabataactivity);
                } else {
                    linearLayoutPauza.setBackgroundResource(R.mipmap.pauselezatotabataactivity);
                }
                dialog.dismiss(); //nebo cancel() ???
            }
        });
    }


    private void vytvorDialogCasu(final Dialog dialog, final String nadpis) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); //pokud budu chtít nadpisy, tak tohle oddělat a tyhle zakomentované řádky pod tím naopak odkomentovat (od comment 1 do comment2

        dialog.setContentView(R.layout.set_time_layout_dialog);


        if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCviceni))) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                //color
                GradientDrawable shape = new GradientDrawable();
                shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                shape.setColor(colorDlazdiceCasCviceni);
                dialog.getWindow().setBackgroundDrawable(shape);
                //color

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

        // Set vibration on scroll
        pocitacMin.setOnValueChangedListener((picker, oldVal, newVal) -> {
            VibratorTimer.INSTANCE.vibrate(dialog.getContext());
        });

        pocitacSec.setOnValueChangedListener((picker, oldVal, newVal) -> {
            VibratorTimer.INSTANCE.vibrate(dialog.getContext());
        });

        //nastavím v dialogu hodnotu, která je načtená ze souboru
        if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCviceni))) {
            pocitacMin.setValue(casCviceni.getMin()); //asdf
            pocitacSec.setValue(casCviceni.getSec());
        }

        Button uloz = (Button) dialog.findViewById(R.id.buttonUloz);
        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nadpis.equals(getResources().getString(R.string.nadpisNastavCasCviceni))) {
                    int rozdilMeziCasemCviceniANovouHodnotou;

                    //uloží si bokem čas cvičení
                    int pomocnyCasCviceni = casCviceni.getHour() * 3600 + casCviceni.getMin() * 60 + casCviceni.getSec();

                    //načte do proměnné casCviceni nový čas cvičení
                    casCviceni = new MyTime(0, pocitacMin.getValue(), pocitacSec.getValue());

                    casPulkyKola = (casCviceni.getHour() * 3600 + casCviceni.getMin() * 60 + casCviceni.getSec()) / 2;
                    if (stav != 1) {
                        //když stav bude 0, tedy když pojede příprava, tak se nastaví hned casPulkyKolaAktualni
                        //problém je, že když bude ve stavu 1, to se musí nechat dojet kolo a pak se teprve nastaví čas půlky kola
                        //a to mám pořešené v tom vláknu 1 cvičení
                        casPulkyKolaAktualni = casPulkyKola;
                    }


                    //spočte rozdíl mezi novým a starým časem cvičení
                    rozdilMeziCasemCviceniANovouHodnotou = (casCviceni.getHour() * 3600 + casCviceni.getMin() * 60 + casCviceni.getSec() - pomocnyCasCviceni);

                    //přepočte zbývající celkový čas
                    prepoctiZbyvajiciCasZmenaCasuKola(rozdilMeziCasemCviceniANovouHodnotou);

                    //zapíše nový čas cvičení
                    if (casCviceni.getSec() < 10) {
                        textViewAktualniPocetTabat.setText(String.valueOf(casCviceni.getMin()) + ":0" + String.valueOf(casCviceni.getSec()));
                    } else {
                        textViewAktualniPocetTabat.setText(String.valueOf(casCviceni.getMin()) + ":" + String.valueOf(casCviceni.getSec()));
                    }
                }


                dialog.dismiss(); //nebo cancel() ???
                //    prepoctiZbyvajiciCasZmenaCasuKola( rozdilMeziCasemCviceniANovouHodnotou);
            }
        });
    }

    private void prepoctiZbyvajiciCasZmenaCasuKola(int novyRozdilMeziCykly) {
        int pomocnyCasCviceni = casCviceni.getHour() * 3600 + casCviceni.getMin() * 60 + casCviceni.getSec();
        int pomocnyCasPauzy = casPauzy.getHour() * 3600 + casPauzy.getMin() * 60 + casPauzy.getSec();
        //   int pomocnyCasMeziTabatami = casMezitabatami.getHour()*3600 + casMezitabatami.getMin()*60 + casMezitabatami.getSec();
        int pomocnyCelkovyCas = casCelkovy.getHour() * 3600 + casCelkovy.getMin() * 60 + casCelkovy.getSec();
        pomocnyCelkovyCas = pomocnyCelkovyCas + (novyRozdilMeziCykly * (puvodniPocetCyklu - aktualniCyklus));

        int pomocnyCelkovyCasHodiny = pomocnyCelkovyCas / 3600;
        int pomocnyCelkovyCasHodinyZbytekPoDeleni = pomocnyCelkovyCas % 3600;
        int pomocnyCelkovyCasMinuty = pomocnyCelkovyCasHodinyZbytekPoDeleni / 60;
        int pomocnyCelkovyCasSekundy = pomocnyCelkovyCasHodinyZbytekPoDeleni % 60;
        casCelkovy.setHour(pomocnyCelkovyCasHodiny);
        casCelkovy.setMin(pomocnyCelkovyCasMinuty);
        casCelkovy.setSec(pomocnyCelkovyCasSekundy);

        zobrazCelkovyCas();
    }


    private void prepoctiZbyvajiciCasCykly(int novyRozdilMeziCykly) {
        int pomocnyCasCviceni = casCviceni.getHour() * 3600 + casCviceni.getMin() * 60 + casCviceni.getSec();
        int pomocnyCasPauzy = casPauzy.getHour() * 3600 + casPauzy.getMin() * 60 + casPauzy.getSec();
        //   int pomocnyCasMeziTabatami = casMezitabatami.getHour()*3600 + casMezitabatami.getMin()*60 + casMezitabatami.getSec();
        int pomocnyCelkovyCas = casCelkovy.getHour() * 3600 + casCelkovy.getMin() * 60 + casCelkovy.getSec();
        pomocnyCelkovyCas = pomocnyCelkovyCas + (pomocnyCasCviceni * novyRozdilMeziCykly * (pocetTabat + 1))
                + (pomocnyCasPauzy * (novyRozdilMeziCykly) * (pocetTabat + 1));

        int pomocnyCelkovyCasHodiny = pomocnyCelkovyCas / 3600;
        int pomocnyCelkovyCasHodinyZbytekPoDeleni = pomocnyCelkovyCas % 3600;
        int pomocnyCelkovyCasMinuty = pomocnyCelkovyCasHodinyZbytekPoDeleni / 60;
        int pomocnyCelkovyCasSekundy = pomocnyCelkovyCasHodinyZbytekPoDeleni % 60;
        casCelkovy.setHour(pomocnyCelkovyCasHodiny);
        casCelkovy.setMin(pomocnyCelkovyCasMinuty);
        casCelkovy.setSec(pomocnyCelkovyCasSekundy);

        zobrazCelkovyCas();
    }

    private void prepoctiZbyvajiciCasTabaty(int novyRozdilMeziTabatami) {
        int pomocnyCasCviceni = casCviceni.getHour() * 3600 + casCviceni.getMin() * 60 + casCviceni.getSec();
        int pomocnyCasPauzy = casPauzy.getHour() * 3600 + casPauzy.getMin() * 60 + casPauzy.getSec();
        int pomocnyCasMeziTabatami = casMezitabatami.getHour() * 3600 + casMezitabatami.getMin() * 60 + casMezitabatami.getSec();
        int pomocnyCelkovyCas = casCelkovy.getHour() * 3600 + casCelkovy.getMin() * 60 + casCelkovy.getSec();
        pomocnyCelkovyCas = pomocnyCelkovyCas + (pomocnyCasCviceni * puvodniPocetCyklu * novyRozdilMeziTabatami)
                + (pomocnyCasPauzy * (puvodniPocetCyklu - 1) * novyRozdilMeziTabatami)
                + (pomocnyCasMeziTabatami * novyRozdilMeziTabatami);


        int pomocnyCelkovyCasHodiny = pomocnyCelkovyCas / 3600;
        int pomocnyCelkovyCasHodinyZbytekPoDeleni = pomocnyCelkovyCas % 3600;
        int pomocnyCelkovyCasMinuty = pomocnyCelkovyCasHodinyZbytekPoDeleni / 60;
        int pomocnyCelkovyCasSekundy = pomocnyCelkovyCasHodinyZbytekPoDeleni % 60;
        casCelkovy.setHour(pomocnyCelkovyCasHodiny);
        casCelkovy.setMin(pomocnyCelkovyCasMinuty);
        casCelkovy.setSec(pomocnyCelkovyCasSekundy);

        zobrazCelkovyCas();
    }

    //metoda, která u numperpickeru udělá ty dvě čárečky jinou barvou, než defaultní - jinak to nešlo
    private void setDividerColor(NumberPicker picker) {

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
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        //}
    }

    private void nastavCislice(long hodnotaAktualni) {
        if (hodnotaAktualni < 60) {
            if (velikostCislic != (R.dimen.velikostCasuOdpocitavaniDva)) {
                textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniDva));
                velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;

            }


            textViewCas.setText(vratDeseticisla((int) pomocny));
        } else {
            if (velikostCislic != (R.dimen.velikostCasuOdpocitavaniCtyri)) {
                textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniCtyri));
                velikostCislic = R.dimen.velikostCasuOdpocitavaniCtyri;
            }
            int minuty = (int) (hodnotaAktualni % 60);
            textViewCas.setText((vratDeseticisla((int) ((pomocny - minuty) / 60))) + ":" + vratDeseticisla(minuty));
        }
        textViewBeziciCasCisloKola.setText("");

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

        udelejLayout();
        nastavCislice(pomocny);


        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus) + "/" + String.valueOf(puvodniPocetCyklu));

        if (casCviceni.getSec() < 10) {
            textViewAktualniPocetTabat.setText(String.valueOf(casCviceni.getMin()) + ":0" + String.valueOf(casCviceni.getSec()));
        } else {
            textViewAktualniPocetTabat.setText(String.valueOf(casCviceni.getMin()) + ":" + String.valueOf(casCviceni.getSec()));
        }

        switch (stav) {
            case 0:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
                    //color
                    GradientDrawable shape = new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasPripravy);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPripravy));

                if (newLayout) {
                    dlazdiceOdpocitavace.setBackgroundColor(colorDlazdiceCasPripravy);
                    zmenNavigationBarColor(colorDlazdiceCasPripravy);
                }

                textViewCasNadpis.setText(R.string.nadpisCasPripravy);
                break;
            case 1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                    //color
                    GradientDrawable shape = new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasCviceni);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCviceni));

                if (newLayout) {
                    dlazdiceOdpocitavace.setBackgroundColor(colorDlazdiceCasCviceni);
                    zmenNavigationBarColor(colorDlazdiceCasCviceni);
                }

                textViewCasNadpis.setText(R.string.nadpisCasCviceni);
                break;
            case 2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzy));//  (R.drawable.background);
                    //color
                    GradientDrawable shape = new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasPauzy);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPauzy));

                if (newLayout) {
                    dlazdiceOdpocitavace.setBackgroundColor(colorDlazdiceCasPauzy);
                    zmenNavigationBarColor(colorDlazdiceCasPauzy);
                }

                textViewCasNadpis.setText(R.string.nadpisCasPauzy);
                break;
            case 3:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzymezitabatami));//  (R.drawable.background);
                } else
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPauzyMeziTabatami));

                if (newLayout) {
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPauzyMeziTabatami));
                    zmenNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPauzyMeziTabatami));
                }

                textViewCasNadpis.setText(R.string.nadpisCasPauzyMeziTabatmi);
                break;
            case 4:
                //konec odpočítávání
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorkonectabaty));//  (R.drawable.background);
                } else
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));

                if (newLayout) {
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                    zmenNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                }

                textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                textViewCas.setText(R.string.konec);
                textViewBeziciCasCisloKola.setText("");
                konecOdpocitavani = true;
                odpocitavac.cancel();
                textViewCasNadpis.setText("");
                Log.d("Jsem na konci: ", "3");
                break;
            case 5:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascooldown));//  (R.drawable.background);
                } else
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCoolDown));

                if (newLayout) {
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCoolDown));
                    zmenNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCoolDown));
                }

                textViewCasNadpis.setText("");
                break;
            default:
                break;
        }
        if (nastavProgressBar) { //zatím to řeším takto, že po otočení se progress vypne, když bude pauza, tak tam žádný nebude, ale když se to znovu rozjede, tak progress bude OK

            progressBar.setVisibility(View.GONE);
        }

        //tady se popasuje s tím, že když je to v pauze, aby to vykreslilo PLAY a čas do spodní prostřední dlaždice
        if (!pauzaNeniZmacknuta)  {

            // textViewPauza.setText(">");
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && !newLayout) {
                linearLayoutPauza.setBackgroundResource(R.mipmap.playlezatotabataactivity);
            } else {
                linearLayoutPauza.setBackgroundResource(R.mipmap.playstojatotabataactivity);
            }
            zobrazCelkovyCas();

        }
    }

    private void udelejLayout() {
        if (newLayout) {
            setContentView(R.layout.activity_custom_new);
            //progress bar kruhový
            progressBar = findViewById(R.id.progressbar);
            if (!nastavProgressBar || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                progressBar.setVisibility(View.GONE);
            }
        } else {
            setContentView(R.layout.activity_classic);

        }



        if (!adsDisabled) {
        // Reklama Goole nová - adaptivní banner
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) { }
        });
        initialLayoutComplete = false;
        adContainerView = findViewById(R.id.ad_view_container);
        adView = new AdView(this);
        adContainerView.addView(adView);
        adContainerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (!initialLayoutComplete) {
                            initialLayoutComplete = true;
                            AdUtils.loadBanner(adView, AD_UNIT_ID,ClassicActivity.this,adContainerView); //volám mnou vytvořenou statickou třídu/metodu
                            //    loadBanner();
                        }
                    }
                });
        }

        //   zavolejReview();

        textViewCas = (TextView) findViewById(R.id.textViewBeziciCas);
        textViewCas.setFontFeatureSettings("tnum"); //protože je nastaven font písma Inter, což je proporcionální písmo a z toho důvodu to při každé změně číslice
        //jakoby odskakuje, protože každá číslice je jinak široká, tak je nastavena tato vlastnost - tabulkové číslice
        textViewCasNadpis = (TextView) findViewById(R.id.textViewBeziciCasNadpis);
        textViewAktualniPocetCyklu = (TextView) findViewById(R.id.textViewAktualniPocetCyklu);
        textViewAktualniPocetTabat = (TextView) findViewById(R.id.textViewAktualniPocetTabat);
        dlazdiceOdpocitavace = (LinearLayout) findViewById(R.id.dlazdiceHlavniCas);

        textViewBeziciCasCisloKola = (TextView) findViewById(R.id.textViewBeziciCasCisloKola);



        dlazdicePodHlavnimCasem1 = (LinearLayout) findViewById(R.id.dlazdicePodHlavnimCasem1);
        dlazdicePodHlavnimCasem2 = (LinearLayout) findViewById(R.id.dlazdicePodHlavnimCasem2);
        dlazdicePodHlavnimCasem3 = (LinearLayout) findViewById(R.id.dlazdicePodHlavnimCasem3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (newLayout) {
                dlazdicePodHlavnimCasem1.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundsedoprusvitnykulaty));
                dlazdicePodHlavnimCasem2.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundsedoprusvitnykulaty));
                dlazdicePodHlavnimCasem3.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundsedoprusvitnykulaty));


            } else {
            dlazdicePodHlavnimCasem1.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundmodrykulaterohy));//  (R.drawable.background);
            dlazdicePodHlavnimCasem2.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundmodrykulaterohy));//  (R.drawable.background);
            dlazdicePodHlavnimCasem3.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundmodrykulaterohy));//  (R.drawable.background);

            GradientDrawable pozadi1 = new GradientDrawable();
            pozadi1.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            pozadi1.setColor(colorDlazdicePocetCyklu);
            GradientDrawable pozadi2 = new GradientDrawable();
            pozadi2.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            pozadi2.setColor(colorDlazdicePocetCyklu);
            GradientDrawable pozadi3 = new GradientDrawable();
            pozadi3.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            pozadi3.setColor(colorDlazdicePocetCyklu);
            GradientDrawable pozadi4 = new GradientDrawable();
            //   pozadi4.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            //   pozadi4.setColor(colorDlazdicePocetCyklu);
            //color
            dlazdicePodHlavnimCasem1.setBackground(pozadi1);
            dlazdicePodHlavnimCasem2.setBackground(pozadi2);
            dlazdicePodHlavnimCasem3.setBackground(pozadi3);
            }

        }

        linearLayoutPauza = findViewById(R.id.linearLayoutPauza);
        //  textViewPauza = (TextView) findViewById(R.id.textViewPauza);
        textViewCelkovyCas = (TextView) findViewById(R.id.textViewCelkovyCas);
        textViewCelkovyCas.setFontFeatureSettings("tnum");//protože je nastaven font písma Inter, což je proporcionální písmo a z toho důvodu to při každé změně číslice
        //jakoby odskakuje, protože každá číslice je jinak široká, tak je nastavena tato vlastnost - tabulkové číslice

        velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;


        //vytvoření dialogu počet tabat
        dialogNastaveniCasuKola = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
        vytvorDialogCasu(dialogNastaveniCasuKola, getResources().getString(R.string.nadpisNastavCasCviceni));

        //vytvoření dialogu počet cyklů
        dialogPocetCyklu = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
        vytvorDialogOpakovani(dialogPocetCyklu, getResources().getString(R.string.nadpisNastavPocetCyklu));


    }

    private void vytvorToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    private void zavlojejReviewNejake() {

        //  zavolejDruhyZpusobReview(); //tohle by mělo fungovat
        zavolejTretiZpusob();
    }

    private void zavolejTretiZpusob() {
        FiveStarsDialog fiveStarsDialog = new FiveStarsDialog(this, "machy79@seznam.cz"); //...ale poslání na e-mail mám vypnuté
        //     FiveStarsDialog fiveStarsDialog = new FiveStarsDialog(this);
        fiveStarsDialog.setRateText(getResources().getString(R.string.napisesrecenziprosim)) //("Will you review app?...5 stars please:-)?")
                .setTitle(getResources().getString(R.string.recenze))
// .setContentTextVisibility(View.GONE) // posibility to hide subtitle
                .setForceMode(false)
                .setStarColor(Color.YELLOW)
                //  .setEmailChooserText("Send email...")
                .setPositiveText(getResources().getString(R.string.ok))
                .setNegativeText(getResources().getString(R.string.notnow))
                .setNeutralText(getResources().getString(R.string.never))
                .setNeutralButtonColor(Color.parseColor("#CF0000"))
                .setNegativeButtonColor(Color.parseColor("#E67220"))
                .setPositiveButtonColor(Color.parseColor("#24AD02"))
                .setButtonTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL))
                .setContentTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL))
                .setStarColor(Color.parseColor("#ffee32"))
                .setNoRatingSelectedText("Please select your rating first.")
                .setUpperBound(5) // Market opened if a rating >= 5 is selected
                .setNegativeReviewListener(ClassicActivity.this) // OVERRIDE mail intent for negative review
                //            .setReviewListener(this) // Used to listen for reviews (if you want to track them )
                .showAfter(6); //počet spuštění, aby se žádost o review spustila (je to na konci v END)
    }

    public void statusBarcolor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(ClassicActivity.this, R.color.colorStatusBarColor));
        }
    }

/*    @Override
    public void onBackPressed(){
        //protože v manifestu mám nastaveno android:noHistory="true", tak při zpětném buttonu by se apka ukončila a neskočila by do mainu,
        //proto jsem tady zpětný button přetypoval, aby při jeho stisknutí to skočilo do mainu
        super.onBackPressed();
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.putExtra("zobrazIntent",0);
        startActivity(mainActivity);
        finish();
    }*/


    /**
     * Called when the activity has become visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override //pridat pro service
    protected void onStart() {
        super.onStart();
        //      Toast.makeText(ClassicActivity.this, "onStart", Toast.LENGTH_SHORT).show();
        //níže uvedené musí být tady, protože když se to sem vrací z onRestart, to znamená, že uživatel se vlrátí na aktivitu, tak kdyby to níže uvedené bylo v onCreate, tak by to dělalo neplechu
        Log.d("ServicaZPozadi", "onStart");

        Intent intent = new Intent(this, ClassicService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        service = new Intent(getApplicationContext(), ClassicService.class);


    }

    @Override //pridat pro service
    protected void onRestart() { //tímhle to projde, když se uživatel vrátí do aplikace z pozadí a klikne na aplikaci nebo když klikne na notifikaci
        super.onRestart();
        //     Toast.makeText(ClassicActivity.this, "onRestart", Toast.LENGTH_SHORT).show();
        Log.d("ServicaZPozadi", "onRestart");
        if ((s.getResult() != 0)) {
            Log.d("ServicaZPozadi", "onRestart2");
            //ZDE SE NAČÍTÁ, KDYŽ UŽIVATEL KLIKNE NA APLIKACI Z POZADÍ
            //      bound = true;
            spustenoZPozadi = true; //tahle proměnná je tady proto, že když se to neověřovalo, zda se apka spouští z pozadí nebo naopak z notifikace,
            //tak se killservice spustilo dvakrát a dělalo to samozřejmě neplechu
            nactiZeServisy();
            spustOdpocitavac();
        } else Log.d("ServicaZPozadi", "onRestart3");
    }

    @Override //pridat pro service
    protected void onStop() {//když uživatel dá aplikaci do pozadí, tak teprve potom se spustí servica a nastaví se v service odpočítávání, sem dám asi všechny proměnné
      //  if (!isClassicServiceRunning()) {
            // Služba `ClassicService` nebeží, můžete ji spustit a provést další akce.

            super.onStop();
            if(s!=null && buttonBackIsNotPressed) {
                //když máme service connection, tak se nemusí startovat servica, ta už je inicializovaná, stačí v ní jen vyvolat metody
                s.nastavHodnoty(aktualniCyklus, puvodniPocetCyklu, casPripravy, colorDlazdiceCasPripravy,
                        casCviceni, colorDlazdiceCasCviceni, casPauzy, colorDlazdiceCasPauzy, casCelkovy,
                        colorDlazdicePocetCyklu, stav, pomocny, pauzaNeniZmacknuta, pocetCyklu);
                Log.d("ChybaCykly", String.valueOf(pocetCyklu));
                s.nastavOdpocitavani();

                s.nastavZvuky(zvukStart, zvukStop, zvukCelkovyKonec,
                        zvukCountdown, zvukPulkaCviceni, casPulkyKola,
                        casPulkyKolaAktualni, zvukPredkoncemKola, casZvukuPredKoncemKola,
                        hlasitost, maxHlasitost, volume);

                // s.setNotification();
            }


            //nakonec musím spustit startForegroundService, aby se v service mohla spustit metoda onStartCommand, ve které je return START_NOT_STICKY - to tady je proto,
            //aby se po uvedení telefonu po vypnutí tato servica po cca 1 minutě nekillnula
        if(service!=null && buttonBackIsNotPressed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    startForegroundService(service);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else
                try {
                    this.startService(service);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }


/*        if(s!=null && buttonBackIsNotPressed) { //tady změna v service 20.12.2024!!!

            try {
                s.setNotification();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

        if(odpocitavac!=null) {
            odpocitavac.cancel();
        }

      //  }

        Log.d("nofifikace", "3333");


    }

    @Override  //pridat pro service
    protected void onDestroy() {
        if (odpocitavac != null) {
            odpocitavac.cancel();
        }
        znicService();
        unregisterReceiver(mMessageReceiver);
        //    android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    //metody pro service
    private void nactiZeServisy() {

        preskocVypisCasu = s.getPreskocVypisCasu();
        casCelkovy = s.getCasCelkovy();
        pocetCyklu = s.getPocetCyklu();
        pauzaNeniZmacknuta = s.getPauzaNeniZmacknuta();

        aktualniCyklus = s.getAktualniCyklus();
        puvodniPocetCyklu = s.getPuvodniPocetCyklu();
        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus) + "/" + String.valueOf(puvodniPocetCyklu));
        stav = s.getStav();
        switch (stav) {
            case 0:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
                    //color
                    GradientDrawable shape = new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasPripravy);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPripravy));
                if (newLayout) {
                    dlazdiceOdpocitavace.setBackgroundColor(colorDlazdiceCasPripravy);
                    zmenNavigationBarColor(colorDlazdiceCasPripravy);
                }
                maxHodnotaProgressBar = casPripravy.getSec() + casPripravy.getMin() * 60 + casPripravy.getHour() * 3600 + 1;
                textViewCasNadpis.setText(R.string.nadpisCasPripravy);
                Log.d("progress-bar2", "maxHodnotaProgressBar0 =" + maxHodnotaProgressBar);
                break;
            case 1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                    //color
                    GradientDrawable shape = new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasCviceni);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCviceni));
                if (newLayout) {
                    dlazdiceOdpocitavace.setBackgroundColor(colorDlazdiceCasCviceni);
                    zmenNavigationBarColor(colorDlazdiceCasCviceni);
                }
                maxHodnotaProgressBar = casCviceni.getSec() + casCviceni.getMin() * 60 + casCviceni.getHour() * 3600 + 1;
                textViewCasNadpis.setText(R.string.nadpisCasCviceni);
                Log.d("progress-bar2", "maxHodnotaProgressBar1 =" + maxHodnotaProgressBar);
                break;
            case 2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzy));//  (R.drawable.background);
                    //color
                    GradientDrawable shape = new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasPauzy);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPauzy));
                if (newLayout) {
                    dlazdiceOdpocitavace.setBackgroundColor(colorDlazdiceCasPauzy);
                    zmenNavigationBarColor(colorDlazdiceCasPauzy);
                }
                maxHodnotaProgressBar = casPauzy.getSec() + casPauzy.getMin() * 60 + casPauzy.getHour() * 3600 + 1;
                textViewCasNadpis.setText(R.string.nadpisCasPauzy);
                Log.d("progress-bar2", "maxHodnotaProgressBar2 =" + maxHodnotaProgressBar);
                break;
            case 3:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                    //color
                    GradientDrawable shape = new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasCviceni);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCviceni));
                if (newLayout) {
                    dlazdiceOdpocitavace.setBackgroundColor(colorDlazdiceCasCviceni);
                    zmenNavigationBarColor(colorDlazdiceCasCviceni);
                }
                maxHodnotaProgressBar = casCviceni.getSec() + casCviceni.getMin() * 60 + casCviceni.getHour() * 3600 + 1;
                textViewCasNadpis.setText(R.string.nadpisCasCviceni);
                Log.d("progress-bar2", "maxHodnotaProgressBar3 =" + maxHodnotaProgressBar);
                break;
            case 4:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorkonectabaty));//  (R.drawable.background);
                } else
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                if (newLayout) {
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                    zmenNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                }
                maxHodnotaProgressBar = 0;
                textViewCasNadpis.setText("");
                Log.d("progress-bar2", "maxHodnotaProgressBar4 =" + maxHodnotaProgressBar);
                break;
            case 5:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorkonectabaty));//  (R.drawable.background);
                } else
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                if (newLayout) {
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                    zmenNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                }
                maxHodnotaProgressBar = 0;
                textViewCasNadpis.setText("");
                Log.d("progress-bar2", "maxHodnotaProgressBar5 =" + maxHodnotaProgressBar);
                break;

        }

        if (pauzaNeniZmacknuta) {
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
        Log.d("progress-bar", String.valueOf(pomocny) +"-ze service načteno v Activity pomocny");
        nastavCislice(pomocny);

        zobrazCelkovyCas();

        if (spustenoZPozadi) { //když to sem projde z pozadí a ne jen z notifikace, tak je vlastně tato activita
            //načtena a je potřebe jen servisu killnout a ne ji zcela zničit, je potřeba si na ni nechat connection, tak proto je zde tato podmínka
            Log.d("ServicaZPozadi", "spustenoZPozadi=true");
            s.killService();
            spustenoZPozadi = false;
        } else znicService();
    }

    private void znicService() { //aby šlo servicu zničit, musí se unbindnout a vlákno v ní, tedy počítadlo, zničit také
        if (bound) {
            Log.d("Servica1", "2");
            //      Toast.makeText(ClassicActivity.this, "unbind", Toast.LENGTH_SHORT).show();
            unbindService(serviceConnection);
            getApplicationContext().stopService(service);
            bound = false;
            s.killService();
            Log.d("Servica1", "3");

        }
    }



    private void zmenNavigationBarColor(int NavigationBarColor) {
        Log.d("barvaNavigationBar", "111");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().setNavigationBarColor(NavigationBarColor);
            Log.d("barvaNavigationBar", "111");
        }
    }


}
