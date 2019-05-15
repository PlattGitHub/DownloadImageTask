package com.example.downloadimagetask

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE
                )
            } else {
                setupFragment()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupFragment()
            } else {
                Snackbar.make(
                    fragment_container,
                    getString(R.string.no_permission),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupFragment() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, DownloadImageFragment.newInstance()).commit()
    }

    companion object {
        const val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1
    }
}