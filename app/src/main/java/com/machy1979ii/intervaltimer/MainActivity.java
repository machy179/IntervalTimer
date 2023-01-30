package com.machy1979ii.intervaltimer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;
import com.machy1979ii.intervaltimer.ui.main.FirstFragment;
import com.machy1979ii.intervaltimer.ui.main.SecondFragment;
import com.machy1979ii.intervaltimer.ui.main.SectionsPagerAdapter;
import com.machy1979ii.intervaltimer.databinding.ActivityMainBinding;
import com.machy1979ii.intervaltimer.ui.main.ThirdFragment;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    //GDPR zpráva
    private ConsentInformation consentInformation;
    private ConsentForm consentForm;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        udelejZpravuGDPR();
   //     askPermissionPostNotification();





/*             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                 String packageName = getApplicationContext().getPackageName();
                 PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                 if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                     Intent intent = new Intent();
                     intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                     intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                     intent.setData(Uri.parse("package:" + packageName));
                     getApplicationContext().startActivity(intent);
                 }
             }*/

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

/*    private void askPermissionPostNotification() {
        // called in a standard activity, use  ContextCompat.checkSelfPermission for AppCompActivity
        Log.i("askpermission", "2");
        if (Build.VERSION.SDK_INT >= 33) {
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
    }*/

//...
}