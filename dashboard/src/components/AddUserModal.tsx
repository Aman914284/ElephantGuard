import React, { useState } from 'react';
import { X, UserPlus, Shield, Phone, Mail, Lock, MapPin, Check } from 'lucide-react';
import { UserRole } from '../types';
import { api } from '../services/api';

interface AddUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUserCreated: () => void;
}

export const AddUserModal: React.FC<AddUserModalProps> = ({
  isOpen,
  onClose,
  onUserCreated
}) => {
  if (!isOpen) return null;

  const [fullName, setFullName] = useState('');
  const [mobile, setMobile] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<UserRole>('Forest Officer');
  const [assignedBeat, setAssignedBeat] = useState('Dalma Wildlife Core Beat 1');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setSuccessMsg(null);

    if (!fullName.trim() || !mobile.trim() || !email.trim() || !password.trim()) {
      setErrorMsg('Please fill in all mandatory fields.');
      return;
    }

    if (mobile.trim().length < 10) {
      setErrorMsg('Mobile number must be at least 10 digits.');
      return;
    }

    if (password.length < 6) {
      setErrorMsg('Password must be at least 6 characters long.');
      return;
    }

    setIsSubmitting(true);
    try {
      await api.createAdminUser({
        fullName: fullName.trim(),
        mobile: mobile.trim(),
        email: email.trim().toLowerCase(),
        password: password.trim(),
        role: role,
        status: 'ACTIVE'
      });

      setSuccessMsg(`Personnel "${fullName.trim()}" successfully registered!`);
      setTimeout(() => {
        onUserCreated();
        onClose();
        // Reset state
        setFullName('');
        setMobile('');
        setEmail('');
        setPassword('');
        setErrorMsg(null);
        setSuccessMsg(null);
      }, 1000);
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to register new personnel.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn">
      <div className="bg-[#0f172a] border border-slate-700 w-full max-w-lg rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between bg-slate-900/70">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-emerald-950 border border-emerald-700/60 text-emerald-400">
              <UserPlus className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-100 font-sans">
                Register Wildlife Personnel
              </h3>
              <div className="text-xs text-slate-400 font-mono">
                Dalma Wildlife Division • Official Onboarding
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

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 overflow-y-auto space-y-4 text-xs text-slate-300">
          
          {errorMsg && (
            <div className="p-3 rounded-xl bg-rose-950/80 border border-rose-800 text-rose-300 text-xs font-mono">
              ⚠️ {errorMsg}
            </div>
          )}

          {successMsg && (
            <div className="p-3 rounded-xl bg-emerald-950/80 border border-emerald-800 text-emerald-300 text-xs font-mono flex items-center gap-2">
              <Check className="w-4 h-4 text-emerald-400" />
              <span>{successMsg}</span>
            </div>
          )}

          {/* Full Name */}
          <div className="space-y-1">
            <label className="text-[11px] font-mono text-slate-400 uppercase">
              Full Name & Designation *
            </label>
            <input
              type="text"
              required
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="e.g. Anand Soren (Beat Officer)"
              className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-emerald-500"
            />
          </div>

          {/* Mobile & Email Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div className="space-y-1">
              <label className="text-[11px] font-mono text-slate-400 uppercase flex items-center gap-1">
                <Phone className="w-3 h-3 text-slate-400" />
                <span>Mobile Number *</span>
              </label>
              <input
                type="tel"
                required
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
                placeholder="10-digit mobile"
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-xs text-slate-100 font-mono placeholder-slate-500 focus:outline-none focus:border-emerald-500"
              />
            </div>

            <div className="space-y-1">
              <label className="text-[11px] font-mono text-slate-400 uppercase flex items-center gap-1">
                <Mail className="w-3 h-3 text-slate-400" />
                <span>Email Address *</span>
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="officer@forest.gov.in"
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-emerald-500"
              />
            </div>
          </div>

          {/* Role & Beat Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div className="space-y-1">
              <label className="text-[11px] font-mono text-slate-400 uppercase flex items-center gap-1">
                <Shield className="w-3 h-3 text-emerald-400" />
                <span>Assigned Role *</span>
              </label>
              <select
                value={role}
                onChange={(e) => setRole(e.target.value as UserRole)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
              >
                <option value="Forest Officer">Forest Officer (Range / QRT)</option>
                <option value="Community Guard">Community Guard (Van Suraksha)</option>
                <option value="Administrator">Administrator (HQ Wildlife Command)</option>
                <option value="Citizen">Citizen (Village Farmer / Resident)</option>
              </select>
            </div>

            <div className="space-y-1">
              <label className="text-[11px] font-mono text-slate-400 uppercase flex items-center gap-1">
                <MapPin className="w-3 h-3 text-teal-400" />
                <span>Assigned Beat / Range</span>
              </label>
              <select
                value={assignedBeat}
                onChange={(e) => setAssignedBeat(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
              >
                <option value="Dalma Wildlife Core Beat 1">Dalma Sanctuary Core Beat 1</option>
                <option value="Asanbani Ghat Sector (NH-33)">Asanbani Ghat Sector (NH-33)</option>
                <option value="Chandil Pass Northern Sector">Chandil Pass Northern Sector</option>
                <option value="Mirzadih Agricultural Buffer">Mirzadih Agricultural Buffer</option>
                <option value="Mango Entry Checkpost">Mango Entry Checkpost</option>
                <option value="Ghatshila Forest Division Corridor">Ghatshila Corridor Link</option>
              </select>
            </div>
          </div>

          {/* Password */}
          <div className="space-y-1">
            <label className="text-[11px] font-mono text-slate-400 uppercase flex items-center gap-1">
              <Lock className="w-3 h-3 text-slate-400" />
              <span>Initial Login Password *</span>
            </label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Minimum 6 characters"
              className="w-full bg-slate-950 border border-slate-700 rounded-xl px-3.5 py-2.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-emerald-500 font-mono"
            />
          </div>

          <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800 text-[11px] text-slate-400 font-sans">
            🛡️ <strong>Authentication Notice:</strong> Once registered, the officer can immediately log in on both the <strong>ElephantGuard Android Mobile Patrol App</strong> and this Central Web Admin Portal.
          </div>

          {/* Actions */}
          <div className="pt-3 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-xs transition-colors flex items-center gap-2 shadow-lg disabled:opacity-50"
            >
              <UserPlus className="w-4 h-4" />
              <span>{isSubmitting ? 'Registering...' : 'Register Personnel'}</span>
            </button>
          </div>

        </form>

      </div>
    </div>
  );
};
