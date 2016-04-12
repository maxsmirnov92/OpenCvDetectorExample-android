package ru.maxsmr.opencv.detectorexample.gui.progressable;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.maxsmr.opencv.detectorexample.R;

public class DialogProgressable implements Progressable, DialogInterface.OnKeyListener, DialogInterface.OnDismissListener, DialogInterface.OnCancelListener {

    protected static final Logger logger = LoggerFactory.getLogger(DialogProgressable.class);

    private final Context context;
    private ProgressDialog progressDialog;

    //    private final String defaultTitle;
    private String title;

    //    private final String defaultMessage;
    private String message;


    private boolean indeterminate = true;
    private int max = 0;

    private boolean cancelable = false;

    private OnBackPressedListener backPressedListener;
    private DialogInterface.OnCancelListener cancelListener;
    private DialogInterface.OnDismissListener dismissListener;


    public DialogProgressable(@NonNull Context context) {
        this(context, "", context.getString(R.string.loading), true, 0, false);
    }

    public DialogProgressable(@NonNull Context context, boolean indeterminate, int max, boolean cancelable) {
        this(context, null, null, indeterminate, max, cancelable);
    }

    public DialogProgressable(@NonNull Context context, @Nullable String title, @Nullable String message, boolean indeterminate, int max, boolean cancelable) {
        this.context = context;
//        this.defaultTitle = "";
//        this.defaultMessage = context.getString(R.string.loading);
//        title = TextUtils.isEmpty(title)? defaultTitle : title;
//        message = TextUtils.isEmpty(message)? defaultMessage : message;
        setTitle(title);
        setMessage(message);
        setIndeterminate(indeterminate, max);
        setCancelable(cancelable);
    }

    public boolean isStarted() {
        return progressDialog != null && progressDialog.isShowing();
    }

    private void begin() {
        if (!isStarted()) {
            progressDialog = new ProgressDialog(context);
            if (!indeterminate) {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            } else {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            }
            progressDialog.setIndeterminate(indeterminate);
            progressDialog.setMax(max);
            progressDialog.setCancelable(cancelable);
            progressDialog.setOnKeyListener(this);
            progressDialog.setOnCancelListener(this);
            progressDialog.setOnDismissListener(this);
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    private void end() {
        dismiss();
    }

    public void notifyProgress(int progress) {
        logger.debug("notifyProgress(), progress=" + progress);
        if (isStarted()) {
            if (!progressDialog.isIndeterminate()) {
                if (progress >= 0 && progress <= progressDialog.getMax()) {
                    progressDialog.setProgress(progress);
                } else {
                    throw new IllegalArgumentException("incorrect progress value: " + progress + " (max: " + progressDialog.getMax());
                }
            }
        }
    }

    public boolean isIndeterminate() {
        return indeterminate;
    }


    public int getMax() {
        return max;
    }

    public DialogProgressable setIndeterminate(boolean indeterminate, int max) {
        this.indeterminate = indeterminate;
        this.max = max < 0 ? 0 : max;
        if (isStarted()) {
            progressDialog.setIndeterminate(indeterminate);
            if (!indeterminate) {
                progressDialog.setMax(max);
            }
        }
        return this;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public DialogProgressable setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        if (isStarted()) {
            progressDialog.setCancelable(cancelable);
        }
        return this;
    }

    public boolean cancel() {
        if (isStarted()) {
            if (cancelable) {
                progressDialog.cancel();
                return true;
            }
        }
        return false;
    }

    public String getTitle() {
        return title;
    }

    public DialogProgressable setTitle(String title) {
        this.title = title;
        if (isStarted()) {
            progressDialog.setTitle(title);
        }
        return this;
    }

    public String getMessage() {
        return message;
    }

    public DialogProgressable setMessage(String message) {
        this.message = message;
        if (isStarted()) {
            progressDialog.setMessage(message);
        }
        return this;
    }

    public DialogProgressable setOnBackPressedListener(OnBackPressedListener listener) {
        this.backPressedListener = listener;
        return this;
    }

    public DialogProgressable setOnCancelListener(DialogInterface.OnCancelListener listener) {
        this.cancelListener = listener;
        return this;
    }

    public DialogProgressable setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.dismissListener = listener;
        return this;
    }

    private void dismiss() {
        if (isStarted()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    @MainThread
    public void onStart() {
        begin();
    }

    @Override
    @MainThread
    public void onStop() {
        end();
    }

    public interface OnBackPressedListener {
        void onBackPressed(DialogInterface dialog);
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        logger.debug("onKey(), dialog=" + dialog + ", keyCode=" + keyCode + ", event=" + event);
        if (keyCode == KeyEvent.KEYCODE_BACK && dialog != null) {
            if (backPressedListener != null) {
                backPressedListener.onBackPressed(dialog);
            }
        }
        return false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        logger.debug("onDismiss(), dialog=" + dialog);
        if (dismissListener != null) {
            dismissListener.onDismiss(dialog);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        logger.debug("onCancel(), dialog=" + dialog);
        if (cancelListener != null) {
            cancelListener.onCancel(dialog);
        }
    }
}