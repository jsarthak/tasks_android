package com.offbeat.apps.tasks.UI.Dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.offbeat.apps.tasks.R;
import com.offbeat.apps.tasks.UI.MainActivity;
import com.offbeat.apps.tasks.Utils.Animations;
import com.offbeat.apps.tasks.Utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NewListFragmentDialog extends DialogFragment {

    private static final String TAG = NewListFragmentDialog.class.getSimpleName();

    View rootView;
    ImageView mCloseButton;
    MainActivity mainActivity;
    ScrollView scrollView;
    RadioGroup mRadioGroup;
    RadioGroup mIconRadioGroup;
    Button mCreateListButton;
    Animations animations;
    TextInputEditText mTitle;
    String startColor, endColor;
    int colors[];
    int icon = 1;
    GradientDrawable gradientDrawable;
    FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_NoActionBar);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        /*Objects.requireNonNull(getActivity()).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= 21) {
            Utils.setWindowFlag(getActivity(), WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        }*/
        super.onDismiss(dialog);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
        assert window != null;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //Utils.setLightStatusBar(getActivity(), window.getDecorView(), getActivity());
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.create_new_list_layout, container, false);
        mainActivity = (MainActivity) getActivity();
        animations = new Animations(mainActivity);
        scrollView = rootView.findViewById(R.id.container);
        startColor = mainActivity.getString(R.string.start_orange_grad);
        endColor = mainActivity.getString(R.string.end_orange_grad);

        colors = new int[]{Color.parseColor(startColor), Color.parseColor(endColor)};
        gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);


        mTitle = rootView.findViewById(R.id.upload_title);

        mCreateListButton = rootView.findViewById(R.id.done_button);
        mCreateListButton.setBackground(gradientDrawable);
        scrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);

                int cx = (int) (mainActivity.mNewListButton.getX() + (mainActivity.mNewListButton.getWidth() / 2))+16;
                int cy = (int) (mainActivity.mNewListButton.getY()) + mainActivity.mNewListButton.getHeight() + 56;
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                float finalRadius = Math.max(width, height) / 2 + Math.max(width - cx, height - cy);
                Animator anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.setDuration(700);
                anim.start();
            }
        });

        mCloseButton = rootView.findViewById(R.id.iv_dialog_cancel);
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mCreateListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mTitle.getText())){
                    Map<String, Object> map = new HashMap<>();
                    map.put(getString(R.string.title), mTitle.getText().toString());
                    map.put(getString(R.string.start_color), startColor);
                    map.put(getString(R.string.end_color), endColor);
                    map.put(getString(R.string.completed_tasks), 0);
                    map.put(getString(R.string.incomplete_tasks), 0);
                    map.put(getString(R.string.timestamp), Timestamp.now());
                    map.put(getActivity().getString(R.string.icon), icon);
                    createTask(mTitle.getText().toString(), map);
                } else{
                    mTitle.setError("Title is required");
                }
            }
        });

        mRadioGroup = rootView.findViewById(R.id.accent_button_group);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.accent_1:
                        startColor = mainActivity.getString(R.string.start_orange_grad);
                        endColor = mainActivity.getString(R.string.end_orange_grad);
                        colors = new int[]{Color.parseColor(startColor), Color.parseColor(endColor)};
                        gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);                        mCreateListButton.setBackground(gradientDrawable);
                        break;
                    case R.id.accent_2:
                        startColor = mainActivity.getString(R.string.start_purple_grad);
                        endColor = mainActivity.getString(R.string.end_purple_grad);
                        colors = new int[]{Color.parseColor(startColor), Color.parseColor(endColor)};
                        gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                        mCreateListButton.setBackground(gradientDrawable);
                        break;
                    case R.id.accent_3:
                        startColor = mainActivity.getString(R.string.start_green_grad);
                        endColor = mainActivity.getString(R.string.end_green_grad);
                        colors = new int[]{Color.parseColor(startColor), Color.parseColor(endColor)};
                        gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                        mCreateListButton.setBackground(gradientDrawable);
                        break;
                    case R.id.accent_4:
                        startColor = mainActivity.getString(R.string.start_yellow_grad);
                        endColor = mainActivity.getString(R.string.end_yellow_grad);
                        colors = new int[]{Color.parseColor(startColor), Color.parseColor(endColor)};
                        gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                        mCreateListButton.setBackground(gradientDrawable);
                        break;
                    default:
                        startColor = mainActivity.getString(R.string.start_orange_grad);
                        endColor = mainActivity.getString(R.string.end_orange_grad);
                        mCreateListButton.setBackground(gradientDrawable);
                        break;
                }
            }
        });

        mIconRadioGroup = rootView.findViewById(R.id.icon_button_group);
        mIconRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.icon_1:
                        icon = 1;
                        break;
                    case R.id.icon_2:
                        icon = 2;
                        break;
                    case R.id.icon_3:
                        icon = 3;
                        break;
                    case R.id.icon_4:
                        icon = 4;
                        break;
                    case R.id.icon_5:
                        icon = 5;
                        break;
                    case R.id.icon_6:
                        icon = 6;
                        break;
                    case R.id.icon_7:
                        icon = 7;
                        break;
                    case R.id.icon_8:
                        icon = 8;
                        break;
                        default:icon = 1;
                        break;
                }
            }
        });

        return rootView;

    }

    void createTask(String title, Map<String, Object> map){
        CollectionReference tasksReference = mFirebaseFirestore.collection(getString(R.string.tasks_collection));
        final DocumentReference userReference = tasksReference.document(Objects.requireNonNull(mFirebaseAuth.getUid()));
        DocumentReference documentReference = userReference.collection(getString(R.string.tasks_collection)).document();

        map.put(mainActivity.getString(R.string.id), documentReference.getId());

                documentReference.set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(mainActivity, "Created new list", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });
    }
}
