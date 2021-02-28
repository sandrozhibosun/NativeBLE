package com.apolis.bltindoor.app

class Const {
    companion object{
        const val  EXTRA_NAME_BLEDEVICE = "key_data"
        const val Tag="BLTindoor"
        const val ACTION_GATT_CONNECTED="ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "EXTRA_DATA"
        const val STATE_DISCONNECTED = 0
        const val STATE_CONNECTING = 1
        const val STATE_CONNECTED = 2
        const val SENSOR_TAG_DEVICE_NAME = "SensorTag"
        const val ACTION_RESPONSE_TO_CLIENT =
            "ACTION_RESPONSE_TO_CLIENT";

    }
}