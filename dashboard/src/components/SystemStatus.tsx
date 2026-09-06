import React, { useState, useEffect } from 'react';
import { AuthService } from '../auth/authService';

export const SystemStatus: React.FC = () => {
  const [status, setStatus] = useState<'ONLINE' | 'DEGRADED' | 'OFFLINE'>('ONLINE');

  useEffect(() => {
    const updateHealth = async () => {
      const current = await AuthService.checkServiceHealth();
      setStatus(current);
    };

    updateHealth();
    window.addEventListener('online', updateHealth);
    window.addEventListener('offline', updateHealth);

    return () => {
      window.removeEventListener('online', updateHealth);
      window.removeEventListener('offline', updateHealth);
    };
  }, []);

  const isOnline = status === 'ONLINE';

  return (
    <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-[11px] font-mono">
      <div className="flex items-center gap-2">
        <span className="relative flex h-2 w-2">
          {isOnline ? (
            <>
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
              <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500" />
            </>
          ) : (
            <span className="relative inline-flex rounded-full h-2 w-2 bg-red-500" />
          )}
        </span>
        <span className={isOnline ? 'text-emerald-400 font-semibold' : 'text-red-400 font-semibold'}>
          {isOnline ? 'AUTHENTICATION SERVICES ONLINE' : 'AUTH SERVICE: OFFLINE'}
        </span>
      </div>

      <div className="flex items-center gap-3 text-slate-500">
        <span className="hidden sm:inline">EDGE NODE STATUS: READY</span>
        <span className="text-slate-600">|</span>
        <span className="text-cyan-400 font-bold font-tactical">SEEMS-AI v5.2</span>
      </div>
    </div>
  );
};
