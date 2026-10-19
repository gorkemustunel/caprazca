package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AppFont = FontFamily.SansSerif

val Typography =
  Typography(
    displaySmall = TextStyle(
      fontFamily = AppFont,
      fontWeight = FontWeight.Bold,
      fontSize = 28.sp,
      lineHeight = 34.sp,
      letterSpacing = (-0.4).sp,
    ),
    headlineMedium = TextStyle(
      fontFamily = AppFont,
      fontWeight = FontWeight.Bold,
      fontSize = 22.sp,
      lineHeight = 28.sp,
      letterSpacing = (-0.3).sp,
    ),
    titleLarge = TextStyle(
      fontFamily = AppFont,
      fontWeight = FontWeight.SemiBold,
      fontSize = 18.sp,
      lineHeight = 24.sp,
      letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
      fontFamily = AppFont,
      fontWeight = FontWeight.SemiBold,
      fontSize = 15.sp,
      lineHeight = 20.sp,
      letterSpacing = (-0.1).sp,
    ),
    titleSmall = TextStyle(
      fontFamily = AppFont,
      fontWeight = FontWeight.SemiBold,
      fontSize = 13.sp,
      lineHeight = 18.sp,
      letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
      fontFamily = AppFont,
      fontWeight = FontWeight.Normal,
      fontSize = 16.sp,
      lineHeight = 24.sp,
      letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
      fontFamily = AppFont,
      fontWeight = FontWeight.Normal,
      fontSize = 14.sp,
      lineHeight = 20.sp,
      letterSpacing = 0.1.sp,
    ),
    bodySmall = TextStyle(
      fontFamily = AppFont,
      fontWeight = FontWeight.Normal,
      fontSize = 12.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
      fontFamily = AppFont,
      fontWeight = FontWeight.SemiBold,
      fontSize = 13.sp,
      lineHeight = 16.sp,
      letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
      fontFamily = AppFont,
      fontWeight = FontWeight.Medium,
      fontSize = 11.sp,
      lineHeight = 14.sp,
      letterSpacing = 0.3.sp,
    ),
    labelSmall = TextStyle(
      fontFamily = AppFont,
      fontWeight = FontWeight.Medium,
      fontSize = 10.sp,
      lineHeight = 12.sp,
      letterSpacing = 0.3.sp,
    ),
  )
