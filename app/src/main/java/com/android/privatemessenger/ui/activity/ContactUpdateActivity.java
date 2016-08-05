package com.android.privatemessenger.ui.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.Contact;
import com.android.privatemessenger.data.model.ErrorResponse;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;
import com.android.privatemessenger.ui.dialog.ExitDialog;
import com.android.privatemessenger.utils.IntentKeys;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactUpdateActivity extends AppCompatActivity {
    private final String TAG = ContactUpdateActivity.this.getClass().getSimpleName();

    @BindView(R.id.collapsing_toolbar)
    public CollapsingToolbarLayout collapsingToolbar;

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @BindView(R.id.et_name)
    public EditText ETName;

    @BindView(R.id.et_phone)
    public EditText ETPhone;

    @BindColor(R.color.white)
    public int colorWhite;

    private Contact contact;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_add);
        ButterKnife.bind(this);

        if (getIntent() == null || getIntent().getSerializableExtra(IntentKeys.OBJECT_CONTACT) == null) {
            return;
        }

        contact = (Contact) getIntent().getSerializableExtra(IntentKeys.OBJECT_CONTACT);

        createToolbar();
        initLayout();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ExitDialog.create(ContactUpdateActivity.this).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        ExitDialog.create(ContactUpdateActivity.this).show();
    }

    @OnClick(R.id.fab_done)
    public void done() {
        if (!isInputValid()) {
            return;
        }

        final ProgressDialog progressDialog = com.android.privatemessenger.ui.dialog.ProgressDialog.create(this);
        progressDialog.show();

        RetrofitAPI.getInstance().updateContact(
                contact.getId(),
                SharedPrefUtils.getInstance(this).getUser().getToken(),
                ETName.getText().toString(),
                ETPhone.getText().toString()
        ).enqueue(new Callback<ErrorResponse>() {
            @Override
            public void onResponse(Call<ErrorResponse> call, Response<ErrorResponse> response) {
                if (response == null || response.body() == null || response.body().isError()) {
                    Toast.makeText(ContactUpdateActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog.cancel();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(Call<ErrorResponse> call, Throwable t) {
                progressDialog.cancel();
                Toast.makeText(ContactUpdateActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure()-> ", t);
            }
        });
    }

    private boolean isInputValid() {
        String name = ETName.getText().toString();
        String phone = ETPhone.getText().toString();

        if (name.equals("") || phone.equals(""))
            return false;
        else
            return true;
    }

    private void createToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initLayout() {
        collapsingToolbar.setCollapsedTitleTextColor(colorWhite);
        collapsingToolbar.setExpandedTitleColor(colorWhite);
        collapsingToolbar.setTitle(contact.getName());

        ETName.setText(contact.getName());
        ETPhone.setText(contact.getPhone());

        ETName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                collapsingToolbar.setTitle(String.valueOf(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
