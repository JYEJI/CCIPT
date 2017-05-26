package com.example.user.project_ccipt;

import android.graphics.Bitmap;

/**
 * Created by user on 2017-05-21.
 */

public class TeamMemberListViewItem {
    private Bitmap iconDrawable ;
    private String titleStr ;

    public void setIcon(Bitmap icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }

    public Bitmap getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
}
