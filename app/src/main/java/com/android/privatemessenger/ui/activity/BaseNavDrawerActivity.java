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
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.digits.sdk.android.Digits;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        Log.d(TAG, "getCurrentSelection()-> " +
                "\nbaseName: " + basename +
                "\nChatListActivity.class.getSimpleName(): " + ChatListActivity.class.getSimpleName());

        if (basename.equals(ChatListActivity.class.getSimpleName())) {
            drawer.setSelection(DrawerItems.ChatListActivity.ordinal());
        } else if (basename.equals(ContactListActivity.class.getSimpleName())) {
            drawer.setSelection(DrawerItems.ContactListActivity.ordinal());
        } else if (basename.equals(CallActivity.class.getSimpleName())) {
            drawer.setSelection(DrawerItems.CallActivity.ordinal());
        } else {
            drawer.setSelection(-1);
        }
    }

    protected void removeToolbarTitle() {
        getSupportActionBar().setTitle("");
    }

    /**
     * Init base NavigationDrawer
     */
    public void getDrawer() {
        // Creating DrawerBuilder
        createDrawerBuilder();

        // Creating Drawer from DrawerBuilder
        createDrawer();

        // Getting current Drawer selection
        getCurrentSelection();
    }

    /**
     * Check if user is logged in. If true -> add new DrawerItem to NavigationDrawer (My page)
     * Building NavigationDrawer from Drawer.Builder
     */
    private void createDrawer() {
        if (drawerBuilder != null) {
            Log.d(TAG, "createDrawer() -> Building Drawer from Drawer.Builder");

            drawer = drawerBuilder.build(); // Building Drawer
            drawer.getRecyclerView().setVerticalScrollBarEnabled(false); // remove ScrollBar from RecyclerView
        } else {
            Log.e(TAG, "createDrawer() -> DrawerBuilder is null");
        }
    }

    public void createDrawerBuilder() {
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
                .withIdentifier(DrawerItems.ContactListActivity.ordinal());

        final PrimaryDrawerItem call = new PrimaryDrawerItem()
                .withName(getResources().getString(R.string.drawer_call))
                .withIcon(GoogleMaterial.Icon.gmd_dialpad)
                .withIdentifier(DrawerItems.CallActivity.ordinal());

        final PrimaryDrawerItem logout = new PrimaryDrawerItem()
                .withName(getResources().getString(R.string.drawer_logout))
                .withIcon(GoogleMaterial.Icon.gmd_settings_power)
                .withIdentifier(DrawerItems.Exit.ordinal());

        String phone = Digits.getSessionManager().getActiveSession() == null ? "" : Digits.getSessionManager().getActiveSession().getPhoneNumber();
        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withProfileImagesVisible(false)
                .withHeaderBackground(R.color.colorPrimary)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(SharedPrefUtils.getInstance(this).getUser().getName())
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
                        call,
                        new DividerDrawerItem(),
                        logout
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
                                case ContactListActivity: {
                                    if (currentClass.equals(ContactListActivity.class.getSimpleName())) {
                                        break;
                                    } else {
                                        startActivity(new Intent(BaseNavDrawerActivity.this, ContactListActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        finish();
                                        break;
                                    }
                                }
                                case CallActivity: {
                                    if (currentClass.equals(CallActivity.class.getSimpleName())) {
                                        break;
                                    } else {
                                        startActivity(new Intent(BaseNavDrawerActivity.this, CallActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        finish();
                                        break;
                                    }
                                }
                                case Exit: {
                                    Log.d(TAG, "onItemClick()-> exit clicked");
                                    RetrofitAPI
                                            .getInstance()
                                            .logout(SharedPrefUtils.getInstance(BaseNavDrawerActivity.this).getUser().getToken())
                                            .enqueue(new Callback<ErrorResponse>() {
                                                @Override
                                                public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                                                    if (response == null || response.body() == null || response.body().isError()) {
                                                        Toast.makeText(BaseNavDrawerActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }

                                                    Digits.getSessionManager().clearActiveSession();
                                                    SharedPrefUtils.getInstance(BaseNavDrawerActivity.this).clear();
                                                    startActivity(new Intent(BaseNavDrawerActivity.this, LoginActivity.class)
                                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                    finish();
                                                }

                                                @Override
                                                public void onFailure(Call<ErrorResponse> call, Throwable t) {
                                                    Log.e(TAG, "onFailure()-> ", t);
                                                    Toast.makeText(BaseNavDrawerActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                                                }
                                            });

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
        String TWITTER_SECRET = getResources().getString(R.string.twitter_secret);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new TwitterCore(authConfig), new Digits());

        // Check if user is logged in
        if (Digits.getSessionManager().getActiveSession() == null || SharedPrefUtils.getInstance(this).getUser().getToken().equals("")) {
            startActivity(new Intent(BaseNavDrawerActivity.this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }

        inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);

        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    protected void onResume() {
        getCurrentSelection();
        super.onResume();
    }

    public enum DrawerItems {
        ChatListActivity,
        ContactListActivity,
        CallActivity,
        Exit
    }
}
