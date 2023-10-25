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
import com.machy1979ii.intervaltimer.models.MyTime;

import angtrim.com.fivestarslibrary.FiveStarsDialog;
import angtrim.com.fivestarslibrary.NegativeReviewListener;
import angtrim.com.fivestarslibrary.ReviewListener;
import com.machy1979ii.intervaltimer.funkce.PraceSeZvukemTabata;
import com.machy1979ii.intervaltimer.services.ClassicService;
import com.machy1979ii.intervaltimer.services.TabataService;

public class TabataActivity extends AppCompatActivity implements NegativeReviewListener, ReviewListener {

    //   ReviewManager manager;
    //   ReviewInfo reviewInfo = null;
    private static final String HOME_ACTIVITY_TAG = TabataActivity.class.getSimpleName();

    private Dialog dialogPoctuTabat;
    private Dialog dialogPocetCyklu;
    private boolean pauzaNeniZmacknuta = true;

    private MyTime casPripravy;
    private int colorDlazdiceCasPripravy; //color
    private MyTime casCviceni;
    private int colorDlazdiceCasCviceni; //color
    private MyTime casPauzy;
    private int colorDlazdiceCasPauzy; //color
    private MyTime casMezitabatami;
    private int colorDlazdiceCasPauzyMeziTabatami; //color
    private MyTime casCoolDown;
    private int colorDlazdiceCasCoolDown; //color
    private MyTime casCelkovy;

    private int pocetTabat;
    private int colorDlazdiceTabaty; //color
    private int aktualniTabata = 1;
    private int puvodniPocetTabat;
    private int puvodniPocetCyklu;
    private int aktualniCyklus = 1;
    private int pocetCyklu;
    private int pauzaMeziTabatami;

    private TextView textViewCas;
    private TextView textViewCasNadpis;
    private TextView textViewAktualniPocetCyklu;
    private TextView textViewAktualniPocetTabat;
    private TextView textViewPauza;
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
    private int zvukStart =1;
    private int zvukStop =1;
    private int zvukCelkovyKonec=1;
    private int zvukCountdown=1;
    private int zvukPauzeMeziTabatami =1;
    private MediaPlayer mediaPlayer = null;
    private int hlasitost = 100;
    private int maxHlasitost = 100;
    float volume;

    //proměnné pro servicu
    private Boolean bound = false;
    private Boolean spustenoZPozadi = false; //příznak, že aplikace byla spuštěna z pozadí a ne z notifikace
    private Intent service;
    private TabataService s;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            TabataService.MyBinder b= (TabataService.MyBinder) binder;
            if (s==null) {
                //       Toast.makeText(ClassicActivity.this, "Connected s=null", Toast.LENGTH_SHORT).show();
                s = b.getService();
            } else {
                //     Toast.makeText(ClassicActivity.this, "Connected s!=null", Toast.LENGTH_SHORT).show();
            }
            bound = true;

            Log.d("Servica1","onServiceConnected ---");

            if((s.getResult()!=0)) {
                //v momentě, kdy se connectne k service, tak se z ní čačte result, pokud je 0, tak je vypnutá, nebo nespuštěná, pokud je nějaké číslo
                //tak se z ní to číslo načte a začne se odpočítávat od toho čísla, sem dám i načtení dalších dat ze servisy
                //načtení ze servisy jsem se pokoušel dávat do onCreata, onResume, onRestart atd., ale připojení k service má asi nějaké nepatrné zpoždění, tak to fungovalo jen tady

                //ZDE SE NAČÍTÁ, KDYŽ UŽIVATEL KLIKNE NA NOTIFIKACI
                //      Toast.makeText(ClassicActivity.this, "from service nacte", Toast.LENGTH_SHORT).show();
                Log.d("Servica1","onServiceConnected");
                nactiZeServisy();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            s = null;
            bound = false;
            Toast.makeText(TabataActivity.this, "onServiceDisconnected", Toast.LENGTH_SHORT).show();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mMessageReceiver, new IntentFilter("znicTabataActivityATabataService"), null, null, RECEIVER_NOT_EXPORTED); //pridat pro service, protože cílím na Android 14, tak jsem musel přidat ještě , null, null, RECEIVER_NOT_EXPORTED, jakože  žůže přijímat pouze vysílání z této aplikace
        } else  registerReceiver(mMessageReceiver, new IntentFilter("znicTabataActivityATabataService"));

        //zamezí vypnutí obrazovky do úsporného režimu po nečinnosti, šlo to udělat
        //v XML -  android:keepScreenOn="true", ale to bych to musel dát do všech XML (land...)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //     fanfareZvuk = MediaPlayer.create(getApplicationContext(), R.raw.ramagochiviolinend);
        //      startZvuk = MediaPlayer.create(getApplicationContext(), R.raw.boxstart);
        //   restZvuk = MediaPlayer.create(getApplicationContext(), R.raw.boxstop);

//tady jsem zkoušel, aby se při backgroundu nevypnula aplikace a pokračovala, ale nakonec jsem to do apky nedal, jestli to
        //tam budu chtít dát, tak do AndroidManifestu musím dát tohle:  <uses-permission android:name="android.permission.WAKE_LOCK" />
     //   PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
     //    @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
     //           "MyWakelockTag");
     //   wakeLock.acquire();

        //tohle tady je, aby statusbar měl určitou barvu, jako barva pozadí reklamy, nešlo mi to udělat v XML lajoutu, tak to řeším takhle
        statusBarcolor();

        if (getIntent().getExtras() != null) { //play google mi zde házel chybu, tak ještě ověřuji, zda Bundle extras není null
            //colors
            colorDlazdiceCasCviceni = getIntent().getExtras().getInt("barvaCviceni", getResources().getColor(R.color.colorCasCviceni)); //color
            colorDlazdiceCasPauzy = getIntent().getExtras().getInt("barvaPauzy",getResources().getColor(R.color.colorCasPauzy)); //color
            colorDlazdiceTabaty = getIntent().getExtras().getInt("barvaTabaty",getResources().getColor(R.color.colorPocetTabat)); //color
            colorDlazdiceCasPripravy = getIntent().getExtras().getInt("barvaPripravy",getResources().getColor(R.color.colorCasPripravy)); //color
            colorDlazdiceCasPauzyMeziTabatami = getIntent().getExtras().getInt("barvaPauzyMeziTabatami",getResources().getColor(R.color.colorCasPauzyMeziTabatami)); //color
            colorDlazdiceCasCoolDown = getIntent().getExtras().getInt("barvaCoolDown",getResources().getColor(R.color.colorCasCoolDown));

            if (getIntent().getExtras().getParcelable("caspripavy") != null) {
                casPripravy = getIntent().getExtras().getParcelable("caspripavy");
                casCviceni = getIntent().getExtras().getParcelable("cascviceni");
                casPauzy = getIntent().getExtras().getParcelable("caspauzy");
                casMezitabatami = getIntent().getExtras().getParcelable("caspauzynezitabatami");
                casCoolDown = getIntent().getExtras().getParcelable("cascooldown");
                casCelkovy = getIntent().getExtras().getParcelable("casCelkovy");
            } else {
                casPripravy = new MyTime(0, 1, 0);
                casCviceni = new MyTime(0, 0, 20);
                casPauzy = new MyTime(0, 0, 20);
                casMezitabatami = new MyTime(0, 1, 0);
                casCoolDown = new MyTime(0, 1, 0);
                casCelkovy = new MyTime(0, 60, 0);
            }

            puvodniPocetCyklu = getIntent().getExtras().getInt("pocetcyklu",8);
            puvodniPocetTabat = getIntent().getExtras().getInt("pocettabat",4);

            zvukStart = getIntent().getIntExtra("zvukstart",1);
            zvukStop = getIntent().getIntExtra("zvukstop",1);
            zvukCelkovyKonec = getIntent().getIntExtra("zvukcelkovykonec",1);
            zvukCountdown = getIntent().getIntExtra("zvukcountdown",1);
            zvukPauzeMeziTabatami = getIntent().getIntExtra("zvukstoptabatas",1);
            hlasitost = getIntent().getIntExtra("hlasitostTabata", 100);
        } else {
            colorDlazdiceCasCviceni = getResources().getColor(R.color.colorCasCviceni);
            colorDlazdiceCasPauzy = getResources().getColor(R.color.colorCasPauzy);
            colorDlazdiceTabaty = getResources().getColor(R.color.colorPocetTabat);
            colorDlazdiceCasPripravy = getResources().getColor(R.color.colorCasPripravy);
            colorDlazdiceCasPauzyMeziTabatami = getResources().getColor(R.color.colorCasPauzyMeziTabatami);
            colorDlazdiceCasCoolDown = getResources().getColor(R.color.colorCasCoolDown);

            casPripravy = new MyTime(0, 1, 0);
            casCviceni = new MyTime(0, 0, 20);
            casPauzy = new MyTime(0, 0, 20);
            casMezitabatami = new MyTime(0, 1, 0);
            casCoolDown = new MyTime(0, 1, 0);
            casCelkovy = new MyTime(0, 60, 0);

            puvodniPocetCyklu = 8;
            puvodniPocetTabat = 4;


            zvukStart = 1;
            zvukStop = 1;
            zvukCelkovyKonec = 1;
            zvukCountdown= 1;
            zvukPauzeMeziTabatami = 1;
            hlasitost = 100;


        }




        udelejLayout();


        volume = (float) (1 - (Math.log(maxHlasitost - hlasitost) / Math.log(maxHlasitost)));

      
        //countdown zvuk řeším jinak, než ostatní zvuky, předělával jsem to, tak abych se moc nevrtal v kodu, tak to nechám takhle jinak
        //       tikZvuk3 = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabataTabata.vratZvukCountdownPodlePozice(zvukCountdown));
        //       tikZvuk2 = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabataTabata.vratZvukCountdownPodlePozice(zvukCountdown));
        //       tikZvuk1 = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabataTabata.vratZvukCountdownPodlePozice(zvukCountdown));
        mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukCountdownPodlePozice(zvukCountdown));

        spustCasovac();
    }



    private void spustCasovac() {
        pauzaMeziTabatami = casMezitabatami.getSec();
        pocetCyklu = puvodniPocetCyklu;
        pocetTabat = puvodniPocetTabat;

        //nastavení časovače - prostě to bude odpočítávat
        //  int pomocnyCelkovyCasMeziTabatami = pauzaMeziTabatami*(pocetTabat-1);
        //   dobaCasovace = casPripravy.getSec() +((casCviceni.getSec()+casPauzy.getSec())*pocetCyklu +pomocnyCelkovyCasMeziTabatami)*pocetTabat;
        dobaCasovace = 100000; //nebudeme to počítat, ale pojede to tolik sekunc
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
            //color
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
            shape.setColor(colorDlazdiceCasPripravy);
            dlazdiceOdpocitavace.setBackground(shape);

        } else  dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasPripravy));

        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus)+"/"+String.valueOf(puvodniPocetCyklu));
        textViewAktualniPocetTabat.setText(String.valueOf(aktualniTabata)+"/"+String.valueOf(puvodniPocetTabat));

        pomocny = casPripravy.getSec()+1 + casPripravy.getMin()*60 +casPripravy.getHour()*3600;
        if (pomocny < 60) {
            textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniDva));
            velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;
        }
        pocetCyklu = pocetCyklu -1;
        pocetTabat = pocetTabat -1;
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

                switch (stav){
                    case 0:
                        //vlákno příprava
                        pomocny = pomocny-1;
                        if (pomocny <= 0) {
                            //tady   restZvuk.start();
                            //  PraceSeZvukemTabata.spustZvukStartStop(getApplicationContext(),zvukStart);
                            //nakonec jsem to musel vyřešit takhle, chtěl jsem spouštět zvuk přímo ve třídě PraceSeZvukemTabata, ale po přehrání pár zvuků
                            //to přestalo přehrávat zvuky, tak jsem to udělal takhle, navíc jsem to pořešil reset() a release(), našel jsem to v nějakém návodu
                            if (mediaPlayer != null) {
                                mediaPlayer.reset();
                                mediaPlayer.release();
                                mediaPlayer = null;
                            }
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukStartStopPodlePozice(zvukStart));
                            mediaPlayer.setVolume(volume,volume);
                            mediaPlayer.start();


                            stav = (byte) (stav + 1);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                                //color
                                GradientDrawable shape =  new GradientDrawable();
                                shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                shape.setColor(colorDlazdiceCasCviceni);
                                dlazdiceOdpocitavace.setBackground(shape);
                                //color
                            } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasCviceni));


                            textViewCasNadpis.setText(R.string.nadpisCasCviceni);
                            pomocny=casCviceni.getSec() +  casCviceni.getMin()*60 +  casCviceni.getHour()*3600 + 1;
                            pomocny = pomocny -1; //protože při GO už to je jedna sekunda a pak mi to při celkovém času nehrálo

                            textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniDva));
                            velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;
                            textViewCas.setText(R.string.cvic);
                            if (aktualniCyklus>1)  {
                                textViewBeziciCasCisloKola.setText(String.valueOf(aktualniCyklus));
                            }
                            preskocVypisCasu = true;

                        }

                        if (preskocVypisCasu) {
                            preskocVypisCasu =false;
                        } else {
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
                                default:
                                    break;
                            }

                        }


                        break;
                    case 1:
                        //vlákno cvičení
                        pomocny = pomocny-1;
                        if (pomocny <= 0) {
                            if(pocetCyklu== 0) {
                                if (pocetTabat== 0) {
                                    //    fanfareZvuk.start();

                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukKonecPodlePozice(zvukCelkovyKonec));
                                    mediaPlayer.setVolume(volume,volume);
                                    mediaPlayer.start();

                                    if (casCoolDown.getSec() == 0 && casCoolDown.getMin()==0) {
                                        //     stav = 4; //tohle jsem doplnil při změně color, aby to na konci, když se otočí
                                        //obrazovka, nehodilo žlutou barvu, ale barvu konec tabaty
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorkonectabaty));//  (R.drawable.background);
                                            //color
                                            GradientDrawable shape =  new GradientDrawable();
                                            shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                            shape.setColor(colorDlazdiceCasCoolDown);
                                            dlazdiceOdpocitavace.setBackground(shape);
                                            //color
                                        } else  dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorKonecTabaty));

                                        textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                                        velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                                        textViewCas.setText(R.string.konec);
                                        textViewBeziciCasCisloKola.setText("");
                                        stav =4;
                                        preskocVypisCasu =true;
                                    } else {
                                        stav = 5;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascooldown));//  (R.drawable.background);
                                            //color
                                            GradientDrawable shape =  new GradientDrawable();
                                            shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                            shape.setColor(colorDlazdiceCasCoolDown);
                                            dlazdiceOdpocitavace.setBackground(shape);
                                            //color
                                        } else   dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasCoolDown));

                                        textViewCasNadpis.setText(R.string.nadpisCasCoolDown);
                                        pomocny=casCoolDown.getSec() +  casCoolDown.getMin()*60 +  casCoolDown.getHour()*3600+1;

                                        textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniCoolDown));
                                        velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                                        textViewCas.setText(R.string.coolDown);
                                        textViewBeziciCasCisloKola.setText("");
                                        preskocVypisCasu = true;
                                    }


                                } else {
                                    //tady   restZvuk.start();
                                    // PraceSeZvukemTabata.spustZvukStartStop(getApplicationContext(),zvukStop);

                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukStartStopPodlePozice(zvukPauzeMeziTabatami));
                                    mediaPlayer.setVolume(volume,volume);
                                    mediaPlayer.start();

                                    stav = 3;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzymezitabatami));//  (R.drawable.background);
                                        //color
                                        GradientDrawable shape =  new GradientDrawable();
                                        shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                        shape.setColor(colorDlazdiceCasPauzyMeziTabatami);
                                        dlazdiceOdpocitavace.setBackground(shape);
                                        //color
                                    } else    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasPauzyMeziTabatami));

                                    textViewCasNadpis.setText(R.string.nadpisOdpocinekMeziTabatami);
                                    pomocny=casMezitabatami.getSec() +  casMezitabatami.getMin()*60 +  casMezitabatami.getHour()*3600+1;
                                    pomocny = pomocny -1; //protože při REST už to je jedna sekunda a pak mi to při celkovém času nehrálo

                                    textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                                    velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                                    textViewCas.setText(R.string.odpocinek);
                                    textViewBeziciCasCisloKola.setText("");
                                    preskocVypisCasu = true;
                                    pocetTabat = pocetTabat-1;
                                }
                            } else {
                                //tady                            restZvuk.start();
                                //  PraceSeZvukemTabata.spustZvukStartStop(getApplicationContext(),zvukStop);

                                if (mediaPlayer != null) {
                                    mediaPlayer.reset();
                                    mediaPlayer.release();
                                }
                                mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukStartStopPodlePozice(zvukStop));
                                mediaPlayer.setVolume(volume,volume);
                                mediaPlayer.start();

                                stav = (byte) (stav + 1);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzy));//  (R.drawable.background);
                                    //color
                                    GradientDrawable shape =  new GradientDrawable();
                                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                    shape.setColor(colorDlazdiceCasPauzy);
                                    dlazdiceOdpocitavace.setBackground(shape);
                                    //color
                                } else    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasPauzy));

                                textViewCasNadpis.setText(R.string.nadpisCasPauzy);
                                pomocny=casPauzy.getSec()+  casPauzy.getMin()*60 +  casPauzy.getHour()*3600+1;
                                pomocny = pomocny -1; //protože při REST už to je jedna sekunda a pak mi to při celkovém času nehrálo

                                textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                                velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                                textViewCas.setText(R.string.odpocinek);
                                textViewBeziciCasCisloKola.setText("");
                                preskocVypisCasu = true;
                            }
                        }

                        if (preskocVypisCasu) {
                            preskocVypisCasu =false;
                        } else {
                            nastavCislice(pomocny);
                            switch ((int) pomocny){
                                case 4:
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
                                default:
                                    break;
                            }

                        }






                        break;
                    case 2:
                        //vlákno pauza
                        pomocny=pomocny-1;
                        if (pomocny <= 0) {
                            if(pocetCyklu== 0) {
                                if(pocetTabat== 0) {
                                    if (casCoolDown.getSec() == 0 && casCoolDown.getMin()==0) {
                                        //   fanfareZvuk.start();

                                        if (mediaPlayer != null) {
                                            mediaPlayer.reset();
                                            mediaPlayer.release();
                                        }
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukKonecPodlePozice(zvukCelkovyKonec));
                                        mediaPlayer.setVolume(volume,volume);
                                        mediaPlayer.start();

                                        stav = 4;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorkonectabaty));//  (R.drawable.background);
                                            //color
                                            GradientDrawable shape =  new GradientDrawable();
                                            shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                            shape.setColor(colorDlazdiceCasCoolDown);
                                            dlazdiceOdpocitavace.setBackground(shape);
                                            //color
                                        } else  dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorKonecTabaty));
                                        textViewCas.setText("");
                                        textViewBeziciCasCisloKola.setText("");
                                        preskocVypisCasu =true;
                                    } else {
                                        //tady                            restZvuk.start();
                                        //   PraceSeZvukemTabata.spustZvukStartStop(getApplicationContext(),zvukCelkovyKonec);

                                        if (mediaPlayer != null) {
                                            mediaPlayer.reset();
                                            mediaPlayer.release();
                                        }
                                        mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukKonecPodlePozice(zvukCelkovyKonec));
                                        mediaPlayer.setVolume(volume,volume);
                                        mediaPlayer.start();
                                        stav = 5;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascooldown));//  (R.drawable.background);
                                            //color
                                            GradientDrawable shape =  new GradientDrawable();
                                            shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                            shape.setColor(colorDlazdiceCasCoolDown);
                                            dlazdiceOdpocitavace.setBackground(shape);
                                            //color
                                        } else   dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasCoolDown));
                                        textViewCasNadpis.setText(R.string.nadpisCasCoolDown);
                                        pomocny=casCoolDown.getSec() +  casCoolDown.getMin()*60 +  casCoolDown.getHour()*3600+1;

                                        textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniCoolDown));
                                        velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                                        textViewCas.setText(R.string.coolDown);
                                        textViewBeziciCasCisloKola.setText("");
                                        preskocVypisCasu = true;
                                    }

                                } else  {
                                    //tady                            restZvuk.start();
                                    //  PraceSeZvukemTabata.spustZvukStartStop(getApplicationContext(),zvukStop);

                                    if (mediaPlayer != null) {
                                        mediaPlayer.reset();
                                        mediaPlayer.release();
                                    }
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukStartStopPodlePozice(zvukPauzeMeziTabatami));
                                    mediaPlayer.setVolume(volume,volume);
                                    mediaPlayer.start();
                                    stav = 3;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzymezitabatami));//  (R.drawable.background);
                                        //color
                                        GradientDrawable shape =  new GradientDrawable();
                                        shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                        shape.setColor(colorDlazdiceCasPauzyMeziTabatami);
                                        dlazdiceOdpocitavace.setBackground(shape);
                                        //color
                                    } else    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasPauzyMeziTabatami));
                                    textViewCasNadpis.setText(R.string.nadpisCasPauzy);
                                    pomocny=casMezitabatami.getSec() +  casMezitabatami.getMin()*60 +  casMezitabatami.getHour()*3600+1;
                                    pomocny = pomocny -1; //protože při REST už to je jedna sekunda a pak mi to při celkovém času nehrálo

                                    textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                                    velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                                    textViewCas.setText(R.string.odpocinek);
                                    textViewBeziciCasCisloKola.setText("");
                                    preskocVypisCasu = true;
                                    pocetTabat = pocetTabat-1;
                                }
                            } else {
                                //tady                            restZvuk.start();
                                //  PraceSeZvukemTabata.spustZvukStartStop(getApplicationContext(),zvukStart);

                                if (mediaPlayer != null) {
                                    mediaPlayer.reset();
                                    mediaPlayer.release();
                                }
                                mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukStartStopPodlePozice(zvukStart));
                                mediaPlayer.setVolume(volume,volume);
                                mediaPlayer.start();
                                stav = 1;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                                    //color
                                    GradientDrawable shape =  new GradientDrawable();
                                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                    shape.setColor(colorDlazdiceCasCviceni);
                                    dlazdiceOdpocitavace.setBackground(shape);
                                    //color
                                } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasCviceni));
                                textViewCasNadpis.setText(R.string.nadpisCasCviceni);
                                aktualniCyklus = aktualniCyklus +1;
                                textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus)+"/"+String.valueOf(puvodniPocetCyklu));
                                pomocny=casCviceni.getSec() +  casCviceni.getMin()*60 +  casCviceni.getHour()*3600+1;
                                pomocny = pomocny -1; //protože při GO už to je jedna sekunda a pak mi to při celkovém času nehrálo


                                textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniDva));
                                velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;
                                textViewCas.setText(R.string.cvic);
                                if (aktualniCyklus>1)  {
                                    textViewBeziciCasCisloKola.setText(String.valueOf(aktualniCyklus));
                                }
                                preskocVypisCasu = true;

                                pocetCyklu = pocetCyklu -1;
                            }

                        }

                        if (preskocVypisCasu) {
                            preskocVypisCasu =false;
                        } else {
                            nastavCislice(pomocny);
                            switch ((int) pomocny){
                                case 4:
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
                                default:
                                    break;
                            }
                        }

                        break;
                    case 3:
                        //vlákno pauza mezi tabatami
                        pomocny=pomocny-1;
                        if (pomocny <= 0) {
                            //tady                            restZvuk.start();
                            // PraceSeZvukemTabata.spustZvukStartStop(getApplicationContext(),zvukStart);

                            if (mediaPlayer != null) {
                                mediaPlayer.reset();
                                mediaPlayer.release();
                            }
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukStartStopPodlePozice(zvukStart));
                            mediaPlayer.setVolume(volume,volume);
                            mediaPlayer.start();

                            stav = 1;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                                //color
                                GradientDrawable shape =  new GradientDrawable();
                                shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                shape.setColor(colorDlazdiceCasCviceni);
                                dlazdiceOdpocitavace.setBackground(shape);
                                //color
                            } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasCviceni));
                            textViewCasNadpis.setText(R.string.nadpisCasCviceni);
                            aktualniCyklus = 1;
                            textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus)+"/"+String.valueOf(puvodniPocetCyklu));
                            aktualniTabata = aktualniTabata +1;
                            textViewAktualniPocetTabat.setText(String.valueOf(aktualniTabata)+"/"+String.valueOf(puvodniPocetTabat));
                            pomocny=casCviceni.getSec() +  casCviceni.getMin()*60 +  casCviceni.getHour()*3600+1;
                            pomocny = pomocny -1; //protože při GO už to je jedna sekunda a pak mi to při celkovém času nehrálo

                            textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniDva));
                            velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;
                            textViewCas.setText(R.string.cvic);
                            if (aktualniCyklus>1)  {
                                textViewBeziciCasCisloKola.setText(String.valueOf(aktualniCyklus));
                            }
                            preskocVypisCasu = true;
                            pocetCyklu = puvodniPocetCyklu-1;


                        }

                        if (preskocVypisCasu) {
                            preskocVypisCasu =false;
                        } else {
                            nastavCislice(pomocny);
                            switch ((int) pomocny){
                                case 4:
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
                                default:
                                    break;
                            }
                        }

                        break;
                    case 4:
                        //konec odpočítávání
                        textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                        textViewCas.setText(R.string.konec);
                        textViewBeziciCasCisloKola.setText("");
                        zavlojejReviewNejake();
                        odpocitavac.cancel();
                        break;

                    case 5:
                        //čas cool down, je to tam doděláno dodatečně, proto to má číslo 5 a ne 4
                        pomocny = pomocny-1;
                        if (pomocny <= 0) {
                            //   restZvuk.start();

                            //  PraceSeZvukemTabata.spustZvukKonec(getApplicationContext(),zvukCelkovyKonec);

                            if (mediaPlayer != null) {
                                mediaPlayer.reset();
                                mediaPlayer.release();
                            }
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukKonecPodlePozice(zvukCelkovyKonec));
                            mediaPlayer.setVolume(volume,volume);
                            mediaPlayer.start();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorkonectabaty));//  (R.drawable.background);
                                //color
                                GradientDrawable shape =  new GradientDrawable();
                                shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                                shape.setColor(colorDlazdiceCasCoolDown);
                                dlazdiceOdpocitavace.setBackground(shape);
                                //color
                            } else  dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorKonecTabaty));
                            textViewCasNadpis.setText("");
                            stav =4;
                            preskocVypisCasu =false;

                        }

                        if (preskocVypisCasu) {
                            preskocVypisCasu =false;
                        } else {
                            nastavCislice(pomocny);
                            switch ((int) pomocny){
                                case 4:
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
                                default:
                                    break;
                            }

                        }
                        break;

                    default:
                        break;

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

    public void showTimePickerDialogNastavPocetTabatVCasovaci(View v) {
        pauzaNeniZmacknuta = false;
        //  textViewPauza.setText(">");
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            linearLayoutPauza.setBackgroundResource(R.mipmap.playlezatotabataactivity);
        } else {
            linearLayoutPauza.setBackgroundResource(R.mipmap.playstojatotabataactivity);
        }
        //    linearLayoutPauza.setBackgroundResource(R.mipmap.playlezatotabataactivity);
        dialogPoctuTabat.show();

    }

    public void showPickerNastavPocetCykluVTabate(View v) {
        pauzaNeniZmacknuta = false;
        //   textViewPauza.setText(">");
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
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
            dialog.getWindow().setBackgroundDrawable(getBaseContext().getResources().getDrawable(R.drawable.backgroundmodrykulaterohy));//  (R.drawable.background);

                 //color
                GradientDrawable shape =  new GradientDrawable();
                shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                shape.setColor(colorDlazdiceTabaty);
                dialog.getWindow().setBackgroundDrawable(shape);
                //color


        } else  dialog.getWindow().setBackgroundDrawableResource(R.color.colorPocetTabat);

        final NumberPicker pocitacDesitky = (NumberPicker) dialog.findViewById(R.id.npDesitky);
        setDividerColor(pocitacDesitky);
        pocitacDesitky.setMinValue(0);
        pocitacDesitky.setMaxValue(9);
        final NumberPicker pocitacJednotky = (NumberPicker) dialog.findViewById(R.id.npJednotky);
        setDividerColor(pocitacJednotky);
        pocitacJednotky.setMinValue(0);
        pocitacJednotky.setMaxValue(9);
        Button uloz = (Button) dialog.findViewById(R.id.buttonUlozPocet);
        uloz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pocet = String.valueOf(String.valueOf(pocitacDesitky.getValue()) + String.valueOf(pocitacJednotky.getValue()));


                if (nadpis.equals(getResources().getString(R.string.nadpisNastavPocetCyklu))) {
                    int novyRozdilMeziCykly = ((pocitacDesitky.getValue())*10 + pocitacJednotky.getValue()) - puvodniPocetCyklu;
                    if ((novyRozdilMeziCykly + pocetCyklu ) > 0 ) {
                        pocetCyklu = pocetCyklu + novyRozdilMeziCykly ;
                        puvodniPocetCyklu = puvodniPocetCyklu + novyRozdilMeziCykly;
                        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus)+"/"+String.valueOf(puvodniPocetCyklu));
                        vytvorToast(getResources().getString(R.string.nadpisPocetCyklu)+" " +pocet);
                        prepoctiZbyvajiciCasCykly(novyRozdilMeziCykly);
                    }

                } else if (nadpis.equals(getResources().getString(R.string.nadpisNastavPocetTabat))) {
                    int novyRozdilMeziTabatami = ((pocitacDesitky.getValue())*10 + pocitacJednotky.getValue()) - puvodniPocetTabat;
                    if ((novyRozdilMeziTabatami + pocetTabat ) > 0 ) {
                        pocetTabat = pocetTabat + novyRozdilMeziTabatami;
                        puvodniPocetTabat = puvodniPocetTabat + novyRozdilMeziTabatami;
                        textViewAktualniPocetTabat.setText(String.valueOf(aktualniTabata)+"/"+String.valueOf(puvodniPocetTabat));
                        vytvorToast(getResources().getString(R.string.nadpisPocetTabat)+" " +pocet);
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
                //              linearLayoutPauza.setBackgroundResource(R.mipmap.pauselezatotabataactivity);
                dialog.dismiss(); //nebo cancel() ???
            }
        });
    }

    private void prepoctiZbyvajiciCasCykly(int novyRozdilMeziCykly) {
        int pomocnyCasCviceni = casCviceni.getHour()*3600 + casCviceni.getMin()*60 + casCviceni.getSec();
        int pomocnyCasPauzy = casPauzy.getHour()*3600 + casPauzy.getMin()*60 + casPauzy.getSec();
        //   int pomocnyCasMeziTabatami = casMezitabatami.getHour()*3600 + casMezitabatami.getMin()*60 + casMezitabatami.getSec();
        int pomocnyCelkovyCas =casCelkovy.getHour()*3600 + casCelkovy.getMin()*60 + casCelkovy.getSec();
        pomocnyCelkovyCas = pomocnyCelkovyCas +(pomocnyCasCviceni * novyRozdilMeziCykly * (pocetTabat+1))
                + (pomocnyCasPauzy * (novyRozdilMeziCykly)*(pocetTabat+1));

        int pomocnyCelkovyCasHodiny = pomocnyCelkovyCas/3600;
        int pomocnyCelkovyCasHodinyZbytekPoDeleni = pomocnyCelkovyCas%3600;
        int pomocnyCelkovyCasMinuty = pomocnyCelkovyCasHodinyZbytekPoDeleni/60;
        int  pomocnyCelkovyCasSekundy =  pomocnyCelkovyCasHodinyZbytekPoDeleni%60;
        casCelkovy.setHour(pomocnyCelkovyCasHodiny);
        casCelkovy.setMin(pomocnyCelkovyCasMinuty);
        casCelkovy.setSec(pomocnyCelkovyCasSekundy);

        zobrazCelkovyCas();
    }

    private void prepoctiZbyvajiciCasTabaty(int novyRozdilMeziTabatami) {
        int pomocnyCasCviceni = casCviceni.getHour()*3600 + casCviceni.getMin()*60 + casCviceni.getSec();
        int pomocnyCasPauzy = casPauzy.getHour()*3600 + casPauzy.getMin()*60 + casPauzy.getSec();
        int pomocnyCasMeziTabatami = casMezitabatami.getHour()*3600 + casMezitabatami.getMin()*60 + casMezitabatami.getSec();
        int pomocnyCelkovyCas =casCelkovy.getHour()*3600 + casCelkovy.getMin()*60 + casCelkovy.getSec();
        pomocnyCelkovyCas = pomocnyCelkovyCas +(pomocnyCasCviceni *puvodniPocetCyklu * novyRozdilMeziTabatami)
                + (pomocnyCasPauzy * (puvodniPocetCyklu-1)*novyRozdilMeziTabatami)
                +(pomocnyCasMeziTabatami*novyRozdilMeziTabatami);


        int pomocnyCelkovyCasHodiny = pomocnyCelkovyCas/3600;
        int pomocnyCelkovyCasHodinyZbytekPoDeleni = pomocnyCelkovyCas%3600;
        int pomocnyCelkovyCasMinuty = pomocnyCelkovyCasHodinyZbytekPoDeleni/60;
        int  pomocnyCelkovyCasSekundy =  pomocnyCelkovyCasHodinyZbytekPoDeleni%60;
        casCelkovy.setHour(pomocnyCelkovyCasHodiny);
        casCelkovy.setMin(pomocnyCelkovyCasMinuty);
        casCelkovy.setSec(pomocnyCelkovyCasSekundy);

        zobrazCelkovyCas();
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


        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus)+"/"+String.valueOf(puvodniPocetCyklu));
        textViewAktualniPocetTabat.setText(String.valueOf(aktualniTabata)+"/"+String.valueOf(puvodniPocetTabat));

        switch (stav){
            case 0:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
                    //color
                    GradientDrawable shape =  new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasPripravy);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else  dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasPripravy));

                textViewCasNadpis.setText(R.string.nadpisCasPripravy);
                break;
            case 1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                    //color
                    GradientDrawable shape =  new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasCviceni);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color

                } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasCviceni));

                textViewCasNadpis.setText(R.string.nadpisCasCviceni);
                break;
            case 2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzy));//  (R.drawable.background);
                    //color
                    GradientDrawable shape =  new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasPauzy);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasPauzy));

                textViewCasNadpis.setText(R.string.nadpisCasPauzy);
                break;
            case 3:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzymezitabatami));//  (R.drawable.background);
                    //color
                    GradientDrawable shape =  new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasPauzyMeziTabatami);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasPauzyMeziTabatami));

                textViewCasNadpis.setText(R.string.nadpisCasPauzyMeziTabatmi);
                break;
            case 4:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorkonectabaty));//  (R.drawable.background);
                    //color
                    GradientDrawable shape =  new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasCoolDown);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else {
                    dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorKonecTabaty));
                }
                textViewCas.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimensionPixelSize(R.dimen.velikostCasuOdpocitavaniFull));
                velikostCislic = R.dimen.velikostCasuOdpocitavaniFull;
                textViewCas.setText(R.string.konec);
                textViewCasNadpis.setText("");
                break;
            case 5:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascooldown));//  (R.drawable.background);
                    //color
                    GradientDrawable shape =  new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasCoolDown);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else   dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.colorCasCoolDown));

                textViewCasNadpis.setText(R.string.nadpisCasCoolDown);
                break;
            default:
                break;
        }
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
    }


    private void udelejLayout() {

        setContentView(R.layout.activity_tabata);

        // reklama Google
        String idAplikace = "ca-app-pub-6701702247641250~7047640994";
    //    MobileAds.initialize(getApplicationContext(), idAplikace);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
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
                 //color
                GradientDrawable pozadi1 =  new GradientDrawable();
                pozadi1.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                pozadi1.setColor(colorDlazdiceTabaty);
                GradientDrawable pozadi2 =  new GradientDrawable();
                pozadi2.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                pozadi2.setColor(colorDlazdiceTabaty);
                GradientDrawable pozadi3 =  new GradientDrawable();
                pozadi3.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                pozadi3.setColor(colorDlazdiceTabaty);
                GradientDrawable pozadi4 =  new GradientDrawable();
             //   pozadi4.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
             //   pozadi4.setColor(colorDlazdiceTabaty);
                //color
                dlazdicePodHlavnimCasem1.setBackground(pozadi1);
                dlazdicePodHlavnimCasem2.setBackground(pozadi2);
                dlazdicePodHlavnimCasem3.setBackground(pozadi3);
             //   mAdView.setBackground(pozadi4);




        }

        //      textViewPauza = (TextView) findViewById(R.id.textViewPauza);

        linearLayoutPauza = findViewById(R.id.linearLayoutPauza);
        textViewCelkovyCas = (TextView) findViewById(R.id.textViewCelkovyCas);
        velikostCislic = R.dimen.velikostCasuOdpocitavaniDva;


        //vytvoření dialogu počet tabat
        dialogPoctuTabat = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
        vytvorDialogOpakovani(dialogPoctuTabat, getResources().getString(R.string.nadpisNastavPocetTabat));

        //vytvoření dialogu počet cyklů
        dialogPocetCyklu = new Dialog(new ContextThemeWrapper(this, R.style.DialogStyle));
        vytvorDialogOpakovani(dialogPocetCyklu, getResources().getString(R.string.nadpisNastavPocetCyklu));



    }

    private void vytvorToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void zavlojejReviewNejake() {

        zavolejTretiZpusob();
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
                .setNegativeReviewListener(TabataActivity.this) // OVERRIDE mail intent for negative review
                //            .setReviewListener(this) // Used to listen for reviews (if you want to track them )
                .showAfter(7); //počet spuštění, aby se žádost o review spustila (je to na konci v END)
    }

    //od tud zkouším, aby se v Samsungu A70 a dalších apka nezastavovala když jde do pozadí
  /*  protected void onResume() {

        super.onResume();//visible

        Log.d(HOME_ACTIVITY_TAG, "Activity resumed");


    }*/

/*    @Override

    protected void onPause() {

        super.onPause();//invisible

        Log.d(HOME_ACTIVITY_TAG, "Activity paused");
        onResume();

        TabataActivity.this.onResume();

    }*/

 /*   @Override

    protected void onStop() {

        super.onStop();

        Log.d(HOME_ACTIVITY_TAG, "Activity stopped");
        onResume();
        TabataActivity.this.onResume();

    }
*/
    public void statusBarcolor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(TabataActivity.this,R.color.colorReklama));
        }
    }

/*    @Override
    public void onBackPressed(){
        //protože v manifestu mám nastaveno android:noHistory="true", tak při zpětném buttonu by se apka ukončila a neskočila by do mainu,
        //proto jsem tady zpětný button přetypoval, aby při jeho stisknutí to skočilo do mainu
        super.onBackPressed();
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.putExtra("zobrazIntent",1);
        startActivity(mainActivity);
        finish();
    }*/

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

        Intent intent = new Intent(this, TabataService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        service = new Intent(getApplicationContext(), TabataService.class);


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
            spustOdpocitavac();
        }
    }

    @Override //pridat pro service
    protected void onStop() {//když uživatel dá aplikaci do pozadí, tak teprve potom se spustí servica a nastaví se v service odpočítávání, sem dám asi všechny proměnné
        super.onStop();
        //když máme service connection, tak se nemusí startovat servica, ta už je inicializovaná, stačí v ní jen vyvolat metody


        s.nastavHodnoty(aktualniCyklus, puvodniPocetCyklu, aktualniTabata, pocetTabat, puvodniPocetTabat, casPripravy,colorDlazdiceCasPripravy,
                casCviceni, colorDlazdiceCasCviceni, casPauzy, colorDlazdiceCasPauzy, casCelkovy,casMezitabatami,
                colorDlazdiceCasPauzyMeziTabatami, stav, pomocny, pauzaNeniZmacknuta,pocetCyklu, casCoolDown);
        s.nastavOdpocitavani();

        s.nastavZvuky(zvukStart, zvukStop, zvukCelkovyKonec,
                zvukCountdown, hlasitost, maxHlasitost, volume);

       // s.setNotification();


        //nakonec musím spustit startForegroundService, aby se v service mohla spustit metoda onStartCommand, ve které je return START_NOT_STICKY - to tady je proto,
        //aby se po uvedení telefonu po vypnutí tato servica po cca 1 minutě nekillnula
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                startForegroundService(service);
            } catch (Exception e) {
            }

        } else
            try {
                this.startService(service);
            } catch (Exception e) {
            }

        try {
            s.setNotification();
        } catch (Exception e) {

        }

        odpocitavac.cancel();

    }


    @Override //pridat pro service
    protected void onDestroy() {
        odpocitavac.cancel();
        znicService();
        unregisterReceiver(mMessageReceiver);

        super.onDestroy();
    }

    //metody pro service
    private void nactiZeServisy() {

        preskocVypisCasu = s.getPreskocVypisCasu();
        casCelkovy = s.getCasCelkovy();
        pocetCyklu = s.getPocetCyklu();
        pauzaNeniZmacknuta = s.getPauzaNeniZmacknuta();
        aktualniTabata = s.getAktualniTabata();

        aktualniCyklus = s.getAktualniCyklus();
        pocetTabat = s.getPocetTabat();
        puvodniPocetCyklu = s.getPuvodniPocetCyklu();
        textViewAktualniPocetCyklu.setText(String.valueOf(aktualniCyklus)+"/"+String.valueOf(puvodniPocetCyklu));
        stav = s.getStav();
        switch (stav) {
            case 0:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspripravy));//  (R.drawable.background);
                    //color
                    GradientDrawable shape =  new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasPripravy);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPripravy));
                break;
            case 1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                    //color
                    GradientDrawable shape =  new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasCviceni);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCviceni));
                break;
            case 2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcaspauzy));//  (R.drawable.background);
                    //color
                    GradientDrawable shape =  new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasPauzy);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasPauzy));
                break;
            case 3:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorcascviceni));//  (R.drawable.background);
                    //color
                    GradientDrawable shape =  new GradientDrawable();
                    shape.setCornerRadius(getResources().getDimension(R.dimen.kulate_rohy));
                    shape.setColor(colorDlazdiceCasCviceni);
                    dlazdiceOdpocitavace.setBackground(shape);
                    //color
                } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorCasCviceni));
                break;
            case 4:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorkonectabaty));//  (R.drawable.background);
                } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                break;
            case 5:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    dlazdiceOdpocitavace.setBackground(getBaseContext().getResources().getDrawable(R.drawable.backgroundcolorkonectabaty));//  (R.drawable.background);
                } else dlazdiceOdpocitavace.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorKonecTabaty));
                break;

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
        textViewAktualniPocetTabat.setText(String.valueOf(aktualniTabata)+"/"+String.valueOf(puvodniPocetTabat));

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


}
