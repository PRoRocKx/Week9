package com.example.evgeniy.week9;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.SharedPreferences.*;

class UndoManager {

    private static final String TEMP_PREFERENCES = "tempPref";
    private static final String SCOPE_PREFERENCES = "scopePref";
    private static final long SAVE_INTERVAL = 5000;

    private final List<View> viewList;

    private final Context context;

    private SharedPreferences tempSettings;
    private SharedPreferences scopeSettings;

    private final UndoTimer undoTimer;

    UndoManager(Context context, List<View> viewList) {
        this.context = context;
        this.viewList = viewList;
        initSharedPref();
        undoTimer = new UndoTimer(this);
        addListeners();
    }

    private void initSharedPref() {
        tempSettings = context.getSharedPreferences(TEMP_PREFERENCES, Context.MODE_PRIVATE);
        tempSettings
                .edit()
                .clear()
                .apply();
        scopeSettings = context.getSharedPreferences(SCOPE_PREFERENCES, Context.MODE_PRIVATE);
        scopeSettings
                .edit()
                .clear()
                .apply();
    }

    private void addListeners() {
        for (View view : viewList) {

            if (view instanceof SeekBar) {
                addSeekBarListeners((SeekBar) view);
            }
            if (view instanceof CompoundButton) {
                addCompoundButtonListener((CompoundButton) view);
            }
        }
    }

    private void addCompoundButtonListener(CompoundButton compoundButton) {
        compoundButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton instanceof RadioButton && b) {
                    return;
                }
                saveChanges(compoundButton.getId(), String.valueOf(!b));
            }
        });
    }

    private void addSeekBarListeners(SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                progress = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveChanges(seekBar.getId(), String.valueOf(progress));
            }
        });
    }

    private void saveChanges(int id, String val) {
        Editor editor = tempSettings.edit();
        editor.putString(getNextSettingsIndex(tempSettings), "id:" + id + ":val:" + val);
        editor.apply();
        undoTimer.setTimer(SAVE_INTERVAL);
    }

    private String getLastSettingsIndex(SharedPreferences settings) {
        return String.valueOf(getSettingsIndex(settings));
    }

    private String getNextSettingsIndex(SharedPreferences settings) {
        return String.valueOf(getSettingsIndex(settings) + 1);
    }

    private int getSettingsIndex(SharedPreferences settings) {
        int i = -1;
        for (String key : settings.getAll().keySet()) {
            i = Math.max(i, Integer.valueOf(key));
        }
        return i;
    }

    private String getSettingsValue(SharedPreferences settings) {
        int i = -1;
        for (String key : settings.getAll().keySet()) {
            i = Math.max(i, Integer.valueOf(key));
        }
        return settings.getString(String.valueOf(i), "-1");
    }

    private Set<String> getSettingsSetValue(SharedPreferences settings) {
        int i = -1;
        for (String key : settings.getAll().keySet()) {
            i = Math.max(i, Integer.valueOf(key));
        }
        return settings.getStringSet(String.valueOf(i), null);
    }

    private void undoOne(String s) {
        String[] strings = s.split(":");
        int id = Integer.valueOf(strings[1]);
        String val = (strings[3]);
        undo(id, val);
    }

    private boolean undoTemp() {
        String setting = getSettingsValue(tempSettings);
        if (setting.equals("-1")) {
            return false;
        }
        undoOne(setting);
        removeLastSettingsValue(tempSettings);
        return true;
    }

    private boolean undoScope() {
        Set<String> setting = getSettingsSetValue(scopeSettings);
        if (setting == null) {
            return false;
        }
        for (String s : setting) {
            undoOne(s);
        }
        removeLastSettingsValue(scopeSettings);
        return true;
    }


    public boolean undo() {
        if (undoTemp()) {
            undoTimer.setTimer(SAVE_INTERVAL);
            return true;
        }
        if (undoScope()) {
            undoTimer.setTimer(SAVE_INTERVAL);
            return true;
        }
        undoTimer.stopTimer();
        return false;
    }

    private void removeLastSettingsValue(SharedPreferences settings) {
        Editor editor = settings.edit();
        editor.remove(getLastSettingsIndex(settings));
        editor.apply();
    }

    private void undo(int id, String val) {
        View view = findViewById(id);
        if (view != null) {
            if (view instanceof SeekBar) {
                setSeekBarVal((SeekBar) view, Integer.valueOf(val));
            }
            if (view instanceof CompoundButton) {
                setCompoundButtonVal((CompoundButton) view, Boolean.valueOf(val));
            }
        }
    }


    private void setCompoundButtonVal(CompoundButton compoundButton, boolean val) {
        if (compoundButton instanceof RadioButton) {
            removeListenerForRadioButton();
        } else {
            compoundButton.setOnCheckedChangeListener(null);
        }
        compoundButton.setChecked(val);

        if (compoundButton instanceof RadioButton) {
            addListenerForRadioButton();
        } else {
            addCompoundButtonListener(compoundButton);
        }
    }

    private void removeListenerForRadioButton() {
        for (View view : viewList) {
            if (view instanceof RadioButton) {
                ((CompoundButton) view).setOnCheckedChangeListener(null);
            }
        }
    }

    private void addListenerForRadioButton() {
        for (View view : viewList) {
            if (view instanceof RadioButton) {
                addCompoundButtonListener((CompoundButton) view);
            }
        }
    }

    private void setSeekBarVal(SeekBar seekBar, int val) {
        seekBar.setProgress(val);
    }

    private View findViewById(int id) {
        for (View view : viewList) {
            if (view.getId() == id) {
                return view;
            }
        }
        return null;
    }


    private void removeEqualsId(String id, Set<String> settings) {
        for (String sett : settings) {
            if (sett.split(":")[1].equals(id)) {
                settings.remove(sett);
                return;
            }
        }
    }

    public void convertTempToScope() {
        Set<String> settings = new HashSet<>();
        while (!tempSettings.getAll().isEmpty()) {
            String setting = getSettingsValue(tempSettings);
            removeEqualsId(setting.split(":")[1], settings);
            settings.add(setting);
            removeLastSettingsValue(tempSettings);

        }
        if (!settings.isEmpty()) {
            scopeSettings
                    .edit()
                    .putStringSet(getNextSettingsIndex(scopeSettings), settings)
                    .apply();
        }
    }


}
