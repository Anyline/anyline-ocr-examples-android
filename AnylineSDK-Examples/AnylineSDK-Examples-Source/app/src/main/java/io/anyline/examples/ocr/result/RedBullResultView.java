package io.anyline.examples.ocr.result;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import at.nineyards.anyline.util.DimensUtil;
import io.anyline.examples.R;

public class RedBullResultView extends RelativeLayout {

    private TextView resultText;

    public RedBullResultView(Context context) {
        super(context);
        init();
    }

    public RedBullResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RedBullResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {

        setPadding(DimensUtil.getPixFromDp(getContext(), 4), DimensUtil.getPixFromDp(getContext(), 16),
                DimensUtil.getPixFromDp(getContext(), 4), DimensUtil.getPixFromDp(getContext(), 16));

        setBackgroundResource(R.drawable.redbull_background);

        inflate(getContext(), R.layout.rb_result, this);

        resultText = (TextView) findViewById(R.id.text_result);
    }

    public void setResult(String result) {
        resultText.setText(result.trim());
    }
}
