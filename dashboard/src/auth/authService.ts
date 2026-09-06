import { AuthResponse, AuthUser, LoginCredentials, UserRole } from '../types';

const SESSION_STORAGE_KEY = 'seems_ai_auth_session';
const SESSION_EXPIRATION_MS = 60 * 60 * 1000; // 1 Hour

let failedAttempts = 0;
let lastFailedTimestamp = 0;

export class AuthService {
  public static async login(credentials: LoginCredentials): Promise<AuthResponse> {
    const now = Date.now();

    if (failedAttempts >= 5 && now - lastFailedTimestamp < 60000) {
      return {
        success: false,
        status: 'RATE_LIMITED',
        error: 'Too many failed login attempts. Please wait 60 seconds before retrying.'
      };
    }

    const id = credentials.identifier.trim();
    const pw = credentials.password || '';

    if (!id || !pw) {
      return {
        success: false,
        status: 'ERROR',
        error: 'Invalid credentials. Please verify your ID and password.'
      };
    }

    await new Promise(res => setTimeout(res, 300));
    return this.authenticateWithDemoEngine(credentials);
  }

  public static async createDemoSession(role: UserRole = 'Forest Officer'): Promise<AuthResponse> {
    await new Promise(res => setTimeout(res, 200));

    const roleNameMap: Record<UserRole, { name: string; unit: string }> = {
      'Administrator': { name: 'Dr. V. Rao (Chief Conservator)', unit: 'HQ Wildlife Command' },
      'Forest Officer': { name: 'Aman Kumar (Range Officer)', unit: 'Dalma North Division' },
      'QRT Operator': { name: 'Patrol Commander Singh', unit: 'QRT Alpha Interceptor' },
      'Analyst': { name: 'Bio-Spatial Research Cell', unit: 'Jharkhand Analytics Hub' },
      'Community Guard': { name: 'Ramesh Murmu (Village Guard)', unit: 'Mirzadih Patrol' },
      'Citizen': { name: 'Citizen Observer', unit: 'Fringe Village Network' },
      'Researcher': { name: 'Prof. S. Das', unit: 'IIT Kharagpur Wildlife AI Lab' }
    };

    const details = roleNameMap[role] || { name: 'Wildlife Officer', unit: 'Dalma Division' };

    const demoUser: AuthUser = {
      id: `DEMO-USR-${Math.floor(1000 + Math.random() * 9000)}`,
      email: `${role.toLowerCase().replace(/\s+/g, '.')}@dalma.forest.gov.in`,
      name: details.name,
      role: role,
      isDemo: true,
      token: `demo-token-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`,
      sector: 'Dalma Pass NH-33',
      assignedUnit: details.unit,
      loginTimestamp: Date.now()
    };

    this.saveSession(demoUser, false);

    return {
      success: true,
      user: demoUser,
      status: 'SUCCESS'
    };
  }

  private static authenticateWithDemoEngine(credentials: LoginCredentials): AuthResponse {
    const { identifier, password, role = 'Forest Officer', rememberMe = false } = credentials;

    if (password && password.length < 4) {
      failedAttempts++;
      lastFailedTimestamp = Date.now();
      return {
        success: false,
        status: 'ERROR',
        error: 'Invalid credentials. Please verify your ID and password.'
      };
    }

    failedAttempts = 0;

    let userName = 'Aman Kumar';
    if (role === 'Administrator' || identifier.toLowerCase().includes('admin')) {
      userName = 'Dr. V. Rao (Chief Conservator)';
    } else if (role === 'QRT Operator' || identifier.toLowerCase().includes('qrt')) {
      userName = 'Patrol Commander Singh';
    } else if (role === 'Analyst' || identifier.toLowerCase().includes('analyst')) {
      userName = 'Spatial AI Analyst';
    } else if (role === 'Forest Officer' || identifier.toLowerCase().includes('officer') || identifier.toLowerCase().includes('aman')) {
      userName = 'Aman Kumar';
    } else if (identifier.includes('@')) {
      const baseName = identifier.split('@')[0].replace(/[._-]/g, ' ');
      userName = baseName.replace(/\b\w/g, (c: string) => c.toUpperCase());
    }

    const user: AuthUser = {
      id: `OFFICER-${Math.floor(1000 + Math.random() * 9000)}`,
      email: identifier.includes('@') ? identifier : `${identifier.toLowerCase()}@dalma.forest.gov.in`,
      name: userName,
      role: role,
      isDemo: false,
      token: `jwt-auth-session-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`,
      sector: 'NH-33 Dalma Wildlife Corridor',
      assignedUnit: 'Jharkhand Forest Department (Dalma Division)',
      loginTimestamp: Date.now()
    };

    this.saveSession(user, rememberMe);

    return {
      success: true,
      user: user,
      status: 'SUCCESS'
    };
  }

  public static async requestPasswordReset(email: string): Promise<{ success: boolean; message: string }> {
    await new Promise(res => setTimeout(res, 400));
    return {
      success: email.includes('@'),
      message: 'Password reset instructions dispatched to registered officer address.'
    };
  }

  public static async checkServiceHealth(): Promise<'ONLINE' | 'DEGRADED' | 'OFFLINE'> {
    return 'ONLINE';
  }

  public static getActiveSession(): AuthUser | null {
    try {
      const local = localStorage.getItem(SESSION_STORAGE_KEY);
      const session = sessionStorage.getItem(SESSION_STORAGE_KEY);
      const raw = local || session;
      if (!raw) return null;

      const parsed: AuthUser = JSON.parse(raw);
      if (parsed.loginTimestamp && Date.now() - parsed.loginTimestamp > SESSION_EXPIRATION_MS) {
        this.logout();
        return null;
      }
      return parsed;
    } catch {
      return null;
    }
  }

  public static saveSession(user: AuthUser, rememberMe: boolean = false): void {
    const raw = JSON.stringify(user);
    if (rememberMe) {
      localStorage.setItem(SESSION_STORAGE_KEY, raw);
    } else {
      sessionStorage.setItem(SESSION_STORAGE_KEY, raw);
    }
  }

  public static logout(): void {
    localStorage.removeItem(SESSION_STORAGE_KEY);
    sessionStorage.removeItem(SESSION_STORAGE_KEY);
  }
}
