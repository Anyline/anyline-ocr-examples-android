package io.anyline.examples.barcode;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import io.anyline.examples.R;

public class CustomCheckbox extends androidx.appcompat.widget.AppCompatCheckBox {
    static private final int UNKNOW = -1;
    static private final int UNCHECKED = 0;
    static private final int CHECKED = 1;
    private int state;

    public CustomCheckbox(Context context) {
        super(context);
        init();
    }

    public CustomCheckbox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomCheckbox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        state = UNKNOW;
        updateBtn();

        setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            // checkbox status is changed from uncheck to checked.
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch (state) {
                    default:
                    case UNKNOW:
                        state = UNCHECKED;
                        break;
                    case UNCHECKED:
                        state = CHECKED;
                        break;
                    case CHECKED:
                        state = UNKNOW;
                        break;
                }
                updateBtn();
            }
        });
    }

    private void updateBtn() {
        int btnDrawable = R.drawable.ic_check;
        switch (state) {
            default:
            case UNKNOW:
                btnDrawable = R.drawable.ic_account_balance_24px;
                break;
            case UNCHECKED:
                btnDrawable = R.drawable.ic_anyline_24px;
                break;
            case CHECKED:
                btnDrawable = R.drawable.ic_check;
                break;
        }

        setButtonDrawable(btnDrawable);
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        updateBtn();
    }
}
