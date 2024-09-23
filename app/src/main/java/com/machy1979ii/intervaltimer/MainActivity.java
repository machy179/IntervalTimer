package com.machy1979ii.intervaltimer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;
import com.machy1979ii.intervaltimer.funkce.AdUtils;
import com.machy1979ii.intervaltimer.services.TimerAnalytics;
import com.machy1979ii.intervaltimer.services.TimerBillingManager;
import com.machy1979ii.intervaltimer.ui.main.SectionsPagerAdapter;
import com.machy1979ii.intervaltimer.databinding.ActivityMainBinding;

import static com.machy1979ii.intervaltimer.funkce.Detect_outdated_consentKt.deleteTCStringIfOutdated;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    //GDPR zpráva
    private ConsentInformation consentInformation;
    private ConsentForm consentForm;

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

        //Google Billing
        TimerBillingManager.Companion.initialize(this.getApplication(), new TimerBillingManager.Companion.OnPurchaseCompletedListenerMainActivity() {

            @Override
            public void onPurchaseCompletedMainActivity() { //tady nastavuju listener, co se má stát, když v TimerBillingManager bude spuštěna metoda onPurchaseCompletedMainActivity
                Log.d("Boxing listener MainA:", "onPurchaseCompleted()");
                if (TimerBillingManager.Companion.getAdsDisabled().getValue()) {

                    // Zajistíme, že removeAds bude voláno na hlavním vlákně, protože tohle je listener a ten jede v jiném vlákně, pokdu jsem removeAds() volal tady normálně, tak
                    //viewNoAds.setVisibility(GONE); dělal neplechu, protože tohle musí být voláno v hlavním vlákně viz níže
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Boxing no Ads2:", "remove ads listener");
                            adsDisabled = TimerBillingManager.Companion.getAdsDisabled().getValue();
                            removeAds();
                        }
                    });
                } else Log.d("Boxing no Ads2:", "NOT remove ads listener");
            }
        }, null);
        adsDisabled = TimerBillingManager.Companion.getAdsDisabled().getValue();
        Log.d("Boxing no Ads2:", adsDisabled.toString());

        //Firebase Analytics
        TimerAnalytics.INSTANCE.getInstance(this).logActivityStart("MainActivity");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setOffscreenPageLimit(2); //...to je tady, aby byly uloženy 2 fragmenty v pravo a 2 fragmenty vlevo od aktuálního fragmentu a nevymazaly se z paměti, taže se i ten třetí fragment, který se po prvním zobrazení dohrává ze souboru, z paměti nevymaže, ani když se pak přesuneme na první fragment

        viewPager.setAdapter(sectionsPagerAdapter);


        //tady dát něco jako getActivity().getIntent().getIntExtra("zobrazIntent", 0);
        //a podle toho, jaké číslo to bude, tak se zavolá ten konkrétní intent
        //v SetSoundClassicActivity a v SetSoundTabataActivity v čudlíku uložit dám proměnnou "zobrazIntent" a číslo podle toho, do jakého fragmentu se má vracet
        //v těch SetSoundech ještě pořešit to, že se nebudou přenášet čísla zvuků, ale rovnou se uloží do souboru
        //nebo to můžu zkusit, první getIntent...viz výše, zavolám už tady a podle toho, jaké bude číslo,
        //se to nasměruje na ten konkrétní intent a tam se getIntentama dotáhne zbytek
        // if ..... tak viewPager.setCurrentItem(1);
        deleteTCStringIfOutdated(getApplicationContext());
        udelejZpravuGDPR();
        askPermissionPostNotification();
        udelejReklamu();

        int zobrazIntent = getIntent().getIntExtra("zobrazIntent", 0);

        switch (zobrazIntent) {
            case 0:
                viewPager.setCurrentItem(0);
                break;
            case 1:
                viewPager.setCurrentItem(1);
                break;
            case 2:
                viewPager.setCurrentItem(2);
                break;

        }

        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);

    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        zrusReklamu();//přidána tato funkce, protože při otočení tam chvíli visela původní reklama s původními rozměry
        udelejReklamu();
    }


    private void udelejReklamu() {
        // Reklama Goole nová - adaptivní banner

        if (!adsDisabled) {
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
                            AdUtils.loadBanner(adView, AD_UNIT_ID,MainActivity.this,adContainerView); //volám mnou vytvořenou statickou třídu/metodu
                            //    loadBanner();
                        }
                    }
                });
    }
    }

    private void zrusReklamu() {
        if (adView != null) {
            adContainerView.removeView(adView); // Odstranění reklamy z rodičovského kontejneru
            adView.destroy(); // Zničení instance reklamy
            adView = null; // Nulování reference na instanci reklamy
        }
    }

    //Google Billing
    private void removeAds() {
        adsDisabled = true;
        AdUtils.removeAds(adView = adView, adContainerView = adContainerView);
        Log.d("Boxing no Ads2:", "removeAds()");
    }


    private void udelejZpravuGDPR() {
        // Set tag for underage of consent. false means users are not underage.
        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                    @Override
                    public void onConsentInfoUpdateSuccess() {
                        // The consent information state was updated.
                        // You are now ready to check if a form is available.
                        if (consentInformation.isConsentFormAvailable()) {
                            loadForm();
                        }
                    }
                },
                new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                    @Override
                    public void onConsentInfoUpdateFailure(FormError formError) {
                        // Handle the error.

                    }
                });
    }

    public void loadForm(){
        UserMessagingPlatform.loadConsentForm(
                this,
                new UserMessagingPlatform.OnConsentFormLoadSuccessListener() {
                    @Override
                    public void onConsentFormLoadSuccess(ConsentForm consentForm) {
                        MainActivity.this.consentForm = consentForm;
                        if(consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
                            consentForm.show(
                                    MainActivity.this,
                                    new ConsentForm.OnConsentFormDismissedListener() {
                                        @Override
                                        public void onConsentFormDismissed(@Nullable FormError formError) {
                                            // Handle dismissal by reloading form.
                                            loadForm();
                                        }
                                    });

                        }

                    }
                },
                new UserMessagingPlatform.OnConsentFormLoadFailureListener() {
                    @Override
                    public void onConsentFormLoadFailure(FormError formError) {
                        /// Handle Error.
                    }
                }
        );
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();


    }

    private void askPermissionPostNotification() {
        // called in a standard activity, use  ContextCompat.checkSelfPermission for AppCompActivity
        int permissionCheck = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS);
        Log.i("askpermission", "3");
        if (!(permissionCheck == PackageManager.PERMISSION_GRANTED)) {
            // User may have declined earlier, ask Android if we should show him a reason
        //    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS)) {
                    Log.i("askpermission", "4");
                    // show an explanation to the user
                    // Good practise: don't block thread after the user sees the explanation, try again to request the permission.
                } else {
                    Log.i("askpermission", "5");
                    // request the permission.
                    // CALLBACK_NUMBER is a integer constants
                    ActivityCompat.requestPermissions(MainActivity.this,  new String[]{Manifest.permission.POST_NOTIFICATIONS},7);
                 //   requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 8);
                    // The callback method gets the result of the request.
                }
            }
        } else {
// got permission use it
        }

    }

//...
}