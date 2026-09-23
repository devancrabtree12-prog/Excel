package com.example.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.util.zip.ZipInputStream

data class ParsedCell(
    val address: String,
    val value: String,
    val formula: String? = null,
    val isError: Boolean = false
)

data class ParsedSheet(
    val name: String,
    val headers: List<String>,
    val rows: List<List<String>>,
    val cellFormulas: Map<String, String> = emptyMap(),
    val rawErrors: List<String> = emptyList()
)

data class ParsedWorkbook(
    val fileName: String,
    val sheets: List<ParsedSheet>
)

object SpreadsheetParser {

    fun parseCsv(
        content: String,
        fileName: String = "uploaded_data.csv",
        firstRowHeaders: Boolean = true
    ): ParsedWorkbook {
        val lines = content.lines().filter { it.trim().isNotEmpty() }
        if (lines.isEmpty()) {
            return ParsedWorkbook(fileName, listOf(ParsedSheet("Sheet1", listOf("Column 1"), emptyList())))
        }

        val parsedRows = lines.map { parseCsvLine(it) }
        val headers: List<String>
        val dataRows: List<List<String>>

        if (firstRowHeaders && parsedRows.isNotEmpty()) {
            headers = parsedRows.first().mapIndexed { i, h -> h.ifBlank { "Column ${i + 1}" } }
            dataRows = parsedRows.drop(1)
        } else {
            val maxCols = parsedRows.maxOfOrNull { it.size } ?: 1
            headers = (1..maxCols).map { "Column $it" }
            dataRows = parsedRows
        }

        return ParsedWorkbook(
            fileName = fileName,
            sheets = listOf(
                ParsedSheet(
                    name = "Data",
                    headers = headers,
                    rows = dataRows
                )
            )
        )
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        // Detect tab or comma or semicolon delimiter
        val delimiter = if (line.contains("\t") && !line.contains(",")) '\t' else if (line.contains(";") && !line.contains(",")) ';' else ','

        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    sb.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                result.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        result.add(sb.toString().trim())
        return result
    }

    fun parseXlsx(
        inputStream: InputStream,
        fileName: String = "uploaded_sheet.xlsx",
        firstRowHeaders: Boolean = true
    ): ParsedWorkbook {
        val sharedStrings = mutableListOf<String>()
        val sheetEntries = mutableListOf<Pair<String, ByteArray>>() // Name -> XML bytes
        var sheetNames = mutableListOf<String>()

        try {
            val zip = ZipInputStream(inputStream)
            var entry = zip.nextEntry
            while (entry != null) {
                when {
                    entry.name == "xl/sharedStrings.xml" -> {
                        sharedStrings.addAll(parseSharedStrings(zip.readBytes()))
                    }
                    entry.name == "xl/workbook.xml" -> {
                        sheetNames = parseWorkbookSheetNames(zip.readBytes())
                    }
                    entry.name.startsWith("xl/worksheets/sheet") && entry.name.endsWith(".xml") -> {
                        sheetEntries.add(entry.name to zip.readBytes())
                    }
                }
                entry = zip.nextEntry
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val parsedSheets = mutableListOf<ParsedSheet>()
        sheetEntries.forEachIndexed { index, (entryName, bytes) ->
            val sheetName = sheetNames.getOrNull(index) ?: "Sheet${index + 1}"
            val sheet = parseWorksheetXml(bytes, sheetName, sharedStrings, firstRowHeaders)
            parsedSheets.add(sheet)
        }

        if (parsedSheets.isEmpty()) {
            return ParsedWorkbook(fileName, listOf(ParsedSheet("Data", listOf("Column 1"), emptyList())))
        }

        return ParsedWorkbook(fileName = fileName, sheets = parsedSheets)
    }

    private fun parseSharedStrings(xmlBytes: ByteArray): List<String> {
        val strings = mutableListOf<String>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(String(xmlBytes, Charsets.UTF_8)))

            var eventType = parser.eventType
            var inT = false
            val currentText = StringBuilder()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "t") {
                            inT = true
                            currentText.clear()
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inT) currentText.append(parser.text)
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "t") {
                            inT = false
                            strings.add(currentText.toString())
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return strings
    }

    private fun parseWorkbookSheetNames(xmlBytes: ByteArray): MutableList<String> {
        val names = mutableListOf<String>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(String(xmlBytes, Charsets.UTF_8)))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "sheet") {
                    val name = parser.getAttributeValue(null, "name")
                    if (!name.isNullOrBlank()) names.add(name)
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return names
    }

    private fun parseWorksheetXml(
        xmlBytes: ByteArray,
        sheetName: String,
        sharedStrings: List<String>,
        firstRowHeaders: Boolean
    ): ParsedSheet {
        val rowsMap = mutableMapOf<Int, MutableMap<Int, String>>()
        val formulas = mutableMapOf<String, String>()
        val errors = mutableListOf<String>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(String(xmlBytes, Charsets.UTF_8)))

            var eventType = parser.eventType
            var currentCellRef = ""
            var currentCellType = ""
            var currentFormula: String? = null
            var currentValue: String? = null
            var currentTag = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == "c") {
                            currentCellRef = parser.getAttributeValue(null, "r") ?: ""
                            currentCellType = parser.getAttributeValue(null, "t") ?: ""
                            currentFormula = null
                            currentValue = null
                        }
                    }
                    XmlPullParser.TEXT -> {
                        when (currentTag) {
                            "f" -> currentFormula = (currentFormula ?: "") + parser.text
                            "v" -> currentValue = (currentValue ?: "") + parser.text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "c") {
                            // Extract row and col from cell ref (e.g., "B3")
                            val (colIdx, rowIdx) = parseCellRef(currentCellRef)
                            var finalVal = currentValue ?: ""
                            if (currentCellType == "s") {
                                val sIndex = finalVal.toIntOrNull()
                                if (sIndex != null && sIndex in sharedStrings.indices) {
                                    finalVal = sharedStrings[sIndex]
                                }
                            }

                            if (currentFormula != null) {
                                formulas[currentCellRef] = "=" + currentFormula.trimStart('=')
                            }

                            if (finalVal.contains("#REF!") || finalVal.contains("#VALUE!") || finalVal.contains("#DIV/0!")) {
                                errors.add("$currentCellRef: $finalVal")
                            }

                            val row = rowsMap.getOrPut(rowIdx) { mutableMapOf() }
                            row[colIdx] = finalVal
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (rowsMap.isEmpty()) {
            return ParsedSheet(sheetName, listOf("Column 1"), emptyList(), formulas, errors)
        }

        val sortedRowIndices = rowsMap.keys.sorted()
        val maxCol = rowsMap.values.flatMap { it.keys }.maxOrNull() ?: 0

        val grid = sortedRowIndices.map { r ->
            val row = rowsMap[r] ?: emptyMap()
            (0..maxCol).map { c -> row[c] ?: "" }
        }

        val headers: List<String>
        val dataRows: List<List<String>>

        if (firstRowHeaders && grid.isNotEmpty()) {
            headers = grid.first().mapIndexed { i, h -> h.ifBlank { "Column ${i + 1}" } }
            dataRows = grid.drop(1)
        } else {
            headers = (1..(maxCol + 1)).map { "Column $it" }
            dataRows = grid
        }

        return ParsedSheet(sheetName, headers, dataRows, formulas, errors)
    }

    private fun parseCellRef(ref: String): Pair<Int, Int> {
        val colPart = ref.filter { it.isLetter() }.uppercase()
        val rowPart = ref.filter { it.isDigit() }
        var col = 0
        for (c in colPart) {
            col = col * 26 + (c - 'A' + 1)
        }
        val colIdx = (col - 1).coerceAtLeast(0)
        val rowIdx = (rowPart.toIntOrNull() ?: 1) - 1
        return Pair(colIdx, rowIdx)
    }
}
