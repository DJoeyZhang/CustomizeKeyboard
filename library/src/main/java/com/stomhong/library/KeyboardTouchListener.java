package com.stomhong.library;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class KeyboardTouchListener implements View.OnTouchListener {
    private KeyboardUtil keyboardUtil;
    private int keyboardType;
    private int scrollTo;

    public KeyboardTouchListener(KeyboardUtil util,int keyboardType,int scrollTo){
        this.keyboardUtil = util;
        this.keyboardType = keyboardType;
        this.scrollTo = scrollTo;
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (keyboardUtil != null && keyboardUtil.getEd() !=null &&v.getId() != keyboardUtil.getEd().getId())
                keyboardUtil.showKeyBoardLayout((EditText) v,keyboardType,scrollTo);
            else if(keyboardUtil != null && keyboardUtil.getEd() ==null){
                keyboardUtil.showKeyBoardLayout((EditText) v,keyboardType,scrollTo);
            }else{
                    if (keyboardUtil != null) {
                        keyboardUtil.setKeyBoardCursorNew((EditText) v);
                }
            }
        }
        return false;
    }
}
