package com.example

import com.example.generator.ExcelXlsxGenerator
import com.example.model.ColumnType
import com.example.model.FormulaSpec
import com.example.model.TrackerPlan
import com.example.model.inferType
import com.example.model.slugify
import com.example.model.toColumnLetter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.zip.ZipInputStream

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testSlugify() {
    assertEquals("sales-commission-tracker", slugify("Sales Commission Tracker"))
    assertEquals("software-audit-log", slugify("Software Audit Log!"))
  }

  @Test
  fun testToColumnLetter() {
    assertEquals("A", toColumnLetter(0))
    assertEquals("B", toColumnLetter(1))
    assertEquals("Z", toColumnLetter(25))
    assertEquals("AA", toColumnLetter(26))
  }

  @Test
  fun testTypeInference() {
    assertEquals(ColumnType.CURRENCY, inferType("Total Commission", listOf("4500.00", "1200.50")))
    assertEquals(ColumnType.PERCENT, inferType("Commission Rate", listOf("0.08", "0.10")))
    assertEquals(ColumnType.DATE, inferType("Close Date", listOf("2026-03-12")))
    assertEquals(ColumnType.STATUS, inferType("Deal Status", listOf("Paid", "Pending")))
  }

  @Test
  fun testXlsxGeneration() {
    val plan = TrackerPlan(
      title = "Commission Tracker",
      headers = listOf("Rep", "Deal Size", "Rate", "Payout"),
      sample_rows = listOf(
        listOf("Sarah Jenkins", "50000", "0.10", "=B2*C2"),
        listOf("Michael Chang", "80000", "0.08", "=B3*C3")
      ),
      formulas = listOf(
        FormulaSpec("D2", "=B2*C2"),
        FormulaSpec("D3", "=B3*C3")
      )
    )

    val bytes = ExcelXlsxGenerator.generateXlsx(plan)
    assertTrue("Generated xlsx should not be empty", bytes.isNotEmpty())

    // Validate that it is a valid ZIP containing the standard OpenXML entries
    val entries = mutableListOf<String>()
    ZipInputStream(bytes.inputStream()).use { zip ->
      var entry = zip.nextEntry
      while (entry != null) {
        entries.add(entry.name)
        entry = zip.nextEntry
      }
    }

    assertTrue(entries.contains("[Content_Types].xml"))
    assertTrue(entries.contains("_rels/.rels"))
    assertTrue(entries.contains("xl/workbook.xml"))
    assertTrue(entries.contains("xl/styles.xml"))
    assertTrue(entries.contains("xl/worksheets/sheet1.xml"))

    val csv = ExcelXlsxGenerator.generateCsv(plan)
    assertTrue(csv.contains("Rep,Deal Size,Rate,Payout"))
    assertTrue(csv.contains("Sarah Jenkins,50000,0.10,=B2*C2"))
  }

  @Test
  fun testAddColumn() {
    val plan = TrackerPlan(
      title = "Inventory",
      headers = listOf("Item", "Qty"),
      sample_rows = listOf(
        listOf("Widget A", "100"),
        listOf("Widget B", "250")
      )
    )

    // Add a "Status" column
    val updatedHeaders = plan.headers + "Status"
    val updatedRows = plan.sample_rows.map { it + "In Stock" }
    val updatedPlan = plan.copy(headers = updatedHeaders, sample_rows = updatedRows)

    assertEquals(3, updatedPlan.headers.size)
    assertEquals("Status", updatedPlan.headers[2])
    assertEquals("In Stock", updatedPlan.sample_rows[0][2])

    val bytes = ExcelXlsxGenerator.generateXlsx(updatedPlan)
    assertTrue("Generated xlsx with added column should not be empty", bytes.isNotEmpty())
  }

  @Test
  fun testPrebuiltBlueprints() {
    val blueprints = com.example.data.PrebuiltBlueprints.getList()
    assertTrue("Should have seeded enterprise blueprints", blueprints.size >= 5)
    val finance = blueprints.first { it.id == "prebuilt-saas-model" }
    assertEquals("Finance", finance.category)
    val plan = finance.toWorkbookPlan()
    assertNotNull("Blueprint should parse to WorkbookPlan", plan)
    assertEquals(3, plan!!.sheets.size)
    assertTrue("Should have instructions tab", plan.sheets.any { it.isInstructions })
    assertTrue("Should have summary dashboard tab", plan.sheets.any { it.isSummary })
    assertTrue("Should have data ledger tab", plan.sheets.any { !it.isInstructions && !it.isSummary })
  }

  @Test
  fun testWorkbookAuditorRepair() {
    val rawCsv = """
Date,Client,Amount,Commission
2026-03-01,Acme,$1200,#REF!
2026-03-05,Globex,$800,#VALUE!
2026-03-10,Initech,$1500,$150
    """.trimIndent()

    val parsed = com.example.parser.SpreadsheetParser.parseCsv(rawCsv, "test_ledger.csv", true)
    val (report, repairedPlan) = com.example.audit.WorkbookAuditor.auditAndRepair(
      parsed = parsed,
      domain = "Financial",
      theme = "Emerald Green"
    )

    assertTrue("Audit should detect broken formulas", report.brokenFormulasFixed >= 2)
    assertTrue("Audit should create multi-tab repaired workbook", repairedPlan.sheets.size >= 2)
    val dataSheet = repairedPlan.dataSheets.first()
    // Row 1 formula #REF! repaired to product or sum formula
    val fixedRow0 = dataSheet.rows[0][3]
    assertTrue("Repaired formula should start with =", fixedRow0.startsWith("="))
  }

  @Test
  fun testMultiTabOpenXmlGeneration() {
    val plan = com.example.data.PrebuiltBlueprints.getList().first().toWorkbookPlan()!!
    val bytes = ExcelXlsxGenerator.generateXlsx(plan)
    assertTrue("Multi-tab xlsx should not be empty", bytes.isNotEmpty())

    val entries = mutableListOf<String>()
    ZipInputStream(bytes.inputStream()).use { zip ->
      var entry = zip.nextEntry
      while (entry != null) {
        entries.add(entry.name)
        entry = zip.nextEntry
      }
    }

    assertTrue(entries.contains("xl/worksheets/sheet1.xml"))
    assertTrue(entries.contains("xl/worksheets/sheet2.xml"))
    assertTrue(entries.contains("xl/worksheets/sheet3.xml"))
  }

  @Test
  fun testDynamicBrandingSystem() {
    val quantGrid = com.example.constants.BrandConstants.forDomain(com.example.model.DomainPreset.FINANCIAL)
    assertEquals("QuantGrid", quantGrid.name)
    assertEquals("Enterprise Spreadsheet Architecture & Intelligent Workbook Generator", quantGrid.subtitle)
    assertEquals(false, quantGrid.isTactical)
    assertTrue("Should have enterprise badges", quantGrid.statusBadges.any { it.code == "SOX_AUDIT" })

    val tacticalGrid = com.example.constants.BrandConstants.forDomain(com.example.model.DomainPreset.MILITARY)
    assertEquals("TacticalGrid", tacticalGrid.name)
    assertEquals("Mission-Critical Operational Trackers & Duty Log Architecture", tacticalGrid.subtitle)
    assertEquals(true, tacticalGrid.isTactical)
    assertTrue("Should have high-contrast DEFCON badge", tacticalGrid.statusBadges.any { it.code == "DEFCON_1" })
    assertTrue("Should have CLASSIFIED NOFORN badge", tacticalGrid.statusBadges.any { it.code == "CLASSIFIED_NOFORN" })
  }
}

