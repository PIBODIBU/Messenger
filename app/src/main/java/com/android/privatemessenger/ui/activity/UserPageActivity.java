package com.android.privatemessenger.ui.activity;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.utils.IntentKeys;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserPageActivity extends BaseNavDrawerActivity {
    private final String TAG = UserPageActivity.this.getClass().getSimpleName();

    @BindView(R.id.collapsing_toolbar)
    public CollapsingToolbarLayout collapsingToolbar;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);
        ButterKnife.bind(this);

        if (!getUserFromIntent()) {
            return;
        }

        getDrawer();
        removeToolbarTitle();

        setCollapsingToolbarTitle(user.getName() == null ? "" : user.getName());
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
}
