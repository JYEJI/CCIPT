package com.example.user.project_ccipt;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by user on 2017-05-12.
 */

public class ListViewItem {
    private Bitmap iconDrawable ;
    private String titleStr ;
    private String descStr ;

    public void setIcon(Bitmap icon) {
        iconDrawable = icon ;
    }
    public void setTitle(String title) {
        titleStr = title ;
    }
    public void setDesc(String desc) {
        descStr = desc ;
    }

    public Bitmap getIcon() {
        return this.iconDrawable ;
    }
    public String getTitle() {
        return this.titleStr ;
    }
    public String getDesc() {
        return this.descStr ;
    }
}
