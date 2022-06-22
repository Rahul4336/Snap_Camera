package s.nap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Splash extends AppCompatActivity {

    SharedPreferences sharedpreferences;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedpreferences = getSharedPreferences("mypref", Context.MODE_PRIVATE);

        if (!checkpermission()) {
            requestpermission();
        }
        else {
            startMainActivity();
        }
    }

    private void requestpermission() {
        ActivityCompat.requestPermissions(Splash.this,new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE,ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION,RECORD_AUDIO,MODIFY_AUDIO_SETTINGS,CAMERA},1);
    }

    private boolean checkpermission() {
        int result0 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result2= ContextCompat.checkSelfPermission(getApplicationContext(),ACCESS_FINE_LOCATION);
        int result3= ContextCompat.checkSelfPermission(getApplicationContext(),ACCESS_COARSE_LOCATION);
        int result4 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result5 = ContextCompat.checkSelfPermission(getApplicationContext(), MODIFY_AUDIO_SETTINGS);
        int result6= ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);

        return result0== PackageManager.PERMISSION_GRANTED && result1== PackageManager.PERMISSION_GRANTED
                && result2== PackageManager.PERMISSION_GRANTED&& result3== PackageManager.PERMISSION_GRANTED
                && result4== PackageManager.PERMISSION_GRANTED&& result5== PackageManager.PERMISSION_GRANTED
               && result6== PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                boolean p0 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean p1 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                boolean p2 = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                boolean p3 = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                boolean p4 = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                boolean p5 = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                boolean p6 = grantResults[6] == PackageManager.PERMISSION_GRANTED;

                if (p0 && p1 && p2 && p3 && p4 && p5 && p6) {
                   startMainActivity();
                }
            } else {
                Toast.makeText(this, "Permission necessary", Toast.LENGTH_SHORT).show();
            }
        }
    }
    void startMainActivity(){
        int SPLASH_TIME_OUT = 1500;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sharedpreferences.contains("verify")) {
                    String verify = sharedpreferences.getString("verify", "");
                    assert verify != null;
                    if (verify.equals("true")) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                    else if (verify.equals("false")) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
                else
                {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }
}