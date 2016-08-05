package com.android.privatemessenger.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.utils.IntentKeys;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyProfileUpdateActivity extends AppCompatActivity {

    private final String TAG = MyProfileUpdateActivity.this.getClass().getSimpleName();

    @BindView(R.id.collapsing_toolbar)
    public CollapsingToolbarLayout collapsingToolbar;

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @BindView(R.id.et_name)
    public EditText ETName;

    @BindView(R.id.tv_phone)
    public TextView TVPhone;

    @BindView(R.id.et_email)
    public EditText ETEmail;

    @BindColor(R.color.white)
    public int colorWhite;

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
                showExitDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    private void showExitDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage(getResources().getString(R.string.dialog_before_exit_message))
                .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
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
                        .setTextColor(ContextCompat.getColor(MyProfileUpdateActivity.this, R.color.colorPrimary));

                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(ContextCompat.getColor(MyProfileUpdateActivity.this, R.color.md_red_500));
            }
        });

        alertDialog.show();
    }

    @OnClick(R.id.fab_done)
    public void done() {
        if (!isInputValid()) {
            return;
        }

        final ProgressDialog progressDialog = com.android.privatemessenger.ui.dialog.ProgressDialog.create(this);
        progressDialog.show();

        RetrofitAPI.getInstance().updateMyInfo(
                SharedPrefUtils.getInstance(this).getUser().getToken(),
                ETName.getText().toString(),
                ETEmail.getText().toString()
        ).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response == null || response.body() == null) {
                    Toast.makeText(MyProfileUpdateActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.cancel();
                setResult(RESULT_OK, new Intent()
                        .putExtra(IntentKeys.OBJECT_USER, response.body()));
                finish();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressDialog.cancel();
                Toast.makeText(MyProfileUpdateActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure()-> ", t);
            }
        });
    }

    private boolean isInputValid() {
        String name = ETName.getText().toString();
        String email = ETEmail.getText().toString();

        if (name.equals("") || email.equals(""))
            return false;
        else
            return true;
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
        collapsingToolbar.setCollapsedTitleTextColor(colorWhite);
        collapsingToolbar.setExpandedTitleColor(colorWhite);

        TVPhone.setText(user.getPhone() == null || TextUtils.isEmpty(user.getPhone()) ?
                getResources().getString(R.string.no_info) : user.getPhone());

        ETEmail.setText(user.getEmail() == null || TextUtils.isEmpty(user.getEmail()) ?
                "" : user.getEmail());

        ETName.setText(user.getEmail() == null || TextUtils.isEmpty(user.getEmail()) ?
                "" : user.getEmail());

        collapsingToolbar.setTitle(user.getName() == null || TextUtils.isEmpty(user.getName()) ?
                "" : user.getName());

        ETName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                collapsingToolbar.setTitle(TextUtils.isEmpty(String.valueOf(s)) ?
                        user.getName() == null || TextUtils.isEmpty(user.getName()) ? "" : user.getName() : String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}