package com.example.tralalero.feature.home.ui;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.tralalero.R;
public class BottomNavigationFragment extends Fragment {
    private int selectedItem = 0;
    private ImageButton btn1, btn2, btn3, btn4;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_navigation, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        btn1 = view.findViewById(R.id.btn1);
        btn2 = view.findViewById(R.id.btn2);
        btn3 = view.findViewById(R.id.btn3);
        btn4 = view.findViewById(R.id.btn4);
        
        // Get ViewPager from MainContainerActivity
        MainContainerActivity activity = (MainContainerActivity) getActivity();
        if (activity == null) return;
        
        ViewPager2 viewPager = activity.findViewById(R.id.viewPager);
        if (viewPager == null) return;
        
        // Setup click listeners to change ViewPager page
        btn1.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        btn2.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        btn3.setOnClickListener(v -> viewPager.setCurrentItem(2, true));
        btn4.setOnClickListener(v -> viewPager.setCurrentItem(3, true));
        
        // Listen to ViewPager page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                selectedItem = position;
                updateSelection();
            }
        });
        
        // Set initial selection
        selectedItem = viewPager.getCurrentItem();
        updateSelection();
    }
    private void updateSelection() {
        if (btn1 == null || btn2 == null || btn3 == null || btn4 == null) return;
        
        // Reset all to black
        btn1.setImageTintList(getResources().getColorStateList(android.R.color.black, null));
        btn2.setImageTintList(getResources().getColorStateList(android.R.color.black, null));
        btn3.setImageTintList(getResources().getColorStateList(android.R.color.black, null));
        btn4.setImageTintList(getResources().getColorStateList(android.R.color.black, null));
        
        // Highlight selected
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
