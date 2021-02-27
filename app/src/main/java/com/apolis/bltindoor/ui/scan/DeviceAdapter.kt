package com.apolis.bltindoor.ui.scan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.apolis.bltindoor.R
import com.apolis.bltindoor.databinding.RowAdapterDeviceBinding
import com.clj.fastble.data.BleDevice

class DeviceAdapter : RecyclerView.Adapter<DeviceAdapter.MyViewHolder>(), DeviceCallbackListener {
    private var mList = ArrayList<BleDevice>()
    var parentFragment: ScanFragment? = null

    lateinit var viewModel: ScanViewModel
    lateinit var onConnectCallListener: OnConnectCallListener

    inner class MyViewHolder(
        val binding: RowAdapterDeviceBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bleDevice: BleDevice) {



        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceAdapter.MyViewHolder {
        val layoutInflater = LayoutInflater.from(parentFragment!!.requireContext())
        val binding = RowAdapterDeviceBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(parentFragment!!).get(ScanViewModel::class.java)
        viewModel.deviceCallbackListener = this
        return MyViewHolder(binding)

    }

    override fun onBindViewHolder(holder: DeviceAdapter.MyViewHolder, position: Int) {
        var device = mList[position]
        holder.binding.device = device
        holder.binding.txtRssi.text = device.rssi.toString()
        holder.binding.btnConnect.setOnClickListener {
            parentFragment!!.onConnectDevice(device)
        }
        holder.binding.btnDisconnect.setOnClickListener {
            parentFragment!!.onDisconnectDevice(device)
        }
        holder.binding.btnDetail.setOnClickListener {
            parentFragment!!.onDetail(device)

        }
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun addDevice(bleDevice: BleDevice) {
        mList.add(bleDevice)
        notifyDataSetChanged()
        Log.d("abc", "adapter size ${mList.size}")
    }

    fun clearScanDevice() {
        mList.clear()
        notifyDataSetChanged()
    }

    fun setDevice(deviceList: ArrayList<BleDevice>) {
        mList = deviceList
        notifyDataSetChanged()
    }

    override fun onConnectCallback() {
        TODO("Not yet implemented")
    }

    override fun onDisconnectCallBack() {
        TODO("Not yet implemented")
    }

    override fun onDetailCallBack(message: String,bleDevice: BleDevice?) {

        Toast.makeText(parentFragment!!.requireContext(), message, Toast.LENGTH_SHORT).show()
        if(bleDevice!=null){
        var bundle = Bundle()
        bundle.putParcelable("device", bleDevice)
        Navigation.findNavController(parentFragment!!.requireView())
            .navigate(R.id.scanFragment_to_operationFragment,bundle)
        }
    }

}