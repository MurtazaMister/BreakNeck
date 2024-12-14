package com.goodwy.dialer.services

data class MotionData(
    val timestamp: Double,
    val accelerometer: Double,
    val gyroscope: Double
)

class MotionDetector {
    companion object {
        private const val SIMILARITY_THRESHOLD = 0.85
        private const val WINDOW_SIZE = 10
        const val LEFT_TILT_TYPE = 2
        const val RIGHT_TILT_TYPE = 1
    }

    val leftTiltTemplate = listOf(
        MotionData(1.26, 1.967259, 0.003278),
        MotionData(1.27, 2.029058, 0.004886),
        MotionData(1.28, 2.088658, 0.008427),
        MotionData(1.29, 2.145232, 0.015757),
        MotionData(1.30, 2.197986, 0.028441),
        MotionData(2.70, 2.687374, 0.076279),
        MotionData(2.71, 2.686894, 0.070283),
        MotionData(2.72, 2.685937, 0.064512),
        MotionData(2.73, 2.683655, 0.058970),
        MotionData(2.74, 2.679312, 0.053649)
    )

    val rightTiltTemplate = listOf(
        MotionData(1.41, 1.411033, -0.010246),
        MotionData(1.42, 1.413590, -0.021940),
        MotionData(1.43, 1.416532, -0.032771),
        MotionData(1.44, 1.418789, -0.042611),
        MotionData(1.45, 1.419307, -0.051452),
        MotionData(2.85, 0.742067, 0.262877),
        MotionData(2.86, 0.786136, 0.225038),
        MotionData(2.87, 0.830647, 0.186015),
        MotionData(2.88, 0.872406, 0.144590),
        MotionData(2.89, 0.908515, 0.099754)
    )

    fun detectMotion(accData: FloatArray, gyroData: FloatArray, timestamps: FloatArray): List<Triple<Int, Int, Int>> {
        println("chay: Starting motion detection - acc: ${accData.size}, gyro: ${gyroData.size}, time: ${timestamps.size}")

        if (accData.size != gyroData.size || accData.size != timestamps.size) {
            println("Data size mismatch - acc: ${accData.size}, gyro: ${gyroData.size}, time: ${timestamps.size}")
            return emptyList()
        }

        val motions = mutableListOf<Triple<Int, Int, Int>>()

        for (i in 0..accData.size - WINDOW_SIZE) {
            val windowAcc = accData.slice(i until i + WINDOW_SIZE)
            val windowGyro = gyroData.slice(i until i + WINDOW_SIZE)

            val leftSimilarity = compareWithTemplate(windowAcc, windowGyro, leftTiltTemplate)
            val rightSimilarity = compareWithTemplate(windowAcc, windowGyro, rightTiltTemplate)

            println("chay: Window $i - Left: $leftSimilarity, Right: $rightSimilarity")

            when {
                leftSimilarity > SIMILARITY_THRESHOLD -> {
                    println("Left tilt detected at index $i (similarity: $leftSimilarity)")
                    motions.add(Triple(LEFT_TILT_TYPE, i, i + WINDOW_SIZE))
                }
                rightSimilarity > SIMILARITY_THRESHOLD -> {
                    println("Right tilt detected at index $i (similarity: $rightSimilarity)")
                    motions.add(Triple(RIGHT_TILT_TYPE, i, i + WINDOW_SIZE))
                }
            }
        }

        val mergedMotions = mergeOverlappingDetections(motions)
        println("chay: Detection complete - found ${mergedMotions.size} motions")
        return mergedMotions
    }

    private fun compareWithTemplate(
        accWindow: List<Float>,
        gyroWindow: List<Float>,
        template: List<MotionData>
    ): Double {
        var similarity = 0.0
        val normalizedAcc = normalizeData(accWindow)
        val normalizedGyro = normalizeData(gyroWindow)

        template.take(WINDOW_SIZE).forEachIndexed { index, templateData ->
            similarity += calculateSimilarity(
                normalizedAcc[index],
                normalizedGyro[index],
                templateData.accelerometer,
                templateData.gyroscope
            )
        }

        return similarity / WINDOW_SIZE
    }

    private fun normalizeData(data: List<Float>): List<Double> {
        val max = data.maxOrNull() ?: 1f
        val min = data.minOrNull() ?: 0f
        val diff = max - min
        val range = if (diff != 0f) diff else 1f
        return data.map { ((it - min) / range).toDouble() }
    }

    private fun calculateSimilarity(
        accValue: Double,
        gyroValue: Double,
        templateAccValue: Double,
        templateGyroValue: Double
    ): Double {
        val accDiff = Math.abs(accValue - templateAccValue)
        val gyroDiff = Math.abs(gyroValue - templateGyroValue)
        return 1.0 - (accDiff + gyroDiff) / 2
    }

    private fun mergeOverlappingDetections(
        detections: List<Triple<Int, Int, Int>>
    ): List<Triple<Int, Int, Int>> {
        if (detections.isEmpty()) return detections

        val sorted = detections.sortedBy { it.second }
        val merged = mutableListOf(sorted[0])

        for (i in 1 until sorted.size) {
            val current = sorted[i]
            val last = merged.last()

            if (current.second <= last.third) {
                if (current.first == last.first) {
                    merged[merged.lastIndex] = Triple(
                        last.first,
                        last.second,
                        maxOf(last.third, current.third)
                    )
                }
            } else {
                merged.add(current)
            }
        }

        return merged
    }
}
