package com.offbeat.apps.tasks.UI.Dialog;

import android.animation.Animator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.offbeat.apps.tasks.R;
import com.offbeat.apps.tasks.UI.TodoListActivity;
import com.offbeat.apps.tasks.Utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NewTodoFragmentDialog extends NewListFragmentDialog {

    View rootView;
    ImageView mCloseButton;
    TodoListActivity todoListActivity;
    Button mDoneButton;
    String startColor, endColor;
    ScrollView scrollView;
    int colors[];
    View taskAccent;
    SimpleDraweeView taskIcon;
    TextView listTitle;
    GradientDrawable gradientDrawable;
    TextInputEditText editText;
    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_NoActionBar);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        assert window != null;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //Utils.setLightStatusBar(getActivity(), window.getDecorView(), getActivity());
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.create_new_todo_layout, container, false);
        todoListActivity = (TodoListActivity) getActivity();
        scrollView = rootView.findViewById(R.id.container);
        mDoneButton = rootView.findViewById(R.id.done_button);
        startColor = todoListActivity.mIntent.getStringExtra(getString(R.string.start_color));
        endColor = todoListActivity.mIntent.getStringExtra(getString(R.string.end_color));
        colors = new int[]{Color.parseColor(startColor), Color.parseColor(endColor)};
        gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        taskAccent = rootView.findViewById(R.id.list_color);
        taskAccent.setBackground(gradientDrawable);
        mDoneButton.setBackground(gradientDrawable);
        editText = rootView.findViewById(R.id.upload_title);
        listTitle = rootView.findViewById(R.id.tv_list_title);
        listTitle.setText(todoListActivity.mIntent.getStringExtra(getString(R.string.title)));
        taskIcon = rootView.findViewById(R.id.iv_list_icon);

        int icon = todoListActivity.mIntent.getIntExtra(getString(R.string.icon), 1);
        switch (icon) {
            case 1:
                taskIcon.setImageResource(R.drawable.icon_1);
                break;
            case 2:
                taskIcon.setImageResource(R.drawable.icon_2);
                break;
            case 3:
                taskIcon.setImageResource(R.drawable.icon_3);
                break;
            case 4:
                taskIcon.setImageResource(R.drawable.icon_4);
                break;
            case 5:
                taskIcon.setImageResource(R.drawable.icon_5);
                break;
            case 6:
                taskIcon.setImageResource(R.drawable.icon_6);
                break;
            case 7:
                taskIcon.setImageResource(R.drawable.icon_7);
                break;
            case 8:
                taskIcon.setImageResource(R.drawable.icon_8);
                break;
            default:
                taskIcon.setImageResource(R.drawable.icon_1);
                break;
        }

        scrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                int cx = (int) (todoListActivity.mFloatingActionButton.getX() + (todoListActivity.mFloatingActionButton.getWidth() / 2))+16;
                int cy = (int) (todoListActivity.mFloatingActionButton.getY()) + todoListActivity.mFloatingActionButton.getHeight() + 56;
                Display display = Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                float finalRadius = Math.max(width, height) / 2 + Math.max(width - cx, height - cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.setDuration(500);
                anim.start();
            }
        });

        mDoneButton.setEnabled(true);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editText.getText())){
                    createNewTask(Objects.requireNonNull(editText.getText()).toString());
                } else {
                    editText.setError(Objects.requireNonNull(getActivity()).getString(R.string.edit_text_empty));
                }
            }
        });

        mCloseButton = rootView.findViewById(R.id.iv_dialog_cancel);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return rootView;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //Utils.setLightStatusBar(getActivity(), Objects.requireNonNull(getActivity()).getWindow().getDecorView(), getActivity());
        dismiss();
    }

    void  createNewTask(String title){
        CollectionReference todoReference = mFirebaseFirestore.collection(getString(R.string.tasks_collection))
                .document(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid())
                .collection(getString(R.string.tasks_collection)).document(todoListActivity.mIntent.getStringExtra(getString(R.string.id))).collection(getString(R.string.tasks_collection));

        DocumentReference documentReference = todoReference.document();
        Map<String, Object> map = new HashMap<>();
        map.put(Objects.requireNonNull(getActivity()).getString(R.string.id), documentReference.getId());
        map.put(getActivity().getString(R.string.title), title);
        map.put(getActivity().getString(R.string.timestamp), Timestamp.now());
        map.put(getActivity().getString(R.string.is_complete), false);
        map.put(getActivity().getString(R.string.due_date), Timestamp.now());
        map.put(getActivity().getString(R.string.description), "");

        documentReference.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mDoneButton.setEnabled(false);
                    DocumentReference todoMainReference = mFirebaseFirestore.collection(getString(R.string.tasks_collection))
                            .document(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid())
                            .collection(getString(R.string.tasks_collection)).document(todoListActivity.mIntent.getStringExtra(getString(R.string.id)));
                    todoListActivity.incomplete = todoListActivity.incomplete + 1;
                    todoMainReference.update(getActivity().getString(R.string.incomplete_tasks), todoListActivity.incomplete).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                todoListActivity.mNumberTasks.setText(todoListActivity.incomplete + " Tasks");
                                Toast.makeText(getActivity(), "Task created", Toast.LENGTH_SHORT).show();
                                dismiss();
                            }
                        }
                    });
                }

            }
        });
    }
}
