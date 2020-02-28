package com.stomhong.library;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

public class KeyboardTouchListener implements View.OnTouchListener {
    private KeyboardUtil keyboardUtil;

    public KeyboardTouchListener(KeyboardUtil util) {
        this.keyboardUtil = util;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (keyboardUtil != null) {
                keyboardUtil.setKeyBoardCursorNew((EditText) v);
            }
        }
        return false;
    }
}
