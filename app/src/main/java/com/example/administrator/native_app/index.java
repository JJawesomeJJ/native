package com.example.administrator.native_app;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.administrator.native_app.activity.BaseActivity;
import com.example.administrator.native_app.activity.OnPermissionCallbackListener;

public class index extends BaseActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
    }
    @Override
    public void init() {

    }

    @Override
    public void onRequestPermission(String[] permissions, OnPermissionCallbackListener listener) {
        super.onRequestPermission(permissions, listener);
    }

    @Override
    public void installData() {

    }

    @Override
    public void onClick(View v) {

    }
}
