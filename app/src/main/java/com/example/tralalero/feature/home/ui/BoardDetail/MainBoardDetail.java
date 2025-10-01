package com.example.tralalero.feature.home.ui.BoardDetail;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tralalero.R;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;

public class MainBoardDetail extends AppCompatActivity {
    private BoardPageAdapter adapter;
    private List<BoardPage> pages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_detail);

        ViewPager2 viewPager = findViewById(R.id.board_viewPager);
        DotsIndicator indicator = findViewById(R.id.board_indicator);

        // Khởi tạo: chỉ có 1 trang "Add list"
        pages = new ArrayList<>();
        pages.add(new BoardPage("Add list", true));

        adapter = new BoardPageAdapter(this, pages);
        viewPager.setAdapter(adapter);

        indicator.setViewPager2(viewPager);
    }
}

