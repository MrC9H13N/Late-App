package red.chronos.late;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationTimeChanger extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.removeNotification();
        if(intent.getStringExtra("moment").equals("Matin")){
            notificationHelper.webRequest(context, "matin");
        } else if(intent.getStringExtra("moment").equals("Après-midi")) {
            notificationHelper.webRequest(context, "aprem");
        }
    }
}
