package com.apolis.bltindoor.ui.operation

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.apolis.bltindoor.R
import com.apolis.bltindoor.app.Const
import com.apolis.bltindoor.helper.DaggerAppComponent
import com.apolis.bltindoor.helper.SampleGattAttributes.OutputStringUtil
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleIndicateCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import java.util.*
import javax.inject.Inject

class OperationFragment : Fragment() {


    companion object {
        fun newInstance() = OperationFragment()
    }

    private lateinit var viewModel: OperationViewModel

    /*
    1.Services: set of provided features and associated behaviors to interact with the peripheral. Each service contains a collection of characteristics.
    2.Characteristics: definition of the data divided into declaration and value. Using permission properties (read, write, notify, indicate) to get a value.
    3.Descriptor: an optional attribute nested in a characteristic that describes the specific value and how to access it.
    4.UUID: Universally Unique ID that are transmitted over the air so a peripheral can inform a central what services it provides.
    */

    @Inject
    lateinit var bleManager: BleManager
    lateinit var bleDevice: BleDevice
    lateinit var bluetoothGattService: BluetoothGattService

    //characteristic is where ble save data.
    //basically the bluetooth communication is read/write and subscribe on characteristic.
    lateinit var bluetoothGattCharacteristic: BluetoothGattCharacteristic
    // the uuid in gatt services will map specific attributes

    lateinit var gatt: BluetoothGatt

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.operation_fragment, container, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(OperationViewModel::class.java)

        val component = DaggerAppComponent.create()
        component.inject(this)
    }

    override fun onResume() {
        super.onResume()

        bleDevice = arguments?.get("device") as BleDevice
        init()

    }
    //in google document, it used broadcast receiver to collect notifications.

    private fun init() {
        //once the device connect to GATT server and discovered service, we can read and write attributes.
        //pairing device might required.
        //in native implement, it should be: BluetoothGattCallback gattCallback = new BluetoothGattCallback() {...}
        //then: gatt =bleDevice.connectGatt(this,true,gattCallback)
        // which set autoconnect to true
        //but luckily we don't need to so in fastBle

        gatt = bleManager.getBluetoothGatt(bleDevice)
        //at that time, we can specific
        bluetoothGattService =
            gatt.services[0]//first service in gatt., we can define here to get specific data.
        //like HEART_RATE_SERVICE_UUID
        bluetoothGattCharacteristic = bluetoothGattService.characteristics[0]
        //more datailed in here, like HEART_RATE_CONTROL_POINT_CHAR_UUID
        read()
        val byteArray = byteArrayOf(
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00
        )
        send(byteArray)

    }

    // send byteArray to BLE device
    //for now it simply read and write, but we can use rxjava as observer patterns to implement  monitor.
    private fun send(bytes: ByteArray) {
        bleManager.write(
            bleDevice,
            bluetoothGattCharacteristic.service.uuid.toString(),//when we want to read spefic data like heart rate, change here.
            bluetoothGattCharacteristic.uuid.toString(),
            bytes,
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    Log.d(Const.Tag, "onWirte Success")
                    if (justWrite == null) {
                        Log.d("abc", "just write:nukk")
                    } else {
                        Log.d(
                            "abc",
                            "just write: ${OutputStringUtil.byteArrayToHexString(justWrite!!)}"
                        )
                    }
                }

                override fun onWriteFailure(exception: BleException?) {
                    Log.d("abc", "OnWrite Failure: ${exception.toString()}")
                }

            }
        )

    }

    //send
    private fun send(hex: String) {


        bleManager.write(
            bleDevice,
            bluetoothGattCharacteristic.service.uuid.toString(),
            bluetoothGattCharacteristic.uuid.toString(),
            HexUtil.hexStringToBytes(hex),
            object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    Log.d("abc", "onWirte Success")
                }

                override fun onWriteFailure(exception: BleException?) {
                    Log.d("abc", "OnWrite Failure")
                }

            }
        )

    }

    //for here we didn't specific any data , but if we want to read some specific data like
    //heart rate,
    private fun read() {
        bleManager.read(
            bleDevice,
            bluetoothGattCharacteristic.service.uuid.toString(),
            bluetoothGattCharacteristic.uuid.toString(),
            object : BleReadCallback() {
                override fun onReadSuccess(data: ByteArray?) {
                    Log.d("abc", "onRead Success")
//                    var string = OutputStringUtil.byteArrayToHexString(data!!)
                    var string = String(data!!)
                    Log.d("abc", string!!)

                }

                override fun onReadFailure(exception: BleException?) {
                    Log.d("abc", "onRead Failure")
                }

            }
        )


    }


    //push notifications by broadcastReceiver when receiving data dynamically
    //there are two ways of notify, indicate and notify, the differences is:
    //notify might lose data, but fast.
    //indicate has wrapped response method, so won't miss data, but slow.

    fun characteristicNotify(uuid_servie: String, uuid_characteristic_notify: String) {
        bleManager.notify(
            bleDevice,
            uuid_servie,//which service specific , like heart rate
            uuid_characteristic_notify,//which ch
            object : BleNotifyCallback() {
                override fun onNotifySuccess() {
                    TODO("Not yet implemented")
                }

                override fun onNotifyFailure(exception: BleException?) {
                    TODO("Not yet implemented")
                }
                    //call back when  characteristic changed.
                override fun onCharacteristicChanged(data: ByteArray?) {

                    TODO("Not yet implemented")
                }

            }

        )
        //close notify
        bleManager.stopNotify(bleDevice, uuid_servie, uuid_characteristic_notify)
    }

    fun characteristicIndicate(uuid_servie: String, uuid_characteristic_notify: String) {
        bleManager.indicate( bleDevice,
            uuid_servie,//which service specific , like heart rate
            uuid_characteristic_notify,//which ch
            object : BleIndicateCallback() {
                override fun onIndicateSuccess() {
                    TODO("Not yet implemented")
                }

                override fun onIndicateFailure(exception: BleException?) {
                    TODO("Not yet implemented")
                }

                override fun onCharacteristicChanged(data: ByteArray?) {
                    TODO("Not yet implemented")
                }

            }
            )
        //close indicate
        bleManager.stopIndicate(bleDevice,uuid_servie,uuid_characteristic_notify)
    }

}