package com.example.tralalero.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Project;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceAdapter extends RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder> {
    
    private List<Project> projectList;
    private Context context;
    private OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public WorkspaceAdapter(Context context) {
        this.context = context;
        this.projectList = new ArrayList<>();
    }

    public void setOnProjectClickListener(OnProjectClickListener listener) {
        this.listener = listener;
    }

    public void setProjectList(List<Project> projectList) {
        this.projectList = projectList != null ? projectList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addProject(Project project) {
        if (project != null) {
            projectList.add(project);
            notifyItemInserted(projectList.size() - 1);
        }
    }

    @NonNull
    @Override
    public WorkspaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.project_item, parent, false);
        return new WorkspaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkspaceViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public class WorkspaceViewHolder extends RecyclerView.ViewHolder {
        private ImageButton projectIcon;
        private TextView projectName;

        public WorkspaceViewHolder(@NonNull View itemView) {
            super(itemView);
            projectIcon = itemView.findViewById(R.id.animalImageButton);
            projectName = itemView.findViewById(R.id.tvSubtitle);
        }

        public void bind(Project project) {
            projectName.setText(project.getName());

            if ("KANBAN".equals(project.getBoardType())) {
                projectIcon.setImageResource(R.drawable.board_icon1);
            } else {
                projectIcon.setImageResource(R.drawable.workspace);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectClick(project);
                }
            });
        }
    }
}