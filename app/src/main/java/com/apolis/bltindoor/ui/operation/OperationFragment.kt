package com.apolis.bltindoor.ui.operation

import android.bluetooth.BluetoothDevice
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


    lateinit var bleDevice: BluetoothDevice
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

        bleDevice = arguments?.get("device") as BluetoothDevice
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

//        gatt = bleManager.getBluetoothGatt(bleDevice)
        //at that time, we can specific
//        bluetoothGattService =
//            gatt.services[0]//first service in gatt., we can define here to get specific data.
//        //like HEART_RATE_SERVICE_UUID
//        bluetoothGattCharacteristic = bluetoothGattService.characteristics[0]

        val byteArray = byteArrayOf(
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
            0x00
        )


    }



}