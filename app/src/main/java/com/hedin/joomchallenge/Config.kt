package com.hedin.joomchallenge

// this initially was thought out as properties, accessed via external storage file,
// but this requires permission, READ_EXTERNAL_STORAGE is dangerous, so explicit
// user consent is needed. Normally, I would init properties in StartActivity,
// kind of splash screen with logo and maybe progress bar, which waits for Application
// to finish its onCreate + requests all desired permissions. Upon reflection, I came
// to consider it an overkill, so... - constants

const val CONFIG_PICASSO_USE_LRU_CACHE = true
const val CONFIG_PICASSO_DISK_CACHE_SIZE = 256 * 1024 * 1024
const val CONFIG_PICASSO_MEMORY_CACHE_SIZE = 128 * 1024 * 1024

const val CONFIG_GRID_VERTICAL_THRESHOLD_ROWS = 12
const val CONFIG_GRID_VERTICAL_COLUMNS = 3

const val CONFIG_GRID_HORIZONTAL_THRESHOLD_ROWS = 6
const val CONFIG_GRID_HORIZONTAL_COLUMNS = 6