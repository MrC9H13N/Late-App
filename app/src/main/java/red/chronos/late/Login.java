package red.chronos.late;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    public static void checkUser(Editable username, Editable password, Context context, Button buttonConnect, ProgressBar progressBar, SharedPreferences sharedPref){
        final String url = "https://www.late.chronos.red/checkUser";
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.i("YOUHOU",response.toString());
                        if(response.toString().equals("error")){
                            Toast toast = Toast.makeText(context, "Erreur de la connexion", Toast.LENGTH_SHORT);
                            toast.show();
                            buttonConnect.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        } else {
                            Toast toast = Toast.makeText(context, "Connexion réussie", Toast.LENGTH_SHORT);
                            toast.show();
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("username", String.valueOf(username));
                            editor.putString("password", String.valueOf(password));
                            editor.putString("userInfo", response);
                            editor.putBoolean("disableNotification", false);
                            editor.apply();
                            Intent myIntent = new Intent(context, Settings.class);
                            context.startActivity(myIntent);
                        }
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
                params.put("username", String.valueOf(username));
                params.put("password", String.valueOf(password));

                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText passwordText = (EditText) findViewById(R.id.editTextPassword);
        EditText editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        Button buttonConnect = (Button) findViewById(R.id.buttonConnect);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideKeyboard(Login.this);
                buttonConnect.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                checkUser(editTextUsername.getText(), passwordText.getText(), Login.this, buttonConnect, progressBar, sharedPref);
                Log.i("Debug", "FINI");
            }
        });

        passwordText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    buttonConnect.performClick();
                    return true;
                }
                return false;
            }
        });
    }
}