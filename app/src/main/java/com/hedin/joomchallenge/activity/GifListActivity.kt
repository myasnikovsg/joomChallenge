package com.hedin.joomchallenge.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.google.gson.Gson
import com.hedin.joomchallenge.*
import com.hedin.joomchallenge.adapter.GifGridRecyclerViewAdapter
import com.hedin.joomchallenge.adapter.GifItemDecoration
import com.hedin.joomchallenge.adapter.PaginationScrollListener
import com.hedin.joomchallenge.model.GifItem
import com.hedin.joomchallenge.model.GiphyError
import com.hedin.joomchallenge.model.GiphyListResponse
import com.hedin.joomchallenge.network.GiphyClient
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.gif_list_activity.*
import org.jetbrains.anko.support.v4.onRefresh
import retrofit2.Response
import java.util.*
import android.os.Parcelable
import com.hedin.joomchallenge.CONFIG_GRID_HORIZONTAL_COLUMNS
import com.hedin.joomchallenge.CONFIG_GRID_HORIZONTAL_THRESHOLD_ROWS
import com.hedin.joomchallenge.CONFIG_GRID_VERTICAL_COLUMNS
import com.hedin.joomchallenge.CONFIG_GRID_VERTICAL_THRESHOLD_ROWS
import com.hedin.joomchallenge.ChallengeApplication.Companion.gson
import com.hedin.joomchallenge.fragment.GifListRetainerFragment

class GifListActivity : AppCompatActivity() {

    // fragment to retain data on config changes
    private var dataFragment: GifListRetainerFragment? = null

    // data
    private lateinit var gifs: MutableList<GifItem>

    private val subscriptions = CompositeDisposable()
    private val pagination = PublishProcessor.create<Int>()

    // how much items to request
    private var itemsPerPage: Int = 0

    // data related variables
    private var requestInProgress = false
    private var hasData = false

    private lateinit var gifAdapter: GifGridRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gif_list_activity)

        Log.d(LOG_TAG, "onCreate")

        supportActionBar?.title = getString(R.string.screen_title_gif_list)

        initFragment()

        initRecycler()

        centralProgressView.setOnRetryAction {
            centralProgressView.showProgress(R.string.loading_list_message) { subscribeForData() }
        }

        refreshContainer.onRefresh(this::onRefresh)

        if (hasData) {
            centralProgressView.visibility = View.GONE
            gifRecycler.visibility = View.VISIBLE
        }

        subscribeForData(sendRequest = !hasData)
    }

    private fun initRecycler() {
        val orientation = resources.configuration.orientation
        itemsPerPage = if (orientation == Configuration.ORIENTATION_LANDSCAPE) ITEMS_PER_PAGE_HORIZONTAL else ITEMS_PER_PAGE_VERTICAL
        val itemsPerRow = if (orientation == Configuration.ORIENTATION_LANDSCAPE) CONFIG_GRID_HORIZONTAL_COLUMNS else CONFIG_GRID_VERTICAL_COLUMNS

        val gridLayoutManager = GridLayoutManager(this, itemsPerRow)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) =
                    when (gifAdapter.getItemViewType(position)) {
                        GifGridRecyclerViewAdapter.VIEW_TYPE_PROGRESS ->
                            itemsPerRow
                        GifGridRecyclerViewAdapter.VIEW_TYPE_ITEM ->
                            1
                        else ->
                            -1
                    }
        }
        gifRecycler.layoutManager = gridLayoutManager

        gifAdapter = GifGridRecyclerViewAdapter(gifs, onItemClick = { item: GifItem, image: ImageView ->
            val intent = Intent(this, GifDetailsActivity::class.java)
            intent.putExtra(GifDetailsActivity.EXTRA_GIF_ITEM, item)
            val options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(this, image, getString(R.string.shared_item_tag))
            startActivity(intent, options.toBundle())
        })
        gifRecycler.adapter = gifAdapter

        gifRecycler.addItemDecoration(GifItemDecoration(resources.getDimensionPixelSize(R.dimen.margin)))

        gifRecycler.addOnScrollListener(object : PaginationScrollListener(gridLayoutManager, itemsPerPage) {
            override fun onLoadMore(currentPage: Int, totalItemCount: Int) {
                if (!requestInProgress) {
                    pagination.onNext(gifAdapter.itemCount)
                }
            }
        })
    }

    private fun initFragment() {
        val fm = supportFragmentManager
        dataFragment = fm.findFragmentByTag(DATA_FRAGMENT_TAG) as? GifListRetainerFragment

        if (dataFragment == null) {
            dataFragment = GifListRetainerFragment()
            fm.beginTransaction().add(dataFragment!!, DATA_FRAGMENT_TAG).commit()
        } else {
            hasData = dataFragment!!.data.isNotEmpty()
        }

        gifs = dataFragment!!.data as MutableList<GifItem>
    }

    private fun onRefresh() {
        Log.d(LOG_TAG, "onRefresh")

        if (bottomProgressView.visibility == View.VISIBLE) {
            fadeOut(bottomProgressView)
        }

        subscribeForData()
    }

    private fun handleError(error: Throwable) {
        Log.d(LOG_TAG, "requesting gifs failed with {${error.message}}")

        handleError(error.asReadableMessage(this))
    }

    private fun handleError(message: CharSequence) {
        if (hasData) {
            handlePaginationError(message)
        } else {
            handleReloadError(message)
        }
    }

    private fun subscribeForData(offset: Int = 0, sendRequest: Boolean = true) {
        if (offset == 0) {
            hasData = false
        }

        subscriptions.clear()

        val disposable = pagination
                .onBackpressureDrop()
                .doOnNext { _ ->
                    requestInProgress = true
                    if (hasData) {
                        if (bottomProgressView.visibility != View.VISIBLE) {
                            gifAdapter.progressVisible = true
                        }
                    }
                }
                .concatMap(this::loadGifs)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse, this::handleError)

        subscriptions.add(disposable)
        if (sendRequest) {
            pagination.onNext(offset)
        }
    }

    private fun handlePaginationError(message: CharSequence) {
        gifAdapter.progressVisible = false
        bottomProgressView.showError(message, true)
        bottomProgressView.setOnRetryAction {
            bottomProgressView.showProgress(R.string.loading_list_message) {
                subscribeForData(gifAdapter.itemCount)
            }
        }
    }

    private fun handleReloadError(message: CharSequence) {
        refreshContainer.isRefreshing = false

        centralProgressView.showError(message, true)

        if (gifRecycler.visibility == View.VISIBLE) {
            fadeOut(gifRecycler, onAnimationEndAction = { gifAdapter.replaceItems(Collections.emptyList()) })
        }
    }

    private fun handleResponse(gifListResponse: Response<GiphyListResponse>) {
        if (gifListResponse.isSuccessful) {
            val newItems = gifListResponse.body().data?.mapNotNull { dataItem ->
                dataItem?.let {
                    GifItem(it)
                }
            }

            Log.d(LOG_TAG, "handleResponse: got ${newItems?.size} new items")

            if (gifRecycler.visibility != View.VISIBLE) {
                fadeOut(centralProgressView)
                fadeIn(gifRecycler) { gifAdapter.addItems(newItems) }
                refreshContainer.isRefreshing = false
            } else if (refreshContainer.isRefreshing) {
                refreshContainer.isRefreshing = false
                gifAdapter.replaceItems(newItems)
            } else {
                gifAdapter.addItems(newItems)
            }

            hasData = true
        } else {
            val giphyError = gson.fromJson(gifListResponse.errorBody().charStream(), GiphyError::class.java)

            handleError(getString(R.string.http_error_message, giphyError?.message)
                    ?: getString(R.string.generic_error_message))
            Log.d(LOG_TAG, "${gifListResponse.code()} ${giphyError?.message
                    ?: gifListResponse.message()}")
        }

        requestInProgress = false
        gifAdapter.progressVisible = false
        bottomProgressView.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.gif_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_refresh -> {
                onRefresh()
                return true
            }
        }
        return onOptionsItemSelected(item)
    }

    private fun loadGifs(offset: Int): Flowable<Response<GiphyListResponse>> {
        Log.d(LOG_TAG, "loadGifs: $offset")
        return GiphyClient.service.trending(ChallengeApplication.GIPHY_API_KEY, offset, itemsPerPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putParcelable(SAVED_LAYOUT_MANAGER_STATE,
                gifRecycler.layoutManager?.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        val state = savedInstanceState?.get(SAVED_LAYOUT_MANAGER_STATE) as Parcelable?
        if (state != null) {
            gifRecycler.layoutManager?.onRestoreInstanceState(state)
        }
    }

    override fun onDestroy() {
        subscriptions.dispose()

        super.onDestroy()
    }

    companion object {

        private const val LOG_TAG = "GifListActivity"

        private const val DATA_FRAGMENT_TAG = "data_fragment"

        private const val SAVED_LAYOUT_MANAGER_STATE = "saved_layout_manager_state"

        private const val ITEMS_PER_PAGE_VERTICAL =
                CONFIG_GRID_VERTICAL_COLUMNS * CONFIG_GRID_VERTICAL_THRESHOLD_ROWS

        private const val ITEMS_PER_PAGE_HORIZONTAL =
                CONFIG_GRID_HORIZONTAL_COLUMNS * CONFIG_GRID_HORIZONTAL_THRESHOLD_ROWS
    }
}
