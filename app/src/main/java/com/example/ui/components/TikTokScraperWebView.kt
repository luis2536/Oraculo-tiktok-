package com.example.ui.components

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TikTokScraperWebView(
    username: String,
    onComment: (String, String) -> Unit,
    onLog: (String) -> Unit
) {
    if (username.isBlank() || username == "@username") return

    val cleanUsername = if (username.startsWith("@")) username else "@$username"
    val url = "https://www.tiktok.com/$cleanUsername/live"

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                // Use a standard desktop user agent to avoid mobile app redirects and force desktop layout where chat is easily parsable
                settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun sendComment(user: String, text: String) {
                        CoroutineScope(Dispatchers.Main).launch {
                            onComment(user, text)
                        }
                    }
                    
                    @JavascriptInterface
                    fun sendLog(log: String) {
                        CoroutineScope(Dispatchers.Main).launch {
                            onLog(log)
                        }
                    }
                }, "TikTokBridge")

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        // Log JS errors for debugging
                        consoleMessage?.let {
                            if (it.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
                                Log.e("TikTokScraper", "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}")
                            }
                        }
                        return super.onConsoleMessage(consoleMessage)
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLog("Página de TikTok Live cargada. Inyectando scraper JS...")
                        
                        // Inject JS to observe chat
                        val js = """
                            (function() {
                                TikTokBridge.sendLog("Iniciando observador de chat nativo...");
                                
                                // Maintain a set of processed message IDs or texts to avoid duplicates
                                var processedMessages = new Set();
                                
                                setInterval(function() {
                                    try {
                                        // TikTok live chat messages usually have specific data-e2e attributes
                                        var chatMessages = document.querySelectorAll('[data-e2e="chat-message"]');
                                        
                                        if (chatMessages && chatMessages.length > 0) {
                                            for (var i = 0; i < chatMessages.length; i++) {
                                                var msgNode = chatMessages[i];
                                                
                                                var userNode = msgNode.querySelector('span:first-child');
                                                var textNode = msgNode.querySelector('span:last-child');
                                                
                                                if (userNode && textNode) {
                                                    var user = userNode.innerText || userNode.textContent;
                                                    var text = textNode.innerText || textNode.textContent;
                                                    var msgKey = user + ":" + text;
                                                    
                                                    if (!processedMessages.has(msgKey)) {
                                                        processedMessages.add(msgKey);
                                                        // Keep set size manageable
                                                        if (processedMessages.size > 500) {
                                                            processedMessages.clear();
                                                        }
                                                        
                                                        // Send to Kotlin
                                                        TikTokBridge.sendComment(user, text);
                                                    }
                                                }
                                            }
                                        }
                                    } catch(e) {
                                        TikTokBridge.sendLog("Error en scraper: " + e.toString());
                                    }
                                }, 1500); // Check every 1.5 seconds
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(js, null)
                    }
                }
                loadUrl(url)
            }
        },
        update = { webView -> 
            if (webView.url != url && !webView.url.isNullOrEmpty()) {
                webView.loadUrl(url)
            }
        },
        modifier = Modifier.size(1.dp) // Hidden WebView
    )
}
