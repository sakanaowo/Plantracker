package com.example.tralalero.feature.home.ui.Home.project;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class ProjectMenuBottomSheet extends BottomSheetDialogFragment {
    
    private static final String ARG_PROJECT_NAME = "project_name";
    private static final String ARG_PROJECT_ID = "project_id";
    
    private String projectName;
    private String projectId;
    private boolean isStarred = false;
    private String currentVisibility = "WORKSPACE"; // PRIVATE, WORKSPACE, PUBLIC
    
    private TextView tvMenuTitle;
    private ImageButton btnClose, btnShare, btnStar, btnVisibility, btnCopy, btnMore;
    private RecyclerView rvMembers, rvActivity;
    private MaterialButton btnInvite;
    private SwitchMaterial switchCompletedStatus;
    private View layoutAbout, layoutArchiveComplete, layoutPowerUps, layoutPinToHome, layoutSynced;
    
    private MemberAdapter memberAdapter;
    private ActivityAdapter activityAdapter;

    public static ProjectMenuBottomSheet newInstance(String projectId, String projectName) {
        ProjectMenuBottomSheet fragment = new ProjectMenuBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECT_ID, projectId);
        args.putString(ARG_PROJECT_NAME, projectName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString(ARG_PROJECT_ID);
            projectName = getArguments().getString(ARG_PROJECT_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.project_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupListeners();
        setupRecyclerViews();
        loadData();
    }

    private void initViews(View view) {
        tvMenuTitle = view.findViewById(R.id.tvMenuTitle);
        btnClose = view.findViewById(R.id.btnClose);
        btnShare = view.findViewById(R.id.btnShare);
        btnStar = view.findViewById(R.id.btnStar);
        btnVisibility = view.findViewById(R.id.btnVisibility);
        btnCopy = view.findViewById(R.id.btnCopy);
        btnMore = view.findViewById(R.id.btnMore);
        
        rvMembers = view.findViewById(R.id.rvMembers);
        rvActivity = view.findViewById(R.id.rvActivity);
        btnInvite = view.findViewById(R.id.btnInvite);
        switchCompletedStatus = view.findViewById(R.id.switchCompletedStatus);
        
        layoutAbout = view.findViewById(R.id.layoutAbout);
        layoutArchiveComplete = view.findViewById(R.id.layoutArchiveComplete);
        layoutPowerUps = view.findViewById(R.id.layoutPowerUps);
        layoutPinToHome = view.findViewById(R.id.layoutPinToHome);
        layoutSynced = view.findViewById(R.id.layoutSynced);
        
        if (projectName != null) {
            tvMenuTitle.setText("Board menu");
        }
        
        // Load saved states from SharedPreferences
        loadSavedStates();
        
        // Update star icon
        updateStarIcon();
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> dismiss());
        
        btnShare.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Share project link", Toast.LENGTH_SHORT).show();
            // TODO: Implement share functionality
        });
        
        btnStar.setOnClickListener(v -> {
            toggleStar();
        });
        
        btnVisibility.setOnClickListener(v -> {
            showVisibilityDialog();
        });
        
        btnCopy.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Copy project", Toast.LENGTH_SHORT).show();
        });
        
        btnMore.setOnClickListener(v -> {
            Toast.makeText(getContext(), "More options", Toast.LENGTH_SHORT).show();
        });
        
        btnInvite.setOnClickListener(v -> {
            // TODO: Kiểm tra quyền owner trước khi cho phép invite
            showInviteDialog();
        });
        
        switchCompletedStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String action = isChecked ? "enabled" : "disabled";
            addActivityLog(action + " showing complete status on " + projectName);
        });
        
        layoutAbout.setOnClickListener(v -> {
            openAboutBoard();
        });
        
        layoutArchiveComplete.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Archive completed cards", Toast.LENGTH_SHORT).show();
        });
        
        layoutPowerUps.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Add Power-Ups", Toast.LENGTH_SHORT).show();
        });
        
        layoutPinToHome.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Pin to home screen", Toast.LENGTH_SHORT).show();
        });
        
        layoutSynced.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Synced", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void toggleStar() {
        isStarred = !isStarred;
        updateStarIcon();
        
        // Save to SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("ProjectData", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("starred_" + projectId, isStarred).apply();
        
        String message = isStarred ? "Starred project" : "Unstarred project";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        
        // TODO: Call API to save star status
        // projectViewModel.toggleStarProject(projectId, isStarred);
    }
    
    private void loadSavedStates() {
        if (getContext() == null) return;
        
        SharedPreferences prefs = requireContext().getSharedPreferences("ProjectData", Context.MODE_PRIVATE);
        
        // Load star state
        isStarred = prefs.getBoolean("starred_" + projectId, false);
        
        // Load visibility state
        currentVisibility = prefs.getString("visibility_" + projectId, "WORKSPACE");
    }
    
    private void updateStarIcon() {
        if (btnStar != null) {
            if (isStarred) {
                btnStar.setImageResource(android.R.drawable.star_big_on);
            } else {
                btnStar.setImageResource(R.drawable.ic_star_outline);
            }
        }
    }
    
    private void showVisibilityDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_visibility);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        com.google.android.material.card.MaterialCardView cardPrivate = dialog.findViewById(R.id.cardPrivate);
        com.google.android.material.card.MaterialCardView cardWorkspace = dialog.findViewById(R.id.cardWorkspace);
        com.google.android.material.card.MaterialCardView cardPublic = dialog.findViewById(R.id.cardPublic);
        
        RadioButton rbPrivate = dialog.findViewById(R.id.rbPrivate);
        RadioButton rbWorkspace = dialog.findViewById(R.id.rbWorkspace);
        RadioButton rbPublic = dialog.findViewById(R.id.rbPublic);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSaveVisibility);
        
        // Set current selection
        updateCardSelection(cardPrivate, cardWorkspace, cardPublic, rbPrivate, rbWorkspace, rbPublic, currentVisibility);
        
        // Card click listeners
        cardPrivate.setOnClickListener(v -> {
            currentVisibility = "PRIVATE";
            updateCardSelection(cardPrivate, cardWorkspace, cardPublic, rbPrivate, rbWorkspace, rbPublic, currentVisibility);
        });
        
        cardWorkspace.setOnClickListener(v -> {
            currentVisibility = "WORKSPACE";
            updateCardSelection(cardPrivate, cardWorkspace, cardPublic, rbPrivate, rbWorkspace, rbPublic, currentVisibility);
        });
        
        cardPublic.setOnClickListener(v -> {
            currentVisibility = "PUBLIC";
            updateCardSelection(cardPrivate, cardWorkspace, cardPublic, rbPrivate, rbWorkspace, rbPublic, currentVisibility);
        });
        
        btnSave.setOnClickListener(v -> {
            // Save to SharedPreferences
            SharedPreferences prefs = requireContext().getSharedPreferences("ProjectData", Context.MODE_PRIVATE);
            prefs.edit().putString("visibility_" + projectId, currentVisibility).apply();
            
            Toast.makeText(getContext(), "Visibility updated to: " + currentVisibility, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            
            // TODO: Call API to save visibility
            // projectViewModel.updateVisibility(projectId, currentVisibility);
        });
        
        dialog.show();
    }
    
    private void updateCardSelection(
            com.google.android.material.card.MaterialCardView cardPrivate,
            com.google.android.material.card.MaterialCardView cardWorkspace,
            com.google.android.material.card.MaterialCardView cardPublic,
            RadioButton rbPrivate,
            RadioButton rbWorkspace,
            RadioButton rbPublic,
            String selectedVisibility) {
        
        // Reset all cards
        cardPrivate.setStrokeWidth(1);
        cardPrivate.setStrokeColor(android.graphics.Color.parseColor("#DFE1E6"));
        cardPrivate.setCardBackgroundColor(android.graphics.Color.WHITE);
        rbPrivate.setChecked(false);
        
        cardWorkspace.setStrokeWidth(1);
        cardWorkspace.setStrokeColor(android.graphics.Color.parseColor("#DFE1E6"));
        cardWorkspace.setCardBackgroundColor(android.graphics.Color.WHITE);
        rbWorkspace.setChecked(false);
        
        cardPublic.setStrokeWidth(1);
        cardPublic.setStrokeColor(android.graphics.Color.parseColor("#DFE1E6"));
        cardPublic.setCardBackgroundColor(android.graphics.Color.WHITE);
        rbPublic.setChecked(false);
        
        // Highlight selected card
        if ("PRIVATE".equals(selectedVisibility)) {
            cardPrivate.setStrokeWidth(4);
            cardPrivate.setStrokeColor(android.graphics.Color.parseColor("#0079BF"));
            cardPrivate.setCardBackgroundColor(android.graphics.Color.parseColor("#DEEBFF"));
            rbPrivate.setChecked(true);
        } else if ("WORKSPACE".equals(selectedVisibility)) {
            cardWorkspace.setStrokeWidth(4);
            cardWorkspace.setStrokeColor(android.graphics.Color.parseColor("#0079BF"));
            cardWorkspace.setCardBackgroundColor(android.graphics.Color.parseColor("#DEEBFF"));
            rbWorkspace.setChecked(true);
        } else if ("PUBLIC".equals(selectedVisibility)) {
            cardPublic.setStrokeWidth(4);
            cardPublic.setStrokeColor(android.graphics.Color.parseColor("#0079BF"));
            cardPublic.setCardBackgroundColor(android.graphics.Color.parseColor("#DEEBFF"));
            rbPublic.setChecked(true);
        }
    }
    
    private void showInviteDialog() {
        // TODO: Implement invite member dialog
        Toast.makeText(getContext(), "Invite member feature - coming soon", Toast.LENGTH_SHORT).show();
    }

    private void setupRecyclerViews() {
        memberAdapter = new MemberAdapter();
        rvMembers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvMembers.setAdapter(memberAdapter);
        
        activityAdapter = new ActivityAdapter();
        rvActivity.setLayoutManager(new LinearLayoutManager(getContext()));
        rvActivity.setAdapter(activityAdapter);
    }

    private void loadData() {
        // Load members (hardcoded for now)
        List<Member> members = new ArrayList<>();
        members.add(new Member("BA", "B22DCVT028 Nguyen Thai Anh", true));
        memberAdapter.setMembers(members);
        
        // Load activity logs (hardcoded for now - giống như trong ảnh)
        List<ActivityLog> activities = new ArrayList<>();
        activities.add(new ActivityLog("BA", "B22DCVT028 Nguyen Thai Anh added Checklist to Test card 2", "Oct 2 at 10:52 AM"));
        activities.add(new ActivityLog("BA", "B22DCVT028 Nguyen Thai Anh added Test card 2 to Test list", "Oct 1 at 8:58 PM"));
        activityAdapter.setActivities(activities);
    }
    
    private void addActivityLog(String action) {
        if (activityAdapter != null) {
            ActivityLog newLog = new ActivityLog("TK", "TI Ks " + action, "Just now");
            activityAdapter.addActivity(newLog);
        }
    }

    static class Member {
        String initials;
        String name;
        boolean isAdmin;

        Member(String initials, String name, boolean isAdmin) {
            this.initials = initials;
            this.name = name;
            this.isAdmin = isAdmin;
        }
    }

    static class ActivityLog {
        String avatar;
        String text;
        String time;

        ActivityLog(String avatar, String text, String time) {
            this.avatar = avatar;
            this.text = text;
            this.time = time;
        }
    }

    static class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
        private List<Member> members = new ArrayList<>();

        void setMembers(List<Member> members) {
            this.members = members;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Member member = members.get(position);
            holder.tvMemberAvatar.setText(member.initials);
            holder.ivAdminBadge.setVisibility(member.isAdmin ? View.VISIBLE : View.GONE);
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMemberAvatar;
            View ivAdminBadge;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMemberAvatar = itemView.findViewById(R.id.tvMemberAvatar);
                ivAdminBadge = itemView.findViewById(R.id.ivAdminBadge);
            }
        }
    }

    static class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
        private List<ActivityLog> activities = new ArrayList<>();

        void setActivities(List<ActivityLog> activities) {
            this.activities = activities;
            notifyDataSetChanged();
        }
        
        void addActivity(ActivityLog activity) {
            activities.add(0, activity);
            notifyItemInserted(0);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ActivityLog activity = activities.get(position);
            holder.tvActivityAvatar.setText(activity.avatar);
            
            String fullText = activity.text;
            SpannableString spannableString = new SpannableString(fullText);
            
            // Bold username (first part before "added")
            int addedIndex = fullText.indexOf(" added");
            if (addedIndex != -1) {
                spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 
                    0, addedIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            // Bold card/item names (text between "added" and "to")
            int toIndex = fullText.indexOf(" to ", addedIndex);
            if (addedIndex != -1 && toIndex != -1) {
                int startBold = addedIndex + 7; // " added ".length()
                spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 
                    startBold, toIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            // Bold target name (text after "to" until end or next word)
            if (toIndex != -1) {
                int startTarget = toIndex + 4; // " to ".length()
                spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 
                    startTarget, fullText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            holder.tvActivityText.setText(spannableString);
            holder.tvActivityTime.setText(activity.time);
        }

        @Override
        public int getItemCount() {
            return activities.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvActivityAvatar;
            TextView tvActivityText;
            TextView tvActivityTime;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvActivityAvatar = itemView.findViewById(R.id.tvActivityAvatar);
                tvActivityText = itemView.findViewById(R.id.tvActivityText);
                tvActivityTime = itemView.findViewById(R.id.tvActivityTime);
            }
        }
    }
    
    private void openAboutBoard() {
        Intent intent = new Intent(getContext(), AboutBoardActivity.class);
        intent.putExtra("projectId", projectId);
        intent.putExtra("projectName", projectName);
        intent.putExtra("description", "It's your board's time to shine! Let people know what this board is used for and what they can expect to see.");
        intent.putExtra("ownerName", "Tl Ks");
        intent.putExtra("ownerUsername", "tlks1");
        // TODO: Load real data from API
        startActivity(intent);
        dismiss();
    }
}
