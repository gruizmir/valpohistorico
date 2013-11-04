package com.opensour.ValpoHistorico;

import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.maps.GoogleMap;

public class ValpoHistorico extends FragmentActivity implements	ActionBar.TabListener, OnLocationClickListener {
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	GoogleMap map;
	private Bundle extras;
	protected MapaFragment mFragment;
	protected RecommendFragment rFragment;
	protected InfoFragment iFragment;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.valpo_historico);
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
				.setText(mSectionsPagerAdapter.getPageTitle(i))
				.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.valpo_historico, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch(position){
			case 0:
				RecommendFragment recommendFragment = new RecommendFragment();
				rFragment = recommendFragment;
				return recommendFragment;
			case 1:
				MapaFragment mapaFragment = new MapaFragment();
				mapaFragment.setOnLocationClickListener(ValpoHistorico.this);
				mFragment = mapaFragment;
				return mapaFragment;
			case 2:
				InfoFragment infoFragment = new InfoFragment();
				iFragment = infoFragment;
				return infoFragment;
			default:
				MapaFragment fg = new MapaFragment();
				fg.setOnLocationClickListener(ValpoHistorico.this);
				return fg;
			}
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	@Override
	public void onLocationClick(Bundle data) {
		mViewPager.setCurrentItem(2);
		extras = data;
		if(extras!=null){
			iFragment.setTitleText(extras.getString("name", "no existe"));
			iFragment.setBodyText(extras.getString("body", ""));
		}
		else{
			Log.e("vacio", "extras es null");
		}
	}

}
