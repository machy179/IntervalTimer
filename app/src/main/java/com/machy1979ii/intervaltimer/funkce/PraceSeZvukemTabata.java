package com.machy1979ii.intervaltimer.funkce;

import android.content.Context;
import android.media.MediaPlayer;

import com.machy1979ii.intervaltimer.R;

public class PraceSeZvukemTabata {
    private static MediaPlayer zvuk;

    private static int pocetZvukuStartStop = 49;
    private static String[] nazvyZvukuStartStop = {"sound 1", "sound 2", "sound 3", "sound 4", "sound 5", "sound 6", "sound 7", "sound 8", "sound 9", "sound 10",
            "sound 11", "sound 12", "sound 13", "sound 14", "sound 15", "sound 16", "sound 17", "sound 18", "sound 19", "sound 20",
            "Sound 21", "sound 22", "sound 23", "sound 24", "sound 25", "sound 26", "sound 27", "sound 28", "sound 29", "sound 30",
            "Sound 31", "sound 32"};
    private static String[] nazvyZvukuStartStop2 = new String[49];

    private static int pocetZvukuKonec = 55;
    private static String[] nazvyZvukuKonec = {"sound 1", "sound 2", "sound 3", "sound 4", "sound 5", "Sound 6", "sound 7", "sound 8", "sound 9", "sound 10",
            "Sound 11", "sound 12", "sound 13", "sound 14", "sound 15", "Sound 16", "sound 17", "sound 18"};
    private static String[] nazvyZvukuKonec2 = new String[55];

    private static int pocetZvukuCountDown = 21;

    private static String[] nazvyZvukuCountdown = {"sound 1", "sound 2", "sound 3", "Sound 4", "sound 5", "sound 6","Sound 7", "sound 8", "sound 9","Sound 10",
            "sound 11"};

    private static String[] nazvyZvukuCountdown2 = new String[21];

    public static int vratZvukStartStopPodlePozice (int pozice) {
        int navratovaPozice= R.raw.boxstop;
        switch ((int) pozice) {
            case 1:
                navratovaPozice= R.raw.boxstop;
                break;
            case 2:
                navratovaPozice= R.raw.boxstart;
                break;
            case 3:
                navratovaPozice= R.raw.bell2;
                break;
            case 4:
                navratovaPozice= R.raw.bell3;
                break;
            case 5:
                navratovaPozice= R.raw.bell4;
                break;
            case 6:
                navratovaPozice= R.raw.yougogirl;
                break;
            case 7:
                navratovaPozice= R.raw.shipsbell;
                break;
            case 8:
                navratovaPozice= R.raw.bell7;
                break;
            case 9:
                navratovaPozice= R.raw.belltinyshake;
                break;
            case 10:
                navratovaPozice= R.raw.boxingbell;
                break;
            case 11:
                navratovaPozice= R.raw.robotgo;
                break;
            case 12:
                navratovaPozice= R.raw.go2;
                break;
            case 13:
                navratovaPozice= R.raw.hit;
                break;
            case 14:
                navratovaPozice= R.raw.letsgo1;
                break;
            case 15:
                navratovaPozice= R.raw.letsgo2;
                break;
            case 16:
                navratovaPozice= R.raw.receptionbell;
                //     zvuk.setDataSource(context, R.raw.boxstop);
                break;


            case 17:
                navratovaPozice= R.raw.cs_1;
                break;
            case 18:
                navratovaPozice= R.raw.cs_2;
                break;
            case 19:
                navratovaPozice= R.raw.cs3;
                break;
            case 20:
                navratovaPozice= R.raw.cs4;
                //     zvuk.setDataSource(context, R.raw.boxstop);
                break;
            case 21:
                navratovaPozice= R.raw.s5;
                break;
            case 22:
                navratovaPozice= R.raw.s6;
                break;
            case 23:
                navratovaPozice= R.raw.s7;
                break;
            case 24:
                navratovaPozice= R.raw.s8;
                break;
            case 25:
                navratovaPozice= R.raw.sk14;
                break;
            case 26:
                navratovaPozice= R.raw.s10;
                break;
            case 27:
                navratovaPozice= R.raw.s11;
                break;
            case 28:
                navratovaPozice= R.raw.s12;
                break;
            case 29:
                navratovaPozice= R.raw.sc16;
                break;
            case 30:
                navratovaPozice= R.raw.sc17;
                break;
            case 31:
                navratovaPozice= R.raw.sc27;
                break;
            case 32:
                navratovaPozice= R.raw.sc28;
                break;
            case 33:
                navratovaPozice= R.raw.newstart1;
                break;
            case 34:
                navratovaPozice= R.raw.newstart19;
                break;
            case 35:
                navratovaPozice= R.raw.newstart3;
                break;
            case 36:
                navratovaPozice= R.raw.newstart4;
                break;
            case 37:
                navratovaPozice= R.raw.newstart5;
                break;
            case 38:
                navratovaPozice= R.raw.newstart6;
                break;
            case 39:
                navratovaPozice= R.raw.newstart7;
                break;
            case 40:
                navratovaPozice= R.raw.newstart8;
                break;
            case 41:
                navratovaPozice= R.raw.newstart20;
                break;
            case 42:
                navratovaPozice= R.raw.newstart10;
                break;
            case 43:
                navratovaPozice= R.raw.newstart11;
                break;
            case 44:
                navratovaPozice= R.raw.newstart12;
                break;
            case 45:
                navratovaPozice= R.raw.newstart18;
                break;
            case 46:
                navratovaPozice= R.raw.newstart17;
                break;
            case 47:
                navratovaPozice= R.raw.newstart15;
                break;
            case 48:
                navratovaPozice= R.raw.newstart16;
                break;
            case 49:
                navratovaPozice= R.raw.nic;
                break;





            default:
                navratovaPozice= R.raw.boxstop;
                break;
        }

        return navratovaPozice;
    }

    public static int vratZvukKonecPodlePozice (int pozice) {
        int navratovaPozice= R.raw.afiobsessionendrock;
        switch ((int) pozice) {
            case 1:
                navratovaPozice= R.raw.fanfara3;
                break;
            case 2:
                navratovaPozice= R.raw.afiobsessionendrock;
                break;
            case 3:
                navratovaPozice= R.raw.davidbainendgamefail;
                break;
            case 4:
                navratovaPozice= R.raw.ramagochiviolinend;
                break;

            case 5:
                navratovaPozice= R.raw.speedenzaendoftheworldvoice;
                break;
            case 6:
                navratovaPozice= R.raw.k15;
                break;
            case 7:
                navratovaPozice= R.raw.k18;
                break;
            case 8:
                navratovaPozice= R.raw.k19;
                break;

            case 9:
                navratovaPozice= R.raw.k20;
                break;
            case 11:
                navratovaPozice= R.raw.k21;
                break;
            case 12:
                navratovaPozice= R.raw.k22;
                break;
            case 13:
                navratovaPozice= R.raw.k23;
                break;

            case 14:
                navratovaPozice= R.raw.k24;
                break;
            case 15:
                navratovaPozice= R.raw.k25;
                break;
            case 16:
                navratovaPozice= R.raw.k26;
                break;
            case 17:
                navratovaPozice= R.raw.sk14;
                break;
            case 18:
                navratovaPozice= R.raw.k13;
                break;
            case 19:
                navratovaPozice= R.raw.receptionbell;
                break;
            case 20:
                navratovaPozice= R.raw.sc27;
                break;
            case 21:
                navratovaPozice= R.raw.boxstop;
                break;
            case 22:
                navratovaPozice= R.raw.boxstart;
                break;
            case 23:
                navratovaPozice= R.raw.bell2;
                break;
            case 24:
                navratovaPozice= R.raw.bell3;
                break;
            case 25:
                navratovaPozice= R.raw.bell4;
                break;
            case 26:
                navratovaPozice= R.raw.boxingbell;
                break;
            case 27:
                navratovaPozice= R.raw.shipsbell;
                break;
            case 28:
                navratovaPozice= R.raw.bell7;
                break;
            case 29:
                navratovaPozice= R.raw.belltinyshake;
                break;
            case 30:
                navratovaPozice= R.raw.s8;
                break;
            case 31:
                navratovaPozice= R.raw.sk14;
                break;
            case 32:
                navratovaPozice= R.raw.s10;
                break;
            case 33:
                navratovaPozice= R.raw.s11;
                break;
            case 34:
                navratovaPozice= R.raw.s12;
                break;
            case 35:
                navratovaPozice= R.raw.sc16;
                break;
            case 36:
                navratovaPozice= R.raw.sc17;
                break;
            case 37:
                navratovaPozice= R.raw.sc27;
                break;
            case 38:
                navratovaPozice= R.raw.sc28;
                break;
            case 39:
                navratovaPozice= R.raw.newstart1;
                break;
            case 40:
                navratovaPozice= R.raw.newstart19;
                break;
            case 41:
                navratovaPozice= R.raw.newstart3;
                break;
            case 42:
                navratovaPozice= R.raw.newstart4;
                break;
            case 43:
                navratovaPozice= R.raw.newstart5;
                break;
            case 44:
                navratovaPozice= R.raw.newstart6;
                break;
            case 45:
                navratovaPozice= R.raw.newstart7;
                break;
            case 46:
                navratovaPozice= R.raw.newstart8;
                break;
            case 47:
                navratovaPozice= R.raw.newstart20;
                break;
            case 48:
                navratovaPozice= R.raw.newstart10;
                break;
            case 49:
                navratovaPozice= R.raw.newstart11;
                break;
            case 50:
                navratovaPozice= R.raw.newstart12;
                break;
            case 51:
                navratovaPozice= R.raw.newstart18;
                break;
            case 52:
                navratovaPozice= R.raw.newstart17;
                break;
            case 53:
                navratovaPozice= R.raw.newstart15;
                break;
            case 54:
                navratovaPozice= R.raw.newstart16;
                break;
            case 55:
                navratovaPozice= R.raw.nic;
                break;



            default:
                navratovaPozice= R.raw.fanfara;
                break;
        }

        return navratovaPozice;
    }

    public static int vratZvukCountdownPodlePozice (int pozice) {
        int navratovaPozice= R.raw.tik3;
        switch ((int) pozice) {
            case 1:
                navratovaPozice= R.raw.tik3;
                break;
            case 2:
                navratovaPozice= R.raw.tik2;
                break;
            case 3:
                navratovaPozice= R.raw.tik;
                break;

            case 4:
                navratovaPozice= R.raw.cs_1;
                break;
            case 5:
                navratovaPozice= R.raw.cs_2;
                break;
            case 6:
                navratovaPozice= R.raw.cs3;
                break;
            case 7:
                navratovaPozice= R.raw.cs4;
                break;
            case 8:
                navratovaPozice= R.raw.sc16;
                break;
            case 9:
                navratovaPozice= R.raw.sc17;
                break;
            case 10:
                navratovaPozice= R.raw.sc27;
                break;
            case 11:
                navratovaPozice= R.raw.sc28;
                break;
            case 12:
                navratovaPozice= R.raw.newcount10;
                break;
            case 13:
                navratovaPozice= R.raw.newcount2;
                break;
            case 14:
                navratovaPozice= R.raw.newcount3;
                break;
            case 15:
                navratovaPozice= R.raw.newcount4;
                break;
            case 16:
                navratovaPozice= R.raw.newcount5;
                break;
            case 17:
                navratovaPozice= R.raw.newcount6;
                break;
            case 18:
                navratovaPozice= R.raw.newcount7;
                break;
            case 19:
                navratovaPozice= R.raw.newcount8;
                break;
            case 20:
                navratovaPozice= R.raw.newcount9;
                break;
            case 21:
                navratovaPozice= R.raw.nic;
                break;

            default:
                navratovaPozice= R.raw.tik3;
                break;
        }

        return navratovaPozice;
    }

    public static void spustZvukStartStop(Context context, int cisloZvuku) {

        switch ((int) cisloZvuku) {
            case 1:
                zvuk = MediaPlayer.create(context, R.raw.boxstop);
                zvuk.start();
                //     zvuk.setDataSource(context, R.raw.boxstop);
                break;
            case 2:
                zvuk = MediaPlayer.create(context, R.raw.boxstart);
                zvuk.start();
                break;
            case 3:
                zvuk = MediaPlayer.create(context, R.raw.bell2);
                zvuk.start();
                break;
            case 4:
                zvuk = MediaPlayer.create(context, R.raw.bell3);
                zvuk.start();
                break;
            case 5:
                zvuk = MediaPlayer.create(context, R.raw.bell4);
                zvuk.start();
                //     zvuk.setDataSource(context, R.raw.boxstop);
                break;
            case 6:
                zvuk = MediaPlayer.create(context, R.raw.yougogirl);
                break;
            case 7:
                zvuk = MediaPlayer.create(context, R.raw.shipsbell);
                zvuk.start();
                break;
            case 8:
                zvuk = MediaPlayer.create(context, R.raw.bell7);
                zvuk.start();
                break;
            case 9:
                zvuk = MediaPlayer.create(context, R.raw.belltinyshake);
                zvuk.start();
                break;
            case 10:
                zvuk = MediaPlayer.create(context, R.raw.boxingbell);
                zvuk.start();
                break;
            case 11:
                zvuk = MediaPlayer.create(context, R.raw.robotgo);
                zvuk.start();
                break;
            case 12:
                zvuk = MediaPlayer.create(context, R.raw.go2);
                zvuk.start();
                break;
            case 13:
                zvuk = MediaPlayer.create(context, R.raw.hit);
                zvuk.start();
                break;
            case 14:
                zvuk = MediaPlayer.create(context, R.raw.letsgo1);
                zvuk.start();
                break;
            case 15:
                zvuk = MediaPlayer.create(context, R.raw.letsgo2);
                zvuk.start();
                break;
            case 16:
                zvuk = MediaPlayer.create(context, R.raw.receptionbell);
                zvuk.start();
                //     zvuk.setDataSource(context, R.raw.boxstop);
                break;

            default:
                zvuk = MediaPlayer.create(context, R.raw.boxstop);
                zvuk.start();
                break;
        }


        //    Toast.makeText(context,Integer.toString(cisloZvuku), Toast.LENGTH_LONG).show();

        //zvuk.start();
    }

    public static void spustZvukKonec(Context context, int cisloZvuku) {

        switch ((int) cisloZvuku) {
            case 1:
                zvuk = MediaPlayer.create(context, R.raw.speedenzaendoftheworldvoice);
                break;
            case 2:
                zvuk = MediaPlayer.create(context, R.raw.afiobsessionendrock);
                break;
            case 3:
                zvuk = MediaPlayer.create(context, R.raw.davidbainendgamefail);
                break;
            case 4:
                zvuk = MediaPlayer.create(context, R.raw.ramagochiviolinend);
                break;

            default:
                zvuk = MediaPlayer.create(context, R.raw.afiobsessionendrock);
                break;

        }
        zvuk.start();
    }

    public static int vratPocetZvukuStartStop() {
        return pocetZvukuStartStop;

    }

    public static int vratPocetZvukuKonec() {
        return pocetZvukuKonec;

    }

    public static int vratPocetZvukuCountDown() {
        return pocetZvukuCountDown;

    }

    public static String[] vratNazvyZvukuStartStop(String nazevZvukuPolozka) {

        for (int i=0; i<(pocetZvukuStartStop); i++) {
            if ((i+1)==(pocetZvukuStartStop)) { //pokud to bude ten poslední zvuk, tak ten je prázdný
                nazvyZvukuStartStop2[i] = "X";
            } else {
                nazvyZvukuStartStop2[i] = nazevZvukuPolozka+ " " +String.valueOf(i+1);
            }
            //    nazvyZvukuStartStop2[i] = nazevZvukuPolozka+ " " +String.valueOf(i+1);
        }

        return nazvyZvukuStartStop2;
        //    return nazvyZvukuStartStop;

    }

    public static String[] vratNazvyZvukuKonec(String nazevZvukuPolozka) {
        for (int i=0; i<(pocetZvukuKonec); i++) {
            if ((i+1)==(pocetZvukuKonec)) { //pokud to bude ten poslední zvuk, tak ten je prázdný
                nazvyZvukuKonec2[i] = "X";
            } else {
                nazvyZvukuKonec2[i] = nazevZvukuPolozka+ " " +String.valueOf(i+1);
            }
            //      nazvyZvukuKonec2[i] = nazevZvukuPolozka+ " " +String.valueOf(i+1);
        }

        return nazvyZvukuKonec2;
        //     return nazvyZvukuKonec;

    }

    public static String[] vratNazvyZvukuCountdown(String nazevZvukuPolozka) {
        for (int i=0; i<(pocetZvukuCountDown); i++) {
            //       nazvyZvukuCountdown2[i] = nazevZvukuPolozka+ " " +String.valueOf(i+1);
            if ((i+1)==(pocetZvukuCountDown)) { //pokud to bude ten poslední zvuk, tak ten je prázdný
                nazvyZvukuCountdown2[i] = "X";
            } else {
                nazvyZvukuCountdown2[i] = nazevZvukuPolozka+ " " +String.valueOf(i+1);
            }
        }

        return nazvyZvukuCountdown2;
//        return nazvyZvukuCountdown;

    }

}
