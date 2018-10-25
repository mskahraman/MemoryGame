package com.example.mehmetsabir.memorygamesforsmartalarm

import android.widget.Button

class Cards(button: Button, x: Int, y: Int) {
    var x: Int = 0
    var y: Int = 0
    var button: Button

    init {
        this.x = x;
        this.y = y
        this.button = button
    }

}