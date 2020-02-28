package com.stomhong.library;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class KeyboardUtil {

    private       Context        mContext;
    private       Activity       mActivity;
    private       PpKeyBoardView keyboardView;
    static        Keyboard       numKeyboard;// 数字键盘
    public static Keyboard       keyboard;//提供给keyboardView 进行画

    public  boolean                     isShow = false;
    private InputFinishListener         inputOver;
    private KeyBoardStateChangeListener keyBoardStateChangeListener;
    private View                        layoutView;
    private View                        keyBoardLayout;

    // 开始输入的键盘状态设置
    private static int inputType = 1;// 默认

    public static final int INPUTTYPE_NUM_X = 4; // 数字，右下角 为X

    private static final int KEYBOARD_SHOW = 1;
    private static final int KEYBOARD_HIDE = 2;

    private EditText   ed;
    private Handler    mHandler;
    private ScrollView sv_main;
    private View       root_view;
    private int        scrollTo = 0;

    /**
     * 最新构造方法，现在都用这个
     *
     * @param rootView rootView 需要是LinearLayout,以适应键盘
     */
    public KeyboardUtil(Context ctx, LinearLayout rootView, ScrollView scrollView) {
        this.mContext = ctx;
        this.mActivity = (Activity) mContext;
        initKeyBoardView(rootView);
        initScrollHandler(rootView, scrollView);
    }

    //设置监听事件
    public void setInputOverListener(InputFinishListener listener) {
        this.inputOver = listener;
    }

    static Keyboard getKeyBoardType() {
        return keyboard;
    }

    private void initKeyBoardView(LinearLayout rootView) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        keyBoardLayout = inflater.inflate(R.layout.input, null);

        keyBoardLayout.setVisibility(View.GONE);
        keyBoardLayout.setBackgroundColor(mActivity.getResources().getColor(R.color.product_list_bac));
        initLayoutHeight((LinearLayout) keyBoardLayout);
        this.layoutView = keyBoardLayout;
        rootView.addView(keyBoardLayout);

        if (keyBoardLayout != null && keyBoardLayout.getVisibility() == View.VISIBLE)
            Log.d("KeyboardUtil", "visible");
    }

    private void initLayoutHeight(LinearLayout layoutView) {
        LinearLayout.LayoutParams keyboard_layoutlLayoutParams = (LinearLayout.LayoutParams) layoutView
                .getLayoutParams();
        RelativeLayout TopLayout = layoutView.findViewById(R.id.keyboard_view_top_rl);
        ImageView IVClose = layoutView.findViewById(R.id.iv_close);
        IVClose.setOnClickListener(new finishListener());
        if (keyboard_layoutlLayoutParams == null) {
            int height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * SIZE.KEYBOARY_H);
            layoutView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, height));
        } else {
            keyboard_layoutlLayoutParams.height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * SIZE.KEYBOARY_H);
        }

        LinearLayout.LayoutParams TopLayoutParams = (LinearLayout.LayoutParams) TopLayout
                .getLayoutParams();

        if (TopLayoutParams == null) {
            int height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * SIZE.KEYBOARY_T_H);
            TopLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, height));
        } else {
            TopLayoutParams.height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * SIZE.KEYBOARY_T_H);
        }
    }

    boolean setKeyBoardCursorNew(EditText edit) {
        this.ed = edit;
        boolean flag = false;

        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return false;
        }
        boolean isOpen = imm.isActive();// isOpen若返回true，则表示输入法打开
        if (isOpen) {
            if (imm.hideSoftInputFromWindow(edit.getWindowToken(), 0))
                flag = true;
        }

        int currentVersion = android.os.Build.VERSION.SDK_INT;
        String methodName = null;
        if (currentVersion >= 16) {
            // 4.2
            methodName = "setShowSoftInputOnFocus";
        } else if (currentVersion >= 14) {
            // 4.0
            methodName = "setSoftInputShownOnFocus";
        }

        if (methodName == null) {
            edit.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method setShowSoftInputOnFocus;
            try {
                setShowSoftInputOnFocus = cls.getMethod(methodName,
                        boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(edit, false);
            } catch (NoSuchMethodException e) {
                edit.setInputType(InputType.TYPE_NULL);
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return flag;
    }

    public void hideSystemKeyBoard() {
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null)
            return;
        imm.hideSoftInputFromWindow(keyBoardLayout.getWindowToken(), 0);
    }

    public void hideAllKeyBoard() {
        hideSystemKeyBoard();
        hideKeyboardLayout();
    }

    private boolean getKeyboardState() {
        return this.isShow;
    }

    EditText getEd() {
        return ed;
    }

    //初始化滑动handler
    @SuppressLint("HandlerLeak")
    private void initScrollHandler(View rootView, ScrollView scrollView) {
        this.sv_main = scrollView;
        this.root_view = rootView;
        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == ed.getId()) {
                    if (sv_main != null)
                        sv_main.smoothScrollTo(0, scrollTo);
                }
            }
        };
    }

    //滑动监听
    private void keyBoardScroll(final EditText editText, int scorllTo) {
        this.scrollTo = scorllTo;
        ViewTreeObserver vto_bighexagon = root_view.getViewTreeObserver();
        vto_bighexagon.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Message msg = new Message();
                msg.what = editText.getId();
                mHandler.sendMessageDelayed(msg, 500);
                // // 防止多次促发
                root_view.getViewTreeObserver()
                        .removeGlobalOnLayoutListener(this);
            }
        });
    }

    //设置一些不需要使用这个键盘的edittext,解决切换问题
    public void setOtherEdittext(EditText... edittexts) {
        for (EditText editText : edittexts) {
            editText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        //防止没有隐藏键盘的情况出现
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideKeyboardLayout();
                            }
                        }, 300);
                        ed = (EditText) v;
                        hideKeyboardLayout();
                    }
                    return false;
                }
            });
        }
    }

    class finishListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            hideKeyboardLayout();
        }
    }


    private OnKeyboardActionListener listener = new OnKeyboardActionListener() {
        @Override
        public void swipeUp() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void onText(CharSequence text) {
            if (ed == null)
                return;
            Editable editable = ed.getText();
            int start = ed.getSelectionStart();
            String temp = editable.subSequence(0, start) + text.toString() + editable.subSequence(start, editable.length());
            ed.setText(temp);
            Editable etext = ed.getText();
            Selection.setSelection(etext, start + 1);
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onPress(int primaryCode) {
            keyboardView.setPreviewEnabled(false);
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            Editable editable = ed.getText();
            int start = ed.getSelectionStart();
            if (primaryCode == Keyboard.KEYCODE_DELETE) {// 回退
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
            } else if(primaryCode == 112){
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(0, start);
                    }
                }
            }else {
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }
    };

    private void showKeyboard() {
        if (keyboardView != null) {
            keyboardView.setVisibility(View.GONE);
        }
        initInputType();
        isShow = true;
        keyboardView.setVisibility(View.VISIBLE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initKeyBoard(int keyBoardViewID) {
        mActivity = (Activity) mContext;
        keyboardView = mActivity
                .findViewById(keyBoardViewID);

        keyboardView.setEnabled(true);
        keyboardView.setOnKeyboardActionListener(listener);
        keyboardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getAction() == MotionEvent.ACTION_MOVE;
            }
        });
    }

    private void initInputType() {
        if (inputType == INPUTTYPE_NUM_X) {
            initKeyBoard(R.id.keyboard_view);
            keyboardView.setPreviewEnabled(false);
            numKeyboard = new Keyboard(mContext, R.xml.symbols_x);
            setMyKeyBoard(numKeyboard);
        }
    }

    private void setMyKeyBoard(Keyboard newkeyboard) {
        keyboard = newkeyboard;
        keyboardView.setKeyboard(newkeyboard);
    }

    //新的隐藏方法
    public void hideKeyboardLayout() {
        if (getKeyboardState()) {
            if (keyBoardLayout != null)
                keyBoardLayout.setVisibility(View.GONE);
            if (keyBoardStateChangeListener != null)
                keyBoardStateChangeListener.KeyBoardStateChange(KEYBOARD_HIDE, ed);
            isShow = false;
            hideKeyboard();
            ed = null;
        }
    }

    //新的show方法
    void showKeyBoardLayout(final EditText editText, int keyBoardType, int scrollTo) {
        if (editText.equals(ed) && getKeyboardState() && inputType == keyBoardType)
            return;

        inputType = keyBoardType;
        this.scrollTo = scrollTo;

        if (setKeyBoardCursorNew(editText)) {
            Handler showHandler = new Handler();
            showHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    show(editText);
                }
            }, 400);
        } else {
            //直接显示
            show(editText);
        }
    }

    private void show(EditText editText) {
        this.ed = editText;
        if (keyBoardLayout != null)
            keyBoardLayout.setVisibility(View.VISIBLE);
        showKeyboard();
        if (keyBoardStateChangeListener != null)
            keyBoardStateChangeListener.KeyBoardStateChange(KEYBOARD_SHOW, editText);
        //用于滑动
        if (scrollTo >= 0) {
            keyBoardScroll(editText, scrollTo);
        }
    }

    private void hideKeyboard() {
        isShow = false;
        if (keyboardView != null) {
            int visibility = keyboardView.getVisibility();
            if (visibility == View.VISIBLE) {
                keyboardView.setVisibility(View.INVISIBLE);
            }
        }
        if (layoutView != null) {
            layoutView.setVisibility(View.GONE);
        }
    }

    public interface InputFinishListener {
        void inputHasOver(int onclickType, EditText editText);
    }

    /**
     * 监听键盘变化
     */
    public interface KeyBoardStateChangeListener {
        void KeyBoardStateChange(int state, EditText editText);
    }

    public void setKeyBoardStateChangeListener(KeyBoardStateChangeListener listener) {
        this.keyBoardStateChangeListener = listener;
    }

}
