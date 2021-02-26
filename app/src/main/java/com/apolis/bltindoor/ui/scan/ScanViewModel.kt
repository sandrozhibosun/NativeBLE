package com.apolis.bltindoor.ui.scan

import android.bluetooth.BluetoothGatt
import android.util.Log
import androidx.lifecycle.ViewModel
import com.apolis.bltindoor.helper.DaggerAppComponent
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleMtuChangedCallback
import com.clj.fastble.callback.BleRssiCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.scan.BleScanRuleConfig
import kotlinx.android.synthetic.main.scan_fragment.*
import javax.inject.Inject


class ScanViewModel : ViewModel() {
    @Inject
    lateinit var bleManager: BleManager

    init {
        val component = DaggerAppComponent.create()
        component.inject(this)
        Log.d("abc","is this device support BLE: "+bleManager.isSupportBle.toString())

    }
    //the device also has mac address, scanRecord, which is a byteArray, that show data when get scanned

    var deviceGetListener: DeviceGetListener? = null
    var deviceCallbackListener: DeviceCallbackListener? = null

    // TODO: Implement the ViewModel
    fun setScanRule() {
        //set timeout, because scan is battery-intensive
        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setScanTimeOut(10000) //
            .build()
        bleManager.initScanRule(scanRuleConfig)
    }

    fun startScan() {
        //might optimize as livedata later.
//        var res=MutableLiveData<ArrayList<BleDevice>>()
//        var temp=ArrayList<BleDevice>()
        bleManager.scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                deviceGetListener!!.onClear()
//                res.postValue(temp)
//                mDeviceAdapter.clearScanDevice()
//                mDeviceAdapter.notifyDataSetChanged()

            }
            //only scan for BLE device
            override fun onLeScan(bleDevice: BleDevice) {
                super.onLeScan(bleDevice)
            }
            //scan for all blue tooth device
            //native android sdk didn't support scan ble or classic bluetooth together
            override fun onScanning(bleDevice: BleDevice) {
                deviceGetListener!!.onGet(bleDevice)
                Log.d("abc", "scan 1")

//                res.postValue(temp)
//                mDeviceAdapter.addDevice(bleDevice)
//                mDeviceAdapter.notifyDataSetChanged()
            }
            override fun onScanFinished(scanResultList: List<BleDevice>) {

            }
        })

//        deviceGetListener!!.onGet(res)
    }
    //connect method, wrap the android bluetoothGatt in connect method, you can see it in source code
    //in android native implement, we take a device to connect Gatt, like
    //var bluetoothGatt: BluetoothGatt? = null
    //...
    //bluetoothGatt = device.connectGatt(this, false, gattCallback)
    //we can perform operation on the Gatt client, and the BluetoothGattCallback is used the deliver results to client.
    //notice one BLE can only have one central device., once it connect, it will stop broadcast to other device.
    fun onConnectDevice(bleDevice: BleDevice) {
        //stop scan after find device, because scan is battery-intensive
        if (!bleManager.isConnected(bleDevice)) {
            bleManager.cancelScan()
        }
        bleManager.connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {
                Log.d("abc", "start connect")
            }

            override fun onConnectFail(exception: BleException?) {
                Log.d("abc", "connect fail")
                Log.d("abc", exception.toString())

            }

            override fun onConnectSuccess(//device 78:20:7b could connect
                bleDevice: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                Log.d("abc", "connect success")
                readRssi(bleDevice!!)
                setMtu(bleDevice!!, 23)

            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {

                if (isActiveDisConnected) {
                    Log.d("abc", "is active disconnect")
                } else {
                    Log.d("abc", "dis connect and didn't active")
                }
            }

        })
    }

    fun onDisconnectDevice(bleDevice: BleDevice) {
        bleManager.disconnect(bleDevice)
    }

    fun onDetail(bleDevice: BleDevice) {
        if (bleManager.isConnected(bleDevice)) {
            deviceCallbackListener!!.onDetailCallBack("this is connected device", bleDevice)
        } else {
            deviceCallbackListener!!.onDetailCallBack("this device didn't connected", null)
        }
    }

    //the strength of signal, the number is less, the signal is better.
    private fun readRssi(bleDevice: BleDevice) {
        bleManager.readRssi(bleDevice, object : BleRssiCallback() {
            override fun onRssiFailure(exception: BleException) {
                Log.d("abc", "onRssiFailure$exception")
            }

            override fun onRssiSuccess(rssi: Int) {
                Log.d("abc", "onRssiSuccess: $rssi")
            }
        })
    }
    // Max transfer Unit, we can change the value to set more than 20 byte situation
    //actually native max package is 20n byte one time, by set this, we will divide the page .
    private fun setMtu(bleDevice: BleDevice, mtu: Int) {
        bleManager.setMtu(bleDevice, mtu, object : BleMtuChangedCallback() {
            override fun onSetMTUFailure(exception: BleException) {
                Log.d("abc", "onsetMTUFailure$exception")
            }

            override fun onMtuChanged(mtu: Int) {
                Log.d("abc", "onMtuChanged: $mtu")
            }
        })
    }

    override fun onCleared() {
        super.onCleared()

        bleManager.disconnectAllDevice()
        bleManager.destroy()
    }

}