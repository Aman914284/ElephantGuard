import React, { useState } from 'react';
import {
  X,
  MapPin,
  Calendar,
  Clock,
  Shield,
  Send,
  Sparkles,
  Check
} from 'lucide-react';
import { IncidentReport, ThreatLevel, VerificationStatus, ResponseStatus } from '../types';

interface IncidentDetailModalProps {
  incident: IncidentReport | null;
  isOpen: boolean;
  onClose: () => void;
  onUpdateStatus: (incidentId: string, verificationStatus: VerificationStatus, responseStatus: ResponseStatus) => Promise<void>;
  onCreateChallengeFromIncident: (incident: IncidentReport) => void;
}

export const IncidentDetailModal: React.FC<IncidentDetailModalProps> = ({
  incident,
  isOpen,
  onClose,
  onUpdateStatus,
  onCreateChallengeFromIncident
}) => {
  if (!isOpen || !incident) return null;

  const [verificationStatus, setVerificationStatus] = useState<VerificationStatus>(incident.verificationStatus);
  const [responseStatus, setResponseStatus] = useState<ResponseStatus>(incident.responseStatus);
  const [isUpdating, setIsUpdating] = useState(false);
  const [successNotice, setSuccessNotice] = useState(false);

  const getThreatBadge = (level: ThreatLevel) => {
    switch (level) {
      case 'CRITICAL':
        return 'bg-rose-500/20 text-rose-300 border-rose-500/40';
      case 'HIGH':
        return 'bg-amber-500/20 text-amber-300 border-amber-500/40';
      case 'CAUTION':
        return 'bg-yellow-500/20 text-yellow-300 border-yellow-500/40';
      default:
        return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40';
    }
  };

  const handleSaveStatus = async () => {
    setIsUpdating(true);
    try {
      await onUpdateStatus(incident.id, verificationStatus, responseStatus);
      setSuccessNotice(true);
      setTimeout(() => setSuccessNotice(false), 2000);
    } catch (err) {
      console.error('Failed to update status:', err);
    } finally {
      setIsUpdating(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn">
      <div className="bg-[#0f172a] border border-slate-700 w-full max-w-2xl rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between bg-slate-900/60">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-slate-800 border border-slate-700">
              <Shield className="w-5 h-5 text-emerald-400" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-slate-100 font-mono">
                  {incident.id}
                </h3>
                <span className={`px-2 py-0.5 rounded text-[11px] font-mono border font-semibold ${getThreatBadge(incident.threatLevel)}`}>
                  {incident.threatLevel}
                </span>
              </div>
              <div className="text-xs text-slate-400">
                Reporter: <span className="text-slate-300 font-medium">{incident.reporterName}</span>
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

        {/* Content Body */}
        <div className="p-6 overflow-y-auto space-y-6 text-sm text-slate-300">
          
          {/* Key Metric Cards */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800">
              <div className="text-[11px] text-slate-400 font-mono uppercase">Elephant Count</div>
              <div className="text-xl font-bold text-slate-100 mt-1">
                {incident.elephantCount} <span className="text-xs font-normal text-slate-400">Animals</span>
              </div>
            </div>

            <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800">
              <div className="text-[11px] text-slate-400 font-mono uppercase">AI Confidence</div>
              <div className="text-xl font-bold text-cyan-400 mt-1">
                {(incident.confidence * 100).toFixed(1)}%
              </div>
            </div>

            <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800">
              <div className="text-[11px] text-slate-400 font-mono uppercase">Risk Score</div>
              <div className="text-xl font-bold text-amber-400 mt-1">
                {incident.riskScore}/100
              </div>
            </div>

            <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800">
              <div className="text-[11px] text-slate-400 font-mono uppercase">Alert Type</div>
              <div className="text-sm font-bold text-slate-200 mt-1 font-mono">
                {incident.isSos ? '🚨 5KM SOS' : 'Edge Optical'}
              </div>
            </div>
          </div>

          {/* Location & Time Info */}
          <div className="p-4 rounded-xl bg-slate-900/50 border border-slate-800/80 space-y-2">
            <div className="flex items-center gap-2 text-xs font-mono text-slate-400">
              <MapPin className="w-4 h-4 text-emerald-400" />
              <span>Real GPS Coordinates:</span>
              <strong className="text-slate-200">
                {incident.latitude.toFixed(5)}° N, {incident.longitude.toFixed(5)}° E
              </strong>
            </div>

            <div className="flex items-center gap-2 text-xs font-mono text-slate-400">
              <Calendar className="w-4 h-4 text-cyan-400" />
              <span>Recorded Timestamp:</span>
              <span className="text-slate-200">{incident.timestamp}</span>
            </div>

            {incident.sourceDevice && (
              <div className="flex items-center gap-2 text-xs font-mono text-slate-400">
                <Clock className="w-4 h-4 text-purple-400" />
                <span>Source Device / Unit:</span>
                <span className="text-slate-300">{incident.sourceDevice}</span>
              </div>
            )}
          </div>

          {/* Incident Notes / Description */}
          <div className="space-y-1.5">
            <label className="text-xs font-mono font-semibold uppercase text-slate-400">
              Field Observations & Corridor Notes
            </label>
            <div className="p-3.5 rounded-xl bg-slate-950 border border-slate-800 text-slate-200 text-xs leading-relaxed font-sans">
              {incident.notes || 'No specific field remarks recorded. Standard patrol waypoint detection.'}
            </div>
          </div>

          {/* Status Update Controls */}
          <div className="p-4 rounded-xl bg-slate-900/80 border border-slate-800 space-y-4">
            <div className="text-xs font-bold font-mono uppercase text-slate-300">
              Official Verification & QRT Dispatch Action
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="text-xs text-slate-400 mb-1 block">Verification Status</label>
                <select
                  value={verificationStatus}
                  onChange={(e) => setVerificationStatus(e.target.value as VerificationStatus)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
                >
                  <option value="New">New</option>
                  <option value="Under Review">Under Review</option>
                  <option value="Verified">Verified</option>
                  <option value="Dispatched">Dispatched</option>
                  <option value="Resolved">Resolved</option>
                  <option value="Rejected">Rejected</option>
                </select>
              </div>

              <div>
                <label className="text-xs text-slate-400 mb-1 block">Response Status</label>
                <select
                  value={responseStatus}
                  onChange={(e) => setResponseStatus(e.target.value as ResponseStatus)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
                >
                  <option value="Standby">Standby</option>
                  <option value="Dispatched">Dispatched</option>
                  <option value="Resolved">Resolved</option>
                </select>
              </div>
            </div>

            <div className="flex items-center justify-between pt-2">
              <button
                onClick={handleSaveStatus}
                disabled={isUpdating}
                className="px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-medium text-xs flex items-center gap-1.5 transition-colors disabled:opacity-50"
              >
                {successNotice ? <Check className="w-4 h-4" /> : <Send className="w-4 h-4" />}
                <span>{successNotice ? 'Updated Successfully!' : 'Save Status Update'}</span>
              </button>

              <button
                onClick={() => {
                  onClose();
                  onCreateChallengeFromIncident(incident);
                }}
                className="px-3.5 py-2 rounded-lg bg-indigo-950/80 hover:bg-indigo-900/80 border border-indigo-700/60 text-indigo-300 font-medium text-xs flex items-center gap-1.5 transition-colors"
              >
                <Sparkles className="w-4 h-4 text-indigo-400" />
                <span>Convert to Societal Challenge</span>
              </button>
            </div>
          </div>

        </div>

        {/* Footer */}
        <div className="px-6 py-3 border-t border-slate-800 bg-slate-900/80 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 font-medium text-xs transition-colors"
          >
            Close Details
          </button>
        </div>

      </div>
    </div>
  );
};
