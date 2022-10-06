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
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.machy1979ii.intervaltimer.ClassicActivity
import com.machy1979ii.intervaltimer.R
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

                pomocny = pomocny -1
                updateNotification(zobrazCasPomocny())

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

        //vytvoření killu servici z notifikace


  //      val stopSelf = Intent(this, ClassicService::class.java)
  //      stopSelf.setAction(ACTION_STOP_FOREGROUND_SERVICE);
        //   val pStopSelf = PendingIntent.getService(this, 0, stopSelf, 0))

        //    val pStopSelf = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT)

        Log.d("Servica","setNotification4-2")
        notificationBuilder = NotificationCompat.Builder(this, channelId )
        notification = notificationBuilder!!.setOngoing(true)
            .setAutoCancel(true) //ABY SE PO KLIKNUTÍ NA NOTIFIKACI SAMA ZNIČILA. K TOMU ALE MUSÍM MÍT V PŘEDEŠLÉ ACTIVITY NASTAVENO, ŽE SE TADY MUSÍ ZNIČIT ODPOČÍTÁVÁNÍ - ZNIČIT VLÁKNO
            .setSmallIcon(R.mipmap.ic_launcher)
            .setColor(resources.getColor(R.color.colorCasCviceni))
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentTitle("This will be boxing timer")
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
        Log.d("Servica","nastavOdpocitavani hour:"+ casOdpocitavani?.hour.toString())
        Log.d("Servica","nastavOdpocitavani min:"+ casOdpocitavani?.min.toString())
        Log.d("Servica","nastavOdpocitavani ses:"+ casOdpocitavani?.sec.toString())
        initCountDownTimer((casOdpocitavani?.hour?.times(3600)!!+ casOdpocitavani?.min?.times(60)!! + casOdpocitavani?.sec).times(1000))
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
        pomocny: Long
    ) {
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

}