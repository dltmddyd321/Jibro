import java.nio.file.Files
import java.nio.file.Path
import java.io.File
import java.util.stream.Collectors

plugins {
    id("com.google.firebase.crashlytics") version "3.0.6" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

tasks.register("printPackageTree") {
    group = "report"
    description = "Prints tree under app/src/main/java/com/windrr/jibrro"
    val ignore = listOf(".git", ".idea", "build", ".gradle")
    val maxDepth = 6

    doLast {
        fun shouldIgnore(p: Path): Boolean {
            val norm = p.toString().replace(File.separatorChar, '/')
            return ignore.any { norm.contains("/$it") } || p.fileName.toString().endsWith(".iml")
        }
        fun walk(path: Path, prefix: String = "", depth: Int = 0) {
            if (depth > maxDepth) return
            val children = Files.list(path).use { s ->
                // JDK 17이면 .toList() 사용 가능; 호환 위해 Collectors 사용
                s.filter { !shouldIgnore(it) }
                    .sorted()
                    .collect(Collectors.toList())
            }
            children.forEachIndexed { i, child ->
                val last = i == children.size - 1
                val connector = if (last) "└── " else "├── "
                println("$prefix$connector${child.fileName}")
                if (Files.isDirectory(child)) {
                    walk(child, prefix + if (last) "    " else "│   ", depth + 1)
                }
            }
        }

        // kotlin+java 뷰는 실제로 java 또는 kotlin 폴더임
        val candidates = listOf(
            "app/src/main/java/com/windrr/jibrro",
            "app/src/main/kotlin/com/windrr/jibrro"
        ).map { file(it).toPath() }
            .filter { Files.exists(it) }

        if (candidates.isEmpty()) {
            println("Not found: app/src/main/java/com/windrr/jibrro")
            return@doLast
        }

        candidates.forEach { root ->
            println(root.toFile().path)
            walk(root)
            println()
        }
    }
}