package com.example.tralalero.feature.home.ui.Home.calendar;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;
import com.example.tralalero.domain.model.User;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom sheet for selecting team members for meeting scheduling
 */
public class MemberSelectionBottomSheet extends BottomSheetDialogFragment {

    private List<User> allMembers;
    private List<User> selectedMembers = new ArrayList<>();
    private MemberSelectionListener listener;
    private MemberSelectionAdapter adapter;

    private RecyclerView rvMembers;
    private TextView tvSelectedCount;
    private Button btnNext;
    private TextInputEditText etSearch;

    public interface MemberSelectionListener {
        void onMembersSelected(List<User> members);
    }

    public static MemberSelectionBottomSheet newInstance(List<User> members) {
        MemberSelectionBottomSheet fragment = new MemberSelectionBottomSheet();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.allMembers = members;
        return fragment;
    }

    public void setMembers(List<User> members) {
        this.allMembers = members;
    }

    public void setListener(MemberSelectionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_member_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupSearchFilter();
        setupButtons();
    }

    private void initializeViews(View view) {
        rvMembers = view.findViewById(R.id.rvMembers);
        tvSelectedCount = view.findViewById(R.id.tvSelectedCount);
        btnNext = view.findViewById(R.id.btnNext);
        etSearch = view.findViewById(R.id.etSearch);
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dismiss());
    }

    private void setupRecyclerView() {
        adapter = new MemberSelectionAdapter(
            allMembers != null ? allMembers : new ArrayList<>(),
            this::onMemberToggled
        );

        rvMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMembers.setAdapter(adapter);
    }

    private void setupSearchFilter() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupButtons() {
        btnNext.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMembersSelected(selectedMembers);
            }
            dismiss();
        });
    }

    private void onMemberToggled(User user, boolean isSelected) {
        if (isSelected) {
            if (!selectedMembers.contains(user)) {
                selectedMembers.add(user);
            }
        } else {
            selectedMembers.remove(user);
        }

        updateSelectedCount();
        btnNext.setEnabled(!selectedMembers.isEmpty());
    }

    private void updateSelectedCount() {
        int count = selectedMembers.size();
        String text = count + " member" + (count != 1 ? "s" : "") + " selected";
        tvSelectedCount.setText(text);
    }
}
