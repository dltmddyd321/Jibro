package com.windrr.ai_scaffolder

import kotlinx.coroutines.runBlocking

/**
 * 🚀 API Scaffolding Agent 실행 진입점
 *
 * ── 실행 방법 ──
 * 1. 환경 변수 설정:
 *    export GEMINI_API_KEY="your-gemini-api-key"
 *
 * 2. Gradle로 실행:
 *    ./gradlew :ai-scaffolder:run --args="실행할_프롬프트"
 *
 * ── 또는 대화형(Interactive) 모드 ──
 *    ./gradlew :ai-scaffolder:run
 *    → 프롬프트를 직접 입력할 수 있습니다.
 */
fun main(args: Array<String>) = runBlocking {
    // ─── API 키 확인 ───
    val apiKey = System.getenv("GEMINI_API_KEY")
        ?: error(
            """
            ❌ GEMINI_API_KEY 환경변수가 설정되지 않았습니다.
            
            다음 명령어로 설정하세요:
              export GEMINI_API_KEY="your-gemini-api-key"
              
            Google AI Studio에서 무료 키를 발급받을 수 있습니다:
              https://aistudio.google.com/apikey
            """.trimIndent()
        )

    println(
        """
        ┌──────────────────────────────────────────────┐
        │  🧩 API Scaffolding Agent                    │
        │  ─────────────────────────────────           │
        │  JSON 응답 / API 명세를 분석하여              │
        │  Data Class · Retrofit · Mock Interceptor    │
        │  코드를 자동 생성합니다.                       │
        └──────────────────────────────────────────────┘
        """.trimIndent()
    )

    // ─── 프롬프트 결정 (CLI 인자 or 대화형 입력) ───
    val prompt = if (args.isNotEmpty()) {
        args.joinToString(" ")
    } else {
        println("📝 JSON 응답 샘플이나 API 명세를 입력하세요 (입력 완료: 빈 줄에서 END 입력):")
        println("─".repeat(50))
        buildString {
            while (true) {
                val line = readLine() ?: break
                if (line.trim().equals("END", ignoreCase = true)) break
                appendLine(line)
            }
        }.trim()
    }

    if (prompt.isBlank()) {
        println("⚠️  입력이 비어 있습니다. 종료합니다.")
        return@runBlocking
    }

    println("\n🔄 에이전트 실행 중...")
    println("─".repeat(50))

    // ─── 에이전트 생성 및 실행 ───
    val handle = ApiScaffoldingAgent.create(apiKey)
    handle.use { agent ->
        val result = agent.run(prompt)
        println("\n✅ 에이전트 실행 완료")
        println("═".repeat(50))
        println(result)
        println("═".repeat(50))
    }
}
