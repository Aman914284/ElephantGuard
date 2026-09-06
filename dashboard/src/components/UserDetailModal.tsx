import React from 'react';
import { X, User, Phone, Mail, Calendar, Shield, Activity, Power } from 'lucide-react';
import { UserRecord } from '../types';

interface UserDetailModalProps {
  user: UserRecord | null;
  isOpen: boolean;
  onClose: () => void;
  onToggleStatus: (userId: string, newStatus: string) => Promise<void>;
}

export const UserDetailModal: React.FC<UserDetailModalProps> = ({
  user,
  isOpen,
  onClose,
  onToggleStatus
}) => {
  if (!isOpen || !user) return null;

  const isSuspended = user.status === 'SUSPENDED' || user.status === 'DEACTIVATED';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn">
      <div className="bg-[#0f172a] border border-slate-700 w-full max-w-md rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between bg-slate-900/60">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-slate-800 border border-slate-700 text-emerald-400">
              <User className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-100 font-sans">
                {user.fullName}
              </h3>
              <div className="text-xs text-slate-400 font-mono">
                User ID: {user.id}
              </div>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-slate-800 text-slate-400 hover:text-white transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* User Data Body */}
        <div className="p-6 space-y-4 text-xs text-slate-300">
          
          <div className="space-y-2.5 bg-slate-950 p-4 rounded-xl border border-slate-800">
            <div className="flex items-center justify-between">
              <span className="text-slate-400 flex items-center gap-2">
                <Shield className="w-3.5 h-3.5 text-emerald-400" />
                <span>Assigned Role:</span>
              </span>
              <span className="font-semibold text-slate-100 bg-slate-900 px-2.5 py-0.5 rounded border border-slate-700">
                {user.role}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-slate-400 flex items-center gap-2">
                <Activity className="w-3.5 h-3.5 text-cyan-400" />
                <span>Account Status:</span>
              </span>
              <span
                className={`font-semibold px-2.5 py-0.5 rounded border ${
                  user.status === 'ACTIVE'
                    ? 'bg-emerald-950 text-emerald-300 border-emerald-800'
                    : 'bg-rose-950 text-rose-300 border-rose-800'
                }`}
              >
                {user.status}
              </span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-slate-400 flex items-center gap-2">
                <Phone className="w-3.5 h-3.5 text-slate-400" />
                <span>Mobile Number:</span>
              </span>
              <span className="font-mono text-slate-200">{user.mobile}</span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-slate-400 flex items-center gap-2">
                <Mail className="w-3.5 h-3.5 text-slate-400" />
                <span>Email Address:</span>
              </span>
              <span className="text-slate-200">{user.email}</span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-slate-400 flex items-center gap-2">
                <Calendar className="w-3.5 h-3.5 text-slate-400" />
                <span>Registration Date:</span>
              </span>
              <span className="font-mono text-slate-300">{user.createdAt}</span>
            </div>

            <div className="flex items-center justify-between">
              <span className="text-slate-400 flex items-center gap-2">
                <Activity className="w-3.5 h-3.5 text-purple-400" />
                <span>Last Active:</span>
              </span>
              <span className="font-mono text-slate-300">{user.lastActive}</span>
            </div>
          </div>

          <div className="p-3.5 rounded-xl bg-slate-900/60 border border-slate-800 text-[11px] text-slate-400 leading-relaxed font-sans">
            🔒 <strong>Security Policy:</strong> Passwords are cryptographically salted & hashed on the central server and cannot be viewed or retrieved by administrators.
          </div>

          {/* Account Action */}
          <div className="pt-2">
            <button
              onClick={() => onToggleStatus(user.id, isSuspended ? 'ACTIVE' : 'SUSPENDED')}
              className={`w-full py-2.5 px-4 rounded-xl font-semibold text-xs flex items-center justify-center gap-2 transition-all ${
                isSuspended
                  ? 'bg-emerald-600 hover:bg-emerald-500 text-white'
                  : 'bg-rose-950/80 hover:bg-rose-900 text-rose-300 border border-rose-800'
              }`}
            >
              <Power className="w-4 h-4" />
              <span>{isSuspended ? 'Reactivate User Account' : 'Suspend / Deactivate Account'}</span>
            </button>
          </div>

        </div>

        {/* Footer */}
        <div className="px-6 py-3 border-t border-slate-800 bg-slate-900/80 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium"
          >
            Close
          </button>
        </div>

      </div>
    </div>
  );
};
