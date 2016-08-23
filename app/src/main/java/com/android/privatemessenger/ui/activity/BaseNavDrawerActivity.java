package com.android.privatemessenger.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.android.privatemessenger.broadcast.IntentFilters;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.data.model.Message;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.data.realm.RealmDB;
import com.android.privatemessenger.data.realm.model.UnreadMessage;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.utils.IntentKeys;
import com.digits.sdk.android.Digits;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseNavDrawerActivity extends AppCompatActivity {

    private static final String TAG = "BaseNavigationDrawer";

    protected DrawerBuilder drawerBuilder = null;
    protected Drawer drawer = null;
    private InputMethodManager inputMethodManager = null;
    private boolean wasInputActive = false;

    private PrimaryDrawerItem chatList;
    private BroadcastReceiver messageReceiver;

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
        } else if (basename.equals(MyProfileActivity.class.getSimpleName())) {
            drawer.setSelection(DrawerItems.MyProfileActivity.ordinal());
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
        registerReceivers();

        // Creating DrawerBuilder
        createDrawerBuilder(createToolbar());

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

    private Toolbar createToolbar() {
        // Инициализируем Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        return toolbar;
    }

    private Toolbar createToolbarWithBackArrow() {
        // Инициализируем Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        return toolbar;
    }

    private void registerReceivers() {
        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshChatsBadge();
            }
        };

        registerReceiver(messageReceiver, new IntentFilter(IntentFilters.NEW_MESSAGE));
    }

    protected void refreshChatsBadge() {
        drawer.updateBadge(DrawerItems.ChatListActivity.ordinal(), new StringHolder(getUnreadDialogCount()));
    }

    private String getUnreadDialogCount() {
        Realm realm = RealmDB.getDefault(this);
        RealmResults<UnreadMessage> results = realm.where(UnreadMessage.class).findAll();

        return results.size() == 0 ? "" : String.valueOf(results.size());
    }

    public void createDrawerBuilder(Toolbar toolbar) {
        final PrimaryDrawerItem myProfile = new PrimaryDrawerItem()
                .withName(getResources().getString(R.string.drawer_my_profile))
                .withIcon(GoogleMaterial.Icon.gmd_person)
                .withIdentifier(DrawerItems.MyProfileActivity.ordinal());

        chatList = new PrimaryDrawerItem()
                .withName(getResources().getString(R.string.drawer_chat_list))
                .withBadge(String.valueOf(getUnreadDialogCount()))
                .withIcon(GoogleMaterial.Icon.gmd_chat)
                .withIdentifier(DrawerItems.ChatListActivity.ordinal());

        final PrimaryDrawerItem contacts = new PrimaryDrawerItem()
                .withName(getResources().getString(R.string.drawer_contacts))
                .withIcon(GoogleMaterial.Icon.gmd_supervisor_account)
                .withIdentifier(DrawerItems.ContactListActivity.ordinal());

        final PrimaryDrawerItem call = new PrimaryDrawerItem()
                .withName(getResources().getString(R.string.drawer_call))
                .withIcon(GoogleMaterial.Icon.gmd_dialpad)
                .withIdentifier(DrawerItems.CallActivity.ordinal());

        final PrimaryDrawerItem debug = new PrimaryDrawerItem()
                .withName(getResources().getString(R.string.drawer_debug))
                .withIcon(GoogleMaterial.Icon.gmd_developer_mode)
                .withIdentifier(DrawerItems.DebugActivity.ordinal());

        final PrimaryDrawerItem exit = new PrimaryDrawerItem()
                .withName(getResources().getString(R.string.drawer_logout))
                .withIcon(GoogleMaterial.Icon.gmd_exit_to_app)
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
                        myProfile,
                        chatList,
                        contacts,
                        call,
                        //debug,
                        new DividerDrawerItem(),
                        exit
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
                                case MyProfileActivity: {
                                    if (currentClass.equals(MyProfileActivity.class.getSimpleName())) {
                                        break;
                                    } else {
                                        finish();

                                        startActivity(new Intent(BaseNavDrawerActivity.this, MyProfileActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                .putExtra(IntentKeys.OBJECT_USER, SharedPrefUtils.getInstance(BaseNavDrawerActivity.this).getUser()));
                                        break;
                                    }
                                }
                                case ChatListActivity: {
                                    if (currentClass.equals(ChatListActivity.class.getSimpleName())) {
                                        break;
                                    } else {
                                        finish();

                                        startActivity(new Intent(BaseNavDrawerActivity.this, ChatListActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        break;
                                    }
                                }
                                case ContactListActivity: {
                                    if (currentClass.equals(ContactListActivity.class.getSimpleName())) {
                                        break;
                                    } else {
                                        finish();

                                        startActivity(new Intent(BaseNavDrawerActivity.this, ContactListActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        break;
                                    }
                                }
                                case CallActivity: {
                                    if (currentClass.equals(CallActivity.class.getSimpleName())) {
                                        break;
                                    } else {
                                        finish();

                                        startActivity(new Intent(BaseNavDrawerActivity.this, CallActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        break;
                                    }
                                }
                                case DebugActivity: {
                                    if (currentClass.equals(DebugActivity.class.getSimpleName())) {
                                        break;
                                    } else {
                                        finish();

                                        startActivity(new Intent(BaseNavDrawerActivity.this, DebugActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                        break;
                                    }
                                }
                                case Exit: {
                                    Log.d(TAG, "onItemClick()-> exit clicked");
                                    finish();
                                    startActivity(new Intent(BaseNavDrawerActivity.this, ChatListActivity.class)
                                            .putExtra(IntentKeys.EXIT, true)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);

        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }

    @Override
    protected void onResume() {
        getCurrentSelection();
        refreshChatsBadge();
        super.onResume();
    }

    public enum DrawerItems {
        MyProfileActivity,
        ChatListActivity,
        ContactListActivity,
        CallActivity,
        DebugActivity,
        Exit
    }
}
