package com.android.privatemessenger.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.android.privatemessenger.R;
import com.android.privatemessenger.data.api.RetrofitAPI;
import com.android.privatemessenger.data.model.User;
import com.android.privatemessenger.data.model.UserPhone;
import com.android.privatemessenger.sharedprefs.SharedPrefUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallActivity extends BaseNavDrawerActivity {

    @BindView(R.id.apactv_phone)
    public AutoCompleteTextView APACTVPhone;

    @BindView(R.id.fab_call)
    public FloatingActionButton FABCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        ButterKnife.bind(this);
        getDrawer();
        loadContacts();
    }

    private void loadContacts() {
        RetrofitAPI.getInstance().getRegisteredUsers(SharedPrefUtils.getInstance(this).getUser().getToken()).enqueue(new Callback<List<User>>() {
            private void onError() {
                Toast.makeText(CallActivity.this, getResources().getString(R.string.toast_loading_error), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response == null || response.body() == null) {
                    onError();
                    return;
                }

                List<UserPhone> phones = new ArrayList<UserPhone>();
                for (User user : response.body()) {
                    phones.add(new UserPhone(user.getPhone()));
                }

                APACTVPhone.setAdapter(new ArrayAdapter<UserPhone>(CallActivity.this, R.layout.search_item, phones));
                APACTVPhone.setThreshold(1);
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                onError();
            }
        });
    }

    @OnClick(R.id.fab_call)
    public void call() {
        String phone = APACTVPhone.getText().toString();
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        startActivity(intent);
    }
}
