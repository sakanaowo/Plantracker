package com.example.tralalero.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.Attachment;

import java.util.ArrayList;
import java.util.List;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder> {
    
    private List<Attachment> attachments = new ArrayList<>();
    private OnAttachmentClickListener listener;
    
    public interface OnAttachmentClickListener {
        void onDownloadClick(Attachment attachment);
        void onDeleteClick(Attachment attachment);
        void onAttachmentClick(Attachment attachment);
    }
    
    public AttachmentAdapter(OnAttachmentClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attachment, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(attachments.get(position));
    }
    
    @Override
    public int getItemCount() {
        android.util.Log.d("AttachmentAdapter", "getItemCount() returning: " + attachments.size());
        return attachments.size();
    }
    
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
        android.util.Log.d("AttachmentAdapter", "setAttachments called with " + this.attachments.size() + " items");
        notifyDataSetChanged();
    }
    
    public List<Attachment> getAttachments() {
        return new ArrayList<>(attachments); // Return copy to prevent external modification
    }
    
    public void addAttachment(Attachment attachment) {
        attachments.add(0, attachment); // Add to top
        android.util.Log.d("AttachmentAdapter", "addAttachment called, new size: " + attachments.size());
        notifyItemInserted(0);
    }
    
    public void removeAttachment(int position) {
        if (position >= 0 && position < attachments.size()) {
            attachments.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivFileIcon;
        private final TextView tvFileName;
        private final TextView tvFileSize;
        private final ImageButton btnDownload;
        private final ImageButton btnDeleteAttachment;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFileIcon = itemView.findViewById(R.id.ivFileIcon);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileSize = itemView.findViewById(R.id.tvFileSize);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnDeleteAttachment = itemView.findViewById(R.id.btnDeleteAttachment);
        }
        
        public void bind(Attachment attachment) {
            // Set file name
            String displayName = attachment.getFileName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = attachment.getFileExtension();
            }
            tvFileName.setText(displayName);
            
            // Set file size
            tvFileSize.setText(attachment.getFileSizeFormatted());
            
            // Set file icon based on MIME type
            setFileIcon(attachment);
            
            // Download button click
            btnDownload.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDownloadClick(attachment);
                }
            });
            
            // Delete button click
            btnDeleteAttachment.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(attachment);
                    }
                }
            });
            
            // Item click to view/open attachment
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAttachmentClick(attachment);
                }
            });
        }
        
        private void setFileIcon(Attachment attachment) {
            int iconResId;
            
            if (attachment.isImage()) {
                iconResId = R.drawable.ic_image;
            } else if (attachment.isPdf()) {
                iconResId = R.drawable.ic_file;
            } else if (attachment.isDocument()) {
                iconResId = R.drawable.ic_file;
            } else {
                // Use generic file icon for all other types
                iconResId = R.drawable.ic_file;
            }
            
            ivFileIcon.setImageResource(iconResId);
        }
    }
}
