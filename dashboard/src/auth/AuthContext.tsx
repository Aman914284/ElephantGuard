import { createContext } from 'react';
import { AuthUser, AuthStatus, LoginCredentials, UserRole } from '../types';

export interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isDemoSession: boolean;
  isLoading: boolean;
  authStatus: AuthStatus;
  errorMessage: string | null;
  selectedRole: UserRole;
  login: (credentials: LoginCredentials) => Promise<boolean>;
  loginDemo: (role?: UserRole) => Promise<boolean>;
  logout: () => void;
  setSelectedRole: (role: UserRole) => void;
  clearError: () => void;
}

export const AuthContext = createContext<AuthContextType | null>(null);
