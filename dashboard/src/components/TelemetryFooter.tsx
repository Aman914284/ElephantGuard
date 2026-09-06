import React from 'react';

export const TelemetryFooter: React.FC = () => {
  return (
    <footer className="w-full py-3 px-4 text-[10px] font-mono text-slate-500 border-t border-slate-900/80 bg-slate-950/80 backdrop-blur-md">
      <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-3 text-center sm:text-left">
        <div className="flex items-center gap-2">
          <span className="w-1.5 h-1.5 rounded-full bg-cyan-400 animate-pulse" />
          <span className="text-slate-400">
            SEEMS-AI AUTH GATEWAY • BIOMETRIC & BIO-SPATIAL CORRIDOR MATRIX
          </span>
        </div>

        <div className="flex flex-wrap items-center gap-4 text-slate-400 mx-auto sm:mx-0">
          <span>
            AUTH NODE: <b className="text-cyan-300">DALMA-GATEWAY-01</b>
          </span>
          <span>
            STATUS: <b className="text-emerald-400">READY</b>
          </span>
          <span>
            ENCRYPTION: <b className="text-cyan-300">ACTIVE</b>
          </span>
          <span>
            SESSION: <b className="text-emerald-400">SECURE</b>
          </span>
          <span>
            VERSION: <b className="text-slate-300 font-tactical">v5.2</b>
          </span>
        </div>
      </div>
    </footer>
  );
};
