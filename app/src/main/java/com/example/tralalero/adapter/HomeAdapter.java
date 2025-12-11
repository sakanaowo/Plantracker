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
import com.example.tralalero.domain.model.Workspace;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_WORKSPACE = 1;

    private List<Object> items = new ArrayList<>();  // Mixed list of headers and workspaces
    private Context context;
    private OnWorkspaceClickListener listener;

    public interface OnWorkspaceClickListener {
        void onWorkspaceClick(Workspace workspace);
    }

    public HomeAdapter(Context context) {
        this.context = context;
    }

    public void setOnWorkspaceClickListener(OnWorkspaceClickListener listener) {
        this.listener = listener;
    }

    public void setWorkspaceList(List<Workspace> workspaceList) {
        items.clear();

        if (workspaceList == null || workspaceList.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        // Separate workspaces into owned and shared
        List<Workspace> ownedWorkspaces = new ArrayList<>();
        List<Workspace> sharedWorkspaces = new ArrayList<>();

        for (Workspace workspace : workspaceList) {
            if (workspace.isOwner()) {
                ownedWorkspaces.add(workspace);
            } else {
                sharedWorkspaces.add(workspace);
            }
        }

        // Add "Your Workspaces" section
        if (!ownedWorkspaces.isEmpty()) {
            items.add("YOUR WORKSPACE");
            items.addAll(ownedWorkspaces);
        }

        // Add "Shared Workspaces" section
        if (!sharedWorkspaces.isEmpty()) {
            items.add("SHARED WORKSPACE");
            items.addAll(sharedWorkspaces);
        }

        notifyDataSetChanged();
    }

    public void addWorkspace(Workspace workspace) {
        if (workspace != null) {
            // Recreate the list with the new workspace
            List<Workspace> currentWorkspaces = new ArrayList<>();
            for (Object item : items) {
                if (item instanceof Workspace) {
                    currentWorkspaces.add((Workspace) item);
                }
            }
            currentWorkspaces.add(workspace);
            setWorkspaceList(currentWorkspaces);
        }
    }

    public void removeWorkspace(int position) {
        if (position >= 0 && position < items.size() && items.get(position) instanceof Workspace) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateWorkspace(List<Workspace> workspace) {
        setWorkspaceList(workspace);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_WORKSPACE;
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
                    .inflate(R.layout.workspace_item, parent, false);
            return new HomeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            String title = (String) items.get(position);
            ((HeaderViewHolder) holder).bind(title);
        } else if (holder instanceof HomeViewHolder) {
            Workspace workspace = (Workspace) items.get(position);
            ((HomeViewHolder) holder).bind(workspace);
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

    // Workspace ViewHolder
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
