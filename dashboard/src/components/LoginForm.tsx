import React, { useState } from 'react';
import { Eye, EyeOff, Lock, User, ShieldAlert, CheckCircle2, ArrowRight } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { RoleSelector } from './RoleSelector';
import { UserRole } from '../types';

interface LoginFormProps {
  onForgotPassword: () => void;
  onSuccess: () => void;
}

export const LoginForm: React.FC<LoginFormProps> = ({
  onForgotPassword,
  onSuccess,
}) => {
  const { login, selectedRole, setSelectedRole, authStatus, errorMessage, clearError, isLoading } = useAuth();

  const [identifier, setIdentifier] = useState<string>('officer.dalma@forest.gov.in');
  const [password, setPassword] = useState<string>('Dalma#Secure2026');
  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [rememberMe, setRememberMe] = useState<boolean>(true);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!identifier.trim() || !password || isLoading) return;

    const success = await login({
      identifier,
      password,
      role: selectedRole,
      rememberMe,
    });

    if (success) {
      setTimeout(() => {
        onSuccess();
      }, 500);
    }
  };

  const handleRoleChange = (role: UserRole) => {
    setSelectedRole(role);
    clearError();
  };

  const isValidating = authStatus === 'VALIDATING';
  const isSuccess = authStatus === 'SUCCESS';
  const isError = !!errorMessage;

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Role Selector */}
      <RoleSelector
        selectedRole={selectedRole}
        onSelectRole={handleRoleChange}
        disabled={isLoading}
      />

      {/* Error / Alert Notification Banner */}
      {isError && (
        <div
          role="alert"
          className="p-3 rounded-xl bg-red-950/80 border border-red-500/60 text-red-200 text-xs flex items-start gap-2.5 animate-fadeIn"
        >
          <ShieldAlert className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
          <div className="flex-1 text-[11px] leading-relaxed">
            <span className="font-semibold block font-mono text-red-300">
              AUTHENTICATION REJECTED
            </span>
            {errorMessage}
          </div>
        </div>
      )}

      {/* Success Notification Banner */}
      {isSuccess && (
        <div
          role="status"
          className="p-3 rounded-xl bg-emerald-950/80 border border-emerald-500/60 text-emerald-200 text-xs flex items-center gap-2.5 animate-fadeIn"
        >
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span className="font-tactical font-semibold tracking-wider text-emerald-300 text-xs">
            AUTHENTICATION SUCCESSFUL — INITIALIZING SESSION...
          </span>
        </div>
      )}

      {/* User ID / Email Input */}
      <div className="space-y-1.5">
        <label
          htmlFor="auth-identifier"
          className="text-xs font-mono uppercase tracking-wider text-slate-400 font-semibold flex items-center justify-between"
        >
          <span>User ID / Email</span>
          <span className="text-[10px] text-slate-500 font-normal">Registered Identity</span>
        </label>
        <div className="relative">
          <input
            id="auth-identifier"
            type="text"
            required
            disabled={isLoading}
            value={identifier}
            onChange={(e) => {
              setIdentifier(e.target.value);
              if (isError) clearError();
            }}
            placeholder="Enter your registered ID"
            className="w-full pl-9 pr-4 py-2.5 rounded-xl bg-slate-900/90 border border-slate-700/80 focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 text-xs text-slate-100 placeholder-slate-500 outline-none font-sans transition disabled:opacity-50"
            autoComplete="username"
          />
          <User className="w-4 h-4 text-slate-500 absolute left-3 top-3 pointer-events-none" />
        </div>
      </div>

      {/* Password Input */}
      <div className="space-y-1.5">
        <div className="flex items-center justify-between text-xs font-mono">
          <label
            htmlFor="auth-password"
            className="uppercase tracking-wider text-slate-400 font-semibold"
          >
            Password
          </label>
          <button
            type="button"
            onClick={onForgotPassword}
            disabled={isLoading}
            className="text-[11px] text-cyan-400 hover:text-cyan-300 transition underline underline-offset-2 hover:no-underline"
          >
            Forgot Password?
          </button>
        </div>
        <div className="relative">
          <input
            id="auth-password"
            type={showPassword ? 'text' : 'password'}
            required
            disabled={isLoading}
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              if (isError) clearError();
            }}
            placeholder="Enter your password"
            className="w-full pl-9 pr-10 py-2.5 rounded-xl bg-slate-900/90 border border-slate-700/80 focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500 text-xs text-slate-100 placeholder-slate-500 outline-none font-sans transition disabled:opacity-50"
            autoComplete="current-password"
          />
          <Lock className="w-4 h-4 text-slate-500 absolute left-3 top-3 pointer-events-none" />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            disabled={isLoading}
            aria-label={showPassword ? 'Hide password' : 'Show password'}
            className="absolute right-3 top-2.5 text-slate-500 hover:text-slate-300 transition p-0.5"
          >
            {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
          </button>
        </div>
      </div>

      {/* Remember Me Checkbox */}
      <div className="flex items-center justify-between pt-1">
        <label className="flex items-center gap-2 cursor-pointer select-none">
          <input
            type="checkbox"
            checked={rememberMe}
            onChange={(e) => setRememberMe(e.target.checked)}
            disabled={isLoading}
            className="w-3.5 h-3.5 rounded bg-slate-900 border-slate-700 text-cyan-600 focus:ring-cyan-500 focus:ring-offset-slate-900"
          />
          <span className="text-xs text-slate-400 font-sans">
            Persist session on this hardware terminal
          </span>
        </label>
      </div>

      {/* Primary Submit Button: SECURE LOGIN */}
      <div className="pt-2">
        <button
          type="submit"
          disabled={isLoading || isSuccess}
          className={`w-full py-3 rounded-xl font-tactical font-bold text-xs tracking-wider transition-all duration-300 flex items-center justify-center gap-2 shadow-lg ${
            isSuccess
              ? 'bg-emerald-500 text-slate-950 shadow-emerald-950/60'
              : isValidating
              ? 'bg-cyan-700 text-slate-950 shadow-cyan-950/60'
              : 'bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 shadow-cyan-950/60 hover:shadow-cyan-900/40 active:scale-[0.99]'
          } disabled:opacity-75 disabled:cursor-not-allowed`}
        >
          {isValidating ? (
            <>
              <span className="w-4 h-4 border-2 border-slate-950 border-t-transparent rounded-full animate-spin" />
              <span>AUTHENTICATING SECURE SESSION...</span>
            </>
          ) : isSuccess ? (
            <>
              <CheckCircle2 className="w-4 h-4" />
              <span>ACCESS GRANTED</span>
            </>
          ) : (
            <>
              <span>SECURE LOGIN</span>
              <ArrowRight className="w-4 h-4" />
            </>
          )}
        </button>
      </div>
    </form>
  );
};
