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

package ru.krivocraft.tortoise.fragments.explorer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import ru.krivocraft.tortoise.R;
import ru.krivocraft.tortoise.contexts.TrackListEditorActivity;
import ru.krivocraft.tortoise.core.storage.SettingsStorageManager;
import ru.krivocraft.tortoise.core.track.TrackList;
import ru.krivocraft.tortoise.fragments.BaseFragment;
import ru.krivocraft.tortoise.fragments.tracklist.TrackListsGridFragment;

import java.util.ArrayList;

public class ExplorerFragment extends BaseFragment {

    private ExplorerPagerAdapter adapter;

    private Explorer explorer;

    private TrackListsGridFragment.OnItemClickListener listener;
    private FloatingActionButton button;
    private int tintColor;
    private TabLayout tabLayout;
    private ViewPager pager;

    public static ExplorerFragment newInstance() {
        return new ExplorerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_explorer, container, false);
    }

    public void changeColor(int colorResource) {
        if (button != null) {
            button.setBackgroundTintList(getContext().getResources().getColorStateList(colorResource));
        }
        if (tabLayout != null) {
            tabLayout.setSelectedTabIndicatorColor(getContext().getResources().getColor(colorResource));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        button = view.findViewById(R.id.add_track_list_button);
        button.setOnClickListener(v -> showCreationDialog(v.getContext()));

        tabLayout = view.findViewById(R.id.explorer_tabs);

        if (getSettingsManager().getOption(SettingsStorageManager.KEY_THEME, false)) {
            tabLayout.setTabTextColors(R.color.black, R.color.black);
        }

        changeColor(tintColor);

        pager = view.findViewById(R.id.explorer_pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                getSettingsManager().putOption("endOnSorted", position > 0);
                if (position > 0) {
                    button.hide();
                } else {
                    button.show();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //Do nothing
            }
        });

        Context activity = view.getContext();
        if (activity != null && explorer != null) {
            this.adapter = new ExplorerPagerAdapter(getChildFragmentManager(), listener, explorer.getCustomLists(), explorer.getSortedLists(), activity);
            pager.setAdapter(adapter);
            pager.setCurrentItem(getSettingsManager().getOption("endOnSorted", false) ? 1 : 0);
            tabLayout.setupWithViewPager(pager);
            explorer.compileTrackLists();
        }
    }

    @Override
    public void invalidate() {
        Activity activity = getActivity();
        if (activity != null) {
            System.out.println("invalidating");
            this.adapter = new ExplorerPagerAdapter(getChildFragmentManager(), listener, explorer.getCustomLists(), explorer.getSortedLists(), activity);
            pager.setAdapter(adapter);
            pager.setCurrentItem(getSettingsManager().getOption("endOnSorted", false) ? 1 : 0);
        }
    }

    private void showCreationDialog(Context context) {
        Intent intent = new Intent(context, TrackListEditorActivity.class);
        intent.putExtra(TrackList.EXTRA_TRACK_LIST, new TrackList("", new ArrayList<>(), TrackList.TRACK_LIST_CUSTOM).toJson());
        intent.putExtra(TrackListEditorActivity.EXTRA_CREATION, true);
        context.startActivity(intent);
    }

    public void setListener(TrackListsGridFragment.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setExplorer(Explorer explorer) {
        this.explorer = explorer;
    }

    public void setTintColor(int tintColor) {
        this.tintColor = tintColor;
    }
}
