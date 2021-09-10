package com.ekosp.indoweb.epesantren.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.ekosp.indoweb.epesantren.R

class BlankFragment : Fragment(R.layout.activity_home_page) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListener()
    }

    private fun setListener() {

    }

}