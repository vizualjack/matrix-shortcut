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

    fun hasUniqueMatch(): Boolean {
        return gestures.size == 1
    }

    fun getUniqueMatchActionName():String? {
        if (!hasUniqueMatch()) return null
        return gestures[0].actionName
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