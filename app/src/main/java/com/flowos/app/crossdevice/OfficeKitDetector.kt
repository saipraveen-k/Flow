package com.flowos.app.crossdevice

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Detects whether vivo Office Kit or compatible cross-device services
 * are available on the current device.
 */
object OfficeKitDetector {

    private const val OFFICE_KIT_PACKAGE = "com.vivo.office" // Hypothetical package name
    private const val QUANTUM_KIT_PACKAGE = "com.vivo.quantumkit"

    fun isOfficeKitAvailable(context: Context): Boolean {
        return isPackageInstalled(context, OFFICE_KIT_PACKAGE) || 
               isPackageInstalled(context, QUANTUM_KIT_PACKAGE) ||
               isVivoDevice()
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun isVivoDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return manufacturer.contains("vivo") || manufacturer.contains("iqoo")
    }
}
