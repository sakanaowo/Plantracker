package com.example.tralalero.feature.home.ui.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {
    private static final String ARG_TYPE = "type";

    public static ListFragment newInstance(String type) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    private String type;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_list_frm, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
        }
//      TODO : lấy dữ liệu từ api
        // ví dụ dữ liệu test
        List<String> data = new ArrayList<>();
        switch (type) {
            case "All":
                data.add("All item 1");
                data.add("All item 2");
                break;
            case "Mentions":
                data.add("Mention 1");
                data.add("Mention 2");
                break;
            case "Unread":
                data.add("Unread 1");
                data.add("Unread 2");
                break;
        }

        // set adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                data
        );

        recyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
                return new RecyclerView.ViewHolder(itemView) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                TextView textView = holder.itemView.findViewById(android.R.id.text1);
                textView.setText(data.get(position));
            }

            @Override
            public int getItemCount() {
                return data.size();
            }
        });

        return view;
    }
}
