package com.github.godark14.secretsleakdetector.inspections

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import kotlin.math.log2

class SecretLeakInspection : LocalInspectionTool() {

    data class SecretPattern(
        val name: String,
        val regex: Regex,
        val highlightGroup: Int = 0,
        val checkPlaceholderMarkers: Boolean = false,
        val checkEntropy: Boolean = false
    )

    private val patterns = listOf(
        SecretPattern("AWS Access Key", Regex("AKIA[0-9A-Z]{16}")),
        SecretPattern("AWS Secret Key", Regex("(?i)aws_secret_access_key\\s*=\\s*['\"]?[A-Za-z0-9/+=]{40}['\"]?")),
        SecretPattern("GitHub Token", Regex("gh[pousr]_[A-Za-z0-9]{36}")),
        SecretPattern(
            "Generic API Key",
            Regex("(?i)(?:api[_-]?key|secret[_-]?key|access[_-]?token)\\s*[:=]\\s*(['\"][A-Za-z0-9_\\-]{16,}['\"])"),
            highlightGroup = 1,
            checkPlaceholderMarkers = true,
            checkEntropy = true
        ),
        SecretPattern("RSA Private Key", Regex("-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----")),
        SecretPattern("Slack Token", Regex("xox[baprs]-[0-9A-Za-z-]{10,}")),
        SecretPattern("Google Cloud API Key", Regex("AIza[0-9A-Za-z_\\-]{35}")),
        SecretPattern("Google Service Account Private Key", Regex("\"private_key\"\\s*:\\s*\"-----BEGIN PRIVATE KEY-----")),
        SecretPattern("Stripe Secret Key", Regex("(?:sk|rk)_live_[0-9A-Za-z]{24,}")),
        SecretPattern("JWT", Regex("eyJ[A-Za-z0-9_-]{5,}\\.[A-Za-z0-9_-]{5,}\\.[A-Za-z0-9_-]{5,}")),
        SecretPattern(
            "Database Connection String with Password",
            Regex("(?i)(postgres(?:ql)?|mysql|mongodb(?:\\+srv)?|redis)://[^:\\s]+:([^@\\s]+)@[^\\s'\"]+"),
            highlightGroup = 2
        )
    )

    private val placeholderMarkers = listOf(
        "your_api_key", "your-api-key", "yourapikey",
        "api_key_here", "changeme", "change_me", "change-me",
        "example", "placeholder", "dummy", "sample",
        "insert_key", "insert_token", "todo", "fixme", "replace_me", "replaceme"
    )

    private val excludedPathSegments = listOf(
        "/node_modules/", "\\node_modules\\",
        "/build/", "\\build\\",
        "/.gradle/", "\\.gradle\\",
        "/.git/", "\\.git\\",
        "/.idea/", "\\.idea\\",
        "/dist/", "\\dist\\",
        "/out/", "\\out\\"
    )

    private val minEntropyBitsPerChar = 3.0

    private fun isRepeatedCharacter(rawValue: String): Boolean {
        val normalized = rawValue.trim('\'', '"')
        return normalized.isNotEmpty() && normalized.all { it == normalized[0] }
    }

    private fun containsPlaceholderMarker(rawValue: String): Boolean {
        val normalized = rawValue.trim('\'', '"').lowercase()
        return placeholderMarkers.any { normalized.contains(it) }
    }

    private fun shannonEntropyPerChar(rawValue: String): Double {
        val value = rawValue.trim('\'', '"')
        if (value.isEmpty()) return 0.0

        val frequencies = value.groupingBy { it }.eachCount()
        val length = value.length.toDouble()

        return frequencies.values.sumOf { count ->
            val probability = count / length
            -probability * log2(probability)
        }
    }

    private fun hasLowEntropy(rawValue: String): Boolean {
        return shannonEntropyPerChar(rawValue) < minEntropyBitsPerChar
    }

    private fun shouldSkipFile(file: PsiFile): Boolean {
        val virtualFile = file.virtualFile ?: return false
        val path = virtualFile.path

        if (excludedPathSegments.any { path.contains(it) }) return true

        val changeListManager = ChangeListManager.getInstance(file.project)
        if (changeListManager.isIgnoredFile(virtualFile)) return true

        return false
    }

    private class ReplaceWithPlaceholderFix(
        private val startOffset: Int,
        private val endOffset: Int,
        private val wasQuoted: Boolean
    ) : LocalQuickFix {

        override fun getFamilyName(): String = "Replace with placeholder"

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val file = descriptor.psiElement.containingFile ?: return
            val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return

            val replacement = if (wasQuoted) "\"REDACTED_SECRET\"" else "REDACTED_SECRET"

            WriteCommandAction.runWriteCommandAction(project) {
                if (endOffset <= document.textLength) {
                    document.replaceString(startOffset, endOffset, replacement)
                }
            }
        }
    }

    override fun checkFile(
        file: PsiFile,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor> {
        if (shouldSkipFile(file)) return ProblemDescriptor.EMPTY_ARRAY

        val text = file.viewProvider.contents
        val problems = mutableListOf<ProblemDescriptor>()

        for (pattern in patterns) {
            pattern.regex.findAll(text).forEach { match ->
                val targetRange = if (pattern.highlightGroup == 0) {
                    match.range
                } else {
                    match.groups[pattern.highlightGroup]?.range ?: match.range
                }

                val matchedValue = text.substring(targetRange.first, targetRange.last + 1)

                if (isRepeatedCharacter(matchedValue)) return@forEach
                if (pattern.checkPlaceholderMarkers && containsPlaceholderMarker(matchedValue)) return@forEach
                if (pattern.checkEntropy && hasLowEntropy(matchedValue)) return@forEach

                val startOffset = targetRange.first
                val endOffset = targetRange.last + 1

                val safeRange = TextRange(
                    startOffset.coerceIn(0, file.textLength),
                    endOffset.coerceIn(0, file.textLength)
                )
                if (safeRange.isEmpty) return@forEach

                val wasQuoted = matchedValue.startsWith("'") || matchedValue.startsWith("\"")

                problems.add(
                    manager.createProblemDescriptor(
                        file,
                        safeRange,
                        "Potential hardcoded secret detected: ${pattern.name}",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                        isOnTheFly,
                        ReplaceWithPlaceholderFix(safeRange.startOffset, safeRange.endOffset, wasQuoted)
                    )
                )
            }
        }
        return problems.toTypedArray()
    }
}