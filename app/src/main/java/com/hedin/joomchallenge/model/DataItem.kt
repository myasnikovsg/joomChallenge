package com.hedin.joomchallenge.model

data class DataItem(
	val images: Map<String, ImageInfo>,
	val id: String,
	val user: User? = null,
	val title: String? = null
) {

	fun getPreview() = images[PREVIEW_KEY]

	companion object {
		const val PREVIEW_KEY = "480w_still"
	}
}
