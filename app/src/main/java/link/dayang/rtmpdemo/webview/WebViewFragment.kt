package link.dayang.rtmpdemo.webview

import android.net.http.SslError
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import link.dayang.rtmpdemo.R
import link.dayang.rtmpdemo.StageActivity
import wendu.dsbridge.DWebView

class WebViewFragment : Fragment {
    private lateinit var root: View

    private lateinit var dWebView: DWebView
    private lateinit var progressBar: ProgressBar
    private var url = ""

    constructor(url: String) : super() {
        val bundle = Bundle()
        bundle.putString(KEY_URL, url)
        arguments = bundle
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_webview, container, false)
        initDWebView()
        return root

    }

    private fun initDWebView() {
        dWebView = root.findViewById(R.id.dwebview)
        dWebView.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action === KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK && dWebView.canGoBack()) {
                    //表示按返回键时的操作
                    dWebView.goBack()
                    return@OnKeyListener true
                }
            }
            false
        })
        dWebView.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.proceed()
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url)
                    dWebView.stopLoading()
                    return true
                }
                return false
            }
        }
        progressBar = root.findViewById(R.id.webview_progress)
        dWebView.webChromeClient = object: WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                }
                super.onProgressChanged(view, newProgress)
            }
        }
        dWebView.addJavascriptObject(JsApi(getStageActivity()), "")
    }

    fun refresh() {
        dWebView.reload()
    }

    override fun onResume() {
        super.onResume()
        url = arguments?.getString(KEY_URL)?:""
        if (url == "") {
            getStageActivity().toast("参数不全")
            dWebView.visibility = View.INVISIBLE
            return
        }
        dWebView.clearCache(true)
        dWebView.clearHistory()

        val webSettings = dWebView.settings

        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE

        WebView.setWebContentsDebuggingEnabled(true)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            webSettings.safeBrowsingEnabled = false
        }

//        dWebView.settings = webSettings



        dWebView.loadUrl(url)
        dWebView.postDelayed(Runnable { dWebView.clearHistory() }, 3000)
    }

    fun getStageActivity(): StageActivity {
        return activity as StageActivity

    }

    companion object {
        const val KEY_URL = "url"

    }

}