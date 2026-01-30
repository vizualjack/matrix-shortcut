package dev.vizualjack.matrix_shortcut.core

fun createExceptionLine(prefixText: String? = null, exception: Exception, suffixText: String? = null): String {
    return "$prefixText\n$exception\n$${exception.stackTraceToString()}\n$suffixText"
}