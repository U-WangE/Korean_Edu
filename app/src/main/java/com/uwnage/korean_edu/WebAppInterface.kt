package com.uwnage.korean_edu

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface

//web에 해당 function을 넣는 class
class WebAppInterface(private val webScriptExecutor: WebScriptExecutor?) {
    @JavascriptInterface
    fun onListItemClick(dataRound: String, videoArcBtnSrc: String) {
        Log.d("WebView", "클릭한 요소 → Class: $dataRound")
    }
}