package jp.kentan.studentportalplus.data.component

import android.graphics.Color

class ClassColor {
    companion object {
        val DEFAULT = Color.parseColor("#4FC3F7")
        val ALL = intArrayOf(
                Color.parseColor("#4FC3F7"), Color.parseColor("#03A9F4"), Color.parseColor("#0288D1"), Color.parseColor("#01579B"), //Light Blue 300 500 700 900
                Color.parseColor("#1B5E20"), Color.parseColor("#388E3C"), Color.parseColor("#4CAF50"), Color.parseColor("#81C784"), //Green      900 700 500 300
                Color.parseColor("#E57373"), Color.parseColor("#F44336"), Color.parseColor("#D32F2F"), Color.parseColor("#B71C1C"), //Red        300 500 700 900
                Color.parseColor("#E65100"), Color.parseColor("#F57C00"), Color.parseColor("#FF9800"), Color.parseColor("#FFB74D"), //Orange     900 700 500 300
                Color.parseColor("#BA68C8"), Color.parseColor("#9C27B0"), Color.parseColor("#7B1FA2"), Color.parseColor("#4A148C"), //Purple     300 500 700 900
                Color.parseColor("#3E2723"), Color.parseColor("#5D4037"), Color.parseColor("#795548"), Color.parseColor("#A1887F")  //Brown      900 700 500 300
        )
        val size = ALL.size
    }
}