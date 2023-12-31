package com.satmatgroup.digiepay.activities_bbps.services_fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.satmatgroup.digiepay.R
import com.satmatgroup.digiepay.activities_bbps.BbpsBillConfirmationActivity
import kotlinx.android.synthetic.main.fragment_bbpsdth.view.*

class BbpsdthFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bbpsdth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.btnShowDetails.setOnClickListener {
            val intent = Intent(context, BbpsBillConfirmationActivity::class.java)
            context?.startActivity(intent)
        }
    }

}