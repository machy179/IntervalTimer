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
import com.machy1979ii.intervaltimer.ClassicActivity
import com.machy1979ii.intervaltimer.R
import com.machy1979ii.intervaltimer.funkce.PraceSeZvukem
import com.machy1979ii.intervaltimer.models.MyTime


class ClassicService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null //aby servica nepadala do Doze Modu, tak je tady třeba tomu zabránit takto,
                                                        // plus dát do manifestu  <uses-permission android:name="android.permission.WAKE_LOCK" />
                                                        //a proměnnou wakeLock vložit do kódu tak, jak jsem ji vložil...viz https://robertohuertas.com/2019/06/29/android_foreground_services/

    private var pauzePlay: Intent? = null
    private var ppauzePlay: PendingIntent? = null
    private var stopSelf: Intent? = null
    private var pStopSelf: PendingIntent? = null

    private val mBinder: IBinder = MyBinder()
 //   private val channelId = "Notification from Service"
    private val channelId = "1"
    private var notification: Notification? = null
    private var notificationBuilder: NotificationCompat.Builder? =null
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
    var casCoolDown = MyTime(0, 0, 0)
    var casCelkovy: MyTime? = null
    var pocetTabat = 1
    var colorDlazdicePocetCyklu = 0
    var aktualniTabata = 1
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

    var casPulkyKola = 0 //pokud uživatel nebude chtít, aby v půlce kola byl nějaký zvuk, tak hodnota bude 0

    var casPulkyKolaAktualni = 0 //je potřeba ještě tuto proměnnou, protože když nastavím jinou délku kola zrovna v kole, tak by to habrovalo

    var zvukPredkoncemKola = 33 //33 je, když není nastaven zvuk

    var casZvukuPredKoncemKola = 20 //pokud uživatel nebude chtít, aby v půlce kola byl nějaký zvuk, tak hodnota bude 0


    private var mediaPlayer: MediaPlayer? = null

    var hlasitost = 100
    var maxHlasitost = 100
    var volume  = 0f

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if ("ACTION_STOP_SERVICE".equals(intent?.getAction())) {
            Log.d("SSS","called to cancel service")
            killService()
        } else if ("ACTION_PLAY_PAUSE_SERVICE".equals(intent?.getAction())) {
            Log.d("SSS","called to play pause service")
            when (pauzaNeniZmacknuta) {
                true ->  {
                    Log.d("SSS","---true")
                    pauzaNeniZmacknuta=false
                    notification = notificationBuilder?.setOngoing(true)?.clearActions()    //musím vymazat všechny addAction a pak je tam znova dát, abych mohl Pause vyměnit za play, jinak jsem to nevymyslel
                        ?.addAction(R.mipmap.play, "Play", ppauzePlay)
                        ?.addAction(R.drawable.back_pokus, "cancel", pStopSelf)
                        ?.build()
                    mNotificationManager?.notify(ONGOING_NOTIFICATION, notification)
                }

                false -> {
                    Log.d("SSS","---false")
                    pauzaNeniZmacknuta=true
                    notification = notificationBuilder?.setOngoing(true)?.clearActions()
                        ?.addAction(R.mipmap.play, "Pause", ppauzePlay)
                        ?.addAction(R.drawable.back_pokus, "cancel", pStopSelf)
                        ?.build()
                    mNotificationManager?.notify(ONGOING_NOTIFICATION, notification)
                }
                else -> print("s does not encode x")
                }
        }



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
        val service: ClassicService
            get() = this@ClassicService
    }

    fun initCountDownTimer(time: Int?) {
        counter = object : CountDownTimer(time!!.toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                result =(millisUntilFinished / 1000).toInt()

                Log.d("Servica result:",result.toString())

                //zkopírováno z ClassicActivity
                if (pauzaNeniZmacknuta) {
                    odectiAZobrazCelkovyCas()
                    when (stav.toInt()) {
                        0 -> {
                            //vlákno příprava
                            //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                notification = notificationBuilder?.setOngoing(true)?.setContentText(
                                    resources.getText(R.string.cvic)
                                )?.setColor(colorDlazdiceCasPripravy)?.build()
                                //color
                            } else notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.cvic))?.setColor(resources.getColor(
                                R.color.colorCasPripravy
                            ))?.build()
                            //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification

                            pomocny = pomocny - 1

                            if (pomocny <= 0) {
                                //tady   restZvuk.start();
                                //  PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukStart);
                                //nakonec jsem to musel vyřešit takhle, chtěl jsem spouštět zvuk přímo ve třídě PraceSeZvukem, ale po přehrání pár zvuků
                                //to přestalo přehrávat zvuky, tak jsem to udělal takhle, navíc jsem to pořešil reset() a release(), našel jsem to v nějakém návodu
                                if (mediaPlayer != null) {
                                    mediaPlayer!!.reset()
                                    mediaPlayer!!.release()
                                    mediaPlayer = null
                                }
                                mediaPlayer = MediaPlayer.create(
                                    applicationContext,
                                    PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart)
                                )
                                mediaPlayer!!.setVolume(volume, volume)
                                mediaPlayer!!.start()
                                stav = (stav + 1).toByte()

                                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    notification = notificationBuilder?.setOngoing(true)?.setContentText(
                                        resources.getText(R.string.cvic)
                                    )?.setColor(colorDlazdiceCasCviceni)?.build()
                                    //color
                                } else notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.cvic))?.setColor(resources.getColor(
                                    R.color.colorCasCviceni
                                ))?.build()
                                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification

                                pomocny =
                                    (casCviceni!!.sec + casCviceni!!.min * 60 + casCviceni!!.hour * 3600 + 1).toLong()
                                pomocny =
                                    pomocny - 1 //protože při GO už to je jedna sekunda a pak mi to při celkovém času nehrálo

                                preskocVypisCasu = true
                            }
                            if (preskocVypisCasu) {
                                preskocVypisCasu = false
                            } else {
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
                                        )
                                        //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
                                        )
                                        //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
                                        )
                                        mediaPlayer!!.setVolume(volume, volume)
                                        mediaPlayer!!.start()
                                    }
                                    else -> {}
                                }
                            }
                        }
                        1 -> {
                            //vlákno cvičení
                            pomocny = pomocny - 1
                            Log.d("VlaknoService: ", "Cviceni:"+pomocny.toString())
                            if (pomocny <= 0) {
                                Log.d("VlaknoService: ", "Cviceni=0")
                                casPulkyKolaAktualni = casPulkyKola
                                if (pocetCyklu == 0) {
                                    Log.d("VlaknoService: ", "Cviceni=0, pocetCyklu=0")
                                    if (pocetTabat == 0) {
                                        Log.d("VlaknoService: ", "Cviceni=0, pocetCyklu=0, pocetTabat=0")
                                        //    fanfareZvuk.start();
                                        if (mediaPlayer != null) {
                                            mediaPlayer!!.reset()
                                            mediaPlayer!!.release()
                                        }
                                        mediaPlayer = MediaPlayer.create(
                                            applicationContext,
                                            PraceSeZvukem.vratZvukStartStopPodlePozice(
                                                zvukCelkovyKonec
                                            )
                                        )
                                        mediaPlayer!!.setVolume(volume, volume)
                                        mediaPlayer!!.start()
                                        if (casCoolDown.sec == 0 && casCoolDown.min == 0) {
                                            //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                            notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.konec))?.setColor(resources.getColor(
                                                R.color.colorKonecTabaty
                                            ))?.build()
                                            //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification

                                            stav = 4
                                            preskocVypisCasu = true
                                            Log.d("Jsem na konci: ", "1")
                                        } else {
                                            stav = 5
                                            //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                            notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.coolDown))?.setColor(resources.getColor(
                                                R.color.colorCasCoolDown
                                            ))?.build()
                                            //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                            preskocVypisCasu = true
                                            Log.d("Jsem na konci: ", "2")
                                        }
                                    } else {
                                        //tady   restZvuk.start();
                                        // PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukStop);
                                        if (mediaPlayer != null) {
                                            mediaPlayer!!.reset()
                                            mediaPlayer!!.release()
                                        }
                                        mediaPlayer = MediaPlayer.create(
                                            applicationContext,
                                            PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStop)
                                        )
                                        mediaPlayer!!.setVolume(volume, volume)
                                        mediaPlayer!!.start()
                                        stav = 3
                                        //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                        notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.odpocinek))?.setColor(resources.getColor(
                                            R.color.colorCasPauzyMeziTabatami
                                        ))?.build()
                                        //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification

                                        pomocny =
                                            (casMezitabatami.sec + casMezitabatami.min * 60 + casMezitabatami.hour * 3600 + 1).toLong()
                                        pomocny =
                                            pomocny - 1 //protože při REST už to je jedna sekunda a pak mi to při celkovém času nehrálo

                                        preskocVypisCasu = true
                                        pocetTabat = pocetTabat - 1
                                    }
                                } else {
                                    //tady                            restZvuk.start();
                                    //  PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukStop);
                                    if (mediaPlayer != null) {
                                        mediaPlayer!!.reset()
                                        mediaPlayer!!.release()
                                    }
                                    mediaPlayer = MediaPlayer.create(
                                        applicationContext,
                                        PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStop)
                                    )
                                    mediaPlayer!!.setVolume(volume, volume)
                                    mediaPlayer!!.start()
                                    stav = (stav + 1).toByte()

                                    //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        notification = notificationBuilder?.setOngoing(true)?.setContentText(
                                            resources.getText(R.string.odpocinek)
                                        )?.setColor(colorDlazdiceCasPauzy)?.build()
                                        //color
                                    } else notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.odpocinek))?.setColor(resources.getColor(
                                        R.color.colorCasPauzy
                                    ))?.build()
                                    //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification

                                    pomocny =
                                        (casPauzy!!.sec + casPauzy!!.min * 60 + casPauzy!!.hour * 3600 + 1).toLong()
                                    pomocny =
                                        pomocny - 1 //protože při REST už to je jedna sekunda a pak mi to při celkovém času nehrálo

                                    preskocVypisCasu = true
                                }
                            }
                            if (preskocVypisCasu) {
                                Log.d("VlaknoService: ", "preskocVypisCasu")
                                preskocVypisCasu = false
                            } else {
                                nastavCislice(pomocny)
                                if (casZvukuPredKoncemKola != 0 && casZvukuPredKoncemKola.toLong() == pomocny && zvukPredkoncemKola != PraceSeZvukem.vratPocetZvukuPulkaCviceni()) {
                                    //pokud bude nastavený zvuk v půlce kola, tak to odehraje tohle níže, má to větší přednost než countdown, takže pokud by to přicházelo na
                                    //stejný čas, tak to odehraje pouze zvuk půlky kola
                                    //aaa
                                    Log.d("Jsem v půlce kola", "halo")
                                    if (mediaPlayer != null) {
                                        mediaPlayer!!.reset()
                                        mediaPlayer!!.release()
                                    }
                                    mediaPlayer = MediaPlayer.create(
                                        applicationContext,
                                        PraceSeZvukem.vratZvukStartStopPodlePozice(
                                            zvukPredkoncemKola
                                        )
                                    )
                                    mediaPlayer!!.setVolume(volume, volume)
                                    mediaPlayer!!.start()
                                } else if (casPulkyKolaAktualni != 0 && casPulkyKolaAktualni.toLong() == pomocny && zvukPulkaCviceni != PraceSeZvukem.vratPocetZvukuPulkaCviceni()) {
                                    //pokud bude nastavený zvuk v půlce kola, tak to odehraje tohle níže, má to větší přednost než countdown, takže pokud by to přicházelo na
                                    //stejný čas, tak to odehraje pouze zvuk půlky kola
                                    if (mediaPlayer != null) {
                                        mediaPlayer!!.reset()
                                        mediaPlayer!!.release()
                                    }
                                    Log.d("Jsem v půlce kola", "halo")
                                    mediaPlayer = MediaPlayer.create(
                                        applicationContext,
                                        PraceSeZvukem.vratZvukStartStopPodlePozice(zvukPulkaCviceni)
                                    )
                                    mediaPlayer!!.setVolume(volume, volume)
                                    mediaPlayer!!.start()
                                } else {
                                    Log.d("cas: ", pomocny.toString())
                                    when (pomocny.toInt()) {
                                        4 -> {
                                            if (mediaPlayer != null) {
                                                mediaPlayer!!.reset()
                                                mediaPlayer!!.release()
                                            }
                                            mediaPlayer = MediaPlayer.create(
                                                applicationContext,
                                                PraceSeZvukem.vratZvukCountdownPodlePozice(
                                                    zvukCountdown
                                                )
                                            )
                                            //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
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
                                                PraceSeZvukem.vratZvukCountdownPodlePozice(
                                                    zvukCountdown
                                                )
                                            )
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
                                                PraceSeZvukem.vratZvukCountdownPodlePozice(
                                                    zvukCountdown
                                                )
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
                                                PraceSeZvukem.vratZvukCountdownPodlePozice(
                                                    zvukCountdown
                                                )
                                            )
                                            mediaPlayer!!.setVolume(volume, volume)
                                            mediaPlayer!!.start()
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }
                        2 -> {
                            //vlákno pauza
                            pomocny = pomocny - 1
                            if (pomocny <= 0) {
                                if (pocetCyklu == 0) {
                                    if (pocetTabat == 0) {
                                        if (casCoolDown.sec == 0 && casCoolDown.min == 0) {
                                            //   fanfareZvuk.start();
                                            if (mediaPlayer != null) {
                                                mediaPlayer!!.reset()
                                                mediaPlayer!!.release()
                                            }
                                            mediaPlayer = MediaPlayer.create(
                                                applicationContext,
                                                PraceSeZvukem.vratZvukStartStopPodlePozice(
                                                    zvukCelkovyKonec
                                                )
                                            )
                                            mediaPlayer!!.setVolume(volume, volume)
                                            mediaPlayer!!.start()
                                            stav = 4
                                            //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                            notification = notificationBuilder?.setOngoing(true)?.setContentText("")?.setColor(resources.getColor(
                                                R.color.colorKonecTabaty
                                            ))?.build()
                                            //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification


                                            preskocVypisCasu = true
                                        } else {
                                            //tady                            restZvuk.start();
                                            //   PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukCelkovyKonec);
                                            if (mediaPlayer != null) {
                                                mediaPlayer!!.reset()
                                                mediaPlayer!!.release()
                                            }
                                            mediaPlayer = MediaPlayer.create(
                                                applicationContext,
                                                PraceSeZvukem.vratZvukStartStopPodlePozice(
                                                    zvukCelkovyKonec
                                                )
                                            )
                                            mediaPlayer!!.setVolume(volume, volume)
                                            mediaPlayer!!.start()
                                            stav = 5
                                            //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                            notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.coolDown))?.setColor(resources.getColor(
                                                R.color.colorCasCoolDown
                                            ))?.build()
                                            //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification

                                            pomocny =
                                                (casCoolDown.sec + casCoolDown.min * 60 + casCoolDown.hour * 3600 + 1).toLong()

                                            preskocVypisCasu = true
                                        }
                                    } else {
                                        //tady                            restZvuk.start();
                                        //  PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukStop);
                                        if (mediaPlayer != null) {
                                            mediaPlayer!!.reset()
                                            mediaPlayer!!.release()
                                        }
                                        mediaPlayer = MediaPlayer.create(
                                            applicationContext,
                                            PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStop)
                                        )
                                        mediaPlayer!!.setVolume(volume, volume)
                                        mediaPlayer!!.start()
                                        stav = 3
                                        //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                        notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.odpocinek))?.setColor(resources.getColor(
                                            R.color.colorCasPauzyMeziTabatami
                                        ))?.build()
                                        //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification

                                        pomocny =
                                            (casMezitabatami.sec + casMezitabatami.min * 60 + casMezitabatami.hour * 3600 + 1).toLong()
                                        pomocny =
                                            pomocny - 1 //protože při REST už to je jedna sekunda a pak mi to při celkovém času nehrálo
                                        preskocVypisCasu = true
                                        pocetTabat = pocetTabat - 1
                                    }
                                } else {
                                    //tady                            restZvuk.start();
                                    //  PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukStart);
                                    if (mediaPlayer != null) {
                                        mediaPlayer!!.reset()
                                        mediaPlayer!!.release()
                                    }
                                    mediaPlayer = MediaPlayer.create(
                                        applicationContext,
                                        PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart)
                                    )
                                    mediaPlayer!!.setVolume(volume, volume)
                                    mediaPlayer!!.start()
                                    stav = 1
                                    //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        notification = notificationBuilder?.setOngoing(true)?.setContentText(
                                            resources.getText(R.string.cvic)
                                        )?.setColor(colorDlazdiceCasCviceni)?.build()
                                        //color
                                    } else notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.cvic))?.setColor(resources.getColor(
                                        R.color.colorCasCviceni
                                    ))?.build()
                                    //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification

                                    aktualniCyklus = aktualniCyklus + 1

                                    pomocny =
                                        (casCviceni!!.sec + casCviceni!!.min * 60 + casCviceni!!.hour * 3600 + 1).toLong()
                                    pomocny =
                                        pomocny - 1 //protože při GO už to je jedna sekunda a pak mi to při celkovém času nehrálo

                                    preskocVypisCasu = true
                                    pocetCyklu = pocetCyklu - 1
                                }
                            }
                            if (preskocVypisCasu) {
                                preskocVypisCasu = false
                            } else {
                                nastavCislice(pomocny)
                                when (pomocny.toInt()) {
                                    4 -> {
                                        if (mediaPlayer != null) {
                                            mediaPlayer!!.reset()
                                            mediaPlayer!!.release()
                                        }
                                        mediaPlayer = MediaPlayer.create(
                                            applicationContext,
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
                                        )
                                        //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
                                        )
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
                                        )
                                        mediaPlayer!!.setVolume(volume, volume)
                                        mediaPlayer!!.start()
                                    }
                                    else -> {}
                                }
                            }
                        }
                        3 -> {
                            //vlákno pauza mezi tabatami
                            pomocny = pomocny - 1
                            if (pomocny <= 0) {
                                //tady                            restZvuk.start();
                                // PraceSeZvukem.spustZvukStartStop(getApplicationContext(),zvukStart);
                                if (mediaPlayer != null) {
                                    mediaPlayer!!.reset()
                                    mediaPlayer!!.release()
                                }
                                mediaPlayer = MediaPlayer.create(
                                    applicationContext,
                                    PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart)
                                )
                                mediaPlayer!!.setVolume(volume, volume)
                                mediaPlayer!!.start()
                                stav = 1
                                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    notification = notificationBuilder?.setOngoing(true)?.setContentText(
                                        resources.getText(R.string.cvic)
                                    )?.setColor(colorDlazdiceCasCviceni)?.build()
                                    //color
                                } else notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.cvic))?.setColor(resources.getColor(
                                    R.color.colorCasCviceni
                                ))?.build()
                                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification


                                aktualniCyklus = 1
                                aktualniTabata = aktualniTabata + 1
                                //       textViewAktualniPocetTabat.setText(String.valueOf(aktualniTabata)+"/"+String.valueOf(puvodniPocetTabat));
                                pomocny =
                                    (casCviceni!!.sec + casCviceni!!.min * 60 + casCviceni!!.hour * 3600 + 1).toLong()
                                pomocny =
                                    pomocny - 1 //protože při GO už to je jedna sekunda a pak mi to při celkovém času nehrálo

                                preskocVypisCasu = true
                                pocetCyklu = puvodniPocetCyklu - 1
                            }
                            if (preskocVypisCasu) {
                                preskocVypisCasu = false
                            } else {
                                nastavCislice(pomocny)
                                when (pomocny.toInt()) {
                                    4 -> {
                                        if (mediaPlayer != null) {
                                            mediaPlayer!!.reset()
                                            mediaPlayer!!.release()
                                        }
                                        mediaPlayer = MediaPlayer.create(
                                            applicationContext,
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
                                        )
                                        //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
                                        )
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
                                        )
                                        mediaPlayer!!.setVolume(volume, volume)
                                        mediaPlayer!!.start()
                                    }
                                    else -> {}
                                }
                            }
                        }
                        4 -> {
                            //konec odpočítávání
                            //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                            notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.konec))?.setColor(resources.getColor(
                                R.color.colorKonecTabaty
                            ))?.build()
                            //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                            counter?.cancel()
                            Log.d("Jsem na konci: ", "3")
                        }
                        5 -> {
                            //čas cool down, je to tam doděláno dodatečně, proto to má číslo 5 a ne 4
                            pomocny = pomocny - 1
                            if (pomocny <= 0) {
                                //   restZvuk.start();

                                //  PraceSeZvukem.spustZvukKonec(getApplicationContext(),zvukCelkovyKonec);
                                if (mediaPlayer != null) {
                                    mediaPlayer!!.reset()
                                    mediaPlayer!!.release()
                                }
                                mediaPlayer = MediaPlayer.create(
                                    applicationContext,
                                    PraceSeZvukem.vratZvukStartStopPodlePozice(zvukCelkovyKonec)
                                )
                                mediaPlayer!!.setVolume(volume, volume)
                                mediaPlayer!!.start()
                                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.konec))?.setColor(resources.getColor(
                                    R.color.colorKonecTabaty
                                ))?.build()
                                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                                stav = 4
                                preskocVypisCasu = false
                            }
                            if (preskocVypisCasu) {
                                preskocVypisCasu = false
                            } else {
                                nastavCislice(pomocny)
                                when (pomocny.toInt()) {
                                    4 -> {
                                        if (mediaPlayer != null) {
                                            mediaPlayer!!.reset()
                                            mediaPlayer!!.release()
                                        }
                                        mediaPlayer = MediaPlayer.create(
                                            applicationContext,
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
                                        )
                                        //     mediaPlayer = MediaPlayer.create(getApplicationContext(), PraceSeZvukem.vratZvukStartStopPodlePozice(zvukStart));
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
                                        )
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
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
                                            PraceSeZvukem.vratZvukCountdownPodlePozice(zvukCountdown)
                                        )
                                        mediaPlayer!!.setVolume(volume, volume)
                                        mediaPlayer!!.start()
                                    }
                                    else -> {}
                                }
                            }
                        }
                        else -> {}
                    }
                    Log.d("Servica stav/pomocny: ",stav.toString()+"/"+pomocny.toString())

               //     mNotificationManager?.notify(ONGOING_NOTIFICATION, notification)
                }
                Log.d("SSS","pocetTabat: "+pocetTabat.toString())

                mNotificationManager?.notify(ONGOING_NOTIFICATION, notification)
                //zkopírováno z ClassicActivity-konec

            }

            override fun onFinish() {
                result = 0
            }
        }
        counter?.start()
    }

    fun setNotification() {
        Log.d("SSS", "setNotification-zacatek")
        notificationIntent = Intent(this, ClassicActivity::class.java)

        // aby se přepnulo na existující instanci aktivity a vymazali všechny další aktivity nad ní zdroj: https://www.peachpit.com/articles/article.aspx?p=1874864
        //ke stejné funkci, aby byla otevřená pouze jedna ClassicActivity jsem dal do manifestu k této activitě android:launchMode="singleTask", takže níže uvedené možná není už potřeba a dubluje se to

        notificationIntent?.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        notificationIntent?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val bubbleIntent = PendingIntent.getActivity(this, 0,  notificationIntent, 0)
        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            } else {
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
                //pokud to bude nižší Android než 8 (version code O), tak podle dokumentace by měl channelId být ignorován
            }

        stopSelf = Intent(this, ClassicService::class.java)
        stopSelf!!.action = "ACTION_STOP_SERVICE"
        pStopSelf = PendingIntent.getService(this, 0, stopSelf!!, 0)

        pauzePlay = Intent(this, ClassicService::class.java)
        pauzePlay!!.action = "ACTION_PLAY_PAUSE_SERVICE"
        ppauzePlay = PendingIntent.getService(this, 0, pauzePlay!!, 0)


        notificationBuilder = NotificationCompat.Builder(this, channelId )
        notification = notificationBuilder!!
            .setOngoing(true)
            .setAutoCancel(true) //ABY SE PO KLIKNUTÍ NA NOTIFIKACI SAMA ZNIČILA. K TOMU ALE MUSÍM MÍT V PŘEDEŠLÉ ACTIVITY NASTAVENO, ŽE SE TADY MUSÍ ZNIČIT ODPOČÍTÁVÁNÍ - ZNIČIT VLÁKNO
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            //.setPriority(NotificationCompat.PRIORITY_HIGH)   // heads-up
            .setContentTitle(resources.getText(R.string.app_name))
            .setCategory(Notification.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            //.setDefaults(Notification.DEFAULT_ALL)
          //  .setDefaults(DEFAULT_SOUND)
         //   .setDefaults(DEFAULT_VIBRATE) //Important for heads-up
          //  .setProgress(100,0,false)
            .setContentText("")
          //  .setTicker("Test Ticker Text")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        //    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
       //     .setVibrate(LongArray(0))


            .addAction(R.mipmap.play, "Pause", ppauzePlay)
            .addAction(R.drawable.back_pokus, "cancel", pStopSelf) //pokud budu chtít dát nějakou další akci například
            .build()


        nastavPocatecniHodnoty() //je potřeba nastavit počáteční hodnoty a je třeba to vložil sem, protože při pauze, kde
        //tikání časovače ignorovalo další propisování nové hodnoty do notifikace, se aktuální čas v pauze špatně propisoval

        notification!!.flags = Notification.DEFAULT_LIGHTS
        notification!!.flags = Notification.FLAG_AUTO_CANCEL
        startForeground(ONGOING_NOTIFICATION,notification)
        Log.d("SSS", "setNotification-konec")
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String{
        Log.d("SSS", "createNotificationChannel()-zacatek")
        val channelName = "Interval Timer Classic Background Service"
        chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH)
        chan!!.description = "Interval Timer Classic Background Service description"
        chan!!.setShowBadge(true)
        chan!!.lockscreenVisibility = Notification.VISIBILITY_PUBLIC

        mNotificationManager = getSystemService(
            NotificationManager::class.java
        )
   //     mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager!!.createNotificationChannel(chan!!)



        Log.d("SSS", "createNotificationChannel()-konec")
        return channelId
    }



    fun killService() {
        //aby šlo tuto servisu zničit, musím  kilnout i vlákno odpočítávání, tedy counter
        counter?.cancel() //musím killnout
        Log.d("SSS","killservice-start");
        // we need this release because of Doze Mode
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
            Log.d("SSS","killservice-stop");
        } catch (e: Exception) {
        }
    }

    fun nastavOdpocitavani(casOdpocitavani: MyTime?) {
        pocetTabat = pocetTabat - 1
        initCountDownTimer(100000.times(1000))


     //   initCountDownTimer((casOdpocitavani?.hour?.times(3600)!!+ casOdpocitavani?.min?.times(60)!! + casOdpocitavani?.sec+2).times(1000))
    }

    fun nastavHodnoty(
        aktualniCyklus: Int,
        puvodniPocetCyklu: Int,
        casPripravy: MyTime?,
        colorDlazdiceCasPripravy: Int?,
        casCviceni: MyTime?,
        colorDlazdiceCasCviceni: Int?,
        casPauzy: MyTime?,
        colorDlazdiceCasPauzy: Int?,
        casCelkovy: MyTime,
        colorDlazdicePocetCyklu: Int,
        stav: Byte,
        pomocny: Long,
        pauzaNeniZmacknuta: Boolean,
        pocetCyklu: Int) {

        Log.d("SSS", "0")
        this.aktualniCyklus = aktualniCyklus
        this.puvodniPocetCyklu = puvodniPocetCyklu
        this.casPripravy = casPripravy
        this.colorDlazdiceCasPripravy = colorDlazdiceCasPripravy!!
        this.casCviceni = casCviceni
        this.colorDlazdiceCasCviceni = colorDlazdiceCasCviceni!!
        this.casPauzy = casPauzy
        this.colorDlazdiceCasPauzy =  colorDlazdiceCasPauzy!!
        this.casCelkovy = casCelkovy
        this.colorDlazdicePocetCyklu = colorDlazdicePocetCyklu
        this.stav = stav
        this.pomocny = pomocny
        this.pocetCyklu = pocetCyklu
        this.pauzaNeniZmacknuta = pauzaNeniZmacknuta

    }

    fun nastavPocatecniHodnoty() {

        Log.d("SSS", "1")
        when (stav.toInt()) {
            0 -> {
                Log.d("SSS", "00")
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification = notificationBuilder?.setOngoing(true)?.setColor(colorDlazdiceCasPripravy)?.build()
                    //color
                } else notification = notificationBuilder?.setOngoing(true)?.setColor(resources.getColor(
                    R.color.colorCasPripravy
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification

                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification = notificationBuilder?.setOngoing(true)?.setContentText(
                        resources.getText(R.string.cvic)
                    )?.setColor(colorDlazdiceCasPripravy)?.build()
                    //color
                } else notification = notificationBuilder?.setOngoing(true)?.setContentText(resources.getText(R.string.cvic))?.setColor(resources.getColor(
                    R.color.colorCasPripravy
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
            }
            1 -> {
                Log.d("SSS", "11")
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification = notificationBuilder?.setOngoing(true)?.setColor(colorDlazdiceCasCviceni)?.build()
                    //color
                } else notification = notificationBuilder?.setOngoing(true)?.setColor(resources.getColor(
                    R.color.colorCasCviceni
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
            }
            2 -> {
                Log.d("SSS", "22")
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    notification = notificationBuilder?.setOngoing(true)?.setColor(colorDlazdiceCasPauzy)?.build()
                    //color
                } else notification = notificationBuilder?.setOngoing(true)?.setColor(resources.getColor(
                    R.color.colorCasPauzy
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
            }
            3 -> {
                Log.d("SSS", "33")
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                notification = notificationBuilder?.setOngoing(true)?.setColor(resources.getColor(
                    R.color.colorCasPauzyMeziTabatami
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
            }
            4 -> {
                Log.d("SSS", "44")
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                notification = notificationBuilder?.setOngoing(true)?.setColor(resources.getColor(
                    R.color.colorKonecTabaty
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
            }
            5 -> {
                Log.d("SSS", "55")
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
                notification = notificationBuilder?.setOngoing(true)?.setColor(resources.getColor(
                    R.color.colorCasCoolDown
                ))?.build()
                //takhle to budu dělat, asi ručně a nakonci swithce dát updateNotification
            }
        }
        nastavCislice(this.pomocny)
        Log.d("SSS", "2")
    }



    fun nastavZvuky(zvukStart: Int, zvukStop: Int, zvukCelkovyKonec: Int,
                    zvukCountdown: Int, zvukPulkaCviceni: Int, casPulkyKola: Int,
                    casPulkyKolaAktualni: Int, zvukPredkoncemKola: Int,casZvukuPredKoncemKola: Int,
                    hlasitost: Int, maxHlasitost: Int, volume: Float) {
        this.zvukStart = zvukStart
        this.zvukStop = zvukStop
        this.zvukCelkovyKonec = zvukCelkovyKonec
        this.zvukCountdown = zvukCountdown
        this.zvukPulkaCviceni = zvukPulkaCviceni
        this.casPulkyKola = casPulkyKola
        this.casPulkyKolaAktualni = casPulkyKolaAktualni
        this.zvukPredkoncemKola = zvukPredkoncemKola
        this.casZvukuPredKoncemKola = casZvukuPredKoncemKola
        this.hlasitost = hlasitost
        this.maxHlasitost = maxHlasitost
        this.volume = volume

    }

    private fun nastavCislice(hodnotaAktualni: Long) {
        Log.d("SSS", hodnotaAktualni.toString())

        if (hodnotaAktualni < 60) {
            notification = notificationBuilder?.setOngoing(true)?.setContentText(
                "$aktualniCyklus/$puvodniPocetCyklu"+"          " +  vratDeseticisla(pomocny.toInt())
            )?.build()

            vratDeseticisla(pomocny.toInt())?.let { Log.d("SSS", it) }
        } else {
            val minuty = (hodnotaAktualni % 60).toInt()
            notification = notificationBuilder?.setOngoing(true)?.setContentText(
                "$aktualniCyklus/$puvodniPocetCyklu"+"          " +  vratDeseticisla(((pomocny - minuty) / 60).toInt()) + ":" + vratDeseticisla(
                    minuty
                ))?.build()

            Log.d("SSS", vratDeseticisla(((pomocny - minuty) / 60).toInt()) + ":" + vratDeseticisla(
                minuty
            ))

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

   //     notification = notificationBuilder?.setProgress(100,curentProgress, false)?.build()
   //     curentProgress++
    }

}