package ru.krivocraft.kbmp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

public class SettingsAdapter extends ArrayAdapter<String> {

    private List<String> objects;
    private Activity context;
    private SettingsManager manager;

    SettingsAdapter(@NonNull Activity context, List<String> objects) {
        super(context, R.layout.settings_item_toggle, objects);
        this.context = context;
        this.objects = objects;
        this.manager = new SettingsManager(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_toggle, null);
            if (position == objects.indexOf(Constants.KEY_THEME)) {
                TextView textView = convertView.findViewById(R.id.settings_text);
                textView.setText("Light theme (Beta)");
                Switch s = convertView.findViewById(R.id.settings_switch);
                initSwitch(s, Constants.KEY_THEME, false);
            } else if (position == objects.indexOf(Constants.KEY_SORT_BY_ARTIST)) {
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                initSwitch(s, Constants.KEY_SORT_BY_ARTIST, false);
                textView.setText("Automatically sort by artist");
            } else if (position == objects.indexOf(Constants.KEY_SORT_BY_TAG)) {
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                initSwitch(s, Constants.KEY_SORT_BY_TAG, false);
                textView.setText("Automatically sort by tag");
            } else if (position == objects.indexOf(Constants.KEY_RECOGNIZE_NAMES)) {
                Switch s = convertView.findViewById(R.id.settings_switch);
                TextView textView = convertView.findViewById(R.id.settings_text);
                initSwitch(s, Constants.KEY_RECOGNIZE_NAMES, true);
                textView.setText("Try to parse track names for tracks with no metadata");
            } else if (position == objects.indexOf(Constants.KEY_CLEAR_CACHE)) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.settings_item_button, null);
                Button b = convertView.findViewById(R.id.settings_button);
                TextView textView = convertView.findViewById(R.id.settings_text);
                b.setText("CLEAR");
                textView.setText("Clear track lists cache");
                b.setOnClickListener(v -> {
                    Utils.clearCache(getContext().getSharedPreferences(Constants.STORAGE_TRACK_LISTS, Context.MODE_PRIVATE));
                    Toast.makeText(getContext(), "Cache cleared", Toast.LENGTH_LONG).show();
                });
            }
        }
        return convertView;
    }

    private void initSwitch(Switch s, String key, boolean defaultValue) {
        boolean option = manager.getOption(key, defaultValue);
        s.setChecked(option);
        s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (option) {
                manager.putOption(key, false);
            } else {
                manager.putOption(key, true);
            }
            if (key.equals(Constants.KEY_THEME)){
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 228, new Intent(context, MainActivity.class), PendingIntent.FLAG_ONE_SHOT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME, 100, pendingIntent);
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.exit(0);
                    }).start();
                }
            }
        });
    }

}
