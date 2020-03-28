package com.offbeat.apps.tasks.UI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.offbeat.apps.tasks.R;
import com.offbeat.apps.tasks.Utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 1001;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ProgressDialog progressDialog;
    Utils mUtils;
    Map<String, Object> myTasks = new HashMap<>();
    Map<String, Object> personalTasks = new HashMap<>();
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setLightStatusBar(this, getWindow().getDecorView(), this);
        mUtils.setTypeFace(this);
        setContentView(R.layout.activity_login);
        progressDialog = new ProgressDialog(this);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.login_token))
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();


                            CollectionReference tasksReference = mFirebaseFirestore.collection(getString(R.string.tasks_collection));
                            final DocumentReference userReference = tasksReference.document(Objects.requireNonNull(mAuth.getUid()));

                            userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (!task.getResult().exists()) {
                                            Map<String, Object> userMap = new HashMap<>();
                                            assert user != null;
                                            userMap.put(getString(R.string.user_id), user.getUid());
                                            userMap.put(getString(R.string.user_email), Objects.requireNonNull(user.getEmail()));
                                            userMap.put(getString(R.string.user_fullname), Objects.requireNonNull(user.getDisplayName()));
                                            userMap.put(getString(R.string.user_profile_picture), user.getPhotoUrl().toString());
                                            myTasks.put(getString(R.string.title), getString(R.string.default_task_mytasks));
                                            myTasks.put(getString(R.string.start_color), getString(R.string.start_orange_grad));
                                            myTasks.put(getString(R.string.end_color), getString(R.string.end_orange_grad));
                                            myTasks.put(getString(R.string.completed_tasks), 0);
                                            myTasks.put(getString(R.string.incomplete_tasks), 0);
                                            myTasks.put(getString(R.string.timestamp), Timestamp.now());
                                            myTasks.put(getString(R.string.icon), 1);
                                            personalTasks.put(getString(R.string.title), getString(R.string.default_task_personal));
                                            personalTasks.put(getString(R.string.start_color), getString(R.string.start_purple_grad));
                                            personalTasks.put(getString(R.string.end_color), getString(R.string.end_purple_grad));
                                            personalTasks.put(getString(R.string.completed_tasks), 0);
                                            personalTasks.put(getString(R.string.incomplete_tasks), 0);
                                            personalTasks.put(getString(R.string.icon), 7);
                                            personalTasks.put(getString(R.string.timestamp), Timestamp.now());
                                            userReference.set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    DocumentReference personaltask = userReference.collection(getString(R.string.tasks_collection)).document();
                                                    personalTasks.put(getString(R.string.id), personaltask.getId());
                                                    personaltask.set(personalTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            DocumentReference mytasks = userReference.collection(getString(R.string.tasks_collection)).document();
                                                            myTasks.put(getString(R.string.id), mytasks.getId());
                                                            mytasks.set(myTasks).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    updateUI();
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            });


                                        } else {
                                            updateUI();
                                        }
                                    }
                                }
                            });
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            updateUI();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            updateUI();
        }
    }

    private void updateUI() {
        progressDialog.dismiss();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
