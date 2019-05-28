package com.hedin.joomchallenge.views

import android.content.Context
import android.support.annotation.StringRes
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.hedin.joomchallenge.*
import kotlinx.android.synthetic.main.progress_with_retry_view.view.*

/**
 * Container with 2 states - progress and error
 * In case of recoverable error, retry button can be shown to facilitate recovery
 * Handles inner layout transitions
 */
class ProgressWithRetryView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.progress_with_retry_view, this, true)
    }

    fun setOnRetryAction(action: (() -> Unit)?) {
        buttonRetry.setOnClickListener { action?.invoke() }
    }

    /**
     * Transitions view to "progress" state, executes passed action when views are fully shown
     *
     * @param progressMessage message to display while in progress
     * @param onProgressShown action to perform fadeIn animations end
     */
    fun showProgress(@StringRes progressMessage: Int, onProgressShown: (() -> Unit)? = null) {
        showProgress(context.getString(progressMessage), onProgressShown)
    }

    /**
     * Transitions view to "progress" state, executes passed action when views are fully shown
     *
     * @param progressMessage message to display while in progress
     * @param onProgressShown action to perform fadeIn animations end
     */
    fun showProgress(progressMessage: String?, onProgressShown: (() -> Unit)? = null) {
        if (visibility == View.VISIBLE) {
            crossfade(progressBar, errorIcon)
            if (progressMessage == null) {
                fadeOut(message)
            } else {
                changeTextWithAnimation(message, progressMessage)
            }
            fadeOut(buttonRetry, onAnimationEndAction = { onProgressShown?.invoke() }, disappearView = false)
        } else {
            errorIcon.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            buttonRetry.visibility = View.GONE
            if (progressMessage == null) {
                message.text = null
                message.visibility = View.GONE
            } else {
                message.text = progressMessage
                message.visibility = View.VISIBLE
            }
            fadeIn(this) { onProgressShown?.invoke() }
        }
    }

    /**
     * Transitions view to "error" state, shows retry button if needed
     *
     * @param errorMessage message to display
     * @param canRetry whether to show retry button
     */
    fun showError(@StringRes errorMessage: Int, canRetry: Boolean) {
        showError(context.getString(errorMessage), canRetry)
    }

    /**
     * Transitions view to "error" state, shows retry button if needed
     *
     * @param errorMessage message to display
     * @param canRetry whether to show retry button
     */
    fun showError(errorMessage: CharSequence, canRetry: Boolean) {
        if (visibility == View.VISIBLE) {
            changeTextWithAnimation(message, errorMessage)
            crossfade(errorIcon, progressBar)
            if (canRetry) {
                fadeIn(buttonRetry)
            } else {
                buttonRetry.visibility = View.GONE
            }
        } else {
            errorIcon.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            message.text = errorMessage
            buttonRetry.visibility = if (canRetry) View.VISIBLE else View.GONE
            fadeIn(this)
        }
    }

}