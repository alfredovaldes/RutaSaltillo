package com.map.develop.rutasaltillov2.JSonParsers;
import android.os.AsyncTask;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import android.util.Log;

import com.map.develop.rutasaltillov2.Kotlin.MapsActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class parsePosiciones extends AsyncTask<Posiciones, Void, Void> {
    Posiciones posx = new Posiciones();
    @Override
    protected Void doInBackground(Posiciones... params) {
        posx = (Posiciones) params[0];
        try {
            String url = "https://busmia.herokuapp.com/posicion/"+posx.idCamion;
            Log.d("WTF", url);
            //Log.d("wtf", posx.context.getFilesDir().getPath());
            JSONParser parser = new JSONParser();
            File file = new File("/storage/emulated/0/Android/data/com.map.develop.rutasaltillov2/files/jwt.token");
            FileReader fileReader = new FileReader(file);
            Object obj = parser.parse(fileReader);
            Log.d("WTF", obj.toString());
            JSONObject jsonObject = new JSONObject(obj.toString());
            String token = jsonObject.get("token").toString();
            Log.d("WTFt", token);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("authorization", "Bearer " + token)
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject ja = new JSONObject(response.body().string());
                posx.latitud= Double.valueOf(ja.get("lat").toString());
                posx.longitud=Double.valueOf(ja.get("lng").toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        params[0]=posx;
        return null;
    }

    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d("wtf",(String.valueOf(posx.latitud)));
        Log.d("wtf",(String.valueOf(posx.longitud)));
        MapsActivity mapsActivity = new MapsActivity();
        //mapsActivity.addMarker(posx.latitud,posx.longitud);

    }
}