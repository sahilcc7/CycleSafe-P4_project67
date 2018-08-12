package com.example.sahil.project67;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.WindowManager;

public class AlertDialogManager {

    public void showAlertDialog(Context context, String title, String message,
                                Boolean status) {

        Log.d("", "ALERT");
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);

        if (status != null)
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });

        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }

}
