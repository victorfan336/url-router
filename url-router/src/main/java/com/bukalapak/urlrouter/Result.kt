package com.bukalapak.urlrouter

import android.content.Context
import android.os.Bundle

/**
 * Created by mrhabibi on 5/26/17.
 */

class Result(val context: Context,
             val url: String,
             val variables: OptParamMap,
             val queries: OptParamsMap,
             val fragment: String?,
             val args: Bundle?)

class RawResult(val variables: OptParamMap = OptParamMap(),
                val queries: OptParamsMap = OptParamsMap(),
                var fragment: String? = null) {

    fun cook(context: Context,
             url: String,
             args: Bundle?): Result = Result(
            context = context,
            url = url,
            variables = variables,
            queries = queries,
            fragment = fragment,
            args = args
    )
}