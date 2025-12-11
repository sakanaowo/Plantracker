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

/**
 * Adapter for displaying projects in the Home screen
 */
public class ProjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_PROJECT = 1;

    private List<Object> items = new ArrayList<>();  // Mixed list of headers and projects
    private Context context;
    private OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
    }

    public ProjectAdapter(Context context) {
        this.context = context;
    }

    public void setOnProjectClickListener(OnProjectClickListener listener) {
        this.listener = listener;
    }

    public void setProjectList(List<Project> projectList) {
        items.clear();

        if (projectList == null || projectList.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        // Group projects by workspace
        java.util.Map<String, List<Project>> projectsByWorkspace = new java.util.LinkedHashMap<>();
        for (Project project : projectList) {
            String workspaceName = project.getWorkspaceName() != null ? 
                project.getWorkspaceName() : "Unknown Workspace";
            
            if (!projectsByWorkspace.containsKey(workspaceName)) {
                projectsByWorkspace.put(workspaceName, new ArrayList<>());
            }
            projectsByWorkspace.get(workspaceName).add(project);
        }

        // Add grouped items with headers
        for (java.util.Map.Entry<String, List<Project>> entry : projectsByWorkspace.entrySet()) {
            items.add(entry.getKey()); // Header
            items.addAll(entry.getValue()); // Projects
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_PROJECT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.project_item, parent, false);
            return new ProjectViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            String title = (String) items.get(position);
            ((HeaderViewHolder) holder).bind(title);
        } else if (holder instanceof ProjectViewHolder) {
            Project project = (Project) items.get(position);
            ((ProjectViewHolder) holder).bind(project);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Header ViewHolder
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView sectionTitle;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.sectionTitle);
        }

        void bind(String title) {
            sectionTitle.setText(title);
        }
    }

    // Project ViewHolder
    public class ProjectViewHolder extends RecyclerView.ViewHolder {
        private ImageButton projectIcon;
        private TextView projectName;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectIcon = itemView.findViewById(R.id.animalImageButton);
            projectName = itemView.findViewById(R.id.tvSubtitle);
        }

        public void bind(Project project) {
            projectName.setText(project.getName());

            // Set icon based on project type
            if ("KANBAN".equalsIgnoreCase(project.getBoardType())) {
                projectIcon.setImageResource(R.drawable.board_icon1);
            } else {
                projectIcon.setImageResource(R.drawable.board_icon1);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectClick(project);
                }
            });
        }
    }
}
