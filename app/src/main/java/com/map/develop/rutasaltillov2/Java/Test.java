package com.map.develop.rutasaltillov2.Java;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.map.develop.rutasaltillov2.JSonParsers.Rutas;
import com.map.develop.rutasaltillov2.R;
import com.map.develop.rutasaltillov2.JSonParsers.jsonParseRutas;

import java.util.ArrayList;
import java.util.List;

import static com.map.develop.rutasaltillov2.JSonParsers.jsonParseRutas.getListaRutas;

public class Test extends AppCompatActivity{

    public AutoCompleteTextView textView;
    static String selectionRutas="";

    List<Rutas> rutasTemp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        //data=(TextView)findViewById(R.id.fetchdata);

        jsonParseRutas process=new jsonParseRutas();
        process.execute();

        textView = findViewById(R.id.autocomplete_region);
        ArrayList<? extends Object> rutas=getListaRutas();
        ArrayAdapter<? extends Object> adapter2 = new ArrayAdapter<Object>(this, android.R.layout.simple_list_item_1, (List<Object>) rutas);
        textView.setAdapter(adapter2);

        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectionRutas = (String)parent.getItemAtPosition(position);
                setSelectionRutas(selectionRutas);
            }
        });


    }

    public static String getSelectionRutas() {
        return selectionRutas;
    }

    public void setSelectionRutas(String selectionRutas) {
        this.selectionRutas = selectionRutas;
    }


    public void ponerRutas(List<Rutas> rut)
    {
        for(Rutas ruts:rut)
        {

        }
    }

}
