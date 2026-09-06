import React, { useState, useEffect, useCallback, ReactNode } from 'react';
import { AuthContext } from './AuthContext';
import { AuthService } from './authService';
import { AuthStatus, AuthUser, LoginCredentials, UserRole } from '../types';

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<AuthUser | null>(() => AuthService.getActiveSession());
  const [selectedRole, setSelectedRole] = useState<UserRole>('Forest Officer');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [authStatus, setAuthStatus] = useState<AuthStatus>('IDLE');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Check session validity periodically
  useEffect(() => {
    const interval = setInterval(() => {
      const active = AuthService.getActiveSession();
      if (!active && user) {
        setUser(null);
        setAuthStatus('SESSION_EXPIRED');
        setErrorMessage('Your session has expired. Please sign in again.');
      }
    }, 30000);

    return () => clearInterval(interval);
  }, [user]);

  const login = useCallback(async (credentials: LoginCredentials): Promise<boolean> => {
    setIsLoading(true);
    setAuthStatus('VALIDATING');
    setErrorMessage(null);

    try {
      const response = await AuthService.login(credentials);

      if (response.success && response.user) {
        setUser(response.user);
        setAuthStatus('SUCCESS');
        setIsLoading(false);
        return true;
      } else {
        setAuthStatus((response.status as AuthStatus) || 'ERROR');
        setErrorMessage(response.error || 'Authentication failed. Please verify your credentials.');
        setIsLoading(false);
        return false;
      }
    } catch {
      setAuthStatus('NETWORK_ERROR');
      setErrorMessage('Unable to reach authentication service. Check your connection or use Demo Access.');
      setIsLoading(false);
      return false;
    }
  }, []);

  const loginDemo = useCallback(async (role?: UserRole): Promise<boolean> => {
    setIsLoading(true);
    setAuthStatus('VALIDATING');
    setErrorMessage(null);

    try {
      const targetRole = role || selectedRole;
      const response = await AuthService.createDemoSession(targetRole);

      if (response.success && response.user) {
        setUser(response.user);
        setAuthStatus('SUCCESS');
        setIsLoading(false);
        return true;
      } else {
        setAuthStatus('NETWORK_ERROR');
        setErrorMessage('Demo initialization failed. Please retry.');
        setIsLoading(false);
        return false;
      }
    } catch {
      setAuthStatus('NETWORK_ERROR');
      setErrorMessage('Unexpected error during demo session setup.');
      setIsLoading(false);
      return false;
    }
  }, [selectedRole]);

  const logout = useCallback(() => {
    AuthService.logout();
    setUser(null);
    setAuthStatus('IDLE');
    setErrorMessage(null);
  }, []);

  const clearError = useCallback(() => {
    setErrorMessage(null);
    setAuthStatus('IDLE');
  }, []);

  const value = {
    user,
    isAuthenticated: !!user,
    isDemoSession: !!user?.isDemo,
    isLoading,
    authStatus,
    errorMessage,
    selectedRole,
    login,
    loginDemo,
    logout,
    setSelectedRole,
    clearError
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
