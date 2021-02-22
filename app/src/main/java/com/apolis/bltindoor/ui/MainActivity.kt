package com.apolis.bltindoor.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.apolis.bltindoor.R
import com.apolis.bltindoor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        val view=binding.root
        setContentView(view)


        val navView=binding.navView

        val navController=
            (supportFragmentManager.findFragmentById(R.id.navi_host_fragment_in_main_activity) as NavHostFragment)
                .navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration= AppBarConfiguration(setOf(
            R.id.navigation_scan,
            R.id.navigation_operation,
            R.id.navigation_history
        ))
        setupActionBarWithNavController(navController,appBarConfiguration)
        navView.setupWithNavController(navController)

    }
}