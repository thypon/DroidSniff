/*
 * AboutActivity.java is the about screen for DroidSniff 
 * Copyright (C) 2012 Evozi <email@evozi.com>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.evozi.droidsniff.controller.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.evozi.droidsniff.R;
import com.sun.swing.internal.plaf.metal.resources.metal_sv;
import in.uncod.android.bypass.Bypass;

import java.util.HashMap;
import java.util.Map;

public final class AboutActivity extends SherlockActivity implements
		ActionBar.TabListener {
	@InjectView(R.id.text) TextView mSelected;
    private final Bypass bypass = new Bypass();

    private final Map<CharSequence, Integer> tabContent = new HashMap<CharSequence, Integer>() {{
        put(getString(R.string.about), R.string.about_content);
        put(getString(R.string.faq), R.string.faq_content);
        put(getString(R.string.guide), R.string.guide_content);
    }};

    private final Map<CharSequence, CharSequence> cacheMarkdown = new HashMap<CharSequence, CharSequence>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
        ButterKnife.inject(this);

		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(true);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        for (CharSequence name : tabContent.keySet()) {
            ActionBar.Tab tab = getSupportActionBar().newTab();
            tab.setText(name);
            tab.setTabListener(this);
            getSupportActionBar().addTab(tab);
        }
	}

	public void onTabReselected(Tab tab, FragmentTransaction transaction) {
	}

	public void onTabSelected(Tab tab, FragmentTransaction transaction) {
        CharSequence content = cacheMarkdown.get(tab.getText());

        if (content == null) {
            content = bypass.markdownToSpannable(getString(tabContent.get(tab.getText())));
            cacheMarkdown.put(tab.getText(), content);
        }

        mSelected.setText(content);
	}

	public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
		case android.R.id.home:
			this.finish();
			return true;
		}
		return false;
	}
}
