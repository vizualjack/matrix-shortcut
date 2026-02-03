package dev.vizualjack.matrix_shortcut.core

import dev.vizualjack.matrix_shortcut.core.data.Gesture
import dev.vizualjack.matrix_shortcut.core.data.GestureInput


class GestureDetector(
    private val gestures : ArrayList<Gesture>
) {
    private val gestureInputs: ArrayList<GestureInput> = arrayListOf()

    fun addGestureInput(keyCode:Int, duration:Int) {
        gestureInputs.add(GestureInput(keyCode, duration))
        cleanUpMissedGestures()
    }

    fun canStillMatch(): Boolean {
        return gestures.size > 0
    }

    fun hasMatch(): Boolean {
        if(gestures.size != 1) return false
        return checkIfInputTotallyMatches(gestures[0])
    }

    fun getMessageOfMatch():String? {
        if (!hasMatch()) return null
        return gestures[0].message
    }

    private fun checkIfInputTotallyMatches(gesture: Gesture): Boolean {
        if(gesture.gestureEntries.size != gestureInputs.size) return false
        for (index in 0..gesture.gestureEntries.size - 1) {
            val inputEntry = gestureInputs[index]
            val gestureEntry = gesture.gestureEntries[index]
            if(inputEntry.duration < gestureEntry.minDuration || inputEntry.keyCode != gestureEntry.keyCode) return false
        }
        return true
    }

    private fun cleanUpMissedGestures() {
        val inputNum = gestureInputs.size - 1
        val deletingGestures = arrayListOf<Gesture>()
        for (gesture in gestures) {
            if (gesture.gestureEntries.size <= inputNum){
                deletingGestures.add(gesture)
                continue
            }
            val gestureEntry = gesture.gestureEntries[inputNum]
            val inputEntry = gestureInputs[inputNum]
            if(inputEntry.keyCode != gestureEntry.keyCode || inputEntry.duration < gestureEntry.minDuration)
            {
                deletingGestures.add(gesture)
            }
        }
        gestures.removeAll(deletingGestures.toSet())
    }
}