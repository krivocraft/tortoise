/*
 * Copyright (c) 2019 Nikifor Fedorov
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     SPDX-License-Identifier: Apache-2.0
 *     Contributors:
 * 	    Nikifor Fedorov - whole development
 */

package ru.krivocraft.tortoise.core.tracklist;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import ru.krivocraft.tortoise.core.explorer.TrackListsStorageManager;
import ru.krivocraft.tortoise.core.model.Track;
import ru.krivocraft.tortoise.core.model.TrackList;
import ru.krivocraft.tortoise.core.model.TrackReference;
import ru.krivocraft.tortoise.core.settings.SettingsStorageManager;
import ru.krivocraft.tortoise.sorting.GetFromDiskTask;
import ru.krivocraft.tortoise.thumbnail.Colors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TracksProvider {

    public static final String ACTION_UPDATE_STORAGE = "action_update_storage";

    private final Context context;
    private ContentResolver contentResolver;

    private TrackListsStorageManager trackListsStorageManager;
    private TracksStorageManager tracksStorageManager;

    private boolean recognize;

    public TracksProvider(Context context) {
        this.contentResolver = context.getContentResolver();
        this.trackListsStorageManager = new TrackListsStorageManager(context, TrackListsStorageManager.FILTER_ALL);
        this.tracksStorageManager = new TracksStorageManager(context);

        SettingsStorageManager settingsManager = new SettingsStorageManager(context);
        this.recognize = settingsManager.getOption(SettingsStorageManager.KEY_RECOGNIZE_NAMES, true);

        this.context = context;
    }

    public void search() {
        requestRescan();
        new GetFromDiskTask(contentResolver, recognize, this::manageStorage, new Colors(context)).execute();
    }

    private void requestRescan() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File("file://" + Environment.getExternalStorageDirectory());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    private void manageStorage(List<Track> tracks) {
        List<TrackReference> allTracks = new ArrayList<>();

        removeNonExistingTracksFromStorage(tracksStorageManager.getTrackStorage(), tracks);
        addNewTracks(allTracks, tracksStorageManager.getTrackStorage(), tracks);

        writeRootTrackList(allTracks);

        notifyTracksStorageChanged();
    }

    private void notifyTracksStorageChanged() {
        context.sendBroadcast(new Intent(ACTION_UPDATE_STORAGE));
    }

    private void writeRootTrackList(List<TrackReference> allTracks) {
        TrackList trackList = new TrackList(TrackListsStorageManager.STORAGE_TRACKS_DISPLAY_NAME, allTracks, TrackList.TRACK_LIST_CUSTOM);
        trackListsStorageManager.updateRootTrackList(trackList);
    }

    private void addNewTracks(List<TrackReference> allTracks, List<Track> existingTracks, List<Track> readTracks) {
        for (Track track : readTracks) {
            TrackReference reference = new TrackReference(track);

            if (!existingTracks.contains(track)) {
                tracksStorageManager.writeTrack(track);
                allTracks.add(reference);
            }
        }
    }

    private void removeNonExistingTracksFromStorage(List<Track> existingTracks, List<Track> readTracks) {
        List<TrackReference> removedReferences = new ArrayList<>();
        for (Track track : existingTracks) {
            TrackReference reference = new TrackReference(track);

            if (!readTracks.contains(track)) {
                tracksStorageManager.removeTrack(track);
                removedReferences.add(reference);
            }
        }
        updateTrackLists(removedReferences);
    }

    private void updateTrackLists(List<TrackReference> removedTracks) {
        List<TrackList> trackLists = trackListsStorageManager.readAllTrackLists();
        for (TrackList trackList : trackLists) {
            removeNonExistingTracksFromTrackList(removedTracks, trackList);
            removeTrackListIfEmpty(trackList);
        }
    }

    private void removeTrackListIfEmpty(TrackList trackList) {
        if (trackList.size() == 0 && !trackList.getDisplayName().equals(TrackListsStorageManager.STORAGE_TRACKS_DISPLAY_NAME)) {
            trackListsStorageManager.removeTrackList(trackList);
        }
    }

    private void removeNonExistingTracksFromTrackList(List<TrackReference> removedTracks, TrackList trackList) {
        List<TrackReference> referencesToRemove = new ArrayList<>();
        for (TrackReference reference : trackList.getTrackReferences()) {
            if (removedTracks.contains(reference)) {
                referencesToRemove.add(reference);
            }
        }
        trackList.removeAll(referencesToRemove);
        trackListsStorageManager.removeTracks(trackList, referencesToRemove);
    }

}

