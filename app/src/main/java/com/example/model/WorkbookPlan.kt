package com.example.model

import com.squareup.moshi.JsonClass
import java.util.UUID

enum class DomainPreset(
    val id: String,
    val title: String,
    val shortLabel: String,
    val icon: String,
    val description: String,
    val complianceNote: String
) {
    FINANCIAL(
        id = "financial",
        title = "Financial & Budgeting",
        shortLabel = "Finance",
        icon = "💰",
        description = "Enforces $#,##0.00 currency formatting, cross-sheet variance formulas, and financial KPI summary cards.",
        complianceNote = "SOX & Financial Audit Compliant: All calculation formulas are protected; reconciliations reference source ledgers."
    ),
    HR(
        id = "hr",
        title = "HR & Operations",
        shortLabel = "HR",
        icon = "👥",
        description = "Employee tracking, attendance calculation formulas, and department metrics.",
        complianceNote = "Confidential Personnel Record: Contains employee PII and compensation tiers. Handle in accordance with HR privacy policies."
    ),
    HEALTHCARE(
        id = "healthcare",
        title = "Healthcare & Patient Tracking",
        shortLabel = "Healthcare",
        icon = "🏥",
        description = "Patient identifiers, status triage alerts, HIPAA notes, and clinical date tracking.",
        complianceNote = "HIPAA Confidentiality Notice: Protected Health Information (PHI). Restrict file sharing to authorized personnel."
    ),
    PROJECT(
        id = "project",
        title = "Project Management",
        shortLabel = "Project Management",
        icon = "📋",
        description = "Task duration calculations, priority color coding, and completion progress bars.",
        complianceNote = "PMO Standard: Milestones and dependency linkages must be reviewed at weekly sprint retrospectives."
    ),
    MILITARY(
        id = "military",
        title = "Military & Defense",
        shortLabel = "Military & Defense",
        icon = "🎖️",
        description = "Mission execution matrices, PERSTAT accountability, sensitive property books, and duty desk logs.",
        complianceNote = "OPSEC / CUI Standards: Personnel records, sensitive items property serials, and operational matrices require authorized clearance."
    ),
    SALES(
        id = "sales",
        title = "Sales & Pipeline",
        shortLabel = "Sales",
        icon = "📈",
        description = "Pipeline stage dropdowns, probability-weighted revenue formulas, and deal conversion metrics.",
        complianceNote = "Sales Operations Protocol: Deal sizes and commission probabilities are locked to authorized fiscal quotas."
    ),
    GENERAL(
        id = "general",
        title = "General / Custom Tracker",
        shortLabel = "General",
        icon = "📊",
        description = "Fully flexible multi-tab architecture dynamically inferred by AI.",
        complianceNote = "Standard data hygiene and formula integrity guidelines apply."
    );

    val isTactical: Boolean
        get() = this == MILITARY

    companion object {
        fun fromId(id: String): DomainPreset = values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GENERAL
        fun fromTitle(title: String): DomainPreset = values().firstOrNull { it.title.contains(title, ignoreCase = true) } ?: GENERAL
    }
}

enum class ColorTheme(
    val id: String,
    val label: String,
    val primaryHex: String,
    val secondaryHex: String,
    val accentHex: String,
    val lightBgHex: String
) {
    MODERN_BLUE("blue", "Modern Blue", "#1E40AF", "#3B82F6", "#93C5FD", "#EFF6FF"),
    EMERALD_GREEN("green", "Emerald Green", "#107C41", "#15803D", "#86EFAC", "#F0FDF4"),
    CHARCOAL("charcoal", "Charcoal", "#334155", "#475569", "#94A3B8", "#F8FAFC"),
    SUNSET("sunset", "Sunset", "#EA580C", "#F97316", "#FDBA74", "#FFF7ED"),
    TACTICAL_OLIVE("olive", "Tactical Olive", "#2E3D29", "#4A5D44", "#A3B899", "#F4F7F3");

    companion object {
        fun fromId(id: String): ColorTheme = values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: EMERALD_GREEN
        fun fromLabel(label: String): ColorTheme = values().firstOrNull { it.label.equals(label, ignoreCase = true) } ?: EMERALD_GREEN
    }
}

enum class ActionGoalMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String
) {
    CREATE_NEW(
        "create_new",
        "Create New Tracker",
        "Constructs a multi-tab workbook from scratch or selected blueprint",
        "✨"
    ),
    UPGRADE_EXISTING(
        "upgrade_existing",
        "Upgrade / Redesign Existing Sheet",
        "Preserves uploaded data, applies color themes, formats numbers, and inserts KPI cards/charts",
        "🎨"
    ),
    AUDIT_AND_FIX(
        "audit_and_fix",
        "Audit & Fix Formulas",
        "Scans for #REF!/#VALUE!, repairs cell references, and inserts dynamic totals",
        "🛠️"
    ),
    ANALYZE_AND_LEARN(
        "analyze_and_learn",
        "Analyze & Learn Structure (Blueprint Extractor)",
        "Extracts column types, syntax, and rules, saving to Template Library for 1-click reuse",
        "🧠"
    )
}

@JsonClass(generateAdapter = true)
data class BlueprintSummary(
    val keyMetrics: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val effectivenessScore: Int = 94,
    val notes: String = ""
)

@JsonClass(generateAdapter = true)
data class KpiCardSpec(
    val label: String,
    val formula: String,
    val format: String = "currency", // "currency", "number", "percent", "text"
    val value: String? = null
)

@JsonClass(generateAdapter = true)
data class ChartSpec(
    val type: String = "column", // "column", "bar", "line", "pie"
    val title: String,
    val range: String
)

@JsonClass(generateAdapter = true)
data class ColumnSpec(
    val header: String,
    val type: String = "text", // "date", "status", "currency", "percent", "number", "text", "category", "priority"
    val formula: String? = null,
    val validation: List<String>? = null // in-cell dropdown list values!
)

@JsonClass(generateAdapter = true)
data class ConditionalRuleSpec(
    val range: String,
    val rule: String, // e.g. "Overdue", "Pending", "top10Percent"
    val color: String // "lightRed", "lightGreen", "yellow", "blue"
)

@JsonClass(generateAdapter = true)
data class SheetPlan(
    val name: String,
    val isInstructions: Boolean = false,
    val instructionsText: String? = null,
    val isSummary: Boolean = false,
    val kpiCards: List<KpiCardSpec> = emptyList(),
    val charts: List<ChartSpec> = emptyList(),
    val columns: List<ColumnSpec> = emptyList(),
    val rows: List<List<String>> = emptyList(),
    val conditionalFormatting: List<ConditionalRuleSpec> = emptyList()
) {
    val isDataTable: Boolean
        get() = !isInstructions && !isSummary && columns.isNotEmpty()
}

@JsonClass(generateAdapter = true)
data class WorkbookPlan(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val domain: String = "General / Custom Tracker",
    val theme: String = "Emerald Green",
    val blueprintSummary: BlueprintSummary? = null,
    val sheets: List<SheetPlan> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val slug: String
        get() = slugify(title)

    val instructionSheet: SheetPlan?
        get() = sheets.firstOrNull { it.isInstructions }

    val dashboardSheet: SheetPlan?
        get() = sheets.firstOrNull { it.isSummary }

    val dataSheets: List<SheetPlan>
        get() = sheets.filter { it.isDataTable }

    val totalRowCount: Int
        get() = sheets.sumOf { it.rows.size }

    val totalFormulaCount: Int
        get() = sheets.sumOf { sheet ->
            sheet.kpiCards.size + sheet.columns.count { it.formula != null } + sheet.rows.sumOf { row ->
                row.count { it.startsWith("=") }
            }
        }
}

data class AuditFinding(
    val sheetName: String,
    val cellLocation: String,
    val issueType: String, // "BROKEN_REFERENCE", "VALUE_ERROR", "MISSING_TOTAL", "INVALID_FORMAT"
    val originalText: String,
    val repairedText: String,
    val explanation: String
)

data class AuditReport(
    val workbookTitle: String,
    val totalFormulasAudited: Int,
    val brokenFormulasFixed: Int,
    val missingTotalsInserted: Int,
    val findings: List<AuditFinding> = emptyList()
)
