export type ThreatLevel = 'SAFE' | 'CAUTION' | 'HIGH' | 'CRITICAL';
export type VerificationStatus = 'New' | 'Under Review' | 'Verified' | 'Dispatched' | 'Resolved' | 'Rejected';
export type ResponseStatus = 'Standby' | 'Dispatched' | 'Resolved';
export type AlertSeverity = 'SAFE' | 'CAUTION' | 'HIGH' | 'CRITICAL';
export type AlertStatus = 'Active' | 'Acknowledged' | 'Closed';
export type ChallengeStatus = 'Reported' | 'Verified' | 'Open for Solutions' | 'In Development' | 'Field Testing' | 'Implemented' | 'Resolved';
export type SolutionStatus = 'Submitted' | 'In Review' | 'Prototype' | 'Field Testing' | 'Implemented' | 'Rejected';
export type UserStatus = 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED';
export type UserRole = 'Administrator' | 'Forest Officer' | 'QRT Operator' | 'Analyst' | 'Community Guard' | 'Citizen' | 'Researcher';

export interface UserRecord {
  id: string;
  fullName: string;
  mobile: string;
  email: string;
  role: string;
  status: UserStatus;
  createdAt: string;
  lastActive: string;
}

export interface IncidentReport {
  id: string;
  reporterId?: string;
  reporterName: string;
  latitude: number;
  longitude: number;
  timestamp: string;
  confidence: number;
  elephantCount: number;
  riskScore: number;
  threatLevel: ThreatLevel;
  verificationStatus: VerificationStatus;
  responseStatus: ResponseStatus;
  photoUrl?: string | null;
  notes?: string;
  isSos: boolean;
  sourceDevice?: string;
}

export interface AlertRecord {
  id: string;
  incidentId: string;
  severity: AlertSeverity;
  locationText: string;
  latitude: number;
  longitude: number;
  distanceKm: number;
  notifiedCount: number;
  createdAt: string;
  acknowledgedAt?: string | null;
  status: AlertStatus;
  notes?: string;
}

export interface SocietalChallenge {
  id: string;
  incidentId?: string;
  title: string;
  description: string;
  location: string;
  latitude?: number;
  longitude?: number;
  status: ChallengeStatus;
  reportsCount: number;
  riskLevel: ThreatLevel;
  evidenceUrl?: string | null;
  createdAt: string;
  solutionCount?: number;
}

export interface CollaborationSolution {
  id: string;
  challengeId: string;
  challengeTitle?: string;
  challengeLocation?: string;
  organization: string;
  organizationType: string;
  description: string;
  status: SolutionStatus;
  contactEmail: string;
  contactPhone?: string;
  prototypeUrl?: string | null;
  createdAt: string;
}

export interface DashboardSummary {
  totalUsers: number;
  activeUsers: number;
  totalIncidents: number;
  verifiedIncidents: number;
  activeCriticalAlerts: number;
  resolvedIncidents: number;
  recentActivity: Array<{
    id: string;
    actor: string;
    type: string;
    severity: string;
    timestamp: string;
    details: string;
  }>;
}

export interface AnalyticsData {
  hasData: boolean;
  threatDistribution: Record<string, number>;
  verificationDistribution: Record<string, number>;
  dateTrends: Array<{ date: string; incidents: number }>;
  topHotspots: Array<{
    location: string;
    latitude: number;
    longitude: number;
    incidentCount: number;
    avgRisk: number;
  }>;
  collaborationPipeline: {
    challengesCrowdsourced: number;
    solutionsSubmitted: number;
    fieldTestingOrImplemented: number;
  };
}

export interface AuthSession {
  token: string;
  user: UserRecord;
}

// Legacy Prototype Support Types
export interface AuthUser {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  isDemo?: boolean;
  token?: string;
  sector?: string;
  assignedUnit?: string;
  loginTimestamp?: number;
}

export interface LoginCredentials {
  identifier: string;
  password?: string;
  role?: UserRole;
  rememberMe?: boolean;
}

export interface AuthResponse {
  success: boolean;
  user?: AuthUser;
  token?: string;
  status?: string;
  error?: string;
}

export type AuthStatus =
  | 'IDLE'
  | 'AUTHENTICATING'
  | 'VALIDATING'
  | 'SUCCESS'
  | 'ERROR'
  | 'RATE_LIMITED'
  | 'NETWORK_ERROR'
  | 'SESSION_EXPIRED';

export interface AuthState {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  errorMessage?: string | null;
  authStatus: AuthStatus;
  selectedRole: UserRole;
  rememberMe: boolean;
  isDemoSession: boolean;
}
