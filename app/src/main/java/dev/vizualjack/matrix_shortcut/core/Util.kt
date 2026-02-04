package dev.vizualjack.matrix_shortcut.core

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings

fun isAccessibilityServiceEnabled(
    context: Context,
    serviceClass: Class<out AccessibilityService>
): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val componentName = ComponentName(context, serviceClass).flattenToString()

    return enabledServices
        .split(':')
        .any { it.equals(componentName, ignoreCase = true) }
}