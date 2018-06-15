package io.anyline.examples.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import io.anyline.examples.R;

/**
 * A simple alert dialog that uses a custom view-style
 * Consists of a title, message, header, positive and negative button
 */
public class SimpleAlertDialog extends android.support.v7.app.AlertDialog.Builder {
    private Context context;
    private ImageView icon;
    private TextView title;
    private TextView message;
    private Button positiveButton;
    private Button negativeButton;

    public SimpleAlertDialog(Context context) {
        this(context, R.style.DialogStyle);
    }

    public SimpleAlertDialog(Context context, int themeResId) {
        super(context, R.style.DialogStyle);
        init();
    }


    private void init() {

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_simple, null);

        icon = (ImageView) view.findViewById(R.id.icon);
        title = (TextView) view.findViewById(R.id.title);
        message = (TextView) view.findViewById(R.id.message);
        positiveButton = (Button) view.findViewById(R.id.positive);
        negativeButton = (Button) view.findViewById(R.id.negative);

        negativeButton.setVisibility(View.GONE);
        title.setVisibility(View.GONE);
        setView(view);
    }

    @Override
    public SimpleAlertDialog setIcon(Drawable icon) {
        if (icon == null) {
            this.icon.setVisibility(View.GONE);
        } else {
            this.icon.setVisibility(View.VISIBLE);
        }
        this.icon.setImageDrawable(icon);
        return this;
    }

    public SimpleAlertDialog setNegative(String string, View.OnClickListener listener) {
        this.negativeButton.setVisibility(View.VISIBLE);
        this.negativeButton.setText(string);
        this.negativeButton.setOnClickListener(listener);
        return this;
    }

    public SimpleAlertDialog setPositive(String string, View.OnClickListener listener) {
        this.positiveButton.setText(string);
        this.positiveButton.setOnClickListener(listener);
        return this;
    }

    public SimpleAlertDialog setMessage(String string) {
        this.message.setText(string);
        return this;
    }

    public SimpleAlertDialog setMessage(CharSequence sequence) {
        this.message.setText(sequence);
        return this;
    }

    public SimpleAlertDialog setTitle(String string) {
        this.title.setVisibility(View.VISIBLE);
        this.title.setText(string);
        return this;
    }


    /**
     * Sets the size of the message text (calls {@link TextView#setTextSize(int, float)}
     *
     * @param unit
     * @param size
     */
    public void setMessageTextSize(int unit, int size) {
        this.message.setTextSize(unit, size);
    }
}