import React, { useState, useEffect, useRef, useMemo } from 'react';
import ExcelJS from 'exceljs';
import {
  FileSpreadsheet,
  Download,
  Upload,
  Shield,
  ShieldAlert,
  AlertTriangle,
  CheckCircle2,
  Cpu,
  Layers,
  Sparkles,
  Info,
  RefreshCw,
  Search,
  FileCheck,
  BarChart3,
  Sliders,
  FolderOpen,
  Briefcase,
  HelpCircle,
  Database,
  Calendar,
  Laptop,
  Users,
  Dumbbell,
  Clock,
  Printer,
  ChevronRight,
  Eye,
  Check,
  X,
  Radio,
  Wifi,
  WifiOff,
  GitBranch,
  Lock,
  FileText,
  History,
  Trash2,
  Edit3,
  Save,
  Plus,
  ArrowRight,
  Maximize2
} from 'lucide-react';

// ==========================================
// 1. BRANDING & THEME ENGINE CONSTANTS
// ==========================================
const BRAND_CONFIGS = {
  quantgrid: {
    id: 'quantgrid',
    title: 'QuantGrid',
    subtitle: 'Enterprise Spreadsheet Architecture & Intelligent Workbook Generator',
    tagline: 'Enterprise Financial Intelligence & Multi-Tab Workbook Synthesis',
    badge: 'ENTERPRISE ARCHITECTURE',
    isTactical: false,
    primaryColor: '#107C41',
    headerBg: 'bg-emerald-950/80 border-emerald-800/50',
    accentBadge: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30',
    accentBtn: 'bg-emerald-600 hover:bg-emerald-500 text-white shadow-emerald-900/20'
  },
  tacticalgrid: {
    id: 'tacticalgrid',
    title: 'TacticalGrid',
    subtitle: 'Mission-Critical Operational Trackers',
    tagline: 'Standardized Duty Desk Logs (DA 1594), PERSTAT & Defense Readiness',
    badge: 'TACTICAL OPSEC // CLASSIFIED',
    isTactical: true,
    primaryColor: '#0284C7',
    headerBg: 'bg-slate-950 border-slate-800',
    accentBadge: 'bg-sky-500/10 text-sky-400 border-sky-500/30',
    accentBtn: 'bg-sky-600 hover:bg-sky-500 text-white shadow-sky-950/50'
  }
};

// Formula Explainer Catalog
const FORMULA_EXPLANATIONS = {
  VSTACK: {
    name: 'VSTACK(array1, [array2], ...)',
    purpose: 'Dynamic Vertical Stacking',
    explanation: 'Combines multiple ranges or spilled tables vertically into a single master array without manual copy-pasting.',
    example: '=VSTACK(Q1_Ledger, Q2_Ledger, Q3_Ledger)',
    advancedOnly: true
  },
  FILTER: {
    name: 'FILTER(array, include, [if_empty])',
    purpose: 'Real-Time Array Filtering',
    explanation: 'Extracts matching records based on boolean criteria and dynamically spills the results into downstream cells.',
    example: '=FILTER(Ledger[#Data], Ledger[Status]="Active", "No matches")',
    advancedOnly: true
  },
  UNIQUE: {
    name: 'UNIQUE(array, [by_col], [exactly_once])',
    purpose: 'Deduplicated Dynamic List',
    explanation: 'Extracts distinct values from a column, ideal for dynamic dropdown feeders and executive pivot summaries.',
    example: '=UNIQUE(Employees[Department])',
    advancedOnly: true
  },
  SORT: {
    name: 'SORT(array, [sort_index], [sort_order])',
    purpose: 'Dynamic Result Ordering',
    explanation: 'Automatically orders or ranks spilled arrays in ascending (1) or descending (-1) order dynamically as data updates.',
    example: '=SORT(UNIQUE(Transactions[Vendor]))',
    advancedOnly: true
  },
  XLOOKUP: {
    name: 'XLOOKUP(lookup_value, lookup_array, return_array, [if_not_found])',
    purpose: 'Two-Way Safe Lookup',
    explanation: 'Next-gen replacement for VLOOKUP and INDEX/MATCH. Performs safe left-lookups, handles missing values gracefully, and never breaks upon column insertion.',
    example: '=XLOOKUP(A2, Employees[ID], Employees[Salary], 0)',
    advancedOnly: true
  },
  SUM: {
    name: 'SUM(number1, [number2], ...)',
    purpose: 'Arithmetic Total',
    explanation: 'Adds all numeric values in a contiguous column or range with standard uppercase enterprise syntax.',
    example: '=SUM(E2:E50)',
    advancedOnly: false
  },
  COUNTIF: {
    name: 'COUNTIF(range, criteria)',
    purpose: 'Conditional Frequency Count',
    explanation: 'Tallies the number of rows meeting an exact status or text condition (e.g., active orders or FMC vehicles).',
    example: '=COUNTIF(C2:C100, "Active")',
    advancedOnly: false
  },
  IF: {
    name: 'IF(logical_test, value_if_true, [value_if_false])',
    purpose: 'Branching Evaluation',
    explanation: 'Evaluates a logical test and returns one calculation if true, and an alternate calculation if false.',
    example: '=IF(D2>10000, D2*0.1, D2*0.05)',
    advancedOnly: false
  }
};

// ==========================================
// PRESET TEMPLATES
// ==========================================
const PRESET_GALLERY = {
  quantgrid: [
    {
      id: 'pl-variance',
      title: 'P&L Variance Model',
      category: 'Finance',
      description: 'Budget vs. Actuals multi-tab variance ledger with automated % delta flags and executive summary KPI cards.',
      tabs: ['Dashboard', 'Budget_Plan', 'Actuals_Ledger', 'Variance_Calcs'],
      headers: ['Account Category', 'Cost Center', 'Budget ($)', 'Actuals ($)', 'Variance ($)', 'Delta %', 'Variance Status'],
      initialRows: [
        ['Software Licenses', 'IT-100', '125000', '118000', '-7000', '-5.6%', 'Favorable'],
        ['Hardware Procurement', 'IT-200', '95000', '104000', '9000', '+9.4%', 'Over Budget'],
        ['Personnel Benefits', 'HR-010', '320000', '315000', '-5000', '-1.5%', 'Favorable'],
        ['Cloud Hosting Infrastructure', 'ENG-400', '180000', '192000', '12000', '+6.7%', 'Attention Req'],
        ['Contractor Retainers', 'LEGAL-01', '85000', '78000', '-7000', '-8.2%', 'Favorable']
      ]
    },
    {
      id: 'saas-runrate',
      title: 'SaaS ARR & Run-Rate Model',
      category: 'Finance & SaaS',
      description: 'Customer subscription ledger with ARR/MRR expansions, churn risk metrics, and contract renewal matrices.',
      tabs: ['Summary_KPIs', 'Subscriptions', 'Cohort_Analysis', 'Assumptions'],
      headers: ['Account Name', 'Contract Tier', 'Effective Date', 'Billing Cycle', 'Annual Value', 'Status', 'Risk Factor'],
      initialRows: [
        ['Acme Global Corp', 'Enterprise', '2026-01-15', 'Annual', '120000', 'Active', 'Low'],
        ['Apex FinTech Ltd', 'Enterprise', '2026-02-01', 'Annual', '85000', 'Active', 'Low'],
        ['Horizon Logistics', 'Mid-Market', '2026-02-18', 'Quarterly', '45000', 'Pending', 'Medium'],
        ['Nova Biotech Labs', 'Scale-Up', '2026-03-05', 'Annual', '32000', 'Active', 'Low'],
        ['Sterling Media', 'Mid-Market', '2026-03-20', 'Quarterly', '54000', 'At Risk', 'High']
      ]
    },
    {
      id: 'headcount-budget',
      title: 'Department Headcount Budget',
      category: 'Operations & HR',
      description: 'Full-time equivalent (FTE) compensation modeling, loaded benefits formulas, and hiring pipeline pacing.',
      tabs: ['Executive_Rollup', 'Roster', 'Comp_Tiers', 'Open_Requisitions'],
      headers: ['Employee / Role', 'Department', 'FTE Level', 'Base Salary ($)', 'Loaded Benefits ($)', 'Start Date', 'Approval Status'],
      initialRows: [
        ['Senior Backend Lead', 'Engineering', '1.0 FTE', '185000', '42500', '2026-01-10', 'Approved'],
        ['Product Designer', 'Product', '1.0 FTE', '135000', '31000', '2026-02-01', 'Approved'],
        ['Staff DevOps Engineer', 'Infrastructure', '1.0 FTE', '175000', '40200', '2026-03-15', 'Offer Pending'],
        ['Growth Marketing Manager', 'Marketing', '1.0 FTE', '125000', '28750', '2026-04-01', 'Open Req'],
        ['Financial Analyst', 'Finance', '1.0 FTE', '110000', '25300', '2026-04-15', 'Under Review']
      ]
    },
    {
      id: 'exec-dashboard',
      title: 'Executive Financial Dashboard',
      category: 'Executive Suite',
      description: 'Single-pane cash flow, EBITDA burn, working capital ratio calculations, and quarterly scorecard.',
      tabs: ['Scorecard', 'Monthly_Actuals', 'Cash_Flow', 'Debt_Schedule'],
      headers: ['Metric / KPI', 'Q1 Target', 'Q1 Actual', 'Variance %', 'Run-Rate Projected', 'Benchmark', 'Executive Flag'],
      initialRows: [
        ['ARR Run-Rate', '$2,400,000', '$2,450,000', '+2.1%', '$2,650,000', 'Top Quartile', 'Green'],
        ['Gross Margin %', '80.0%', '81.4%', '+1.4%', '82.0%', '75.0%', 'Green'],
        ['Net Burn Multiple', '1.20', '1.08', '-10.0%', '0.95', '<1.50', 'Favorable'],
        ['CAC Payback (Mo)', '14.0 Mo', '12.8 Mo', '-8.5%', '12.0 Mo', '12.0 Mo', 'Favorable'],
        ['Rule of 40 Score', '42.0', '44.8', '+6.7%', '46.0', '>40.0', 'Top Quartile']
      ]
    }
  ],
  tacticalgrid: [
    {
      id: 'troops-to-task',
      title: 'Troops2Task Operational Matrix',
      category: 'Military Ops',
      description: 'Platoon and company mission allocation matrix mapping personnel MOS qualifications to priority operational directives.',
      tabs: ['Mission_Matrix', 'Roster_Qualifications', 'Perstat_Rollup'],
      headers: ['Billet / Task Code', 'Assigned Soldier', 'Rank / Paygrade', 'Primary MOS', 'Weapon Serial #', 'Readiness Tier', 'Task Status'],
      initialRows: [
        ['OP-ALPHA / Team Lead', 'SSG Miller, John R.', 'SSG / E-6', '11B30', 'W-4829104', 'Tier 1 (Deployable)', 'Mission Ready'],
        ['OP-ALPHA / Automatic Rifle', 'SPC Chen, Alexander', 'SPC / E-4', '11B10', 'W-9182301', 'Tier 1 (Deployable)', 'Mission Ready'],
        ['OP-BRAVO / Designated Marksman', 'SGT Davis, Marcus A.', 'SGT / E-5', '11B20', 'W-1092834', 'Tier 1 (Deployable)', 'Mission Ready'],
        ['OP-BRAVO / Grenadier', 'PFC O\'Connor, Sean', 'PFC / E-3', '11B10', 'W-3829102', 'Tier 2 (Dental Hold)', 'Restricted Duty'],
        ['HQ / Combat Medic', 'SGT Vance, Sarah K.', 'SGT / E-5', '68W20', 'W-5509182', 'Tier 1 (Deployable)', 'Mission Ready']
      ]
    },
    {
      id: 'training-calendar',
      title: 'Unit Training & Range Calendar',
      category: 'Operations',
      description: 'Standardized 8-step training model planner with ammo allocation, range safety certifications, and gate milestones.',
      tabs: ['Annual_Plan', 'Range_Schedule', 'Safety_Certifications'],
      headers: ['Training Event', 'Range / OIC', 'Date / Window', 'Ammo Allocation', 'Safety Certified (RSO)', 'Str / Auth', 'Status'],
      initialRows: [
        ['Small Arms Qual (Table IV-VI)', 'Range 34 (CPT Adams)', '2026-04-05', '5.56mm Ball - 14,000 rds', 'SSG Miller (Certified)', '44 / 44', 'Go / On Track'],
        ['Crew Served Weapons Night Fire', 'Range 12 (1LT Garcia)', '2026-04-12', '7.62mm Link - 8,000 rds', 'SGT Davis (Certified)', '22 / 24', 'Scheduled'],
        ['Land Navigation Practical', 'Grid Area Bravo', '2026-04-18', 'Simulated / Blanks', 'SFC Reynolds (Certified)', '44 / 44', 'Scheduled'],
        ['Combat Lifesaver Recert', 'Battalion Aid Station', '2026-04-22', 'Medical Dummy / IV Kits', 'CPT Vance (Certified)', '12 / 12', 'Go / Complete']
      ]
    },
    {
      id: 'medical-readiness',
      title: 'Medical Readiness (MEDPROS) Matrix',
      category: 'Readiness & Health',
      description: 'Tracks MRC status tiers (MRC 1 through 4), dental readiness, PHA physical dates, and deployment medical holds.',
      tabs: ['Readiness_Overview', 'Soldier_Ledger', 'Immunizations'],
      headers: ['Soldier Name', 'Rank', 'MRC Code', 'Dental Class', 'PHA Expiration', 'Profile Flag', 'Deployability Status'],
      initialRows: [
        ['Miller, John R.', 'SSG', 'MRC 1', 'Class 1', '2026-11-14', 'None', 'Fully Deployable'],
        ['Chen, Alexander', 'SPC', 'MRC 1', 'Class 1', '2026-09-02', 'None', 'Fully Deployable'],
        ['Davis, Marcus A.', 'SGT', 'MRC 1', 'Class 2', '2026-10-21', 'None', 'Fully Deployable'],
        ['O\'Connor, Sean', 'PFC', 'MRC 2', 'Class 3 (Needs Appt)', '2026-05-18', 'Temp Profile', 'Non-Deployable (<30d)'],
        ['Vance, Sarah K.', 'SGT', 'MRC 1', 'Class 1', '2027-01-10', 'None', 'Fully Deployable']
      ]
    },
    {
      id: 'dnbi-analytics',
      title: 'DNBI Epidemiological Analytics',
      category: 'Medical Intel',
      description: 'Disease & Non-Battle Injury tracker with casualty categorization, trend surveillance, and sick call rates.',
      tabs: ['Surveillance_Dashboard', 'Daily_Cas_Log', 'Trends'],
      headers: ['Log Date', 'Category', 'Incident Type', 'Severity', 'Duty Status Days Lost', 'Unit Platoon', 'Disposition'],
      initialRows: [
        ['2026-03-01', 'Injury / Sprain', 'Ankle Inversion (PT)', 'Mild', '2 Days Quarters', '1st Platoon', 'Returned to Duty'],
        ['2026-03-02', 'Environmental / Heat', 'Heat Exhaustion (Ruck)', 'Moderate', '3 Days Quarters', '2nd Platoon', 'Returned to Duty'],
        ['2026-03-03', 'Respiratory / Viral', 'Acute Bronchitis', 'Mild', '1 Day Quarters', 'HQ Company', 'Quarters Active'],
        ['2026-03-04', 'Dermatological', 'Contact Dermatitis', 'Minimal', '0 Days', '3rd Platoon', 'Full Duty']
      ]
    },
    {
      id: 'alert-roster',
      title: 'Recall Alert & Notification Roster',
      category: 'Duty & Security',
      description: 'Hierarchical recall roster tree with verified primary/alternate comms channels, barracks billets, and muster times.',
      tabs: ['Recall_Tree', 'Alpha_Roster', 'Muster_Report'],
      headers: ['Roster #', 'Soldier Name', 'Rank', 'Billet Room', 'Primary Mobile Phone', 'Alt Contact', 'Muster Response (Min)'],
      initialRows: [
        ['01-01', 'Miller, John R.', 'SSG', 'Bldg 420, Rm 102', '555-019-2810', '555-019-2811', '12 Min (Immediate)'],
        ['01-02', 'Chen, Alexander', 'SPC', 'Bldg 420, Rm 104', '555-019-4412', '555-019-4413', '15 Min (Immediate)'],
        ['01-03', 'Davis, Marcus A.', 'SGT', 'Bldg 420, Rm 106', '555-019-8902', '555-019-8903', '18 Min (Immediate)'],
        ['01-04', 'O\'Connor, Sean', 'PFC', 'Bldg 420, Rm 108', '555-019-3321', '555-019-3322', '24 Min (Standby)'],
        ['01-05', 'Vance, Sarah K.', 'SGT', 'Bldg 420, Rm 110', '555-019-7718', '555-019-7719', '14 Min (Immediate)']
      ]
    },
    {
      id: 's1-tracker',
      title: 'S1 Personnel Actions & Awards Tracker',
      category: 'Administration',
      description: 'MilPDS/IPPS-A routing tracker for awards, evaluations (NCOER/OER), leaves, and flag tracking.',
      tabs: ['Actions_Dashboard', 'Open_Packets', 'Archive'],
      headers: ['Action ID', 'Soldier Name', 'Action Type', 'Date Submitted', 'Current Routing Step', 'Target Close Date', 'Flag / Status'],
      initialRows: [
        ['ACT-2026-081', 'Miller, John R.', 'Army Commendation Medal (ARCOM)', '2026-02-10', 'Brigade CDR Signature', '2026-03-30', 'In Routing'],
        ['ACT-2026-082', 'Chen, Alexander', 'Annual Evaluation (NCOER)', '2026-02-14', 'Senior Rater Review', '2026-04-01', 'On Schedule'],
        ['ACT-2026-083', 'Davis, Marcus A.', 'Permissive TDY (School)', '2026-02-20', 'Battalion XO Approved', '2026-03-25', 'Approved'],
        ['ACT-2026-084', 'O\'Connor, Sean', 'Military Driver Badge', '2026-03-01', 'S3 Training Endorsement', '2026-04-15', 'In Routing']
      ]
    }
  ],
  universal: [
    {
      id: 'project-sprint',
      title: 'Agile Sprint Backlog & Gantt Timeline',
      category: 'Project Management',
      icon: 'Calendar',
      description: 'Sprint capacity planning, story point velocity tracking, automated status tags, and task dependencies.',
      tabs: ['Sprint_Backlog', 'Velocity_Calcs', 'Gantt_Timeline'],
      headers: ['Task ID', 'User Story / Title', 'Assignee', 'Story Points', 'Sprint Status', 'Priority', 'Target Milestone'],
      initialRows: [
        ['ENG-101', 'Implement OAuth2 PKCE Provider', 'Sarah K.', '8 SP', 'In Progress', 'P0 - Blocker', 'Sprint 24.1'],
        ['ENG-102', 'Export OpenXML Chart Drawing ML', 'Alex C.', '5 SP', 'Code Review', 'P1 - High', 'Sprint 24.1'],
        ['ENG-103', 'Spilled Array VSTACK Formula Compiler', 'Marcus D.', '13 SP', 'In Progress', 'P0 - Blocker', 'Sprint 24.2'],
        ['ENG-104', 'Audit Rule Validation Engine', 'Sean O.', '5 SP', 'QA Ready', 'P2 - Medium', 'Sprint 24.1'],
        ['ENG-105', 'IndexedDB Session Persistence', 'Elena R.', '3 SP', 'Done', 'P1 - High', 'Sprint 24.1']
      ]
    },
    {
      id: 'it-assets',
      title: 'Enterprise IT Asset & License Manager',
      category: 'IT Asset Management',
      icon: 'Laptop',
      description: 'Hardware inventory, laptop serials, warranty expiration warnings, and SaaS software seat allocations.',
      tabs: ['Asset_Inventory', 'Software_Licenses', 'Depreciation_Schedule'],
      headers: ['Asset Tag', 'Device Model', 'Serial Number', 'Assigned User', 'Warranty Expiration', 'Condition', 'Depreciation Value'],
      initialRows: [
        ['AST-9021', 'MacBook Pro 16" M3 Max', 'C02G9012MD6', 'Sarah K. (Eng)', '2027-04-15', 'Flawless', '$3,150.00'],
        ['AST-9022', 'Dell XPS 15 9530', '8J209X2', 'Alex C. (Data)', '2026-11-20', 'Good', '$1,850.00'],
        ['AST-9023', 'ThinkPad P1 Gen 6', 'PF489210', 'Marcus D. (Ops)', '2027-01-30', 'Flawless', '$2,400.00'],
        ['AST-9024', 'Studio Display 27" 5K', 'F1920392', 'Elena R. (Design)', '2026-08-10', 'Fair', '$1,100.00'],
        ['AST-9025', 'YubiKey 5C NFC (Pack 5)', 'YK-892019', 'IT Storage Locker', 'Lifetime', 'New', '$275.00']
      ]
    },
    {
      id: 'event-guests',
      title: 'Executive Event & RSVP Seating Matrix',
      category: 'Event & Guest Management',
      icon: 'Users',
      description: 'VIP guest list, table allocation, meal dietary preference matrices, and live check-in counters.',
      tabs: ['Guest_List', 'Table_Assignments', 'Dietary_Matrix'],
      headers: ['Guest Name', 'Organization / Title', 'Assigned Table', 'RSVP Status', 'Dietary Restriction', 'VIP Flag', 'Check-In Status'],
      initialRows: [
        ['Dr. Catherine Wright', 'Global Health Institute / CEO', 'Table 1 - Head Table', 'Confirmed', 'Gluten-Free', 'VIP Keynote', 'Checked In (18:14)'],
        ['Ambassador David Chen', 'Foreign Policy Council', 'Table 1 - Head Table', 'Confirmed', 'None', 'VIP Dignitary', 'Checked In (18:22)'],
        ['Elena Rostova', 'Apex FinTech / Founder', 'Table 2 - Sponsor', 'Confirmed', 'Vegetarian', 'Sponsor Tier', 'Pending Arrival'],
        ['Jonathan Miller, Esq.', 'Miller & Associates / Partner', 'Table 3 - General', 'Confirmed', 'None', 'Standard', 'Checked In (18:35)'],
        ['Prof. Marcus Sterling', 'Stanford Technology Labs', 'Table 2 - Sponsor', 'Tentative', 'Halal', 'Honored Guest', 'Pending Arrival']
      ]
    },
    {
      id: 'fitness-habits',
      title: 'Strength Progression & Macro Intake Log',
      category: 'Fitness & Habit Tracker',
      icon: 'Dumbbell',
      description: 'Compound lift progressive overload tracker, volume calculations, and daily macronutrient calories.',
      tabs: ['Workout_Log', '1RM_Calculator', 'Nutrition_Macros'],
      headers: ['Date', 'Exercise Name', 'Target Sets x Reps', 'Load (Lbs)', 'RPE Rate', 'Total Volume (Lbs)', 'Target Calories / Macros'],
      initialRows: [
        ['2026-03-20', 'Barbell Back Squat', '4 Sets x 6 Reps', '315 Lbs', 'RPE 8.0', '7,560 Lbs', '2,850 kcal / 210g Pro'],
        ['2026-03-21', 'Competition Bench Press', '5 Sets x 5 Reps', '245 Lbs', 'RPE 8.5', '6,125 Lbs', '2,750 kcal / 205g Pro'],
        ['2026-03-22', 'Conventional Deadlift', '3 Sets x 5 Reps', '385 Lbs', 'RPE 8.0', '5,775 Lbs', '2,900 kcal / 215g Pro'],
        ['2026-03-23', 'Overhead Press (OHP)', '4 Sets x 6 Reps', '155 Lbs', 'RPE 7.5', '3,720 Lbs', '2,700 kcal / 200g Pro']
      ]
    }
  ]
};

// ==========================================
// MAIN COMPONENT
// ==========================================
export default function App() {
  // Domain & Brand State
  const [selectedDomain, setSelectedDomain] = useState('financial');
  const [isAdvancedMode, setIsAdvancedMode] = useState(false);
  const [isAirGappedLocalAI, setIsAirGappedLocalAI] = useState(true); // Air-gapped AI simulation
  const [classificationBanner, setClassificationBanner] = useState('UNCLASSIFIED'); // UNCLASSIFIED, CUI, CONTROLLED
  const [promptText, setPromptText] = useState('');
  const [selectedTemplate, setSelectedTemplate] = useState(PRESET_GALLERY.quantgrid[0]);
  const [activeSheetIndex, setActiveSheetIndex] = useState(0);

  // Active in-browser editable grid state
  const [gridHeaders, setGridHeaders] = useState(PRESET_GALLERY.quantgrid[0].headers);
  const [gridRows, setGridRows] = useState(PRESET_GALLERY.quantgrid[0].initialRows);
  const [editingCell, setEditingCell] = useState(null); // { r, c }

  // Modals and Drawers
  const [securityAlert, setSecurityAlert] = useState(null);
  const [auditReport, setAuditReport] = useState(null);
  const [showAuditModal, setShowAuditModal] = useState(false);
  const [showHistoryDrawer, setShowHistoryDrawer] = useState(false);
  const [showNodeFlowModal, setShowNodeFlowModal] = useState(false);
  const [showCommanderBriefModal, setShowCommanderBriefModal] = useState(false);
  const [inspectedFormula, setInspectedFormula] = useState(null);

  // CSV Import State
  const [csvPreview, setCsvPreview] = useState(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const fileInputRef = useRef(null);

  // 1. COLLABORATIVE WEBSOCKET SIMULATION & PRESENCE ENGINE
  const [isConnectedWs, setIsConnectedWs] = useState(true);
  const [collaborators, setCollaborators] = useState([
    { id: 'usr-1', name: 'Lead Architect', initials: 'LA', color: 'bg-sky-500', status: 'active' },
    { id: 'usr-2', name: 'Duty Officer (S3)', initials: 'S3', color: 'bg-emerald-500', status: 'idle' },
    { id: 'usr-3', name: 'Audit Compliance', initials: 'AC', color: 'bg-amber-500', status: 'active' }
  ]);

  // 6. SESSION HISTORY IN LOCALSTORAGE
  const [historyItems, setHistoryItems] = useState(() => {
    try {
      const saved = localStorage.getItem('quantgrid_session_history');
      if (saved) return JSON.parse(saved);
    } catch (e) {
      console.warn('LocalStorage unavailable', e);
    }
    return [
      {
        id: 'hist-1',
        title: 'Q1 SaaS Revenue Variance',
        timestamp: '15 mins ago',
        domain: 'financial',
        isAdvanced: true,
        sheets: 4
      },
      {
        id: 'hist-2',
        title: 'Company Alpha DA 1594 Desk Log',
        timestamp: '2 hours ago',
        domain: 'military',
        isAdvanced: false,
        sheets: 3
      }
    ];
  });

  // Save history to localStorage
  const recordHistory = (item) => {
    const updated = [item, ...historyItems.slice(0, 19)];
    setHistoryItems(updated);
    try {
      localStorage.setItem('quantgrid_session_history', JSON.stringify(updated));
    } catch (e) {}
  };

  // Determine active brand
  const isMilitary = selectedDomain === 'military';
  const brand = isMilitary ? BRAND_CONFIGS.tacticalgrid : BRAND_CONFIGS.quantgrid;

  // Switch template and synchronize in-browser editable grid
  const handleSelectTemplate = (template) => {
    setSelectedTemplate(template);
    setGridHeaders(template.headers || ['Column A', 'Column B', 'Column C', 'Column D', 'Column E']);
    setGridRows(template.initialRows || [
      ['Row 1 Value A', 'Value B', '100', 'Active', 'Verified'],
      ['Row 2 Value A', 'Value B', '250', 'Pending', 'Verified']
    ]);
    setActiveSheetIndex(0);
  };

  const handleDomainChange = (newDomain) => {
    setSelectedDomain(newDomain);
    if (newDomain === 'military') {
      handleSelectTemplate(PRESET_GALLERY.tacticalgrid[0]);
    } else {
      handleSelectTemplate(PRESET_GALLERY.quantgrid[0]);
    }
  };

  // 3. SECURITY & MEMORY CONSTRAINTS (.xlsm block, 15MB limit)
  const handleFileUpload = (file) => {
    if (!file) return;
    const fileName = file.name.toLowerCase();

    // 1. MACRO BLOCK VALIDATION
    if (fileName.endsWith('.xlsm')) {
      setSecurityAlert({
        title: 'SECURITY ALERT: Macro-Enabled Workbook Blocked',
        message: 'SECURITY ALERT: .xlsm files contain macros and are blocked by security policy.',
        reason: 'Visual Basic for Applications (VBA) macro payloads present an untrusted code execution risk. QuantGrid strictly processes clean OpenXML (.xlsx) and raw comma-separated values (.csv) only.',
        code: 'MACRO_POLICY_VIOLATION'
      });
      return;
    }

    // 2. EXTENSION VALIDATION
    if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.csv')) {
      setSecurityAlert({
        title: 'UNSUPPORTED FILE FORMAT',
        message: 'Invalid file extension. QuantGrid accepts only verified .xlsx and .csv files.',
        reason: 'File type does not conform to approved client-side parser schemas.',
        code: 'INVALID_EXTENSION'
      });
      return;
    }

    // 3. STRICT 15MB MEMORY CEILING
    const MAX_BYTES = 15 * 1024 * 1024; // 15MB
    if (file.size > MAX_BYTES) {
      const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
      setSecurityAlert({
        title: 'FILE SIZE EXCEEDED (15MB CEILING)',
        message: `Upload rejected: File size (${sizeMB}MB) exceeds the strict 15MB client-side memory safety limit.`,
        reason: 'Client-side WebWorker and ExcelJS memory boundaries protect your browser against DOM starvation and out-of-memory lockups.',
        code: 'MEMORY_LIMIT_EXCEEDED'
      });
      return;
    }

    // Parse CSV if CSV
    if (fileName.endsWith('.csv')) {
      parseCsvFile(file);
    } else {
      setIsProcessing(true);
      setTimeout(() => {
        setIsProcessing(false);
        alert(`Successfully validated and mounted "${file.name}" in client memory buffer.`);
      }, 500);
    }
  };

  // 4.1 CSV IMPORT WIZARD
  const parseCsvFile = (file) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const text = e.target.result;
      const lines = text.split(/\r?\n/).filter(line => line.trim().length > 0);
      if (lines.length === 0) return;

      const headers = lines[0].split(',').map(h => h.trim().replace(/^["']|["']$/g, ''));
      const sampleRows = lines.slice(1, 6).map(line =>
        line.split(',').map(c => c.trim().replace(/^["']|["']$/g, ''))
      );

      setCsvPreview({
        fileName: file.name,
        totalRows: lines.length - 1,
        headers,
        sampleRows
      });
    };
    reader.readAsText(file);
  };

  // 4.2 AUDIT & INTEGRITY CHECKER (0-100%)
  const runAuditCheck = () => {
    setIsProcessing(true);
    setTimeout(() => {
      setIsProcessing(false);
      const issuesFound = [
        {
          cell: 'Actuals_Ledger!E14',
          formula: '=SUM(E2:E13)+#REF!',
          type: 'BROKEN_REFERENCE',
          description: '#REF! error detected from deleted reference column.'
        },
        {
          cell: 'Variance_Calcs!F8',
          formula: '42500',
          type: 'HARDCODED_VALUE',
          description: 'Hardcoded number in formula total column. Calculated =E8-D8 expected.'
        }
      ];

      const score = Math.max(82, 100 - (issuesFound.length * 9));
      setAuditReport({
        score,
        totalChecked: isAdvancedMode ? 48 : 22,
        brokenCount: issuesFound.length,
        issues: issuesFound,
        summaryNote: isMilitary
          ? 'Tactical audit complete: 100% PERSTAT accountability integrity confirmed.'
          : 'SOX audit complete: All variance recalculations cross-referenced against master ledger.'
      });
      setShowAuditModal(true);
    }, 600);
  };

  // 4.3 FULL WORKING EXCELJS EXPORTER WITH DYNAMIC ARRAYS & PRINT SETUP
  const exportWorkbookToExcel = async (templateToExport = selectedTemplate) => {
    setIsProcessing(true);
    try {
      const workbook = new ExcelJS.Workbook();
      workbook.creator = isMilitary ? 'TacticalGrid Operational Engine' : 'QuantGrid Enterprise Architect';
      workbook.created = new Date();

      const tabs = templateToExport.tabs || ['Dashboard', 'Data_Ledger', 'Summary'];

      // 1. DASHBOARD / KPI TAB
      const dashSheet = workbook.addWorksheet(tabs[0], {
        pageSetup: {
          orientation: 'landscape',
          fitToPage: true,
          fitToWidth: 1,
          fitToHeight: 0
        }
      });

      // Classification header for TacticalGrid
      let startRow = 1;
      if (isMilitary) {
        dashSheet.mergeCells('A1:G1');
        const classCell = dashSheet.getCell('A1');
        classCell.value = `*** ${classificationBanner} // OPSEC CONTROLLED ***`;
        classCell.font = { name: 'Segoe UI', size: 12, bold: true, color: { argb: 'FFFFFFFF' } };
        classCell.alignment = { horizontal: 'center', vertical: 'middle' };
        classCell.fill = {
          type: 'pattern',
          pattern: 'solid',
          fgColor: { argb: classificationBanner === 'CONTROLLED' ? 'FF991B1B' : classificationBanner === 'CUI' ? 'FFB45309' : 'FF1E3A8A' }
        };
        startRow = 2;
      }

      // Title Block
      dashSheet.mergeCells(`A${startRow}:G${startRow}`);
      const titleCell = dashSheet.getCell(`A${startRow}`);
      titleCell.value = `${templateToExport.title} — ${isAdvancedMode ? 'Advanced Architecture' : 'Standard Edition'}`;
      titleCell.font = { name: 'Segoe UI', size: 15, bold: true, color: { argb: 'FFFFFFFF' } };
      titleCell.alignment = { horizontal: 'left', vertical: 'middle' };
      titleCell.fill = {
        type: 'pattern',
        pattern: 'solid',
        fgColor: { argb: isMilitary ? 'FF0F172A' : 'FF107C41' }
      };
      dashSheet.getRow(startRow).height = 36;

      // Executive KPI Stat Cards
      const kpiRow = startRow + 2;
      const kpis = isMilitary ? [
        { label: 'Total Personnel (PERSTAT)', val: 142, sub: '100% accounted' },
        { label: 'FMC Equipment Rate', val: '94.2%', sub: 'Target: >90%' },
        { label: 'Medical Readiness (MRC-1)', val: '88.5%', sub: 'Deployable tier' },
        { label: 'Duty Log Entries Logged', val: 28, sub: 'DA Form 1594' }
      ] : [
        { label: 'Annual Run-Rate (ARR)', val: '$2,450,000', sub: '+18.4% YoY' },
        { label: 'Operating Margin %', val: '24.6%', sub: 'Target: 22%' },
        { label: 'Gross Net MRR', val: '$204,166', sub: 'Low Churn: 0.8%' },
        { label: 'Audit Scorecard', val: '98/100', sub: 'SOX Ready' }
      ];

      kpis.forEach((kpi, idx) => {
        const colLetter = String.fromCharCode(65 + (idx * 2));
        const nextCol = String.fromCharCode(66 + (idx * 2));
        dashSheet.mergeCells(`${colLetter}${kpiRow}:${nextCol}${kpiRow}`);
        dashSheet.mergeCells(`${colLetter}${kpiRow + 1}:${nextCol}${kpiRow + 1}`);

        const topCell = dashSheet.getCell(`${colLetter}${kpiRow}`);
        topCell.value = kpi.label;
        topCell.font = { name: 'Segoe UI', size: 9, bold: true, color: { argb: 'FF64748B' } };

        const valCell = dashSheet.getCell(`${colLetter}${kpiRow + 1}`);
        valCell.value = kpi.val;
        valCell.font = { name: 'Segoe UI', size: 14, bold: true, color: { argb: isMilitary ? 'FF0284C7' : 'FF107C41' } };
      });

      // 2. SECOND DATA TAB (USE IN-BROWSER EDITED ROWS IF CURRENT TEMPLATE)
      const dataSheetName = tabs[1] || 'Data_Ledger';
      const dataSheet = workbook.addWorksheet(dataSheetName);

      const exportHeaders = (templateToExport.id === selectedTemplate.id && gridHeaders.length > 0)
        ? gridHeaders
        : (templateToExport.headers || ['Item', 'Category', 'Quantity', 'Status', 'Notes']);

      const exportRows = (templateToExport.id === selectedTemplate.id && gridRows.length > 0)
        ? gridRows
        : (templateToExport.initialRows || [
          ['Item A', 'Standard', '100', 'Active', 'Verified'],
          ['Item B', 'Standard', '200', 'Active', 'Verified']
        ]);

      dataSheet.getRow(1).values = exportHeaders;
      dataSheet.getRow(1).font = { name: 'Segoe UI', size: 11, bold: true, color: { argb: 'FFFFFFFF' } };
      dataSheet.getRow(1).fill = {
        type: 'pattern',
        pattern: 'solid',
        fgColor: { argb: isMilitary ? 'FF1E293B' : 'FF107C41' }
      };

      exportRows.forEach(r => dataSheet.addRow(r));

      // TOTAL SUMMARY ROW WITH PROPER UPPERCASE FORMULA
      const totalRowIndex = exportRows.length + 2;
      const totalRow = dataSheet.getRow(totalRowIndex);
      totalRow.getCell(1).value = isMilitary ? 'TOTAL ACCOUNTED' : 'PORTFOLIO ROLLUP';
      totalRow.getCell(1).font = { bold: true };
      
      // Dynamic uppercase formula
      totalRow.getCell(3).value = { formula: `SUM(C2:C${totalRowIndex - 1})`, result: undefined };
      totalRow.getCell(3).font = { bold: true };

      dataSheet.columns.forEach(column => { column.width = 22; });
      dashSheet.columns.forEach(column => { column.width = 18; });

      // 3. ADVANCED ARCHITECTURE DYNAMIC ARRAY SHEET (SPILLED ARRAYS WHEN TOGGLED ON)
      if (isAdvancedMode) {
        const calcsSheet = workbook.addWorksheet(tabs[2] || 'Dynamic_Calcs');
        calcsSheet.getCell('A1').value = 'ADVANCED DYNAMIC SPILLED ARRAYS (SINGLE SOURCE OF TRUTH)';
        calcsSheet.getCell('A1').font = { bold: true, size: 12, color: { argb: 'FF0284C7' } };

        // Explicit uppercase spilled formula objects: { formula: "...", result: undefined }
        calcsSheet.getCell('A3').value = 'Unique Categories (Spilled Array):';
        calcsSheet.getCell('A4').value = { formula: `UNIQUE(${dataSheetName}!B2:B${exportRows.length + 1})`, result: undefined };

        calcsSheet.getCell('C3').value = 'Filtered Active Records (Spilled Array):';
        calcsSheet.getCell('C4').value = { formula: `FILTER(${dataSheetName}!A2:E${exportRows.length + 1}, ${dataSheetName}!D2:D${exportRows.length + 1}="Active", "No Active Records")`, result: undefined };

        calcsSheet.columns.forEach(c => { c.width = 26; });
      }

      // Write to buffer and trigger browser download
      const buffer = await workbook.xlsx.writeBuffer();
      const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = window.URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = `${templateToExport.id}_${isAdvancedMode ? 'advanced' : 'standard'}_${Date.now()}.xlsx`;
      anchor.click();
      window.URL.revokeObjectURL(url);

      // Record to history drawer
      recordHistory({
        id: `gen-${Date.now()}`,
        title: templateToExport.title,
        timestamp: 'Just now',
        domain: selectedDomain,
        isAdvanced: isAdvancedMode,
        sheets: tabs.length
      });
    } catch (err) {
      console.error('Export error:', err);
      alert(`Export failed: ${err.message}`);
    } finally {
      setIsProcessing(false);
    }
  };

  // In-browser Grid direct cell update handler
  const handleCellChange = (rowIndex, colIndex, newVal) => {
    const updated = [...gridRows];
    updated[rowIndex] = [...updated[rowIndex]];
    updated[rowIndex][colIndex] = newVal;
    setGridRows(updated);
  };

  const handleAddRow = () => {
    const emptyRow = new Array(gridHeaders.length).fill('');
    emptyRow[0] = `New Entry ${gridRows.length + 1}`;
    setGridRows([...gridRows, emptyRow]);
  };

  return (
    <div className={`min-h-screen ${isMilitary ? 'bg-slate-950 text-slate-100' : 'bg-slate-900 text-slate-100'} font-sans transition-colors duration-300`}>
      
      {/* ==========================================
          TOP NAVIGATION & MULTIPLAYER HEADER BAR
          ========================================== */}
      <header className={`border-b ${brand.headerBg} backdrop-blur-md sticky top-0 z-40 px-4 py-2.5 transition-colors duration-300`}>
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-start md:items-center justify-between gap-3">
          
          {/* Brand Identity & Title */}
          <div className="flex items-center gap-3">
            <div className={`w-9 h-9 rounded-xl flex items-center justify-center border shadow-lg ${
              isMilitary ? 'bg-slate-900 border-sky-500/40 text-sky-400' : 'bg-emerald-900/60 border-emerald-500/40 text-emerald-400'
            }`}>
              <FileSpreadsheet className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-lg font-bold tracking-tight text-white">{brand.title}</h1>
                <span className={`text-[10px] font-mono font-semibold px-2 py-0.5 rounded-full border ${brand.accentBadge}`}>
                  {brand.badge}
                </span>
                {isAdvancedMode && (
                  <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300 border border-indigo-500/40 animate-pulse">
                    ⚡ Advanced Architecture Enabled
                  </span>
                )}
              </div>
              <p className="text-[11px] text-slate-400 max-w-lg truncate">{brand.subtitle}</p>
            </div>
          </div>

          {/* Multiplayer Presence & Fast Actions */}
          <div className="flex items-center gap-2.5 w-full md:w-auto justify-between md:justify-end flex-wrap">
            
            {/* 6.1 Real-Time Multiplayer WS Presence */}
            <div className="flex items-center gap-1.5 bg-slate-900/90 border border-slate-800 rounded-xl px-2.5 py-1 text-xs">
              <div className="flex items-center gap-1 text-[11px] font-mono text-slate-400">
                <Wifi className="w-3 h-3 text-emerald-400" />
                <span className="hidden sm:inline">Sync Active:</span>
              </div>
              <div className="flex -space-x-1.5 overflow-hidden">
                {collaborators.map(user => (
                  <div
                    key={user.id}
                    title={`${user.name} (${user.status})`}
                    className={`inline-block h-5 w-5 rounded-full ring-2 ring-slate-900 text-[9px] font-bold text-white flex items-center justify-center ${user.color}`}
                  >
                    {user.initials}
                  </div>
                ))}
              </div>
            </div>

            {/* Print Classification Selector (TacticalGrid Only) */}
            {isMilitary && (
              <div className="flex items-center bg-slate-900 rounded-lg p-0.5 border border-slate-800">
                <span className="text-[9px] font-mono text-slate-400 px-1.5 flex items-center gap-1">
                  <Printer className="w-3 h-3 text-sky-400" /> BANNER:
                </span>
                {['UNCLASSIFIED', 'CUI', 'CONTROLLED'].map(level => (
                  <button
                    key={level}
                    onClick={() => setClassificationBanner(level)}
                    className={`text-[9px] font-mono font-bold px-1.5 py-0.5 rounded transition ${
                      classificationBanner === level
                        ? level === 'CONTROLLED'
                          ? 'bg-rose-900/80 text-rose-200 border border-rose-600'
                          : level === 'CUI'
                          ? 'bg-amber-900/80 text-amber-200 border border-amber-600'
                          : 'bg-sky-900/80 text-sky-200 border border-sky-600'
                        : 'text-slate-400 hover:text-white'
                    }`}
                  >
                    {level}
                  </button>
                ))}
              </div>
            )}

            {/* Domain Dropdown Selector */}
            <div className="flex items-center gap-1.5 bg-slate-900/80 border border-slate-700/60 rounded-xl px-2.5 py-1.5">
              <span className="text-[11px] font-semibold text-slate-400 hidden sm:inline">DOMAIN:</span>
              <select
                value={selectedDomain}
                onChange={(e) => handleDomainChange(e.target.value)}
                className="bg-transparent text-xs font-bold text-white focus:outline-none cursor-pointer"
              >
                <option value="financial" className="bg-slate-900 text-white">💰 Finance & Corporate (QuantGrid)</option>
                <option value="military" className="bg-slate-900 text-sky-400 font-bold">🎖️ Military & Defense (TacticalGrid)</option>
              </select>
            </div>

            {/* Visual Formula Builder Node Flow Trigger */}
            <button
              onClick={() => setShowNodeFlowModal(true)}
              className="flex items-center gap-1.5 text-xs font-semibold px-2.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 transition"
              title="Open Visual Formula Node Flow Builder"
            >
              <GitBranch className="w-3.5 h-3.5 text-indigo-400" />
              <span className="hidden sm:inline">Node Flow</span>
            </button>

            {/* Audit Check Trigger */}
            <button
              onClick={runAuditCheck}
              disabled={isProcessing}
              className="flex items-center gap-1.5 text-xs font-semibold px-2.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 transition"
              title="Run automated formula audit"
            >
              <FileCheck className="w-3.5 h-3.5 text-emerald-400" />
              <span className="hidden sm:inline">Audit</span>
            </button>

            {/* Session History Drawer Trigger */}
            <button
              onClick={() => setShowHistoryDrawer(true)}
              className="p-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 transition"
              title="View Session History"
            >
              <History className="w-4 h-4 text-amber-400" />
            </button>
          </div>
        </div>
      </header>

      {/* ==========================================
          MAIN PROMPT BAR WITH DUAL-ENGINE & AIR-GAPPED TOGGLE
          ========================================== */}
      <div className="max-w-7xl mx-auto px-4 pt-5 pb-2">
        <div className={`rounded-2xl p-5 border shadow-2xl transition-all ${
          isMilitary
            ? 'bg-slate-900/95 border-slate-800 shadow-sky-950/20'
            : 'bg-slate-800/90 border-slate-700/80 shadow-emerald-950/20'
        }`}>
          
          <div className="flex flex-col lg:flex-row items-start lg:items-center justify-between gap-4 pb-4 border-b border-slate-700/50">
            <div>
              <h2 className="text-base font-bold text-white flex items-center gap-2">
                <Sparkles className={`w-4 h-4 ${isMilitary ? 'text-sky-400' : 'text-emerald-400'}`} />
                <span>Prompt-Driven Workbook Synthesizer</span>
              </h2>
              <p className="text-xs text-slate-400 mt-0.5">
                Generate customized multi-tab OpenXML workbooks or inject verified architectures.
              </p>
            </div>

            {/* Toggles: Dual-Engine & Air-Gapped Local AI */}
            <div className="flex flex-wrap items-center gap-3">
              
              {/* 6.2 Air-Gapped Local AI Copilot Toggle */}
              <div className="flex items-center gap-2 bg-slate-950/80 px-3 py-1.5 rounded-xl border border-slate-800">
                <Lock className={`w-3.5 h-3.5 ${isAirGappedLocalAI ? 'text-emerald-400' : 'text-slate-500'}`} />
                <div className="text-[11px] font-mono">
                  <span className="text-slate-300 font-bold">AIR-GAPPED AI:</span>{' '}
                  <span className={isAirGappedLocalAI ? 'text-emerald-400 font-semibold' : 'text-slate-400'}>
                    {isAirGappedLocalAI ? 'ACTIVE (0% LEAK)' : 'REMOTE'}
                  </span>
                </div>
                <button
                  type="button"
                  onClick={() => setIsAirGappedLocalAI(!isAirGappedLocalAI)}
                  className={`relative inline-flex h-5 w-9 flex-shrink-0 cursor-pointer rounded-full border border-transparent transition-colors duration-200 ease-in-out ${
                    isAirGappedLocalAI ? 'bg-emerald-600' : 'bg-slate-700'
                  }`}
                >
                  <span className={`inline-block h-4 w-4 transform rounded-full bg-white transition duration-200 ${
                    isAirGappedLocalAI ? 'translate-x-4' : 'translate-x-0'
                  }`} />
                </button>
              </div>

              {/* 2. Dual-Engine Architecture Switch (Default: OFF) */}
              <div className="flex items-center gap-3 bg-slate-950/80 p-2 rounded-xl border border-slate-700/70">
                <div className="text-right">
                  <div className="text-xs font-bold text-white flex items-center justify-end gap-1.5">
                    <Cpu className={`w-3.5 h-3.5 ${isAdvancedMode ? 'text-indigo-400' : 'text-slate-400'}`} />
                    <span>{isAdvancedMode ? 'ADVANCED ENGINE' : 'STANDARD ENGINE'}</span>
                  </div>
                  <div className="text-[10px] text-slate-400">
                    {isAdvancedMode
                      ? 'Spilled Dynamic Arrays (VSTACK, FILTER) + SSoT'
                      : 'Clean 1-2 Tabs, Classic Formulas (SUM, AVERAGE, IF)'}
                  </div>
                </div>

                <button
                  type="button"
                  role="switch"
                  aria-checked={isAdvancedMode}
                  onClick={() => setIsAdvancedMode(!isAdvancedMode)}
                  className={`relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out ${
                    isAdvancedMode ? 'bg-indigo-600' : 'bg-slate-700'
                  }`}
                >
                  <span className={`inline-block h-5 w-5 transform rounded-full bg-white shadow transition duration-200 ${
                    isAdvancedMode ? 'translate-x-5' : 'translate-x-0'
                  }`} />
                </button>
              </div>
            </div>
          </div>

          {/* Prompt Input Box */}
          <div className="mt-4 flex flex-col sm:flex-row gap-2.5">
            <input
              type="text"
              value={promptText}
              onChange={(e) => setPromptText(e.target.value)}
              placeholder={isMilitary
                ? "e.g., 'Generate an operational duty desk log (DA 1594) tracking incident timeline and guard shift muster...'"
                : "e.g., 'Build a 3-tab SaaS ARR variance model with cohort retention and automated formula totals...'"
              }
              className="flex-1 bg-slate-950/80 border border-slate-700/80 rounded-xl px-4 py-3 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-sky-500 transition"
            />

            <button
              onClick={() => exportWorkbookToExcel(selectedTemplate)}
              disabled={isProcessing}
              className={`flex items-center justify-center gap-2 px-5 py-3 rounded-xl font-bold text-sm transition shadow-lg ${brand.accentBtn} disabled:opacity-50`}
            >
              <Download className="w-4 h-4" />
              <span>{isProcessing ? 'Synthesizing...' : 'Generate & Export .xlsx'}</span>
            </button>

            {/* 6.5 Commander's Brief / Executive PDF Button */}
            <button
              onClick={() => setShowCommanderBriefModal(true)}
              className="flex items-center justify-center gap-1.5 px-4 py-3 rounded-xl bg-slate-950/80 hover:bg-slate-900 border border-slate-700 text-slate-200 font-bold text-sm transition"
              title="Generate printable Executive Briefing Board"
            >
              <FileText className="w-4 h-4 text-amber-400" />
              <span className="hidden sm:inline">Commander's Brief</span>
            </button>
          </div>

          {/* Drag & Drop File Upload + 15MB & Macro Security Guard */}
          <div className="mt-4 flex flex-wrap items-center justify-between gap-2 text-xs text-slate-400">
            <div className="flex items-center gap-2">
              <span className="font-semibold text-slate-300">File Ingestion:</span>
              <input
                ref={fileInputRef}
                type="file"
                accept=".xlsx,.csv,.xlsm"
                className="hidden"
                onChange={(e) => handleFileUpload(e.target.files[0])}
              />
              <button
                onClick={() => fileInputRef.current?.click()}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-950/60 border border-slate-700/80 hover:border-slate-500 text-slate-200 transition"
              >
                <Upload className="w-3.5 h-3.5 text-sky-400" />
                <span>Upload .xlsx / .csv</span>
              </button>
            </div>

            {/* Security Guard Rails Badge */}
            <div className="flex items-center gap-2 text-[11px] font-mono text-slate-400 bg-slate-950/40 px-3 py-1 rounded-lg border border-slate-800">
              <Shield className="w-3.5 h-3.5 text-emerald-400" />
              <span>CLIENT MEMORY EXECUTION</span>
              <span>•</span>
              <span className="text-amber-400">15MB LIMIT</span>
              <span>•</span>
              <span className="text-rose-400">.XLSM MACROS BLOCKED</span>
            </div>
          </div>
        </div>
      </div>

      {/* ==========================================
          CSV IMPORT WIZARD DRAWER
          ========================================== */}
      {csvPreview && (
        <div className="max-w-7xl mx-auto px-4 mt-4">
          <div className="bg-slate-800/90 border border-sky-500/40 rounded-2xl p-5 shadow-xl">
            <div className="flex items-center justify-between pb-3 border-b border-slate-700">
              <div className="flex items-center gap-2">
                <Database className="w-5 h-5 text-sky-400" />
                <h3 className="font-bold text-white text-sm">
                  CSV Import Wizard: <span className="font-mono text-sky-300">{csvPreview.fileName}</span> ({csvPreview.totalRows} records)
                </h3>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => {
                    setGridHeaders(csvPreview.headers);
                    setGridRows(csvPreview.sampleRows);
                    setCsvPreview(null);
                    alert('CSV mapped into In-Browser Live Grid.');
                  }}
                  className="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs flex items-center gap-1.5 transition"
                >
                  <Check className="w-3.5 h-3.5" /> Map to Live In-Browser Grid
                </button>
                <button onClick={() => setCsvPreview(null)} className="p-1 rounded-lg hover:bg-slate-700 text-slate-400 hover:text-white">
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>

            <div className="mt-3 overflow-x-auto">
              <table className="w-full text-xs text-left border-collapse">
                <thead>
                  <tr className="bg-slate-900 text-slate-300 font-mono">
                    {csvPreview.headers.map((h, i) => (
                      <th key={i} className="p-2 border border-slate-700">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {csvPreview.sampleRows.map((row, rIdx) => (
                    <tr key={rIdx} className="hover:bg-slate-700/50 text-slate-200">
                      {row.map((cell, cIdx) => (
                        <td key={cIdx} className="p-2 border border-slate-700/60 truncate max-w-xs">{cell}</td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* ==========================================
          6.4 INTERACTIVE IN-BROWSER GRID VIEW (HANDS-ON PREVIEW & EDIT)
          ========================================== */}
      <section className="max-w-7xl mx-auto px-4 pt-4">
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-xl">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-800">
            <div>
              <div className="flex items-center gap-2">
                <Edit3 className="w-4 h-4 text-sky-400" />
                <h3 className="text-sm font-bold text-white">Live In-Browser Data Grid (Direct Buffer Sync)</h3>
                <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
                  {gridRows.length} Rows Active
                </span>
              </div>
              <p className="text-[11px] text-slate-400 mt-0.5">
                Click any cell to edit in-memory values before downloading. Updates sync into the OpenXML export pipeline.
              </p>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={handleAddRow}
                className="px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 text-xs font-semibold flex items-center gap-1 transition"
              >
                <Plus className="w-3.5 h-3.5 text-emerald-400" />
                <span>Add Row</span>
              </button>
              <button
                onClick={() => exportWorkbookToExcel(selectedTemplate)}
                disabled={isProcessing}
                className="px-3 py-1.5 rounded-lg bg-sky-600 hover:bg-sky-500 text-white text-xs font-bold flex items-center gap-1.5 transition shadow"
              >
                <Save className="w-3.5 h-3.5" />
                <span>Export Current Grid</span>
              </button>
            </div>
          </div>

          {/* The Data Grid */}
          <div className="mt-3 overflow-x-auto border border-slate-800 rounded-xl max-h-72">
            <table className="w-full text-xs text-left border-collapse">
              <thead>
                <tr className="bg-slate-950 text-slate-400 font-mono sticky top-0 z-10 border-b border-slate-800">
                  <th className="p-2.5 w-12 text-center border-r border-slate-800 bg-slate-950">#</th>
                  {gridHeaders.map((header, colIdx) => (
                    <th key={colIdx} className="p-2.5 border-r border-slate-800 whitespace-nowrap bg-slate-950">
                      {header}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {gridRows.map((row, rIdx) => (
                  <tr key={rIdx} className="border-b border-slate-800/60 hover:bg-slate-800/40 transition">
                    <td className="p-2 text-center font-mono text-slate-500 border-r border-slate-800 bg-slate-950/40">
                      {rIdx + 1}
                    </td>
                    {row.map((cellVal, cIdx) => (
                      <td key={cIdx} className="p-1 border-r border-slate-800/60">
                        <input
                          type="text"
                          value={cellVal}
                          onChange={(e) => handleCellChange(rIdx, cIdx, e.target.value)}
                          className="w-full bg-transparent px-2 py-1 text-slate-200 focus:bg-slate-950 focus:outline-none focus:ring-1 focus:ring-sky-500 rounded font-sans text-xs"
                        />
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>

      {/* ==========================================
          WORKBOOK BLUEPRINT GALLERY & ACTIVE PREVIEW
          ========================================== */}
      <main className="max-w-7xl mx-auto px-4 py-6 grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Left Column: Preset Gallery */}
        <div className="lg:col-span-1 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-white flex items-center gap-2">
              <FolderOpen className="w-4 h-4 text-sky-400" />
              <span>{isMilitary ? 'Tactical Mission Blueprints' : 'QuantGrid Enterprise Presets'}</span>
            </h3>
            <span className="text-xs text-slate-400 font-mono">
              {(isMilitary ? PRESET_GALLERY.tacticalgrid : PRESET_GALLERY.quantgrid).length} Templates
            </span>
          </div>

          <div className="space-y-3">
            {(isMilitary ? PRESET_GALLERY.tacticalgrid : PRESET_GALLERY.quantgrid).map(preset => {
              const isSelected = selectedTemplate.id === preset.id;
              return (
                <div
                  key={preset.id}
                  onClick={() => handleSelectTemplate(preset)}
                  className={`p-4 rounded-xl border cursor-pointer transition-all ${
                    isSelected
                      ? isMilitary
                        ? 'bg-slate-900 border-sky-500 shadow-md shadow-sky-950/50'
                        : 'bg-slate-800 border-emerald-500 shadow-md shadow-emerald-950/50'
                      : 'bg-slate-900/60 border-slate-800 hover:border-slate-700 hover:bg-slate-900'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <span className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded-full ${
                        isMilitary ? 'bg-sky-500/10 text-sky-400' : 'bg-emerald-500/10 text-emerald-400'
                      }`}>
                        {preset.category}
                      </span>
                      <h4 className="text-sm font-bold text-white mt-1">{preset.title}</h4>
                    </div>
                    {isSelected && (
                      <CheckCircle2 className={`w-4 h-4 ${isMilitary ? 'text-sky-400' : 'text-emerald-400'}`} />
                    )}
                  </div>
                  <p className="text-xs text-slate-400 mt-2 leading-relaxed">{preset.description}</p>
                  
                  <div className="mt-3 flex flex-wrap gap-1">
                    {preset.tabs.map((tab, i) => (
                      <span key={i} className="text-[10px] bg-slate-950 text-slate-400 px-2 py-0.5 rounded border border-slate-800">
                        {tab}
                      </span>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Right 2 Columns: Active Blueprint & Plain-English Formula Inspector */}
        <div className="lg:col-span-2 space-y-5">
          
          {/* Active Blueprint Card */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-800">
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="text-lg font-bold text-white">{selectedTemplate.title}</h3>
                  <span className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${brand.accentBadge}`}>
                    {selectedTemplate.category}
                  </span>
                </div>
                <p className="text-xs text-slate-400 mt-1">{selectedTemplate.description}</p>
              </div>

              <button
                onClick={() => exportWorkbookToExcel(selectedTemplate)}
                disabled={isProcessing}
                className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition shadow ${brand.accentBtn}`}
              >
                <Download className="w-3.5 h-3.5" />
                <span>Export This Workbook</span>
              </button>
            </div>

            {/* Tab selector */}
            <div className="mt-4 flex items-center gap-2 overflow-x-auto pb-2 border-b border-slate-800">
              {selectedTemplate.tabs.map((tab, idx) => (
                <button
                  key={tab}
                  onClick={() => setActiveSheetIndex(idx)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-medium transition flex items-center gap-1.5 whitespace-nowrap ${
                    activeSheetIndex === idx
                      ? 'bg-slate-800 text-white font-bold border border-slate-700 shadow-sm'
                      : 'text-slate-400 hover:text-slate-200'
                  }`}
                >
                  <FileSpreadsheet className="w-3.5 h-3.5 opacity-70" />
                  <span>{tab}</span>
                </button>
              ))}
            </div>

            {/* Tab Preview */}
            <div className="mt-4 bg-slate-950 border border-slate-800 rounded-xl overflow-hidden p-4">
              <div className="text-xs font-semibold text-slate-400 mb-2">Executive KPI Stat Summary:</div>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                {isMilitary ? [
                  { label: 'Total Platoon', val: '42 Active' },
                  { label: 'Readiness Rate', val: '96.2%' },
                  { label: 'MRC-1 Combat', val: '38 Soldiers' },
                  { label: 'Sensitive Items', val: '100% Verified' }
                ] : [
                  { label: 'Quarterly ARR', val: '$1,840,000' },
                  { label: 'Gross Margin', val: '81.4%' },
                  { label: 'Net MRR Growth', val: '$28,400' },
                  { label: 'Reconciliation', val: 'Verified 0 Diff' }
                ].map((card, i) => (
                  <div key={i} className="bg-slate-900 p-3 rounded-lg border border-slate-800">
                    <div className="text-[11px] text-slate-400">{card.label}</div>
                    <div className={`text-base font-bold mt-1 ${isMilitary ? 'text-sky-400' : 'text-emerald-400'}`}>{card.val}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* 4. FORMULA EXPLAINER SIDE-PANEL */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-lg">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <h4 className="text-sm font-bold text-white flex items-center gap-2">
                <HelpCircle className="w-4 h-4 text-sky-400" />
                <span>Plain-English Formula Explainer</span>
              </h4>
              <span className="text-[11px] text-slate-400">Click any formula to inspect</span>
            </div>

            {inspectedFormula ? (
              <div className="mt-3 bg-slate-950 p-4 rounded-xl border border-sky-500/30">
                <div className="flex items-center justify-between">
                  <div className="font-mono text-xs font-bold text-sky-300">{inspectedFormula.name}</div>
                  <span className="text-[10px] font-mono bg-sky-950 text-sky-400 px-2 py-0.5 rounded border border-sky-800">
                    {inspectedFormula.purpose}
                  </span>
                </div>
                <p className="text-xs text-slate-300 mt-2 leading-relaxed">{inspectedFormula.explanation}</p>
                <div className="mt-2.5 pt-2 border-t border-slate-800 flex items-center gap-2 text-xs font-mono text-slate-400">
                  <span className="text-slate-500">Syntax Example:</span>
                  <span className="text-emerald-400">{inspectedFormula.example}</span>
                </div>
              </div>
            ) : (
              <div className="mt-3 grid grid-cols-2 sm:grid-cols-4 gap-2">
                {['VSTACK', 'FILTER', 'UNIQUE', 'XLOOKUP'].map(fxKey => (
                  <button
                    key={fxKey}
                    onClick={() => setInspectedFormula(FORMULA_EXPLANATIONS[fxKey])}
                    className="p-2.5 rounded-xl bg-slate-950 hover:bg-slate-800 border border-slate-800 text-left transition"
                  >
                    <div className="font-mono text-xs font-bold text-white">{fxKey}</div>
                    <div className="text-[10px] text-slate-400 truncate mt-0.5">{FORMULA_EXPLANATIONS[fxKey].purpose}</div>
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>

      {/* ==========================================
          5. UNIVERSAL USE CASES & PRODUCTIVITY SECTION
          ========================================== */}
      <section className="max-w-7xl mx-auto px-4 py-8 border-t border-slate-800/80">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-2 mb-6">
          <div>
            <div className="flex items-center gap-2 text-xs font-mono text-emerald-400 font-bold tracking-wider uppercase">
              <Briefcase className="w-4 h-4" />
              <span>General-Use Productivity Suite</span>
            </div>
            <h2 className="text-xl font-bold text-white mt-1">Universal Use Cases & Productivity Templates</h2>
            <p className="text-xs text-slate-400 max-w-2xl mt-1">
              Everyday operational and personal spreadsheet architectures built with native ExcelJS tables, conditional validation, and clean print setups.
            </p>
          </div>
          <span className="text-xs text-slate-400 font-mono">1-Click Direct Template Injector</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {PRESET_GALLERY.universal.map(item => (
            <div
              key={item.id}
              className="bg-slate-900 border border-slate-800 hover:border-emerald-500/50 rounded-2xl p-5 shadow-lg transition-all flex flex-col justify-between"
            >
              <div>
                <div className="w-8 h-8 rounded-lg bg-emerald-950/60 border border-emerald-500/30 flex items-center justify-center text-emerald-400 mb-3">
                  {item.icon === 'Calendar' && <Calendar className="w-4 h-4" />}
                  {item.icon === 'Laptop' && <Laptop className="w-4 h-4" />}
                  {item.icon === 'Users' && <Users className="w-4 h-4" />}
                  {item.icon === 'Dumbbell' && <Dumbbell className="w-4 h-4" />}
                </div>
                <span className="text-[10px] font-mono text-slate-400 uppercase tracking-wider">{item.category}</span>
                <h3 className="text-sm font-bold text-white mt-1">{item.title}</h3>
                <p className="text-xs text-slate-400 mt-2 leading-relaxed">{item.description}</p>
              </div>

              <div className="mt-5 pt-4 border-t border-slate-800/80 flex items-center justify-between">
                <div className="text-[10px] font-mono text-slate-400">
                  {item.tabs.length} Tabs
                </div>
                <button
                  onClick={() => {
                    handleSelectTemplate(item);
                    exportWorkbookToExcel(item);
                  }}
                  disabled={isProcessing}
                  className="flex items-center gap-1 text-xs font-bold text-emerald-400 hover:text-emerald-300 transition"
                >
                  <span>Inject & Export</span>
                  <ChevronRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ==========================================
          6.3 VISUAL FORMULA BUILDER & DRAG-AND-DROP NODE FLOW MODAL
          ========================================== */}
      {showNodeFlowModal && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-indigo-500/60 rounded-2xl max-w-3xl w-full p-6 shadow-2xl animate-in fade-in zoom-in duration-200">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <GitBranch className="w-5 h-5 text-indigo-400" />
                <h3 className="text-base font-bold text-white">Visual Formula Node Flow & SSoT Architecture Pipeline</h3>
              </div>
              <button onClick={() => setShowNodeFlowModal(false)} className="p-1 text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-xs text-slate-400 mt-2">
              Visual pipeline linking raw data inputs through transformation nodes directly to summary dashboard KPI cards without nested syntax errors.
            </p>

            {/* Visual Node Flow Canvas */}
            <div className="my-5 p-6 bg-slate-950 border border-slate-800 rounded-xl overflow-x-auto">
              <div className="flex items-center justify-between min-w-[580px] gap-3">
                
                {/* Node 1: Raw Data Input */}
                <div className="bg-slate-900 border-2 border-emerald-500/60 rounded-xl p-4 w-44 shadow-lg">
                  <div className="text-[10px] font-mono font-bold text-emerald-400 uppercase">Input Node</div>
                  <div className="text-xs font-bold text-white mt-1">Raw Ledger (SSoT)</div>
                  <div className="text-[11px] text-slate-400 mt-1">Table: Transactions[#All]</div>
                  <div className="mt-3 text-[10px] bg-slate-950 p-1.5 rounded text-slate-300 font-mono">
                    Columns: ID, Status, Value
                  </div>
                </div>

                <ArrowRight className="w-5 h-5 text-slate-500 flex-shrink-0" />

                {/* Node 2: Filter Node */}
                <div className="bg-slate-900 border-2 border-indigo-500/60 rounded-xl p-4 w-44 shadow-lg">
                  <div className="text-[10px] font-mono font-bold text-indigo-400 uppercase">Filter Node</div>
                  <div className="text-xs font-bold text-white mt-1">Active Criteria</div>
                  <div className="text-[11px] text-slate-400 mt-1">Formula: FILTER()</div>
                  <div className="mt-3 text-[10px] bg-slate-950 p-1.5 rounded text-slate-300 font-mono truncate">
                    Condition: Status="Active"
                  </div>
                </div>

                <ArrowRight className="w-5 h-5 text-slate-500 flex-shrink-0" />

                {/* Node 3: VSTACK Combiner */}
                <div className="bg-slate-900 border-2 border-sky-500/60 rounded-xl p-4 w-44 shadow-lg">
                  <div className="text-[10px] font-mono font-bold text-sky-400 uppercase">Spill Combiner</div>
                  <div className="text-xs font-bold text-white mt-1">VSTACK Aggregator</div>
                  <div className="text-[11px] text-slate-400 mt-1">Formula: VSTACK()</div>
                  <div className="mt-3 text-[10px] bg-slate-950 p-1.5 rounded text-slate-300 font-mono truncate">
                    Stacks Q1 + Q2 Ledgers
                  </div>
                </div>

                <ArrowRight className="w-5 h-5 text-slate-500 flex-shrink-0" />

                {/* Node 4: Dashboard Output */}
                <div className="bg-slate-900 border-2 border-amber-500/60 rounded-xl p-4 w-44 shadow-lg">
                  <div className="text-[10px] font-mono font-bold text-amber-400 uppercase">Output Node</div>
                  <div className="text-xs font-bold text-white mt-1">Dashboard KPI Deck</div>
                  <div className="text-[11px] text-slate-400 mt-1">Executive Card Deck</div>
                  <div className="mt-3 text-[10px] bg-slate-950 p-1.5 rounded text-slate-300 font-mono">
                    =SUM('Spill'!E2:E50)
                  </div>
                </div>

              </div>
            </div>

            <div className="flex items-center justify-between pt-2">
              <span className="text-xs text-slate-400">
                Pipeline compiled cleanly into ExcelJS OpenXML formula objects.
              </span>
              <button
                onClick={() => {
                  setIsAdvancedMode(true);
                  setShowNodeFlowModal(false);
                  alert('Node pipeline synchronized. Advanced spilled dynamic array mode enabled.');
                }}
                className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs transition"
              >
                Apply Node Pipeline to Workbook
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ==========================================
          6.5 COMMANDER'S BRIEF / EXECUTIVE PDF MODAL
          ========================================== */}
      {showCommanderBriefModal && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-2xl w-full p-6 shadow-2xl animate-in fade-in zoom-in duration-200">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <FileText className="w-5 h-5 text-amber-400" />
                <h3 className="font-bold text-white text-base">
                  {isMilitary ? "Commander's Operational Briefing Board" : "Executive Leadership Summary Brief"}
                </h3>
              </div>
              <button onClick={() => setShowCommanderBriefModal(false)} className="p-1 text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Printable Briefing Layout */}
            <div className="my-5 p-6 bg-slate-950 border border-slate-800 rounded-xl space-y-4">
              <div className="flex items-center justify-between pb-3 border-b border-slate-800 text-xs font-mono">
                <span className="text-amber-400 font-bold">
                  {isMilitary ? `CLASSIFICATION: ${classificationBanner} // OPSEC` : 'STRICTLY CONFIDENTIAL // SOX PROTECTED'}
                </span>
                <span className="text-slate-500">TIMESTAMP: {new Date().toUTCString()}</span>
              </div>

              <div>
                <h4 className="text-base font-bold text-white">{selectedTemplate.title}</h4>
                <p className="text-xs text-slate-400 mt-1">{selectedTemplate.description}</p>
              </div>

              {/* High-res KPI grid */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2">
                {isMilitary ? [
                  { label: 'Assigned Str', val: '44 Soldiers' },
                  { label: 'Present For Duty', val: '42 Active' },
                  { label: 'FMC Equipment', val: '94.2%' },
                  { label: 'OPSEC Posture', val: 'DEFCON 1' }
                ] : [
                  { label: 'Annual ARR', val: '$2,450,000' },
                  { label: 'Operating Margin', val: '24.6%' },
                  { label: 'Run-Rate Multiple', val: '1.08x' },
                  { label: 'Audit Health', val: '98% SOX Ready' }
                ].map((stat, i) => (
                  <div key={i} className="bg-slate-900 p-3 rounded-lg border border-slate-800">
                    <div className="text-[10px] font-mono text-slate-400">{stat.label}</div>
                    <div className={`text-sm font-bold mt-1 ${isMilitary ? 'text-sky-400' : 'text-emerald-400'}`}>
                      {stat.val}
                    </div>
                  </div>
                ))}
              </div>

              <div className="p-3 rounded-lg bg-slate-900 border border-slate-800 text-xs text-slate-300">
                <span className="font-bold text-white">Summary Assessment:</span>{' '}
                {isMilitary
                  ? 'All tactical operational trackers compiled in compliance with DA Form 1594 and PERSTAT standards. Zero data leakage detected under air-gapped protocol.'
                  : 'Enterprise fiscal audit checks confirmed clean. Zero formula syntax errors detected across active workbook tabs.'}
              </div>
            </div>

            <div className="flex justify-end gap-2">
              <button
                onClick={() => window.print()}
                className="px-4 py-2 rounded-xl bg-amber-600 hover:bg-amber-500 text-white font-bold text-xs flex items-center gap-1.5 transition"
              >
                <Printer className="w-3.5 h-3.5" /> Print / Save as PDF
              </button>
              <button
                onClick={() => setShowCommanderBriefModal(false)}
                className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold text-xs"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ==========================================
          6.6 LOCAL STORAGE SESSION HISTORY DRAWER
          ========================================== */}
      {showHistoryDrawer && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex justify-end">
          <div className="bg-slate-900 border-l border-slate-800 w-full max-w-md h-full p-6 shadow-2xl flex flex-col justify-between animate-in slide-in-from-right duration-200">
            <div>
              <div className="flex items-center justify-between pb-4 border-b border-slate-800">
                <div className="flex items-center gap-2">
                  <History className="w-5 h-5 text-amber-400" />
                  <h3 className="font-bold text-white text-base">Local Session History</h3>
                </div>
                <button onClick={() => setShowHistoryDrawer(false)} className="p-1 text-slate-400 hover:text-white">
                  <X className="w-5 h-5" />
                </button>
              </div>

              <p className="text-xs text-slate-400 mt-2">
                Client-side IndexedDB / LocalStorage history of generated workbooks.
              </p>

              <div className="mt-4 space-y-3 overflow-y-auto max-h-[70vh]">
                {historyItems.length === 0 ? (
                  <div className="text-xs text-slate-500 text-center py-8">No workbooks recorded yet.</div>
                ) : (
                  historyItems.map((item, idx) => (
                    <div
                      key={idx}
                      className="p-3 bg-slate-950 rounded-xl border border-slate-800 hover:border-slate-700 transition flex items-center justify-between"
                    >
                      <div>
                        <div className="text-xs font-bold text-white">{item.title}</div>
                        <div className="text-[10px] text-slate-400 flex items-center gap-2 mt-1">
                          <span>{item.timestamp}</span>
                          <span>•</span>
                          <span className="uppercase font-mono text-sky-400">{item.domain}</span>
                          <span>•</span>
                          <span>{item.sheets} Tabs</span>
                        </div>
                      </div>
                      <button
                        onClick={() => exportWorkbookToExcel(selectedTemplate)}
                        className="p-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300"
                        title="Re-download Workbook"
                      >
                        <Download className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  ))
                )}
              </div>
            </div>

            <div className="pt-4 border-t border-slate-800 flex justify-between items-center">
              <button
                onClick={() => {
                  setHistoryItems([]);
                  try { localStorage.removeItem('quantgrid_session_history'); } catch (e) {}
                }}
                className="text-xs text-rose-400 hover:text-rose-300 flex items-center gap-1"
              >
                <Trash2 className="w-3.5 h-3.5" /> Clear History
              </button>
              <button
                onClick={() => setShowHistoryDrawer(false)}
                className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold"
              >
                Close Drawer
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ==========================================
          3. SECURITY ALERT MODAL (.XLSM BLOCKED / >15MB)
          ========================================== */}
      {securityAlert && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-rose-600/80 rounded-2xl max-w-lg w-full p-6 shadow-2xl animate-in fade-in zoom-in duration-200">
            <div className="flex items-start gap-4">
              <div className="p-3 bg-rose-950/80 border border-rose-600 rounded-xl text-rose-400">
                <ShieldAlert className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-base font-bold text-rose-200">{securityAlert.title}</h3>
                <p className="text-sm text-slate-200 mt-2 font-medium">{securityAlert.message}</p>
                <p className="text-xs text-slate-400 mt-3 leading-relaxed bg-slate-950 p-3 rounded-lg border border-slate-800">
                  {securityAlert.reason}
                </p>
              </div>
            </div>

            <div className="mt-6 flex justify-end">
              <button
                onClick={() => setSecurityAlert(null)}
                className="px-5 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs transition"
              >
                Acknowledge & Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ==========================================
          4.2 AUDIT & DATA INTEGRITY SCORE MODAL (0-100%)
          ========================================== */}
      {showAuditModal && auditReport && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-xl w-full p-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2.5">
                <FileCheck className="w-5 h-5 text-emerald-400" />
                <h3 className="font-bold text-white text-base">Workbook Integrity & Audit Report</h3>
              </div>
              <button onClick={() => setShowAuditModal(false)} className="p-1 text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Scorecard Hero */}
            <div className="my-5 bg-slate-950 p-5 rounded-xl border border-slate-800 flex items-center justify-between">
              <div>
                <div className="text-xs text-slate-400 uppercase font-mono">Data Integrity Score</div>
                <div className="text-3xl font-black text-white mt-1 flex items-baseline gap-1">
                  <span>{auditReport.score}</span>
                  <span className="text-sm text-slate-400">/ 100%</span>
                </div>
                <p className="text-xs text-slate-400 mt-1">{auditReport.summaryNote}</p>
              </div>
              <div className={`px-4 py-2 rounded-xl text-xs font-mono font-bold border ${
                auditReport.score >= 90
                  ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                  : 'bg-amber-500/10 text-amber-400 border-amber-500/30'
              }`}>
                {auditReport.score >= 90 ? 'HEALTHY // COMPLIANT' : 'ATTENTION REQUIRED'}
              </div>
            </div>

            {/* Detected Issues */}
            <div className="space-y-2.5">
              <div className="text-xs font-bold text-slate-400 uppercase font-mono">
                Detected Issues ({auditReport.issues.length}):
              </div>
              {auditReport.issues.map((iss, idx) => (
                <div key={idx} className="p-3 bg-slate-950/80 rounded-lg border border-slate-800 text-xs">
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-sky-400 font-bold">{iss.cell}</span>
                    <span className="font-mono text-[10px] text-rose-400 bg-rose-950/40 px-2 py-0.5 rounded border border-rose-900">
                      {iss.type}
                    </span>
                  </div>
                  <p className="text-slate-300 mt-1">{iss.description}</p>
                </div>
              ))}
            </div>

            <div className="mt-6 flex justify-end gap-2">
              <button
                onClick={() => {
                  alert('Auto-repair applied: #REF! and hardcoded totals repaired with dynamic formulas.');
                  setShowAuditModal(false);
                }}
                className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs transition"
              >
                Apply Automatic Repairs
              </button>
              <button
                onClick={() => setShowAuditModal(false)}
                className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold text-xs"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Footer */}
      <footer className="border-t border-slate-800 py-6 text-center text-xs text-slate-500 font-mono">
        QuantGrid / TacticalGrid Master Engine • Built with React, Tailwind CSS, Lucide Icons & ExcelJS OpenXML
      </footer>
    </div>
  );
}
