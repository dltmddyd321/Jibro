package com.windrr.ai_scaffolder

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import java.io.File

/**
 * 프로젝트 파일 시스템을 조작하는 도구 모음.
 * [baseProjectPath]는 app 모듈의 루트 경로 (예: ../app)
 */
@LLMDescription("안드로이드 프로젝트의 소스 코드를 탐색하고, 파일을 읽고, 새로운 Kotlin 파일을 생성할 수 있는 도구 모음입니다.")
class ApiTools(
    private val baseProjectPath: String = "../app"
) : ToolSet {

    private val srcRoot get() = File("$baseProjectPath/src/main/java")

    // ──────────────────────────────────────────────
    // 🔍 검색
    // ──────────────────────────────────────────────
    @Tool
    @LLMDescription("프로젝트 내에서 특정 키워드(예: NetworkModule, Interceptor)가 포함된 파일의 경로를 찾습니다.")
    fun codeSearchTool(
        @LLMDescription("검색할 클래스명이나 키워드") keyword: String
    ): String {
        val foundFiles = srcRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { it.name.contains(keyword, ignoreCase = true) || it.readText().contains(keyword) }
            .map { it.relativeTo(srcRoot).path }
            .toList()

        return if (foundFiles.isNotEmpty()) {
            "다음 파일들을 찾았습니다:\n" + foundFiles.joinToString("\n")
        } else {
            "'$keyword'를 포함하는 파일을 찾지 못했습니다."
        }
    }

    // ──────────────────────────────────────────────
    // 🗂️ 패키지 구조 탐색
    // ──────────────────────────────────────────────
    @Tool
    @LLMDescription("프로젝트의 전체 패키지·폴더 구조를 트리 형태로 반환합니다. 어디에 파일을 생성할지 결정하기 전에 반드시 호출하세요.")
    fun listPackageStructure(): String {
        val sb = StringBuilder()
        fun walk(dir: File, indent: String) {
            val children = dir.listFiles()?.sorted() ?: return
            for (child in children) {
                if (child.name.startsWith(".")) continue
                if (child.isDirectory) {
                    sb.appendLine("$indent├── ${child.name}/")
                    walk(child, "$indent│   ")
                } else {
                    sb.appendLine("$indent├── ${child.name}")
                }
            }
        }
        sb.appendLine(srcRoot.canonicalPath)
        walk(srcRoot, "")
        return sb.toString()
    }

    // ──────────────────────────────────────────────
    // 👀 파일 읽기
    // ──────────────────────────────────────────────
    @Tool
    @LLMDescription("지정된 경로의 파일 내용을 읽어옵니다.")
    fun readFile(
        @LLMDescription("읽어올 파일의 상대 경로 (프로젝트 루트 기준)") path: String
    ): String {
        val file = File(path)
        return if (file.exists()) file.readText() else "파일이 존재하지 않습니다: $path"
    }

    // ──────────────────────────────────────────────
    // 📁 디렉토리 생성
    // ──────────────────────────────────────────────
    @Tool
    @LLMDescription("새로운 패키지(폴더)를 생성합니다.")
    fun createDirectory(
        @LLMDescription("생성할 폴더 경로 (예: ../app/src/main/java/com/windrr/jibrro/data/remote)") path: String
    ): String {
        val dir = File(path)
        return if (dir.mkdirs()) "폴더 생성 완료: $path" else "폴더가 이미 있거나 생성 실패: $path"
    }

    // ──────────────────────────────────────────────
    // ✍️ 파일 저장
    // ──────────────────────────────────────────────
    @Tool
    @LLMDescription("Kotlin 코드를 실제 파일로 저장합니다. 부모 디렉토리가 없으면 자동으로 생성됩니다.")
    fun writeKotlinFile(
        @LLMDescription("저장할 파일 경로 (예: ../app/src/main/java/com/windrr/jibrro/data/remote/MyApi.kt)") path: String,
        @LLMDescription("작성할 Kotlin 코드 전체 내용") content: String
    ): String {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
        return "✅ 파일 저장 성공: $path (${content.length} chars)"
    }

    // ──────────────────────────────────────────────
    // 🛠️ 파일 수정 (특정 위치에 코드 삽입 등)
    // ──────────────────────────────────────────────
    @Tool
    @LLMDescription("기존 파일의 내용을 수정합니다. 특정 텍스트를 찾아 치환하거나 코드를 추가할 때 사용합니다.")
    fun patchFile(
        @LLMDescription("수정할 파일의 상대 경로") path: String,
        @LLMDescription("찾을 텍스트 (정확히 일치해야 함)") targetContent: String,
        @LLMDescription("새로 바꿀 텍스트") replacementContent: String
    ): String {
        val file = File(path)
        if (!file.exists()) return "파일이 존재하지 않습니다: $path"

        val content = file.readText()
        if (!content.contains(targetContent)) {
            return "대상 텍스트를 찾을 수 없습니다. 정확한 텍스트를 입력했는지 확인하세요."
        }

        val newContent = content.replace(targetContent, replacementContent)
        file.writeText(newContent)
        return "✅ 파일 수정 성공: $path"
    }
}