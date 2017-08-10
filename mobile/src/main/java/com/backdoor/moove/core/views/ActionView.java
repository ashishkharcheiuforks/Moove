package com.backdoor.moove.core.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.utils.SuperUtil;
import com.backdoor.moove.core.utils.ViewUtils;

/**
 * Copyright 2015 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ActionView extends LinearLayout {

    public static final int TYPE_CALL = 1;
    public static final int TYPE_MESSAGE = 2;

    private Activity activity;
    private CheckBox actionCheck;
    private LinearLayout actionBlock;
    private RadioButton callAction, messageAction;
    private ImageButton selectNumber;
    private EditText numberView;
    private InputMethodManager imm;

    private OnActionListener listener;

    public ActionView(Context context) {
        super(context);
        init(context, null);
    }

    public ActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ActionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.action_view_layout, this);
        setOrientation(VERTICAL);

        actionBlock = findViewById(R.id.actionBlock);
        actionBlock.setVisibility(View.GONE);

        actionCheck = findViewById(R.id.actionCheck);
        actionCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ViewUtils.showOver(actionBlock);
                    selectNumber = findViewById(R.id.selectNumber);
                    selectNumber.setOnClickListener(contactClick);
                    selectNumber.setImageResource(R.drawable.ic_person_add_white_24dp);

                    numberView = findViewById(R.id.numberView);
                    numberView.setFocusableInTouchMode(true);
                    numberView.setOnFocusChangeListener(new OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            imm = (InputMethodManager) activity.getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                            if (!hasFocus) {
                                imm.hideSoftInputFromWindow(numberView.getWindowToken(), 0);
                            } else {
                                imm.showSoftInput(numberView, 0);
                            }
                        }
                    });
                    numberView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imm = (InputMethodManager) activity.getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                            if (!imm.isActive(numberView)) {
                                imm.showSoftInput(numberView, 0);
                            }
                        }
                    });

                    callAction = findViewById(R.id.callAction);
                    callAction.setChecked(true);
                    messageAction = findViewById(R.id.messageAction);
                    messageAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (listener != null) {
                                listener.onTypeChange(b);
                            }
                        }
                    });
                } else {
                    ViewUtils.hideOver(actionBlock);
                }
                if (listener != null) {
                    listener.onActionChange(b);
                }
            }
        });

        if (actionCheck.isChecked()) {
            ViewUtils.showOver(actionBlock);
        }
    }

    public void setListener(OnActionListener listener) {
        this.listener = listener;
    }

    public boolean hasAction() {
        return actionCheck.isChecked();
    }

    public void setAction(boolean action) {
        actionCheck.setChecked(action);
    }

    public void showError() {
        numberView.setError(activity.getString(R.string.empty_field));
    }

    public int getType() {
        if (hasAction()) {
            if (callAction.isChecked()) {
                return TYPE_CALL;
            } else {
                return TYPE_MESSAGE;
            }
        } else {
            return 0;
        }
    }

    public void setType(int type) {
        if (type == TYPE_CALL) {
            callAction.setChecked(true);
        } else {
            messageAction.setChecked(true);
        }
    }

    public String getNumber() {
        return numberView.getText().toString().trim();
    }

    public void setNumber(String number) {
        numberView.setText(number);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * Select contact button click listener.
     */
    private OnClickListener contactClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            SuperUtil.selectContact(activity, Constants.REQUEST_CODE_CONTACTS);
        }
    };

    public interface OnActionListener {
        void onActionChange(boolean b);

        void onTypeChange(boolean type);
    }
}
