package com.offbeat.apps.tasks.UI;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.ItemTransformation;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.gtomato.android.ui.transformer.CoverFlowViewTransformer;
import com.gtomato.android.ui.widget.CarouselView;
import com.offbeat.apps.tasks.Adapter.TaskListAdapter;
import com.offbeat.apps.tasks.Data.TaskList;
import com.offbeat.apps.tasks.R;
import com.offbeat.apps.tasks.UI.Dialog.NewListFragmentDialog;
import com.offbeat.apps.tasks.Utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import javax.annotation.Nullable;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CoordinatorLayout mCoordinatorLayout;
    private SimpleDraweeView mAvatar;
    public FrameLayout mNewListButton;
    private TextView mUserTitle, mIntro, mDate;
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
    private ArrayList<TaskList> mTaskListArrayList = new ArrayList<>();
    private CarouselView mCarouselView;
    public TaskListAdapter mTaskListAdapter;
    private CoverFlowViewTransformer mCoverTransformer;
    private int mTotalTasks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= 21) {
            Utils.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        Utils.setTypeFace(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCoordinatorLayout = findViewById(R.id.main_coordinator);
        mUserTitle = findViewById(R.id.tv_name_title);
        mAvatar = findViewById(R.id.main_avatar);
        mCarouselView = findViewById(R.id.rv_main);
        mTaskListAdapter = new TaskListAdapter(this, mTaskListArrayList);
        mIntro = findViewById(R.id.tv_main_intro);
        mDate = findViewById(R.id.tv_main_date);
        mNewListButton = findViewById(R.id.new_list_button);
        mDate.setText(getDate());
        mCoverTransformer = new CoverFlowViewTransformer();
        mCoverTransformer.setYProjection(0);
        mCoverTransformer.setOffsetXPercent((float) 1.05);
        //mCoverTransformer.setScaleYFactor((float) 0.2);
        mCarouselView.setTransformer(mCoverTransformer);
        mCarouselView.setEnableFling(false);
        mCarouselView.setAdapter(mTaskListAdapter);
        mNewListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewListFragmentDialog dialogFragment = new NewListFragmentDialog();
                android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                dialogFragment.show(ft, "dialog");
            }
        });
        getData();
    }

    void getData() {
        CollectionReference taskListCollection = mFirebaseFirestore.collection(getString(R.string.tasks_collection))
                .document(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid())
                .collection(getString(R.string.tasks_collection));

        mAvatar.setImageURI(mFirebaseAuth.getCurrentUser().getPhotoUrl());
        String name = mFirebaseAuth.getCurrentUser().getDisplayName();
        if (name.contains(" ")) {
            name = name.split(" ")[0];
        }
        mUserTitle.setText("Hello, " + name);

        taskListCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                        if (documentChange.getType().equals(DocumentChange.Type.ADDED)) {
                            DocumentSnapshot documentSnapshot = documentChange.getDocument();
                            TaskList taskList = documentSnapshot.toObject(TaskList.class);
                            mTaskListArrayList.add(taskList);
                            mTaskListAdapter.notifyItemInserted(mTaskListArrayList.size() - 1);
                            mTotalTasks += taskList.incomplete_tasks;
                        } else if (documentChange.getType().equals(DocumentChange.Type.MODIFIED)){
                            DocumentSnapshot documentSnapshot2 = documentChange.getDocument();
                            TaskList taskList = documentSnapshot2.toObject(TaskList.class);
                            if (mTaskListArrayList.size() > 0) {
                                String title = taskList.title;
                                int index = 0;
                                for (int i = 0; i < mTaskListArrayList.size(); i++) {
                                    if ((mTaskListArrayList.get(i) != null) && title.equals(mTaskListArrayList.get(i).title)) {
                                        index = i;
                                        break;
                                    }
                                }
                                mTaskListArrayList.set(index, taskList);
                                mTaskListAdapter.notifyItemChanged(index, taskList);
                            }
                        }
                    }

                    if (mTaskListArrayList.size() > 0) {
                        mIntro.setText(getWish()+"\nYou have " + mTotalTasks + " Tasks pending");
                        TaskList taskList = mTaskListArrayList.get(0);
                        int colors[] = {Color.parseColor(taskList.start_color), Color.parseColor(taskList.end_color)};
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
                        mCoordinatorLayout.setBackground(gradientDrawable);
                        mCarouselView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                TaskList taskList = mTaskListArrayList.get(mCarouselView.getCurrentAdapterPosition());
                                int colors[] = {Color.parseColor(taskList.start_color), Color.parseColor(taskList.end_color)};
                                GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
                                mCoordinatorLayout.setBackground(gradientDrawable);
                            }
                        });
                    } else {
                        int colors[] = {Color.parseColor("#FF7B02"), Color.parseColor("#FFCB52")};
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
                        mCoordinatorLayout.setBackground(gradientDrawable);
                    }
                }
            }
        });
    }

    String getDate(){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String weekDay = null;
        switch (dayOfWeek){
            case Calendar.MONDAY:
                weekDay = getString(R.string.monday);
                break;
            case Calendar.TUESDAY:
                weekDay = getString(R.string.tuesday);
                break;
            case Calendar.WEDNESDAY:
                weekDay = getString(R.string.wednesday);
                break;
            case Calendar.THURSDAY:
                weekDay = getString(R.string.thursday);
                break;
            case Calendar.FRIDAY:
                weekDay = getString(R.string.friday);
                break;
            case Calendar.SATURDAY:
                weekDay = getString(R.string.saturday);
                break;
            case Calendar.SUNDAY:
                weekDay = getString(R.string.sunday);
                break;
        }

        String monthName = null;
        switch (month){
            case Calendar.JANUARY:
                monthName = getString(R.string.january);
                break;
            case Calendar.FEBRUARY:
                monthName =  getString(R.string.february);
                break;
            case Calendar.MARCH:
                monthName =  getString(R.string.march);
                break;
            case Calendar.APRIL:
                monthName =  getString(R.string.april);
                break;
            case Calendar.MAY:
                monthName =  getString(R.string.may);
                break;
            case Calendar.JUNE:
                monthName =  getString(R.string.june);
                break;
            case Calendar.JULY:
                monthName =  getString(R.string.july);
                break;
            case Calendar.AUGUST:
                monthName =  getString(R.string.august);
                break;
            case Calendar.SEPTEMBER:
                monthName =  getString(R.string.september);
                break;
            case Calendar.OCTOBER:
                monthName =  getString(R.string.october);
                break;
            case Calendar.NOVEMBER:
                monthName =  getString(R.string.november);
                break;
            case Calendar.DECEMBER:
                monthName =  getString(R.string.december);
                break;
        }
        return weekDay + ", " + monthName + " " + day;
    }

    String getWish(){
        Calendar calendar = Calendar.getInstance();
         int hour = calendar.get(Calendar.HOUR_OF_DAY);
         if (hour>=0 && hour<12){
             return "Good Morning!";
         } else if (hour>=12 && hour < 16){
             return "Good Afternoon!";
         } else{
             return "Good Evening!";
         }
    }
}
