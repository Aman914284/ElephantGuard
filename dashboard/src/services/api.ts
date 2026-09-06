import {
  UserRecord,
  IncidentReport,
  AlertRecord,
  SocietalChallenge,
  CollaborationSolution,
  DashboardSummary,
  AnalyticsData,
  AuthSession
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000';

class ApiService {
  private token: string | null = null;

  constructor() {
    this.token = localStorage.getItem('elephant_guard_token');
  }

  public setToken(token: string | null) {
    this.token = token;
    if (token) {
      localStorage.setItem('elephant_guard_token', token);
    } else {
      localStorage.removeItem('elephant_guard_token');
    }
  }

  public getToken(): string | null {
    return this.token || localStorage.getItem('elephant_guard_token');
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>),
    };

    const currentToken = this.getToken();
    if (currentToken) {
      headers['Authorization'] = `Bearer ${currentToken}`;
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });

      if (!response.ok) {
        let errorMsg = `HTTP Error ${response.status}`;
        try {
          const errData = await response.json();
          if (errData && errData.detail) {
            errorMsg = typeof errData.detail === 'string' ? errData.detail : JSON.stringify(errData.detail);
          }
        } catch {
          // fallback to status text
          errorMsg = response.statusText || errorMsg;
        }
        throw new Error(errorMsg);
      }

      return (await response.json()) as T;
    } catch (error: any) {
      console.error(`API Error on [${options.method || 'GET'}] ${endpoint}:`, error);
      throw error;
    }
  }

  // Auth Endpoints
  public async adminLogin(identifier: string, password: string): Promise<AuthSession> {
    const res = await this.request<{ success: boolean; token: string; user: UserRecord }>('/api/v1/admin/login', {
      method: 'POST',
      body: JSON.stringify({ identifier, password }),
    });
    this.setToken(res.token);
    return { token: res.token, user: res.user };
  }

  public async userLogin(identifier: string, password: string): Promise<AuthSession> {
    const res = await this.request<{ success: boolean; token: string; user: UserRecord }>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ identifier, password }),
    });
    this.setToken(res.token);
    return { token: res.token, user: res.user };
  }

  public async registerUser(data: {
    fullName: string;
    mobile: string;
    email: string;
    password: string;
    role?: string;
  }): Promise<AuthSession> {
    const res = await this.request<{ success: boolean; token: string; user: UserRecord }>('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    return { token: res.token, user: res.user };
  }

  public async getMe(): Promise<UserRecord> {
    return this.request<UserRecord>('/api/v1/auth/me');
  }

  // Summary / KPIs
  public async getSummary(): Promise<DashboardSummary> {
    return this.request<DashboardSummary>('/api/v1/admin/summary');
  }

  // Users Management
  public async getUsers(query?: string, role?: string, status?: string): Promise<UserRecord[]> {
    const params = new URLSearchParams();
    if (query) params.append('query', query);
    if (role) params.append('role', role);
    if (status) params.append('status', status);
    const queryString = params.toString() ? `?${params.toString()}` : '';
    return this.request<UserRecord[]>(`/api/v1/admin/users${queryString}`);
  }

  public async getUserDetail(id: string): Promise<UserRecord> {
    return this.request<UserRecord>(`/api/v1/admin/users/${id}`);
  }

  public async createAdminUser(data: {
    fullName: string;
    mobile: string;
    email: string;
    password: string;
    role: string;
    status?: string;
  }): Promise<UserRecord> {
    return this.request<UserRecord>('/api/v1/admin/users', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  public async updateUserStatus(userId: string, status: string): Promise<{ success: boolean }> {
    return this.request<{ success: boolean }>(`/api/v1/admin/users/${userId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status }),
    });
  }

  // Incidents
  public async getIncidents(threatLevel?: string, verificationStatus?: string): Promise<IncidentReport[]> {
    const params = new URLSearchParams();
    if (threatLevel) params.append('threatLevel', threatLevel);
    if (verificationStatus) params.append('verificationStatus', verificationStatus);
    const queryString = params.toString() ? `?${params.toString()}` : '';
    return this.request<IncidentReport[]>(`/api/v1/admin/incidents${queryString}`);
  }

  public async getIncidentDetail(id: string): Promise<IncidentReport> {
    return this.request<IncidentReport>(`/api/v1/admin/incidents/${id}`);
  }

  public async createIncident(data: Partial<IncidentReport>): Promise<{ success: boolean; incidentId: string }> {
    return this.request<{ success: boolean; incidentId: string }>('/api/v1/admin/incidents', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  public async updateIncident(id: string, data: Partial<IncidentReport>): Promise<{ success: boolean }> {
    return this.request<{ success: boolean }>(`/api/v1/admin/incidents/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  }

  // Alerts
  public async getAlerts(status?: string, severity?: string): Promise<AlertRecord[]> {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (severity) params.append('severity', severity);
    const queryString = params.toString() ? `?${params.toString()}` : '';
    return this.request<AlertRecord[]>(`/api/v1/admin/alerts${queryString}`);
  }

  public async updateAlert(id: string, data: Partial<AlertRecord>): Promise<{ success: boolean }> {
    return this.request<{ success: boolean }>(`/api/v1/admin/alerts/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  }

  // Crowdsourced Challenges (SIH26043)
  public async getChallenges(): Promise<SocietalChallenge[]> {
    return this.request<SocietalChallenge[]>('/api/v1/admin/challenges');
  }

  public async createChallenge(data: Partial<SocietalChallenge>): Promise<{ success: boolean; challengeId: string }> {
    return this.request<{ success: boolean; challengeId: string }>('/api/v1/admin/challenges', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  public async updateChallenge(id: string, data: Partial<SocietalChallenge>): Promise<{ success: boolean }> {
    return this.request<{ success: boolean }>(`/api/v1/admin/challenges/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  }

  // University & Industry Solutions
  public async getSolutions(challengeId?: string): Promise<CollaborationSolution[]> {
    const queryString = challengeId ? `?challengeId=${challengeId}` : '';
    return this.request<CollaborationSolution[]>(`/api/v1/admin/solutions${queryString}`);
  }

  public async submitSolution(data: Partial<CollaborationSolution>): Promise<{ success: boolean; solutionId: string }> {
    return this.request<{ success: boolean; solutionId: string }>('/api/v1/admin/solutions', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  public async updateSolution(id: string, data: Partial<CollaborationSolution>): Promise<{ success: boolean }> {
    return this.request<{ success: boolean }>(`/api/v1/admin/solutions/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  }

  // Analytics
  public async getAnalytics(): Promise<AnalyticsData> {
    return this.request<AnalyticsData>('/api/v1/admin/analytics');
  }
}

export const api = new ApiService();
