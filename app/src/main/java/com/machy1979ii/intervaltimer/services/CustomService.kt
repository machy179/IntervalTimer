package com.machy1979ii.intervaltimer.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.machy1979ii.intervaltimer.CustomActivity
import com.machy1979ii.intervaltimer.R
import com.machy1979ii.intervaltimer.funkce.PraceSeZvukemTabata
import com.machy1979ii.intervaltimer.models.MyTime
import com.machy1979ii.intervaltimer.models.PolozkaCasuKola


class CustomService : Service() {
    private var wakeLock: PowerManager.WakeLock? =
        null //aby servica nepadala do Doze Modu, tak je tady třeba tomu zabránit takto,
    // plus dát do manifestu  <uses-permission android:name="android.permission.WAKE_LOCK" />
    //a proměnnou wakeLock vložit do kódu tak, jak jsem ji vložil...viz https://robertohuertas.com/2019/06/29/android_foreground_services/

    private var pauzePlay: Intent? = null
    private var ppauzePlay: PendingIntent? = null
    private var stopSelf: Intent? = null
    private var pStopSelf: PendingIntent? = null

    private val mBinder: IBinder = MyBinder()
    private val channelId = "1"
    private var notification: Notification? = null
    private var notificationBuilder: NotificationCompat.Builder? = null
    private val ONGOING_NOTIFICATION = 1010
    private var mNotificationManager: NotificationManager? = null
    var notificationIntent: Intent? = null
    var result = 0
    private var chan: NotificationChannel? = null
    private var counter: CountDownTimer? = null

    var casPripravy: MyTime? = null
    var colorDlazdiceCasPripravy = 0
    var casCviceni: MyTime? = null
    var colorDlazdiceCasCviceni = 0
    var casPauzy: MyTime? = null
    var colorDlazdiceCasPauzy = 0
    var casMezitabatami = MyTime(0, 0, 0)
    var colorDlazdiceCasPauzyMeziTabatami = 0 //color
    var casCoolDown = MyTime(0, 0, 0)
    var casCelkovy: MyTime? = null
    var colorSpodnichDlazdic = 0
    var pocetTabat = 1
    var colorDlazdicePocetCyklu = 0
    var aktualniTabata = 1
    var puvodniPocetTabat = 1
    var puvodniPocetCyklu = 0
    var aktualniCyklus = 1
    var pocetCyklu = 0
    var pomocny: Long = 0
    var pauzaNeniZmacknuta = true
    var preskocVypisCasu = false
    var stav: Byte = 0 //0-priprava, 1-cviceni, 2-pauza, 3-pauza mezi tabatami
    var zvukStart = 1
    var zvukStop = 1
    var zvukCelkovyKonec = 1
    var zvukCountdown = 1
    var zvukPulkaCviceni = 33 //33 je, když není nastaven zvuk

    var casPulkyKola =
        0 //pokud uživatel nebude chtít, aby v půlce kola byl nějaký zvuk, tak hodnota bude 0

    var casPulkyKolaAktualni =
        0 //je potřeba ještě tuto proměnnou, protože když nastavím jinou délku kola zrovna v kole, tak by to habrovalo

    var zvukPredkoncemKola = 33 //33 je, když není nastaven zvuk

    var casZvukuPredKoncemKola =
        20 //pokud uživatel nebude chtít, aby v půlce kola byl nějaký zvuk, tak hodnota bude 0

    var polozkyCasyKol = ArrayList<PolozkaCasuKola>()
    var aktualniPolozkaCasu: PolozkaCasuKola? = null
    var pocitadloPolozekCasu = 0
    var jeKonecOdpocitavani = false


    private var mediaPlayer: MediaPlayer? = null

    var hlasitost = 100
    var maxHlasitost = 100
    var volume = 0f

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //nejdříve zjistí, jestli to sem neskočilo z broadcastu
        if ("ACTION_STOP_SERVICE".equals(intent?.getAction())) {
            val intent1 = Intent("znicCustomActivityACustomService")
            sendBroadcast(intent1)
        } else if ("ACTION_PLAY_PAUSE_SERVICE".equals(intent?.getAction())) {
            when (pauzaNeniZmacknuta) {
                true -> {
                    pauzaNeniZmacknuta = false
                    notification =
                        notificationBuilder?.clearActions()    //musím vymazat všechny addAction a pak je tam znova dát, abych mohl Pause vyměnit za play, jinak jsem to nevymyslel
                            ?.addAction(R.mipmap.play, getString(R.string.pokracovat), ppauzePlay)
                            ?.addAction(R.mipmap.minus, getString(R.string.konec), pStopSelf)
                            ?.build()
                    mNotificationManager?.notify(ONGOING_NOTIFICATION, notification)
                }
                false -> {
                    pauzaNeniZmacknuta = true
                    notification = notificationBuilder?.clearActions()
                        ?.addAction(
                            R.mipmap.pausestojatotabataactivity,
                            getString(R.string.pauza),
                            ppauzePlay
                        )
                        ?.addAction(R.mipmap.minus, getString(R.string.konec), pStopSelf)
                        ?.build()
                    mNotificationManager?.notify(ONGOING_NOTIFICATION, notification)
                }
            }
        }


        //potom se musí pořešit, aby servica neskákala do Doze Modu
        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }
        //to tady je proto, aby se po uvedení telefonu po vypnutí tato servica po cca 1 minutě nekillnula - ale ještě jsem musel dát výše uvedené, protože se sice nekillnula, ale postupně přecházela do Doze Modu
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    inner class MyBinder : Binder() {
        val service: CustomService
            get() = this@CustomService
    }

    fun initCountDownTimer(time: Int?) {
        counter = object : CountDownTimer(time!!.toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                result = (millisUntilFinished / 1000).toInt()


                //zkopírováno z CustomActivity
                if (pauzaNeniZmacknuta) {
                    if (!(aktualniPolozkaCasu === polozkyCasyKol.get(0))) {
                        odectiAZobrazCelkovyCas()
                    }
                    pomocny = pomocny - 1

                    nastavCislice(pomocny)
                    when (pomocny.toInt()) {
                        4 -> {
                            //musel jsem tam dát ještě při odpočítávání falešný tik na 4té sekundě, protože u pomalejších zařízeních mi
                            //ten třetí tik přehrával pomalu (když několik sekund před tím nebyl zvuk spuštěn) a než se stihnul přehrát, tak už to bylo na dvojce, tak jsem sem dal
                            //falešný 4 tik, který načtu, spustím a ihned vypnu. U dalších tiků problém nebyl. Nevím proč, ale nějak MediaPlayer jak není několik sekund
                            //spuštěn, tak pak se načítá pomalu a v pomalejších zařízeních je to znát
                            if (mediaPlayer != null) {
                                mediaPlayer!!.reset()
                                mediaPlayer!!.release()
                            }
                            mediaPlayer = MediaPlayer.create(
                                applicationContext,
                                PraceSeZvukemTabata.vratZvukCountdownPodlePozice(zvukCountdown)
                            )
                            //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukStartStopPodlePozice(zvukStart));
                            mediaPlayer!!.setVolume(volume, volume)
                            mediaPlayer!!.start()
                            mediaPlayer!!.stop()
                        }
                        3 -> {
                            if (mediaPlayer != null) {
                                mediaPlayer!!.reset()
                                mediaPlayer!!.release()
                            }
                            mediaPlayer = MediaPlayer.create(
                                applicationContext,
                                PraceSeZvukemTabata.vratZvukCountdownPodlePozice(zvukCountdown)
                            )
                            //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukemTabata.vratZvukStartStopPodlePozice(zvukStart));
                            mediaPlayer!!.setVolume(volume, volume)
                            mediaPlayer!!.start()
                        }
                        2 -> {
                            if (mediaPlayer != null) {
                                mediaPlayer!!.reset()
                                mediaPlayer!!.release()
                            }
                            mediaPlayer = MediaPlayer.create(
                                applicationContext,
                                PraceSeZvukemTabata.vratZvukCountdownPodlePozice(zvukCountdown)
                            )
                            mediaPlayer!!.setVolume(volume, volume)
                            mediaPlayer!!.start()
                        }
                        1 -> {
                            if (mediaPlayer != null) {
                                mediaPlayer!!.reset()
                                mediaPlayer!!.release()
                            }
                            mediaPlayer = MediaPlayer.create(
                                applicationContext,
                                PraceSeZvukemTabata.vratZvukCountdownPodlePozice(zvukCountdown)
                            )
                            mediaPlayer!!.setVolume(volume, volume)
                            mediaPlayer!!.start()
                        }
                        0 -> {
                            //tady   restZvuk.start();
                            //  PraceSeZvukemTabata.spustZvukStartStop(getApplicationContext(),zvukStart);
                            //nakonec jsem to musel vyřešit takhle, chtěl jsem spouštět zvuk přímo ve třídě PraceSeZvukemTabata, ale po přehrání pár zvuků
                            //to přestalo přehrávat zvuky, tak jsem to udělal takhle, navíc jsem to pořešil reset() a release(), našel jsem to v nějakém návodu
                            if (mediaPlayer != null) {
                                mediaPlayer!!.reset()
                                mediaPlayer!!.release()
                                mediaPlayer = null
                            }

                            //je konec odpočítávání předchozí polozkyCasu, tak se musí v tomto vláknu nastavit nová polozkaCasu
                            pocitadloPolozekCasu++
                            if (pocitadloPolozekCasu != polozkyCasyKol.size) { //zjistí, zda není celkový konec odpočítávání
                                aktualniCyklus = pocitadloPolozekCasu
                                //  textViewAktualniPocetCyklu.setText("$aktualniCyklus/$puvodniPocetCyklu")
                                aktualniPolozkaCasu = polozkyCasyKol.get(pocitadloPolozekCasu)
                                mediaPlayer = MediaPlayer.create(
                                    applicationContext,
                                    PraceSeZvukemTabata.vratZvukStartStopPodlePozice(
                                        aktualniPolozkaCasu!!.getZvuk()
                                    )
                                )
                                mediaPlayer!!.setVolume(volume, volume)
                                mediaPlayer!!.start()
                                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    notification =
                                        notificationBuilder?.setColor(aktualniPolozkaCasu!!.getColorDlazdice())
                                            ?.build()
                                    //color
                                } else notification = notificationBuilder?.setColor(
                                    resources.getColor(
                                        R.color.colorCasCviceni
                                    )
                                )?.build()
                                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification

                                notification = notificationBuilder?.setContentText(
                                    "$aktualniCyklus/$puvodniPocetCyklu " + "          " + "${aktualniPolozkaCasu?.nazevCasu} "
                                )?.build()



                                pomocny = (aktualniPolozkaCasu!!.getTime()
                                    .getSec() + aktualniPolozkaCasu!!.getTime()
                                    .getMin() * 60 + aktualniPolozkaCasu!!.getTime()
                                    .getHour() * 3600 + 1).toLong()
                                pomocny =
                                    pomocny - 1 //protože na začátku už to je jedna sekunda a pak mi to při celkovém času nehrálo
                                nastavStartKola(aktualniPolozkaCasu!!.getNazevCasu())
                                //  nastavCislice(pomocny);
                            } else { //je celkový konec odpočítávání
                                jeKonecOdpocitavani = true
                                nastaveniPozadiKonce()
                                mediaPlayer = MediaPlayer.create(
                                    applicationContext,
                                    PraceSeZvukemTabata.vratZvukKonecPodlePozice(
                                        zvukCelkovyKonec
                                    )
                                )
                                mediaPlayer!!.setVolume(volume, volume)
                                mediaPlayer!!.start()
                                counter!!.cancel()
                            }
                        }
                        else -> {}
                    }
                    mNotificationManager?.notify(ONGOING_NOTIFICATION, notification)
                }
                //zkopírováno z CustomActivity-konec

            }

            override fun onFinish() {
                result = 0
            }
        }
        counter?.start()
    }

    fun setNotification() {

        notificationIntent = Intent(this, CustomActivity::class.java)

        // aby se přepnulo na existující instanci aktivity a vymazali všechny další aktivity nad ní zdroj: https://www.peachpit.com/articles/article.aspx?p=1874864
        //ke stejné funkci, aby byla otevřená pouze jedna TabataActivity jsem dal do manifestu k této activitě android:launchMode="singleTask", takže níže uvedené možná není už potřeba a dubluje se to

        notificationIntent?.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        notificationIntent?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            } else {
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
                //pokud to bude nižší Android než 8 (version code O), tak podle dokumentace by měl channelId být ignorován
            }

        stopSelf = Intent(this, CustomService::class.java)
        stopSelf!!.action = "ACTION_STOP_SERVICE"
        pStopSelf = PendingIntent.getService(this, 0, stopSelf!!, 0)

        pauzePlay = Intent(this, CustomService::class.java)
        pauzePlay!!.action = "ACTION_PLAY_PAUSE_SERVICE"
        ppauzePlay = PendingIntent.getService(this, 0, pauzePlay!!, 0)

        notificationBuilder = NotificationCompat.Builder(this, channelId)
        notification = notificationBuilder!!
            //  .setOngoing(true) //bylo by heads-up okno pořád otevřené
            .setAutoCancel(true) //ABY SE PO KLIKNUTÍ NA NOTIFIKACI SAMA ZNIČILA. K TOMU ALE MUSÍM MÍT V PŘEDEŠLÉ ACTIVITY NASTAVENO, ŽE SE TADY MUSÍ ZNIČIT ODPOČÍTÁVÁNÍ - ZNIČIT VLÁKNO
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)   // heads-up
            .setContentTitle(resources.getText(R.string.app_name))
            .setCategory(Notification.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            .setContentText("")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true) //aby se heads-up oběvilo jen jednou a ne po každém notify()
            //   .addAction(R.mipmap.pausestojatotabataactivity, getString(R.string.pauza), ppauzePlay)
            .addAction(
                R.mipmap.minus,
                getString(R.string.konec),
                pStopSelf
            ) //pokud budu chtít dát nějakou další akci například
            .build()

        when (pauzaNeniZmacknuta) {
            false -> {
                notification =
                    notificationBuilder?.clearActions()    //musím vymazat všechny addAction a pak je tam znova dát, abych mohl Pause vyměnit za play, jinak jsem to nevymyslel
                        ?.addAction(R.mipmap.play, getString(R.string.pokracovat), ppauzePlay)
                        ?.addAction(R.mipmap.minus, getString(R.string.konec), pStopSelf)
                        ?.build()
                //      mNotificationManager?.notify(ONGOING_NOTIFICATION, notification)
            }
            true -> {
                notification = notificationBuilder?.clearActions()
                    ?.addAction(
                        R.mipmap.pausestojatotabataactivity,
                        getString(R.string.pauza),
                        ppauzePlay
                    )
                    ?.addAction(R.mipmap.minus, getString(R.string.konec), pStopSelf)
                    ?.build()
                //      mNotificationManager?.notify(ONGOING_NOTIFICATION, notification)
            }
        }

        nastavPocatecniHodnoty() //je potřeba nastavit počáteční hodnoty a je třeba to vložil sem, protože při pauze, kde
        //tikání časovače ignorovalo další propisování nové hodnoty do notifikace, se aktuální čas v pauze špatně propisoval

        notification!!.flags = Notification.DEFAULT_LIGHTS
        notification!!.flags = Notification.FLAG_AUTO_CANCEL
        startForeground(ONGOING_NOTIFICATION, notification)

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelName = "Interval Timer Custom Background Service"
        chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH
        ) //Kdybych chtěl, aby notifikace nebyla v malém okně heads-up, tak bych sem musel dát DEFAULT NEBO LOW...
        chan!!.description = "Interval Timer Custom Background Service description"
        chan!!.setShowBadge(true) //aby v případě notifikace byla u spouštěcí ikony aplikace značka, že je spuštěna notifikace, jako když je například u WA nová zpráva, tak u ikony na domovské stránce je značka nové zpárvy
        chan!!.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        mNotificationManager = getSystemService(
            NotificationManager::class.java
        )
        mNotificationManager!!.createNotificationChannel(chan!!)


        return channelId
    }

    fun killService() {
        //aby šlo tuto servisu zničit, musím  kilnout i vlákno odpočítávání, tedy counter
        counter?.cancel() //musím killnout
        // we need this release because of Doze Mode
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
            Log.d("SSS", "killservice-stop");
        } catch (e: Exception) {
        }
    }

    fun nastavOdpocitavani() {
        //  pocetTabat = pocetTabat - 1
        if(!jeKonecOdpocitavani)
        {
            initCountDownTimer(100000.times(1000))
        } else {
            nastaveniPozadiKonce()
        }
    }

    fun nastavHodnoty(
        aktualniCyklus: Int,
        puvodniPocetCyklu: Int,
        casCelkovy: MyTime,
        stav: Byte,
        colorSpodnichDlazdic: Int,
        pomocny: Long,
        pauzaNeniZmacknuta: Boolean,
        pocetCyklu: Int,
        polozkyCasyKol: ArrayList<PolozkaCasuKola>,
        aktualniPolozkaCasu: PolozkaCasuKola,
        pocitadloPolozekCasu: Int,
        jeKonecOdpocitavani: Boolean
    ) {

        this.aktualniCyklus = aktualniCyklus
        this.puvodniPocetCyklu = puvodniPocetCyklu
        this.casCelkovy = casCelkovy
        this.stav = stav
        this.colorSpodnichDlazdic = colorSpodnichDlazdic
        this.pomocny = pomocny
        this.pocetCyklu = pocetCyklu
        this.pauzaNeniZmacknuta = pauzaNeniZmacknuta
        this.polozkyCasyKol = polozkyCasyKol
        this.aktualniPolozkaCasu = aktualniPolozkaCasu
        this.pocitadloPolozekCasu = pocitadloPolozekCasu
        this.jeKonecOdpocitavani = jeKonecOdpocitavani

        Log.d("casMezitabatami 1 sec: ", casMezitabatami.sec.toString())

    }

    fun nastavPocatecniHodnoty() {

/*        when (stav.toInt()) {
            0 -> {

                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification = notificationBuilder?.setColor(colorDlazdiceCasPripravy)?.build()
                    //color
                } else notification = notificationBuilder?.setColor(resources.getColor(
                    R.color.colorCasPripravy
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification

                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification = notificationBuilder?.setContentText(
                        resources.getText(R.string.cvic)
                    )?.setColor(colorDlazdiceCasPripravy)?.build()
                    //color
                } else notification = notificationBuilder?.setContentText(resources.getText(R.string.cvic))?.setColor(resources.getColor(
                    R.color.colorCasPripravy
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
            }
            1 -> {

                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification = notificationBuilder?.setColor(colorDlazdiceCasCviceni)?.build()
                    //color
                } else notification = notificationBuilder?.setColor(resources.getColor(
                    R.color.colorCasCviceni
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
            }
            2 -> {

                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification = notificationBuilder?.setColor(colorDlazdiceCasPauzy)?.build()
                    //color
                } else notification = notificationBuilder?.setColor(resources.getColor(
                    R.color.colorCasPauzy
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
            }
            3 -> {

                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                notification = notificationBuilder?.setColor(resources.getColor(
                    R.color.colorCasPauzyMeziTabatami
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
            }
            4 -> {

                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                notification = notificationBuilder?.setColor(resources.getColor(
                    R.color.colorKonecTabaty
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
            }
            5 -> {

                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                notification = notificationBuilder?.setColor(resources.getColor(
                    R.color.colorCasCoolDown
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
            }
        }*/
        //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification =
                notificationBuilder?.setColor(aktualniPolozkaCasu!!.getColorDlazdice())?.build()
            //color
        } else notification = notificationBuilder?.setColor(
            resources.getColor(
                R.color.colorCasCviceni
            )
        )?.build()
        //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
        nastavCislice(this.pomocny)
        mNotificationManager?.notify(ONGOING_NOTIFICATION, notification)

    }

    fun nastavZvuky(
        zvukCelkovyKonec: Int,
        zvukCountdown: Int, hlasitost: Int, maxHlasitost: Int, volume: Float
    ) {

        this.zvukCelkovyKonec = zvukCelkovyKonec
        this.zvukCountdown = zvukCountdown
        this.hlasitost = hlasitost
        this.maxHlasitost = maxHlasitost
        this.volume = volume

    }

    private fun nastavCislice(hodnotaAktualni: Long) {

        if (hodnotaAktualni < 60) {
            notification = notificationBuilder?.setContentText(
                "$aktualniCyklus/$puvodniPocetCyklu " + "${aktualniPolozkaCasu?.nazevCasu} " + "          " + vratDeseticisla(
                    pomocny.toInt()
                )
            )?.build()

        } else {
            val minuty = (hodnotaAktualni % 60).toInt()
            notification = notificationBuilder?.setContentText(
                "$aktualniCyklus/$puvodniPocetCyklu " + "${aktualniPolozkaCasu?.nazevCasu} " + "          " + vratDeseticisla(
                    ((pomocny - minuty) / 60).toInt()
                ) + ":" + vratDeseticisla(
                    minuty
                )
            )?.build()
        }
    }

    private fun vratDeseticisla(cislo: Int): String? {
        return if (cislo < 10) {
            "0$cislo"
        } else cislo.toString()
    }

    private fun odectiAZobrazCelkovyCas() {
        if ((casCelkovy!!.hour == 0) and (casCelkovy!!.min == 0) and (casCelkovy!!.sec == 0)) {
        } else {
            if (casCelkovy!!.sec == 0) {
                casCelkovy!!.min = casCelkovy!!.min - 1
                casCelkovy!!.sec = 59
            } else {
                casCelkovy!!.sec = casCelkovy!!.sec - 1
            }
        }
    }

    private fun nastavStartKola(hodnotaNazvuKola: String) {
/*      //  textViewCas.setText(hodnotaNazvuKola)
        if (aktualniPolozkaCasu!!.poradiVCyklu > 1) {
            textViewBeziciCasCisloKola.setText(aktualniPolozkaCasu!!.poradiVCyklu.toString())
        }*/
    }

    private fun nastaveniPozadiKonce() {
        //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
        notification = notificationBuilder?.setContentText(
            resources.getText(
                R.string.konec
            )
        )?.setColor(
            resources.getColor(
                R.color.colorKonecTabaty
            )
        )?.build()
        //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
    }

}