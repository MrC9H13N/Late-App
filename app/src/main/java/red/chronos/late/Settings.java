    package red.chronos.late;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

    public class Settings extends AppCompatActivity {

    Integer countdown = 3;
    Toast toast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /*Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.EFFECT_HEAVY_CLICK));*/

        Button buttonDeleteAll = (Button) findViewById(R.id.buttonDeleteAll);
        Button buttonSendNotification = (Button) findViewById(R.id.buttonSendNotification);
        TextView textViewUserInfo = (TextView) findViewById(R.id.textViewUserInfo);
        Switch switchActivation = (Switch) findViewById(R.id.switchActivation);
        NotificationHelper notificationHelper = new NotificationHelper(this);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        textViewUserInfo.setText(sharedPref.getString("userInfo", null));

        switchActivation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPref.edit();
                if(isChecked){
                    notificationHelper.removeNotification();
                    editor.putBoolean("disableNotification", true);
                } else {
                    editor.putBoolean("disableNotification", false);
                }
                editor.apply();
            }
        });

        buttonDeleteAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                countdown--;
                if(countdown < 1){
                    sharedPref.edit().clear().apply();
                    Intent myIntent = new Intent(Settings.this, MainActivity.class);
                    Settings.this.startActivity(myIntent);
                } else {
                    if (toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(Settings.this, "Appuyez encore "+countdown+" fois sur le bouton afin de supprimer toutes les données", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        buttonSendNotification.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Calendar rightNow = Calendar.getInstance();
                int ch = rightNow.get(Calendar.HOUR_OF_DAY);
                if(ch < 13) {
                    notificationHelper.webRequest(Settings.this, "matin");
                } else {
                    notificationHelper.webRequest(Settings.this, "aprem");
                }

            }
        });
    }
}