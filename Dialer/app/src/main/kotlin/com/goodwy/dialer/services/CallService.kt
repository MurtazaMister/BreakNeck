package com.goodwy.dialer.services

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import androidx.core.app.ActivityCompat
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.goodwy.dialer.activities.CallActivity
import com.goodwy.dialer.extensions.config
import com.goodwy.dialer.extensions.getStateCompat
import com.goodwy.dialer.helpers.CallManager
import com.goodwy.dialer.helpers.CallNotificationManager
import com.goodwy.dialer.helpers.MyCameraImpl
import com.goodwy.dialer.helpers.NoCall
import com.goodwy.dialer.models.Events
import org.greenrobot.eventbus.EventBus

class CallService : InCallService() {
    private val callNotificationManager by lazy { CallNotificationManager(this) }
    private val callImuHandler by lazy { CallIMUHandler(this) }
    private val callListener = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            when (state) {
                Call.STATE_DISCONNECTED, Call.STATE_DISCONNECTING -> callNotificationManager.cancelNotification()
                else -> callNotificationManager.setupNotification()
            }
        }
    }

    lateinit var rightTemplate: PyObject
    lateinit var leftTemplate: PyObject

    private fun storeRightTileTemplate() {
        val python = Python.getInstance()
        val module = python.getModule("data_analysis_breakneck")
        val accData = module.get("df_acc_test_left_right_tilt_filtered")
        val gyroData = module.get("df_gyro_test_left_right_tilt_filtered")
        val results = module.callAttr("match_tilt", accData, gyroData)
        rightTemplate = results

        rightTemplate.asList().forEach {
            println("chay: right tilt $it")
        }
    }

    private fun storeLeftTiltTemplate() {
        val python = Python.getInstance()
        val module = python.getModule("data_analysis_breakneck")
        val accData = module.get("df_acc_test_left_right_tilt_filtered")
        val gyroData = module.get("df_gyro_test_left_right_tilt_filtered")
        val results = module.callAttr("match_tilt", accData, gyroData)
        leftTemplate = results

        leftTemplate.asList().forEach {
            println("chay: left tilt $it")
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.onCallAdded(call)
        CallManager.inCallService = this
        call.registerCallback(callListener)
        /** Handling condition based on Audio Route **/


        when (call.state) {
            Call.STATE_RINGING -> {
                println("chay: call state ringing")
                storeLeftTiltTemplate()
                storeRightTileTemplate()
            }
            Call.STATE_DISCONNECTED -> {

            }
        }

        when {
            config.showIncomingCallsFullScreen /*&& getPhoneSize() < 2*/ -> {
                try {
                    startActivity(CallActivity.getStartIntent(this))
                    callNotificationManager.setupNotification(true)
                } catch (e: Exception) {
                    // seems like startActivity can throw AndroidRuntimeException and ActivityNotFoundException, not yet sure when and why, lets show a notification
                    callNotificationManager.setupNotification()
                }
            }
            else -> {
                /** notification displayed using this **/
                callNotificationManager.setupNotification()
            }
        }
    }

    private fun isBluetoothDeviceConnected(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return false
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        val connectedDevices = bluetoothAdapter.bondedDevices.filter { _ ->
            val connectionState = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)
            connectionState == BluetoothProfile.STATE_CONNECTED
        }
        return connectedDevices.isNotEmpty()
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callListener)
        callNotificationManager.cancelNotification()
        val wasPrimaryCall = call == CallManager.getPrimaryCall()
        CallManager.onCallRemoved(call)
        if (CallManager.getPhoneState() == NoCall) {
            CallManager.inCallService = null
            callNotificationManager.cancelNotification()
        } else {
            callNotificationManager.setupNotification()
            if (wasPrimaryCall) {
                startActivity(CallActivity.getStartIntent(this))
            }
        }
        call.details?.let {
            if (config.flashForAlerts) MyCameraImpl.newInstance(this).stopSOS()
        }
        EventBus.getDefault().post(Events.RefreshCallLog)
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        if (audioState != null) {
            callImuHandler.handleAudioRouteChange(audioState)
            CallManager.onAudioStateChanged(audioState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callNotificationManager.cancelNotification()
        if (config.flashForAlerts) MyCameraImpl.newInstance(this).stopSOS()
    }
}

