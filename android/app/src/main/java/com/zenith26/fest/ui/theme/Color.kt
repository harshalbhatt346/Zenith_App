package com.zenith26.fest.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand Purple & Indigo
val BrandPurple50 = Color(0xFFF5F3FF)
val BrandPurple100 = Color(0xFFEDE9FE)
val BrandPurple500 = Color(0xFF8B5CF6)
val BrandPurple600 = Color(0xFF7C3AED)
val BrandPurple700 = Color(0xFF6D28D9)
val BrandPurple800 = Color(0xFF5B21B6)
val BrandPurple900 = Color(0xFF4C1D95)
val BrandIndigo900 = Color(0xFF312E81)
val BrandIndigo800 = Color(0xFF3730A3)
val BrandIndigo600 = Color(0xFF4F46E5)
val BrandIndigo200 = Color(0xFFC7D2FE)
val BrandPink400 = Color(0xFFF472B6)
val BrandPink500 = Color(0xFFEC4899)
val BrandPink300 = Color(0xFFF9A8D4)

// Slate Neutrals
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)

// Status & Semantic Colors
val Rose50 = Color(0xFFFFF1F2)
val Rose100 = Color(0xFFFFE4E6)
val Rose200 = Color(0xFFFECDD3)
val Rose400 = Color(0xFFFB7185)
val Rose500 = Color(0xFFF43F5E)
val Rose600 = Color(0xFFE11D48)
val Rose700 = Color(0xFFBE123C)
val Rose800 = Color(0xFF9F1239)

val Emerald50 = Color(0xFFECFDF5)
val Emerald100 = Color(0xFFD1FAE5)
val Emerald200 = Color(0xFFA7F3D0)
val Emerald600 = Color(0xFF059669)
val Emerald800 = Color(0xFF065F46)

val Blue50 = Color(0xFFEFF6FF)
val Blue100 = Color(0xFFDBEAFE)
val Blue200 = Color(0xFFBFDBFE)
val Blue600 = Color(0xFF2563EB)
val Blue800 = Color(0xFF1E40AF)

val Amber50 = Color(0xFFFFFBEB)
val Amber100 = Color(0xFFFEF3C7)
val Amber200 = Color(0xFFFDE68A)
val Amber600 = Color(0xFFD97706)
val Amber800 = Color(0xFF92400E)

// Gradients matching Tailwind specs
val FestTitleGradient = Brush.horizontalGradient(
    colors = listOf(BrandPurple600, BrandIndigo600, BrandPink500)
)

val HeroBackgroundGradient = Brush.verticalGradient(
    colors = listOf(BrandIndigo900, BrandPurple900, Slate900)
)

val HeroTitleGradient = Brush.horizontalGradient(
    colors = listOf(BrandPink400, Color(0xFFD8B4FE), BrandIndigo200)
)

val CardTopBarGradient = Brush.horizontalGradient(
    colors = listOf(BrandPurple500, BrandIndigo600)
)

val ModalHeaderGradient = Brush.horizontalGradient(
    colors = listOf(BrandPurple700, BrandIndigo800)
)

val ConfirmButtonGradient = Brush.horizontalGradient(
    colors = listOf(BrandPurple600, BrandIndigo600)
)
