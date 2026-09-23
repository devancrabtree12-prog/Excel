/**
 * Brand Constants for QuantGrid / TacticalGrid
 * Decouples branding, themes, strings, and high-contrast status badge configurations.
 */

export interface StatusBadgeConfig {
  code: string;
  label: string;
  bgHex: string;
  borderHex: string;
  textHex: string;
}

export interface BrandConfig {
  name: string;
  logoIcon: string;
  subtitle: string;
  categoryTag: string;
  tagline: string;
  colors: {
    primary: string;
    secondary: string;
    background: string;
    surface: string;
    surfaceVariant: string;
    border: string;
    textPrimary: string;
    textSecondary: string;
    accent: string;
    headerBg: string;
  };
  statusBadges: StatusBadgeConfig[];
}

export const DOMAINS = {
  FINANCE: 'financial',
  HR: 'hr',
  HEALTHCARE: 'healthcare',
  PROJECT_MANAGEMENT: 'project',
  MILITARY_DEFENSE: 'military',
  SALES: 'sales',
  GENERAL: 'general',
} as const;

export const DOMAIN_OPTIONS = [
  { id: DOMAINS.FINANCE, name: 'Finance & Accounting', icon: '💰' },
  { id: DOMAINS.HR, name: 'Human Resources & People Ops', icon: '👥' },
  { id: DOMAINS.HEALTHCARE, name: 'Healthcare & Clinical', icon: '🏥' },
  { id: DOMAINS.PROJECT_MANAGEMENT, name: 'Project Management & Agile', icon: '📋' },
  { id: DOMAINS.MILITARY_DEFENSE, name: 'Military & Defense', icon: '🎖️' },
  { id: DOMAINS.SALES, name: 'Sales & Revenue Pipeline', icon: '📈' },
  { id: DOMAINS.GENERAL, name: 'General Enterprise Tracker', icon: '📊' },
];

export const QUANTGRID_BRAND: BrandConfig = {
  name: 'QuantGrid',
  logoIcon: '📊',
  subtitle: 'Enterprise Spreadsheet Architecture & Intelligent Workbook Generator',
  categoryTag: 'ENTERPRISE',
  tagline: 'Precision Financial, Operational, and Multi-tab Workbook Synthesis',
  colors: {
    primary: '#107C41', // Microsoft Excel / QuantGrid Emerald
    secondary: '#15803D',
    background: '#F8FAFC',
    surface: '#FFFFFF',
    surfaceVariant: '#E2E8F0',
    border: '#CBD5E1',
    textPrimary: '#0F172A',
    textSecondary: '#64748B',
    accent: '#22C55E',
    headerBg: '#0A4A28',
  },
  statusBadges: [
    {
      code: 'SOX_AUDIT',
      label: 'SOX AUDIT READY',
      bgHex: '#ECFDF5',
      borderHex: '#10B981',
      textHex: '#065F46',
    },
    {
      code: 'VERIFIED_ENGINE',
      label: 'ENGINE VERIFIED',
      bgHex: '#EFF6FF',
      borderHex: '#3B82F6',
      textHex: '#1E40AF',
    },
    {
      code: 'DYNAMIC_FORMULAS',
      label: '100% FORMULA INTEGRITY',
      bgHex: '#F0FDF4',
      borderHex: '#22C55E',
      textHex: '#15803D',
    },
  ],
};

export const TACTICALGRID_BRAND: BrandConfig = {
  name: 'TacticalGrid',
  logoIcon: '🎖️',
  subtitle: 'Mission-Critical Operational Trackers & Duty Log Architecture',
  categoryTag: 'TACTICAL OPSEC',
  tagline: 'High-Integrity Duty Logs, PERSTAT Accountability & Defense Readiness',
  colors: {
    primary: '#38BDF8', // Tactical radar cyan / sky
    secondary: '#0EA5E9',
    background: '#020617', // Tactical Black (Slate 950)
    surface: '#0F172A',    // Dark Slate (Slate 900)
    surfaceVariant: '#1E293B', // Slate 800
    border: '#334155',     // High-contrast slate 700
    textPrimary: '#F8FAFC',
    textSecondary: '#94A3B8',
    accent: '#22C55E',     // Night-vision phosphorescent green
    headerBg: '#020617',   // Pitch tactical black header
  },
  statusBadges: [
    {
      code: 'DEFCON_1',
      label: 'MISSION READY // DEFCON-1',
      bgHex: '#052E16',
      borderHex: '#22C55E',
      textHex: '#4ADE80',
    },
    {
      code: 'CLASSIFIED_NOFORN',
      label: 'CLASSIFIED // NOFORN',
      bgHex: '#451A03',
      borderHex: '#F59E0B',
      textHex: '#FCD34D',
    },
    {
      code: 'OPSEC_PROTOCOL',
      label: 'OPSEC PROTOCOL ACTIVE',
      bgHex: '#082F49',
      borderHex: '#38BDF8',
      textHex: '#7DD3FC',
    },
    {
      code: 'PERSTAT_ACCOUNTED',
      label: 'PERSTAT 100% ACCOUNTED',
      bgHex: '#450A0A',
      borderHex: '#EF4444',
      textHex: '#FCA5A5',
    },
  ],
};

export function getBrandForDomain(domainId: string): BrandConfig {
  if (domainId === DOMAINS.MILITARY_DEFENSE) {
    return TACTICALGRID_BRAND;
  }
  return QUANTGRID_BRAND;
}
