package red.chronos.late;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            notificationHelper.webRequest(context, "aprem");
        }
    }
}
