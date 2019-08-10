package ru.krivocraft.kbmp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

public class TrackListFragment extends Fragment {

    private TrackList trackList;
    private TrackAdapter adapter;
    private boolean showControls;

    private List<Track> tracks;

    public TrackListFragment() {
    }

    static TrackListFragment newInstance(TrackList trackList, boolean showControls) {
        TrackListFragment trackListFragment = new TrackListFragment();
        trackListFragment.init(showControls, trackList);
        return trackListFragment;
    }

    void init(boolean showControls, TrackList trackList) {
        this.showControls = showControls;
        this.trackList = trackList;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);

        EditText searchFrame = rootView.findViewById(R.id.search_edit_text);
        ImageButton buttonShuffle = rootView.findViewById(R.id.shuffle);
        ListView listView = rootView.findViewById(R.id.fragment_track_list);
        ProgressBar progressBar = rootView.findViewById(R.id.track_list_progress);

        final Context context = getContext();
        if (context != null) {
            String[] paths = trackList.getTracks().toArray(new String[0]);

            LoadDataTask task = new LoadDataTask();
            task.setContentResolver(context.getContentResolver());
            task.setCallback(tracks -> {
                this.tracks = tracks;
                this.adapter = new TrackAdapter(this.tracks, context);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    Intent serviceIntent = new Intent(Constants.Actions.ACTION_PLAY_FROM_LIST);
                    serviceIntent.putExtra(Constants.Extras.EXTRA_PATH, ((Track) parent.getItemAtPosition(position)).getPath());
                    serviceIntent.putExtra(Constants.Extras.EXTRA_TRACK_LIST, (new ArrayList<>(CollectionUtils.collect(this.tracks, Track::getPath))).toArray(new String[0]));
                    view.getContext().sendBroadcast(serviceIntent);

                    Intent interfaceIntent = new Intent(Constants.Actions.ACTION_SHOW_PLAYER);
                    view.getContext().sendBroadcast(interfaceIntent);
                });
                progressBar.setVisibility(View.GONE);
                searchFrame.setVisibility(View.VISIBLE);
                buttonShuffle.setVisibility(View.VISIBLE);
                listView.setVisibility(View.VISIBLE);
            });
            task.execute(paths);
        }

        if (showControls) {
            searchFrame.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (context != null) {
                        List<Track> trackListSearched = Utils.search(s, trackList.getTracks(), context.getContentResolver());
                        listView.setAdapter(new TrackAdapter(trackListSearched, context));
                        if (s.length() == 0) {
                            listView.setAdapter(adapter);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            buttonShuffle.setOnClickListener(v -> {
                Collections.shuffle(this.tracks);
                adapter.notifyDataSetChanged();
            });
        } else {
            searchFrame.setVisibility(View.INVISIBLE);
            buttonShuffle.setVisibility(View.INVISIBLE);
        }


        return rootView;
    }
}
