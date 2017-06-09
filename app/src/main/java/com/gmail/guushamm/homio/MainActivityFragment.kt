package com.gmail.guushamm.homio

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.annotations.Nullable

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater?, @Nullable container: ViewGroup?, @Nullable savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_main, container, false)
    }
}
