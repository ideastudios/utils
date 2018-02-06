package tech.oom.julian.custom.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import tech.oom.julian.utils.R;


/**
 * 警告框式Dialog [ 标题 + 消息文本 + 左按钮 + 右按钮 ]
 *
 * @author maple
 * @time 17/3/21
 */
public class AlertDialog extends BaseDialog {
    private TextView txt_title;
    private TextView txt_msg;
    private Button leftButton;
    private Button rightButton;
    private View img_line;

    private boolean showTitle = false;
    private boolean showMsg = false;
    private boolean showRightBtn = false;
    private boolean showLeftBtn = false;


    public AlertDialog(Context context) {
        super(context);
        rootView = LayoutInflater.from(context).inflate(R.layout.view_alert_dialog, null);

        // get custom Dialog layout
        txt_title = (TextView) rootView.findViewById(R.id.txt_title);
        txt_title.setVisibility(View.GONE);
        txt_msg = (TextView) rootView.findViewById(R.id.txt_msg);
        txt_msg.setVisibility(View.GONE);
        leftButton = (Button) rootView.findViewById(R.id.bt_left);
        leftButton.setVisibility(View.GONE);
        rightButton = (Button) rootView.findViewById(R.id.bt_right);
        rightButton.setVisibility(View.GONE);
        img_line = (View) rootView.findViewById(R.id.img_line);
        img_line.setVisibility(View.GONE);

        // set Dialog style
        dialog = new Dialog(context, R.style.AlertDialogStyle);
        dialog.setContentView(rootView);

        setScaleWidth(0.72);
    }

    public AlertDialog setCancelable(boolean cancel) {
        dialog.setCancelable(cancel);
        return this;
    }

    public AlertDialog setScaleWidth(double scWidth) {
        return (AlertDialog) super.setScaleWidth(scWidth);
    }

    public AlertDialog setTitle(String title) {
        return setTitle(title, Color.parseColor("#000000"));
    }

    public AlertDialog setTitle(String title, @ColorInt int color) {

        if (TextUtils.isEmpty(title)) {

        } else {
            txt_title.setText(title);
            showTitle = true;
        }
        txt_title.setTextColor(color);
        return this;
    }

    public AlertDialog setMsg(String msg) {
        return setMsg(msg, Color.parseColor("#000000"));
    }

    public AlertDialog setMsg(String msg, @ColorInt int color) {

        if (TextUtils.isEmpty(msg)) {

        } else {
            showMsg = true;
            txt_msg.setText(msg);
        }
        txt_msg.setTextColor(color);
        return this;
    }

    public AlertDialog setRightButton(String text, final OnClickListener listener) {
        showRightBtn = true;
        if (TextUtils.isEmpty(text)) {
            rightButton.setText("OK");
        } else {
            rightButton.setText(text);
        }
        rightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onClick(v);
                dialog.dismiss();
            }
        });
        return this;
    }

    public AlertDialog setLeftButton(String text, final OnClickListener listener) {
        showLeftBtn = true;
        if (TextUtils.isEmpty(text)) {
            leftButton.setText("Cancel");
        } else {
            leftButton.setText(text);
        }
        leftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onClick(v);
                dialog.dismiss();
            }
        });
        return this;
    }

    private void setLayout() {
        if (!showTitle && !showMsg) {
            txt_title.setText("Alert");
            txt_title.setVisibility(View.VISIBLE);
        }

        if (showTitle) {
            txt_title.setVisibility(View.VISIBLE);
        }

        if (showMsg) {
            txt_msg.setVisibility(View.VISIBLE);
        }

        if (!showRightBtn && !showLeftBtn) {
            rightButton.setText("OK");
            rightButton.setVisibility(View.VISIBLE);
            rightButton.setBackgroundResource(R.drawable.alertdialog_single_selector);
            rightButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        if (showRightBtn && showLeftBtn) {
            rightButton.setVisibility(View.VISIBLE);
            rightButton.setBackgroundResource(R.drawable.alertdialog_right_selector);
            leftButton.setVisibility(View.VISIBLE);
            leftButton.setBackgroundResource(R.drawable.alertdialog_left_selector);
            img_line.setVisibility(View.VISIBLE);
        }

        if (showRightBtn && !showLeftBtn) {
            rightButton.setVisibility(View.VISIBLE);
            rightButton.setBackgroundResource(R.drawable.alertdialog_single_selector);
        }

        if (!showRightBtn && showLeftBtn) {
            leftButton.setVisibility(View.VISIBLE);
            leftButton.setBackgroundResource(R.drawable.alertdialog_single_selector);
        }
    }

    public void show() {
        setLayout();
        dialog.show();
    }
}
