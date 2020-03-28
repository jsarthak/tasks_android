package com.offbeat.apps.tasks.Adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.gtomato.android.ui.widget.CarouselView;
import com.offbeat.apps.tasks.Data.TaskList;
import com.offbeat.apps.tasks.R;
import com.offbeat.apps.tasks.UI.MainActivity;
import com.offbeat.apps.tasks.UI.TodoListActivity;

import java.util.ArrayList;
import java.util.Objects;

public class TaskListAdapter extends CarouselView.Adapter<CarouselView.ViewHolder> {

    Context mContext;
    ArrayList<TaskList> mTaskListArrayList;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirebaseFirestore;
    private static final String TAG = TaskListAdapter.class.getSimpleName();

    public TaskListAdapter(Context mContext, ArrayList<TaskList> mTaskListArrayList) {
        this.mContext = mContext;
        this.mTaskListArrayList = mTaskListArrayList;
        this.mFirebaseAuth = FirebaseAuth.getInstance();
        this.mFirebaseFirestore = FirebaseFirestore.getInstance();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.task_list_item, viewGroup, false);
        RecyclerView.ViewHolder viewHolder = new TaskListViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final TaskList taskList = mTaskListArrayList.get(position);
        final TaskListViewHolder holder = (TaskListViewHolder) viewHolder;
        int w = mContext.getResources().getDisplayMetrics().widthPixels - 200;
        holder.cardView.setLayoutParams(new CardView.LayoutParams(w, ViewGroup.LayoutParams.MATCH_PARENT));
        holder.count.setText(taskList.incomplete_tasks + " Tasks");
        holder.title.setText(taskList.title);
        int colors[] = {Color.parseColor(taskList.start_color), Color.parseColor(taskList.end_color)};
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setCornerRadius(24f);
        int icon = taskList.icon;
        switch (icon) {
            case 1:
                holder.accent.setImageResource(R.drawable.icon_1);
                break;
            case 2:
                holder.accent.setImageResource(R.drawable.icon_2);
                break;
            case 3:
                holder.accent.setImageResource(R.drawable.icon_3);
                break;
            case 4:
                holder.accent.setImageResource(R.drawable.icon_4);
                break;
            case 5:
                holder.accent.setImageResource(R.drawable.icon_5);
                break;
            case 6:
                holder.accent.setImageResource(R.drawable.icon_6);
                break;
            case 7:
                holder.accent.setImageResource(R.drawable.icon_7);
                break;
            case 8:
                holder.accent.setImageResource(R.drawable.icon_8);
                break;
            default:
                holder.accent.setImageResource(R.drawable.icon_1);
                break;
        }
        holder.accent.setBackground(gradientDrawable);
        GradientDrawable gradientDrawableProgress = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
        holder.progress.setBackground(gradientDrawableProgress);
        int percent = 0;
        int progressWidth = 0;
        if ((taskList.incomplete_tasks + taskList.completed_tasks) > 0) {
            percent = (taskList.completed_tasks / (taskList.completed_tasks + taskList.incomplete_tasks)) * 100;
            progressWidth = holder.progressBackground.getWidth() * (percent / 100);
        }
        holder.percent.setText(percent + "%");
        holder.progress.setLayoutParams(new FrameLayout.LayoutParams(progressWidth, ViewGroup.LayoutParams.MATCH_PARENT));
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, TodoListActivity.class);
                intent.putExtra(mContext.getString(R.string.id), taskList.id);
                intent.putExtra(mContext.getString(R.string.title), taskList.title);
                intent.putExtra(mContext.getString(R.string.start_color), taskList.start_color);
                intent.putExtra(mContext.getString(R.string.end_color), taskList.end_color);
                intent.putExtra(mContext.getString(R.string.completed_tasks), taskList.completed_tasks);
                intent.putExtra(mContext.getString(R.string.incomplete_tasks), taskList.incomplete_tasks);
                intent.putExtra(mContext.getString(R.string.icon), taskList.icon);
                mContext.startActivity(intent);
            }
        });
        holder.options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.task_list_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_edit:
                                return true;
                            case R.id.action_delete:
                                CollectionReference tasksReference = mFirebaseFirestore.collection(mContext.getString(R.string.tasks_collection));
                                final DocumentReference userReference = tasksReference.document(Objects.requireNonNull(mFirebaseAuth.getUid()));
                                userReference.collection(mContext.getString(R.string.tasks_collection)).document(taskList.id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mTaskListArrayList.remove(taskList);
                                            TaskListAdapter.this.notifyDataSetChanged();
                                            Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                return true;
                            default:
                                return true;
                        }
                    }
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTaskListArrayList.size();
    }

    class TaskListViewHolder extends RecyclerView.ViewHolder {
        TextView count, title, percent;
        SimpleDraweeView accent;
        ImageView options;
        RelativeLayout container;
        CardView cardView;
        View progressBackground;
        FrameLayout progress;

        public TaskListViewHolder(@NonNull View itemView) {
            super(itemView);
            accent = itemView.findViewById(R.id.iv_task_list_accent);
            count = itemView.findViewById(R.id.tv_task_list_number_items);
            title = itemView.findViewById(R.id.tv_task_list_tile);
            container = itemView.findViewById(R.id.container);
            options = itemView.findViewById(R.id.iv_task_list_item_menu);
            progress = itemView.findViewById(R.id.task_progress);
            percent = itemView.findViewById(R.id.progress_percent);
            progressBackground = itemView.findViewById(R.id.progress_background);
            cardView = itemView.findViewById(R.id.task_list_card);
        }
    }
}
