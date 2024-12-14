package com.goodwy.dialer.services

data class ImuReading(
    val timestamp: Long,
    val accelerometer: AccelerometerData,
    val gyroscope: GyroscopeData
)

data class AccelerometerData(
    val x: Float,
    val y: Float,
    val z: Float
)

data class GyroscopeData(
    val x: Float,
    val y: Float,
    val z: Float
)

enum class MatchType {
    RIGHT_TILT,
    LEFT_TILT,
    FORWARD_TILT,
    BACKWARD_TILT
}

data class MatchResult(
    val startIndex: Int,
    val endIndex: Int,
    val dtwDistance: Double,
    val type: MatchType
)

class ImuDataProcessor {
    private val accelerometerReadings = mutableListOf<AccelerometerData>()
    private val gyroscopeReadings = mutableListOf<GyroscopeData>()
    private val timestamps = mutableListOf<Long>()

    fun processReading(reading: ImuReading) {
        timestamps.add(reading.timestamp)
        accelerometerReadings.add(reading.accelerometer)
        gyroscopeReadings.add(reading.gyroscope)
    }

    fun exportToPython(): String {
        // Convert the collected data to Python-compatible format
        return """
            import pandas as pd
            import numpy as np
            
            acc_data = pd.DataFrame({
                'timestamp': ${timestamps},
                'x': ${accelerometerReadings.map { it.x }},
                'y': ${accelerometerReadings.map { it.y }},
                'z': ${accelerometerReadings.map { it.z }}
            })
            
            gyro_data = pd.DataFrame({
                'timestamp': ${timestamps},
                'x': ${gyroscopeReadings.map { it.x }},
                'y': ${gyroscopeReadings.map { it.y }},
                'z': ${gyroscopeReadings.map { it.z }}
            })
        """.trimIndent()
    }

    fun clear() {
        accelerometerReadings.clear()
        gyroscopeReadings.clear()
        timestamps.clear()
    }
}
