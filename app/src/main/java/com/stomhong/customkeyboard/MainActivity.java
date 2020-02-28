package com.stomhong.customkeyboard;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.stomhong.library.KeyboardTouchListener;
import com.stomhong.library.KeyboardUtil;

public class MainActivity extends AppCompatActivity {

    private LinearLayout rootView;
    private ScrollView scrollView;
    private EditText normalEd;
    private EditText specialEd;
    private KeyboardUtil keyboardUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootView = (LinearLayout) findViewById(R.id.root_view);
        scrollView = (ScrollView) findViewById(R.id.sv_main);

        normalEd = (EditText) findViewById(R.id.normal_ed);
        specialEd = (EditText) findViewById(R.id.special_ed);

        initMoveKeyBoard();


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0 ) {
            if(keyboardUtil.isShow){
                keyboardUtil.hideSystemKeyBoard();
                keyboardUtil.hideAllKeyBoard();
                keyboardUtil.hideKeyboardLayout();
            }else {
                return super.onKeyDown(keyCode, event);
            }

            return false;
        } else
            return super.onKeyDown(keyCode, event);
    }

    private void initMoveKeyBoard() {
        keyboardUtil = new KeyboardUtil(this,rootView);
        keyboardUtil.showKeyBoardLayout();
    }



}