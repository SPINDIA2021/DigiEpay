package com.satmatgroup.digiepay.dmt

import java.io.Serializable

class SelectDmtModel (
    val service_id: String,
    val name: String,
    val status: String,
    val service_type: String
) :Serializable