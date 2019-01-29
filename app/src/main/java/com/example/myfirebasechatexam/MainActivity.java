package com.example.myfirebasechatexam;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    // Firebase Auth
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    // Google Auth
    private GoogleApiClient mGoogleApiClient;

    // Firebase Database
    private DatabaseReference mFirebaseDbRef;

    // 사용자 정보
    private String mUsername;
    private String mPhotoUrl;

    private EditText mEditTextMsg;
    private RecyclerView mRecyclerChat;
    private FirebaseRecyclerAdapter mFirebaseAdapter;

    public static final String MSG_CHILD = "messages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // GoogleApiClient 초기화
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Firebase Auth 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // 로그인 체크
        if (mFirebaseUser == null){
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null)
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }

        // Firebase DB 초기화
        mFirebaseDbRef = FirebaseDatabase.getInstance().getReference();

        mEditTextMsg = findViewById(R.id.etMsg);
        mRecyclerChat = findViewById(R.id.recyclerChat);
        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatMessage chatMessage = new ChatMessage(mEditTextMsg.getText().toString(), mUsername, mPhotoUrl, null);
                mFirebaseDbRef.child(MSG_CHILD).push().setValue(chatMessage);
                mEditTextMsg.setText("");
                mRecyclerChat.smoothScrollToPosition(
                        mRecyclerChat.getAdapter().getItemCount() - 1);
            }
        });

        // FirebaseAdapter options 객체 생성
        Query query = mFirebaseDbRef.child(MSG_CHILD);
        FirebaseRecyclerOptions<ChatMessage> options = new FirebaseRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .build();

        // FirebaseAdapter 등록
        mFirebaseAdapter = new RecyclerAdapter(options);
        mRecyclerChat.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerChat.setAdapter(mFirebaseAdapter);

        // 키보드 올라왔을때 스크롤
        mRecyclerChat.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerChat.smoothScrollToPosition(
                                    mRecyclerChat.getAdapter().getItemCount());
                        }
                    }, 100);
                }
            }
        });

        // 새로운 채팅 있을때 스크롤
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerChat.getLayoutManager();
                int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerChat.scrollToPosition(positionStart);
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "연결 에러", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuSignOut:
                // Firebase Auth, Google Auth 둘다 로그아웃 해야함.
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = "";
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAdapter.stopListening();
    }
}
