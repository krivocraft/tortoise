package ru.krivocraft.kbmp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.krivocraft.kbmp.constants.Constants;

public class TrackListFragment extends Fragment {

    private TracksAdapter tracksAdapter;
    private boolean showControls;

    private TrackList trackList;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TrackList trackList = TrackList.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK_LIST));
            if (trackList != null) {
                TrackListFragment.this.trackList = trackList;
            }
            processPaths(context);
        }
    };
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView progressText;

    public TrackListFragment() {
    }

    private MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            tracksAdapter.notifyDataSetChanged();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            tracksAdapter.notifyDataSetChanged();
        }
    };

    static TrackListFragment newInstance(TrackList trackList, boolean showControls, Activity context) {
        TrackListFragment trackListFragment = new TrackListFragment();
        trackListFragment.init(showControls, trackList, context);
        return trackListFragment;
    }

    private void init(boolean showControls, TrackList trackList, Activity context) {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(context);
        mediaController.registerCallback(callback);
        this.showControls = showControls;
        this.trackList = trackList;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tracklist, container, false);

        EditText searchFrame = rootView.findViewById(R.id.search_edit_text);
        ImageButton buttonShuffle = rootView.findViewById(R.id.shuffle);
        recyclerView = rootView.findViewById(R.id.fragment_track_recycler_view);
        progressBar = rootView.findViewById(R.id.track_list_progress);
        progressText = rootView.findViewById(R.id.obtaining_text_track_list);

        final Context context = getContext();
        if (context != null) {
            processPaths(context);

            if (showControls) {
                searchFrame.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        List<TrackReference> trackListSearched = Utils.search(context, s, TrackListFragment.this.trackList.getTrackReferences());
                        recyclerView.setAdapter(new TracksAdapter(new TrackList("found", trackListSearched, Constants.TRACK_LIST_CUSTOM), context, showControls));
                        if (s.length() == 0) {
                            recyclerView.setAdapter(tracksAdapter);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                buttonShuffle.setOnClickListener(v -> tracksAdapter.shuffle());
                searchFrame.setVisibility(View.VISIBLE);
                buttonShuffle.setVisibility(View.VISIBLE);
            } else {
                IntentFilter filter = new IntentFilter();
                filter.addAction(Constants.Actions.ACTION_UPDATE_TRACK_LIST);
                context.registerReceiver(receiver, filter);
            }
        }


        return rootView;
    }

    private void processPaths(Context context) {
        this.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        this.tracksAdapter = new TracksAdapter(trackList, context, showControls);
        this.recyclerView.setAdapter(tracksAdapter);

        progressBar.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);

        if (!showControls) {
            ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(tracksAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(recyclerView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
