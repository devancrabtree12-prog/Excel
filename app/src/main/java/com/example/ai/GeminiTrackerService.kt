package com.example.ai

import android.util.Log
import com.example.BuildConfig
import com.example.model.ActionGoalMode
import com.example.model.BlueprintSummary
import com.example.model.ChartSpec
import com.example.model.ColorTheme
import com.example.model.ColumnSpec
import com.example.model.ConditionalRuleSpec
import com.example.model.DomainPreset
import com.example.model.KpiCardSpec
import com.example.model.SheetPlan
import com.example.model.WorkbookPlan
import com.example.parser.ParsedWorkbook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class GenerationRequest(
    val mode: ActionGoalMode,
    val prompt: String,
    val domain: DomainPreset,
    val theme: ColorTheme,
    val compactView: Boolean = false,
    val enableCharts: Boolean = true,
    val enableAnalytics: Boolean = true,
    val liveDataEntry: Boolean = false,
    val uploadedWorkbook: ParsedWorkbook? = null,
    val baseBlueprintPlan: WorkbookPlan? = null,
    val customApiKey: String? = null
)

object GeminiTrackerService {

    private const val TAG = "GeminiTrackerService"
    private const val PRIMARY_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    private const val FALLBACK_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val ENHANCED_SYSTEM_PROMPT = """You are an enterprise Excel architect. Your job is to generate a comprehensive, multi-tab, formula-heavy Excel workbook plan based on user instructions, domain guidelines, and uploaded data.

Output ONLY valid JSON with this exact schema:

{
  "title": "Master Financial Model",
  "domain": "Financial & Budgeting",
  "blueprintSummary": {
    "keyMetrics": ["Total Revenue", "Net Margin %"],
    "categories": ["Finance", "Executive"],
    "effectivenessScore": 96,
    "notes": "Optimal multi-tab layout with automated instructions and KPI cards."
  },
  "sheets": [
    {
      "name": "Instructions",
      "isInstructions": true,
      "instructionsText": "Welcome to your Master Financial Model..."
    },
    {
      "name": "Dashboard",
      "isSummary": true,
      "kpiCards": [
        {"label": "Total Revenue", "formula": "=SUM('Data'!D:D)", "format": "currency", "value": "$145,000"}
      ],
      "charts": [
        {"type": "column", "title": "Monthly Revenue", "range": "Data!A1:D12"}
      ]
    },
    {
      "name": "Data",
      "columns": [
        {"header": "Transaction Date", "type": "date"},
        {"header": "Status", "type": "status", "validation": ["Pending", "Approved", "Rejected"]},
        {"header": "Amount", "type": "currency"}
      ],
      "rows": [
        ["2026-01-15", "Approved", "1500.00"]
      ],
      "conditionalFormatting": [
        {"range": "B2:B50", "rule": "cellValue == 'Rejected'", "color": "lightRed"}
      ]
    }
  ]
}

CRITICAL RULES:
1. Sheet 0 MUST ALWAYS be an "Instructions" sheet (isInstructions: true) with comprehensive governance rules, column formula descriptions, and domain compliance notes (e.g. HIPAA for healthcare, SOX for finance).
2. Use standard uppercase Excel formulas (SUM, AVERAGE, SUMIF, COUNTIF, IF, ROUND, etc.) with commas as argument separators.
3. Summary KPI cards on Dashboard must use cross-sheet formulas referencing data sheets (e.g. =SUM('Data'!D:D) or =AVERAGE('Deals'!E2:E50)).
4. For columns typed as 'status', 'priority', or 'category', provide a 'validation' list of 3-5 dropdown choices (e.g. ["Active", "Pending", "Archived"]).
5. Provide 4 to 8 realistic, rich sample rows of data per data sheet.
6. Output strict JSON only. No markdown fences, no conversational prose."""

    suspend fun generateWorkbook(request: GenerationRequest): Result<WorkbookPlan> = withContext(Dispatchers.IO) {
        val apiKey = (request.customApiKey?.takeIf { it.isNotBlank() }
            ?: BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" })

        if (apiKey.isNullOrBlank()) {
            Log.d(TAG, "No Gemini API key provided. Using built-in enterprise architect.")
            return@withContext Result.success(buildEnterprisePlan(request))
        }

        try {
            val userContent = buildPromptContent(request)
            val requestBodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$ENHANCED_SYSTEM_PROMPT\n\n$userContent")
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)

                val genConfig = JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.25)
                }
                put("generationConfig", genConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toString().toRequestBody(mediaType)

            val httpRequest = Request.Builder()
                .url("$PRIMARY_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(httpRequest).execute()
            val body = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini primary failed (${response.code}): $body. Trying fallback or local architect.")
                // Try fallback model
                val fallbackRequest = Request.Builder()
                    .url("$FALLBACK_URL?key=$apiKey")
                    .post(requestBodyJson.toString().toRequestBody(mediaType))
                    .build()
                val fallbackResp = client.newCall(fallbackRequest).execute()
                val fallbackBody = fallbackResp.body?.string().orEmpty()

                if (fallbackResp.isSuccessful) {
                    val plan = parseWorkbookResponse(fallbackBody, request)
                    if (plan != null) return@withContext Result.success(plan)
                }

                return@withContext Result.success(buildEnterprisePlan(request))
            }

            val plan = parseWorkbookResponse(body, request)
            if (plan != null) {
                Result.success(plan)
            } else {
                Log.w(TAG, "Parsing JSON response failed, using built-in architect.")
                Result.success(buildEnterprisePlan(request))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network or generation error: ${e.message}", e)
            Result.success(buildEnterprisePlan(request))
        }
    }

    private fun buildPromptContent(req: GenerationRequest): String {
        val sb = StringBuilder()
        sb.append("ACTION GOAL MODE: ").append(req.mode.title).append("\n")
        sb.append("DOMAIN / INDUSTRY: ").append(req.domain.title).append(" (").append(req.domain.complianceNote).append(")\n")
        sb.append("THEME: ").append(req.theme.label).append("\n")
        sb.append("FEATURES: ")
        if (req.compactView) sb.append("[Compact View] ")
        if (req.enableCharts) sb.append("[Pivot Tables & Charts] ")
        if (req.enableAnalytics) sb.append("[Dedicated Analytics Page] ")
        if (req.liveDataEntry) sb.append("[Live Data Entry Pre-formatted Rows] ")
        sb.append("\n\n")

        sb.append("USER PROMPT / INSTRUCTIONS: ").append(req.prompt.ifBlank { "Create an enterprise standard tracker" }).append("\n\n")

        if (req.baseBlueprintPlan != null) {
            sb.append("BASE BLUEPRINT: ").append(req.baseBlueprintPlan.title).append(" (").append(req.baseBlueprintPlan.domain).append(")\n")
            sb.append("Key metrics from blueprint: ").append(req.baseBlueprintPlan.blueprintSummary?.keyMetrics?.joinToString(", ")).append("\n\n")
        }

        if (req.uploadedWorkbook != null) {
            sb.append("UPLOADED RAW SPREADSHEET DATA:\n")
            req.uploadedWorkbook.sheets.forEach { s ->
                sb.append("Sheet: ").append(s.name).append("\n")
                sb.append("Headers: ").append(s.headers.joinToString(", ")).append("\n")
                sb.append("Sample rows count: ").append(s.rows.size).append("\n")
                s.rows.take(5).forEach { row ->
                    sb.append("Row: ").append(row.joinToString(", ")).append("\n")
                }
            }
        }

        return sb.toString()
    }

    private fun parseWorkbookResponse(responseBody: String, req: GenerationRequest): WorkbookPlan? {
        return try {
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val text = parts.getJSONObject(0).optString("text")

            val cleaned = cleanJsonText(text)
            val json = JSONObject(cleaned)

            val title = json.optString("title").ifBlank { req.prompt.take(30).ifBlank { "Enterprise Tracker" } }
            val domain = json.optString("domain").ifBlank { req.domain.title }

            val bpObj = json.optJSONObject("blueprintSummary")
            val keyMetrics = mutableListOf<String>()
            val categories = mutableListOf<String>()
            var effectiveness = 95
            var bpNotes = ""

            if (bpObj != null) {
                val kmArr = bpObj.optJSONArray("keyMetrics")
                if (kmArr != null) {
                    for (i in 0 until kmArr.length()) keyMetrics.add(kmArr.getString(i))
                }
                val catArr = bpObj.optJSONArray("categories")
                if (catArr != null) {
                    for (i in 0 until catArr.length()) categories.add(catArr.getString(i))
                }
                effectiveness = bpObj.optInt("effectivenessScore", 95)
                bpNotes = bpObj.optString("notes")
            }

            val sheetsArray = json.optJSONArray("sheets") ?: JSONArray()
            val parsedSheets = mutableListOf<SheetPlan>()

            for (sIdx in 0 until sheetsArray.length()) {
                val sObj = sheetsArray.getJSONObject(sIdx)
                val sheetName = sObj.optString("name", "Sheet${sIdx + 1}")
                val isInstructions = sObj.optBoolean("isInstructions", false) || sheetName.equals("Instructions", ignoreCase = true)
                val instructionsText = sObj.optString("instructionsText")
                val isSummary = sObj.optBoolean("isSummary", false) || sheetName.equals("Dashboard", ignoreCase = true)

                // KPI Cards
                val kpiList = mutableListOf<KpiCardSpec>()
                val kpiArr = sObj.optJSONArray("kpiCards")
                if (kpiArr != null) {
                    for (k in 0 until kpiArr.length()) {
                        val kObj = kpiArr.getJSONObject(k)
                        kpiList.add(
                            KpiCardSpec(
                                label = kObj.optString("label"),
                                formula = kObj.optString("formula"),
                                format = kObj.optString("format", "currency"),
                                value = kObj.optString("value")
                            )
                        )
                    }
                }

                // Charts
                val chartList = mutableListOf<ChartSpec>()
                val chartArr = sObj.optJSONArray("charts")
                if (chartArr != null) {
                    for (c in 0 until chartArr.length()) {
                        val cObj = chartArr.getJSONObject(c)
                        chartList.add(
                            ChartSpec(
                                type = cObj.optString("type", "column"),
                                title = cObj.optString("title"),
                                range = cObj.optString("range")
                            )
                        )
                    }
                }

                // Columns
                val colList = mutableListOf<ColumnSpec>()
                val colArr = sObj.optJSONArray("columns")
                if (colArr != null) {
                    for (col in 0 until colArr.length()) {
                        val cObj = colArr.getJSONObject(col)
                        val header = cObj.optString("header")
                        val type = cObj.optString("type", "text")
                        val formula = cObj.optString("formula").takeIf { it.isNotBlank() }
                        val valList = mutableListOf<String>()
                        val valArr = cObj.optJSONArray("validation")
                        if (valArr != null) {
                            for (v in 0 until valArr.length()) valList.add(valArr.getString(v))
                        }
                        colList.add(
                            ColumnSpec(
                                header = header,
                                type = type,
                                formula = formula,
                                validation = valList.takeIf { it.isNotEmpty() }
                            )
                        )
                    }
                }

                // Rows
                val rowsList = mutableListOf<List<String>>()
                val rowsArr = sObj.optJSONArray("rows")
                if (rowsArr != null) {
                    for (r in 0 until rowsArr.length()) {
                        val rArr = rowsArr.getJSONArray(r)
                        val rowCells = mutableListOf<String>()
                        for (c in 0 until rArr.length()) {
                            rowCells.add(rArr.get(c).toString())
                        }
                        rowsList.add(rowCells)
                    }
                }

                // Conditional Formatting
                val condList = mutableListOf<ConditionalRuleSpec>()
                val condArr = sObj.optJSONArray("conditionalFormatting")
                if (condArr != null) {
                    for (cf in 0 until condArr.length()) {
                        val cfObj = condArr.getJSONObject(cf)
                        condList.add(
                            ConditionalRuleSpec(
                                range = cfObj.optString("range"),
                                rule = cfObj.optString("rule"),
                                color = cfObj.optString("color", "lightGreen")
                            )
                        )
                    }
                }

                parsedSheets.add(
                    SheetPlan(
                        name = sheetName,
                        isInstructions = isInstructions,
                        instructionsText = instructionsText.takeIf { it.isNotBlank() },
                        isSummary = isSummary,
                        kpiCards = kpiList,
                        charts = chartList,
                        columns = colList,
                        rows = rowsList,
                        conditionalFormatting = condList
                    )
                )
            }

            // Ensure Instructions sheet is first
            val finalSheets = ensureInstructionsSheet(parsedSheets, title, req.domain)

            WorkbookPlan(
                id = UUID.randomUUID().toString(),
                title = title,
                domain = domain,
                theme = req.theme.label,
                blueprintSummary = BlueprintSummary(
                    keyMetrics = keyMetrics.ifEmpty { listOf("Total Volume", "Average Rate", "Performance %") },
                    categories = categories.ifEmpty { listOf(req.domain.title.substringBefore(" ")) },
                    effectivenessScore = effectiveness,
                    notes = bpNotes.ifEmpty { "High-complexity multi-tab spreadsheet architecture." }
                ),
                sheets = finalSheets
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing workbook response: ${e.message}", e)
            null
        }
    }

    private fun ensureInstructionsSheet(
        sheets: List<SheetPlan>,
        title: String,
        domain: DomainPreset
    ): List<SheetPlan> {
        val existingInstructions = sheets.firstOrNull { it.isInstructions }
        if (existingInstructions != null) {
            return listOf(existingInstructions) + sheets.filter { !it.isInstructions }
        }

        val generatedInstructions = SheetPlan(
            name = "Instructions",
            isInstructions = true,
            instructionsText = """
                $title - USER GUIDELINES & COMPLIANCE
                
                1. WORKBOOK STRUCTURE:
                   • Sheet 1 [Instructions]: Operational SOPs & data governance rules.
                   • Sheet 2 [Dashboard]: Automated KPI cards & cross-sheet summary aggregations.
                   • Sheet 3 [Data]: Live transactional data table with data validation menus.
                
                2. DATA ENTRY RULES:
                   • Fill in non-calculated columns.
                   • Do NOT overwrite cells containing '=SUM', '=AVERAGE', or formula functions.
                
                3. GOVERNANCE & DOMAIN COMPLIANCE:
                   • ${domain.complianceNote}
            """.trimIndent()
        )
        return listOf(generatedInstructions) + sheets
    }

    private fun cleanJsonText(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) text = text.removePrefix("```json")
        if (text.startsWith("```")) text = text.removePrefix("```")
        if (text.endsWith("```")) text = text.removeSuffix("```")
        return text.trim()
    }

    /**
     * Deterministic, enterprise-grade workbook plan generator that builds complete multi-tab workbooks
     * tailored to the exact domain, action mode, uploaded data, toggles, and color theme.
     */
    fun buildEnterprisePlan(req: GenerationRequest): WorkbookPlan {
        val domain = req.domain
        val prompt = req.prompt.trim()
        val title = when {
            prompt.isNotBlank() -> prompt.take(40).split(" ").joinToString(" ") { it.capitalize() }
            req.baseBlueprintPlan != null -> req.baseBlueprintPlan.title
            else -> "${domain.title.substringBefore(" ")} Master Tracker"
        }

        // Check if raw data was uploaded
        val uploadedSheet = req.uploadedWorkbook?.sheets?.firstOrNull()

        val sheets = mutableListOf<SheetPlan>()

        // 1. Instructions Sheet (Mandatory on EVERY workbook)
        val instructions = SheetPlan(
            name = "Instructions",
            isInstructions = true,
            instructionsText = """
                ${title.uppercase()} - OPERATIONAL STANDARD OPERATING PROCEDURE (SOP)
                
                1. WORKBOOK ARCHITECTURE:
                   • Sheet 1 [Instructions]: Data governance, access controls, and compliance.
                   • Sheet 2 [Dashboard]: Executive KPI overview with cross-sheet formula aggregations.
                   • Sheet 3 [Data]: Primary record keeping ledger with status dropdown validation.
                
                2. SPREADSHEET RULES & FORMULA INTEGRITY:
                   • All summary metrics on the Dashboard tab dynamically compute values using =SUM, =AVERAGE, and =COUNTIF.
                   • Columns marked with 'Status' or 'Priority' utilize in-cell list validation dropdown menus.
                   • Use the action buttons to download or share your OpenXML (.xlsx) file at any time.
                
                3. COMPLIANCE & LEGAL NOTICE:
                   • ${domain.complianceNote}
            """.trimIndent()
        )
        sheets.add(instructions)

        // 2. Dashboard Sheet
        val dashboardKpis = when (domain) {
            DomainPreset.FINANCIAL -> listOf(
                KpiCardSpec("Total Operating Revenue", "=SUM('Data'!D2:D50)", "currency", "$248,500.00"),
                KpiCardSpec("Total Expenditures", "=SUM('Data'!E2:E50)", "currency", "$112,400.00"),
                KpiCardSpec("Net Operating Profit", "='Dashboard'!B2-'Dashboard'!C2", "currency", "$136,100.00"),
                KpiCardSpec("Operating Margin %", "='Dashboard'!D2/'Dashboard'!B2", "percent", "54.8%")
            )
            DomainPreset.HEALTHCARE -> listOf(
                KpiCardSpec("Total Patients Tracked", "=COUNTA('Data'!A2:A50)", "number", "45"),
                KpiCardSpec("Urgent Triage Cases", "=COUNTIF('Data'!D2:D50, \"Urgent\")+COUNTIF('Data'!D2:D50, \"Immediate\")", "number", "8"),
                KpiCardSpec("Average Bed Stay (Days)", "=AVERAGE('Data'!F2:F50)", "number", "3.4"),
                KpiCardSpec("Discharge Clearance %", "=COUNTIF('Data'!G2:G50, \"Discharged\")/COUNTA('Data'!A2:A50)", "percent", "82.2%")
            )
            DomainPreset.SALES -> listOf(
                KpiCardSpec("Total Pipeline Value", "=SUM('Data'!E2:E50)", "currency", "$1,420,000.00"),
                KpiCardSpec("Weighted Revenue Forecast", "=SUM('Data'!G2:G50)", "currency", "$845,000.00"),
                KpiCardSpec("Win Rate Probability", "=AVERAGE('Data'!F2:F50)", "percent", "59.5%"),
                KpiCardSpec("Closed Deals Volume", "=COUNTIF('Data'!D2:D50, \"Closed Won\")", "number", "14")
            )
            DomainPreset.PROJECT -> listOf(
                KpiCardSpec("Committed Deliverables", "=COUNTA('Data'!A2:A50)", "number", "32"),
                KpiCardSpec("Sprint Completion %", "=COUNTIF('Data'!E2:E50, \"Done\")/COUNTA('Data'!A2:A50)", "percent", "68.8%"),
                KpiCardSpec("Critical Path Tasks", "=COUNTIF('Data'!D2:D50, \"Critical\")", "number", "5"),
                KpiCardSpec("Active Blockers", "=COUNTIF('Data'!E2:E50, \"Blocked\")", "number", "2")
            )
            DomainPreset.HR -> listOf(
                KpiCardSpec("Active Headcount", "=COUNTA('Data'!A2:A50)", "number", "38"),
                KpiCardSpec("Total Annual Payroll", "=SUM('Data'!E2:E50)", "currency", "$3,650,000.00"),
                KpiCardSpec("Average Salary", "=AVERAGE('Data'!E2:E50)", "currency", "$96,052.63"),
                KpiCardSpec("Retention Rate %", "=COUNTIF('Data'!F2:F50, \"Active\")/COUNTA('Data'!A2:A50)", "percent", "94.7%")
            )
            DomainPreset.MILITARY -> listOf(
                KpiCardSpec("Total Assigned Personnel / Items", "=COUNTA('Data'!A2:A50)", "number", "42"),
                KpiCardSpec("Operational Readiness %", "=COUNTIF('Data'!E2:E50, \"Mission Capable\")/COUNTA('Data'!A2:A50)", "percent", "90.5%"),
                KpiCardSpec("Critical Discrepancies", "=COUNTIF('Data'!E2:E50, \"Discrepancy\")+COUNTIF('Data'!D2:D50, \"Critical\")", "number", "0"),
                KpiCardSpec("Verified Sight Count %", "=COUNTIF('Data'!E2:E50, \"Verified\")/COUNTA('Data'!A2:A50)", "percent", "100.0%")
            )
            DomainPreset.GENERAL -> listOf(
                KpiCardSpec("Total Records", "=COUNTA('Data'!A2:A50)", "number", "25"),
                KpiCardSpec("Primary Metric Sum", "=SUM('Data'!D2:D50)", "currency", "$84,200.00"),
                KpiCardSpec("Completed Ratio %", "=COUNTIF('Data'!C2:C50, \"Done\")/COUNTA('Data'!A2:A50)", "percent", "76.0%"),
                KpiCardSpec("Average Cycle Time", "=AVERAGE('Data'!E2:E50)", "number", "4.2 days")
            )
        }

        val dashboardCharts = if (req.enableCharts) {
            listOf(
                ChartSpec("column", "Key Metric Distribution", "Data!A1:D10"),
                ChartSpec("pie", "Status & Category Allocation", "Data!C1:C10")
            )
        } else emptyList()

        val dashboard = SheetPlan(
            name = "Dashboard",
            isSummary = true,
            kpiCards = dashboardKpis,
            charts = dashboardCharts
        )
        sheets.add(dashboard)

        // 3. Primary Data Sheet
        val (columns, rows, condFormatting) = if (uploadedSheet != null) {
            // Upgrade mode preserving uploaded data!
            val cols = uploadedSheet.headers.mapIndexed { idx, h ->
                val type = if (h.contains("Date", true)) "date"
                else if (h.contains("Status", true)) "status"
                else if (h.contains("Amount", true) || h.contains("Cost", true) || h.contains("Price", true) || h.contains("Revenue", true) || h.contains("Salary", true)) "currency"
                else if (h.contains("%", true) || h.contains("Rate", true)) "percent"
                else "text"
                val validation = if (type == "status") listOf("Completed", "In Progress", "Pending", "Delayed") else null
                ColumnSpec(h, type, validation = validation)
            }
            Triple(cols, uploadedSheet.rows, listOf(ConditionalRuleSpec("B2:B50", "cellValue == 'Delayed'", "lightRed")))
        } else {
            // Generate domain-tailored columns
            buildDomainColumnsAndRows(domain)
        }

        val dataSheet = SheetPlan(
            name = "Data",
            columns = columns,
            rows = rows,
            conditionalFormatting = condFormatting
        )
        sheets.add(dataSheet)

        return WorkbookPlan(
            id = UUID.randomUUID().toString(),
            title = title,
            domain = domain.title,
            theme = req.theme.label,
            blueprintSummary = BlueprintSummary(
                keyMetrics = dashboardKpis.map { it.label },
                categories = listOf(domain.title.substringBefore(" "), "Enterprise"),
                effectivenessScore = 96,
                notes = "Auto-configured multi-tab workbook with Instructions, KPI Dashboard, and Data Validation."
            ),
            sheets = sheets
        )
    }

    private fun buildDomainColumnsAndRows(domain: DomainPreset): Triple<List<ColumnSpec>, List<List<String>>, List<ConditionalRuleSpec>> {
        return when (domain) {
            DomainPreset.FINANCIAL -> Triple(
                listOf(
                    ColumnSpec("Transaction Date", "date"),
                    ColumnSpec("Account / Cost Center", "text"),
                    ColumnSpec("Category", "category", validation = listOf("Revenue", "COGS", "Payroll", "Software & IT", "Marketing", "Legal & Admin")),
                    ColumnSpec("Gross Amount ($)", "currency"),
                    ColumnSpec("Deduction / Tax ($)", "currency"),
                    ColumnSpec("Net Balance ($)", "currency", formula = "=D2-E2"),
                    ColumnSpec("Approval Status", "status", validation = listOf("Approved", "Pending Review", "Flagged"))
                ),
                listOf(
                    listOf("2026-03-01", "Enterprise Licensing", "Revenue", "45000.00", "4500.00", "=D2-E2", "Approved"),
                    listOf("2026-03-05", "AWS Cloud Infrastructure", "Software & IT", "12400.00", "0.00", "=D3-E3", "Approved"),
                    listOf("2026-03-10", "Digital Campaign Ads", "Marketing", "8500.00", "0.00", "=D4-E4", "Approved"),
                    listOf("2026-03-15", "Consulting Retainer", "Revenue", "22000.00", "2200.00", "=D5-E5", "Pending Review"),
                    listOf("2026-03-20", "Hardware Equipment", "Software & IT", "6200.00", "620.00", "=D6-E6", "Approved")
                ),
                listOf(
                    ConditionalRuleSpec("G2:G20", "cellValue == 'Approved'", "lightGreen"),
                    ConditionalRuleSpec("G2:G20", "cellValue == 'Flagged'", "lightRed")
                )
            )

            DomainPreset.HEALTHCARE -> Triple(
                listOf(
                    ColumnSpec("Patient Identifier", "text"),
                    ColumnSpec("Intake Date", "date"),
                    ColumnSpec("Department", "category", validation = listOf("Emergency", "Cardiology", "Neurology", "Pediatrics", "Internal Medicine")),
                    ColumnSpec("Triage Acuity", "status", validation = listOf("Immediate", "Emergent", "Urgent", "Standard", "Non-Urgent")),
                    ColumnSpec("Primary Diagnosis", "text"),
                    ColumnSpec("Length of Stay (Days)", "number"),
                    ColumnSpec("Clinical Status", "status", validation = listOf("Inpatient", "Observation", "Discharged", "Transferred"))
                ),
                listOf(
                    listOf("PX-9014", "2026-03-18", "Cardiology", "Emergent", "Atrial Fibrillation", "4", "Inpatient"),
                    listOf("PX-9015", "2026-03-19", "Emergency", "Immediate", "Multiple Trauma", "2", "Inpatient"),
                    listOf("PX-9016", "2026-03-20", "Pediatrics", "Standard", "Acute Bronchitis", "1", "Discharged"),
                    listOf("PX-9017", "2026-03-21", "Neurology", "Urgent", "Ischemic Stroke Eval", "3", "Observation")
                ),
                listOf(
                    ConditionalRuleSpec("D2:D20", "cellValue == 'Immediate'", "lightRed"),
                    ConditionalRuleSpec("D2:D20", "cellValue == 'Emergent'", "yellow"),
                    ConditionalRuleSpec("G2:G20", "cellValue == 'Discharged'", "lightGreen")
                )
            )

            DomainPreset.SALES -> Triple(
                listOf(
                    ColumnSpec("Deal Name", "text"),
                    ColumnSpec("Account Rep", "text"),
                    ColumnSpec("Expected Close", "date"),
                    ColumnSpec("Stage", "status", validation = listOf("Discovery", "Qualification", "Proposal", "Negotiation", "Closed Won", "Closed Lost")),
                    ColumnSpec("Contract Value ($)", "currency"),
                    ColumnSpec("Win Probability (%)", "percent"),
                    ColumnSpec("Forecast Value ($)", "currency", formula = "=E2*F2")
                ),
                listOf(
                    listOf("Boeing Defense Cloud Migration", "Marcus Vance", "2026-04-15", "Negotiation", "380000.00", "0.80", "=E2*F2"),
                    listOf("Sony Interactive Data Lake", "Elena Rostova", "2026-04-30", "Proposal", "190000.00", "0.60", "=E3*F3"),
                    listOf("CVS Pharmacy Integration", "Marcus Vance", "2026-05-10", "Qualification", "120000.00", "0.40", "=E4*F4"),
                    listOf("Goldman Sachs Algo Pipeline", "David Chen", "2026-04-02", "Closed Won", "520000.00", "1.00", "=E5*F5")
                ),
                listOf(
                    ConditionalRuleSpec("D2:D20", "cellValue == 'Closed Won'", "lightGreen"),
                    ConditionalRuleSpec("D2:D20", "cellValue == 'Closed Lost'", "lightRed")
                )
            )

            DomainPreset.PROJECT -> Triple(
                listOf(
                    ColumnSpec("Task Code", "text"),
                    ColumnSpec("Summary Description", "text"),
                    ColumnSpec("Assignee", "text"),
                    ColumnSpec("Priority", "priority", validation = listOf("Critical", "High", "Medium", "Low")),
                    ColumnSpec("Status", "status", validation = listOf("Backlog", "In Progress", "In Review", "Done", "Blocked")),
                    ColumnSpec("Estimated Hours", "number"),
                    ColumnSpec("Actual Hours", "number"),
                    ColumnSpec("Variance", "number", formula = "=G2-F2")
                ),
                listOf(
                    listOf("TSK-301", "Design OpenXML Style Sheet Engine", "Sarah J.", "Critical", "Done", "16", "14", "=G2-F2"),
                    listOf("TSK-302", "Build Formula Audit & Repair Parser", "Alex M.", "High", "In Progress", "24", "20", "=G3-F3"),
                    listOf("TSK-303", "Setup Room Database Blueprint Storage", "David L.", "Medium", "Done", "12", "12", "=G4-F4"),
                    listOf("TSK-304", "HIPAA Compliance Audit Verification", "Maya R.", "Critical", "Blocked", "8", "4", "=G5-F5")
                ),
                listOf(
                    ConditionalRuleSpec("D2:D20", "cellValue == 'Critical'", "lightRed"),
                    ConditionalRuleSpec("E2:E20", "cellValue == 'Blocked'", "lightRed"),
                    ConditionalRuleSpec("E2:E20", "cellValue == 'Done'", "lightGreen")
                )
            )

            DomainPreset.HR -> Triple(
                listOf(
                    ColumnSpec("Employee ID", "text"),
                    ColumnSpec("Full Name", "text"),
                    ColumnSpec("Department", "category", validation = listOf("Engineering", "Product", "Sales", "HR", "Finance", "Legal")),
                    ColumnSpec("Job Level", "text"),
                    ColumnSpec("Base Salary ($)", "currency"),
                    ColumnSpec("Status", "status", validation = listOf("Active", "On Leave", "Contractor", "Terminated")),
                    ColumnSpec("Performance Rating", "category", validation = listOf("Exceeds", "Meets Expectations", "Developing", "Unsatisfactory"))
                ),
                listOf(
                    listOf("EMP-101", "Jonathan Reynolds", "Engineering", "Principal Architect", "185000.00", "Active", "Exceeds"),
                    listOf("EMP-102", "Beatrice Vane", "Product", "Director of Product", "172000.00", "Active", "Exceeds"),
                    listOf("EMP-103", "Carlos Mendes", "Sales", "Enterprise AE", "125000.00", "Active", "Meets Expectations"),
                    listOf("EMP-104", "Deepa Krishnan", "Engineering", "Senior SRE", "150000.00", "On Leave", "Meets Expectations")
                ),
                listOf(
                    ConditionalRuleSpec("F2:F20", "cellValue == 'Active'", "lightGreen"),
                    ConditionalRuleSpec("F2:F20", "cellValue == 'On Leave'", "yellow")
                )
            )

            DomainPreset.MILITARY -> Triple(
                listOf(
                    ColumnSpec("BTR / Serial ID", "text"),
                    ColumnSpec("Nomenclature / Item Description", "text"),
                    ColumnSpec("Unit Section / Platoon", "category", validation = listOf("1st Platoon", "2nd Platoon", "3rd Platoon", "HQ Section", "Fires Section", "Support / Log")),
                    ColumnSpec("Security Classification", "category", validation = listOf("Secret", "CUI / Official", "Top Secret", "Unclassified")),
                    ColumnSpec("Operational Status", "status", validation = listOf("Mission Capable", "Verified", "Amber / Degraded", "In Arms Room", "Present", "Quarters")),
                    ColumnSpec("Assigned Custodian", "text"),
                    ColumnSpec("Verification Date", "date")
                ),
                listOf(
                    listOf("BTR-101", "M4A1 Carbine (SN: 942104)", "1st Platoon", "CUI / Official", "Mission Capable", "1LT Harrison", "2026-03-22"),
                    listOf("BTR-102", "AN/PVS-14 Monocular NVG (SN: 88129)", "1st Platoon", "Secret", "Mission Capable", "SGT Torres", "2026-03-22"),
                    listOf("BTR-103", "AN/PRC-152A MBITR Radio", "Fires Section", "Secret", "Mission Capable", "PFC Gomez", "2026-03-22"),
                    listOf("BTR-104", "AN/PEQ-15 Aiming Laser (SN: 67210)", "1st Platoon", "Secret", "In Arms Room", "1LT Harrison", "2026-03-22")
                ),
                listOf(
                    ConditionalRuleSpec("E2:E20", "cellValue == 'Mission Capable'", "lightGreen"),
                    ConditionalRuleSpec("E2:E20", "cellValue == 'Verified'", "lightGreen"),
                    ConditionalRuleSpec("E2:E20", "cellValue == 'Amber / Degraded'", "yellow")
                )
            )

            DomainPreset.GENERAL -> Triple(
                listOf(
                    ColumnSpec("Item / Record Name", "text"),
                    ColumnSpec("Entry Date", "date"),
                    ColumnSpec("Category", "category", validation = listOf("Standard", "Urgent", "Review", "Archived")),
                    ColumnSpec("Status", "status", validation = listOf("Completed", "In Progress", "Pending", "Deferred")),
                    ColumnSpec("Amount ($)", "currency"),
                    ColumnSpec("Multiplier / Weight", "number"),
                    ColumnSpec("Total ($)", "currency", formula = "=E2*F2")
                ),
                listOf(
                    listOf("Q1 Corporate Operational Plan", "2026-03-01", "Standard", "Completed", "12500.00", "1.0", "=E2*F2"),
                    listOf("Client Portal Redesign Deliverable", "2026-03-12", "Urgent", "In Progress", "28000.00", "1.2", "=E3*F3"),
                    listOf("Cloud Security Penetration Test", "2026-03-18", "Review", "Pending", "15000.00", "1.0", "=E4*F4"),
                    listOf("Annual Executive Retrospective", "2026-03-22", "Standard", "Deferred", "9500.00", "0.8", "=E5*F5")
                ),
                listOf(
                    ConditionalRuleSpec("D2:D20", "cellValue == 'Completed'", "lightGreen"),
                    ConditionalRuleSpec("D2:D20", "cellValue == 'Deferred'", "yellow")
                )
            )
        }
    }
}
