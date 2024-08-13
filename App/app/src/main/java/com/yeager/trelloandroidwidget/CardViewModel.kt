package com.yeager.trelloandroidwidget

import android.net.Uri
import java.time.LocalDate

data class CardViewModel(
    val cardName: String,
    val cardUrl: Uri,
    val dueDate: LocalDate?,
    val listName: String,
) {
    override fun toString() = "$cardName\n  $dueDate $listName"
}