package com.stomhong.library;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class KeyboardUtil {

    private Context mContext;
    private Activity mActivity;
    private PpKeyBoardView keyboardView;
    private EditText mEditText;
    static Keyboard numKeyboard;// 数字键盘
    private static Keyboard keyboard;//提供给keyboardView 进行画

    public boolean isShow = false;
    private KeyBoardStateChangeListener keyBoardStateChangeListener;
    private View layoutView;
    private View keyBoardLayout;


    private static final int KEYBOARD_SHOW = 1;
    private static final int KEYBOARD_HIDE = 2;

    private EditText ed;

    /**
     * 最新构造方法，现在都用这个
     *
     * @param rootView rootView 需要是LinearLayout,以适应键盘
     */
    public KeyboardUtil(Context ctx, LinearLayout rootView) {
        this.mContext = ctx;
        this.mActivity = (Activity) mContext;
        initKeyBoardView(rootView);
    }

    static Keyboard getKeyBoardType() {
        return keyboard;
    }

    private void initKeyBoardView(LinearLayout rootView) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        keyBoardLayout = inflater.inflate(R.layout.input, null);
        mEditText = keyBoardLayout.findViewById(R.id.edit_text);
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
        if (keyboard_layoutlLayoutParams == null) {
            int height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * SIZE.KEYBOARY_H);
            layoutView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, height));
        } else {
            keyboard_layoutlLayoutParams.height = (int) (mActivity.getResources().getDisplayMetrics().heightPixels * SIZE.KEYBOARY_H);
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
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
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
            } else if (primaryCode == 88) {
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(0, start);
                    }
                }
            } else {
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
        keyboardView.setOnTouchListener((v, event) -> event.getAction() == MotionEvent.ACTION_MOVE);
    }

    private void initInputType() {
        initKeyBoard(R.id.keyboard_view);
        keyboardView.setPreviewEnabled(false);
        numKeyboard = new Keyboard(mContext, R.xml.symbols_x);
        setMyKeyBoard(numKeyboard);
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
    public void showKeyBoardLayout() {
        if (getKeyboardState())
            return;

        if (setKeyBoardCursorNew(mEditText)) {
            Handler showHandler = new Handler();
            showHandler.postDelayed(() -> show(mEditText), 400);
        } else {
            //直接显示
            show(mEditText);
        }
    }

    private void show(EditText editText) {
        this.ed = editText;
        if (keyBoardLayout != null)
            keyBoardLayout.setVisibility(View.VISIBLE);
        showKeyboard();
        if (keyBoardStateChangeListener != null)
            keyBoardStateChangeListener.KeyBoardStateChange(KEYBOARD_SHOW, editText);
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
