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
import com.example.tralalero.model.Workspace;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceAdapter extends RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder> {
    
    private List<Workspace> workspaceList;
    private Context context;
    private OnWorkspaceClickListener listener;

    public interface OnWorkspaceClickListener {
        void onWorkspaceClick(Workspace workspace);
    }

    public WorkspaceAdapter(Context context) {
        this.context = context;
        this.workspaceList = new ArrayList<>();
    }

    public void setOnWorkspaceClickListener(OnWorkspaceClickListener listener) {
        this.listener = listener;
    }

    public void setWorkspaceList(List<Workspace> workspaceList) {
        this.workspaceList = workspaceList != null ? workspaceList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addWorkspace(Workspace workspace) {
        if (workspace != null) {
            workspaceList.add(workspace);
            notifyItemInserted(workspaceList.size() - 1);
        }
    }

    @NonNull
    @Override
    public WorkspaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.your_workspace_item, parent, false);
        return new WorkspaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkspaceViewHolder holder, int position) {
        Workspace workspace = workspaceList.get(position);
        holder.bind(workspace);
    }

    @Override
    public int getItemCount() {
        return workspaceList.size();
    }

    public class WorkspaceViewHolder extends RecyclerView.ViewHolder {
        private ImageButton workspaceIcon;
        private TextView workspaceName;

        public WorkspaceViewHolder(@NonNull View itemView) {
            super(itemView);
            workspaceIcon = itemView.findViewById(R.id.animalImageButton);
            workspaceName = itemView.findViewById(R.id.tvSubtitle);
        }

        public void bind(Workspace workspace) {
            workspaceName.setText(workspace.getName());
            
            // Set icon based on workspace type or use a default icon
            if ("PERSONAL".equals(workspace.getType())) {
                workspaceIcon.setImageResource(R.drawable.acc_icon);
            } else {
                workspaceIcon.setImageResource(R.drawable.board_icon1);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWorkspaceClick(workspace);
                }
            });
        }
    }
}