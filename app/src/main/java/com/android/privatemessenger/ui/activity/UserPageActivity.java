package com.android.privatemessenger.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.utils.IntentKeys;
import com.r0adkll.slidr.Slidr;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserPageActivity extends BaseNavDrawerActivity {
    private final String TAG = UserPageActivity.this.getClass().getSimpleName();

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
        setContentView(R.layout.activity_user_page);
        ButterKnife.bind(this);
        Slidr.attach(this);

        if (!getUserFromIntent()) {
            return;
        }

        getDrawer();
        removeToolbarTitle();
        initLayout();

        setCollapsingToolbarTitle(user.getName() == null ? "" : user.getName());
    }

    private void initLayout() {
        collapsingToolbar.setCollapsedTitleTextColor(colorWhite);
        collapsingToolbar.setExpandedTitleColor(colorWhite);

        TVPhone.setText(user.getPhone() == null ? getResources().getString(R.string.no_info) : user.getPhone());
        TVEmail.setText(user.getEmail() == null ? getResources().getString(R.string.no_info) : user.getEmail());
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

    @OnClick(R.id.fab_call)
    public void call() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + user.getPhone()));
        startActivity(intent);
    }
}
