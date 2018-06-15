package io.anyline.examples.dialog;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import io.anyline.examples.R;


public class SingleMessageDialog extends Dialog {

    protected TextView textView;

    protected ImageView imageView;

    private int messageResource;
    private int iconRessource;
    private int autoDismissDuration;

    private Runnable autoDismissTask;
    private Handler handler = new Handler();

    public SingleMessageDialog(Context context, @StringRes int textRes) {
        super(context);
        messageResource = textRes;
    }

    public SingleMessageDialog(Context context, @StringRes int textRes, @DrawableRes int iconRes) {
        super(context);
        messageResource = textRes;
        iconRessource = iconRes;
    }

    public void setAutoDismissMs(int autoDismissMs) {
        this.autoDismissDuration = autoDismissMs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_single_message);
        textView = (TextView) findViewById(R.id.text_view);
        imageView = (ImageView) findViewById(R.id.image_view);

        textView.setText(messageResource);
        imageView.setImageResource(iconRessource);
        imageView.setVisibility(iconRessource > 0 ? View.VISIBLE : View.GONE);

        setupAutoDismiss();
    }

    private void setupAutoDismiss() {
        if(autoDismissDuration == 0)
            return ;

        if(autoDismissTask == null) {
            autoDismissTask = new Runnable() {

                @Override
                public void run() {

                    if (isShowing())
                        dismiss();
                }
            };
        }

        handler.postDelayed(autoDismissTask, autoDismissDuration);
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(autoDismissTask);
        super.onStop();
    }
}
