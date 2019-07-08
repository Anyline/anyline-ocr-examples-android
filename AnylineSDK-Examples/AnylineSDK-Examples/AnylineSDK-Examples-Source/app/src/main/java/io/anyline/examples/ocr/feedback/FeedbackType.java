package io.anyline.examples.ocr.feedback;

import io.anyline.examples.R;

/**
 * Created by andrea on 30/05/16.
 */
public enum FeedbackType {
    SHAKY(R.drawable.shaky, R.string.feedback_shaky),
    TOO_BRIGHT(R.drawable.bright, R.string.feedback_too_bright),
    TOO_DARK(R.drawable.dark, R.string.feedback_too_dark),
    PERFECT(R.drawable.happy, R.string.feedback_perfect);

    private int iconId;
    private int stringId;

    FeedbackType(int iconId, int stringId) {
        this.iconId = iconId;
        this.stringId = stringId;
    }

    public int getIconId() {
        return iconId;
    }

    public int getStringId() {
        return stringId;
    }
}
