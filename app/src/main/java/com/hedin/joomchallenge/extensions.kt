package com.hedin.joomchallenge

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import retrofit2.HttpException

fun Throwable.asReadableMessage(context: Context): String = if (this is HttpException) {
    context.getString(R.string.http_error_message, this.response().message())
} else {
    context.getString(R.string.generic_error_message);
}

fun ViewGroup.inflate(layoutRes: Int): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, false)
}

fun fadeIn(view: View, onAnimationEndAction: (() -> Unit)? = null) = view.apply {
    clearAnimation()
    alpha = 0f
    visibility = View.VISIBLE

    animate()
            .alpha(1f)
            .setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEndAction?.invoke()
                }
            })
}

fun fadeOut(view: View, onAnimationEndAction: (() -> Unit)? = null, disappearView: Boolean = true) = view.apply {
    clearAnimation()
    animate()
            .alpha(0f)
            .setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = if (disappearView) View.GONE else View.INVISIBLE
                    onAnimationEndAction?.invoke()
                }
            })
}

fun changeTextWithAnimation(textView: TextView, text: CharSequence?) {
    val setTextAndFadeIn: () -> Unit = {
        textView.text = text
        fadeIn(textView)
    }

    if (textView.visibility == View.VISIBLE) {
        fadeOut(textView, onAnimationEndAction = setTextAndFadeIn, disappearView = false)
    } else {
        setTextAndFadeIn.invoke()
    }
}

fun crossfade(emergingView: View, hidingView: View) {
    fadeIn(emergingView)
    fadeOut(hidingView)
}

