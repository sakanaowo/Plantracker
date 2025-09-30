package com.example.tralalero.feature.home.ui.BoardDetail;

public class BoardPage {
    private String title;
    private boolean isAddList; // nếu true thì trang này là "Add list"

    public BoardPage(String title, boolean isAddList) {
        this.title = title;
        this.isAddList = isAddList;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAddList() {
        return isAddList;
    }
}
