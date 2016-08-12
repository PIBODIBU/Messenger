package com.android.privatemessenger.ui.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.privatemessenger.R;
import com.android.privatemessenger.ui.fragment.ContactsAllFragment;
import com.android.privatemessenger.ui.fragment.ContactsMyFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactListActivity extends BaseNavDrawerActivity {

    public final String TAG = ContactListActivity.this.getClass().getSimpleName();

    @BindView(R.id.view_pager)
    public ViewPager viewPager;

    @BindView(R.id.tab_layout)
    public TabLayout tabLayout;

    @BindView(R.id.fab)
    public FloatingActionButton fab;

    private MenuItem cancelMenuItem;

    private ContactsAllFragment contactsAllFragment;
    private ContactsMyFragment contactsMyFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        ButterKnife.bind(this);
        getDrawer();

        setupViewPager();
        setupLayout();
    }

    private void setupLayout() {
        fab.setOnClickListener(contactsAllFragment.getFabClickListener());
    }

    private void setupViewPager() {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        contactsAllFragment = new ContactsAllFragment();
        contactsMyFragment = new ContactsMyFragment();

        adapter.addFragment(contactsAllFragment, getResources().getString(R.string.tab_contacts_all));
        adapter.addFragment(contactsMyFragment, getResources().getString(R.string.tab_contacts_my));

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (adapter.getItem(position) instanceof ContactsAllFragment) {
                    fab.setOnClickListener(contactsAllFragment.getFabClickListener());
                } else if (adapter.getItem(position) instanceof ContactsMyFragment) {
                    deactivateSelectionMode();
                    fab.setOnClickListener(contactsMyFragment.getFabClickListener());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_list, menu);

        cancelMenuItem = menu.findItem(R.id.item_cancel);
        contactsAllFragment.setCancelMenuItem(cancelMenuItem);
        cancelMenuItem.setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_cancel:
                deactivateSelectionMode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deactivateSelectionMode() {
        try {
            fab.setImageResource(R.drawable.ic_add_white_24dp);
            contactsAllFragment.adapter.setSelectionModeActivated(false);
            cancelMenuItem.setVisible(false);
        } catch (Exception ex) {
            Log.e(TAG, "deactivateSelectionMode()-> ", ex);
        }
    }
}