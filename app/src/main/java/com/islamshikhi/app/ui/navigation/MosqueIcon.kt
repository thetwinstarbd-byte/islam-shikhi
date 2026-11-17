package com.islamshikhi.app.ui.navigation

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector

/** ইবাদত ট্যাবের মসজিদ-আইকন (গম্বুজ, দুই মিনার ও দরজা)। */
val MosqueIcon: ImageVector by lazy {
    materialIcon(name = "Mosque") {
        materialPath(pathFillType = PathFillType.EvenOdd) {
            // গম্বুজ
            moveTo(12f, 2.5f)
            curveTo(8.8f, 4.9f, 7.2f, 7.2f, 7.2f, 9.5f)
            lineTo(7.2f, 11f)
            lineTo(16.8f, 11f)
            lineTo(16.8f, 9.5f)
            curveTo(16.8f, 7.2f, 15.2f, 4.9f, 12f, 2.5f)
            close()
            // মূল ভবন ও দরজা
            moveTo(6f, 12f)
            lineTo(18f, 12f)
            lineTo(18f, 21f)
            lineTo(6f, 21f)
            close()
            moveTo(10.6f, 21f)
            lineTo(10.6f, 17f)
            curveTo(10.6f, 15.5f, 13.4f, 15.5f, 13.4f, 17f)
            lineTo(13.4f, 21f)
            close()
            // বাঁ মিনার
            moveTo(2.5f, 8.5f)
            lineTo(4.25f, 5.5f)
            lineTo(6f, 8.5f)
            lineTo(6f, 21f)
            lineTo(2.5f, 21f)
            close()
            // ডান মিনার
            moveTo(18f, 8.5f)
            lineTo(19.75f, 5.5f)
            lineTo(21.5f, 8.5f)
            lineTo(21.5f, 21f)
            lineTo(18f, 21f)
            close()
        }
    }
}
