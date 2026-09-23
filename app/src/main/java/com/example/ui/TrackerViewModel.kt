package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiTrackerService
import com.example.ai.GenerationRequest
import com.example.audit.WorkbookAuditor
import com.example.data.AppDatabase
import com.example.data.BlueprintEntity
import com.example.data.PrebuiltBlueprints
import com.example.data.TrackerEntity
import com.example.model.ActionGoalMode
import com.example.model.AuditReport
import com.example.model.BlueprintSummary
import com.example.model.ColorTheme
import com.example.model.ColumnSpec
import com.example.model.ColumnType
import com.example.model.DomainPreset
import com.example.model.SheetPlan
import com.example.model.TrackerPlan
import com.example.model.WorkbookPlan
import com.example.model.toColumnLetter
import com.example.parser.ParsedWorkbook
import com.example.parser.SpreadsheetParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val trackerDao = db.trackerDao()
    private val blueprintDao = db.blueprintDao()

    // 1. Current Action Goal Mode
    private val _actionGoalMode = MutableStateFlow(ActionGoalMode.CREATE_NEW)
    val actionGoalMode: StateFlow<ActionGoalMode> = _actionGoalMode.asStateFlow()

    // 2. Domain Preset
    private val _domainPreset = MutableStateFlow(DomainPreset.FINANCIAL)
    val domainPreset: StateFlow<DomainPreset> = _domainPreset.asStateFlow()

    // Dual-Engine Toggle: Standard Mode (false) vs Advanced Architecture (true)
    private val _isAdvancedArchitecture = MutableStateFlow(false)
    val isAdvancedArchitecture: StateFlow<Boolean> = _isAdvancedArchitecture.asStateFlow()

    // 3. Color Theme
    private val _colorTheme = MutableStateFlow(ColorTheme.EMERALD_GREEN)
    val colorTheme: StateFlow<ColorTheme> = _colorTheme.asStateFlow()

    // 4. Advanced Feature Toggles
    private val _compactView = MutableStateFlow(false)
    val compactView: StateFlow<Boolean> = _compactView.asStateFlow()

    private val _enableCharts = MutableStateFlow(true)
    val enableCharts: StateFlow<Boolean> = _enableCharts.asStateFlow()

    private val _enableAnalytics = MutableStateFlow(true)
    val enableAnalytics: StateFlow<Boolean> = _enableAnalytics.asStateFlow()

    private val _liveDataEntry = MutableStateFlow(false)
    val liveDataEntry: StateFlow<Boolean> = _liveDataEntry.asStateFlow()

    // 5. Raw Data Upload
    private val _firstRowHeaders = MutableStateFlow(true)
    val firstRowHeaders: StateFlow<Boolean> = _firstRowHeaders.asStateFlow()

    private val _uploadedWorkbook = MutableStateFlow<ParsedWorkbook?>(null)
    val uploadedWorkbook: StateFlow<ParsedWorkbook?> = _uploadedWorkbook.asStateFlow()

    // 6. Template Library & Blueprints
    val blueprints: StateFlow<List<BlueprintEntity>> = blueprintDao.getAllBlueprints()
        .combine(MutableStateFlow(PrebuiltBlueprints.getList())) { dbBlueprints, prebuilts ->
            val all = mutableListOf<BlueprintEntity>()
            all.addAll(prebuilts)
            // Add custom ones from DB (filtering duplicates)
            dbBlueprints.filter { !it.isPrebuilt }.forEach { all.add(it) }
            all
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrebuiltBlueprints.getList())

    private val _selectedBlueprint = MutableStateFlow<BlueprintEntity?>(null)
    val selectedBlueprint: StateFlow<BlueprintEntity?> = _selectedBlueprint.asStateFlow()

    // 7. Active Workbook & Multi-tab Navigation
    private val _currentWorkbook = MutableStateFlow<WorkbookPlan?>(null)
    val currentWorkbook: StateFlow<WorkbookPlan?> = _currentWorkbook.asStateFlow()

    private val _activeSheetIndex = MutableStateFlow(0)
    val activeSheetIndex: StateFlow<Int> = _activeSheetIndex.asStateFlow()

    // Legacy TrackerPlan adapter for backward-compatibility
    private val _currentPlan = MutableStateFlow<TrackerPlan?>(null)
    val currentPlan: StateFlow<TrackerPlan?> = _currentPlan.asStateFlow()

    // 8. Audit Report
    private val _auditReport = MutableStateFlow<AuditReport?>(null)
    val auditReport: StateFlow<AuditReport?> = _auditReport.asStateFlow()

    // 9. Status & Progress
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _selectedCell = MutableStateFlow<Pair<Int, Int>?>(Pair(0, 0))
    val selectedCell: StateFlow<Pair<Int, Int>?> = _selectedCell.asStateFlow()

    private val _customApiKey = MutableStateFlow("")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    val history: StateFlow<List<TrackerEntity>> = trackerDao.getAllTrackers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Pre-populate database with prebuilt blueprints
        viewModelScope.launch {
            try {
                blueprintDao.insertAll(PrebuiltBlueprints.getList())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Initialize with default SaaS Financial Model
        val defaultBlueprint = PrebuiltBlueprints.getList().first()
        val defaultPlan = defaultBlueprint.toWorkbookPlan()
        if (defaultPlan != null) {
            setWorkbookPlan(defaultPlan)
        }
    }

    // Setters for Mode, Domain, Theme, Toggles
    fun setActionGoalMode(mode: ActionGoalMode) {
        _actionGoalMode.value = mode
    }

    fun setDomainPreset(domain: DomainPreset) {
        val prev = _domainPreset.value
        _domainPreset.value = domain
        if (domain == DomainPreset.MILITARY) {
            setColorTheme(ColorTheme.TACTICAL_OLIVE)
        } else if (prev == DomainPreset.MILITARY) {
            setColorTheme(ColorTheme.EMERALD_GREEN)
        }
    }

    fun toggleAdvancedArchitecture(enabled: Boolean) {
        _isAdvancedArchitecture.value = enabled
    }

    fun setColorTheme(theme: ColorTheme) {
        _colorTheme.value = theme
        // If we have an active workbook, update its theme
        val wb = _currentWorkbook.value
        if (wb != null) {
            _currentWorkbook.value = wb.copy(theme = theme.label)
        }
    }

    fun toggleCompactView(enabled: Boolean) { _compactView.value = enabled }
    fun toggleCharts(enabled: Boolean) { _enableCharts.value = enabled }
    fun toggleAnalytics(enabled: Boolean) { _enableAnalytics.value = enabled }
    fun toggleLiveDataEntry(enabled: Boolean) { _liveDataEntry.value = enabled }
    fun toggleFirstRowHeaders(enabled: Boolean) { _firstRowHeaders.value = enabled }

    fun setCustomApiKey(key: String) {
        _customApiKey.value = key.trim()
    }

    fun selectSheet(index: Int) {
        val wb = _currentWorkbook.value ?: return
        if (index in wb.sheets.indices) {
            _activeSheetIndex.value = index
            _selectedCell.value = Pair(0, 0)
        }
    }

    fun selectCell(col: Int, row: Int) {
        _selectedCell.value = Pair(col, row)
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // Raw File Handling
    fun uploadRawCsv(content: String, fileName: String = "uploaded_data.csv") {
        try {
            val parsed = SpreadsheetParser.parseCsv(content, fileName, _firstRowHeaders.value)
            _uploadedWorkbook.value = parsed
            _statusMessage.value = "Loaded ${parsed.sheets.firstOrNull()?.rows?.size ?: 0} rows from $fileName"
        } catch (e: Exception) {
            _statusMessage.value = "Failed to parse CSV: ${e.message}"
        }
    }

    fun uploadFileFromUri(uri: Uri, fileName: String) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    if (fileName.endsWith(".xlsx", ignoreCase = true)) {
                        val parsed = SpreadsheetParser.parseXlsx(stream, fileName, _firstRowHeaders.value)
                        _uploadedWorkbook.value = parsed
                        _statusMessage.value = "Parsed Excel workbook: ${parsed.sheets.size} sheets loaded."
                    } else {
                        val content = stream.bufferedReader().use { it.readText() }
                        val parsed = SpreadsheetParser.parseCsv(content, fileName, _firstRowHeaders.value)
                        _uploadedWorkbook.value = parsed
                        _statusMessage.value = "Parsed CSV file: ${parsed.sheets.firstOrNull()?.rows?.size ?: 0} rows."
                    }
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error reading file: ${e.message}"
            }
        }
    }

    fun clearUploadedFile() {
        _uploadedWorkbook.value = null
        _statusMessage.value = "Cleared uploaded file"
    }

    // Primary Generation / Execution based on selected mode
    fun executeCurrentMode(prompt: String) {
        when (_actionGoalMode.value) {
            ActionGoalMode.CREATE_NEW -> generateNewTracker(prompt)
            ActionGoalMode.UPGRADE_EXISTING -> upgradeExistingSheet(prompt)
            ActionGoalMode.AUDIT_AND_FIX -> auditAndFixSpreadsheet()
            ActionGoalMode.ANALYZE_AND_LEARN -> extractAndLearnBlueprint(prompt)
        }
    }

    fun generateNewTracker(prompt: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            _statusMessage.value = "Generating multi-tab enterprise workbook..."
            _auditReport.value = null

            try {
                val req = GenerationRequest(
                    mode = ActionGoalMode.CREATE_NEW,
                    prompt = prompt,
                    domain = _domainPreset.value,
                    theme = _colorTheme.value,
                    compactView = _compactView.value,
                    enableCharts = _enableCharts.value,
                    enableAnalytics = _enableAnalytics.value,
                    liveDataEntry = _liveDataEntry.value,
                    baseBlueprintPlan = _selectedBlueprint.value?.toWorkbookPlan(),
                    customApiKey = _customApiKey.value
                )
                val result = GeminiTrackerService.generateWorkbook(req)
                result.onSuccess { workbook ->
                    setWorkbookPlan(workbook)
                    _statusMessage.value = "Created '${workbook.title}' with ${workbook.sheets.size} sheets & ${workbook.totalFormulaCount} formulas."
                }.onFailure {
                    _statusMessage.value = "Generation error: ${it.message}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Execution failed: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun upgradeExistingSheet(prompt: String) {
        val uploaded = _uploadedWorkbook.value
        if (uploaded == null) {
            _statusMessage.value = "Please upload a .csv or .xlsx file first to upgrade!"
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _statusMessage.value = "Upgrading sheet with color theme & KPI cards..."
            _auditReport.value = null

            try {
                val req = GenerationRequest(
                    mode = ActionGoalMode.UPGRADE_EXISTING,
                    prompt = prompt.ifBlank { "Upgrade layout, apply ${colorTheme.value.label} theme, and insert KPI deck." },
                    domain = _domainPreset.value,
                    theme = _colorTheme.value,
                    compactView = _compactView.value,
                    enableCharts = _enableCharts.value,
                    enableAnalytics = _enableAnalytics.value,
                    liveDataEntry = _liveDataEntry.value,
                    uploadedWorkbook = uploaded,
                    customApiKey = _customApiKey.value
                )
                val result = GeminiTrackerService.generateWorkbook(req)
                result.onSuccess { workbook ->
                    setWorkbookPlan(workbook)
                    _statusMessage.value = "Redesigned '${workbook.title}' with ${workbook.sheets.size} tabs & preserved data!"
                }.onFailure {
                    _statusMessage.value = "Upgrade failed: ${it.message}"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Upgrade error: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun auditAndFixSpreadsheet() {
        val uploaded = _uploadedWorkbook.value
        if (uploaded == null) {
            _statusMessage.value = "Please upload a spreadsheet to audit broken formulas!"
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _statusMessage.value = "Auditing formula references, #REF! errors, and missing totals..."

            try {
                val (report, repairedWorkbook) = WorkbookAuditor.auditAndRepair(
                    parsed = uploaded,
                    domain = _domainPreset.value.title,
                    theme = _colorTheme.value.label
                )
                _auditReport.value = report
                setWorkbookPlan(repairedWorkbook)
                _statusMessage.value = "Audit complete: ${report.brokenFormulasFixed} broken formulas repaired, ${report.missingTotalsInserted} totals inserted!"
            } catch (e: Exception) {
                _statusMessage.value = "Audit error: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun extractAndLearnBlueprint(customTitle: String) {
        val wb = _currentWorkbook.value
        if (wb == null) {
            _statusMessage.value = "No active tracker to extract blueprint from!"
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            _statusMessage.value = "Extracting column architecture and formula syntax..."

            try {
                val title = customTitle.ifBlank { "Custom ${wb.domain.substringBefore(" ")} Blueprint" }
                val summary = BlueprintSummary(
                    keyMetrics = wb.sheets.flatMap { s -> s.kpiCards.map { it.label } }.ifEmpty { listOf("Volume", "Run-Rate") },
                    categories = listOf(wb.domain.substringBefore(" "), "Learned"),
                    effectivenessScore = 96,
                    notes = "Blueprint learned from ${wb.title}. Multi-tab with ${wb.sheets.size} sheets."
                )
                val updatedPlan = wb.copy(title = title, blueprintSummary = summary)
                val entity = BlueprintEntity.fromWorkbookPlan(
                    plan = updatedPlan,
                    category = summary.categories.first(),
                    isPrebuilt = false
                )
                blueprintDao.insertBlueprint(entity)
                _selectedBlueprint.value = entity
                _statusMessage.value = "Blueprint saved to Template Library: '${title}'!"
            } catch (e: Exception) {
                _statusMessage.value = "Extraction error: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun applyBlueprint(blueprint: BlueprintEntity) {
        _selectedBlueprint.value = blueprint
        val plan = blueprint.toWorkbookPlan()
        if (plan != null) {
            setWorkbookPlan(plan)
            val matchingDomain = DomainPreset.fromTitle(plan.domain)
            _domainPreset.value = matchingDomain
            val matchingTheme = ColorTheme.fromLabel(plan.theme)
            _colorTheme.value = matchingTheme
            _statusMessage.value = "Applied Blueprint '${blueprint.title}'"
        }
    }

    fun deleteCustomBlueprint(id: String) {
        viewModelScope.launch {
            blueprintDao.deleteCustomBlueprint(id)
            if (_selectedBlueprint.value?.id == id) {
                _selectedBlueprint.value = null
            }
            _statusMessage.value = "Blueprint removed from library"
        }
    }

    private fun setWorkbookPlan(workbook: WorkbookPlan) {
        _currentWorkbook.value = workbook
        // Default active sheet to Dashboard or first data sheet if Instructions is 0
        _activeSheetIndex.value = if (workbook.sheets.size > 1) 1 else 0
        _selectedCell.value = Pair(0, 0)

        // Keep legacy trackerPlan in sync for existing tests / components
        val dataSheet = workbook.dataSheets.firstOrNull() ?: workbook.sheets.firstOrNull()
        if (dataSheet != null) {
            val legacy = TrackerPlan(
                id = workbook.id,
                title = workbook.title,
                headers = dataSheet.columns.map { it.header },
                sample_rows = dataSheet.rows,
                createdAt = workbook.createdAt
            )
            _currentPlan.value = legacy
            viewModelScope.launch {
                trackerDao.insertTracker(TrackerEntity.fromTrackerPlan(legacy))
            }
        }
    }

    // Grid Editing: Update Cell, Add Row, Add Column, Delete Column, Delete Row
    fun updateCell(colIndex: Int, rowIndex: Int, newValue: String) {
        val wb = _currentWorkbook.value ?: return
        val currentSheetIdx = _activeSheetIndex.value
        if (currentSheetIdx !in wb.sheets.indices) return
        val sheet = wb.sheets[currentSheetIdx]

        if (rowIndex !in sheet.rows.indices) return
        val currentRows = sheet.rows.map { it.toMutableList() }.toMutableList()
        val row = currentRows[rowIndex]
        while (row.size <= colIndex) {
            row.add("")
        }
        row[colIndex] = newValue

        val updatedSheet = sheet.copy(rows = currentRows)
        val updatedSheets = wb.sheets.toMutableList()
        updatedSheets[currentSheetIdx] = updatedSheet

        val updatedWb = wb.copy(sheets = updatedSheets)
        _currentWorkbook.value = updatedWb

        // Also update legacy if this was dataSheet
        _currentPlan.value?.let { legacy ->
            if (sheet.isDataTable) {
                _currentPlan.value = legacy.copy(sample_rows = currentRows)
            }
        }
    }

    fun addRow() {
        val wb = _currentWorkbook.value ?: return
        val currentSheetIdx = _activeSheetIndex.value
        if (currentSheetIdx !in wb.sheets.indices) return
        val sheet = wb.sheets[currentSheetIdx]
        if (!sheet.isDataTable && sheet.columns.isEmpty()) return

        val newRow = MutableList(sheet.columns.size) { "" }
        val newRowIndex = sheet.rows.size + 2 // 1-indexed in Excel (row 1 is header)

        // Adapt formulas if column has formula
        sheet.columns.forEachIndexed { cIdx, col ->
            if (col.formula != null) {
                newRow[cIdx] = col.formula.replace("2", newRowIndex.toString())
            }
        }

        val updatedRows = sheet.rows + listOf(newRow)
        val updatedSheet = sheet.copy(rows = updatedRows)
        val updatedSheets = wb.sheets.toMutableList()
        updatedSheets[currentSheetIdx] = updatedSheet

        val updatedWb = wb.copy(sheets = updatedSheets)
        _currentWorkbook.value = updatedWb
        _statusMessage.value = "Added row to ${sheet.name}"
    }

    fun addColumn(columnName: String, colType: String = "text", defaultValue: String = "") {
        val wb = _currentWorkbook.value ?: return
        val currentSheetIdx = _activeSheetIndex.value
        if (currentSheetIdx !in wb.sheets.indices) return
        val sheet = wb.sheets[currentSheetIdx]
        if (!sheet.isDataTable) return

        val cleanName = columnName.trim().ifEmpty { "New Column" }
        val validation = if (colType == "status") listOf("Active", "Pending", "Done") else null
        val newCol = ColumnSpec(cleanName, colType, validation = validation)

        val updatedColumns = sheet.columns + newCol
        val updatedRows = sheet.rows.mapIndexed { rIdx, row ->
            val rowNum = rIdx + 2
            val cellVal = if (defaultValue.startsWith("=")) {
                defaultValue.replace("2", rowNum.toString())
            } else {
                defaultValue
            }
            row + listOf(cellVal)
        }

        val updatedSheet = sheet.copy(columns = updatedColumns, rows = updatedRows)
        val updatedSheets = wb.sheets.toMutableList()
        updatedSheets[currentSheetIdx] = updatedSheet

        val updatedWb = wb.copy(sheets = updatedSheets)
        _currentWorkbook.value = updatedWb
        _statusMessage.value = "Added Column '$cleanName' to ${sheet.name}"

        // Update legacy plan
        _currentPlan.value?.let { legacy ->
            _currentPlan.value = legacy.copy(
                headers = updatedColumns.map { it.header },
                sample_rows = updatedRows
            )
        }
    }

    fun deleteColumn(columnIndex: Int) {
        val wb = _currentWorkbook.value ?: return
        val currentSheetIdx = _activeSheetIndex.value
        if (currentSheetIdx !in wb.sheets.indices) return
        val sheet = wb.sheets[currentSheetIdx]
        if (!sheet.isDataTable || sheet.columns.size <= 1 || columnIndex !in sheet.columns.indices) return

        val colName = sheet.columns[columnIndex].header
        val updatedColumns = sheet.columns.filterIndexed { idx, _ -> idx != columnIndex }
        val updatedRows = sheet.rows.map { row ->
            row.filterIndexed { idx, _ -> idx != columnIndex }
        }

        val updatedSheet = sheet.copy(columns = updatedColumns, rows = updatedRows)
        val updatedSheets = wb.sheets.toMutableList()
        updatedSheets[currentSheetIdx] = updatedSheet

        val updatedWb = wb.copy(sheets = updatedSheets)
        _currentWorkbook.value = updatedWb
        _statusMessage.value = "Removed Column '$colName'"
    }

    fun deleteRow(rowIndex: Int) {
        val wb = _currentWorkbook.value ?: return
        val currentSheetIdx = _activeSheetIndex.value
        if (currentSheetIdx !in wb.sheets.indices) return
        val sheet = wb.sheets[currentSheetIdx]
        if (rowIndex !in sheet.rows.indices) return

        val updatedRows = sheet.rows.filterIndexed { index, _ -> index != rowIndex }
        val updatedSheet = sheet.copy(rows = updatedRows)
        val updatedSheets = wb.sheets.toMutableList()
        updatedSheets[currentSheetIdx] = updatedSheet

        val updatedWb = wb.copy(sheets = updatedSheets)
        _currentWorkbook.value = updatedWb
        _statusMessage.value = "Deleted row"
    }

    // Backward compatibility for legacy tests
    fun generateTracker(prompt: String) {
        generateNewTracker(prompt)
    }

    fun loadSavedTracker(entity: TrackerEntity) {
        val plan = entity.toTrackerPlan()
        _currentPlan.value = plan
        val converted = WorkbookPlan(
            id = plan.id,
            title = plan.title,
            sheets = listOf(
                SheetPlan(name = "Instructions", isInstructions = true, instructionsText = "Guidance for ${plan.title}"),
                SheetPlan(
                    name = "Data",
                    columns = plan.headers.map { ColumnSpec(it, "text") },
                    rows = plan.sample_rows
                )
            )
        )
        _currentWorkbook.value = converted
        _activeSheetIndex.value = 1
        _statusMessage.value = "Loaded '${plan.title}'"
    }

    fun deleteTracker(id: String) {
        viewModelScope.launch {
            trackerDao.deleteTrackerById(id)
            if (_currentPlan.value?.id == id) {
                _currentPlan.value = null
            }
            _statusMessage.value = "Tracker deleted"
        }
    }
}
