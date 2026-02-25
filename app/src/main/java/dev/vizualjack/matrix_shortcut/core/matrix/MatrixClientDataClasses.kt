package dev.vizualjack.matrix_shortcut.core.matrix

data class Room(
    val roomId: String,
    val displayName: String,
    val membersAmount: Int
) {
    override fun toString(): String {
        return displayName
    }
}