package com.example.tralalero.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.model.Workspace;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    private List<Workspace> workspaceList;
    private Context context;
    private OnWorkspaceClickListener listener;

    public interface OnWorkspaceClickListener {
        void onWorkspaceClick(Workspace workspace);
    }

    public HomeAdapter(Context context) {
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

    public void removeWorkspace(int position) {
        if (position >= 0 && position < workspaceList.size()) {
            workspaceList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateWorkspace(int position, Workspace workspace) {
        if (position >= 0 && position < workspaceList.size() && workspace != null) {
            workspaceList.set(position, workspace);
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.workspace_item, parent, false);
        return new HomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        Workspace workspace = workspaceList.get(position);
        holder.bind(workspace);
    }

    @Override
    public int getItemCount() {
        return workspaceList.size();
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder {
        private ImageView workspaceIcon;
        private TextView workspaceName;

        public HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            workspaceIcon = itemView.findViewById(R.id.ivWorkspaceIcon);
            workspaceName = itemView.findViewById(R.id.tvWorkspaceName);
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

