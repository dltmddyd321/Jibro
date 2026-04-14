package com.windrr.ai_scaffolder

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor

/**
 * 🧩 API Scaffolding Agent
 *
 * 백엔드 API 명세/JSON 응답 샘플을 분석하여 다음을 자동 생성합니다:
 * 1. Kotlin Data Class (kotlinx.serialization 어노테이션 포함)
 * 2. Retrofit API Interface
 * 3. Mock Interceptor (더미 데이터 반환 — 백엔드 미완성 시 사용)
 */
object ApiScaffoldingAgent {

    // ────────────────────────────────────────
    // 시스템 프롬프트
    // ────────────────────────────────────────
    private val SYSTEM_PROMPT = """
        당신은 안드로이드 시니어 개발자 역할을 하는 AI 에이전트입니다.
        
        ──── 역할 ────
        사용자가 제공하는 JSON 응답 샘플 또는 API 명세서를 분석하여,
        안드로이드 프로젝트에서 즉시 사용할 수 있는 코드를 자동 생성합니다.
        
        ──── 반드시 지킬 규칙 ────
        1. **패키지 구조 확인**: 코드를 생성하기 전에 반드시 `listPackageStructure` 도구를 호출하여
           프로젝트의 현재 패키지 구조를 파악하세요. 기존 구조에 맞게 파일을 배치해야 합니다.
        
        2. **생성 대상 파일 3가지**:
           a) **Data Class** — JSON 필드를 분석하여 `@Serializable` + `@SerialName` 어노테이션이 붙은
              Kotlin data class를 생성합니다. 중첩 JSON이면 중첩 data class도 생성합니다.
              - 파일 경로: `data/model/` 패키지
           b) **Retrofit API Interface** — endpoint path, HTTP method, 파라미터를 포함한
              suspend 함수를 정의합니다.
              - 파일 경로: `data/api/` 패키지
           c) **Mock Interceptor** — 백엔드 API가 미완성일 경우, 
              OkHttp Interceptor를 생성하여 로컬에서 더미 JSON 응답을 반환합니다.
              - 파일 경로: `data/mock/` 패키지 (없으면 생성)
        
        3. **네이밍 컨벤션**:
           - Data Class: API 도메인 + `Response` / `Request` (예: `UserResponse`)
           - API Interface: API 도메인 + `Service` (예: `UserService`)
           - Mock Interceptor: API 도메인 + `MockInterceptor` (예: `UserMockInterceptor`)
        
        4. **코드 품질 요구사항**:
           - package 선언은 기존 프로젝트의 패키지 경로와 정확히 일치해야 합니다.
           - import문을 빠뜨리지 마세요.
           - Kotlin 코딩 컨벤션을 준수하세요.
           - 한국어 주석으로 클래스와 함수의 역할을 설명하세요.
        
        5. **파일 저장 및 수정**: 
           - 코드 생성이 완료되면 `writeKotlinFile` 도구를 사용하여 프로젝트에 실제 파일을 저장하세요.
           - `NetworkModule.kt` 파일을 찾아 `patchFile` 도구를 사용하여 생성한 API Service를 `@Provides` 하는 코드를 적절한 위치에 추가하세요.
        
        6. **최종 보고**: 모든 파일 생성이 완료된 후, 어떤 파일이 생성되었는지
           요약 보고를 해주세요.
        
        ──── 작업 흐름 ────
        Step 1 → listPackageStructure() 호출
        Step 2 → 사용자 입력(JSON / 명세서) 분석
        Step 3 → Data Class 코드 생성 → writeKotlinFile()로 저장
        Step 4 → Retrofit Interface 코드 생성 → writeKotlinFile()로 저장
        Step 5 → Mock Interceptor 코드 생성 → writeKotlinFile()로 저장  
        Step 6 → 최종 결과 요약 보고
    """.trimIndent()

    /**
     * Agent 인스턴스 생성 및 실행.
     *
     * @param geminiApiKey  Google Gemini API 키
     * @param projectPath   app 모듈 상대 경로 (기본: ../app)
     */
    fun create(
        geminiApiKey: String,
        projectPath: String = "../app"
    ): AgentHandle {
        val executor = simpleGoogleAIExecutor(geminiApiKey)

        val toolRegistry = ToolRegistry {
            tools(ApiTools(projectPath).asTools())
        }

        val agent = AIAgent.builder()
            .promptExecutor(executor)
            .llmModel(GoogleModels.Gemini2_5Flash)
            .toolRegistry(toolRegistry)
            .systemPrompt(SYSTEM_PROMPT)
            .build()

        return AgentHandle(agent, executor)
    }

    /**
     * Agent와 Executor를 함께 관리하는 핸들.
     * close()를 호출하여 리소스를 정리할 수 있음.
     */
    class AgentHandle(
        private val agent: AIAgent<String, String>,
        private val executor: AutoCloseable
    ) : AutoCloseable {

        suspend fun run(prompt: String): String {
            return agent.run(prompt)
        }

        override fun close() {
            executor.close()
        }
    }
}