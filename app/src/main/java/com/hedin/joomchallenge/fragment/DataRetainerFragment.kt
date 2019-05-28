package com.hedin.joomchallenge.fragment

import android.os.Bundle
import android.support.v4.app.Fragment

open class DataRetainerFragment<T>() : Fragment() {

    val data: List<T> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
    }

}