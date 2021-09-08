package red.chronos;

import android.app.Application;
import android.content.Context;

public class Late extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        Late.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Late.context;
    }
}
