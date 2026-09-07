package com.flowos.app.ai

import com.flowos.app.domain.model.IntentCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Intent detection is deterministic keyword scoring — these tests pin it. */
class IntentPredictorTest {

    @Test
    fun `prepare for review is detected`() {
        val intent = IntentPredictor.predict("We need to prepare for the project review tomorrow")
        assertEquals(IntentCategory.PREPARE_REVIEW, intent.category)
        assertEquals("prepare for review", intent.label)
        assertTrue(intent.confidence > 0.5)
        assertTrue(intent.suggestedActions.isNotEmpty())
    }

    @Test
    fun `send document is detected`() {
        val intent = IntentPredictor.predict("Send the updated document to Prakash tonight")
        assertEquals(IntentCategory.SEND_DOCUMENT, intent.category)
        assertEquals("send document", intent.label)
    }

    @Test
    fun `submit assignment is detected`() {
        val intent = IntentPredictor.predict("Submit the assignment before the deadline")
        assertEquals(IntentCategory.SUBMIT_ASSIGNMENT, intent.category)
    }

    @Test
    fun `follow up is detected`() {
        val intent = IntentPredictor.predict("Follow up with Prakash about the report")
        assertEquals(IntentCategory.FOLLOW_UP, intent.category)
    }

    @Test
    fun `project and deadline enrich the prediction`() {
        val intent = IntentPredictor.predict(
            text = "prepare for review",
            projectName = "Architecture Review",
            deadlineEpochMillis = 123L,
        )
        assertEquals("Architecture Review", intent.relatedProject)
        assertEquals(123L, intent.relatedDeadlineEpochMillis)
    }

    @Test
    fun `identical input always yields identical output`() {
        val a = IntentPredictor.predict("Finish architecture and send document")
        val b = IntentPredictor.predict("Finish architecture and send document")
        assertEquals(a, b)
    }

    @Test
    fun `plain verbs fall back to general task with low confidence`() {
        val intent = IntentPredictor.predict("something vague happened today")
        assertEquals(IntentCategory.GENERAL_TASK, intent.category)
        assertTrue(intent.confidence <= 0.5)
    }
}
