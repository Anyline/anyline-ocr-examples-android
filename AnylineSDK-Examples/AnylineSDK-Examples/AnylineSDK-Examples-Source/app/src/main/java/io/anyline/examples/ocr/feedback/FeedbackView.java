package io.anyline.examples.ocr.feedback;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import at.nineyards.anyline.util.DimensUtil;
import io.anyline.examples.R;

/**
 * Created by andrea on 30/05/16.
 */
public class FeedbackView extends RelativeLayout {

    private static final String TAG = FeedbackView.class.getSimpleName();
    public static final int FEEDBACK_TIMEOUT = 600;

    private ImageView feedbackIcon;
    private TextView feedbackText;
    private FeedbackType feedbackType = FeedbackType.PERFECT;
    boolean animationInProgress;
    private long feedbackTimestamp;

    public FeedbackView(Context context) {
        super(context);
        initView();
    }

    public FeedbackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FeedbackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.view_feedback, this);
        setPadding(DimensUtil.getPixFromDp(getContext(), 4), DimensUtil.getPixFromDp(getContext(), 16),
                DimensUtil.getPixFromDp(getContext(), 4), DimensUtil.getPixFromDp(getContext(), 16));

        feedbackIcon = (ImageView) findViewById(R.id.feedback_icon);
        feedbackText = (TextView) findViewById(R.id.feedback_text);

        this.setVisibility(View.INVISIBLE);
    //    setFeedbackType(FeedbackType.PERFECT);

    }

    /**
     * @param cutoutRect     cutoutRect of ScanView
     * @param watermarkRect  watermarkRect of ScanView
     * @param scanViewHeight height of ScanView
     * @return calculated y position for top border of the feedbackView
     */
    public int calculateYPosition(Rect cutoutRect, Rect watermarkRect, int scanViewHeight) {
        if (cutoutRect == null || scanViewHeight <= 0) {
            return 0;
        }

        int yPosition;

        // place FeedbackView below cutout (and watermark) if there is enough space
        yPosition = Math.max(cutoutRect.bottom, watermarkRect == null ? 0 : watermarkRect.bottom);
        if (yPosition + this.getMeasuredHeight() <= scanViewHeight) {
            return yPosition;
        }

        // place FeedbackView above cutout (and watermark) if there is enough space
        yPosition = Math.min(cutoutRect.top, watermarkRect == null ? Integer.MAX_VALUE : watermarkRect.top);
        if (yPosition - this.getMeasuredHeight() >= 0) {
            return yPosition - this.getMeasuredHeight();
        }

        // place FeedbackView in center of cutoutView if there is not enough space outside of cutoutView
        yPosition = (cutoutRect.bottom - cutoutRect.top) / 2 - this.getMeasuredHeight() / 2;
        return yPosition;
    }

    public void setFeedbackType(FeedbackType feedbackType) {

        if (!animationInProgress && System.currentTimeMillis() - feedbackTimestamp > FEEDBACK_TIMEOUT) {
            // if feedback type changed
            if (this.feedbackType != feedbackType) {
                // check if previous feedback type was SHAKY and current feedback type is PERFECT (from onReports $brightness)
                // only update feedbackView if this is not the case -- update of feedback type SHAKY is done in else ***
                if (!(this.feedbackType == FeedbackType.SHAKY && feedbackType == FeedbackType.PERFECT)) {
                    feedbackIcon.setImageResource(feedbackType.getIconId());
                    feedbackText.setText(feedbackType.getStringId());
                    this.setVisibility(VISIBLE);
                    feedbackTimestamp = System.currentTimeMillis();
                }
                this.feedbackType = feedbackType;
            }
            // if feedback type did not change
            else {
                if (this.feedbackType == FeedbackType.PERFECT) {
                    // *** needed to reset shaky feedback, since there is no callback for that (use onReports $brightness)
                    feedbackIcon.setImageResource(feedbackType.getIconId());
                    feedbackText.setText(feedbackType.getStringId());
                    feedbackTimestamp = System.currentTimeMillis();

                    animationInProgress = true;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animationInProgress = false;
                            FeedbackView.this.setVisibility(INVISIBLE);
                        }
                    }, 600);
                }
            }
        }

    }

}
