package com.machy1979ii.intervaltimer.funkce;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Simonka Patlokova on 22. 3. 2017.
 */

public class PraceSeSouboremTabata {

    public PraceSeSouboremTabata() {

    }

    public void writeToFile(String data) {


        String path = Environment.getExternalStorageDirectory().toString();
        String vysledekUlozeni;

        File dir = new File(path + "/TabataTimer");
        if (!dir.exists())
            dir.mkdirs();

        String nazevSouboru = ("data.txt");

        File file = new File(dir, nazevSouboru);
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

    public String readFromFile() throws IOException {

        String path = Environment.getExternalStorageDirectory().toString();
        String vysledekUlozeni;

        File dir = new File(path + "/TabataTimer");
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

        String nacteny = new String(bytes);
        return nacteny;
    }

    public void writeToFileInternal(String data, Context context) {

        File file = new File(context.getFilesDir() + "/TabataTimer", "data.txt");

        //      String path = Environment.getExternalStorageDirectory().toString();
        //      String vysledekUlozeni;

        File dir = new File(context.getFilesDir() + "/TabataTimer");
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

    public String readFromInternalFile(Context context) throws IOException {

        String path = context.getFilesDir().toString();
        File dir = new File(path + "/TabataTimer");
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

        String nacteny = new String(bytes);
        return nacteny;
    }


    public void writeToFileInternalZvuk(String data, Context context) {

        File file = new File(context.getFilesDir() + "/TabataTimer", "datatime.txt");

        //      String path = Environment.getExternalStorageDirectory().toString();
        //      String vysledekUlozeni;

        File dir = new File(context.getFilesDir() + "/TabataTimer");
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

        String path = context.getFilesDir().toString();
        File dir = new File(path + "/TabataTimer");
        String nazevSouboru = ("datatime.txt");
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

        String nacteny = new String(bytes);
        return nacteny;
    }

    public void writeToFileInternalColor(String data, Context context) {
        Log.i("asdf:", "5");
        File file = new File(context.getFilesDir() + "/TabataTimer", "datacolor.txt");

        //      String path = Environment.getExternalStorageDirectory().toString();
        //      String vysledekUlozeni;

        File dir = new File(context.getFilesDir() + "/TabataTimer");
        if (!dir.exists())
            dir.mkdirs();
        Log.i("asdf:", "6");
        try {
            Log.i("asdf:", "7");
            FileOutputStream stream = new FileOutputStream(file);
            Log.i("asdf:", "8");
            try {
                Log.i("asdf:", "9");
                stream.write(data.getBytes());

            } finally {
                Log.i("asdf:", "10");
                stream.close();
            }
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());

        }


    }

    public String readFromInternalFileColor(Context context) throws IOException {

        String path = context.getFilesDir().toString();
        File dir = new File(path + "/TabataTimer");
        String nazevSouboru = ("datacolor.txt");
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

        String nacteny = new String(bytes);
        return nacteny;
    }
}
