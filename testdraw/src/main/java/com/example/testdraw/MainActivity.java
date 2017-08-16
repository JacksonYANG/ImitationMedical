package com.example.testdraw;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements DrawSwich.OnChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DrawSwich drawSwich = (DrawSwich) findViewById(R.id.switch1);
        drawSwich.setChecked(false);
        drawSwich.setOnChangedListener(this);
    }

    @Override
    public void OnChanged(DrawSwich drawSwitch, boolean checkState) {
        Log.d("asf","asfdasfsffasdfasfasfsfsadf"+checkState);
    }
}
