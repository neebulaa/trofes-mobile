package pepes.co.trofes.ui.common

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun YouTubeInlinePlayer(
    youtubeId: String,
    modifier: Modifier = Modifier,
) {
    var webView: WebView? = null

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).also { wv ->
                webView = wv
                setup(wv)
                wv.loadDataWithBaseURL(
                    "https://www.youtube.com",
                    youtubeHtml(youtubeId),
                    "text/html",
                    "utf-8",
                    null,
                )
            }
        },
        update = { wv ->
            if (wv.url == null) {
                wv.loadDataWithBaseURL(
                    "https://www.youtube.com",
                    youtubeHtml(youtubeId),
                    "text/html",
                    "utf-8",
                    null,
                )
            }
        },
    )

    DisposableEffect(youtubeId) {
        onDispose {
            webView?.stopLoading()
            webView?.loadUrl("about:blank")
            webView?.clearHistory()
            webView?.removeAllViews()
            webView?.destroy()
            webView = null
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun setup(webView: WebView) {
    webView.webChromeClient = WebChromeClient()
    webView.webViewClient = WebViewClient()

    val s = webView.settings
    s.javaScriptEnabled = true
    s.domStorageEnabled = true
    s.loadWithOverviewMode = true
    s.useWideViewPort = true
    s.mediaPlaybackRequiresUserGesture = true
    s.cacheMode = WebSettings.LOAD_DEFAULT
}

private fun youtubeHtml(youtubeId: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
          <style>
            html, body { margin:0; padding:0; background:#000; height:100%; }
            .wrap { position:relative; width:100%; height:100%; }
            iframe { position:absolute; top:0; left:0; width:100%; height:100%; border:0; }
          </style>
        </head>
        <body>
          <div class=\"wrap\">
            <iframe
              src=\"https://www.youtube.com/embed/$youtubeId\"
              title=\"YouTube video\"
              allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\"
              allowfullscreen>
            </iframe>
          </div>
        </body>
        </html>
    """.trimIndent()
}
