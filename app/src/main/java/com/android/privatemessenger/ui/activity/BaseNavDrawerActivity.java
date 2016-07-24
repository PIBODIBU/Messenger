package com.android.privatemessenger.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.digits.sdk.android.Digits;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import butterknife.BindString;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

public class BaseNavDrawerActivity extends AppCompatActivity {

    private static final String TAG = "BaseNavigationDrawer";

    protected DrawerBuilder drawerBuilder = null;
    protected Drawer drawer = null;
    private InputMethodManager inputMethodManager = null;
    private boolean wasInputActive = false;

    public BaseNavDrawerActivity() {
    }

    public void getCurrentSelection() {
        String basename = BaseNavDrawerActivity.this.getClass().getSimpleName();

        if (drawer == null) {
            return;
        }

        if (basename.equals(ChatListActivity.class.getSimpleName())) {
            drawer.setSelection(DrawerItems.ChatListActivity.ordinal());
        } else {
            drawer.setSelection(-1);
        }
    }

    /**
     * Init base NavigationDrawer
     */
    public void getDrawer() {
        // Creating DrawerBuilder
        setUpDrawerBuilder();

        // Creating Drawer from DrawerBuilder
        setUpDrawer();

        // Getting current Drawer selection
        getCurrentSelection();
    }

    /**
     * Check if user is logged in. If true -> add new DrawerItem to NavigationDrawer (My page)
     * Building NavigationDrawer from Drawer.Builder
     */
    private void setUpDrawer() {
        if (drawerBuilder != null) {
            Log.d(TAG, "setUpDrawer() -> Building Drawer from Drawer.Builder");

            drawer = drawerBuilder.build(); // Building Drawer
            drawer.getRecyclerView().setVerticalScrollBarEnabled(false); // remove ScrollBar from RecyclerView
        } else {
            Log.e(TAG, "setUpDrawer() -> DrawerBuilder is null");
        }
    }

    public void setUpDrawerBuilder() {
        // Инициализируем Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        final PrimaryDrawerItem chatList = new PrimaryDrawerItem()
                .withName(getResources().getString(R.string.drawer_chat_list))
                .withIcon(GoogleMaterial.Icon.gmd_reorder)
                .withIdentifier(DrawerItems.ChatListActivity.ordinal());

        final PrimaryDrawerItem contacts = new PrimaryDrawerItem()
                .withName(getResources().getString(R.string.drawer_contacts))
                .withIcon(GoogleMaterial.Icon.gmd_supervisor_account)
                .withIdentifier(DrawerItems.ContactsActivity.ordinal());

        final PrimaryDrawerItem call = new PrimaryDrawerItem()
                .withName(getResources().getString(R.string.drawer_call))
                .withIcon(GoogleMaterial.Icon.gmd_call)
                .withIdentifier(DrawerItems.CallActivity.ordinal());

        String phone = Digits.getSessionManager().getActiveSession() == null ? "" : Digits.getSessionManager().getActiveSession().getPhoneNumber();
        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withProfileImagesVisible(false)
                .withHeaderBackground(R.color.colorPrimary)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName("John Smith")
                                .withEmail(phone))
                .build();

        /**
         * Implementing DrawerBuilder
         */
        drawerBuilder = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withAccountHeader(accountHeader)
                .withHeaderDivider(false)
                .withSliderBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
                .addDrawerItems(
                        chatList,
                        contacts,
                        call
                )
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Скрываем клавиатуру при открытии Navigation Drawer
                        if (inputMethodManager.isAcceptingText()) {
                            if (getCurrentFocus() != null) {
                                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                            }
                            wasInputActive = true;
                        } else {
                            wasInputActive = false;
                        }
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Показать клавиатуру при закрытии Navigation Drawer, если она была открыта
                        if (wasInputActive)
                            inputMethodManager.showSoftInput(getCurrentFocus(), 0);
                    }

                    @Override
                    public void onDrawerSlide(View view, float v) {

                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    // Обработка клика
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        try {
                            String currentClass = BaseNavDrawerActivity.this.getClass().getSimpleName();
                            DrawerItems drawerItems = DrawerItems.values()[(int) drawerItem.getIdentifier()];

                            Log.d(TAG, "onItemClick()-> currentClass: " + currentClass);

                            switch (drawerItems) {
                                case ChatListActivity: {
                                    if (currentClass.equals(ChatListActivity.class.getSimpleName())) {
                                        break;
                                    } else {
                                        startActivity(new Intent(BaseNavDrawerActivity.this, ChatListActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        finish();
                                        break;
                                    }
                                }
                                case ContactsActivity: {
                                    if (currentClass.equals(ChatListActivity.class.getSimpleName())) {
                                        break;
                                    } else {
                                        startActivity(new Intent(BaseNavDrawerActivity.this, ChatActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        finish();
                                        break;
                                    }
                                }
                                default: {
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return false;
                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown():\nkeyCode: " + Integer.toString(keyCode) + "\nkeyEvent: " + event);
        try {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU: {
                    if (drawer.isDrawerOpen())
                        drawer.closeDrawer();
                    else
                        drawer.openDrawer();
                    break;
                }
                case KeyEvent.KEYCODE_BACK: {
                    if (drawer.isDrawerOpen()) { // Check if Drawer is opened
                        drawer.closeDrawer();
                    } else {
                        super.onBackPressed();
                    }

                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String TWITTER_KEY = getResources().getString(R.string.twitter_key);
        String TWITTER_SECRET = getResources().getString(R.string.twitter_secrey);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits());

        // Check if user is logged in
        if (Digits.getSessionManager().getActiveSession() == null) {
            startActivity(new Intent(BaseNavDrawerActivity.this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }

        inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    protected void onPostResume() {
        getCurrentSelection();
        super.onPostResume();
    }

    public enum DrawerItems {
        ChatListActivity,
        ContactsActivity,
        CallActivity
    }
}
