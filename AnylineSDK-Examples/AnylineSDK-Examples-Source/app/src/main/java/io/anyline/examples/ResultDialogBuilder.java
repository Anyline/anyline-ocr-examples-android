/*
 * Anyline
 * ResultDialogBuilder.java
 *
 * Copyright (c) 2015 9yards GmbH
 *
 * Created by martin at 2015-07-03
 */

package io.anyline.examples;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import at.nineyards.anyline.models.AnylineImage;

/**
 * A Simple Dialog Builder to display scan results.
 */
public class ResultDialogBuilder extends AlertDialog.Builder {

    private ImageView imageView;
    private TextView textView;

    private ImageView secondImageView;
    private TextView secondTextView;

    public ResultDialogBuilder(Context context) {
        super(context);
        init();
    }

    public ResultDialogBuilder(Context context, int theme) {
        super(context, theme);
        init();
    }

    public ResultDialogBuilder(Context context, Boolean isDoubleResult) {
        super(context);
        if (isDoubleResult) {
            initDoubleResult();
        } else {
            init();
        }
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_result, null);

        imageView = (ImageView) view.findViewById(R.id.image);
        textView = (TextView) view.findViewById(R.id.text);


        setView(view);
    }

    private void initDoubleResult() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_double_result, null);

        imageView = (ImageView) view.findViewById(R.id.first_image);
        textView = (TextView) view.findViewById(R.id.first_text);

        secondImageView = (ImageView) view.findViewById(R.id.second_image);
        secondTextView = (TextView) view.findViewById(R.id.second_text);


        setView(view);
    }

    public ResultDialogBuilder setResultImage(AnylineImage resultImage) {
        imageView.setImageBitmap(resultImage.getBitmap());
        return this;
    }

    public ResultDialogBuilder setSecondResultImage(AnylineImage secondResultImage) {
        secondImageView.setImageBitmap(secondResultImage.getBitmap());
        return this;
    }

    public ResultDialogBuilder setText(CharSequence resultText) {
        textView.setText(resultText);
        return this;
    }

    public ResultDialogBuilder setSecondText(CharSequence secondResultText) {
        secondTextView.setText(secondResultText);
        return this;
    }

    public ResultDialogBuilder setTextSize(int unit, float size) {
        textView.setTextSize(unit, size);
        return this;
    }

    public ResultDialogBuilder setTextGravity(int gravity) {
        textView.setGravity(gravity);
        if (secondTextView != null) {
            secondTextView.setGravity(gravity);
        }
        return this;
    }

}
