package com.map.develop.rutasaltillov2.JSonParsers;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Mario on 17/12/2017.
 */

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.map.develop.rutasaltillov2.Java.Test;
import com.map.develop.rutasaltillov2.Kotlin.MapsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.util.Arrays.*;


public class jsonParseRutas extends AsyncTask<Context,Void,Void> {

    String data="";
    String dataParse="";
    static ArrayList listaRutas = new ArrayList();
    OkHttpClient client = new OkHttpClient();
    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    @Override
    protected Void doInBackground(Context... params) {
            Context context = (Context) params[0];
        try {

            String url="https://busmia.herokuapp.com/ruta";
            Log.d("WTF", url);
            /*
            HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
            InputStream inputStream=httpURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            String line="";

            while (line !=null)
            {
                line=bufferedReader.readLine();
                data+=line;

            }
            */
            Log.d("wtf", context.getFilesDir().getPath());
            JSONParser parser = new JSONParser();;
            File file = new File("/storage/emulated/0/Android/data/com.map.develop.rutasaltillov2/files/jwt.token");
            FileReader fileReader  = new FileReader(file);
            Object obj = parser.parse(fileReader);
            Log.d("WTF", obj.toString());
            JSONObject jsonObject= new JSONObject(obj.toString());
            String token = jsonObject.get("token").toString();
            Log.d("WTFt", token);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("authorization", "Bearer "+token)
                    .build();

            Response response = client.newCall(request).execute();
            JSONArray ja=new JSONArray(response.body().string());
            for(int i=0; i<ja.length();i++)
            {
                JSONObject jo=ja.getJSONObject(i);
                listaRutas.add(jo.get("nombre"));
                setListaRutas(listaRutas);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d("wtf", "SI jalo el get compa");
    }

    //Get y Set de Array List
    public static ArrayList getListaRutas() {
        return listaRutas;
    }

    public void setListaRutas(ArrayList listaRutas) {
        this.listaRutas = listaRutas;
    }


}