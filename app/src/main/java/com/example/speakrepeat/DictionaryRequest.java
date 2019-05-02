package com.example.speakrepeat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class DictionaryRequest extends AsyncTask<String,Integer,String> implements TextToSpeech.OnInitListener {

    final String app_id = "bc2438f5";
    final String app_key = "f15b9357c7729f95b184193459ae5d6b";


    Context context;

    private TextToSpeech repeatTTS;

    DictionaryRequest(Context context){
        this.context = context;
    }


    @Override
    protected String doInBackground(String... params) {
        try {
//            myurl = strings[0];
            URL url = new URL(params[0]);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("app_id", app_id);
            urlConnection.setRequestProperty("app_key", app_key);

            // read the output from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }

            Log.d("Dictionary Request", "doInBackground: " + stringBuilder.toString());

            return stringBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();

        }
    }

    public void onInit(int initStatus) {
        //if successful, set locale
        if (initStatus == TextToSpeech.SUCCESS)
            repeatTTS.setLanguage(Locale.UK);//***choose your own locale here***

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        String def;

        try{
            JSONObject js = new JSONObject(s);
            JSONArray results = js.getJSONArray("results");

            JSONObject lEntries = results.getJSONObject(0);
            JSONArray larray = lEntries.getJSONArray("lexicalEntries");

            String word = lEntries.getString("id");

            JSONObject entries = larray.getJSONObject(0);
            JSONArray e = entries.getJSONArray("entries");

            JSONObject jsonObject = e.getJSONObject(0);
            JSONArray sensearray = jsonObject.getJSONArray("senses");

            JSONObject d = sensearray.getJSONObject(0);
            JSONArray de = d.getJSONArray("definitions");


//            Log.d("Name:",de[0].toString());
            def = de.toString();
            final String ans;
            ans = def.replace("[","");
            final String bns;
            bns = ans.replace("]","");

            Toast.makeText(context,bns,Toast.LENGTH_LONG).show();

            repeatTTS = new TextToSpeech(context,this);


            final AlertDialog.Builder alert = new AlertDialog.Builder(context);

            alert.setTitle("Definition of : "+word);
            alert.setMessage(bns);

            alert.setPositiveButton("Ok", null);
            alert.setNeutralButton("Speak", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alert.show();
                    repeatTTS.speak("Meaning: "+ bns, TextToSpeech.QUEUE_FLUSH, null);
                }
            });

            alert.show();

        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }


    }

}
