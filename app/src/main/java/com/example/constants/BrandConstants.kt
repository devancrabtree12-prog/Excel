package com.example.constants

import androidx.compose.ui.graphics.Color
import com.example.model.DomainPreset

/**
 * Brand constants decoupled from layout components.
 * Corresponds to constants/brand.ts
 */
data class StatusBadgeStyle(
    val code: String,
    val label: String,
    val bg: Color,
    val border: Color,
    val text: Color
)

data class BrandTheme(
    val name: String,
    val logoIcon: String,
    val subtitle: String,
    val categoryTag: String,
    val tagline: String,
    val isTactical: Boolean,
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val headerBg: Color,
    val statusBadges: List<StatusBadgeStyle>
)

object BrandConstants {
    val QUANTGRID = BrandTheme(
        name = "QuantGrid",
        logoIcon = "📊",
        subtitle = "Enterprise Spreadsheet Architecture & Intelligent Workbook Generator",
        categoryTag = "ENTERPRISE",
        tagline = "Precision Financial, Operational, and Multi-tab Workbook Synthesis",
        isTactical = false,
        primary = Color(0xFF107C41),
        secondary = Color(0xFF15803D),
        background = Color(0xFFF8FAFC),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFE2E8F0),
        border = Color(0xFFCBD5E1),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF64748B),
        accent = Color(0xFF22C55E),
        headerBg = Color(0xFF0A4A28),
        statusBadges = listOf(
            StatusBadgeStyle("SOX_AUDIT", "SOX AUDIT READY", Color(0xFFECFDF5), Color(0xFF10B981), Color(0xFF065F46)),
            StatusBadgeStyle("VERIFIED_ENGINE", "ENGINE VERIFIED", Color(0xFFEFF6FF), Color(0xFF3B82F6), Color(0xFF1E40AF)),
            StatusBadgeStyle("DYNAMIC_FORMULAS", "100% FORMULA INTEGRITY", Color(0xFFF0FDF4), Color(0xFF22C55E), Color(0xFF15803D))
        )
    )

    val TACTICALGRID = BrandTheme(
        name = "TacticalGrid",
        logoIcon = "🎖️",
        subtitle = "Mission-Critical Operational Trackers & Duty Log Architecture",
        categoryTag = "TACTICAL OPSEC",
        tagline = "High-Integrity Duty Logs, PERSTAT Accountability & Defense Readiness",
        isTactical = true,
        primary = Color(0xFF38BDF8),
        secondary = Color(0xFF0EA5E9),
        background = Color(0xFF020617), // Tactical Black
        surface = Color(0xFF0F172A),    // Dark Slate
        surfaceVariant = Color(0xFF1E293B),
        border = Color(0xFF334155),     // High-contrast slate 700 border
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        accent = Color(0xFF22C55E),     // Night vision tactical green
        headerBg = Color(0xFF020617),   // Deep tactical black
        statusBadges = listOf(
            StatusBadgeStyle("DEFCON_1", "MISSION READY // DEFCON-1", Color(0xFF052E16), Color(0xFF22C55E), Color(0xFF4ADE80)),
            StatusBadgeStyle("CLASSIFIED_NOFORN", "CLASSIFIED // NOFORN", Color(0xFF451A03), Color(0xFFF59E0B), Color(0xFFFCD34D)),
            StatusBadgeStyle("OPSEC_PROTOCOL", "OPSEC PROTOCOL ACTIVE", Color(0xFF082F49), Color(0xFF38BDF8), Color(0xFF7DD3FC)),
            StatusBadgeStyle("PERSTAT_ACCOUNTED", "PERSTAT 100% ACCOUNTED", Color(0xFF450A0A), Color(0xFFEF4444), Color(0xFFFCA5A5))
        )
    )

    fun forDomain(domain: DomainPreset): BrandTheme {
        return if (domain == DomainPreset.MILITARY) TACTICALGRID else QUANTGRID
    }
}
