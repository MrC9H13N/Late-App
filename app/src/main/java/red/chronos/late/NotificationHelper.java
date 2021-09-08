package red.chronos.late;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

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

import java.util.HashMap;
import java.util.Map;

import red.chronos.Late;

public class NotificationHelper extends ContextWrapper {

    private NotificationManager notifManager;

    private static final String CHANNEL_HIGH_ID = "red.chronos.late.HIGH_CHANNEL";
    private static final String CHANNEL_HIGH_NAME = "High Channel";

    private static final String CHANNEL_DEFAULT_ID = "red.chronos.late.DEFAULT_CHANNEL";
    private static final String CHANNEL_DEFAUL_NAME = "Default Channel";


    public NotificationHelper( Context base ) {
        super( base );

        notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long [] swPattern = new long[] { 0, 500, 110, 500, 110, 450, 110, 200, 110,
                170, 40, 450, 110, 200, 110, 170, 40, 500 };

        NotificationChannel notificationChannelHigh = new NotificationChannel(
                CHANNEL_HIGH_ID, CHANNEL_HIGH_NAME, notifManager.IMPORTANCE_HIGH );
        notificationChannelHigh.enableLights( true );
        notificationChannelHigh.setLightColor( Color.RED );
        notificationChannelHigh.setShowBadge( true );
        notificationChannelHigh.enableVibration( true );
        notificationChannelHigh.setVibrationPattern( swPattern );
        notificationChannelHigh.setLockscreenVisibility( Notification.VISIBILITY_PUBLIC );
        notifManager.createNotificationChannel( notificationChannelHigh );

        NotificationChannel notificationChannelDefault = new NotificationChannel(
                CHANNEL_DEFAULT_ID, CHANNEL_DEFAUL_NAME, notifManager.IMPORTANCE_DEFAULT );
        notificationChannelDefault.enableLights( true );
        notificationChannelDefault.setLightColor( Color.WHITE );
        notificationChannelDefault.enableVibration( true );
        notificationChannelDefault.setShowBadge( false );
        notifManager.createNotificationChannel( notificationChannelDefault );
    }

    public void progressNotif(int id){
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_DEFAULT_ID);
        builder.setContentTitle("Récupération des données en cours")
                .setContentText("")
                .setOngoing(true)
                .setSmallIcon(R.drawable.countdown)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        int PROGRESS_MAX = 100;
        int PROGRESS_CURRENT = 0;
        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        notificationManager.notify(id, builder.build());

        builder.setProgress(0,0,true)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.glacier));
        notificationManager.notify(id, builder.build());
    }

    public static void cancelNotification(Context ctx, int notifyId) {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }

    public void webRequest(Context ctx, String moment){
        NotificationHelper notificationHelper = new NotificationHelper(ctx);
        cancelNotification(ctx,1);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        String username = sharedPref.getString("username", null);
        String password = sharedPref.getString("password", null);

        notificationHelper.progressNotif(2);

        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url ="https://www.late.chronos.red/";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        //Edit JSON data
                        String notifMessage = "";
                        if(response.equals("{\"events\" : []}")){
                            notifMessage = "Aucun cours aujourd'hui";
                        } else {
                            try {
                                JSONObject jObject = new JSONObject(response);
                                Log.i("WebRequest", response);
                                JSONArray jArray = jObject.getJSONArray("events");
                                for (int i=0; i < jArray.length(); i++)
                                {
                                    JSONObject oneObject = jArray.getJSONObject(i);
                                    String id_data = oneObject.getString("id");
                                    String title_data = oneObject.getString("title");
                                    title_data = title_data.replace("\n\n","");
                                    Log.i("INFO",title_data);
                                    if(title_data.startsWith("\n")){
                                        title_data = title_data.substring(2);
                                    }
                                    String[] partstitle = title_data.split("\n");
                                    String heure = partstitle[2];
                                    String matiere = partstitle[1];
                                    String salle = partstitle[0];
                                    salle = salle.replace(" - ","");
                                    if(salle.toLowerCase().contains("teams")) salle = "Teams";
                                    if(salle.toLowerCase().contains("isen")){
                                        salle = salle.substring(salle.toLowerCase().indexOf("isen"),salle.toLowerCase().indexOf("isen")+9);
                                    }
                                    Log.i("JSON", id_data);
                                    Log.i("JSON", heure);
                                    if(moment.equals("matin")){
                                        if(Integer.parseInt(heure.substring(0,2)) < 13){
                                            notifMessage += heure + " " + salle + "\n" + matiere + "\n\n";
                                        }
                                    } else if (moment.equals("aprem")){
                                        if(Integer.parseInt(heure.substring(0,2)) >= 13){
                                            notifMessage += heure + " " + salle + "\n" + matiere + "\n\n";
                                        }
                                    }
                                }
                                notifMessage = notifMessage.substring(0, notifMessage.length() - 2);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        cancelNotification(ctx,2);
                        if(moment.equals("matin")){
                            notificationHelper.notify(1, false, "Emploi du temps", notifMessage, ctx, "Après-midi");
                        } else if (moment.equals("aprem")){
                            notificationHelper.notify(1, false, "Emploi du temps", notifMessage, ctx, "Matin");
                        }

                        Log.i("Web",response);
                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }


    public void notify( int id, boolean prioritary, String title, String message, Context context, String secondButtonText) {
        Intent activityIntent = new Intent(context, MainActivity.class ) ;
        PendingIntent pActivityIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentUpdate = new Intent(context,NotificationUpdater.class);
        intentUpdate.putExtra("moment", secondButtonText);
        PendingIntent pIntentUpdate = PendingIntent.getBroadcast(context,1,intentUpdate,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentChangeTime = new Intent(context,NotificationTimeChanger.class);
        intentChangeTime.putExtra("moment", secondButtonText);
        PendingIntent pIntentChangeTime = PendingIntent.getBroadcast(context,1,intentChangeTime,PendingIntent.FLAG_UPDATE_CURRENT);
//https://stackoverflow.com/questions/41312669/android-calling-methods-from-notification-action-button
        String channelId = prioritary ? CHANNEL_HIGH_ID : CHANNEL_DEFAULT_ID;
        Resources res = getApplicationContext().getResources();
        Notification notification = new NotificationCompat.Builder( getApplicationContext(), channelId )
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.countdown)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.countdown))
                .setContentIntent(pActivityIntent)
                .addAction(0, "Actualiser", pIntentUpdate)
                .addAction(0, secondButtonText, pIntentChangeTime)
                .setAutoCancel(true)
                .setOngoing(true)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.glacier))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .build();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Late.getAppContext());
        Boolean disableNotification = sharedPref.getBoolean("disableNotification", false);
        if(!disableNotification) notifManager.notify(id, notification);
    }

    public void removeNotification(){
        notifManager.deleteNotificationChannel(CHANNEL_DEFAULT_ID);
    }
}
