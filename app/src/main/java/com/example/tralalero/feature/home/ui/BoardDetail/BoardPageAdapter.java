package com.example.tralalero.feature.home.ui.BoardDetail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tralalero.R;

import java.util.List;

public class BoardPageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_BOARD = 0;
    private static final int TYPE_ADD_LIST = 1;

    private List<BoardPage> pages;
    private Context context;

    // flag quản lý trạng thái form AddList
    private boolean isFormVisible = false;

    public BoardPageAdapter(Context context, List<BoardPage> pages) {
        this.context = context;
        this.pages = pages;
    }

    @Override
    public int getItemViewType(int position) {
        return pages.get(position).isAddList() ? TYPE_ADD_LIST : TYPE_BOARD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_ADD_LIST) {
            View view = inflater.inflate(R.layout.item_add_list, parent, false);
            return new AddListViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.board_list_item, parent, false);
            return new BoardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BoardPage page = pages.get(position);

        if (holder instanceof BoardViewHolder) {
            // bind dữ liệu cho list bình thường
            ((BoardViewHolder) holder).tvBoardTitle.setText(page.getTitle());
        } else if (holder instanceof AddListViewHolder) {
            AddListViewHolder vh = (AddListViewHolder) holder;

            // đảm bảo render đúng trạng thái khi recycle
            if (isFormVisible) {
                vh.formLayout.setVisibility(View.VISIBLE);
                vh.btnAddList.setVisibility(View.GONE);
            } else {
                vh.formLayout.setVisibility(View.GONE);
                vh.btnAddList.setVisibility(View.VISIBLE);
            }

            // sự kiện bấm AddList
            vh.btnAddList.setOnClickListener(v -> {
                isFormVisible = true;
                notifyItemChanged(position);
            });

            // sự kiện bấm Cancel
            vh.btnCancel.setOnClickListener(v -> {
                isFormVisible = false;
                vh.edtListName.setText("");
                notifyItemChanged(position);
            });

            // sự kiện bấm Confirm
            vh.btnConfirm.setOnClickListener(v -> {
                String name = vh.edtListName.getText().toString().trim();
                if (!name.isEmpty()) {
                    // thêm board mới trước item AddList
                    pages.add(pages.size() - 1, new BoardPage(name, false));
                    notifyDataSetChanged();
                }
                // reset trạng thái
                isFormVisible = false;
                vh.edtListName.setText("");
                notifyItemChanged(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    // ViewHolder cho item Board
    static class BoardViewHolder extends RecyclerView.ViewHolder {
        TextView tvBoardTitle;
        RecyclerView taskRecycler;
        TextView btnAddCard;

        public BoardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBoardTitle = itemView.findViewById(R.id.tvBoardTitle);
            taskRecycler = itemView.findViewById(R.id.taskRecycler);
            btnAddCard = itemView.findViewById(R.id.btnAddCard);
        }
    }

    // ViewHolder cho item AddList
    static class AddListViewHolder extends RecyclerView.ViewHolder {
        Button btnAddList, btnCancel, btnConfirm;
        EditText edtListName;
        View formLayout;

        public AddListViewHolder(@NonNull View itemView) {
            super(itemView);
            btnAddList = itemView.findViewById(R.id.btnAddList);
            formLayout = itemView.findViewById(R.id.formLayout);
            edtListName = itemView.findViewById(R.id.edtListName);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
        }
    }
}
