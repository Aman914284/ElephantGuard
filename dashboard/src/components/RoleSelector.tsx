import React from 'react';
import { UserRole } from '../types';
import { Shield, Trees, Truck, Activity } from 'lucide-react';

interface RoleSelectorProps {
  selectedRole: UserRole;
  onSelectRole: (role: UserRole) => void;
  disabled?: boolean;
}

const ROLES: Array<{
  id: UserRole;
  label: string;
  sublabel: string;
  icon: React.ElementType;
}> = [
  {
    id: 'Forest Officer',
    label: 'Forest Officer',
    sublabel: 'Field Ops & Sanctuary Patrol',
    icon: Trees,
  },
  {
    id: 'QRT Operator',
    label: 'QRT Operator',
    sublabel: 'Rapid Intercept Unit',
    icon: Truck,
  },
  {
    id: 'Analyst',
    label: 'Analyst',
    sublabel: 'Telemetry & Risk Matrix',
    icon: Activity,
  },
  {
    id: 'Administrator',
    label: 'Administrator',
    sublabel: 'System & Mesh Control',
    icon: Shield,
  },
];

export const RoleSelector: React.FC<RoleSelectorProps> = ({
  selectedRole,
  onSelectRole,
  disabled = false,
}) => {
  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between text-xs">
        <label className="font-mono uppercase tracking-wider text-slate-400 font-semibold flex items-center gap-1.5">
          <span className="w-1.5 h-1.5 rounded-full bg-cyan-400" />
          Access Level
        </label>
        <span className="text-[10px] font-mono text-slate-500">
          Role-Gated Protocol
        </span>
      </div>

      <div className="grid grid-cols-2 gap-2">
        {ROLES.map((role) => {
          const isSelected = selectedRole === role.id;
          const Icon = role.icon;

          return (
            <button
              key={role.id}
              type="button"
              disabled={disabled}
              onClick={() => onSelectRole(role.id)}
              className={`relative flex items-start gap-2.5 p-2.5 rounded-xl border text-left transition-all duration-200 ${
                isSelected
                  ? 'bg-cyan-950/40 border-cyan-500/60 shadow-lg shadow-cyan-950/40'
                  : 'bg-slate-900/50 border-slate-800/80 hover:border-slate-700 hover:bg-slate-900/80 text-slate-400'
              } ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}`}
            >
              <div
                className={`p-1.5 rounded-lg border shrink-0 transition-colors ${
                  isSelected
                    ? 'bg-cyan-500/20 text-cyan-300 border-cyan-500/40'
                    : 'bg-slate-950 text-slate-500 border-slate-800'
                }`}
              >
                <Icon className="w-3.5 h-3.5" />
              </div>

              <div className="min-w-0 flex-1">
                <div
                  className={`text-xs font-semibold leading-tight truncate ${
                    isSelected ? 'text-slate-100 font-tactical tracking-wide' : 'text-slate-300'
                  }`}
                >
                  {role.label}
                </div>
                <div className="text-[10px] text-slate-500 truncate font-mono mt-0.5">
                  {role.sublabel}
                </div>
              </div>

              {isSelected && (
                <span className="absolute top-1.5 right-1.5 flex h-1.5 w-1.5">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan-400 opacity-75" />
                  <span className="relative inline-flex rounded-full h-1.5 w-1.5 bg-cyan-500" />
                </span>
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
};
