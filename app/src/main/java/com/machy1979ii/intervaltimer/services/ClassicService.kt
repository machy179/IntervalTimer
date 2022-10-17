package com.machy1979ii.intervaltimer.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.machy1979ii.intervaltimer.ClassicActivity
import com.machy1979ii.intervaltimer.R
import com.machy1979ii.intervaltimer.funkce.PraceSeZvukem
import com.machy1979ii.intervaltimer.models.MyTime

class ClassicService : Service() {
    private val mBinder: IBinder = MyBinder()
    private val channelId = "Notification from Service"
    private var notification: Notification? = null
    private var notificationBuilder: NotificationCompat.Builder? =null
    private val ONGOING_NOTIFICATION = 1010
    private var mNotificationManager: NotificationManager? = null
    public var ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"
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
    var puvodniPocetTabat = 1
    var puvodniPocetCyklu = 0
    var aktualniCyklus = 1
    var pocetCyklu = 0
    var pauzaMeziTabatami = 0
    var pomocny: Long = 0
    var pauzaNeniZmacknuta = true

    var dlazdiceOdpocitavace = null
    var textViewCas: String = ""

    var preskocVypisCasu = false



    var stav: Byte = 0 //0-priprava, 1-cviceni, 2-pauza, 3-pauza mezi tabatami

    //    private MediaPlayer tikZvuk3;
    //    private MediaPlayer tikZvuk2;
    //    private MediaPlayer tikZvuk1;
    //    private MediaPlayer restZvuk;
    //   private MediaPlayer startZvuk;
    //    private MediaPlayer fanfareZvuk;
    private var zvukStart = 1
    private var zvukStop = 1
    private var zvukCelkovyKonec = 1
    private var zvukCountdown = 1
    private var zvukPulkaCviceni = 33 //33 je, když není nastaven zvuk

    private var casPulkyKola = 0 //pokud uživatel nebude chtít, aby v půlce kola byl nějaký zvuk, tak hodnota bude 0

    private var casPulkyKolaAktualni = 0 //je potřeba ještě tuto proměnnou, protože když nastavím jinou délku kola zrovna v kole, tak by to habrovalo

    private var zvukPredkoncemKola = 33 //33 je, když není nastaven zvuk

    private var casZvukuPredKoncemKola = 20 //pokud uživatel nebude chtít, aby v půlce kola byl nějaký zvuk, tak hodnota bude 0


    private var mediaPlayer: MediaPlayer? = null

    private var hlasitost = 100
    private var maxHlasitost = 100
    var volume  = 0f

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

/*        val notification: Notification = NotificationCompat.Builder(this, CHANNELID)
            .setContentTitle("title")
            .setContentText("text")
            .setSmallIcon(R.drawable.baseline_pause_white_24)
            .build()
        startForeground(2001, notification)*/

        Log.d("StartService","2");

        //to tady je proto, aby se po uvedení telefonu po vypnutí tato servica po cca 1 minutě nekillnula
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    inner class MyBinder : Binder() {
        val service: ClassicService
            get() = this@ClassicService
    }


    fun initCountDownTimer(time: Int?) {
        Log.d("Servica","initCountDownTimer:"+time.toString())
        counter = object : CountDownTimer(time!!.toLong(), 1000) {
            
            override fun onTick(millisUntilFinished: Long) {
                result =(millisUntilFinished / 1000).toInt()
                Log.d("notifikace",result.toString())
                Log.d("Servica",result.toString())

      //          pomocny = pomocny -1
        //        updateNotification(zobrazCasPomocny())
                
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
                    mNotificationManager?.notify(ONGOING_NOTIFICATION, notification)
                }
                //zkopírováno z ClassicActivity-konec

            }

            override fun onFinish() {
                result = 0
            }
        }
        counter?.start()
    }

    private fun resultToHourMinSec(): String {
        var hour = result/3600
        var min = (result % 3600)/60
        var sec = (result % 3600)%60

        casCelkovy?.hour ?: hour
        casCelkovy?.min ?: min
        casCelkovy?.sec ?: sec


        return hour.toString()+":"+min.toString()+":"+sec.toString()


    }

    fun setNotification4() {

    //    val notificationIntent = Intent(this, ClassicActivity::class.java)
        notificationIntent = Intent(this, ClassicActivity::class.java)

        notificationIntent?.putExtra("caspripavy", casPripravy)
        notificationIntent?.putExtra("cascviceni", casCviceni)
        notificationIntent?.putExtra("caspauzy", casPauzy)

        notificationIntent?.putExtra("cascelkovy", casCelkovy)

        notificationIntent?.putExtra("pocetcyklu", pocetCyklu)

        notificationIntent?.putExtra("barvaCviceni", colorDlazdiceCasCviceni)
        notificationIntent?.putExtra("barvaPauzy", colorDlazdiceCasPauzy) //color
        notificationIntent?.putExtra("barvaPocetCyklu", colorDlazdicePocetCyklu)
        notificationIntent?.putExtra("barvaPripravy", colorDlazdiceCasPripravy)

        notificationIntent?.putExtra("zvukstart", zvukStart)
        notificationIntent?.putExtra("zvukstop", zvukStop)
        notificationIntent?.putExtra("zvukcelkovykonec", zvukCelkovyKonec)
        notificationIntent?.putExtra("zvukcountdown", zvukCountdown)
        notificationIntent?.putExtra("zvukpulkakola", zvukPulkaCviceni)
        notificationIntent?.putExtra("zvukpredkoncemkola", zvukPredkoncemKola)
        notificationIntent?.putExtra("caszvukupupredkoncemkola", casZvukuPredKoncemKola)

        notificationIntent?.putExtra("hlasitost", hlasitost)

        Log.d(
            "FindingError",
            "colorDlazdicePocetCyklu +++--- : " + colorDlazdicePocetCyklu
        )






        //   notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("Servica","setNotification4()")

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel()
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
                //pokud to bude nižší Android než 8 (version code O), tak podle dokumentace by měl channelId být ignorován
            }


        Log.d("Servica","setNotification4-2")
        notificationBuilder = NotificationCompat.Builder(this, channelId )
        notification = notificationBuilder!!.setOngoing(true)
            .setAutoCancel(true) //ABY SE PO KLIKNUTÍ NA NOTIFIKACI SAMA ZNIČILA. K TOMU ALE MUSÍM MÍT V PŘEDEŠLÉ ACTIVITY NASTAVENO, ŽE SE TADY MUSÍ ZNIČIT ODPOČÍTÁVÁNÍ - ZNIČIT VLÁKNO
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(resources.getColor(R.color.colorCasCviceni))
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentTitle(resources.getText(R.string.app_name))
            .setContentText("Time: "+ result)
            .setCategory(Notification.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            //   .addAction(R.drawable.ic_launcher_foreground, "Stop", pStopSelf) //pokud budu chtít dát nějakou další akci například

            .build()


        notification!!.flags = Notification.DEFAULT_LIGHTS
        notification!!.flags = Notification.FLAG_AUTO_CANCEL
        Log.d("Servica","setNotification4-3")
        startForeground(ONGOING_NOTIFICATION,notification)
        Log.d("Servica","setNotification4-40," +
                "")
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String{

        val channelName = "My Background Service"
        chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_HIGH)
        chan!!.lightColor = Color.BLUE
        chan!!.importance = NotificationManager.IMPORTANCE_NONE
        chan!!.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager!!.createNotificationChannel(chan!!)

        //   val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //    service.createNotificationChannel(chan)
        return channelId
    }

    /**
     * This is the method that can be called to update the Notification
     */
    private fun updateNotification(text: String) {



        if(4970>result && result>4950) {
            notification = notificationBuilder?.setOngoing(true)?.setContentText(text)?.setColor(resources.getColor(
                R.color.colorCasPauzy
            ))?.build()
        } else notification = notificationBuilder?.setOngoing(true)?.setContentText(text)?.setColor(resources.getColor(
            R.color.colorCasCviceni
        ))?.build()

        //notification = notificationBuilder?.setOngoing(true)?.setContentText(text)?.build()
        mNotificationManager?.notify(ONGOING_NOTIFICATION, notification)
    }

    fun killService() { //aby šlo tuto servisu zničit, musím  kilnout i vlákno odpočítávání, tedy counter
        Log.d("Servica","killService()")
        counter?.cancel() //musím killnout
        stopForeground(true)
        stopSelf()
    }

    fun nastavOdpocitavani(casOdpocitavani: MyTime?) {
        pocetTabat = pocetTabat - 1
        Log.d("Servica","nastavOdpocitavani hour:"+ casOdpocitavani?.hour.toString())
        Log.d("Servica","nastavOdpocitavani min:"+ casOdpocitavani?.min.toString())
        Log.d("Servica","nastavOdpocitavani ses:"+ casOdpocitavani?.sec.toString())
        initCountDownTimer((casOdpocitavani?.hour?.times(3600)!!+ casOdpocitavani?.min?.times(60)!! + casOdpocitavani?.sec+1).times(1000))
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
        this.aktualniCyklus = aktualniCyklus
        this.puvodniPocetCyklu = puvodniPocetCyklu
        this.casPripravy = casPripravy
        this.colorDlazdiceCasPripravy = colorDlazdiceCasPripravy!!
        this.casCviceni = casCviceni
        this.colorDlazdiceCasCviceni = colorDlazdiceCasCviceni!!
        this.casPauzy = casPauzy
        this. colorDlazdiceCasPauzy =  colorDlazdiceCasPauzy!!
        this.casCelkovy = casCelkovy
        this.colorDlazdicePocetCyklu = colorDlazdicePocetCyklu
        this.stav = stav
        this.pomocny = pomocny
        this.pocetCyklu = pocetCyklu
        this.pauzaNeniZmacknuta = pauzaNeniZmacknuta

        Log.d(
            "FindingError",
            "colorDlazdicePocetCyklu +++ : " + this.colorDlazdicePocetCyklu
        )

        Log.d(
            "FindingError",
            "CasPripravy: " + casPripravy!!.hour.toString() + casPripravy!!.min.toString() + casPripravy!!.sec.toString()
        )

        Log.d(
            "FindingError",
            "CasCviceni ++++ : " + casCviceni!!.hour.toString()
        )

        Log.d(
            "FindingError",
            "CasPauzy : " + casPauzy!!.hour.toString() + casPauzy!!.min.toString() + casPauzy!!.sec.toString()
        )

        notificationIntent?.putExtra("caspripavy", casPripravy)
        notificationIntent?.putExtra("cascviceni", casCviceni)
        notificationIntent?.putExtra("caspauzy", casPauzy)

        notificationIntent?.putExtra("cascelkovy", casCelkovy)

        notificationIntent?.putExtra("pocetcyklu", pocetCyklu)

        notificationIntent?.putExtra("barvaCviceni", colorDlazdiceCasCviceni)
        notificationIntent?.putExtra("barvaPauzy", colorDlazdiceCasPauzy) //color
        notificationIntent?.putExtra("barvaPocetCyklu", colorDlazdicePocetCyklu)
        notificationIntent?.putExtra("barvaPripravy", colorDlazdiceCasPripravy)
      //  notificationIntent?.putExtra("pomocny", pomocny)


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



        notificationIntent?.putExtra("zvukstart", zvukStart)
        notificationIntent?.putExtra("zvukstop", zvukStop)
        notificationIntent?.putExtra("zvukcelkovykonec", zvukCelkovyKonec)
        notificationIntent?.putExtra("zvukcountdown", zvukCountdown)
        notificationIntent?.putExtra("zvukpulkakola", zvukPulkaCviceni)
        notificationIntent?.putExtra("zvukpredkoncemkola", zvukPredkoncemKola)
        notificationIntent?.putExtra("caszvukupupredkoncemkola", casZvukuPredKoncemKola)

        notificationIntent?.putExtra("hlasitost", hlasitost)





    }


    private fun zobrazCasPomocny(): String {
        if (pomocny < 60) {
            return pomocny.toString()
        } else {
            var hour = pomocny/3600
            var min = (pomocny % 3600)/60
            var sec = (pomocny % 3600)%60
            return hour.toString()+":"+min.toString()+":"+sec.toString()
        }

    }

    private fun nastavCislice(hodnotaAktualni: Long) {
        if (hodnotaAktualni < 60) {
            notification = notificationBuilder?.setOngoing(true)?.setContentText(
                vratDeseticisla(pomocny.toInt())
            )?.build()
        } else {
            val minuty = (hodnotaAktualni % 60).toInt()
            notification = notificationBuilder?.setOngoing(true)?.setContentText(
                vratDeseticisla(((pomocny - minuty) / 60).toInt()) + ":" + vratDeseticisla(
                    minuty
                ))?.build()

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

}