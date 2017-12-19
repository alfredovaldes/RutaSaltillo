package com.map.develop.rutasaltillov2.JSonParsers;
import android.content.Context;
import android.os.AsyncTask;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class parsePosiciones extends AsyncTask<Context, Void, List<LatLng>> {
    List<LatLng> points = new ArrayList<LatLng>();
    @Override
    protected Void doInBackground(Context... params) {
        Context context = (Context) params[0];
        try {
            String url = "https://busmia.herokuapp.com/posicion/";
            Log.d("WTF", url);
            Log.d("wtf", context.getFilesDir().getPath());
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
            for (int i = 0; i < ja.length(); i++) {
                JSONObject junior = ja.getJSONObject(String.valueOf(i));
                points.add(new LatLng(Double.parseDouble(junior.get("lat").toString()), Double.parseDouble(junior.get("lng").toString())));
                Log.d("LatLng", points.get(i).toString());
                setPoints(points);
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
    protected void onPostExecute(Void aVoid,List<LatLng>... params) {
        super.onPostExecute(aVoid);
        Log.d("wtf", "SI jalo el get de posiciones compa");
        return getPoints();
    }
    //Get y Set de Array List
    public List<LatLng> getPoints() {
        return points;
    }
    public void setPoints(List<LatLng> points) {
        this.points = points;
    }
}