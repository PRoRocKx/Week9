package com.example.evgeniy.week9;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindViews({R.id.seekBar, R.id.radioButton, R.id.radioButton2, R.id.radioButton3, R.id.checkBox, R.id.toggleButton, R.id.switch1})
    List<View> viewList;

    UndoManager undoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        undoManager = new UndoManager(this, viewList);
    }

    @Override
    public void onBackPressed() {
        if (!undoManager.undo()) {
            super.onBackPressed();
        }
    }
}
