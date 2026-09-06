import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  MapPin,
  Sparkles,
  Building2,
  BarChart3,
  Search,
  Plus,
  UserPlus,
  CheckCircle,
  Activity,
  ArrowUpRight,
  RefreshCw
} from 'lucide-react';
import { api } from '../services/api';
import {
  UserRecord,
  IncidentReport,
  AlertRecord,
  SocietalChallenge,
  CollaborationSolution,
  DashboardSummary,
  AnalyticsData,
  VerificationStatus,
  ResponseStatus
} from '../types';

import { AdminHeader } from '../components/AdminHeader';
import { AdminSidebar, AdminTab } from '../components/AdminSidebar';
import { RealTimeMap } from '../components/RealTimeMap';
import { IncidentDetailModal } from '../components/IncidentDetailModal';
import { CreateChallengeModal } from '../components/CreateChallengeModal';
import { CreateSolutionModal } from '../components/CreateSolutionModal';
import { NewIncidentModal } from '../components/NewIncidentModal';
import { UserDetailModal } from '../components/UserDetailModal';
import { AddUserModal } from '../components/AddUserModal';

export const AdminDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [currentUser, setCurrentUser] = useState<UserRecord | null>(null);
  const [activeTab, setActiveTab] = useState<AdminTab>('home');

  // Data States
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [users, setUsers] = useState<UserRecord[]>([]);
  const [incidents, setIncidents] = useState<IncidentReport[]>([]);
  const [alerts, setAlerts] = useState<AlertRecord[]>([]);
  const [challenges, setChallenges] = useState<SocietalChallenge[]>([]);
  const [solutions, setSolutions] = useState<CollaborationSolution[]>([]);
  const [analytics, setAnalytics] = useState<AnalyticsData | null>(null);

  // Filters & Search
  const [userSearch, setUserSearch] = useState<string>('');
  const [userRoleFilter, setUserRoleFilter] = useState<string>('ALL');
  const [userStatusFilter, setUserStatusFilter] = useState<string>('ALL');

  const [incidentThreatFilter, setIncidentThreatFilter] = useState<string>('ALL');
  const [incidentStatusFilter, setIncidentStatusFilter] = useState<string>('ALL');

  const [alertSeverityFilter, setAlertSeverityFilter] = useState<string>('ALL');
  const [alertStatusFilter, setAlertStatusFilter] = useState<string>('ALL');

  // Modals
  const [selectedIncident, setSelectedIncident] = useState<IncidentReport | null>(null);
  const [isIncidentModalOpen, setIsIncidentModalOpen] = useState<boolean>(false);

  const [selectedUser, setSelectedUser] = useState<UserRecord | null>(null);
  const [isUserModalOpen, setIsUserModalOpen] = useState<boolean>(false);

  const [isCreateChallengeOpen, setIsCreateChallengeOpen] = useState<boolean>(false);
  const [challengeIncidentContext, setChallengeIncidentContext] = useState<IncidentReport | null>(null);

  const [isCreateSolutionOpen, setIsCreateSolutionOpen] = useState<boolean>(false);
  const [targetChallengeForSolution, setTargetChallengeForSolution] = useState<SocietalChallenge | null>(null);

  const [isNewIncidentModalOpen, setIsNewIncidentModalOpen] = useState<boolean>(false);
  const [newIncidentCoords, setNewIncidentCoords] = useState<{ latitude: number; longitude: number } | null>(null);
  const [isAddUserModalOpen, setIsAddUserModalOpen] = useState<boolean>(false);

  // 1. Initial Load, Auth Check & Real-time Live Polling
  useEffect(() => {
    const token = api.getToken();
    if (!token) {
      navigate('/login', { replace: true });
      return;
    }

    loadInitialData();

    // 5-Second Real-Time Live Sync with Dalma Wildlife field units & mobile app
    const interval = setInterval(() => {
      refreshAllData();
    }, 5000);

    return () => clearInterval(interval);
  }, [navigate]);

  const loadInitialData = async () => {
    try {
      // Load current user profile
      const me = await api.getMe();
      setCurrentUser(me);

      // Fetch all primary dataset
      await refreshAllData();
    } catch (err: any) {
      console.error('Failed to load dashboard data:', err);
      if (err.message && err.message.includes('401')) {
        api.setToken(null);
        navigate('/login', { replace: true });
      }
    }
  };

  const refreshAllData = async () => {
    try {
      const [sum, usrs, incs, alrts, chals, sols, anlytcs] = await Promise.all([
        api.getSummary().catch(() => null),
        api.getUsers().catch(() => []),
        api.getIncidents().catch(() => []),
        api.getAlerts().catch(() => []),
        api.getChallenges().catch(() => []),
        api.getSolutions().catch(() => []),
        api.getAnalytics().catch(() => null),
      ]);

      if (sum) setSummary(sum);
      setUsers(usrs);
      setIncidents(incs);
      setAlerts(alrts);
      setChallenges(chals);
      setSolutions(sols);
      if (anlytcs) setAnalytics(anlytcs);
    } catch (error) {
      console.error('Error refreshing data:', error);
    }
  };

  const handleLogout = () => {
    api.setToken(null);
    navigate('/login', { replace: true });
  };

  // Handlers for status updates
  const handleUpdateIncidentStatus = async (
    incidentId: string,
    verificationStatus: VerificationStatus,
    responseStatus: ResponseStatus
  ) => {
    await api.updateIncident(incidentId, { verificationStatus, responseStatus });
    await refreshAllData();
    if (selectedIncident && selectedIncident.id === incidentId) {
      setSelectedIncident({
        ...selectedIncident,
        verificationStatus,
        responseStatus
      });
    }
  };

  const handleToggleUserStatus = async (userId: string, newStatus: string) => {
    await api.updateUserStatus(userId, newStatus);
    await refreshAllData();
    if (selectedUser && selectedUser.id === userId) {
      setSelectedUser({
        ...selectedUser,
        status: newStatus as any
      });
    }
  };

  const handleAcknowledgeAlert = async (alertId: string) => {
    await api.updateAlert(alertId, { status: 'Acknowledged' });
    await refreshAllData();
  };

  const handleCloseAlert = async (alertId: string) => {
    await api.updateAlert(alertId, { status: 'Closed' });
    await refreshAllData();
  };

  const handleCreateChallenge = async (challengeData: any) => {
    await api.createChallenge(challengeData);
    await refreshAllData();
  };

  const handleSubmitSolution = async (solutionData: any) => {
    await api.submitSolution(solutionData);
    await refreshAllData();
  };

  const handleCreateNewIncident = async (incidentData: any) => {
    await api.createIncident(incidentData);
    await refreshAllData();
  };

  // Filtered Users
  const filteredUsers = users.filter((u) => {
    const matchesSearch =
      userSearch === '' ||
      u.fullName.toLowerCase().includes(userSearch.toLowerCase()) ||
      u.email.toLowerCase().includes(userSearch.toLowerCase()) ||
      u.mobile.includes(userSearch);
    const matchesRole = userRoleFilter === 'ALL' || u.role === userRoleFilter;
    const matchesStatus = userStatusFilter === 'ALL' || u.status === userStatusFilter;
    return matchesSearch && matchesRole && matchesStatus;
  });

  // Filtered Incidents
  const filteredIncidents = incidents.filter((inc) => {
    const matchesThreat = incidentThreatFilter === 'ALL' || inc.threatLevel === incidentThreatFilter;
    const matchesStatus = incidentStatusFilter === 'ALL' || inc.verificationStatus === incidentStatusFilter;
    return matchesThreat && matchesStatus;
  });

  // Filtered Alerts
  const filteredAlerts = alerts.filter((alt) => {
    const matchesSeverity = alertSeverityFilter === 'ALL' || alt.severity === alertSeverityFilter;
    const matchesStatus = alertStatusFilter === 'ALL' || alt.status === alertStatusFilter;
    return matchesSeverity && matchesStatus;
  });

  const activeCriticalAlertsCount = alerts.filter(
    (a) => a.severity === 'CRITICAL' && a.status === 'Active'
  ).length;

  const openChallengesCount = challenges.filter(
    (c) => c.status !== 'Resolved'
  ).length;

  return (
    <div className="min-h-screen bg-[#060b18] text-slate-100 flex flex-col font-sans">
      
      {/* Top Government & Forest Department Header */}
      <AdminHeader
        user={currentUser}
        activeAlertsCount={activeCriticalAlertsCount}
        onLogout={handleLogout}
        onNavigateToAlerts={() => setActiveTab('alerts')}
      />

      {/* Main Layout: Sidebar + Content */}
      <div className="flex-1 flex max-w-[1700px] w-full mx-auto">
        
        {/* Navigation Sidebar */}
        <AdminSidebar
          activeTab={activeTab}
          onSelectTab={setActiveTab}
          criticalAlertsCount={activeCriticalAlertsCount}
          openChallengesCount={openChallengesCount}
        />

        {/* Dynamic Main Workspace */}
        <main className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto max-h-[calc(100vh-4rem)]">
          
          {/* Header Bar within Tab */}
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
            <div>
              <h1 className="text-xl sm:text-2xl font-bold text-slate-100 tracking-tight capitalize">
                {activeTab === 'home' && 'Dashboard Overview'}
                {activeTab === 'users' && 'Registered Users & Officer Accounts'}
                {activeTab === 'incidents' && 'Elephant Sighting & Incident Reports'}
                {activeTab === 'map' && 'Real-Time Spatial Corridor Map'}
                {activeTab === 'alerts' && 'Proximity & Broadcast Alert Management'}
                {activeTab === 'challenges' && 'SIH26043 Crowdsourced Challenges'}
                {activeTab === 'collaboration' && 'University & Industry Collaboration'}
                {activeTab === 'analytics' && 'Wildfire & Incident Analytics'}
              </h1>
              <p className="text-xs text-slate-400 font-mono">
                Dalma Wildlife Division • Active Monitoring Grid
              </p>
            </div>

            <div className="flex items-center gap-2.5">
              <div className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-emerald-950/60 border border-emerald-800 text-emerald-400 font-mono text-[11px]">
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                <span>LIVE RADAR SYNC</span>
              </div>

              <button
                onClick={refreshAllData}
                className="p-2 rounded-xl bg-slate-900 border border-slate-700 text-slate-300 hover:text-white hover:bg-slate-800 transition-colors flex items-center gap-1.5 text-xs font-mono"
                title="Refresh Live Data"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">Sync</span>
              </button>

              {activeTab === 'users' && (
                <button
                  onClick={() => setIsAddUserModalOpen(true)}
                  className="px-3.5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-xs flex items-center gap-1.5 shadow-md transition-colors"
                >
                  <UserPlus className="w-4 h-4" />
                  <span>Register Personnel</span>
                </button>
              )}

              {activeTab === 'incidents' && (
                <button
                  onClick={() => {
                    setNewIncidentCoords(null);
                    setIsNewIncidentModalOpen(true);
                  }}
                  className="px-3.5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-xs flex items-center gap-1.5 shadow-md transition-colors"
                >
                  <Plus className="w-4 h-4" />
                  <span>Log Sighting</span>
                </button>
              )}

              {activeTab === 'challenges' && (
                <button
                  onClick={() => {
                    setChallengeIncidentContext(null);
                    setIsCreateChallengeOpen(true);
                  }}
                  className="px-3.5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs flex items-center gap-1.5 shadow-md transition-colors"
                >
                  <Sparkles className="w-4 h-4" />
                  <span>Crowdsource Challenge</span>
                </button>
              )}
            </div>
          </div>

          {/* ============================================================= */}
          {/* 1. DASHBOARD HOME VIEW */}
          {/* ============================================================= */}
          {activeTab === 'home' && (
            <div className="space-y-6">
              
              {/* Top 6 KPI Stat Cards */}
              <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3.5">
                <div className="p-4 rounded-2xl bg-[#0d162e] border border-slate-800">
                  <div className="text-[11px] font-mono text-slate-400 uppercase">Total Users</div>
                  <div className="text-2xl font-bold text-slate-100 mt-1">
                    {summary?.totalUsers ?? users.length}
                  </div>
                  <div className="text-[10px] text-slate-400 font-mono mt-1">Registered in System</div>
                </div>

                <div className="p-4 rounded-2xl bg-[#0d162e] border border-slate-800">
                  <div className="text-[11px] font-mono text-slate-400 uppercase">Active Users</div>
                  <div className="text-2xl font-bold text-emerald-400 mt-1">
                    {summary?.activeUsers ?? users.filter(u => u.status === 'ACTIVE').length}
                  </div>
                  <div className="text-[10px] text-emerald-400/70 font-mono mt-1">Active Accounts</div>
                </div>

                <div className="p-4 rounded-2xl bg-[#0d162e] border border-slate-800">
                  <div className="text-[11px] font-mono text-slate-400 uppercase">Total Incidents</div>
                  <div className="text-2xl font-bold text-slate-100 mt-1">
                    {summary?.totalIncidents ?? incidents.length}
                  </div>
                  <div className="text-[10px] text-slate-400 font-mono mt-1">Logged Sightings</div>
                </div>

                <div className="p-4 rounded-2xl bg-[#0d162e] border border-slate-800">
                  <div className="text-[11px] font-mono text-slate-400 uppercase">Verified</div>
                  <div className="text-2xl font-bold text-cyan-400 mt-1">
                    {summary?.verifiedIncidents ?? incidents.filter(i => i.verificationStatus === 'Verified').length}
                  </div>
                  <div className="text-[10px] text-cyan-400/70 font-mono mt-1">Officer Confirmed</div>
                </div>

                <div className="p-4 rounded-2xl bg-[#0d162e] border border-slate-800">
                  <div className="text-[11px] font-mono text-slate-400 uppercase">Critical Alerts</div>
                  <div className="text-2xl font-bold text-rose-400 mt-1">
                    {summary?.activeCriticalAlerts ?? activeCriticalAlertsCount}
                  </div>
                  <div className="text-[10px] text-rose-400/70 font-mono mt-1">Active Broadcasts</div>
                </div>

                <div className="p-4 rounded-2xl bg-[#0d162e] border border-slate-800">
                  <div className="text-[11px] font-mono text-slate-400 uppercase">Resolved</div>
                  <div className="text-2xl font-bold text-teal-400 mt-1">
                    {summary?.resolvedIncidents ?? incidents.filter(i => i.verificationStatus === 'Resolved' || i.responseStatus === 'Resolved').length}
                  </div>
                  <div className="text-[10px] text-teal-400/70 font-mono mt-1">Corridor Cleared</div>
                </div>
              </div>

              {/* Middle Section: Quick Interactive Map Snippet + Recent Activity */}
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                
                {/* Left 7 Cols: Interactive Map Preview */}
                <div className="lg:col-span-7 bg-[#0d162e] border border-slate-800 rounded-3xl p-5 space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <MapPin className="w-4 h-4 text-emerald-400" />
                      <h2 className="text-sm font-bold text-slate-100 font-sans">
                        Live Corridor Radar Map
                      </h2>
                    </div>
                    <button
                      onClick={() => setActiveTab('map')}
                      className="text-xs font-mono text-emerald-400 hover:text-emerald-300 flex items-center gap-1"
                    >
                      <span>Full Map</span>
                      <ArrowUpRight className="w-3.5 h-3.5" />
                    </button>
                  </div>

                  <RealTimeMap
                    incidents={incidents}
                    alerts={alerts}
                    onSelectIncident={(inc) => {
                      setSelectedIncident(inc);
                      setIsIncidentModalOpen(true);
                    }}
                    onRequestNewIncident={(coords) => {
                      setNewIncidentCoords(coords);
                      setIsNewIncidentModalOpen(true);
                    }}
                  />
                </div>

                {/* Right 5 Cols: Recent Activity Stream & Quick Actions */}
                <div className="lg:col-span-5 space-y-6">
                  
                  {/* Recent Activity List */}
                  <div className="bg-[#0d162e] border border-slate-800 rounded-3xl p-5 space-y-4">
                    <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                      <div className="flex items-center gap-2">
                        <Activity className="w-4 h-4 text-cyan-400" />
                        <h2 className="text-sm font-bold text-slate-100 font-sans">
                          Recent Corridor Activity
                        </h2>
                      </div>
                      <span className="text-[10px] font-mono text-slate-400">Live Feed</span>
                    </div>

                    <div className="space-y-3 max-h-[380px] overflow-y-auto pr-1">
                      {summary?.recentActivity && summary.recentActivity.length > 0 ? (
                        summary.recentActivity.map((act) => (
                          <div
                            key={act.id}
                            className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 text-xs space-y-1 hover:border-slate-700 transition-colors"
                          >
                            <div className="flex items-center justify-between">
                              <span className="font-semibold text-slate-200">{act.actor}</span>
                              <span
                                className={`text-[10px] px-2 py-0.5 rounded font-mono font-bold ${
                                  act.severity === 'CRITICAL'
                                    ? 'bg-rose-950 text-rose-400 border border-rose-800'
                                    : act.severity === 'HIGH'
                                    ? 'bg-amber-950 text-amber-400 border border-amber-800'
                                    : 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                                }`}
                              >
                                {act.severity}
                              </span>
                            </div>
                            <p className="text-slate-400 text-[11px] leading-relaxed line-clamp-2">
                              {act.details}
                            </p>
                            <div className="text-[10px] text-slate-400 font-mono pt-1">
                              {act.timestamp}
                            </div>
                          </div>
                        ))
                      ) : (
                        <div className="text-center py-8 text-xs text-slate-400 font-mono">
                          No recent incident activity recorded.
                        </div>
                      )}
                    </div>
                  </div>

                  {/* SIH26043 Problem Solving Shortcut Banner */}
                  <div className="bg-gradient-to-br from-indigo-950/60 to-slate-900 border border-indigo-800/40 rounded-3xl p-5 space-y-3">
                    <div className="flex items-center gap-2 text-indigo-400 text-xs font-mono font-bold">
                      <Sparkles className="w-4 h-4" />
                      <span>SIH26043 Problem Solving Matrix</span>
                    </div>
                    <p className="text-xs text-slate-300 leading-relaxed">
                      Transform recurring elephant-human corridor conflict data into open societal challenges for academic researchers & industry partners.
                    </p>
                    <button
                      onClick={() => setActiveTab('challenges')}
                      className="w-full py-2 px-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs transition-colors flex items-center justify-center gap-1.5"
                    >
                      <span>Explore Active Challenges ({challenges.length})</span>
                      <ArrowUpRight className="w-3.5 h-3.5" />
                    </button>
                  </div>

                </div>

              </div>

            </div>
          )}

          {/* ============================================================= */}
          {/* 2. REGISTERED USERS MANAGEMENT */}
          {/* ============================================================= */}
          {activeTab === 'users' && (
            <div className="space-y-4">
              
              {/* Personnel Summary Stats */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3.5">
                <div className="p-4 rounded-2xl bg-[#0d162e] border border-slate-800 flex items-center justify-between">
                  <div>
                    <div className="text-[10px] font-mono text-slate-400 uppercase">Total Personnel</div>
                    <div className="text-2xl font-bold text-slate-100 mt-0.5">{users.length}</div>
                    <div className="text-[10px] text-emerald-400 font-mono">Active Accounts</div>
                  </div>
                  <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-emerald-400">
                    <UserPlus className="w-5 h-5" />
                  </div>
                </div>

                <div className="p-4 rounded-2xl bg-[#0d162e] border border-slate-800 flex items-center justify-between">
                  <div>
                    <div className="text-[10px] font-mono text-slate-400 uppercase">Forest Officers / QRT</div>
                    <div className="text-2xl font-bold text-cyan-400 mt-0.5">
                      {users.filter(u => u.role === 'Forest Officer').length}
                    </div>
                    <div className="text-[10px] text-slate-400 font-mono">Range & Patrol Units</div>
                  </div>
                  <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-cyan-400">
                    <Activity className="w-5 h-5" />
                  </div>
                </div>

                <div className="p-4 rounded-2xl bg-[#0d162e] border border-slate-800 flex items-center justify-between">
                  <div>
                    <div className="text-[10px] font-mono text-slate-400 uppercase">Van Suraksha Guards</div>
                    <div className="text-2xl font-bold text-teal-400 mt-0.5">
                      {users.filter(u => u.role === 'Community Guard').length}
                    </div>
                    <div className="text-[10px] text-slate-400 font-mono">Village Fringe Grid</div>
                  </div>
                  <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-teal-400">
                    <MapPin className="w-5 h-5" />
                  </div>
                </div>

                <div className="p-4 rounded-2xl bg-[#0d162e] border border-slate-800 flex items-center justify-between">
                  <div>
                    <div className="text-[10px] font-mono text-slate-400 uppercase">HQ Administrators</div>
                    <div className="text-2xl font-bold text-indigo-400 mt-0.5">
                      {users.filter(u => u.role === 'Administrator').length}
                    </div>
                    <div className="text-[10px] text-slate-400 font-mono">Command Level</div>
                  </div>
                  <button
                    onClick={() => setIsAddUserModalOpen(true)}
                    className="p-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white shadow-md transition-colors"
                    title="Register New Personnel"
                  >
                    <Plus className="w-5 h-5" />
                  </button>
                </div>
              </div>

              {/* Search & Filters */}
              <div className="bg-[#0d162e] border border-slate-800 rounded-2xl p-4 flex flex-col sm:flex-row gap-3 items-center justify-between">
                <div className="relative w-full sm:w-80">
                  <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" />
                  <input
                    type="text"
                    value={userSearch}
                    onChange={(e) => setUserSearch(e.target.value)}
                    placeholder="Search by name, email, or mobile..."
                    className="w-full bg-slate-950 border border-slate-700/80 rounded-xl pl-10 pr-4 py-2 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-emerald-500"
                  />
                </div>

                <div className="flex items-center gap-3 w-full sm:w-auto">
                  <select
                    value={userRoleFilter}
                    onChange={(e) => setUserRoleFilter(e.target.value)}
                    className="bg-slate-950 border border-slate-700/80 rounded-xl px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-emerald-500"
                  >
                    <option value="ALL">All Roles</option>
                    <option value="Administrator">Administrator</option>
                    <option value="Forest Officer">Forest Officer</option>
                    <option value="Citizen">Citizen</option>
                    <option value="Community Guard">Community Guard</option>
                  </select>

                  <select
                    value={userStatusFilter}
                    onChange={(e) => setUserStatusFilter(e.target.value)}
                    className="bg-slate-950 border border-slate-700/80 rounded-xl px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-emerald-500"
                  >
                    <option value="ALL">All Statuses</option>
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="SUSPENDED">SUSPENDED</option>
                    <option value="DEACTIVATED">DEACTIVATED</option>
                  </select>
                </div>
              </div>

              {/* Users Table */}
              <div className="bg-[#0d162e] border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs text-slate-300">
                    <thead className="bg-slate-900/90 text-slate-400 font-mono uppercase text-[11px] border-b border-slate-800">
                      <tr>
                        <th className="px-5 py-3.5">Full Name</th>
                        <th className="px-5 py-3.5">Mobile Number</th>
                        <th className="px-5 py-3.5">Email Address</th>
                        <th className="px-5 py-3.5">Role</th>
                        <th className="px-5 py-3.5">Status</th>
                        <th className="px-5 py-3.5">Registered</th>
                        <th className="px-5 py-3.5">Last Active</th>
                        <th className="px-5 py-3.5 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-800/80">
                      {filteredUsers.length > 0 ? (
                        filteredUsers.map((u) => (
                          <tr key={u.id} className="hover:bg-slate-800/40 transition-colors">
                            <td className="px-5 py-3.5 font-semibold text-slate-100 flex items-center gap-2">
                              <span>{u.fullName}</span>
                            </td>
                            <td className="px-5 py-3.5 font-mono text-slate-300">{u.mobile}</td>
                            <td className="px-5 py-3.5 text-slate-300">{u.email}</td>
                            <td className="px-5 py-3.5">
                              <span className="px-2 py-0.5 rounded bg-slate-800 border border-slate-700 text-slate-300 font-mono text-[11px]">
                                {u.role}
                              </span>
                            </td>
                            <td className="px-5 py-3.5">
                              <span
                                className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold ${
                                  u.status === 'ACTIVE'
                                    ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                                    : 'bg-rose-950 text-rose-400 border border-rose-800'
                                }`}
                              >
                                {u.status}
                              </span>
                            </td>
                            <td className="px-5 py-3.5 text-slate-400 font-mono text-[11px]">
                              {u.createdAt}
                            </td>
                            <td className="px-5 py-3.5 text-slate-400 font-mono text-[11px]">
                              {u.lastActive}
                            </td>
                            <td className="px-5 py-3.5 text-right space-x-2">
                              <button
                                onClick={() => {
                                  setSelectedUser(u);
                                  setIsUserModalOpen(true);
                                }}
                                className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white font-medium text-[11px] transition-colors"
                              >
                                View Details
                              </button>
                            </td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td colSpan={8} className="px-5 py-12 text-center text-slate-400 font-mono">
                            No registered users matching the selected filters.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>

            </div>
          )}

          {/* ============================================================= */}
          {/* 3. INCIDENT REPORTS */}
          {/* ============================================================= */}
          {activeTab === 'incidents' && (
            <div className="space-y-4">
              
              {/* Filter Bar */}
              <div className="bg-[#0d162e] border border-slate-800 rounded-2xl p-4 flex flex-wrap gap-3 items-center justify-between">
                <div className="flex items-center gap-3">
                  <span className="text-xs font-mono text-slate-400 uppercase">Filters:</span>
                  <select
                    value={incidentThreatFilter}
                    onChange={(e) => setIncidentThreatFilter(e.target.value)}
                    className="bg-slate-950 border border-slate-700/80 rounded-xl px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-emerald-500"
                  >
                    <option value="ALL">All Threat Levels</option>
                    <option value="CRITICAL">CRITICAL</option>
                    <option value="HIGH">HIGH</option>
                    <option value="CAUTION">CAUTION</option>
                    <option value="SAFE">SAFE</option>
                  </select>

                  <select
                    value={incidentStatusFilter}
                    onChange={(e) => setIncidentStatusFilter(e.target.value)}
                    className="bg-slate-950 border border-slate-700/80 rounded-xl px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-emerald-500"
                  >
                    <option value="ALL">All Verification Statuses</option>
                    <option value="New">New</option>
                    <option value="Under Review">Under Review</option>
                    <option value="Verified">Verified</option>
                    <option value="Dispatched">Dispatched</option>
                    <option value="Resolved">Resolved</option>
                    <option value="Rejected">Rejected</option>
                  </select>
                </div>

                <div className="text-xs font-mono text-slate-400">
                  Total Reports: <strong className="text-slate-200">{filteredIncidents.length}</strong>
                </div>
              </div>

              {/* Incidents Table */}
              <div className="bg-[#0d162e] border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs text-slate-300">
                    <thead className="bg-slate-900/90 text-slate-400 font-mono uppercase text-[11px] border-b border-slate-800">
                      <tr>
                        <th className="px-4 py-3.5">Incident ID</th>
                        <th className="px-4 py-3.5">Reporter</th>
                        <th className="px-4 py-3.5">Date & Time</th>
                        <th className="px-4 py-3.5">GPS Coordinates</th>
                        <th className="px-4 py-3.5">Herd Count</th>
                        <th className="px-4 py-3.5">AI Confidence</th>
                        <th className="px-4 py-3.5">Threat Level</th>
                        <th className="px-4 py-3.5">Verification</th>
                        <th className="px-4 py-3.5">Response</th>
                        <th className="px-4 py-3.5 text-right">Inspect</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-800/80">
                      {filteredIncidents.length > 0 ? (
                        filteredIncidents.map((inc) => (
                          <tr key={inc.id} className="hover:bg-slate-800/40 transition-colors">
                            <td className="px-4 py-3.5 font-mono font-bold text-slate-100">
                              {inc.id}
                            </td>
                            <td className="px-4 py-3.5 text-slate-200 font-medium">
                              {inc.reporterName}
                            </td>
                            <td className="px-4 py-3.5 font-mono text-slate-400 text-[11px]">
                              {inc.timestamp}
                            </td>
                            <td className="px-4 py-3.5 font-mono text-slate-300 text-[11px]">
                              {inc.latitude.toFixed(4)} N, {inc.longitude.toFixed(4)} E
                            </td>
                            <td className="px-4 py-3.5 font-bold text-slate-100">
                              {inc.elephantCount}
                            </td>
                            <td className="px-4 py-3.5 text-cyan-400 font-mono">
                              {(inc.confidence * 100).toFixed(0)}%
                            </td>
                            <td className="px-4 py-3.5">
                              <span
                                className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold ${
                                  inc.threatLevel === 'CRITICAL'
                                    ? 'bg-rose-950 text-rose-400 border border-rose-800'
                                    : inc.threatLevel === 'HIGH'
                                    ? 'bg-amber-950 text-amber-400 border border-amber-800'
                                    : inc.threatLevel === 'CAUTION'
                                    ? 'bg-yellow-950 text-yellow-400 border border-yellow-800'
                                    : 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                                }`}
                              >
                                {inc.threatLevel}
                              </span>
                            </td>
                            <td className="px-4 py-3.5">
                              <span className="px-2 py-0.5 rounded bg-slate-800 border border-slate-700 text-slate-300 font-mono text-[11px]">
                                {inc.verificationStatus}
                              </span>
                            </td>
                            <td className="px-4 py-3.5">
                              <span className="px-2 py-0.5 rounded bg-slate-800 border border-slate-700 text-slate-300 font-mono text-[11px]">
                                {inc.responseStatus}
                              </span>
                            </td>
                            <td className="px-4 py-3.5 text-right">
                              <button
                                onClick={() => {
                                  setSelectedIncident(inc);
                                  setIsIncidentModalOpen(true);
                                }}
                                className="px-3 py-1 rounded-lg bg-emerald-950/80 hover:bg-emerald-900 border border-emerald-800 text-emerald-300 text-xs font-medium transition-colors"
                              >
                                View Details
                              </button>
                            </td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td colSpan={10} className="px-5 py-12 text-center text-slate-400 font-mono">
                            No incident reports match the current filters.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>

            </div>
          )}

          {/* ============================================================= */}
          {/* 4. REAL-TIME MAP */}
          {/* ============================================================= */}
          {activeTab === 'map' && (
            <div className="space-y-4">
              <div className="bg-[#0d162e] border border-slate-800 rounded-2xl p-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2">
                <div className="flex items-center gap-2 text-xs font-mono text-slate-300">
                  <MapPin className="w-4 h-4 text-emerald-400" />
                  <span>Dalma Wildlife Sanctuary GIS Grid • High-Resolution Satellite & Live Edge Sightings</span>
                </div>
                <div className="flex items-center gap-3 text-xs font-mono">
                  <span className="text-emerald-400 font-bold">{incidents.length} Active GPS Markers Rendered</span>
                  <button
                    onClick={() => {
                      setNewIncidentCoords(null);
                      setIsNewIncidentModalOpen(true);
                    }}
                    className="px-3 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-xs flex items-center gap-1.5 shadow-md transition-colors"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    <span>Log Sighting</span>
                  </button>
                </div>
              </div>

              <RealTimeMap
                incidents={incidents}
                alerts={alerts}
                onSelectIncident={(inc) => {
                  setSelectedIncident(inc);
                  setIsIncidentModalOpen(true);
                }}
                onRequestNewIncident={(coords) => {
                  setNewIncidentCoords(coords);
                  setIsNewIncidentModalOpen(true);
                }}
              />
            </div>
          )}

          {/* ============================================================= */}
          {/* 5. ALERT MANAGEMENT */}
          {/* ============================================================= */}
          {activeTab === 'alerts' && (
            <div className="space-y-4">
              
              {/* Filter Bar */}
              <div className="bg-[#0d162e] border border-slate-800 rounded-2xl p-4 flex flex-wrap gap-3 items-center justify-between">
                <div className="flex items-center gap-3">
                  <span className="text-xs font-mono text-slate-400 uppercase">Filters:</span>
                  <select
                    value={alertSeverityFilter}
                    onChange={(e) => setAlertSeverityFilter(e.target.value)}
                    className="bg-slate-950 border border-slate-700/80 rounded-xl px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-emerald-500"
                  >
                    <option value="ALL">All Severities</option>
                    <option value="CRITICAL">CRITICAL</option>
                    <option value="HIGH">HIGH</option>
                    <option value="CAUTION">CAUTION</option>
                    <option value="SAFE">SAFE</option>
                  </select>

                  <select
                    value={alertStatusFilter}
                    onChange={(e) => setAlertStatusFilter(e.target.value)}
                    className="bg-slate-950 border border-slate-700/80 rounded-xl px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-emerald-500"
                  >
                    <option value="ALL">All Statuses</option>
                    <option value="Active">Active</option>
                    <option value="Acknowledged">Acknowledged</option>
                    <option value="Closed">Closed</option>
                  </select>
                </div>

                <div className="text-xs font-mono text-slate-400">
                  Total Alerts: <strong className="text-slate-200">{filteredAlerts.length}</strong>
                </div>
              </div>

              {/* Alerts Table */}
              <div className="bg-[#0d162e] border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs text-slate-300">
                    <thead className="bg-slate-900/90 text-slate-400 font-mono uppercase text-[11px] border-b border-slate-800">
                      <tr>
                        <th className="px-5 py-3.5">Alert ID</th>
                        <th className="px-5 py-3.5">Severity</th>
                        <th className="px-5 py-3.5">Detection Location</th>
                        <th className="px-5 py-3.5">Distance</th>
                        <th className="px-5 py-3.5">Users Notified</th>
                        <th className="px-5 py-3.5">Created At</th>
                        <th className="px-5 py-3.5">Status</th>
                        <th className="px-5 py-3.5 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-800/80">
                      {filteredAlerts.length > 0 ? (
                        filteredAlerts.map((alt) => (
                          <tr key={alt.id} className="hover:bg-slate-800/40 transition-colors">
                            <td className="px-5 py-3.5 font-mono font-bold text-slate-100">{alt.id}</td>
                            <td className="px-5 py-3.5">
                              <span
                                className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold ${
                                  alt.severity === 'CRITICAL'
                                    ? 'bg-rose-950 text-rose-400 border border-rose-800'
                                    : alt.severity === 'HIGH'
                                    ? 'bg-amber-950 text-amber-400 border border-amber-800'
                                    : 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                                }`}
                              >
                                {alt.severity}
                              </span>
                            </td>
                            <td className="px-5 py-3.5 text-slate-200">{alt.locationText}</td>
                            <td className="px-5 py-3.5 font-mono text-slate-300">{alt.distanceKm} km</td>
                            <td className="px-5 py-3.5 font-bold text-cyan-400">{alt.notifiedCount} Users</td>
                            <td className="px-5 py-3.5 font-mono text-slate-400 text-[11px]">{alt.createdAt}</td>
                            <td className="px-5 py-3.5">
                              <span
                                className={`px-2 py-0.5 rounded font-mono text-[10px] ${
                                  alt.status === 'Active'
                                    ? 'bg-rose-950/80 text-rose-300 border border-rose-800 font-bold'
                                    : alt.status === 'Acknowledged'
                                    ? 'bg-amber-950/80 text-amber-300 border border-amber-800'
                                    : 'bg-slate-800 text-slate-400 border border-slate-700'
                                }`}
                              >
                                {alt.status}
                              </span>
                            </td>
                            <td className="px-5 py-3.5 text-right space-x-2">
                              {alt.status === 'Active' && (
                                <button
                                  onClick={() => handleAcknowledgeAlert(alt.id)}
                                  className="px-2.5 py-1 rounded bg-amber-950 hover:bg-amber-900 border border-amber-800 text-amber-300 text-[11px] font-medium"
                                >
                                  Acknowledge
                                </button>
                              )}
                              {alt.status !== 'Closed' && (
                                <button
                                  onClick={() => handleCloseAlert(alt.id)}
                                  className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 text-[11px] font-medium"
                                >
                                  Close
                                </button>
                              )}
                            </td>
                          </tr>
                        ))
                      ) : (
                        <tr>
                          <td colSpan={8} className="px-5 py-12 text-center text-slate-400 font-mono">
                            No alerts matching the selected filters.
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>

            </div>
          )}

          {/* ============================================================= */}
          {/* 6. CROWDSOURCED CHALLENGES (SIH26043) */}
          {/* ============================================================= */}
          {activeTab === 'challenges' && (
            <div className="space-y-6">
              
              {/* SIH Banner */}
              <div className="p-5 rounded-2xl bg-gradient-to-r from-indigo-950/70 via-slate-900 to-indigo-950/40 border border-indigo-800/40 space-y-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2 text-indigo-400 font-mono text-xs font-bold uppercase">
                    <Sparkles className="w-4 h-4" />
                    <span>Smart India Hackathon SIH26043 Workflow</span>
                  </div>
                  <span className="text-[11px] font-mono text-slate-400">Citizen Reports → Societal Solutions</span>
                </div>
                <p className="text-xs text-slate-300 leading-relaxed max-w-3xl">
                  «A digital platform to crowdsource societal challenges and facilitate collaborative problem solving through universities and industry partnerships.»
                </p>
                <div className="pt-2 flex flex-wrap items-center gap-2 text-[10px] font-mono text-slate-400">
                  <span className="px-2 py-0.5 rounded bg-slate-800 border border-slate-700">1. Citizen Report</span>
                  <span>→</span>
                  <span className="px-2 py-0.5 rounded bg-slate-800 border border-slate-700">2. Forest Verification</span>
                  <span>→</span>
                  <span className="px-2 py-0.5 rounded bg-indigo-950 text-indigo-300 border border-indigo-700">3. Societal Challenge</span>
                  <span>→</span>
                  <span className="px-2 py-0.5 rounded bg-teal-950 text-teal-300 border border-teal-700">4. University/Industry Solution</span>
                  <span>→</span>
                  <span className="px-2 py-0.5 rounded bg-emerald-950 text-emerald-300 border border-emerald-700">5. Field Implementation</span>
                </div>
              </div>

              {/* Challenges Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {challenges.length > 0 ? (
                  challenges.map((chal) => (
                    <div
                      key={chal.id}
                      className="bg-[#0d162e] border border-slate-800 rounded-2xl p-5 space-y-4 hover:border-slate-700 transition-all flex flex-col justify-between"
                    >
                      <div className="space-y-2.5">
                        <div className="flex items-center justify-between">
                          <span className="font-mono font-bold text-xs text-indigo-400">
                            {chal.id}
                          </span>
                          <span
                            className={`px-2 py-0.5 rounded text-[10px] font-mono font-bold ${
                              chal.riskLevel === 'CRITICAL'
                                ? 'bg-rose-950 text-rose-400 border border-rose-800'
                                : 'bg-amber-950 text-amber-400 border border-amber-800'
                            }`}
                          >
                            {chal.riskLevel}
                          </span>
                        </div>

                        <h3 className="font-bold text-slate-100 text-sm leading-snug">
                          {chal.title}
                        </h3>

                        <p className="text-slate-300 text-xs leading-relaxed line-clamp-3">
                          {chal.description}
                        </p>

                        <div className="text-[11px] text-slate-400 flex items-center gap-1.5">
                          <MapPin className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                          <span className="truncate">{chal.location}</span>
                        </div>
                      </div>

                      <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-xs">
                        <div className="space-y-1">
                          <div className="text-[10px] text-slate-400 font-mono">Current Status:</div>
                          <span className="px-2.5 py-0.5 rounded-full bg-slate-800 text-slate-200 border border-slate-700 font-mono text-[10px] font-semibold">
                            {chal.status}
                          </span>
                        </div>

                        <button
                          onClick={() => {
                            setTargetChallengeForSolution(chal);
                            setIsCreateSolutionOpen(true);
                          }}
                          className="px-3.5 py-1.5 rounded-xl bg-teal-600 hover:bg-teal-500 text-white font-semibold text-xs transition-colors flex items-center gap-1"
                        >
                          <Building2 className="w-3.5 h-3.5" />
                          <span>Propose Solution</span>
                        </button>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="col-span-2 text-center py-12 text-slate-400 font-mono text-xs bg-[#0d162e] rounded-2xl border border-slate-800">
                    No active societal challenges logged. Click "Crowdsource Challenge" to create one.
                  </div>
                )}
              </div>

            </div>
          )}

          {/* ============================================================= */}
          {/* 7. UNIVERSITY & INDUSTRY COLLABORATION */}
          {/* ============================================================= */}
          {activeTab === 'collaboration' && (
            <div className="space-y-4">
              
              <div className="bg-[#0d162e] border border-slate-800 rounded-2xl p-4 flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs font-mono text-slate-300">
                  <Building2 className="w-4 h-4 text-teal-400" />
                  <span>Academic & Industry Solution Registry (IITs, NITs, Wildlife Tech Firms)</span>
                </div>
                <div className="text-xs font-mono text-teal-400">
                  {solutions.length} Proposals Active
                </div>
              </div>

              {/* Solutions Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {solutions.length > 0 ? (
                  solutions.map((sol) => (
                    <div
                      key={sol.id}
                      className="bg-[#0d162e] border border-slate-800 rounded-2xl p-5 space-y-3.5 hover:border-slate-700 transition-all flex flex-col justify-between"
                    >
                      <div className="space-y-2">
                        <div className="flex items-center justify-between">
                          <span className="font-mono font-bold text-xs text-teal-400">{sol.id}</span>
                          <span className="px-2 py-0.5 rounded text-[10px] font-mono font-semibold bg-slate-800 border border-slate-700 text-slate-300">
                            {sol.organizationType}
                          </span>
                        </div>

                        <h3 className="font-bold text-slate-100 text-sm">
                          {sol.organization}
                        </h3>

                        <div className="text-[11px] text-indigo-300 font-mono line-clamp-1">
                          Assigned: {sol.challengeTitle || sol.challengeId}
                        </div>

                        <p className="text-slate-300 text-xs leading-relaxed">
                          {sol.description}
                        </p>
                      </div>

                      <div className="pt-3 border-t border-slate-800/80 space-y-2 text-xs">
                        <div className="flex items-center justify-between">
                          <div className="text-[11px] text-slate-400 font-mono">
                            Stage: <strong className="text-teal-300">{sol.status}</strong>
                          </div>
                          {sol.prototypeUrl && (
                            <a
                              href={sol.prototypeUrl}
                              target="_blank"
                              rel="noreferrer"
                              className="text-[11px] text-cyan-400 hover:underline font-mono"
                            >
                              View Demo ↗
                            </a>
                          )}
                        </div>

                        <div className="text-[10px] text-slate-400 font-mono flex items-center justify-between">
                          <span>Contact: {sol.contactEmail}</span>
                          {sol.contactPhone && <span>{sol.contactPhone}</span>}
                        </div>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="col-span-2 text-center py-12 text-slate-400 font-mono text-xs bg-[#0d162e] rounded-2xl border border-slate-800">
                    No solutions registered yet. Propose solutions on open challenges.
                  </div>
                )}
              </div>

            </div>
          )}

          {/* ============================================================= */}
          {/* 8. ANALYTICS */}
          {/* ============================================================= */}
          {activeTab === 'analytics' && (
            <div className="space-y-6">
              
              {analytics && analytics.hasData ? (
                <>
                  {/* Top Stats Overview */}
                  <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                    
                    {/* Threat Distribution Card */}
                    <div className="bg-[#0d162e] border border-slate-800 rounded-2xl p-5 space-y-3">
                      <div className="flex items-center justify-between border-b border-slate-800 pb-2">
                        <span className="font-bold text-xs uppercase font-mono text-slate-300">
                          Risk Level Breakdown
                        </span>
                        <BarChart3 className="w-4 h-4 text-rose-400" />
                      </div>
                      <div className="space-y-2">
                        {Object.entries(analytics.threatDistribution).map(([level, count]) => (
                          <div key={level} className="space-y-1">
                            <div className="flex justify-between text-xs font-mono">
                              <span className="text-slate-300">{level}</span>
                              <span className="font-bold text-slate-100">{count}</span>
                            </div>
                            <div className="w-full h-2 rounded-full bg-slate-950 overflow-hidden">
                              <div
                                className={`h-full rounded-full ${
                                  level === 'CRITICAL'
                                    ? 'bg-rose-500'
                                    : level === 'HIGH'
                                    ? 'bg-amber-500'
                                    : level === 'CAUTION'
                                    ? 'bg-yellow-500'
                                    : 'bg-emerald-500'
                                }`}
                                style={{ width: `${Math.min(100, count * 25)}%` }}
                              />
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* Verification Status Breakdown */}
                    <div className="bg-[#0d162e] border border-slate-800 rounded-2xl p-5 space-y-3">
                      <div className="flex items-center justify-between border-b border-slate-800 pb-2">
                        <span className="font-bold text-xs uppercase font-mono text-slate-300">
                          Verification Pipeline
                        </span>
                        <CheckCircle className="w-4 h-4 text-emerald-400" />
                      </div>
                      <div className="space-y-2">
                        {Object.entries(analytics.verificationDistribution).map(([stat, count]) => (
                          <div key={stat} className="space-y-1">
                            <div className="flex justify-between text-xs font-mono">
                              <span className="text-slate-300">{stat}</span>
                              <span className="font-bold text-slate-100">{count}</span>
                            </div>
                            <div className="w-full h-2 rounded-full bg-slate-950 overflow-hidden">
                              <div
                                className="h-full rounded-full bg-cyan-500"
                                style={{ width: `${Math.min(100, count * 30)}%` }}
                              />
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* SIH Solution Conversion */}
                    <div className="bg-[#0d162e] border border-slate-800 rounded-2xl p-5 space-y-3">
                      <div className="flex items-center justify-between border-b border-slate-800 pb-2">
                        <span className="font-bold text-xs uppercase font-mono text-slate-300">
                          SIH26043 Solution Funnel
                        </span>
                        <Sparkles className="w-4 h-4 text-indigo-400" />
                      </div>
                      <div className="space-y-3 text-xs">
                        <div className="flex justify-between font-mono">
                          <span className="text-slate-400">Crowdsourced Challenges:</span>
                          <strong className="text-indigo-400">{analytics.collaborationPipeline.challengesCrowdsourced}</strong>
                        </div>
                        <div className="flex justify-between font-mono">
                          <span className="text-slate-400">Solutions Registered:</span>
                          <strong className="text-teal-400">{analytics.collaborationPipeline.solutionsSubmitted}</strong>
                        </div>
                        <div className="flex justify-between font-mono">
                          <span className="text-slate-400">Field Tested / Implemented:</span>
                          <strong className="text-emerald-400">{analytics.collaborationPipeline.fieldTestingOrImplemented}</strong>
                        </div>
                      </div>
                    </div>

                  </div>

                  {/* Top Conflict Hotspots Table */}
                  <div className="bg-[#0d162e] border border-slate-800 rounded-2xl p-5 space-y-3">
                    <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                      <span className="font-bold text-xs uppercase font-mono text-slate-300">
                        Top Corridor Conflict Hotspots (GPS Clusters)
                      </span>
                      <span className="text-[10px] font-mono text-slate-400">Aggregated Spatial Data</span>
                    </div>

                    <div className="overflow-x-auto">
                      <table className="w-full text-left text-xs text-slate-300">
                        <thead className="bg-slate-900/90 text-slate-400 font-mono uppercase text-[11px]">
                          <tr>
                            <th className="px-4 py-2.5">Spatial Cluster</th>
                            <th className="px-4 py-2.5">Coordinates</th>
                            <th className="px-4 py-2.5">Incident Count</th>
                            <th className="px-4 py-2.5">Average Risk Score</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-800/60">
                          {analytics.topHotspots.map((hot, idx) => (
                            <tr key={idx} className="hover:bg-slate-800/30">
                              <td className="px-4 py-3 font-semibold text-slate-200">{hot.location}</td>
                              <td className="px-4 py-3 font-mono text-slate-400">{hot.latitude}° N, {hot.longitude}° E</td>
                              <td className="px-4 py-3 font-bold text-emerald-400">{hot.incidentCount} Sightings</td>
                              <td className="px-4 py-3 font-mono text-amber-400 font-bold">{hot.avgRisk} / 100</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                </>
              ) : (
                <div className="p-16 text-center bg-[#0d162e] border border-slate-800 rounded-2xl space-y-2">
                  <div className="text-base font-bold text-slate-300">No data available</div>
                  <p className="text-xs text-slate-400 font-mono">
                    Incident data will populate here as field reports and Android edge telemetry are synchronized.
                  </p>
                </div>
              )}

            </div>
          )}

        </main>

      </div>

      {/* Modals */}
      <IncidentDetailModal
        incident={selectedIncident}
        isOpen={isIncidentModalOpen}
        onClose={() => {
          setIsIncidentModalOpen(false);
          setSelectedIncident(null);
        }}
        onUpdateStatus={handleUpdateIncidentStatus}
        onCreateChallengeFromIncident={(inc) => {
          setChallengeIncidentContext(inc);
          setIsCreateChallengeOpen(true);
        }}
      />

      <CreateChallengeModal
        isOpen={isCreateChallengeOpen}
        initialIncident={challengeIncidentContext}
        onClose={() => {
          setIsCreateChallengeOpen(false);
          setChallengeIncidentContext(null);
        }}
        onSubmitChallenge={handleCreateChallenge}
      />

      <CreateSolutionModal
        isOpen={isCreateSolutionOpen}
        challenge={targetChallengeForSolution}
        onClose={() => {
          setIsCreateSolutionOpen(false);
          setTargetChallengeForSolution(null);
        }}
        onSubmitSolution={handleSubmitSolution}
      />

      <NewIncidentModal
        isOpen={isNewIncidentModalOpen}
        initialCoords={newIncidentCoords}
        onClose={() => {
          setIsNewIncidentModalOpen(false);
          setNewIncidentCoords(null);
        }}
        onSubmitIncident={handleCreateNewIncident}
      />

      <UserDetailModal
        user={selectedUser}
        isOpen={isUserModalOpen}
        onClose={() => {
          setIsUserModalOpen(false);
          setSelectedUser(null);
        }}
        onToggleStatus={handleToggleUserStatus}
      />

      <AddUserModal
        isOpen={isAddUserModalOpen}
        onClose={() => setIsAddUserModalOpen(false)}
        onUserCreated={refreshAllData}
      />

    </div>
  );
};
