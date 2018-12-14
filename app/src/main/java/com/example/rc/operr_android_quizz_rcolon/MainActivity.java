package com.example.rc.operr_android_quizz_rcolon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    //ignores warning about line "if(BuildConfig.BUILD_TYPE.equals("qa")){" always having constant result
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView buildBanner = findViewById(R.id.buildBanner);
        /*
        note: for the purpose of this exercise, all build variants use a debug signing config so that the app can build and run fine, and because we don't have any signing keys to use for release/prod.
         */
        switch (BuildConfig.BUILD_TYPE){
            case "qa":
                buildBanner.setText(R.string.qa_env);
                break;
            case "production":
                buildBanner.setText(R.string.prod_env);
                break;
            case "debug":
                buildBanner.setText(R.string.env_debug);
                break;
            default:
                buildBanner.setText(R.string.env_release);
                break;
        }
        startStickyService();
    }

    /**
     * Starts background sticky service that would trigger the 5-minute persistent notification.
     */
    private void startStickyService() {
        Intent intent = new Intent(this, CustomStickyService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startService(intent); //starts app Service if it has not been started yet
    }
}
