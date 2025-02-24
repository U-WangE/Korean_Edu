package com.uwnage.korean_edu

import android.webkit.WebView

interface WebScriptExecutor {
    fun scrollToElement(element: String): String
    fun injectJavaScript(): String
}

class WebScriptExecutorImpl: WebScriptExecutor {
    override fun scrollToElement(element: String): String {
        return """
            (function() {
                function scrollToElement() {
                    var selector = ${element.trim().let { "\"$it\"" }}; // 문자열 처리
                    var element = document.querySelector(selector);
                    if (element) {
                        element.scrollIntoView({ behavior: 'smooth', block: 'start' });
                        observer.disconnect(); // 감지 중지
                    }
                }
                
                var observer = new MutationObserver(function(mutations, observer) {
                    scrollToElement();
                });
        
                var targetNode = document.getElementById("wrap");
                if (targetNode) {
                    observer.observe(targetNode, { childList: true, subtree: true });
                }
                
                // 혹시라도 observer가 동작하지 않을 경우 대비하여 0.2초 뒤 강제 실행
                setTimeout(scrollToElement, 200);
            })();
        """.trimIndent()
    }


    override fun injectJavaScript(): String {
        return """
            (function() {
                document.addEventListener("click", function(event) {
                    var target = event.target;
                    var parentD02 = target.closest('.d_02'); // .d_02 클래스를 가진 가장 가까운 부모 찾기
                    
                    if (!parentD02) {
                        return; // 클릭한 요소가 .d_02 내부에 없다면 종료
                    }
                    
                    var videoArcBtn = parentD02.querySelector('img.video_arc_btn') || "no-video";
                    
                    if (target === videoArcBtn) {
                        return;
                    }
                    
                    var parentRow = parentD02.closest('.parent_round_tr'); // 부모의 부모인 .parent_round_tr 찾기
                    
                    if (parentRow) {
                        var parentDataRound = parentRow.getAttribute("data-round") || "no-data-round";
                        
                        if (parentDataRound !== "no-data-round" && videoArcBtn !== "no-video") {
                            videoArcBtn.click()
                        }
                    }
                }, true);
            })();
        """.trimIndent()
    }
}