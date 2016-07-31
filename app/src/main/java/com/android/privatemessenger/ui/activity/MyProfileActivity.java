package com.android.privatemessenger.ui.activity;

import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.utils.IntentKeys;
import com.android.privatemessenger.utils.RequestCodes;
import com.r0adkll.slidr.Slidr;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyProfileActivity extends BaseNavDrawerActivity {

    private final String TAG = MyProfileActivity.this.getClass().getSimpleName();

    @BindView(R.id.collapsing_toolbar)
    public CollapsingToolbarLayout collapsingToolbar;

    @BindView(R.id.tv_phone)
    public TextView TVPhone;

    @BindView(R.id.tv_email)
    public TextView TVEmail;

    @BindColor(R.color.white)
    public int colorWhite;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        ButterKnife.bind(this);

        if (!getUserFromIntent()) {
            return;
        }

        getDrawer();
        removeToolbarTitle();
        initLayout();
        updateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RequestCodes.ACTIVITY_MY_PROFILE_EDIT:
                if (data != null && data.getSerializableExtra(IntentKeys.OBJECT_USER) != null) {
                    user = (User) data.getSerializableExtra(IntentKeys.OBJECT_USER);
                    updateUI();
                }
                break;
            default:
                break;
        }
    }

    private void updateUI() {
        TVPhone.setText(user.getPhone() == null ? getResources().getString(R.string.no_info) : user.getPhone());
        TVEmail.setText(user.getEmail() == null ? getResources().getString(R.string.no_info) : user.getEmail());

        setCollapsingToolbarTitle(user.getName() == null ? "" : user.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_profile:
                editMyProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.fab_edit)
    public void editMyProfile() {
        startActivityForResult(new Intent(MyProfileActivity.this, MyProfileEditActivity.class)
                .putExtra(IntentKeys.OBJECT_USER, user), RequestCodes.ACTIVITY_MY_PROFILE_EDIT);
    }

    private void initLayout() {
        collapsingToolbar.setCollapsedTitleTextColor(colorWhite);
        collapsingToolbar.setExpandedTitleColor(colorWhite);
    }

    private void setCollapsingToolbarTitle(String title) {
        collapsingToolbar.setTitle(title);
    }

    private boolean getUserFromIntent() {
        if (getIntent() == null || getIntent().getSerializableExtra(IntentKeys.OBJECT_USER) == null) {
            return false;
        }

        user = (User) getIntent().getSerializableExtra(IntentKeys.OBJECT_USER);
        return true;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);

        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);

        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }
}
