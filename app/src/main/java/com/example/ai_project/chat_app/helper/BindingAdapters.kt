package com.example.ai_project.chat_app.helper

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView

object BindingAdapters {

    @BindingAdapter("messageGravity")
    @JvmStatic
    fun setMessageGravity(cardView: MaterialCardView, isUser: Boolean) {
        val params = cardView.layoutParams as? LinearLayout.LayoutParams ?: return
        params.gravity = if (isUser) Gravity.END else Gravity.START
        cardView.layoutParams = params
    }

    @BindingAdapter("textGravity")
    @JvmStatic
    fun setTextGravity(textView: TextView, isUser: Boolean) {
        val params = textView.layoutParams as? LinearLayout.LayoutParams ?: return
        params.gravity = if (isUser) Gravity.END else Gravity.START
        textView.layoutParams = params
    }
}