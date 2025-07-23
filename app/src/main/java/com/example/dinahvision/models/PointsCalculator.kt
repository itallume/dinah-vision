package com.example.dinahvision.models

import java.util.Date

object PointsCalculator{
    fun calculate(prevision:Prevision):Int{
        val currentDate = Date()

        if (prevision.finished && prevision.predicted) {
            val endDate = prevision.getEndDateAsDate()

            return if (currentDate <= endDate) {
                10
            } else {
                5
            }
        }
        return 0
    }
}