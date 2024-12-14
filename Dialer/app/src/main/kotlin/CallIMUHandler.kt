package com.goodwy.dialer.services

import android.telecom.CallAudioState
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.goodwy.dialer.BluetoothManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class CallIMUHandler(private val service: CallService) {
    private var bluetoothManager: BluetoothManager? = null
    private var imuCollectionJob: Job? = null
    private val isCollecting = AtomicBoolean(false)

    private val pythonModule: PyObject by lazy {
        val py = Python.getInstance()
        py.getModule("data_analysis_breakneck")
    }

    fun handleAudioRouteChange(audioState: CallAudioState) {
        when (audioState.route) {
            CallAudioState.ROUTE_BLUETOOTH -> {
                println("chay: handling bluetooth audio route")
//                startIMUCollection()
                logGestureType(parseGesture(pickLastGesture()))
            }
            CallAudioState.ROUTE_SPEAKER -> {
                println("chay: handling speaker audio route")
                val gesture = pickLastGesture()
                gesture.asList().forEach {
                    println("chay: picked gesture is $it and its type is ${it.get("ty")}")
                }
                logGestureType(parseGesture(pickLastGesture()))
            }
            CallAudioState.ROUTE_EARPIECE -> {
                println("chay: handling ear piece audio route")
                logGestureType(parseGesture(pickLastGesture()))
            }
            else -> {
                println("chay: not performing any gesture recognition")
                stopIMUCollection()
            }
        }
    }

    private fun logGestureType(data: MatchResult?) {
        when(data?.matchType) {
            1 -> {
                println("chay: right tilt to accept the call")
            }
            2 -> {
                println("chay: left tilt to reject the call")
            }
            3 -> {}
            4 -> {}
         }
    }

    private fun parseGesture(gestureData: PyObject): MatchResult? {
        return try {
            val startIndex = gestureData.get("start_index")?.toInt()
            val endIndex = gestureData.get("end_index")?.toInt()
            val dtwDistance = gestureData.get("dtw_distance")?.toDouble()
            // Extract the enum value directly from the string representation
            val typeString = gestureData.get("type").toString()
            val matchType = when {
                typeString.contains("LEFT_TILT") -> 2
                typeString.contains("RIGHT_TILT") -> 1
                typeString.contains("FRONT_NOD") -> 3
                typeString.contains("BACK_NOD") -> 4
                else -> null
            }

            MatchResult(
                startIndex = startIndex,
                endIndex = endIndex,
                dtwDistance = dtwDistance,
                matchType = matchType
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Update the MatchResult data class to be more explicit
    data class MatchResult(
        val startIndex: Int?,
        val endIndex: Int?,
        val dtwDistance: Double?,
        val matchType: Int?  // 1: RIGHT_TILT, 2: LEFT_TILT, 3: FRONT_NOD, 4: BACK_NOD
    )

    // Optional: Add an enum for better type safety
    enum class GestureType(val value: Int) {
        RIGHT_TILT(1),
        LEFT_TILT(2),
        FRONT_NOD(3),
        BACK_NOD(4);

        companion object {
            fun fromInt(value: Int) = values().firstOrNull { it.value == value }
        }
    }

    private fun startIMUCollection() {
        if (isCollecting.get()) return
        bluetoothManager = BluetoothManager(service)
        bluetoothManager?.connectToIMU()
        isCollecting.set(true)
        imuCollectionJob = CoroutineScope(Dispatchers.Default).launch {
            bluetoothManager?.imuData?.collectLatest { imuData ->
                imuData?.let { processIMUData(it) }
            }
        }
    }

    private fun processIMUData(imuData: BluetoothManager.IMUData) {
        try {
            val (accel, gyro, timestamp) = imuData
            val elapsedSeconds = (timestamp - startTimestamp) / 1000.0

            timestamps.add(elapsedSeconds)
            accX.add(accel.first)
            accY.add(accel.second)
            accZ.add(accel.third)
            gyroX.add(gyro.first)
            gyroY.add(gyro.second)
            gyroZ.add(gyro.third)

            if (timestamps.size >= WINDOW_SIZE) {
                // TODO: modify stream code as needed

                // Clear old data while keeping some overlap
                val overlap = WINDOW_SIZE / 2
                timestamps.drop(overlap)
                accX.drop(overlap)
                accY.drop(overlap)
                accZ.drop(overlap)
                gyroX.drop(overlap)
                gyroY.drop(overlap)
                gyroZ.drop(overlap)
            }
        } catch (e: Exception) {
            println("chay: Error processing IMU data: ${e.message}")
        }
    }

    private fun stopIMUCollection() {
        println("chay: stopping imu collection")
        isCollecting.set(false)
        imuCollectionJob?.cancel()
        imuCollectionJob = null
        bluetoothManager?.disconnect()
        bluetoothManager = null
        clearData()
    }

    private fun clearData() {
        timestamps.clear()
        accX.clear()
        accY.clear()
        accZ.clear()
        gyroX.clear()
        gyroY.clear()
        gyroZ.clear()
    }

    private fun getGesturesFromPython(accPath: String, gyroPath: String): List<MotionMatch> {
        val result = pythonModule.callAttr("match_tilt_from_kotlin", accPath, gyroPath)
        // Convert Python list to Kotlin list
        return convertPyMatchesToKotlin(result)
    }

    private fun displayResult() {
        val python = Python.getInstance()
        val module = python.getModule("data_analysis_breakneck")
        val accData = module.get("df_acc_test_left_right_tilt_filtered")
        val gyroData = module.get("df_gyro_test_left_right_tilt_filtered")
        val results = module.callAttr("match_tilt", accData, gyroData)

        results.asList().forEach {
            println("chay: result is $it")
        }
    }

    private fun displayLeftTiltResult() {
        val python = Python.getInstance()
        val module = python.getModule("data_analysis_breakneck")
        val accData = module.get("df_acc_single_left_tilt_filtered")
        val gyroData = module.get("df_gyro_single_left_tilt_filtered")
        val results = module.callAttr("match_tilt", accData, gyroData)

        results.asList().forEach {
            println("chay: result is $it")
        }
        println("chay: this will reject the call")
    }


    fun pickLastGesture(): PyObject {
        val python = Python.getInstance()
        val module = python.getModule("data_analysis_breakneck")
        val accData = module.get("df_acc_test_left_right_tilt_filtered")
        val gyroData = module.get("df_gyro_test_left_right_tilt_filtered")
        val results = module.callAttr("match_tilt", accData, gyroData)
        return results
    }

    private fun detectGesture() {
        try {
            val accPath = "app://Right tilt/Accelerometer.csv"
            val gyroPath = "app://Right tilt/Gyroscope.csv"
            println("chay: Starting detection with paths: $accPath, $gyroPath")

            val matches = getGesturesFromPython(accPath, gyroPath)
            println("chay: Got matches: $matches")

            for (match in matches) {
                println("chay: Detected ${match.type} from ${match.startIndex} to ${match.endIndex}")
            }
        } catch (e: Exception) {
            println("chay: exception in detect motion ${e.message}")
            e.printStackTrace()  // This will print the full stack trace
        }
    }

    private fun convertPyMatchesToKotlin(pyMatches: PyObject): List<MotionMatch> {
        val matches = mutableListOf<MotionMatch>()

        for (i in 0 until pyMatches.asList().size) {
            val match = pyMatches.asList()[i]
            matches.add(
                MotionMatch(
                    startIndex = match.get("start_index")?.toInt(),
                    endIndex = match.get("end_index")?.toInt(),
                    dtwDistance = match.get("dtw_distance")?.toDouble(),
                    type = convertMotionType(match["type"].toString())
                )
            )
        }

        return matches
    }

    private fun convertMotionType(pyType: String): MotionType {
        return when {
            pyType.contains("RIGHT_TILT") -> MotionType.RIGHT_TILT
            pyType.contains("LEFT_TILT") -> MotionType.LEFT_TILT
            pyType.contains("FRONT_NOD") -> MotionType.FRONT_NOD
            pyType.contains("BACK_NOD") -> MotionType.BACK_NOD
            else -> throw IllegalArgumentException("Unknown motion type: $pyType")
        }
    }

    // Data classes for motion detection results
    data class MotionMatch(
        val startIndex: Int?,
        val endIndex: Int?,
        val dtwDistance: Double?,
        val type: MotionType?
    )

    enum class MotionType {
        RIGHT_TILT,
        LEFT_TILT,
        FRONT_NOD,
        BACK_NOD
    }

    companion object {
        private const val WINDOW_SIZE = 150
        private var startTimestamp: Long = 0
        private val timestamps = mutableListOf<Double>()
        private val accX = mutableListOf<Float>()
        private val accY = mutableListOf<Float>()
        private val accZ = mutableListOf<Float>()
        private val gyroX = mutableListOf<Float>()
        private val gyroY = mutableListOf<Float>()
        private val gyroZ = mutableListOf<Float>()
    }
}




