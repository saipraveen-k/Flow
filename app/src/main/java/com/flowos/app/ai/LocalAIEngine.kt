package com.flowos.app.ai

import com.flowos.app.domain.model.AIAnalysisResult
import com.flowos.app.domain.model.CaptureDraft
import com.flowos.app.domain.model.ExtractedDeadline
import com.flowos.app.domain.model.ExtractedDependency
import com.flowos.app.domain.model.ExtractedTask
import com.flowos.app.domain.model.IntentType
import com.flowos.app.domain.model.Priority
import com.flowos.app.util.TimeParser
import java.time.LocalDateTime

/**
 * Fully offline, rule-based understanding engine. It runs deterministic
 * heuristics (sentence segmentation, verb-phrase detection, capitalised-name
 * extraction, keyword scoring and the rule-based [TimeParser]) — no network,
 * no bundled model. The same interface a Qualcomm/Snapdragon runtime engine
 * would implement later, so it can be swapped without touching the UI.
 */
class LocalAIEngine : AIEngine {

    override val id: String = "local-heuristic"

    override val processingLabel: String = "Processing on device"

    override suspend fun analyze(draft: CaptureDraft): AIAnalysisResult {
        val text = draft.text.trim()
        if (text.isEmpty()) {
            return AIAnalysisResult(
                summary = "Empty capture",
                intent = IntentType.UNKNOWN,
                priority = Priority.LOW,
                people = emptyList(),
                deadlines = emptyList(),
                tasks = emptyList(),
                dependencies = emptyList(),
                project = null,
                confidence = 0.0,
                sourceType = draft.sourceType,
            )
        }

        val now = LocalDateTime.now()
        val sentences = splitSentences(text)

        val people = extractPeople(text)
        val deadlines = sentences.mapNotNull { sentence ->
            TimeParser.parseDeadline(sentence, now)?.let { millis ->
                ExtractedDeadline(
                    label = TimeParser.humanLabel(millis),
                    epochMillis = millis,
                    isEvent = detectIntent(sentence) == IntentType.MEETING,
                )
            }
        }.distinctBy { it.epochMillis }

        val tasks = sentences.mapNotNull { sentence ->
            buildTask(sentence, people, now)
        }
        val project = detectProject(text)

        return AIAnalysisResult(
            summary = buildSummary(sentences, tasks),
            intent = detectIntent(text),
            priority = detectPriority(text),
            people = people,
            deadlines = deadlines,
            tasks = tasks,
            dependencies = inferDependencies(tasks),
            project = project,
            confidence = computeConfidence(tasks, deadlines, people),
            sourceType = draft.sourceType,
            detectedIntent = IntentPredictor.predict(
                text = text,
                projectName = project,
                deadlineEpochMillis = deadlines.minOfOrNull { it.epochMillis },
            ),
        )
    }

    private fun buildSummary(sentences: List<String>, tasks: List<ExtractedTask>): String {
        return if (tasks.isEmpty()) {
            "Captured ${sentences.size} sentences, but no specific tasks were identified."
        } else {
            "Identified ${tasks.size} tasks: " + tasks.joinToString(", ") { it.title }
        }
    }

    // ---- Segmentation ------------------------------------------------------

    private fun splitSentences(text: String): List<String> =
        text.split(Regex("[.!?\n;]+"))
            .map { it.trim() }
            .filter { it.length > 2 }

    // ---- Task extraction ----------------------------------------------------

    private fun buildTask(
        sentence: String,
        people: List<String>,
        now: LocalDateTime,
    ): ExtractedTask? {
        val lower = sentence.lowercase()

        val isEvent = EVENT_KEYWORDS.any { it in lower }
        val hasActionVerb = ACTION_VERBS.any { " $it " in " $lower " || lower.startsWith("$it ") }

        if (!hasActionVerb && !isEvent) return null

        val deadline = TimeParser.parseDeadline(sentence, now)

        val title = buildTaskTitle(sentence)
        val other = people.firstOrNull { person ->
            lower.contains(person.lowercase()) && person != "You"
        }

        return ExtractedTask(
            title = title,
            description = sentence,
            priority = detectPriority(sentence),
            deadlineEpochMillis = deadline,
            deadlineLabel = deadline?.let(TimeParser::humanLabel),
            person = other,
            requiresSharing = SHARE_KEYWORDS.any { it in lower },
            requiresFile = FILE_KEYWORDS.any { it in lower },
        )
    }

    /** Trims leading connective words so titles start with the action. */
    private fun buildTaskTitle(sentence: String): String {
        val cleaned = sentence
            .replace(Regex("^(and|then|also|please|pls|so)\\s+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s+i have\\b.*$", RegexOption.IGNORE_CASE), "")
            .trim()
        return cleaned.take(80).ifBlank { sentence.take(80) }
    }

    // ---- Entity detection ----------------------------------------------------

    private fun extractPeople(text: String): List<String> {
        val detected = mutableSetOf<String>()
        // "send X to Prakash", "share with Priya", "call Rahul"
        val prepositionNames = Regex(
            """(?:to|with|for|from|ask|call|message|tell)\s+([A-Z][a-z]{2,})""",
        ).findAll(text)
        prepositionNames.forEach { detected += it.groupValues[1] }

        // Remaining capitalised words that are not sentence starters or known
        // non-person words (naive but useful heuristic for messages).
        KNOWN_NON_PERSON_WORDS.forEach { word -> detected.remove(word) }

        if (detected.isNotEmpty()) detected.add("You")
        return detected.toList()
    }

    private fun detectIntent(text: String): IntentType {
        val lower = text.lowercase()
        return when {
            MEETING_KEYWORDS.any { it in lower } -> IntentType.MEETING
            NOTICE_KEYWORDS.any { it in lower } -> IntentType.NOTICE
            FOLLOW_UP_KEYWORDS.any { it in lower } -> IntentType.FOLLOW_UP
            ACTION_VERBS.any { verb -> Regex("\\b$verb\\b").containsMatchIn(lower) } ->
                IntentType.TASK_ASSIGNMENT

            else -> IntentType.UNKNOWN
        }
    }

    private fun detectPriority(text: String): Priority {
        val lower = text.lowercase()
        return when {
            HIGH_PRIORITY_KEYWORDS.any { it in lower } -> Priority.HIGH
            LOW_PRIORITY_KEYWORDS.any { it in lower } -> Priority.LOW
            else -> Priority.MEDIUM
        }
    }

    private fun detectProject(text: String): String? {
        val quoted = Regex("\"([^\"]{3,40})\"").find(text)?.groupValues?.get(1)
        if (quoted != null) return quoted
        val projectMention = Regex(
            """(?:project|for)\s+([A-Z][A-Za-z0-9]{2,}(?:\s+[A-Z][A-Za-z0-9]{2,})?)""",
        ).find(text)?.groupValues?.get(1)
        return projectMention?.takeIf { it !in KNOWN_NON_PERSON_WORDS }
    }

    // ---- Dependencies --------------------------------------------------------

    /**
     * Rule: a share/notify task depends on the production task in the same
     * capture, because you cannot send what you have not made.
     */
    private fun inferDependencies(tasks: List<ExtractedTask>): List<ExtractedDependency> {
        val dependencies = mutableListOf<ExtractedDependency>()
        tasks.forEachIndexed { toIndex, task ->
            val isShare = task.requiresSharing ||
                Regex("\\b(send|share|forward|email)\\b", RegexOption.IGNORE_CASE)
                    .containsMatchIn(task.title)
            if (isShare && toIndex > 0) {
                val producer = tasks.subList(0, toIndex).lastOrNull { candidate ->
                    !candidate.requiresSharing &&
                        Regex("\\b(finish|prepare|draft|write|build|make|update|complete|finalize)\\b", RegexOption.IGNORE_CASE)
                            .containsMatchIn(candidate.title)
                }
                if (producer != null) {
                    val fromIndex = tasks.indexOf(producer)
                    dependencies += ExtractedDependency(
                        fromIndex = fromIndex,
                        toIndex = toIndex,
                        reason = "Finish \"${producer.title}\" before sending",
                    )
                }
            }
        }
        return dependencies
    }

    private fun computeConfidence(
        tasks: List<ExtractedTask>,
        deadlines: List<ExtractedDeadline>,
        people: List<String>,
    ): Double {
        var score = 0.35
        if (tasks.isNotEmpty()) score += 0.25
        if (deadlines.isNotEmpty()) score += 0.2
        if (people.isNotEmpty()) score += 0.1
        if (tasks.size > 1) score += 0.05
        return score.coerceIn(0.0, 0.85) // heuristics are never claimed as certain
    }

    private companion object {
        val ACTION_VERBS = listOf(
            "finish", "complete", "send", "share", "prepare", "review", "write",
            "draft", "submit", "call", "schedule", "buy", "pick", "update", "fix",
            "clean", "book", "pay", "reply", "follow", "email", "remind", "bring",
            "make", "build", "finalize", "finalise", "plan", "upload", "deliver",
        )

        val EVENT_KEYWORDS = listOf("meeting", "review will be held", "session", "standup", "interview", "event")

        val MEETING_KEYWORDS = listOf("meeting", "review", "standup", "call with", "interview")

        val NOTICE_KEYWORDS = listOf("notice", "announcement", "all students", "must bring", "circular")

        val FOLLOW_UP_KEYWORDS = listOf("follow up", "check on", "ping", "remind")

        val HIGH_PRIORITY_KEYWORDS = listOf("urgent", "asap", "immediately", "tonight", "important", "critical", "eod")

        val LOW_PRIORITY_KEYWORDS = listOf("someday", "eventually", "no rush", "whenever", "low priority")

        val SHARE_KEYWORDS = listOf("send", "share", "forward", "email", "whatsapp")

        val FILE_KEYWORDS = listOf("document", "file", "diagram", "presentation", "deck", "pdf", "report")

        val KNOWN_NON_PERSON_WORDS = setOf(
            "Project", "Review", "Tomorrow", "Tonight", "Today", "Internal", "September",
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
            "Students", "Presentation", "Architecture", "Demo", "Hackathon",
        )
    }
}
