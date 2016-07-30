package com.android.privatemessenger.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.utils.IntentKeys;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyProfileEditActivity extends AppCompatActivity {

    private final String TAG = MyProfileEditActivity.this.getClass().getSimpleName();

    @BindView(R.id.collapsing_toolbar)
    public CollapsingToolbarLayout collapsingToolbar;

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @BindView(R.id.et_name)
    public EditText ETName;

    @BindView(R.id.tv_phone)
    public TextView TVPhone;

    @BindView(R.id.tv_email)
    public EditText ETEmail;

    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile_edit);
        ButterKnife.bind(this);

        if (!getDataFromIntent()) {
            return;
        }

        createToolbar();
        initLayout();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void finish() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.dialog_before_exit_message))
                .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MyProfileEditActivity.super.finish();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(ContextCompat.getColor(MyProfileEditActivity.this, R.color.colorPrimary));

                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(MyProfileEditActivity.this, R.color.md_red_500));
            }
        });

        alertDialog.show();
    }

    @OnClick(R.id.fab_done)
    public void done() {
        user.setName(ETName.getText().toString());
        user.setEmail(ETEmail.getText().toString());

        setResult(RESULT_OK, new Intent()
                .putExtra(IntentKeys.OBJECT_USER, user));
    }

    private void createToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private boolean getDataFromIntent() {
        if (getIntent() == null || getIntent().getSerializableExtra(IntentKeys.OBJECT_USER) == null)
            return false;

        user = (User) getIntent().getSerializableExtra(IntentKeys.OBJECT_USER);
        return true;
    }

    private void initLayout() {
        ETName.setText(user.getName());
        TVPhone.setText(user.getPhone());
        ETEmail.setText(user.getEmail());
    }
}
