package org.forestdept.seemsai

import android.graphics.RectF
import org.forestdept.seemsai.engine.TemporalValidationEngine
import org.forestdept.seemsai.model.DetectedTarget
import org.forestdept.seemsai.model.RawDetection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TemporalValidationTest {

    private lateinit var temporalEngine: TemporalValidationEngine

    @Before
    fun setUp() {
        temporalEngine = TemporalValidationEngine(windowSize = 5, requiredThreshold = 3)
    }

    private fun createElephantDetection(confidence: Float = 0.90f): RawDetection {
        return RawDetection(
            target = DetectedTarget.ELEPHANT,
            confidence = confidence,
            boundingBox = RectF(0.2f, 0.2f, 0.7f, 0.7f)
        )
    }

    private fun createPersonDetection(): RawDetection {
        return RawDetection(
            target = DetectedTarget.PERSON,
            confidence = 0.85f,
            boundingBox = RectF(0.4f, 0.4f, 0.6f, 0.6f)
        )
    }

    @Test
    fun testInitialState_isNotConfirmed() {
        val result = temporalEngine.processFrame(null)
        assertFalse(result.isConfirmed)
        assertEquals(0, result.positiveCount)
        assertEquals(1, result.totalFramesInWindow)
        assertEquals("NO HAZARD DETECTED", result.statusDescription)
    }

    @Test
    fun testOneDetection_isValidating() {
        val result = temporalEngine.processFrame(createElephantDetection())
        assertFalse(result.isConfirmed)
        assertEquals(1, result.positiveCount)
        assertTrue(result.statusDescription.contains("VALIDATING"))
    }

    @Test
    fun testTwoDetections_isValidating() {
        temporalEngine.processFrame(createElephantDetection())
        val result = temporalEngine.processFrame(createElephantDetection())
        assertFalse(result.isConfirmed)
        assertEquals(2, result.positiveCount)
        assertTrue(result.statusDescription.contains("VALIDATING"))
    }

    @Test
    fun testThreeDetectionsInFiveFrames_escalatesToConfirmed() {
        // Frame 1: Elephant (+)
        temporalEngine.processFrame(createElephantDetection())
        // Frame 2: Null (-)
        temporalEngine.processFrame(null)
        // Frame 3: Elephant (+)
        temporalEngine.processFrame(createElephantDetection())
        // Frame 4: Null (-)
        temporalEngine.processFrame(null)
        // Frame 5: Elephant (+) -> 3 out of 5!
        val result = temporalEngine.processFrame(createElephantDetection())

        assertTrue(result.isConfirmed)
        assertEquals(3, result.positiveCount)
        assertEquals(5, result.totalFramesInWindow)
        assertTrue(result.statusDescription.contains("CONFIRMED ELEPHANT EVENT"))
    }

    @Test
    fun testNonElephantDetection_doesNotTriggerConfirmation() {
        // Feed 5 person frames
        for (i in 1..4) {
            temporalEngine.processFrame(createPersonDetection())
        }
        val result = temporalEngine.processFrame(createPersonDetection())
        assertFalse(result.isConfirmed)
        assertEquals(0, result.positiveCount)
    }

    @Test
    fun testLowConfidenceElephant_rejectedAsNoise() {
        // Elephant with low confidence (0.15f < 0.30f threshold)
        for (i in 1..4) {
            temporalEngine.processFrame(createElephantDetection(confidence = 0.15f))
        }
        val result = temporalEngine.processFrame(createElephantDetection(confidence = 0.15f))
        assertFalse(result.isConfirmed)
        assertEquals(0, result.positiveCount)
    }

    @Test
    fun testRollingWindowDeEscalation() {
        // 3 positive detections -> Confirmed
        temporalEngine.processFrame(createElephantDetection())
        temporalEngine.processFrame(createElephantDetection())
        val confirmedResult = temporalEngine.processFrame(createElephantDetection())
        assertTrue(confirmedResult.isConfirmed)

        // Feed 3 consecutive null frames -> positive count drops to 1 / 5 -> De-escalates!
        temporalEngine.processFrame(null)
        temporalEngine.processFrame(null)
        val deEscalatedResult = temporalEngine.processFrame(null)

        assertFalse(deEscalatedResult.isConfirmed)
        assertEquals(2, deEscalatedResult.positiveCount)
    }

    @Test
    fun testReset_clearsHistory() {
        temporalEngine.processFrame(createElephantDetection())
        temporalEngine.processFrame(createElephantDetection())
        temporalEngine.reset()

        val freshResult = temporalEngine.processFrame(null)
        assertEquals(0, freshResult.positiveCount)
        assertFalse(freshResult.isConfirmed)
    }
}
