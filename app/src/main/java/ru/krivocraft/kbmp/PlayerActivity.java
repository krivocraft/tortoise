package ru.krivocraft.kbmp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import ru.krivocraft.kbmp.constants.Constants;

public class PlayerActivity extends BaseActivity {

    private final static int INDEX_FRAGMENT_PLAYER = 0;
    private final static int INDEX_FRAGMENT_PLAYLIST = 1;
    private ViewPager pager;
    private MediaBrowserCompat mediaBrowser;

    private TrackList trackList;
    private int track;
    private LargePlayerFragment largePlayerFragment;
    private TrackListFragment trackListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        initMediaBrowser();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Actions.ACTION_RESULT_TRACK_LIST);
        registerReceiver(receiver, filter);
    }

    private void initMediaBrowser() {
        mediaBrowser = new MediaBrowserCompat(
                PlayerActivity.this,
                new ComponentName(PlayerActivity.this, MediaPlaybackService.class),
                new MediaBrowserCompat.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();
                            MediaControllerCompat controller = new MediaControllerCompat(PlayerActivity.this, token);
                            MediaControllerCompat.setMediaController(PlayerActivity.this, controller);
                            sendBroadcast(new Intent(Constants.Actions.ACTION_REQUEST_TRACK_LIST));
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConnectionFailed() {
                        Log.e("TAG", "onConnectionFailed");
                        Toast.makeText(PlayerActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionSuspended() {
                        Log.e("TAG", "onConnectionSuspended");
                    }
                },
                null);
        mediaBrowser.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (largePlayerFragment != null) {
            largePlayerFragment.requestPosition(this);
        }
    }

    private void createTrackListFragment() {
        trackListFragment = TrackListFragment.newInstance(trackList, false, PlayerActivity.this);
    }

    private void createPlayerFragment() {
        largePlayerFragment = LargePlayerFragment.newInstance(PlayerActivity.this, trackList);
    }

    private void initPager() {
        pager = findViewById(R.id.pager_p);
        createPlayerFragment();
        createTrackListFragment();
        pager.setAdapter(new PagerAdapter());
        pager.invalidate();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PlayerActivity.this.trackList = TrackList.fromJson(intent.getStringExtra(Constants.Extras.EXTRA_TRACK_LIST));
            PlayerActivity.this.track = intent.getIntExtra(Constants.Extras.EXTRA_CURSOR, 0);
            initPager();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowser.disconnect();
        unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        if (pager.getCurrentItem() == INDEX_FRAGMENT_PLAYLIST) {
            pager.setCurrentItem(INDEX_FRAGMENT_PLAYER);
        } else {
            super.onBackPressed();
        }
    }

    private class PagerAdapter extends FragmentPagerAdapter {


        PagerAdapter() {
            super(PlayerActivity.this.getSupportFragmentManager());
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {
            if (i == INDEX_FRAGMENT_PLAYER) {
                return getPlayerPage();
            } else if (i == INDEX_FRAGMENT_PLAYLIST) {
                return getTrackListPage();
            }
            return null;
        }

        @NonNull
        private LargePlayerFragment getPlayerPage() {
            return largePlayerFragment;
        }

        private TrackListFragment getTrackListPage() {
            return trackListFragment;
        }

    }
}
