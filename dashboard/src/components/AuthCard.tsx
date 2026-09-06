import React, { useState } from 'react';
import { Sparkles, Terminal } from 'lucide-react';
import { LoginForm } from './LoginForm';
import { ForgotPasswordModal } from './ForgotPasswordModal';
import { SystemStatus } from './SystemStatus';
import { useAuth } from '../hooks/useAuth';

interface AuthCardProps {
  onSuccess: () => void;
}

export const AuthCard: React.FC<AuthCardProps> = ({ onSuccess }) => {
  const { loginDemo, isLoading } = useAuth();
  const [isForgotModalOpen, setIsForgotModalOpen] = useState<boolean>(false);

  const handleDemoAccess = async () => {
    const success = await loginDemo();
    if (success) {
      setTimeout(() => {
        onSuccess();
      }, 400);
    }
  };

  return (
    <div className="glass-panel rounded-3xl p-6 sm:p-8 relative overflow-hidden hud-corner max-w-md w-full mx-auto border border-slate-800/80 shadow-2xl">
      {/* Decorative Top Accent Glow */}
      <div className="absolute -top-24 left-1/2 -translate-x-1/2 w-48 h-48 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />

      {/* Header */}
      <div className="space-y-1.5 mb-6">
        <div className="flex items-center justify-between">
          <span className="text-[10px] font-mono font-bold tracking-widest text-cyan-400 bg-cyan-950/80 px-2.5 py-1 rounded border border-cyan-800/50 uppercase flex items-center gap-1.5">
            <Terminal className="w-3 h-3" />
            GATEWAY NODE // AUTH-01
          </span>
          <span className="text-[10px] font-mono text-slate-500">
            PORT 443 [TLS 1.3]
          </span>
        </div>

        <h2 className="font-tactical font-black text-2xl text-slate-100 tracking-wide pt-2">
          Welcome back
        </h2>
        <p className="text-xs text-slate-400 leading-relaxed font-sans">
          Sign in to access the SEEMS-AI Command Center & Bio-Spatial Radar.
        </p>
      </div>

      {/* Primary Login Form */}
      <LoginForm
        onForgotPassword={() => setIsForgotModalOpen(true)}
        onSuccess={onSuccess}
      />

      {/* Divider */}
      <div className="relative my-5">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-slate-800/80" />
        </div>
        <div className="relative flex justify-center text-[10px] uppercase font-mono">
          <span className="bg-[#0f172a] px-3 text-slate-500">
            Evaluation & Review Bypass
          </span>
        </div>
      </div>

      {/* Hackathon Demo Access Button */}
      <div className="space-y-2">
        <button
          type="button"
          onClick={handleDemoAccess}
          disabled={isLoading}
          className="w-full py-2.5 px-4 rounded-xl bg-slate-900/90 hover:bg-slate-800/90 text-cyan-300 border border-cyan-800/40 hover:border-cyan-600/60 font-mono text-xs font-semibold tracking-wider transition-all duration-200 flex items-center justify-center gap-2 shadow-sm active:scale-[0.99] disabled:opacity-50"
        >
          <Sparkles className="w-3.5 h-3.5 text-cyan-400" />
          <span>Continue with Demo Access</span>
        </button>

        <p className="text-[10px] text-slate-500 font-mono text-center leading-tight">
          Demo access — restricted prototype session for hackathon jury & research evaluation
        </p>
      </div>

      {/* Real-time System Status */}
      <div className="mt-5">
        <SystemStatus />
      </div>

      {/* Forgot Password Modal Dialog */}
      <ForgotPasswordModal
        isOpen={isForgotModalOpen}
        onClose={() => setIsForgotModalOpen(false)}
      />
    </div>
  );
};
