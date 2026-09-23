package com.example.data

import com.example.model.BlueprintSummary
import com.example.model.ChartSpec
import com.example.model.ColumnSpec
import com.example.model.ConditionalRuleSpec
import com.example.model.DomainPreset
import com.example.model.KpiCardSpec
import com.example.model.SheetPlan
import com.example.model.WorkbookPlan

object PrebuiltBlueprints {
    fun getList(): List<BlueprintEntity> {
        return listOf(
            createSaasModel(),
            createPatientIntake(),
            createSalesPipeline(),
            createProjectTracker(),
            createHrOperations(),
            createPerstatAccountability(),
            createSensitiveItemsPropertyBook(),
            createStaffDutyDeskLog(),
            createMissionExecutionMatrix()
        )
    }

    private fun createSaasModel(): BlueprintEntity {
        val plan = WorkbookPlan(
            id = "prebuilt-saas-model",
            title = "SaaS Financial Run-Rate Model",
            domain = DomainPreset.FINANCIAL.title,
            theme = "Emerald Green",
            blueprintSummary = BlueprintSummary(
                keyMetrics = listOf("ARR Run-Rate", "Net MRR Growth", "Gross Margin %", "Rule of 40"),
                categories = listOf("Finance", "Executive", "SaaS"),
                effectivenessScore = 98,
                notes = "Enterprise multi-tab recurring revenue model with automated cross-sheet variance and dynamic summary KPI deck."
            ),
            sheets = listOf(
                SheetPlan(
                    name = "Instructions",
                    isInstructions = true,
                    instructionsText = """
                        WELCOME TO THE SAAS FINANCIAL RUN-RATE MODEL
                        
                        1. WORKBOOK ARCHITECTURE:
                           • Sheet 1 [Instructions]: Operational SOPs & SOX audit guardrails.
                           • Sheet 2 [Dashboard]: Executive KPI summary cards with cross-sheet formulas referencing ARR ledgers.
                           • Sheet 3 [Subscriptions]: Live customer contract ledger with MRR, Renewal Dates, and Expansion tiers.
                           • Sheet 4 [OpEx]: Departmental expenditures and headcount burn rates.
                        
                        2. PROTECTED FORMULAS:
                           • All summary cards on [Dashboard] are linked via =SUM('Subscriptions'!E2:E50).
                           • Do NOT overwrite calculated columns marked with 'fx'.
                        
                        3. COMPLIANCE & GOVERNANCE:
                           • SOX & GAAP Compliant: Revenue recognized monthly over active contract terms.
                    """.trimIndent()
                ),
                SheetPlan(
                    name = "Dashboard",
                    isSummary = true,
                    kpiCards = listOf(
                        KpiCardSpec(label = "Total Annual Run-Rate (ARR)", formula = "=SUM('Subscriptions'!F2:F20)*12", format = "currency", value = "$1,840,000.00"),
                        KpiCardSpec(label = "Active MRR", formula = "=SUM('Subscriptions'!F2:F20)", format = "currency", value = "$153,333.33"),
                        KpiCardSpec(label = "Avg Deal Size (ACV)", formula = "=AVERAGE('Subscriptions'!F2:F20)*12", format = "currency", value = "$92,000.00"),
                        KpiCardSpec(label = "Gross Margin", formula = "=('Dashboard'!B2-'OpEx'!C2)/'Dashboard'!B2", format = "percent", value = "81.4%")
                    ),
                    charts = listOf(
                        ChartSpec(type = "column", title = "Monthly Run-Rate by Tier", range = "Subscriptions!C2:F10"),
                        ChartSpec(type = "pie", title = "Revenue Distribution by Tier", range = "Subscriptions!D2:D10")
                    )
                ),
                SheetPlan(
                    name = "Subscriptions",
                    columns = listOf(
                        ColumnSpec("Account Name", type = "text"),
                        ColumnSpec("Contract Date", type = "date"),
                        ColumnSpec("Subscription Tier", type = "category", validation = listOf("Enterprise", "Mid-Market", "Scale-Up")),
                        ColumnSpec("Contract Status", type = "status", validation = listOf("Active", "Renewed", "Pending", "At Risk")),
                        ColumnSpec("Billing Cycle", type = "category", validation = listOf("Annual", "Quarterly", "Monthly")),
                        ColumnSpec("MRR ($)", type = "currency")
                    ),
                    rows = listOf(
                        listOf("Acme Global Corp", "2026-01-15", "Enterprise", "Active", "Annual", "24500.00"),
                        listOf("Horizon Logistics", "2026-02-01", "Enterprise", "Active", "Annual", "18200.00"),
                        listOf("Apex FinTech Ltd", "2026-02-18", "Mid-Market", "Active", "Annual", "12500.00"),
                        listOf("Nova Biotech", "2026-03-05", "Scale-Up", "Pending", "Quarterly", "8900.00"),
                        listOf("Sterling Media", "2026-03-20", "Mid-Market", "Active", "Annual", "14200.00")
                    ),
                    conditionalFormatting = listOf(
                        ConditionalRuleSpec("D2:D10", "cellValue == 'At Risk'", "lightRed"),
                        ConditionalRuleSpec("D2:D10", "cellValue == 'Active'", "lightGreen")
                    )
                )
            )
        )
        return BlueprintEntity.fromWorkbookPlan(plan, category = "Finance", isPrebuilt = true)
    }

    private fun createPatientIntake(): BlueprintEntity {
        val plan = WorkbookPlan(
            id = "prebuilt-patient-intake",
            title = "Patient Clinical Intake & Triage Tracker",
            domain = DomainPreset.HEALTHCARE.title,
            theme = "Modern Blue",
            blueprintSummary = BlueprintSummary(
                keyMetrics = listOf("Daily Patient Volume", "Average Wait Time (Min)", "Critical Triage Rate %", "Bed Utilization"),
                categories = listOf("Healthcare", "Clinical", "Operations"),
                effectivenessScore = 97,
                notes = "HIPAA-aligned clinical triage workflow featuring acuity prioritization and admission timestamp metrics."
            ),
            sheets = listOf(
                SheetPlan(
                    name = "Instructions",
                    isInstructions = true,
                    instructionsText = """
                        PATIENT CLINICAL INTAKE & TRIAGE PROTOCOL
                        
                        1. WORKBOOK ARCHITECTURE:
                           • Sheet 1 [Instructions]: Clinical governance & HIPAA privacy notices.
                           • Sheet 2 [Triage Overview]: Bed utilization counters and triage acuity breakdown.
                           • Sheet 3 [Patient Registry]: Active intake queue with priority flags and vital status.
                        
                        2. DATA PROTECTION & HIPAA MANDATE:
                           • STRICT CONFIDENTIALITY: Contains Protected Health Information (PHI).
                           • Do NOT export or print without explicit charge nurse de-identification.
                        
                        3. TRIAGE ACUITY CODES:
                           • Level 1 (Resuscitation) -> Immediate Physician intervention.
                           • Level 2 (Emergent) -> Under 15-minute physician exam.
                           • Level 3 (Urgent) -> Rapid diagnostic referral.
                    """.trimIndent()
                ),
                SheetPlan(
                    name = "Triage Overview",
                    isSummary = true,
                    kpiCards = listOf(
                        KpiCardSpec(label = "Total Active Patients", formula = "=COUNTA('Patient Registry'!A2:A50)", format = "number", value = "28"),
                        KpiCardSpec(label = "High Acuity (Level 1-2)", formula = "=COUNTIF('Patient Registry'!D2:D50, \"Emergent\")+COUNTIF('Patient Registry'!D2:D50, \"Immediate\")", format = "number", value = "6"),
                        KpiCardSpec(label = "Avg Intake Wait Time", formula = "=AVERAGE('Patient Registry'!F2:F50)", format = "number", value = "18.4 min"),
                        KpiCardSpec(label = "Beds Occupied %", formula = "=COUNTA('Patient Registry'!A2:A50)/35", format = "percent", value = "80.0%")
                    ),
                    charts = listOf(
                        ChartSpec(type = "pie", title = "Patients by Acuity Level", range = "Patient Registry!D2:D20"),
                        ChartSpec(type = "column", title = "Wait Times by Unit", range = "Patient Registry!E2:F20")
                    )
                ),
                SheetPlan(
                    name = "Patient Registry",
                    columns = listOf(
                        ColumnSpec("Patient ID", type = "text"),
                        ColumnSpec("Intake Time", type = "date"),
                        ColumnSpec("Assigned Department", type = "category", validation = listOf("Emergency", "ICU", "Cardiology", "Observation")),
                        ColumnSpec("Acuity Level", type = "status", validation = listOf("Immediate", "Emergent", "Urgent", "Standard")),
                        ColumnSpec("Primary Complaint", type = "text"),
                        ColumnSpec("Wait Time (Min)", type = "number"),
                        ColumnSpec("Attending MD", type = "text"),
                        ColumnSpec("Disposition", type = "status", validation = listOf("Admitted", "In Observation", "Discharged"))
                    ),
                    rows = listOf(
                        listOf("PT-2026-081", "2026-03-22 08:14", "Emergency", "Emergent", "Acute Chest Pain", "8", "Dr. Vance", "Admitted"),
                        listOf("PT-2026-082", "2026-03-22 08:30", "Emergency", "Standard", "Sprained Ankle", "25", "Dr. Gomez", "Discharged"),
                        listOf("PT-2026-083", "2026-03-22 09:05", "Cardiology", "Immediate", "Arrhythmia", "4", "Dr. Chen", "Admitted"),
                        listOf("PT-2026-084", "2026-03-22 09:40", "Observation", "Urgent", "Abdominal Pain", "18", "Dr. Vance", "In Observation")
                    ),
                    conditionalFormatting = listOf(
                        ConditionalRuleSpec("D2:D20", "cellValue == 'Immediate'", "lightRed"),
                        ConditionalRuleSpec("D2:D20", "cellValue == 'Emergent'", "yellow"),
                        ConditionalRuleSpec("H2:H20", "cellValue == 'Discharged'", "lightGreen")
                    )
                )
            )
        )
        return BlueprintEntity.fromWorkbookPlan(plan, category = "Healthcare", isPrebuilt = true)
    }

    private fun createSalesPipeline(): BlueprintEntity {
        val plan = WorkbookPlan(
            id = "prebuilt-sales-pipeline",
            title = "B2B Enterprise Sales Pipeline & Forecast",
            domain = DomainPreset.SALES.title,
            theme = "Sunset",
            blueprintSummary = BlueprintSummary(
                keyMetrics = listOf("Weighted Pipeline Value", "Win Rate %", "Total Active Opportunities", "Avg Sales Cycle"),
                categories = listOf("Sales", "Revenue", "Executive"),
                effectivenessScore = 96,
                notes = "Probability-weighted conversion funnel with automated deal size stage discounting and sales rep quotas."
            ),
            sheets = listOf(
                SheetPlan(
                    name = "Instructions",
                    isInstructions = true,
                    instructionsText = """
                        B2B ENTERPRISE PIPELINE MANAGEMENT WORKBOOK
                        
                        1. OVERVIEW:
                           • Use this sheet to track multi-stage sales opportunities and calculate quarterly forecasts.
                        
                        2. PROBABILITY STAGES:
                           • Discovery: 20%
                           • Demo / Eval: 40%
                           • Technical Validation: 60%
                           • Proposal / Procurement: 80%
                           • Closed Won: 100%
                        
                        3. AUTOMATED FORMULAS:
                           • Weighted Value = Deal Amount * Probability
                           • Do not overwrite column G ('Weighted Forecast').
                    """.trimIndent()
                ),
                SheetPlan(
                    name = "Pipeline Dashboard",
                    isSummary = true,
                    kpiCards = listOf(
                        KpiCardSpec(label = "Total Unweighted Pipeline", formula = "=SUM('Deals'!E2:E50)", format = "currency", value = "$3,450,000.00"),
                        KpiCardSpec(label = "Weighted Forecast", formula = "=SUM('Deals'!G2:G50)", format = "currency", value = "$1,980,000.00"),
                        KpiCardSpec(label = "Average Deal Size", formula = "=AVERAGE('Deals'!E2:E50)", format = "currency", value = "$115,000.00"),
                        KpiCardSpec(label = "Win Probability Avg", formula = "=AVERAGE('Deals'!F2:F50)", format = "percent", value = "57.4%")
                    ),
                    charts = listOf(
                        ChartSpec(type = "column", title = "Weighted Forecast by Stage", range = "Deals!D2:G20"),
                        ChartSpec(type = "pie", title = "Pipeline Share by Owner", range = "Deals!B2:E20")
                    )
                ),
                SheetPlan(
                    name = "Deals",
                    columns = listOf(
                        ColumnSpec("Opportunity Name", type = "text"),
                        ColumnSpec("Account Executive", type = "text"),
                        ColumnSpec("Target Close Date", type = "date"),
                        ColumnSpec("Sales Stage", type = "status", validation = listOf("Discovery", "Demo", "Technical Validation", "Proposal", "Closed Won", "Closed Lost")),
                        ColumnSpec("Deal Size ($)", type = "currency"),
                        ColumnSpec("Probability (%)", type = "percent"),
                        ColumnSpec("Weighted Forecast ($)", type = "currency", formula = "=E2*F2")
                    ),
                    rows = listOf(
                        listOf("Oracle Cloud Migration", "Marcus Brody", "2026-04-15", "Proposal", "250000.00", "0.80", "=E2*F2"),
                        listOf("Nike Omnichannel Portal", "Elena Rostova", "2026-04-30", "Technical Validation", "180000.00", "0.60", "=E3*F3"),
                        listOf("Delta Air Logistics ERP", "Marcus Brody", "2026-05-15", "Demo", "320000.00", "0.40", "=E4*F4"),
                        listOf("Pfizer Clinical Analytics", "Sarah Jenkins", "2026-04-10", "Closed Won", "450000.00", "1.00", "=E5*F5")
                    ),
                    conditionalFormatting = listOf(
                        ConditionalRuleSpec("D2:D20", "cellValue == 'Closed Won'", "lightGreen"),
                        ConditionalRuleSpec("D2:D20", "cellValue == 'Closed Lost'", "lightRed")
                    )
                )
            )
        )
        return BlueprintEntity.fromWorkbookPlan(plan, category = "Sales", isPrebuilt = true)
    }

    private fun createProjectTracker(): BlueprintEntity {
        val plan = WorkbookPlan(
            id = "prebuilt-project-tracker",
            title = "Agile Sprint Project & Deliverable Tracker",
            domain = DomainPreset.PROJECT.title,
            theme = "Charcoal",
            blueprintSummary = BlueprintSummary(
                keyMetrics = listOf("Sprint Completion %", "Total Story Points", "Blocked Deliverables", "Velocity (Points/Day)"),
                categories = listOf("Project Management", "Engineering", "Agile"),
                effectivenessScore = 95,
                notes = "Sprint delivery board with task duration calculations, priority tagging, and blocker tracking."
            ),
            sheets = listOf(
                SheetPlan(
                    name = "Instructions",
                    isInstructions = true,
                    instructionsText = """
                        AGILE SPRINT DELIVERY SPREADSHEET
                        
                        1. INSTRUCTIONS:
                           • Keep story point estimations consistent with the Fibonacci scale (1, 2, 3, 5, 8, 13).
                           • Update task statuses daily prior to the 09:30 AM standup.
                        
                        2. COMPLETION CALCULATIONS:
                           • Sprint Completion % is driven by closed task weight against committed story points.
                    """.trimIndent()
                ),
                SheetPlan(
                    name = "Sprint Dashboard",
                    isSummary = true,
                    kpiCards = listOf(
                        KpiCardSpec(label = "Total Story Points", formula = "=SUM('Task Backlog'!E2:E50)", format = "number", value = "84 pts"),
                        KpiCardSpec(label = "Completed Points", formula = "=SUMIF('Task Backlog'!F2:F50, \"Done\", 'Task Backlog'!E2:E50)", format = "number", value = "52 pts"),
                        KpiCardSpec(label = "Sprint Progress", formula = "='Sprint Dashboard'!B2/'Sprint Dashboard'!A2", format = "percent", value = "61.9%"),
                        KpiCardSpec(label = "Critical Blockers", formula = "=COUNTIF('Task Backlog'!F2:F50, \"Blocked\")", format = "number", value = "2")
                    ),
                    charts = listOf(
                        ChartSpec(type = "bar", title = "Story Points by Assignee", range = "Task Backlog!C2:E20"),
                        ChartSpec(type = "pie", title = "Task Status Breakdown", range = "Task Backlog!F2:F20")
                    )
                ),
                SheetPlan(
                    name = "Task Backlog",
                    columns = listOf(
                        ColumnSpec("Task Key", type = "text"),
                        ColumnSpec("Deliverable Title", type = "text"),
                        ColumnSpec("Owner", type = "text"),
                        ColumnSpec("Priority", type = "priority", validation = listOf("Critical", "High", "Medium", "Low")),
                        ColumnSpec("Story Points", type = "number"),
                        ColumnSpec("Status", type = "status", validation = listOf("Backlog", "In Progress", "Code Review", "Done", "Blocked")),
                        ColumnSpec("Due Date", type = "date")
                    ),
                    rows = listOf(
                        listOf("ENG-101", "Auth0 SSO Single Sign-On Integration", "David K.", "Critical", "8", "Done", "2026-03-20"),
                        listOf("ENG-102", "Stripe Metered Billing Webhook Hook", "Maya S.", "High", "5", "In Progress", "2026-03-24"),
                        listOf("ENG-103", "Kubernetes Pod Memory Leak Triage", "Alex R.", "Critical", "13", "Blocked", "2026-03-22"),
                        listOf("ENG-104", "Dark Mode UI Palette Accessibility Audit", "Chris P.", "Medium", "3", "Done", "2026-03-21")
                    ),
                    conditionalFormatting = listOf(
                        ConditionalRuleSpec("D2:D20", "cellValue == 'Critical'", "lightRed"),
                        ConditionalRuleSpec("F2:F20", "cellValue == 'Blocked'", "lightRed"),
                        ConditionalRuleSpec("F2:F20", "cellValue == 'Done'", "lightGreen")
                    )
                )
            )
        )
        return BlueprintEntity.fromWorkbookPlan(plan, category = "Project Management", isPrebuilt = true)
    }

    private fun createHrOperations(): BlueprintEntity {
        val plan = WorkbookPlan(
            id = "prebuilt-hr-operations",
            title = "Quarterly HR Headcount & Attendance Tracker",
            domain = DomainPreset.HR.title,
            theme = "Modern Blue",
            blueprintSummary = BlueprintSummary(
                keyMetrics = listOf("Total Headcount", "Monthly Payroll Run", "Attendance Rate %", "Open Requisitions"),
                categories = listOf("HR & Operations", "Personnel", "Finance"),
                effectivenessScore = 95,
                notes = "Departmental headcount audit with automated compensation aggregation and tenure tracking."
            ),
            sheets = listOf(
                SheetPlan(
                    name = "Instructions",
                    isInstructions = true,
                    instructionsText = """
                        HR PERSONNEL AUDIT & ATTENDANCE WORKBOOK
                        
                        1. CONFIDENTIALITY:
                           • Personnel compensation data is strictly confidential.
                        
                        2. FORMULAS & AGGREGATIONS:
                           • Monthly Payroll = Annual Base Salary / 12.
                           • Summaries are populated onto [HR Metrics] using dynamic cross-sheet =AVERAGE and =SUM formulas.
                    """.trimIndent()
                ),
                SheetPlan(
                    name = "HR Metrics",
                    isSummary = true,
                    kpiCards = listOf(
                        KpiCardSpec(label = "Total Full-Time Staff", formula = "=COUNTA('Employee Directory'!A2:A100)", format = "number", value = "42"),
                        KpiCardSpec(label = "Total Annual Payroll", formula = "=SUM('Employee Directory'!F2:F100)", format = "currency", value = "$4,250,000.00"),
                        KpiCardSpec(label = "Avg Compensation", formula = "=AVERAGE('Employee Directory'!F2:F100)", format = "currency", value = "$101,190.48"),
                        KpiCardSpec(label = "Avg Tenure (Years)", formula = "=AVERAGE('Employee Directory'!G2:G100)", format = "number", value = "3.2 yrs")
                    ),
                    charts = listOf(
                        ChartSpec(type = "pie", title = "Headcount by Department", range = "Employee Directory!C2:C30"),
                        ChartSpec(type = "column", title = "Payroll by Department", range = "Employee Directory!C2:F30")
                    )
                ),
                SheetPlan(
                    name = "Employee Directory",
                    columns = listOf(
                        ColumnSpec("Employee ID", type = "text"),
                        ColumnSpec("Full Name", type = "text"),
                        ColumnSpec("Department", type = "category", validation = listOf("Engineering", "Product", "Sales", "Marketing", "HR", "Finance")),
                        ColumnSpec("Role / Title", type = "text"),
                        ColumnSpec("Employment Status", type = "status", validation = listOf("Active", "On Leave", "Notice Period")),
                        ColumnSpec("Annual Salary ($)", type = "currency"),
                        ColumnSpec("Tenure (Years)", type = "number")
                    ),
                    rows = listOf(
                        listOf("EMP-001", "Jennifer Lopez-Gomez", "Engineering", "Staff Software Engineer", "Active", "165000.00", "4.2"),
                        listOf("EMP-002", "Robert MacMillan", "Sales", "Enterprise Account Exec", "Active", "120000.00", "2.8"),
                        listOf("EMP-003", "Amina Al-Mansoor", "Product", "Senior Product Manager", "Active", "145000.00", "3.5"),
                        listOf("EMP-004", "William Chen", "Finance", "Senior Financial Analyst", "Active", "110000.00", "1.9")
                    ),
                    conditionalFormatting = listOf(
                        ConditionalRuleSpec("E2:E30", "cellValue == 'On Leave'", "yellow"),
                        ConditionalRuleSpec("E2:E30", "cellValue == 'Active'", "lightGreen")
                    )
                )
            )
        )
        return BlueprintEntity.fromWorkbookPlan(plan, category = "HR & Operations", isPrebuilt = true)
    }

    private fun createPerstatAccountability(): BlueprintEntity {
        val plan = WorkbookPlan(
            id = "prebuilt-perstat-accountability",
            title = "PERSTAT Personnel Accountability & Readiness Tracker",
            domain = DomainPreset.MILITARY.title,
            theme = "Tactical Olive",
            blueprintSummary = BlueprintSummary(
                keyMetrics = listOf("Assigned Strength", "Present For Duty (PXD)", "Operational Readiness %", "Non-Deployable / Quarters"),
                categories = listOf("Military", "Accountability", "Operations"),
                effectivenessScore = 99,
                notes = "Tactical personnel status (PERSTAT) reporting workbook featuring 100% accountability roll call, duty status codes, and cross-sheet platoon rollups."
            ),
            sheets = listOf(
                SheetPlan(
                    name = "Instructions",
                    isInstructions = true,
                    instructionsText = """
                        UNIT PERSONNEL STATUS (PERSTAT) & READINESS DIRECTIVE
                        
                        1. PURPOSE & ACCOUNTABILITY CADENCE:
                           • Establish 100% positive accountability of all assigned and attached personnel.
                           • Submit roll-up to Battalion S1 daily at 0630 ZULU and 1830 ZULU.
                        
                        2. DUTY STATUS CODES:
                           • PXD: Present for Duty (Full operational capability).
                           • TDY: Temporary Duty / Schools / Tasking.
                           • LEV: Authorized Leave.
                           • QTR: Quarters / Medical Non-Deployable.
                           • AWL: Absent Without Leave (Immediate commander notification required).
                        
                        3. OPSEC & CUI GOVERNANCE:
                           • CONTROLLED UNCLASSIFIED INFORMATION (CUI) // REL TO USA.
                           • Contains Battle Roster IDs and military personnel rosters. Unauthorized disclosure is strictly prohibited under DoD Directive 5200.48.
                    """.trimIndent()
                ),
                SheetPlan(
                    name = "Readiness Overview",
                    isSummary = true,
                    kpiCards = listOf(
                        KpiCardSpec(label = "Total Assigned Strength", formula = "=COUNTA('Unit Roster'!A2:A100)", format = "number", value = "46"),
                        KpiCardSpec(label = "Present for Duty (PXD)", formula = "=COUNTIF('Unit Roster'!E2:E100, \"Present\")", format = "number", value = "41"),
                        KpiCardSpec(label = "Operational Readiness %", formula = "='Readiness Overview'!B2/'Readiness Overview'!A2", format = "percent", value = "89.1%"),
                        KpiCardSpec(label = "Non-Deployable / Quarters", formula = "=COUNTIF('Unit Roster'!E2:E100, \"Quarters\")+COUNTIF('Unit Roster'!E2:E100, \"Medical\")", format = "number", value = "3")
                    ),
                    charts = listOf(
                        ChartSpec(type = "column", title = "Personnel Distribution by Platoon", range = "Unit Roster!F2:F20"),
                        ChartSpec(type = "pie", title = "Duty Status Breakdown", range = "Unit Roster!E2:E20")
                    )
                ),
                SheetPlan(
                    name = "Unit Roster",
                    columns = listOf(
                        ColumnSpec("Battle Roster ID", type = "text"),
                        ColumnSpec("Rank / Grade", type = "category", validation = listOf("PVT", "PFC", "SPC", "CPL", "SGT", "SSG", "SFC", "1SG", "2LT", "1LT", "CPT")),
                        ColumnSpec("Last Name", type = "text"),
                        ColumnSpec("First Name", type = "text"),
                        ColumnSpec("Duty Status", type = "status", validation = listOf("Present", "Leave", "TDY", "Quarters", "Hospitalized", "Medical", "AWOL")),
                        ColumnSpec("Assigned Platoon", type = "category", validation = listOf("1st Platoon", "2nd Platoon", "3rd Platoon", "HQ Section", "Fires Section")),
                        ColumnSpec("Primary MOS", type = "text"),
                        ColumnSpec("Weapons Qual", type = "category", validation = listOf("Expert", "Sharpshooter", "Marksman", "Pending"))
                    ),
                    rows = listOf(
                        listOf("BTR-01", "CPT", "Miller", "Robert", "Present", "HQ Section", "11A", "Expert"),
                        listOf("BTR-02", "1SG", "Kowalski", "Stefan", "Present", "HQ Section", "11Z", "Expert"),
                        listOf("BTR-03", "1LT", "Harrison", "David", "Present", "1st Platoon", "11A", "Expert"),
                        listOf("BTR-04", "SFC", "Ramirez", "Carlos", "Present", "1st Platoon", "11B", "Expert"),
                        listOf("BTR-05", "SSG", "Washington", "Marcus", "Leave", "1st Platoon", "11B", "Sharpshooter"),
                        listOf("BTR-06", "SGT", "Torres", "Elena", "Present", "2nd Platoon", "68W", "Expert"),
                        listOf("BTR-07", "SPC", "O'Connor", "Liam", "Quarters", "2nd Platoon", "11B", "Marksman"),
                        listOf("BTR-08", "PFC", "Gomez", "Anthony", "Present", "3rd Platoon", "25U", "Sharpshooter")
                    ),
                    conditionalFormatting = listOf(
                        ConditionalRuleSpec("E2:E25", "cellValue == 'Present'", "lightGreen"),
                        ConditionalRuleSpec("E2:E25", "cellValue == 'Quarters'", "yellow"),
                        ConditionalRuleSpec("E2:E25", "cellValue == 'AWOL'", "lightRed")
                    )
                )
            )
        )
        return BlueprintEntity.fromWorkbookPlan(plan, category = "Military Operations", isPrebuilt = true)
    }

    private fun createSensitiveItemsPropertyBook(): BlueprintEntity {
        val plan = WorkbookPlan(
            id = "prebuilt-sensitive-items-property",
            title = "Sensitive Items Property Book & Serial Tracker",
            domain = DomainPreset.MILITARY.title,
            theme = "Charcoal",
            blueprintSummary = BlueprintSummary(
                keyMetrics = listOf("Total Sensitive Items", "Verified 100% Serialized", "Verification Rate %", "Open Discrepancies"),
                categories = listOf("Military", "Supply & Logistics", "Property Book"),
                effectivenessScore = 99,
                notes = "Strict serial-numbered sensitive items inventory (Weapons, Optics, COMSEC, NODs) with zero-tolerance loss tracking and vault audit logs."
            ),
            sheets = listOf(
                SheetPlan(
                    name = "Instructions",
                    isInstructions = true,
                    instructionsText = """
                        SENSITIVE ITEMS CYCLIC INVENTORY SOP (AR 710-2 / DA PAM 710-2-1)
                        
                        1. INVENTORY PROTOCOL:
                           • Perform visual, physical sight-count verification of serial numbers on 100% of sensitive items.
                           • Monthly commander sensitive items inventory must reconcile against Master Unit Property Record.
                        
                        2. CATEGORIES INCLUDED:
                           • Category I / II Small Arms (M4A1, M17, M249, M240B).
                           • Night Vision Devices (NVDs) & Laser Aiming Devices (AN/PVS-14, PEQ-15).
                           • Cryptographic & COMSEC Equipment (AN/PRC-152, SKLs).
                        
                        3. DISCREPANCY REPORTING:
                           • Any unaccounted serial number triggers immediate Arms Room lockdown and CCIR notification to Commander / Provost Marshal Office.
                    """.trimIndent()
                ),
                SheetPlan(
                    name = "Property Summary",
                    isSummary = true,
                    kpiCards = listOf(
                        KpiCardSpec(label = "Total Serialized Items", formula = "=COUNTA('Sensitive Items Ledger'!A2:A100)", format = "number", value = "68"),
                        KpiCardSpec(label = "Physically Verified", formula = "=COUNTIF('Sensitive Items Ledger'!F2:F100, \"Verified\")", format = "number", value = "68"),
                        KpiCardSpec(label = "Inventory Reconciliation", formula = "='Property Summary'!B2/'Property Summary'!A2", format = "percent", value = "100.0%"),
                        KpiCardSpec(label = "Discrepancy Count", formula = "=COUNTIF('Sensitive Items Ledger'!F2:F100, \"Discrepancy\")", format = "number", value = "0")
                    ),
                    charts = listOf(
                        ChartSpec(type = "pie", title = "Items by Equipment Category", range = "Sensitive Items Ledger!C2:C20"),
                        ChartSpec(type = "column", title = "Inventory by Storage Location", range = "Sensitive Items Ledger!G2:G20")
                    )
                ),
                SheetPlan(
                    name = "Sensitive Items Ledger",
                    columns = listOf(
                        ColumnSpec("Item Control #", type = "text"),
                        ColumnSpec("LIN / NSN", type = "text"),
                        ColumnSpec("Nomenclature", type = "text"),
                        ColumnSpec("Serial Number", type = "text"),
                        ColumnSpec("Sub-Hand Receipt Holder", type = "text"),
                        ColumnSpec("Inventory Status", type = "status", validation = listOf("Verified", "In Arms Room", "Field Dispatched", "Discrepancy", "Maintenance")),
                        ColumnSpec("Vault / Armory Rack", type = "category", validation = listOf("Rack A-01", "Rack A-02", "Optics Cage 1", "COMSEC Safe", "Field Pack")),
                        ColumnSpec("Last Verified Date", type = "date")
                    ),
                    rows = listOf(
                        listOf("SI-001", "LIN: W95825", "M4A1 5.56mm Carbine", "W942104", "1LT Harrison", "Verified", "Rack A-01", "2026-03-22"),
                        listOf("SI-002", "LIN: W95825", "M4A1 5.56mm Carbine", "W942105", "SFC Ramirez", "Verified", "Rack A-01", "2026-03-22"),
                        listOf("SI-003", "LIN: P98103", "AN/PVS-14 Monocular NVG", "NVG-88129", "SGT Torres", "Verified", "Optics Cage 1", "2026-03-22"),
                        listOf("SI-004", "LIN: P98103", "AN/PVS-14 Monocular NVG", "NVG-88130", "SPC O'Connor", "Verified", "Optics Cage 1", "2026-03-22"),
                        listOf("SI-005", "LIN: C05012", "AN/PRC-152A Handheld Radio", "RT-44109", "PFC Gomez", "Verified", "COMSEC Safe", "2026-03-22"),
                        listOf("SI-006", "LIN: L77102", "AN/PEQ-15 ATPIAL Laser", "AT-67210", "1LT Harrison", "Verified", "Optics Cage 1", "2026-03-22")
                    ),
                    conditionalFormatting = listOf(
                        ConditionalRuleSpec("F2:F20", "cellValue == 'Verified'", "lightGreen"),
                        ConditionalRuleSpec("F2:F20", "cellValue == 'Discrepancy'", "lightRed")
                    )
                )
            )
        )
        return BlueprintEntity.fromWorkbookPlan(plan, category = "Property & Logistics", isPrebuilt = true)
    }

    private fun createStaffDutyDeskLog(): BlueprintEntity {
        val plan = WorkbookPlan(
            id = "prebuilt-staff-duty-log",
            title = "Staff Duty Desk Log & Incident Register (DA Form 1594)",
            domain = DomainPreset.MILITARY.title,
            theme = "Tactical Olive",
            blueprintSummary = BlueprintSummary(
                keyMetrics = listOf("Total Shift Entries", "Critical Incidents (SIR)", "Red Cross Inquiries", "Security Checks Logged"),
                categories = listOf("Military", "Staff Duty", "Command"),
                effectivenessScore = 98,
                notes = "Operational Staff Duty Officer (SDO) and Staff Duty NCO (SDNCO) 24-hour log book conforming to DA Form 1594 specifications."
            ),
            sheets = listOf(
                SheetPlan(
                    name = "Instructions",
                    isInstructions = true,
                    instructionsText = """
                        STAFF DUTY OFFICER / NCO 24-HOUR LOG (DA FORM 1594) SOP
                        
                        1. GENERAL LOG REQUIREMENTS:
                           • Maintain a chronological, continuous log of all operational events, inspections, and communications.
                           • Shift spans from 0900 hours on date of assumption to 0900 hours following morning.
                        
                        2. MANDATORY CHECKPOINTS:
                           • Hourly physical checks of arms rooms, HQ exterior perimeters, and barracks areas.
                           • Record all incoming Red Cross emergency communications and immediately notify Chain of Command.
                        
                        3. SERIOUS INCIDENT REPORTS (SIR):
                           • Any vehicle incident, hospitalization, law enforcement contact, or unauthorized absence must be escalated immediately to Battalion Commander and CSM.
                    """.trimIndent()
                ),
                SheetPlan(
                    name = "Duty Overview",
                    isSummary = true,
                    kpiCards = listOf(
                        KpiCardSpec(label = "Total Shift Entries", formula = "=COUNTA('Duty Log'!A2:A100)", format = "number", value = "24"),
                        KpiCardSpec(label = "Security Checks Logged", formula = "=COUNTIF('Duty Log'!D2:D100, \"Security Check\")", format = "number", value = "12"),
                        KpiCardSpec(label = "Serious Incidents (SIR)", formula = "=COUNTIF('Duty Log'!E2:E100, \"Critical\")+COUNTIF('Duty Log'!E2:E100, \"High\")", format = "number", value = "1"),
                        KpiCardSpec(label = "Red Cross Emergencies", formula = "=COUNTIF('Duty Log'!D2:D100, \"Red Cross Message\")", format = "number", value = "1")
                    ),
                    charts = listOf(
                        ChartSpec(type = "pie", title = "Entries by Category", range = "Duty Log!D2:D20"),
                        ChartSpec(type = "column", title = "Incident Severity Breakdown", range = "Duty Log!E2:E20")
                    )
                ),
                SheetPlan(
                    name = "Duty Log",
                    columns = listOf(
                        ColumnSpec("Entry #", type = "text"),
                        ColumnSpec("Time (Local)", type = "date"),
                        ColumnSpec("Logged By", type = "text"),
                        ColumnSpec("Event Type", type = "category", validation = listOf("Shift Assumption", "Security Check", "Arms Room Check", "Red Cross Message", "Command Brief", "Incident / SIR", "Shift Relief")),
                        ColumnSpec("Severity Level", type = "status", validation = listOf("Routine", "Information", "Elevated", "High", "Critical")),
                        ColumnSpec("Incident Summary / Action Taken", type = "text"),
                        ColumnSpec("Chain of Command Notified", type = "text")
                    ),
                    rows = listOf(
                        listOf("001", "0900", "1LT Harrison", "Shift Assumption", "Routine", "Assumed Staff Duty responsibilities from outgoing shift. Binder, keys, and phone inventory complete.", "BDE TOC"),
                        listOf("002", "1015", "SSG Washington", "Arms Room Check", "Routine", "Physical inspection of Battalion Arms Room exterior doors and seal numbers. All seals intact.", "Arms Room NCOIC"),
                        listOf("003", "1200", "1LT Harrison", "Command Brief", "Information", "Battalion Commander visited desk for operational update.", "Battalion CDR"),
                        listOf("004", "1430", "SSG Washington", "Red Cross Message", "Elevated", "Received emergency Red Cross death in family notification for SPC Vance. Contacted 1SG Kowalski.", "1SG Kowalski / CDR"),
                        listOf("005", "1600", "SSG Washington", "Security Check", "Routine", "Barracks day room and exterior perimeter physical security walk. No discrepancies.", "None"),
                        listOf("006", "2100", "1LT Harrison", "Security Check", "Routine", "Evening physical lock-up of motor pool gates and HQ building.", "Staff Duty Files")
                    ),
                    conditionalFormatting = listOf(
                        ConditionalRuleSpec("E2:E20", "cellValue == 'Critical'", "lightRed"),
                        ConditionalRuleSpec("E2:E20", "cellValue == 'Elevated'", "yellow"),
                        ConditionalRuleSpec("E2:E20", "cellValue == 'Routine'", "lightGreen")
                    )
                )
            )
        )
        return BlueprintEntity.fromWorkbookPlan(plan, category = "Command & Staff Duty", isPrebuilt = true)
    }

    private fun createMissionExecutionMatrix(): BlueprintEntity {
        val plan = WorkbookPlan(
            id = "prebuilt-mission-execution-matrix",
            title = "Mission Execution Matrix & Operational Sync",
            domain = DomainPreset.MILITARY.title,
            theme = "Tactical Olive",
            blueprintSummary = BlueprintSummary(
                keyMetrics = listOf("Total Mission Objectives", "Objectives Complete", "Mission Progress %", "Active Phase"),
                categories = listOf("Military", "Operations", "Tactics"),
                effectivenessScore = 98,
                notes = "Synchronized mission execution matrix tracking operational phases, decision points, CCIR criteria, and comms frequencies."
            ),
            sheets = listOf(
                SheetPlan(
                    name = "Instructions",
                    isInstructions = true,
                    instructionsText = """
                        OPERATIONAL MISSION EXECUTION SYNC MATRIX
                        
                        1. CONCEPT OF THE OPERATION:
                           • Synchronize maneuver elements, fires, sustainment, and C2 milestones across all operational phases.
                           • H-Hour serves as the baseline execution time trigger.
                        
                        2. DECISION POINTS (DP) & CCIR:
                           • Trigger CCIR #1: Movement through Phase Line (PL) Amber.
                           • Trigger CCIR #2: Casualties in excess of 10% or key equipment failure.
                        
                        3. COMMUNICATIONS NETWORKS:
                           • Primary CMD Net: 42.10 MHz (FH/CT)
                           • Fires Net: 38.45 MHz (FH/CT)
                           • Medevac Net: 46.20 MHz (Single Channel)
                    """.trimIndent()
                ),
                SheetPlan(
                    name = "Mission Sync",
                    isSummary = true,
                    kpiCards = listOf(
                        KpiCardSpec(label = "Total Key Tasks", formula = "=COUNTA('Execution Matrix'!A2:A50)", format = "number", value = "18"),
                        KpiCardSpec(label = "Tasks Complete", formula = "=COUNTIF('Execution Matrix'!F2:F50, \"Complete\")", format = "number", value = "11"),
                        KpiCardSpec(label = "Phase Progress Rate %", formula = "='Mission Sync'!B2/'Mission Sync'!A2", format = "percent", value = "61.1%"),
                        KpiCardSpec(label = "Active Critical Phase", formula = "=\"Phase II - Movement\"", format = "text", value = "Phase II - Movement")
                    ),
                    charts = listOf(
                        ChartSpec(type = "pie", title = "Task Status Distribution", range = "Execution Matrix!F2:F20"),
                        ChartSpec(type = "column", title = "Task Load by Unit Element", range = "Execution Matrix!C2:C20")
                    )
                ),
                SheetPlan(
                    name = "Execution Matrix",
                    columns = listOf(
                        ColumnSpec("Phase Identifier", type = "category", validation = listOf("Phase I - Prep", "Phase II - Movement", "Phase III - Seize / Clear", "Phase IV - Consolidate")),
                        ColumnSpec("Timing Trigger", type = "text"),
                        ColumnSpec("Responsible Element", type = "category", validation = listOf("Alpha Co (Main)", "Bravo Co (Support)", "Scout Plt (Recon)", "Fires Section (Arty)", "Sustainment / Med")),
                        ColumnSpec("Assigned Mission Task", type = "text"),
                        ColumnSpec("Decision Point / CCIR Trigger", type = "text"),
                        ColumnSpec("Execution Status", type = "status", validation = listOf("Not Started", "In Progress", "Complete", "On Hold", "Delayed")),
                        ColumnSpec("Primary Comms Freq", type = "text")
                    ),
                    rows = listOf(
                        listOf("Phase I - Prep", "H-4 to H-1", "Alpha Co (Main)", "PCC/PCI inspections and weapons zero check", "100% sensitive items present", "Complete", "42.10 MHz"),
                        listOf("Phase I - Prep", "H-3 to H-1", "Scout Plt (Recon)", "Infiltrate observation posts along Axis Hammer", "Confirm enemy presence on OBJ Tiger", "Complete", "38.45 MHz"),
                        listOf("Phase II - Movement", "H-Hour", "Bravo Co (Support)", "Establish Support-by-Fire position SBF-1", "Suppress enemy outpost position", "In Progress", "42.10 MHz"),
                        listOf("Phase II - Movement", "H+30 Min", "Alpha Co (Main)", "Cross Phase Line (PL) Amber toward OBJ", "Breach outer obstacle wire", "In Progress", "42.10 MHz"),
                        listOf("Phase III - Seize / Clear", "H+1.5 Hours", "Alpha Co (Main)", "Clear and secure trench network on OBJ Tiger", "CCIR: Destroy key bunker structure", "Not Started", "42.10 MHz"),
                        listOf("Phase IV - Consolidate", "H+3 Hours", "Sustainment / Med", "Establish Casualty Collection Point (CCP-1)", "Evacuate Priority 1 casualties via Medevac", "Not Started", "46.20 MHz")
                    ),
                    conditionalFormatting = listOf(
                        ConditionalRuleSpec("F2:F20", "cellValue == 'Complete'", "lightGreen"),
                        ConditionalRuleSpec("F2:F20", "cellValue == 'In Progress'", "yellow"),
                        ConditionalRuleSpec("F2:F20", "cellValue == 'Delayed'", "lightRed")
                    )
                )
            )
        )
        return BlueprintEntity.fromWorkbookPlan(plan, category = "Tactical Operations", isPrebuilt = true)
    }
}
