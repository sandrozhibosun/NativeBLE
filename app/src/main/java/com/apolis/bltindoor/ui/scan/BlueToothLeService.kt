package com.apolis.bltindoor.ui.scan

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.apolis.bltindoor.app.Const
import com.apolis.bltindoor.helper.SampleGattAttributes.SampleGattAttributes
import java.util.*
import javax.inject.Inject


class BlueToothLeService : Service() {
    companion object {
        val TAG = BlueToothLeService::class.java.name
    }

    @Inject
    lateinit var blueToothManager: BluetoothManager

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    //bluetoothDevice in this project was got by Ibinder
    //but if we have a target device, better just set the mac address, and search this device from
    lateinit var bluetoothDevice: BluetoothDevice

    //    lateinit var deviceAddress: String
    //basically it's a connection between client device and server device.
    var bluetoothGatt: BluetoothGatt? = null
    val batteryService = UUID.fromString(SampleGattAttributes.Battery_Service)

    private var mConnectionState = Const.STATE_DISCONNECTED

    val uuid_target_attritubes = ""


    val gattCallBack = object : BluetoothGattCallback() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //here we can setMtu,which mean Max transmisstion unit, to set this, we need LOLLIPOP api
                gatt?.requestMtu(31)

                intentAction = Const.ACTION_GATT_CONNECTED
                mConnectionState = Const.STATE_CONNECTED
                broadcastUpdate(intentAction)
                Log.d(TAG, "Connected to GATT server.")
                /**
                discover services after successful connection.
                because the client device doesn't know any specific attributes on server device
                so when call gatt connect first time, must discover the devices for specific attributes.
                eg: discover what services are available on this device, like heart-rate monitor?
                 */
                Log.d(
                    TAG, "Attempting to start service discovery:" +
                            bluetoothGatt!!.discoverServices()
                )
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = Const.ACTION_GATT_DISCONNECTED
                mConnectionState = Const.STATE_DISCONNECTED
                Log.d(TAG, "Disconnected from GATT server.")
                broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(Const.ACTION_GATT_SERVICES_DISCOVERED)
                Log.d(TAG, "onServicesDiscovered received: $status")
            } else {
                Log.d(TAG, "onServicesDiscovered received: $status")
            }

            //if we want monitor Battery Service: notices here we set the characteristic for read and notify.
            val characteristic = gatt?.getService(batteryService)?.getCharacteristic(
                UUID.fromString(SampleGattAttributes.Battery_Level)
            )
            //read battery level characteristic
            gatt?.readCharacteristic(characteristic)
            //set battery level notifycation
            gatt?.setCharacteristicNotification(characteristic, true)
            //or we can set value
            //characteristic?.value=byteArrayOf(50)
            //gatt?.writeCharacteristic(characteristic)


        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            //also here we can broadcast the battery level changed.
            super.onCharacteristicChanged(gatt, characteristic)
            characteristic?.let {
                val batteryLevel = characteristic.value[0].toInt()
                Log.d(TAG, "Battery Level is :$batteryLevel")

            }
        }
    }

    fun connect() {
        /**
         * if we connect a ble device, no pairing, because when a device is advertising as ble
        any person could connect it at anytime for any reason.
        then we will get a gatt client from connect method.
        we use gatt client to implement gatt protocol communication
         */
        bluetoothGatt =
            bluetoothDevice.connectGatt(this, false, gattCallBack)//predefined gattCallback
        Log.d(TAG, "connected")
        /* if we already have a specific device mac address to connect,  we can pass it as a parameter,
        and call like: val device=bluetoothAdapter.getRemoteDevice(macAddress) to get the device from
        adapter, then we can connect the specific device.
        */

    }

    /**
     * read data from characteristic, every characteristic also has uuid,we can used for read and write the value of them.
    by checking the source code we can find the value is defined a byte array,also characteristics has permissions
    defined in it so sometimes we can't read/write data cause permissions.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (bluetoothGatt == null) {
            Log.d(TAG, "bluetoothGatt is not initialized")
            return
        }
        bluetoothGatt!!.readCharacteristic(characteristic)

    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (bluetoothGatt == null) {
            Log.d(TAG, "bluetoothGatt is not initialized")
            return
        }
        bluetoothGatt!!.writeCharacteristic(characteristic)

    }

    /**
    the mainly advantage of Ble , it can subscribe to a notification
    traditional Bluetooth device constantly receiving data from other ends.
    BLE don't send any data, so need to register for a notification to receive changes.
    so it more like , talked to device, when this characteristics changes, talk it to me
    we need to set descriptor on specific characteristic to notify it. that's why it low enengy.
     */
    //Enables or disables notification on a give characteristic.
    //in notification we mainly used descriptors, which also can set byteArray in it.

    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ) {
        if (bluetoothGatt == null) {
            Log.d(TAG, "bluetoothGatt is not initialized")
            return
        }
        bluetoothGatt!!.setCharacteristicNotification(characteristic, enable)
        if (uuid_target_attritubes.equals(characteristic.uuid)) {
            //find the descriptor in this characteristic, which can enable notification
            //by specific uuid
            val descriptor = characteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG)
            )
            //set the notification on.
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            //write this descriptor in character.
            bluetoothGatt!!.writeDescriptor(descriptor)
        }
    }


    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */

    fun disconnect() {
        if (bluetoothGatt == null) {
            Log.d(TAG, "bluetoothGatt is not initialized")
            return
        }
        bluetoothGatt?.disconnect()
    }

    fun close() {
        if (bluetoothGatt == null) {
            Log.d(TAG, "bluetoothGatt is not initialized")
            return
        }
        bluetoothGatt?.close()
    }


    private fun broadcastUpdate(action: String) {
        val Intent = Intent(action)
        sendBroadcast(Intent)
        /** if we want to read data from characteristic, and send it to server,
        we can go to the profile specifications to checkout how they are defined:
        for example:http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        check the format of characteristics by flags in characteristic.getProperties, then decided the the decode format
        then get data from characteristic by format, then put it in broadcast.

         */
    }

    //Bound Service
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BlueToothLeService = this@BlueToothLeService
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "service are binded")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }


}
