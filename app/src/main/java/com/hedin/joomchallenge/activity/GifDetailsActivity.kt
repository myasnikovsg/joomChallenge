package com.hedin.joomchallenge.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.google.gson.Gson
import com.hedin.joomchallenge.ChallengeApplication
import com.hedin.joomchallenge.ChallengeApplication.Companion.gson
import com.hedin.joomchallenge.R
import com.hedin.joomchallenge.asReadableMessage
import com.hedin.joomchallenge.crossfade
import com.hedin.joomchallenge.model.GifItem
import com.hedin.joomchallenge.model.GiphyError
import com.hedin.joomchallenge.model.GiphyItemResponse
import com.hedin.joomchallenge.network.GiphyClient
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.gif_details_activity.*
import kotlinx.android.synthetic.main.user_info_layout.*
import retrofit2.Response

class GifDetailsActivity : AppCompatActivity() {

    private val subscriptions = CompositeDisposable()

    private var gifItem: GifItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(LOG_TAG, "onCreate")

        setContentView(R.layout.gif_details_activity)

        gifItem = intent.getParcelableExtra(EXTRA_GIF_ITEM)
        if (gifItem == null) {
            gifItem = savedInstanceState?.getParcelable(EXTRA_GIF_ITEM)
        }

        if (gifItem == null) {
            val gifId = intent.data?.lastPathSegment ?: ""

            supportActionBar?.title = getString(R.string.screen_title_gif_details_unknown, gifId)
            gifDetailsContainer.visibility = View.GONE

            val showProgressAndLoad: () -> Unit = {
                centralProgressView.showProgress(R.string.loading_item_message) {
                    loadData(gifId)
                }
            }

            centralProgressView.setOnRetryAction(showProgressAndLoad)

            showProgressAndLoad.invoke()
        } else {
            presentContent()
        }
    }

    private fun presentContent() {
        Log.d(LOG_TAG, "present content")

        if (centralProgressView.visibility == View.VISIBLE) {
            crossfade(gifDetailsContainer, centralProgressView)
        }

        supportActionBar?.title = gifItem?.title

        gifItem?.let { item ->
            Picasso.get()
                    .load(item.url)
                    .placeholder(R.drawable.ic_gif_placeholder)
                    .into(gifImage)

            item.username?.let {
                usernameTitle.visibility = View.VISIBLE
                username.visibility = View.VISIBLE
                username.text = it
            }

            item.name?.let {
                nameTitle.visibility = View.VISIBLE
                name.visibility = View.VISIBLE
                name.text = it
            }

            item.twitter?.let {
                twitterTitle.visibility = View.VISIBLE
                twitter.visibility = View.VISIBLE
                twitter.text = it
            }

            item.profile?.let {
                profileTitle.visibility = View.VISIBLE
                profile.visibility = View.VISIBLE
                profile.text = it
            }
        }
    }

    private fun loadData(id: String) {
        Log.d(LOG_TAG, "loadData, id is $id")

        subscriptions.clear()

        val subscription = GiphyClient.service.byId(id, ChallengeApplication.GIPHY_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse, this::handleError)

        subscriptions.add(subscription)
    }

    private fun handleResponse(itemResponse: Response<GiphyItemResponse>) {
        if (itemResponse.isSuccessful) {
            val dataItem = itemResponse.body().data

            if (dataItem == null) {
                Log.d(LOG_TAG, "handleResponse: got malformed response")

                handleError(getString(R.string.generic_error_message))
            } else {
                gifItem = GifItem(dataItem)

                Log.d(LOG_TAG,"handleResponse: got gif $gifItem")

                presentContent()
            }
        } else {
            val giphyError = gson.fromJson(itemResponse.errorBody().charStream(), GiphyError::class.java)

            handleError(getString(R.string.http_error_message, giphyError?.message)
                    ?: getString(R.string.generic_error_message))
            Log.e(LOG_TAG, "${itemResponse.code()} ${giphyError?.message ?: itemResponse.message()}")
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        gifItem?.let {
            outState?.putParcelable(EXTRA_GIF_ITEM, it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                supportFinishAfterTransition()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleError(throwable: Throwable) {
        handleError(throwable.asReadableMessage(this))
    }

    private fun handleError(message: CharSequence) {
        centralProgressView.showError(message, true)
    }

    override fun onDestroy() {
        subscriptions.dispose()

        super.onDestroy()
    }

    companion object {

        private const val LOG_TAG = "GifDetailsActivity"

        const val EXTRA_GIF_ITEM = "extra_gif_item"
    }
}