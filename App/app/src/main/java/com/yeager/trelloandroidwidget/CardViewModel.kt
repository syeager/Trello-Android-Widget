package com.yeager.trelloandroidwidget

import android.net.Uri
import java.time.LocalDate

data class CardViewModel(
    val cardName: String,
    val cardUrl: Uri,
    val dueDate: LocalDate?,
    val listName: String,
) {
    fun render(showDueDate: Boolean, showListName: Boolean): String {
        var value = cardName
        if (!showDueDate && !showListName) {
            return value
        }

        value += "\n  "
        if (showDueDate) {
            value += "${dueDate ?: "----"} "
        }

        if (showListName) {
            value += listName
        }

        return value
    }
}