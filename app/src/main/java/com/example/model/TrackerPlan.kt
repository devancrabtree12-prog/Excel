package com.example.model

import com.squareup.moshi.JsonClass
import java.util.UUID

enum class ColumnType(val label: String, val symbol: String) {
    CURRENCY("Currency", "$"),
    PERCENT("Percent", "%"),
    DATE("Date", "📅"),
    STATUS("Status", "🏷️"),
    NUMBER("Number", "#"),
    TEXT("Text", "Aa")
}

enum class SpreadsheetTheme(
    val id: String,
    val displayName: String,
    val primaryHex: String,
    val headerArgb: Long,
    val accentArgb: Long
) {
    EMERALD("EMERALD", "Emerald Classic", "FF107C41", 0xFF107C41, 0xFFE8F5E9),
    NAVY("NAVY", "Modern Navy", "FF1E40AF", 0xFF1E40AF, 0xFFEFF6FF),
    SLATE("SLATE", "Slate Corporate", "FF334155", 0xFF334155, 0xFFF1F5F9),
    AMBER("AMBER", "Sunset Amber", "FFB45309", 0xFFB45309, 0xFFFEF3C7),
    ROSE("ROSE", "Rose Gold", "FF9D174D", 0xFF9D174D, 0xFFFCE7F3);

    companion object {
        fun fromId(id: String?): SpreadsheetTheme = entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: EMERALD
    }
}

@JsonClass(generateAdapter = true)
data class AuditIssue(
    val cell: String,
    val issueType: String, // "BROKEN_REFERENCE", "HARDCODED_TOTAL", "MISSING_FORMULA", "SUMMARY_ROW_INSERTED"
    val description: String,
    val originalValue: String,
    val fixedValue: String
)

@JsonClass(generateAdapter = true)
data class TrackerAuditReport(
    val issuesFound: Int,
    val fixedCount: Int,
    val issues: List<AuditIssue> = emptyList(),
    val summaryNotes: String = ""
)

@JsonClass(generateAdapter = true)
data class FormulaSpec(
    val cell: String,
    val formula: String
)

@JsonClass(generateAdapter = true)
data class ConditionalFormatRule(
    val range: String,
    val rule: String,
    val color: String
)

@JsonClass(generateAdapter = true)
data class TrackerPlan(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val headers: List<String>,
    val sample_rows: List<List<String>>,
    val formulas: List<FormulaSpec> = emptyList(),
    val conditional_formatting: List<ConditionalFormatRule> = emptyList(),
    val theme: String = "EMERALD",
    val hasInstructionsSheet: Boolean = false,
    val instructions: List<String> = emptyList(),
    val auditReport: TrackerAuditReport? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val slug: String
        get() = slugify(title)

    fun getColumnType(colIndex: Int): ColumnType {
        if (colIndex !in headers.indices) return ColumnType.TEXT
        val header = headers[colIndex]
        val samples = sample_rows.mapNotNull { row ->
            row.getOrNull(colIndex)
        }
        return inferType(header, samples)
    }

    fun getColumnWidth(colIndex: Int): Int {
        val header = headers.getOrNull(colIndex) ?: ""
        return widthFor(header)
    }

    fun getFormulaForCell(cellAddress: String): String? {
        val normalized = cellAddress.uppercase().trim()
        return formulas.firstOrNull { it.cell.uppercase().trim() == normalized }?.formula
    }
}

fun slugify(value: String): String {
    return value
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .replace(Regex("^-|-$"), "")
        .take(40)
        .ifEmpty { "tracker" }
}

fun inferType(header: String, samples: List<String>): ColumnType {
    val name = header.lowercase()
    if (Regex("date|due|issued|start|end|day|created|deadline|timestamp").containsMatchIn(name)) return ColumnType.DATE
    if (Regex("status|stage|state|priority|type|done|billable|active|flag|category|phase").containsMatchIn(name)) return ColumnType.STATUS
    if (Regex("%|percent|progress|rate of|margin|ratio|commission rate|discount rate|interest rate|tax rate|vat rate").containsMatchIn(name)) return ColumnType.PERCENT
    if (samples.any { it.contains("%") }) return ColumnType.PERCENT
    if (Regex("amount|price|cost|total|value|revenue|salary|budget|balance|fee|hourly rate|rate|commission|payout|expense|profit").containsMatchIn(name)) return ColumnType.CURRENCY
    
    val numericSamples = samples.filter { it.isNotBlank() }
    if (numericSamples.isNotEmpty() && numericSamples.all { s ->
        val cleaned = s.replace("$", "").replace("%", "").replace(",", "").trim()
        cleaned.toDoubleOrNull() != null
    }) {
        return ColumnType.NUMBER
    }
    return ColumnType.TEXT
}

fun widthFor(header: String): Int {
    return (header.length + 6).coerceIn(12, 34)
}

fun cleanCell(raw: Any?): String {
    if (raw == null) return ""
    val text = raw.toString().trim()
    if (text.startsWith("=")) return ""
    return text
}

fun stripFences(text: String): String {
    var cleaned = text.trim()
    if (cleaned.startsWith("```json")) {
        cleaned = cleaned.removePrefix("```json")
    } else if (cleaned.startsWith("```")) {
        cleaned = cleaned.removePrefix("```")
    }
    if (cleaned.endsWith("```")) {
        cleaned = cleaned.removeSuffix("```")
    }
    return cleaned.trim()
}

/**
 * Converts 0-indexed column integer to Excel letter (0 -> A, 1 -> B, ..., 26 -> AA)
 */
fun toColumnLetter(colIndex: Int): String {
    var num = colIndex
    val sb = StringBuilder()
    while (num >= 0) {
        sb.insert(0, ('A'.code + (num % 26)).toChar())
        num = (num / 26) - 1
    }
    return sb.toString()
}

/**
 * Converts column letter to 0-indexed integer ("A" -> 0, "B" -> 1, "AA" -> 26)
 */
fun fromColumnLetter(colLetter: String): Int {
    var result = 0
    for (char in colLetter.uppercase()) {
        result = result * 26 + (char.code - 'A'.code + 1)
    }
    return result - 1
}

/**
 * Parses "B2" to Pair(colIndex = 1, rowIndex = 1) (0-indexed)
 */
fun parseCellAddress(address: String): Pair<Int, Int>? {
    val regex = Regex("^([A-Za-z]+)(\\d+)$")
    val match = regex.find(address.trim()) ?: return null
    val (letters, digits) = match.destructured
    val col = fromColumnLetter(letters)
    val row = digits.toIntOrNull()?.minus(1) ?: return null
    return Pair(col, row)
}
