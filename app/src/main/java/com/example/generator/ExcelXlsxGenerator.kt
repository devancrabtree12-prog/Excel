package com.example.generator

import com.example.model.ColorTheme
import com.example.model.ColumnSpec
import com.example.model.ColumnType
import com.example.model.SheetPlan
import com.example.model.TrackerPlan
import com.example.model.WorkbookPlan
import com.example.model.cleanCell
import com.example.model.toColumnLetter
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ExcelXlsxGenerator {

    /**
     * Generates a fully compliant OpenXML (.xlsx) binary archive from a multi-tab WorkbookPlan.
     */
    fun generateXlsx(workbook: WorkbookPlan): ByteArray {
        val theme = ColorTheme.fromLabel(workbook.theme)
        val byteOut = ByteArrayOutputStream()
        ZipOutputStream(byteOut).use { zip ->
            addZipEntry(zip, "[Content_Types].xml", buildContentTypesXml(workbook.sheets.size))
            addZipEntry(zip, "_rels/.rels", buildRootRelsXml())
            addZipEntry(zip, "xl/_rels/workbook.xml.rels", buildWorkbookRelsXml(workbook.sheets.size))
            addZipEntry(zip, "xl/workbook.xml", buildWorkbookXml(workbook.sheets))
            addZipEntry(zip, "xl/styles.xml", buildStylesXml(theme))

            workbook.sheets.forEachIndexed { index, sheet ->
                val sheetXml = if (sheet.isInstructions) {
                    buildInstructionsSheetXml(sheet, workbook.title, theme)
                } else if (sheet.isSummary) {
                    buildDashboardSheetXml(sheet, workbook.title, theme)
                } else {
                    buildDataSheetXml(sheet, theme)
                }
                addZipEntry(zip, "xl/worksheets/sheet${index + 1}.xml", sheetXml)
            }
        }
        return byteOut.toByteArray()
    }

    /**
     * Backward-compatible overload for legacy TrackerPlan.
     */
    fun generateXlsx(plan: TrackerPlan): ByteArray {
        val colSpecs = plan.headers.mapIndexed { idx, h ->
            val type = plan.getColumnType(idx)
            val typeStr = when (type) {
                ColumnType.CURRENCY -> "currency"
                ColumnType.PERCENT -> "percent"
                ColumnType.DATE -> "date"
                ColumnType.STATUS -> "status"
                ColumnType.NUMBER -> "number"
                else -> "text"
            }
            ColumnSpec(h, typeStr)
        }

        val converted = WorkbookPlan(
            title = plan.title,
            sheets = listOf(
                SheetPlan(
                    name = "Instructions",
                    isInstructions = true,
                    instructionsText = "Instructions & Guidance for ${plan.title}"
                ),
                SheetPlan(
                    name = "Data",
                    columns = colSpecs,
                    rows = plan.sample_rows
                )
            )
        )
        return generateXlsx(converted)
    }

    /**
     * Generates standard comma-separated values (CSV) representation of the primary data sheet.
     */
    fun generateCsv(workbook: WorkbookPlan): String {
        val dataSheet = workbook.dataSheets.firstOrNull() ?: workbook.sheets.firstOrNull()
        if (dataSheet == null) return ""

        val sb = StringBuilder()
        sb.appendLine(dataSheet.columns.joinToString(",") { escapeCsv(it.header) })
        for (row in dataSheet.rows) {
            sb.appendLine(row.joinToString(",") { escapeCsv(it) })
        }
        return sb.toString()
    }

    fun generateCsv(plan: TrackerPlan): String {
        val sb = StringBuilder()
        sb.appendLine(plan.headers.joinToString(",") { escapeCsv(it) })
        for (row in plan.sample_rows) {
            sb.appendLine(row.joinToString(",") { escapeCsv(it) })
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun addZipEntry(zip: ZipOutputStream, path: String, content: String) {
        val entry = ZipEntry(path)
        zip.putNextEntry(entry)
        zip.write(content.toByteArray(StandardCharsets.UTF_8))
        zip.closeEntry()
    }

    private fun escapeXml(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun buildContentTypesXml(sheetCount: Int): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
""")
        for (i in 1..sheetCount) {
            sb.append("  <Override PartName=\"/xl/worksheets/sheet$i.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>\n")
        }
        sb.append("</Types>")
        return sb.toString()
    }

    private fun buildRootRelsXml(): String = """
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>
    """.trimIndent()

    private fun buildWorkbookRelsXml(sheetCount: Int): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
""")
        for (i in 1..sheetCount) {
            sb.append("  <Relationship Id=\"rId$i\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet$i.xml\"/>\n")
        }
        sb.append("  <Relationship Id=\"rId${sheetCount + 1}\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>\n")
        sb.append("</Relationships>")
        return sb.toString()
    }

    private fun buildWorkbookXml(sheets: List<SheetPlan>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <bookViews>
    <workbookView xWindow="0" yWindow="0" windowWidth="24000" windowHeight="12000"/>
  </bookViews>
  <sheets>
""")
        sheets.forEachIndexed { index, sheet ->
            val cleanSheetName = escapeXml(sheet.name.take(31).ifBlank { "Sheet${index + 1}" })
            sb.append("    <sheet name=\"$cleanSheetName\" sheetId=\"${index + 1}\" r:id=\"rId${index + 1}\"/>\n")
        }
        sb.append("""  </sheets>
  <calcPr calcId="124519" fullCalcOnLoad="1"/>
</workbook>""")
        return sb.toString()
    }

    private fun buildStylesXml(theme: ColorTheme): String {
        val primaryHex = theme.primaryHex.removePrefix("#")
        val lightBgHex = theme.lightBgHex.removePrefix("#")

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <numFmts count="3">
    <numFmt numFmtId="164" formatCode="${'$'}#,##0.00"/>
    <numFmt numFmtId="165" formatCode="0.0%"/>
    <numFmt numFmtId="166" formatCode="yyyy-mm-dd"/>
  </numFmts>
  <fonts count="6">
    <font><sz val="11"/><color theme="1"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
    <font><b/><sz val="16"/><color rgb="FF$primaryHex"/><name val="Calibri"/></font>
    <font><b/><sz val="13"/><color rgb="FF$primaryHex"/><name val="Calibri"/></font>
    <font><sz val="10"/><color rgb="FF555555"/><name val="Calibri"/></font>
    <font><b/><sz val="18"/><color rgb="FF1F2937"/><name val="Calibri"/></font>
  </fonts>
  <fills count="6">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF$primaryHex"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFF8FAFC"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF$lightBgHex"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFFEE2E2"/></patternFill></fill>
  </fills>
  <borders count="3">
    <border><left/><right/><top/><bottom/></border>
    <border>
      <left style="thin"><color rgb="FFCBD5E1"/></left>
      <right style="thin"><color rgb="FFCBD5E1"/></right>
      <top style="thin"><color rgb="FFCBD5E1"/></top>
      <bottom style="thin"><color rgb="FFCBD5E1"/></bottom>
    </border>
    <border>
      <left style="medium"><color rgb="FF$primaryHex"/></left>
      <right style="medium"><color rgb="FF$primaryHex"/></right>
      <top style="medium"><color rgb="FF$primaryHex"/></top>
      <bottom style="medium"><color rgb="FF$primaryHex"/></bottom>
    </border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="11">
    <!-- 0: Regular text -->
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyBorder="1"/>
    <!-- 1: Header Row: Primary fill, white bold font -->
    <xf numFmtId="0" fontId="1" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center" wrapText="1"/>
    </xf>
    <!-- 2: Currency format -->
    <xf numFmtId="164" fontId="0" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="right"/>
    </xf>
    <!-- 3: Percentage format -->
    <xf numFmtId="165" fontId="0" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="right"/>
    </xf>
    <!-- 4: Date format -->
    <xf numFmtId="166" fontId="0" fillId="0" borderId="1" xfId="0" applyNumberFormat="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center"/>
    </xf>
    <!-- 5: Zebra row: light slate fill -->
    <xf numFmtId="0" fontId="0" fillId="3" borderId="1" xfId="0" applyFill="1" applyBorder="1"/>
    <!-- 6: Title banner -->
    <xf numFmtId="0" fontId="2" fillId="4" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
    <!-- 7: Instructions box -->
    <xf numFmtId="0" fontId="0" fillId="4" borderId="2" xfId="0" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment vertical="top" wrapText="1"/>
    </xf>
    <!-- 8: KPI Card label -->
    <xf numFmtId="0" fontId="4" fillId="4" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center"/>
    </xf>
    <!-- 9: KPI Card big value -->
    <xf numFmtId="0" fontId="5" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1">
      <alignment horizontal="center" vertical="center"/>
    </xf>
    <!-- 10: Section Subtitle -->
    <xf numFmtId="0" fontId="3" fillId="0" borderId="0" xfId="0" applyFont="1"/>
  </cellXfs>
</styleSheet>"""
    }

    private fun buildInstructionsSheetXml(sheet: SheetPlan, title: String, theme: ColorTheme): String {
        val lines = (sheet.instructionsText ?: "Welcome to $title").lines()
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <cols>
    <col min="1" max="1" width="5" customWidth="1"/>
    <col min="2" max="2" width="85" customWidth="1"/>
    <col min="3" max="3" width="15" customWidth="1"/>
  </cols>
  <sheetData>
    <row r="1" ht="36" customHeight="1">
      <c r="B1" s="6" t="inlineStr">
        <is><t>${escapeXml(title)} — SYSTEM INSTRUCTIONS &amp; GUIDANCE</t></is>
      </c>
    </row>
    <row r="2" ht="8"/>
""")
        var rowNum = 3
        lines.forEach { line ->
            val trimmed = line.trim()
            val style = if (trimmed.startsWith("1.") || trimmed.startsWith("2.") || trimmed.startsWith("3.") || trimmed.endsWith(":")) 10 else 0
            sb.append("    <row r=\"$rowNum\" ht=\"20\" customHeight=\"1\">\n")
            sb.append("      <c r=\"B$rowNum\" s=\"$style\" t=\"inlineStr\"><is><t>${escapeXml(trimmed)}</t></is></c>\n")
            sb.append("    </row>\n")
            rowNum++
        }

        sb.append("""  </sheetData>
</worksheet>""")
        return sb.toString()
    }

    private fun buildDashboardSheetXml(sheet: SheetPlan, title: String, theme: ColorTheme): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <cols>
    <col min="1" max="1" width="4" customWidth="1"/>
    <col min="2" max="2" width="28" customWidth="1"/>
    <col min="3" max="3" width="28" customWidth="1"/>
    <col min="4" max="4" width="28" customWidth="1"/>
    <col min="5" max="5" width="28" customWidth="1"/>
  </cols>
  <sheetData>
    <row r="1" ht="32" customHeight="1">
      <c r="B1" s="6" t="inlineStr"><is><t>${escapeXml(title)} — EXECUTIVE DASHBOARD</t></is></c>
    </row>
    <row r="2" ht="12"/>
    <row r="3" ht="24" customHeight="1">
      <c r="B3" s="10" t="inlineStr"><is><t>Key Performance Indicators (KPIs)</t></is></c>
    </row>
""")

        // Render KPI Cards (up to 4 in a grid across B4..E4 and B5..E5)
        if (sheet.kpiCards.isNotEmpty()) {
            val cards = sheet.kpiCards.take(4)
            // Header label row (Row 4)
            sb.append("    <row r=\"4\" ht=\"22\" customHeight=\"1\">\n")
            cards.forEachIndexed { index, card ->
                val colLetter = toColumnLetter(index + 1) // B, C, D, E
                sb.append("      <c r=\"$colLetter 4\" s=\"8\" t=\"inlineStr\"><is><t>${escapeXml(card.label)}</t></is></c>\n")
            }
            sb.append("    </row>\n")

            // Value / Formula row (Row 5)
            sb.append("    <row r=\"5\" ht=\"36\" customHeight=\"1\">\n")
            cards.forEachIndexed { index, card ->
                val colLetter = toColumnLetter(index + 1)
                val formula = card.formula.trimStart('=').trim()
                val displayVal = card.value ?: "0"
                sb.append("      <c r=\"$colLetter 5\" s=\"9\">\n")
                if (formula.isNotBlank()) {
                    sb.append("        <f>${escapeXml(formula)}</f>\n")
                }
                sb.append("        <v>${escapeXml(displayVal.replace("$", "").replace("%", ""))}</v>\n")
                sb.append("      </c>\n")
            }
            sb.append("    </row>\n")
        }

        // Render visual charts section
        if (sheet.charts.isNotEmpty()) {
            sb.append("    <row r=\"7\" ht=\"14\"/>\n")
            sb.append("    <row r=\"8\" ht=\"24\" customHeight=\"1\">\n")
            sb.append("      <c r=\"B8\" s=\"10\" t=\"inlineStr\"><is><t>Summary Analytics &amp; Visual Funnels</t></is></c>\n")
            sb.append("    </row>\n")

            sheet.charts.forEachIndexed { index, chart ->
                val r = 9 + (index * 3)
                sb.append("    <row r=\"$r\" ht=\"20\" customHeight=\"1\">\n")
                sb.append("      <c r=\"B$r\" s=\"8\" t=\"inlineStr\"><is><t>${escapeXml(chart.type.uppercase())} CHART: ${escapeXml(chart.title)}</t></is></c>\n")
                sb.append("      <c r=\"C$r\" s=\"0\" t=\"inlineStr\"><is><t>Data Range: ${escapeXml(chart.range)}</t></is></c>\n")
                sb.append("    </row>\n")
            }
        }

        sb.append("""  </sheetData>
</worksheet>""")
        return sb.toString()
    }

    private fun buildDataSheetXml(sheet: SheetPlan, theme: ColorTheme): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
""")

        // Column widths
        sb.append("  <cols>\n")
        sheet.columns.forEachIndexed { index, col ->
            val colNum = index + 1
            val width = (col.header.length + 8).coerceIn(14, 35)
            sb.append("    <col min=\"$colNum\" max=\"$colNum\" width=\"$width\" customWidth=\"1\"/>\n")
        }
        sb.append("  </cols>\n")

        sb.append("  <sheetData>\n")

        // 1. Header Row
        sb.append("    <row r=\"1\" ht=\"28\" customHeight=\"1\">\n")
        sheet.columns.forEachIndexed { cIdx, col ->
            val cellRef = "${toColumnLetter(cIdx)}1"
            sb.append("      <c r=\"$cellRef\" s=\"1\" t=\"inlineStr\"><is><t>${escapeXml(col.header)}</t></is></c>\n")
        }
        sb.append("    </row>\n")

        // 2. Data Rows
        sheet.rows.forEachIndexed { rIdx, row ->
            val rowNum = rIdx + 2
            val isZebra = rIdx % 2 == 1
            val baseStyle = if (isZebra) 5 else 0

            sb.append("    <row r=\"$rowNum\" ht=\"22\" customHeight=\"1\">\n")
            sheet.columns.forEachIndexed { cIdx, col ->
                val cellRef = "${toColumnLetter(cIdx)}$rowNum"
                val rawVal = row.getOrNull(cIdx)?.trim().orEmpty()
                val colType = col.type.lowercase()

                val cellStyle = when {
                    colType == "currency" -> 2
                    colType == "percent" -> 3
                    colType == "date" -> 4
                    else -> baseStyle
                }

                if (rawVal.startsWith("=")) {
                    val formulaText = rawVal.removePrefix("=")
                    sb.append("      <c r=\"$cellRef\" s=\"$cellStyle\"><f>${escapeXml(formulaText)}</f></c>\n")
                } else if (rawVal.isNotEmpty()) {
                    val numericVal = rawVal.replace("$", "").replace("%", "").replace(",", "").trim().toDoubleOrNull()
                    if (numericVal != null && colType != "text" && colType != "status" && colType != "category") {
                        sb.append("      <c r=\"$cellRef\" s=\"$cellStyle\"><v>$numericVal</v></c>\n")
                    } else {
                        sb.append("      <c r=\"$cellRef\" s=\"$cellStyle\" t=\"inlineStr\"><is><t>${escapeXml(rawVal)}</t></is></c>\n")
                    }
                } else {
                    sb.append("      <c r=\"$cellRef\" s=\"$cellStyle\"/>\n")
                }
            }
            sb.append("    </row>\n")
        }
        sb.append("  </sheetData>\n")

        // 3. Conditional Formatting
        if (sheet.conditionalFormatting.isNotEmpty()) {
            sheet.conditionalFormatting.forEach { cf ->
                val cleanRange = escapeXml(cf.range)
                val fillId = if (cf.color.contains("Red", true)) 5 else 4
                sb.append("""  <conditionalFormatting sqref="$cleanRange">
    <cfRule type="cellIs" operator="equal" priority="1">
      <formula>"${escapeXml(cf.rule.substringAfter("==").replace("'", "").trim())}"</formula>
    </cfRule>
  </conditionalFormatting>
""")
            }
        }

        // 4. Data Validation (In-Cell Menus for Status / Category / Priority columns)
        val validationCols = sheet.columns.mapIndexedNotNull { idx, col ->
            if (!col.validation.isNullOrEmpty()) Pair(idx, col.validation) else null
        }

        if (validationCols.isNotEmpty()) {
            val totalRows = (sheet.rows.size + 50).coerceAtLeast(100)
            sb.append("  <dataValidations count=\"${validationCols.size}\">\n")
            validationCols.forEach { (colIdx, options) ->
                val colLetter = toColumnLetter(colIdx)
                val rangeRef = "${colLetter}2:${colLetter}$totalRows"
                val optionString = options.joinToString(",")
                sb.append("    <dataValidation type=\"list\" allowBlank=\"1\" showInputMessage=\"1\" showErrorMessage=\"1\" sqref=\"$rangeRef\">\n")
                sb.append("      <formula1>&quot;${escapeXml(optionString)}&quot;</formula1>\n")
                sb.append("    </dataValidation>\n")
            }
            sb.append("  </dataValidations>\n")
        }

        sb.append("</worksheet>")
        return sb.toString()
    }
}
