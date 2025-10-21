package com.example.tralalero.feature.home.ui;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.tralalero.R;
public class BottomNavigationFragment extends Fragment {
    private OnNavigationItemSelectedListener listener;
    private int selectedItem = 0;
    public interface OnNavigationItemSelectedListener {
        void onNavigationItemSelected(int position);
    }
    public void setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener listener) {
        this.listener = listener;
    }
    public void setSelectedItem(int position) {
        this.selectedItem = position;
        updateSelection();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_navigation, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton btn1 = view.findViewById(R.id.btn1);
        ImageButton btn2 = view.findViewById(R.id.btn2);
        ImageButton btn3 = view.findViewById(R.id.btn3);
        ImageButton btn4 = view.findViewById(R.id.btn4);
        btn1.setOnClickListener(v -> {
            selectedItem = 0;
            updateSelection();
            if (listener != null) listener.onNavigationItemSelected(0);
        });
        btn2.setOnClickListener(v -> {
            selectedItem = 1;
            updateSelection();
            if (listener != null) listener.onNavigationItemSelected(1);
        });
        btn3.setOnClickListener(v -> {
            selectedItem = 2;
            updateSelection();
            if (listener != null) listener.onNavigationItemSelected(2);
        });
        btn4.setOnClickListener(v -> {
            selectedItem = 3;
            updateSelection();
            if (listener != null) listener.onNavigationItemSelected(3);
        });
        updateSelection();
    }
    private void updateSelection() {
        if (getView() == null) return;
        ImageButton btn1 = getView().findViewById(R.id.btn1);
        ImageButton btn2 = getView().findViewById(R.id.btn2);
        ImageButton btn3 = getView().findViewById(R.id.btn3);
        ImageButton btn4 = getView().findViewById(R.id.btn4);
        btn1.setImageTintList(getResources().getColorStateList(android.R.color.black, null));
        btn2.setImageTintList(getResources().getColorStateList(android.R.color.black, null));
        btn3.setImageTintList(getResources().getColorStateList(android.R.color.black, null));
        btn4.setImageTintList(getResources().getColorStateList(android.R.color.black, null));
        int selectedColor = getResources().getColor(R.color.primary, null);
        switch (selectedItem) {
            case 0:
                btn1.setImageTintList(getResources().getColorStateList(R.color.primary, null));
                break;
            case 1:
                btn2.setImageTintList(getResources().getColorStateList(R.color.primary, null));
                break;
            case 2:
                btn3.setImageTintList(getResources().getColorStateList(R.color.primary, null));
                break;
            case 3:
                btn4.setImageTintList(getResources().getColorStateList(R.color.primary, null));
                break;
        }
    }
}
