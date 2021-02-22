package com.apolis.bltindoor.helper

import com.apolis.bltindoor.ui.scan.ScanFragment
import com.apolis.bltindoor.ui.scan.ScanViewModel
import com.clj.fastble.BleManager
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(BLEManagerModule::class))
interface AppComponent {
    fun inject(scanViewModel: ScanViewModel)
}