package com.apolis.bltindoor.ui.scan

import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.apolis.bltindoor.app.Const
import java.util.*
import javax.inject.Inject


class BluetoothLeServer @Inject constructor(var mBluetoothManager: BluetoothManager){

    companion object{
        val TAG= BluetoothLeServer::class.java.name
        private val UUID_SERVER: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")
        private val UUID_CHARREAD: UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb")
        private val UUID_CHARWRITE: UUID = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb")
        private val UUID_DESCRIPTOR: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    var characteristicRead: BluetoothGattCharacteristic? = null
    lateinit var currentDevice:BluetoothDevice
    lateinit var mBluetoothGattServer:BluetoothGattServer

    private val bluetoothGattServerCallback: BluetoothGattServerCallback =
        object : BluetoothGattServerCallback() {
            /**
             * when connection got changed
             * @param device
             * @param status
             * @param newState
             */
            override fun onConnectionStateChange(
                device: BluetoothDevice,
                status: Int,
                newState: Int
            ) {
                Log.d(
                    TAG,
                    String.format(
                        "1.onConnectionStateChange：device name = %s, address = %s",
                        device.name,
                        device.address
                    )
                )
                Log.d(
                    TAG,
                    String.format(
                        "1.onConnectionStateChange：status = %s, newState =%s ",
                        status,
                        newState
                    )
                )
                super.onConnectionStateChange(device, status, newState)
                currentDevice = device
            }
            override fun onServiceAdded(status: Int, service: BluetoothGattService) {
                super.onServiceAdded(status, service)
                Log.e(TAG, String.format("onServiceAdded：status = %s", status))
            }

            override fun onCharacteristicReadRequest(
                device: BluetoothDevice,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic
            ) {
                Log.e(
                    TAG,
                    String.format(
                        "onCharacteristicReadRequest：device name = %s, address = %s",
                        device.name,
                        device.address
                    )
                )
                Log.e(
                    TAG,
                    String.format(
                        "onCharacteristicReadRequest：requestId = %s, offset = %s",
                        requestId,
                        offset
                    )
                )
                mBluetoothGattServer.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    characteristic.value
                )
                //            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            }

            /**
             * 3. onCharacteristicWriteRequest, receive the bytes here
             * @param device
             * @param requestId
             * @param characteristic
             * @param preparedWrite
             * @param responseNeeded
             * @param offset
             * @param requestBytes
             */
            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                requestBytes: ByteArray
            ) {
                Log.e(
                    TAG,
                    String.format(
                        "3.onCharacteristicWriteRequest：device name = %s, address = %s",
                        device.name,
                        device.address
                    )
                )
                mBluetoothGattServer.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    requestBytes
                )
                //4. managed response data
                var intentAction = Const.ACTION_RESPONSE_TO_CLIENT
                onResponseToClient(requestBytes, device, requestId, characteristic)
            }

            /**
             * 2.when execute bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS...
             *  start onCharacteristicWriteRequest
             * @param device
             * @param requestId
             * @param descriptor
             * @param preparedWrite
             * @param responseNeeded
             * @param offset
             * @param value
             */
            override fun onDescriptorWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                descriptor: BluetoothGattDescriptor,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
            ) {
                Log.e(
                    TAG,
                    String.format(
                        "2.onDescriptorWriteRequest：device name = %s, address = %s",
                        device.name,
                        device.address
                    )
                )
                //            Log.e(TAG, String.format("2.onDescriptorWriteRequest：requestId = %s, preparedWrite = %s, responseNeeded = %s, offset = %s, value = %s,", requestId, preparedWrite, responseNeeded, offset, OutputStringUtil.toHexString(value)));

                // now tell the connected device that this was all successfull
                mBluetoothGattServer.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    value
                )
            }

            /**
             * 5. when read the characteristic, and response successful, the client device
             * will read and invoke this method
             * @param device
             * @param requestId
             * @param offset
             * @param descriptor
             */
            override fun onDescriptorReadRequest(
                device: BluetoothDevice,
                requestId: Int,
                offset: Int,
                descriptor: BluetoothGattDescriptor
            ) {
                Log.e(
                    TAG,
                    String.format(
                        "onDescriptorReadRequest：device name = %s, address = %s",
                        device.name,
                        device.address
                    )
                )
                Log.e(TAG, String.format("onDescriptorReadRequest：requestId = %s", requestId))
                //            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
                mBluetoothGattServer.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    null
                )
            }
            //if data got changed , send notification
            override fun onNotificationSent(device: BluetoothDevice, status: Int) {
                super.onNotificationSent(device, status)
                Log.e(
                    TAG,
                    String.format(
                        "5.onNotificationSent：device name = %s, address = %s",
                        device.name,
                        device.address
                    )
                )
                Log.e(TAG, String.format("5.onNotificationSent：status = %s", status))
            }

            override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
                super.onMtuChanged(device, mtu)
                Log.e(TAG, String.format("onMtuChanged：mtu = %s", mtu))
            }

            override fun onExecuteWrite(device: BluetoothDevice, requestId: Int, execute: Boolean) {
                super.onExecuteWrite(device, requestId, execute)
                Log.e(TAG, String.format("onExecuteWrite：requestId = %s", requestId))
            }
        }

    private fun initServices(context: Context) {
        mBluetoothGattServer =
            mBluetoothManager.openGattServer(context, bluetoothGattServerCallback)
        val service = BluetoothGattService(UUID_SERVER, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        //add a read characteristic.
         characteristicRead = BluetoothGattCharacteristic(
             UUID_CHARREAD,
             BluetoothGattCharacteristic.PROPERTY_READ,
             BluetoothGattCharacteristic.PERMISSION_READ
         )
        //add a descriptor
        val descriptor =
            BluetoothGattDescriptor(UUID_DESCRIPTOR, BluetoothGattCharacteristic.PERMISSION_WRITE)
        characteristicRead!!.addDescriptor(descriptor)
        service.addCharacteristic(characteristicRead)

        //add a write characteristic.
        val characteristicWrite = BluetoothGattCharacteristic(
            UUID_CHARWRITE,
            BluetoothGattCharacteristic.PROPERTY_WRITE or
                    BluetoothGattCharacteristic.PROPERTY_READ or
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(characteristicWrite)
        mBluetoothGattServer.addService(service)
        Log.e(TAG, "2. initServices ok")
    }


    private fun onResponseToClient(
        reqeustBytes: ByteArray,
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        Log.e(
            TAG,
            String.format(
                "4.onResponseToClient：device name = %s, address = %s",
                device.name,
                device.address
            )
        )
        Log.e(TAG, String.format("4.onResponseToClient：requestId = %s", requestId))
        //        String msg = OutputStringUtil.transferForPrint(reqeustBytes);
        val msg = String(reqeustBytes)
        println("4.received:$msg")
//        broadcastUpdate(intentAction, msg)
        currentDevice = device
    }

    private fun sendToClient(message: String) {
        characteristicRead!!.setValue(message.toByteArray())
        if (currentDevice != null) mBluetoothGattServer.notifyCharacteristicChanged(
            currentDevice,
            characteristicRead,
            false
        )
        println("Me:$message")
        var mOutString = message
    }
}