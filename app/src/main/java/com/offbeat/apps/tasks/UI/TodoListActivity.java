package com.offbeat.apps.tasks.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.offbeat.apps.tasks.Adapter.TodoAdapter;
import com.offbeat.apps.tasks.Data.TaskList;
import com.offbeat.apps.tasks.Data.Todo;
import com.offbeat.apps.tasks.R;
import com.offbeat.apps.tasks.UI.Dialog.NewListFragmentDialog;
import com.offbeat.apps.tasks.UI.Dialog.NewTodoFragmentDialog;
import com.offbeat.apps.tasks.Utils.Utils;

import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nullable;

public class TodoListActivity extends AppCompatActivity {

    private static final String TAG = TodoListActivity.class.getSimpleName();

    public TextView mTitleText, mNumberTasks;
    private SimpleDraweeView mTaskImage;
    public Intent mIntent;
    public FloatingActionButton mFloatingActionButton;
    private Utils mUtils;
    private ArrayList<Todo> mTodoArrayList = new ArrayList<>();
    private TodoAdapter mTodoAdapter;
    private FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    public int completed = 0, incomplete = 0;
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= 21) {
            Utils.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        Utils.setLightStatusBar(this, getWindow().getDecorView(), this);
        Utils.setTypeFace(this);
        setContentView(R.layout.activity_todo_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageView mBackButton = toolbar.getRootView().findViewById(R.id.back);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mRecyclerView = findViewById(R.id.rv_tasks);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(TodoListActivity.this, LinearLayoutManager.VERTICAL, false));

        mTitleText = findViewById(R.id.tv_task_list_title);
        mTaskImage = findViewById(R.id.task_list_image);
        mNumberTasks = findViewById(R.id.tv_task_list_count);
        mFloatingActionButton = findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewTodoFragmentDialog dialogFragment = new NewTodoFragmentDialog();
                android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                dialogFragment.show(ft, "dialog");
            }
        });
        mIntent = getIntent();
        if (mIntent.hasExtra(getString(R.string.title))) {
            String mListTitle = mIntent.getStringExtra(getString(R.string.title));
            mTitleText.setText(mListTitle);
            completed = mIntent.getIntExtra(getString(R.string.completed_tasks),0);
            incomplete = mIntent.getIntExtra(getString(R.string.incomplete_tasks),0);
            mNumberTasks.setText(incomplete + " Tasks");
            getTasks(mIntent.getStringExtra(getString(R.string.id)));

            int icon = mIntent.getIntExtra(getString(R.string.icon), 1);
            switch (icon) {
                case 1:
                    mTaskImage.setImageResource(R.drawable.icon_1);
                    break;
                case 2:
                    mTaskImage.setImageResource(R.drawable.icon_2);
                    break;
                case 3:
                    mTaskImage.setImageResource(R.drawable.icon_3);
                    break;
                case 4:
                    mTaskImage.setImageResource(R.drawable.icon_4);
                    break;
                case 5:
                    mTaskImage.setImageResource(R.drawable.icon_5);
                    break;
                case 6:
                    mTaskImage.setImageResource(R.drawable.icon_6);
                    break;
                case 7:
                    mTaskImage.setImageResource(R.drawable.icon_7);
                    break;
                case 8:
                    mTaskImage.setImageResource(R.drawable.icon_8);
                    break;
                default:
                    mTaskImage.setImageResource(R.drawable.icon_1);
                    break;
            }
        }
    }

    void getTasks(final String id) {
        mTodoArrayList.clear();
        mTodoAdapter = new TodoAdapter(TodoListActivity.this, mTodoArrayList,id, completed, incomplete);
        CollectionReference todoReference = mFirebaseFirestore.collection(getString(R.string.tasks_collection))
                .document(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid())
                .collection(getString(R.string.tasks_collection)).document(id).collection(getString(R.string.tasks_collection));
        todoReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        if (documentChange.getType().equals(DocumentChange.Type.ADDED)) {
                            DocumentSnapshot documentSnapshot = documentChange.getDocument();
                            Todo taskList = documentSnapshot.toObject(Todo.class);
                            mTodoArrayList.add(taskList);
                            mTodoAdapter.notifyItemInserted(mTodoArrayList.size() - 1);
                        } else if (documentChange.getType().equals(DocumentChange.Type.MODIFIED)) {
                            DocumentSnapshot documentSnapshot2 = documentChange.getDocument();
                            Todo taskList = documentSnapshot2.toObject(Todo.class);
                            if (mTodoArrayList.size() > 0) {
                                String title = taskList.title;
                                int index = 0;
                                for (int i = 0; i < mTodoArrayList.size(); i++) {
                                    if ((mTodoArrayList.get(i) != null) && title.equals(mTodoArrayList.get(i).title)) {
                                        index = i;
                                        break;
                                    }
                                }
                                mTodoArrayList.set(index, taskList);
                                mTodoAdapter.notifyItemChanged(index, taskList);
                            }
                        }
                    }
                    mRecyclerView.setAdapter(mTodoAdapter);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishAfterTransition();
    }
}
