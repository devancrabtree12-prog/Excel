package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.constants.BrandConstants
import com.example.generator.ExcelXlsxGenerator
import com.example.model.ActionGoalMode
import com.example.model.ColorTheme
import com.example.model.DomainPreset
import com.example.ui.components.AddColumnDialog
import com.example.ui.components.ApiKeyDialog
import com.example.ui.components.AuditReportCard
import com.example.ui.components.BlueprintManagerSheet
import com.example.ui.components.DomainSelector
import com.example.ui.components.FeatureToggles
import com.example.ui.components.HistorySheet
import com.example.ui.components.MainHeader
import com.example.ui.components.ModeSelector
import com.example.ui.components.MultiTabSpreadsheetViewer
import com.example.ui.components.ThemeSelector
import com.example.ui.components.UploadDropzone
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcelTrackerScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ViewModel State
    val actionGoalMode by viewModel.actionGoalMode.collectAsState()
    val domainPreset by viewModel.domainPreset.collectAsState()
    val colorTheme by viewModel.colorTheme.collectAsState()
    val compactView by viewModel.compactView.collectAsState()
    val enableCharts by viewModel.enableCharts.collectAsState()
    val enableAnalytics by viewModel.enableAnalytics.collectAsState()
    val liveDataEntry by viewModel.liveDataEntry.collectAsState()
    val firstRowHeaders by viewModel.firstRowHeaders.collectAsState()
    val uploadedWorkbook by viewModel.uploadedWorkbook.collectAsState()

    val blueprints by viewModel.blueprints.collectAsState()
    val selectedBlueprint by viewModel.selectedBlueprint.collectAsState()
    val currentWorkbook by viewModel.currentWorkbook.collectAsState()
    val activeSheetIndex by viewModel.activeSheetIndex.collectAsState()
    val auditReport by viewModel.auditReport.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val history by viewModel.history.collectAsState()
    val selectedCell by viewModel.selectedCell.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()

    // Dialog & UI State
    var userPromptText by remember { mutableStateOf("") }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showBlueprintSheet by remember { mutableStateOf(false) }
    var showAddColumnDialog by remember { mutableStateOf(false) }

    val isMilitaryMode = domainPreset == DomainPreset.MILITARY

    val themeHex = parseHexColor(colorTheme.primaryHex)

    // File saving launcher for .xlsx
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri: Uri? ->
        if (uri != null && currentWorkbook != null) {
            scope.launch {
                try {
                    val bytes = ExcelXlsxGenerator.generateXlsx(currentWorkbook!!)
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(bytes)
                    }
                    Toast.makeText(context, "Spreadsheet saved successfully!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MainHeader(
                selectedDomain = domainPreset,
                onDomainSelected = { domain ->
                    viewModel.setDomainPreset(domain)
                    if (domain == DomainPreset.MILITARY) {
                        viewModel.setColorTheme(ColorTheme.TACTICAL_OLIVE)
                    }
                },
                blueprintCount = blueprints.size,
                historyCount = history.size,
                hasApiKey = customApiKey.isNotBlank(),
                onOpenBlueprints = { showBlueprintSheet = true },
                onOpenHistory = { showHistorySheet = true },
                onOpenApiKey = { showApiKeyDialog = true }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 0. PORTAL SELECTOR: QuantGrid Enterprise Hub vs TacticalGrid Military Trackers
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMilitaryMode) Color(0xFF1E281C) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, if (isMilitaryMode) Color(0xFF4A5D44) else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().testTag("brand_portal_selector")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Portal Tab 1: QuantGrid Enterprise Hub
                    Surface(
                        onClick = {
                            if (isMilitaryMode) {
                                viewModel.setDomainPreset(DomainPreset.FINANCIAL)
                                viewModel.setColorTheme(ColorTheme.EMERALD_GREEN)
                            }
                        },
                        shape = RoundedCornerShape(9.dp),
                        color = if (!isMilitaryMode) Color(0xFF0F5132) else Color.Transparent,
                        modifier = Modifier.weight(1f).testTag("portal_tab_quantgrid")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🏢", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "QuantGrid",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (!isMilitaryMode) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Enterprise Hub",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (!isMilitaryMode) Color(0xFFD1E7DD) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Portal Tab 2: TacticalGrid Military Trackers & Duty Logs
                    Surface(
                        onClick = {
                            if (!isMilitaryMode) {
                                viewModel.setDomainPreset(DomainPreset.MILITARY)
                                viewModel.setColorTheme(ColorTheme.TACTICAL_OLIVE)
                            }
                        },
                        shape = RoundedCornerShape(9.dp),
                        color = if (isMilitaryMode) Color(0xFF384A33) else Color.Transparent,
                        modifier = Modifier.weight(1.2f).testTag("portal_tab_tacticalgrid")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🎖️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "TacticalGrid",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isMilitaryMode) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Military Trackers & Duty Logs",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isMilitaryMode) Color(0xFFA3B899) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // DYNAMIC PORTAL BANNER & PAGE HEADER
            if (isMilitaryMode) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.5.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth().testTag("tacticalgrid_portal_banner")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF020617),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8))
                                ) {
                                    Text(
                                        text = "TACTICALGRID // OPSEC",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF451A03),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                                ) {
                                    Text(
                                        text = "CLASSIFIED // NOFORN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color(0xFFFCD34D),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF052E16),
                                border = BorderStroke(1.dp, Color(0xFF22C55E))
                            ) {
                                Text(
                                    text = "DEFCON-1 READY",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4ADE80),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TacticalGrid Mission Trackers & Duty Log Architecture",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Mission-Critical Operational Trackers & Duty Log Architecture: standardized DA 1594 duty desk logs, PERSTAT accountability, sensitive item property serials, and tactical execution matrices.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D3E27)),
                    border = BorderStroke(1.dp, Color(0xFF1E6B47)),
                    modifier = Modifier.fillMaxWidth().testTag("quantgrid_portal_banner")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF105C38)
                                ) {
                                    Text(
                                        text = "QUANTGRID ENTERPRISE HUB",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color(0xFF86EFAC),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1E40AF).copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        text = "SOX & HIPAA READY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = Color(0xFF93C5FD),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("OPENXML .XLSX", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF86EFAC))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "QuantGrid Intelligent Workbook Generator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Enterprise spreadsheet architecture, automated cross-sheet formula reconciliation, dynamic KPI dashboards, and verified template blueprints.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD1E7DD)
                        )
                    }
                }
            }

            // 1. ACTION GOAL MODE SELECTOR
            ModeSelector(
                selectedMode = actionGoalMode,
                onModeSelected = { viewModel.setActionGoalMode(it) }
            )

            // 2. ACTIVE BASE BLUEPRINT INDICATOR (If selected)
            selectedBlueprint?.let { bp ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bookmarks,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Base Template: ${bp.title}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Category: ${bp.category} • Formulas: ${bp.keyFormulas}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // 3. PROMPT INPUT & FAST INSPIRATION CHIPS
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SPECIFICATIONS & PROMPT",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                OutlinedTextField(
                    value = userPromptText,
                    onValueChange = { userPromptText = it },
                    label = {
                        val labelText = when (actionGoalMode) {
                            ActionGoalMode.CREATE_NEW -> "Describe your tracker (or select a blueprint)..."
                            ActionGoalMode.UPGRADE_EXISTING -> "Instructions for upgrade (color, formatting, KPIs)..."
                            ActionGoalMode.AUDIT_AND_FIX -> "Specific formula audit notes (optional)..."
                            ActionGoalMode.ANALYZE_AND_LEARN -> "Title for learned blueprint template..."
                        }
                        Text(labelText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prompt_input_field"),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 4
                )

                // Quick Prompt Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val promptChips = if (isMilitaryMode) {
                        when (actionGoalMode) {
                            ActionGoalMode.CREATE_NEW -> listOf(
                                "PERSTAT Personnel Accountability",
                                "Sensitive Items Property Book",
                                "Staff Duty & Incident Log (DA 1594)",
                                "Mission Execution Matrix & Sync",
                                "Arms Room Master Inventory",
                                "Convoy Movement Manifest"
                            )
                            ActionGoalMode.UPGRADE_EXISTING -> listOf(
                                "Standardize military duty log entries",
                                "Inject PERSTAT readiness KPIs",
                                "Highlight critical security discrepancies",
                                "Format ZULU time & serial references"
                            )
                            ActionGoalMode.AUDIT_AND_FIX -> listOf(
                                "Audit property serial numbers & verify totals",
                                "Fix #REF! in battle roster formulas",
                                "Verify 100% headcount reconciliation",
                                "Validate COMSEC inventory sums"
                            )
                            ActionGoalMode.ANALYZE_AND_LEARN -> listOf(
                                "PERSTAT Accountability Blueprint",
                                "Tactical Duty Log Blueprint",
                                "Mission Sync Matrix Architecture"
                            )
                        }
                    } else {
                        when (actionGoalMode) {
                            ActionGoalMode.CREATE_NEW -> listOf(
                                "SaaS Financial Run-Rate Model",
                                "Patient Clinical Intake",
                                "B2B Sales Pipeline",
                                "Agile Sprint Delivery",
                                "Quarterly HR Headcount",
                                "Vendor Audit Log"
                            )
                            ActionGoalMode.UPGRADE_EXISTING -> listOf(
                                "Modernize theme & format currencies",
                                "Add Executive KPI Dashboard",
                                "Format dates and highlight status"
                            )
                            ActionGoalMode.AUDIT_AND_FIX -> listOf(
                                "Fix #REF! and add column totals",
                                "Reconcile cross-sheet variance",
                                "Verify sum ranges"
                            )
                            ActionGoalMode.ANALYZE_AND_LEARN -> listOf(
                                "Enterprise SaaS Template",
                                "Clinical Patient Registry Blueprint",
                                "Revenue Forecast Blueprint"
                            )
                        }
                    }

                    promptChips.forEach { chipText ->
                        FilterChip(
                            selected = userPromptText == chipText,
                            onClick = { userPromptText = chipText },
                            label = { Text(chipText, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isMilitaryMode) Color(0xFF384A33) else MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = if (isMilitaryMode) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // 4. RAW DATA DROPZONE (Visible for Upgrade, Audit, or New)
            UploadDropzone(
                uploadedWorkbook = uploadedWorkbook,
                firstRowHeaders = firstRowHeaders,
                onFirstRowHeadersChange = { viewModel.toggleFirstRowHeaders(it) },
                onFileSelected = { uri, fileName -> viewModel.uploadFileFromUri(uri, fileName) },
                onRawCsvEntered = { text, name -> viewModel.uploadRawCsv(text, name) },
                onClearFile = { viewModel.clearUploadedFile() }
            )

            // 5. DOMAIN / INDUSTRY PRESET SELECTOR
            DomainSelector(
                selectedDomain = domainPreset,
                onDomainSelected = { viewModel.setDomainPreset(it) }
            )

            // 6. ADVANCED FEATURE TOGGLES
            FeatureToggles(
                compactView = compactView,
                onCompactViewChange = { viewModel.toggleCompactView(it) },
                enableCharts = enableCharts,
                onEnableChartsChange = { viewModel.toggleCharts(it) },
                enableAnalytics = enableAnalytics,
                onEnableAnalyticsChange = { viewModel.toggleAnalytics(it) },
                liveDataEntry = liveDataEntry,
                onLiveDataEntryChange = { viewModel.toggleLiveDataEntry(it) }
            )

            // 7. COLOR THEME SELECTOR
            ThemeSelector(
                selectedTheme = colorTheme,
                onThemeSelected = { viewModel.setColorTheme(it) }
            )

            // 8. PRIMARY ACTION BUTTON
            val buttonText = when (actionGoalMode) {
                ActionGoalMode.CREATE_NEW -> if (isMilitaryMode) "Generate Tactical Mission Workbook (.xlsx)" else "Generate Multi-Tab Workbook (.xlsx)"
                ActionGoalMode.UPGRADE_EXISTING -> if (isMilitaryMode) "Upgrade & Standardize Tactical Log" else "Upgrade & Redesign Sheet"
                ActionGoalMode.AUDIT_AND_FIX -> if (isMilitaryMode) "Audit & Verify Tactical Formulas" else "Audit & Repair Formulas"
                ActionGoalMode.ANALYZE_AND_LEARN -> if (isMilitaryMode) "Extract & Save Tactical Blueprint" else "Extract & Save to Blueprint Library"
            }

            Button(
                onClick = { viewModel.executeCurrentMode(userPromptText) },
                enabled = !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("execute_action_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeHex
                )
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Architecting Spreadsheet...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = when (actionGoalMode) {
                            ActionGoalMode.CREATE_NEW -> Icons.Default.AutoAwesome
                            ActionGoalMode.UPGRADE_EXISTING -> Icons.Default.TableChart
                            ActionGoalMode.AUDIT_AND_FIX -> Icons.Default.AutoAwesome
                            ActionGoalMode.ANALYZE_AND_LEARN -> Icons.Default.Bookmarks
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // 9. AUDIT REPORT CARD (If available)
            auditReport?.let { report ->
                AuditReportCard(report = report)
            }

            // 10. INTERACTIVE MULTI-TAB SPREADSHEET VIEWER
            currentWorkbook?.let { wb ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = wb.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${wb.domain} • ${wb.sheets.size} Sheets • Theme: ${wb.theme}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Save as blueprint button
                        OutlinedButton(
                            onClick = {
                                viewModel.extractAndLearnBlueprint(wb.title)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_as_blueprint_btn")
                        ) {
                            Icon(Icons.Default.Bookmarks, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Blueprint", fontSize = 11.sp)
                        }
                    }

                    MultiTabSpreadsheetViewer(
                        workbook = wb,
                        activeSheetIndex = activeSheetIndex,
                        selectedCell = selectedCell,
                        onSelectSheet = { viewModel.selectSheet(it) },
                        onSelectCell = { col, row -> viewModel.selectCell(col, row) },
                        onUpdateCell = { col, row, newVal -> viewModel.updateCell(col, row, newVal) },
                        onAddRow = { viewModel.addRow() },
                        onAddColumn = { showAddColumnDialog = true },
                        onDeleteColumn = { viewModel.deleteColumn(it) },
                        onDeleteRow = { viewModel.deleteRow(it) }
                    )

                    // EXPORT & SHARING BUTTONS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                saveFileLauncher.launch("${wb.slug}.xlsx")
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeHex),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("download_xlsx_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download (.xlsx)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                shareWorkbookFile(context, wb)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("share_xlsx_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share in Excel", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val csv = ExcelXlsxGenerator.generateCsv(wb)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("CSV Data", csv)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "CSV copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(44.dp)
                                .testTag("copy_csv_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy CSV", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Blueprint Library Bottom Sheet
    if (showBlueprintSheet) {
        BlueprintManagerSheet(
            blueprints = blueprints,
            selectedBlueprint = selectedBlueprint,
            isMilitaryMode = isMilitaryMode,
            onSelectBlueprint = { bp ->
                viewModel.applyBlueprint(bp)
                userPromptText = bp.title
            },
            onDeleteBlueprint = { viewModel.deleteCustomBlueprint(it) },
            onDismiss = { showBlueprintSheet = false }
        )
    }

    // History Sheet
    if (showHistorySheet) {
        HistorySheet(
            history = history,
            onSelectTracker = { entity ->
                viewModel.loadSavedTracker(entity)
                showHistorySheet = false
            },
            onDeleteTracker = { id -> viewModel.deleteTracker(id) },
            onDismiss = { showHistorySheet = false }
        )
    }

    // Add Column Dialog
    if (showAddColumnDialog) {
        AddColumnDialog(
            currentColumnCount = currentWorkbook?.dataSheets?.firstOrNull()?.columns?.size ?: 3,
            onDismiss = { showAddColumnDialog = false },
            onAddColumn = { name, defaultValue ->
                viewModel.addColumn(name, "text", defaultValue)
                showAddColumnDialog = false
            }
        )
    }

    // API Key Dialog
    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentCustomKey = customApiKey,
            onDismiss = { showApiKeyDialog = false },
            onSaveKey = { key ->
                viewModel.setCustomApiKey(key)
                showApiKeyDialog = false
                Toast.makeText(context, "API Key saved in local session", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

private fun shareWorkbookFile(context: Context, workbook: com.example.model.WorkbookPlan) {
    try {
        val bytes = ExcelXlsxGenerator.generateXlsx(workbook)
        val file = File(context.cacheDir, "${workbook.slug}.xlsx")
        FileOutputStream(file).use { it.write(bytes) }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, workbook.title)
            putExtra(Intent.EXTRA_TEXT, "Here is the generated spreadsheet: ${workbook.title}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Open or Share Spreadsheet"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error preparing file: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun parseHexColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    val colorInt = clean.toLong(16).toInt()
    return Color(0xFF000000 or colorInt.toLong())
}
