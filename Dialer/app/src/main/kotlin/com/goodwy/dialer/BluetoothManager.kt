package com.goodwy.dialer

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.chaquo.python.Python
import com.goodwy.dialer.services.AccelerometerData
import com.goodwy.dialer.services.GyroscopeData
import com.goodwy.dialer.services.ImuDataProcessor
import com.goodwy.dialer.services.ImuReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer
import java.util.UUID

class BluetoothManager(private val context: Context) {
    companion object {
        private const val DEVICE_ADDRESS = "EA:2C:29:6A:8E:3D"
        // Since your device is already paired, we can discover the UUIDs
        private var IMU_SERVICE_UUID: String? = null
        private var IMU_CHARACTERISTIC_UUID: String? = null
    }
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val imuDataProcessor = ImuDataProcessor()

    private val _imuData = MutableStateFlow<IMUData?>(null)
    val imuData: StateFlow<IMUData?> = _imuData

    data class IMUData (val accelerometer: Triple<Float, Float, Float>,
                        val gyroscope: Triple<Float, Float, Float>,
                        val timestamp: Long
    )

    fun connectToIMU() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS)?.connectGatt(
            context,
            false,
            gattCallback
        )
    }

    private val gattCallback = object: BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    bluetoothGatt = gatt
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    bluetoothGatt = null
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                val service = gatt.getService(UUID.fromString(IMU_SERVICE_UUID))
                val characteristic = service?.getCharacteristic(UUID.fromString(IMU_CHARACTERISTIC_UUID))
                characteristic?.let {
                    gatt.setCharacteristicNotification(it, true)
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            val reading = parseImuData(value)
            imuDataProcessor.processReading(reading)
        }
    }

    private fun parseImuData(data: ByteArray): ImuReading {
        return ImuReading(
            timestamp = System.currentTimeMillis(),
            accelerometer = AccelerometerData(
                x = ByteBuffer.wrap(data.slice(0..3).toByteArray()).float,
                y = ByteBuffer.wrap(data.slice(4..7).toByteArray()).float,
                z = ByteBuffer.wrap(data.slice(8..11).toByteArray()).float
            ),
            gyroscope = GyroscopeData(
                x = ByteBuffer.wrap(data.slice(12..15).toByteArray()).float,
                y = ByteBuffer.wrap(data.slice(16..19).toByteArray()).float,
                z = ByteBuffer.wrap(data.slice(20..23).toByteArray()).float
            )
        )
    }

    fun analyzeTiltPatterns() {
        val python = Python.getInstance()

        // First, export the collected data to Python
        val setupCode = imuDataProcessor.exportToPython()

        // Execute the setup code


        // Now run your existing analysis
        val module = python.getModule("data_analysis_breakneck")
        val results = module.callAttr("match_tilt",
            python.getModule("__main__").get("acc_data"),
            python.getModule("__main__").get("gyro_data")
        )

        // Process results
        println("Analysis results: $results")
    }

    fun disconnect() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
