package com.example.brickkotlin

class Brick(
    var isVisible: Boolean = true,
    var row: Int,
    var column: Int,
    var width: Int,
    var height: Int
) {
    fun setInvisible() {
        isVisible = false
    }

    fun getVisibility(): Boolean {
        return isVisible
    }
}