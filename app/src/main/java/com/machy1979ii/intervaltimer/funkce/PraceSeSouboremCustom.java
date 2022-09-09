package com.machy1979ii.intervaltimer.funkce;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.machy1979ii.intervaltimer.models.ArrayListSouborPolozekCasuKol;
import com.machy1979ii.intervaltimer.models.SouborPolozekCasuKola;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PraceSeSouboremCustom {

    private Object ArrayList;

    public PraceSeSouboremCustom() {
    }

    public void writeToFileInternal(ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol, Context context) {

        File file = new File(context.getFilesDir() + "/CustomTimer", "data.txt");

        //      String path = Environment.getExternalStorageDirectory().toString();
        //      String vysledekUlozeni;

        String data = vlozVsechnyPolozkyCasyKolDoJsonu(vsechnyPolozkyCasyKol);
        Log.d("nacteniSouboru", "vloženo do JSON: "+String.valueOf(data));

        File dir = new File(context.getFilesDir() + "/CustomTimer");
        if (!dir.exists())
            dir.mkdirs();

        try {
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(data.getBytes());
                Log.d("nacteniSouboru", "---------------------------Uloženo OK-------------------------------");
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            Log.d("nacteniSouboru", "File write failed: " + e.toString());

        }


    }

    private String vlozVsechnyPolozkyCasyKolDoJsonu(ArrayList<SouborPolozekCasuKola> vsechnyPolozkyCasyKol) {
        Gson gson = new Gson();
        ArrayListSouborPolozekCasuKol ArrayLisTrida = new ArrayListSouborPolozekCasuKol(vsechnyPolozkyCasyKol);

        String data = gson.toJson(ArrayLisTrida);
        Log.d("jsonPokus", String.valueOf(data));
        return data;
    }

    public ArrayList<SouborPolozekCasuKola> readFromInternalFile(Context context) throws IOException {

        String path = context.getFilesDir().toString();
        File dir = new File(path + "/CustomTimer");
        String nazevSouboru = ("data.txt");
        File file = new File(dir, nazevSouboru);
        int length = (int) file.length();

        byte[] bytes = new byte[length];

        FileInputStream in = new FileInputStream(file);
        try {
            in.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }

        Log.i("nacteniSouboru","666");
        String nactenyJson = new String(bytes);
        Log.d("nacteniSouboru", String.valueOf(nactenyJson));
        ArrayList<SouborPolozekCasuKola> SouborPolozekCasuKola = new ArrayList<SouborPolozekCasuKola>();
        SouborPolozekCasuKola = vratZJsonuVsechnyPolozkyCasyKol(nactenyJson);
        return SouborPolozekCasuKola;
    }

    private ArrayList<SouborPolozekCasuKola> vratZJsonuVsechnyPolozkyCasyKol(String nactenyJson) {
        Gson gson = new Gson();
        ArrayListSouborPolozekCasuKol souborPolozekCasuKola = gson.fromJson(nactenyJson, ArrayListSouborPolozekCasuKol.class);
        return souborPolozekCasuKola.getSouborPolozekCasuKola();

    }

    public void writeToFileInternalZvuk(String data, Context context) {

        File file = new File(context.getFilesDir() + "/CustomTimer", "datatime.txt");

        //      String path = Environment.getExternalStorageDirectory().toString();
        //      String vysledekUlozeni;

        File dir = new File(context.getFilesDir() + "/CustomTimer");
        if (!dir.exists())
            dir.mkdirs();

        try {
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(data.getBytes());

            } finally {
                stream.close();
            }
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());

        }


    }

    public String readFromInternalFileZvuk(Context context) throws IOException {
        Log.i("1-1", "chyba 1111");
        String path = context.getFilesDir().toString();
        File dir = new File(path + "/CustomTimer");
        String nazevSouboru = ("datatime.txt");
        File file = new File(dir, nazevSouboru);
        int length = (int) file.length();

        byte[] bytes = new byte[length];

        Log.i("1-1", "chyba 2222");

        FileInputStream in = new FileInputStream(file);
        try {
            in.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }

        String nacteny = new String(bytes);
        return nacteny;
    }


}
