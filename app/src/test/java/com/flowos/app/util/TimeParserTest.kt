package com.flowos.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class TimeParserTest {

    private val now = LocalDateTime.of(2026, 9, 6, 10, 0) // Sunday, Sep 6, 2026

    @Test
    fun testToday() {
        val result = TimeParser.parseDeadline("Do this today", now)
        assertNotNull(result)
        val resultDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(result!!),
            ZoneId.systemDefault()
        )
        assertEquals(now.toLocalDate(), resultDateTime.toLocalDate())
        assertEquals(LocalTime.of(18, 0), resultDateTime.toLocalTime())
    }

    @Test
    fun testTomorrow() {
        val result = TimeParser.parseDeadline("Do this tomorrow", now)
        assertNotNull(result)
        val resultDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(result!!),
            ZoneId.systemDefault()
        )
        assertEquals(now.toLocalDate().plusDays(1), resultDateTime.toLocalDate())
        assertEquals(LocalTime.of(9, 0), resultDateTime.toLocalTime())
    }

    @Test
    fun testTonight() {
        val result = TimeParser.parseDeadline("Do this tonight", now)
        assertNotNull(result)
        val resultDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(result!!),
            ZoneId.systemDefault()
        )
        assertEquals(now.toLocalDate(), resultDateTime.toLocalDate())
        assertEquals(LocalTime.of(20, 0), resultDateTime.toLocalTime())
    }

    @Test
    fun testExplicitDate() {
        val result = TimeParser.parseDeadline("Meeting on September 10 at 2 pm", now)
        assertNotNull(result)
        val resultDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(result!!),
            ZoneId.systemDefault()
        )
        assertEquals(LocalDate.of(2026, 9, 10), resultDateTime.toLocalDate())
        assertEquals(LocalTime.of(14, 0), resultDateTime.toLocalTime())
    }

    @Test
    fun testInvalidInput() {
        val result = TimeParser.parseDeadline("Just some text", now)
        assertNull(result)
    }

    @Test
    fun testEmptyInput() {
        val result = TimeParser.parseDeadline("", now)
        assertNull(result)
    }

    @Test
    fun testHumanLabel() {
        val epoch = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val label = TimeParser.humanLabel(epoch, now.toLocalDate())
        assertEquals("Today · 10:00 AM", label)
    }
}
