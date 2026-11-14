package com.smartpaymentrouter.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object IntentHelper {

    fun launchUpiIntent(context: Context, upiUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            val chooser = Intent.createChooser(intent, "Pay with:").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No UPI payment app found. Please copy details to your banking app.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
