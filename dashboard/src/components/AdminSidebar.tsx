import React from 'react';
import {
  LayoutDashboard,
  Users,
  AlertOctagon,
  MapPin,
  Bell,
  Sparkles,
  Building2,
  BarChart3
} from 'lucide-react';

export type AdminTab =
  | 'home'
  | 'users'
  | 'incidents'
  | 'map'
  | 'alerts'
  | 'challenges'
  | 'collaboration'
  | 'analytics';

interface AdminSidebarProps {
  activeTab: AdminTab;
  onSelectTab: (tab: AdminTab) => void;
  criticalAlertsCount?: number;
  openChallengesCount?: number;
}

export const AdminSidebar: React.FC<AdminSidebarProps> = ({
  activeTab,
  onSelectTab,
  criticalAlertsCount = 0,
  openChallengesCount = 0
}) => {
  const navItems: Array<{
    id: AdminTab;
    label: string;
    icon: React.ComponentType<{ className?: string }>;
    badge?: number;
    badgeColor?: string;
  }> = [
    { id: 'home', label: 'Dashboard Home', icon: LayoutDashboard },
    { id: 'users', label: 'Registered Users', icon: Users },
    { id: 'incidents', label: 'Incident Reports', icon: AlertOctagon },
    { id: 'map', label: 'Real-Time Map', icon: MapPin },
    {
      id: 'alerts',
      label: 'Alert Management',
      icon: Bell,
      badge: criticalAlertsCount,
      badgeColor: 'bg-rose-500 text-white'
    },
    {
      id: 'challenges',
      label: 'SIH Challenges',
      icon: Sparkles,
      badge: openChallengesCount,
      badgeColor: 'bg-amber-500 text-slate-950 font-bold'
    },
    { id: 'collaboration', label: 'University & Industry', icon: Building2 },
    { id: 'analytics', label: 'Analytics & Trends', icon: BarChart3 },
  ];

  return (
    <aside className="w-64 bg-[#0a0f1d] border-r border-slate-800 flex flex-col justify-between p-4 min-h-[calc(100vh-4rem)]">
      <div className="space-y-1">
        <div className="px-3 py-2 text-[11px] font-mono uppercase tracking-wider text-slate-400 font-semibold">
          Forest Command Center
        </div>

        <nav className="space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onSelectTab(item.id)}
                className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-xl font-medium text-xs sm:text-sm transition-all duration-150 ${
                  isActive
                    ? 'bg-emerald-600/20 text-emerald-300 border border-emerald-500/40 shadow-sm font-semibold'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60'
                }`}
              >
                <div className="flex items-center gap-3">
                  <Icon
                    className={`w-4 h-4 ${
                      isActive ? 'text-emerald-400' : 'text-slate-400'
                    }`}
                  />
                  <span>{item.label}</span>
                </div>

                {item.badge !== undefined && item.badge > 0 && (
                  <span
                    className={`px-2 py-0.5 rounded-full text-[10px] font-mono ${
                      item.badgeColor || 'bg-slate-800 text-slate-300'
                    }`}
                  >
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Corridor Status Card at bottom */}
      <div className="mt-6 p-3 rounded-xl bg-slate-900/90 border border-slate-800/80 text-left space-y-2">
        <div className="flex items-center justify-between">
          <span className="text-[11px] font-mono text-emerald-400 font-bold">
            DALMA SANCTUARY
          </span>
          <span className="flex h-2 w-2 relative">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
            <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
          </span>
        </div>
        <div className="text-[11px] text-slate-400 font-sans leading-relaxed">
          Active Corridor: NH-33 Asanbani to Chandil Pass (18.4 km)
        </div>
        <div className="text-[10px] text-slate-400 font-mono pt-1 border-t border-slate-800 flex justify-between">
          <span>QRT Patrol Units: 4</span>
          <span>Optical Feeds: 16</span>
        </div>
      </div>
    </aside>
  );
};
