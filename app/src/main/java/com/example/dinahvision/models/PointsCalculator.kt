package com.example.dinahvision.models

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

object PointsCalculator{
    fun calculate(prevision: Prevision): Int {
        if (!prevision.finished || !prevision.predicted) return 0

        val today: LocalDate = Instant
            .ofEpochMilli(Date().time)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val endDate: LocalDate = Instant
            .ofEpochMilli(prevision.getEndDateAsDate().time)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        return if (!today.isAfter(endDate)) {
            10
        } else {
            5
        }
    }
}