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
                        
                        // Inject advanced, multi-selector robust JS observer
                        val js = """
                            (function() {
                                TikTokBridge.sendLog("Iniciando observador de chat avanzado...");
                                var processedMessages = new Set();
                                
                                // Periodically log structural diagnostic info to the app console
                                setTimeout(function() {
                                    try {
                                        var divs = document.querySelectorAll('div').length;
                                        TikTokBridge.sendLog("[Scraper JS] Inicializado. Elementos div en página: " + divs);
                                    } catch(e) {}
                                }, 4000);
                                
                                setInterval(function() {
                                    try {
                                        var chatMessages = [];
                                        
                                        // Selector Fallback 1: data-e2e chat-message
                                        var e2eList = document.querySelectorAll('[data-e2e="chat-message"]');
                                        if (e2eList && e2eList.length > 0) {
                                            chatMessages = Array.from(e2eList);
                                        }
                                        
                                        // Selector Fallback 2: Common TikTok webcast container classes
                                        if (chatMessages.length === 0) {
                                            var webcastList = document.querySelectorAll('div[class*="webcast-chatroom___item"], div[class*="ChatroomItem"], div[class*="ChatMessage"], div[class*="MessageContainer"], div[class*="webcast-chatroom___message"]');
                                            if (webcastList && webcastList.length > 0) {
                                                chatMessages = Array.from(webcastList);
                                            }
                                        }
                                        
                                        // Selector Fallback 3: Inner elements inside ChatRoom
                                        if (chatMessages.length === 0) {
                                            var chatContainer = document.querySelector('[data-e2e="chat-room"], div[class*="ChatroomContainer"], div[class*="ChatRoom"]');
                                            if (chatContainer) {
                                                var pDivs = chatContainer.querySelectorAll('div');
                                                chatMessages = Array.from(pDivs).filter(function(el) {
                                                    return el.textContent.includes(':') && el.textContent.length > 4 && el.textContent.length < 250;
                                                });
                                            }
                                        }
                                        
                                        if (chatMessages && chatMessages.length > 0) {
                                            for (var i = 0; i < chatMessages.length; i++) {
                                                var msgNode = chatMessages[i];
                                                var user = "";
                                                var text = "";
                                                
                                                // Extraction Strategy A: data-e2e / classes
                                                var userNode = msgNode.querySelector('[data-e2e="comment-username"], span[class*="UserNickname"], span[class*="nickname"], span[class*="username"], a[href*="/@"]');
                                                var textNode = msgNode.querySelector('[data-e2e="comment-text"], [data-e2e="chat-message-text"], span[class*="CommentText"], span[class*="text"], span[class*="comment"]');
                                                
                                                if (userNode && textNode) {
                                                    user = userNode.innerText || userNode.textContent || "";
                                                    text = textNode.innerText || textNode.textContent || "";
                                                } else {
                                                    // Extraction Strategy B: spans
                                                    var spans = msgNode.querySelectorAll('span');
                                                    if (spans.length >= 2) {
                                                        user = spans[0].innerText || spans[0].textContent || "";
                                                        text = spans[spans.length - 1].innerText || spans[spans.length - 1].textContent || "";
                                                    } else {
                                                        // Extraction Strategy C: Colon splitter
                                                        var rawText = msgNode.textContent || msgNode.innerText || "";
                                                        var colonIdx = rawText.indexOf(':');
                                                        if (colonIdx > 0) {
                                                            user = rawText.substring(0, colonIdx);
                                                            text = rawText.substring(colonIdx + 1);
                                                        }
                                                    }
                                                }
                                                
                                                // Clean outputs
                                                user = user.replace(/[\s:@]/g, "").trim();
                                                text = text.trim();
                                                
                                                if (user && text && text.length >= 3) {
                                                    var msgKey = user + ":" + text;
                                                    if (!processedMessages.has(msgKey)) {
                                                        processedMessages.add(msgKey);
                                                        if (processedMessages.size > 500) {
                                                            processedMessages.clear();
                                                        }
                                                        
                                                        // Log and trigger Kotlin callback
                                                        TikTokBridge.sendLog("✔ COMENTARIO LEÍDO -> @" + user + ": " + text);
                                                        TikTokBridge.sendComment(user, text);
                                                    }
                                                }
                                            }
                                        }
                                    } catch(e) {
                                        TikTokBridge.sendLog("Error en scraper JS: " + e.toString());
                                    }
                                }, 1000);
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
