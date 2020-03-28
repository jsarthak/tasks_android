package com.offbeat.apps.tasks.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.offbeat.apps.tasks.Data.Todo;
import com.offbeat.apps.tasks.R;
import java.util.ArrayList;
import java.util.Objects;

public class TodoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = TodoAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<Todo> mTodoArrayList;
    private int completedTasks, incompleteTasks;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;

    private String id;

    public TodoAdapter(Context mContext, ArrayList<Todo> mTodoArrayList, String id, int com, int inc) {
        this.mContext = mContext;
        this.mTodoArrayList = mTodoArrayList;
        this.completedTasks = com;
        this.incompleteTasks = inc;
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        this.id = id;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.todo_list_item, viewGroup, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final Todo todo = mTodoArrayList.get(i);
        ((TodoViewHolder) viewHolder).title.setText(todo.title);
        ((TodoViewHolder) viewHolder).complete.setChecked(todo.is_complete);
        ((TodoViewHolder) viewHolder).complete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DocumentReference todoReference = mFirebaseFirestore.collection(mContext.getString(R.string.tasks_collection))
                            .document(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid())
                            .collection(mContext.getString(R.string.tasks_collection)).document(id).collection(mContext.getString(R.string.tasks_collection)).document(todo.id);
                    todoReference.update(mContext.getString(R.string.is_complete), true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            DocumentReference taskReference = mFirebaseFirestore.collection(mContext.getString(R.string.tasks_collection))
                                    .document(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid())
                                    .collection(mContext.getString(R.string.tasks_collection)).document(id);
                            taskReference.update(mContext.getString(R.string.completed_tasks), completedTasks + 1, mContext.getString(R.string.incomplete_tasks), incompleteTasks - 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d(TAG, "onComplete: ");
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTodoArrayList.size();
    }

    class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        CheckBox complete;

        TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            complete = itemView.findViewById(R.id.cb_todo_list);
        }
    }
}
