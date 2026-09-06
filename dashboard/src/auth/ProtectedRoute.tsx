import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-[#020617] flex flex-col items-center justify-center gap-4 text-cyan-400 font-mono">
        <div className="w-12 h-12 border-2 border-cyan-500/20 border-t-cyan-400 rounded-full animate-spin" />
        <p className="text-xs tracking-widest uppercase">Verifying Security Credentials...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    // Redirect to login preserving destination state
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};
