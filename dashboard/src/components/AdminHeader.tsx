import React from 'react';
import { Shield, Bell, LogOut, User } from 'lucide-react';
import { UserRecord } from '../types';

interface AdminHeaderProps {
  user: UserRecord | null;
  activeAlertsCount: number;
  onLogout: () => void;
  onNavigateToAlerts?: () => void;
}

export const AdminHeader: React.FC<AdminHeaderProps> = ({
  user,
  activeAlertsCount,
  onLogout,
  onNavigateToAlerts
}) => {
  return (
    <header className="bg-[#0b1329] border-b border-slate-800 text-slate-100 sticky top-0 z-40 shadow-lg">
      <div className="max-w-[1700px] mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        
        {/* Brand / Emblem */}
        <div className="flex items-center gap-3.5">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-emerald-600 to-teal-800 flex items-center justify-center shadow-md border border-emerald-500/40">
            <Shield className="w-5 h-5 text-white" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-base sm:text-lg tracking-wide text-slate-100 font-sans">
                ELEPHANT GUARD
              </span>
              <span className="text-[10px] px-2 py-0.5 rounded bg-emerald-950/80 border border-emerald-500/40 text-emerald-400 font-mono font-bold uppercase tracking-wider">
                ADMIN PORTAL
              </span>
            </div>
            <div className="text-[11px] text-slate-400 font-mono hidden sm:block">
              Jharkhand Forest Department • Dalma Wildlife Division (NH-33)
            </div>
          </div>
        </div>

        {/* Center Live Ticker */}
        <div className="hidden md:flex items-center gap-4 bg-slate-900/80 px-3.5 py-1.5 rounded-full border border-slate-800">
          <div className="flex items-center gap-2">
            <span className="relative flex h-2.5 w-2.5">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500"></span>
            </span>
            <span className="text-xs font-mono text-slate-300">Central Mesh: Online</span>
          </div>
          <div className="h-3 w-px bg-slate-700" />
          <div className="text-xs font-mono text-cyan-400">
            Corridor: Dalma Pass NH-33
          </div>
        </div>

        {/* Right Section: Alert Bell + User Badge + Logout */}
        <div className="flex items-center gap-3">
          {/* Active Alerts Button */}
          <button
            onClick={onNavigateToAlerts}
            className="relative p-2 rounded-lg bg-slate-900/80 hover:bg-slate-800 border border-slate-700/60 text-slate-300 hover:text-white transition-colors"
            title="View Active Critical Alerts"
          >
            <Bell className="w-4 h-4 text-amber-400" />
            {activeAlertsCount > 0 && (
              <span className="absolute -top-1 -right-1 px-1.5 py-0.2 min-w-[18px] h-[18px] rounded-full bg-rose-600 text-white text-[10px] font-bold flex items-center justify-center border border-slate-900 animate-pulse">
                {activeAlertsCount}
              </span>
            )}
          </button>

          {/* User Profile */}
          <div className="flex items-center gap-2.5 pl-2 border-l border-slate-800">
            <div className="w-8 h-8 rounded-lg bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-300">
              <User className="w-4 h-4 text-emerald-400" />
            </div>
            <div className="hidden sm:block text-left">
              <div className="text-xs font-semibold text-slate-200 leading-tight">
                {user?.fullName || 'Wildlife Officer'}
              </div>
              <div className="text-[10px] text-slate-400 font-mono">
                {user?.role || 'Administrator'}
              </div>
            </div>
          </div>

          {/* Logout Button */}
          <button
            onClick={onLogout}
            className="ml-2 flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-red-950/40 hover:bg-red-900/60 border border-red-800/50 text-red-300 hover:text-red-100 text-xs font-medium transition-all"
            title="Log out of Admin Dashboard"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Logout</span>
          </button>
        </div>

      </div>
    </header>
  );
};
