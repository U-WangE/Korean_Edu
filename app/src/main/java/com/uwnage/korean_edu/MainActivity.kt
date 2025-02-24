package com.uwnage.korean_edu

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uwnage.korean_edu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!

    private var webView: WebView? = null
    private var webScriptExecutor: WebScriptExecutor? = null

    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private fun setBackPressedCallback() {
        onBackPressedCallback = object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView?.canGoBack() == true)
                    webView?.goBack() // 뒤로 가기 기능
                else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        this.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onStart() {
        super.onStart()
        setBackPressedCallback()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.wvWebView
        webScriptExecutor = WebScriptExecutorImpl()

        setupWebView()
        setBackPressedCallback()

        webView?.loadUrl("https://edu.korean.go.kr/kires/s-portal/s_portal.html")
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webSettings = webView?.settings
        webSettings?.apply {
            javaScriptEnabled = true // JavaScript 활성화
            domStorageEnabled = true // 로컬 저장소 사용 허용
            useWideViewPort = true // 뷰포트 사용 설정
            loadWithOverviewMode = true // 컨텐츠가 화면 크기에 맞게 조정됨
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(false)
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView?.addJavascriptInterface(WebAppInterface(webScriptExecutor), "Android") // JavaScript Interface 추가

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // 현재 웹사이트의 쿠키 가져오기
                val cookies = cookieManager.getCookie(url)
                Log.d("WebView", "쿠키: $cookies")
                // 쿠키 저장 (앱 종료 후에도 유지)
                cookieManager.flush()

                evaluateJavascriptInClassRoom()
            }
        }
        webView?.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                // 새 WebView를 생성하지 않고, 현재 WebView에서 로드
                val transport = resultMsg?.obj as WebView.WebViewTransport
                transport.webView = view
                resultMsg.sendToTarget()
                return true
            }
        }
        binding.srSwipeLayout.setOnRefreshListener {
            // 새로고침 코드를 작성
            webView?.reload()
            binding.srSwipeLayout.isRefreshing = false
        }
    }

    fun evaluateJavascriptInClassRoom() {
        if (webView?.url?.contains("https://edu.korean.go.kr/kires/s-portal/classroom_main.html?courseId=") == true) {
            webScriptExecutor?.let {
                webView?.evaluateJavascript(it.scrollToElement ("#wrap .sub_title"), null)
                webView?.evaluateJavascript(it.injectJavaScript(), null)
            }
        }
    }
}