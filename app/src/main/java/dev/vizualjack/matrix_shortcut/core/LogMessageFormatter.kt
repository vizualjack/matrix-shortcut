package dev.vizualjack.matrix_shortcut.core

fun createExceptionLine(prefixText: String? = null, exception: Exception, suffixText: String? = null): String {
    return "$prefixText$exception\n$${exception.stackTraceToString()}$suffixText"
}