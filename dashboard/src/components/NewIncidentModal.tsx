import React, { useState } from 'react';
import { X, AlertOctagon, Send, AlertTriangle } from 'lucide-react';
import { ThreatLevel, VerificationStatus, ResponseStatus } from '../types';

interface NewIncidentModalProps {
  isOpen: boolean;
  initialCoords?: { latitude: number; longitude: number } | null;
  onClose: () => void;
  onSubmitIncident: (incidentData: {
    reporterName: string;
    latitude: number;
    longitude: number;
    elephantCount: number;
    confidence: number;
    riskScore: number;
    threatLevel: ThreatLevel;
    verificationStatus: VerificationStatus;
    responseStatus: ResponseStatus;
    notes: string;
    isSos: boolean;
  }) => Promise<void>;
}

export const NewIncidentModal: React.FC<NewIncidentModalProps> = ({
  isOpen,
  initialCoords,
  onClose,
  onSubmitIncident
}) => {
  if (!isOpen) return null;

  const [reporterName, setReporterName] = useState('Dalma Range Patrol Unit');
  const [latitude, setLatitude] = useState(initialCoords?.latitude ?? 22.8942);
  const [longitude, setLongitude] = useState(initialCoords?.longitude ?? 86.2081);
  const [elephantCount, setElephantCount] = useState(2);
  const [confidence] = useState(0.95);
  const [riskScore] = useState(80);
  const [threatLevel, setThreatLevel] = useState<ThreatLevel>('HIGH');
  const [verificationStatus, setVerificationStatus] = useState<VerificationStatus>('Verified');
  const [responseStatus, setResponseStatus] = useState<ResponseStatus>('Dispatched');
  const [notes, setNotes] = useState('');
  const [isSos, setIsSos] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setErrorMsg(null);

    try {
      await onSubmitIncident({
        reporterName: reporterName.trim(),
        latitude: Number(latitude),
        longitude: Number(longitude),
        elephantCount: Number(elephantCount),
        confidence: Number(confidence),
        riskScore: Number(riskScore),
        threatLevel,
        verificationStatus,
        responseStatus,
        notes: notes.trim(),
        isSos
      });
      onClose();
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to log incident report.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn">
      <div className="bg-[#0f172a] border border-slate-700 w-full max-w-xl rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-800 flex items-center justify-between bg-slate-900/60">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded-xl bg-emerald-950 border border-emerald-700/60 text-emerald-400">
              <AlertOctagon className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-100 font-sans">
                Log New Wildlife Incident
              </h3>
              <div className="text-xs text-slate-400 font-mono">
                Central Wildlife Sighting & Dispatch Entry
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
            <div className="p-3 rounded-lg bg-red-950/80 border border-red-800 text-red-300 flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-red-400 shrink-0" />
              <span>{errorMsg}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Reporter / Patrol Unit Name *
              </label>
              <input
                type="text"
                required
                value={reporterName}
                onChange={(e) => setReporterName(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3.5 py-2.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-emerald-500"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Elephant Count
              </label>
              <input
                type="number"
                min={1}
                max={50}
                required
                value={elephantCount}
                onChange={(e) => setElephantCount(Number(e.target.value))}
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3.5 py-2.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-emerald-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Latitude (GPS) *
              </label>
              <input
                type="number"
                step="0.0001"
                required
                value={latitude}
                onChange={(e) => setLatitude(Number(e.target.value))}
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3.5 py-2.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-emerald-500 font-mono"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Longitude (GPS) *
              </label>
              <input
                type="number"
                step="0.0001"
                required
                value={longitude}
                onChange={(e) => setLongitude(Number(e.target.value))}
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3.5 py-2.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-emerald-500 font-mono"
              />
            </div>
          </div>

          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Threat Level
              </label>
              <select
                value={threatLevel}
                onChange={(e) => setThreatLevel(e.target.value as ThreatLevel)}
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
              >
                <option value="CRITICAL">CRITICAL</option>
                <option value="HIGH">HIGH</option>
                <option value="CAUTION">CAUTION</option>
                <option value="SAFE">SAFE</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Verification
              </label>
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
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Response
              </label>
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

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">
              Field Remarks / Sighting Notes
            </label>
            <textarea
              rows={3}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="e.g. Herd spotted crossing highway 200m north of Chandil Toll gate. Warning sirens sounded."
              className="w-full bg-slate-950 border border-slate-700 rounded-lg p-3 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-emerald-500"
            />
          </div>

          <div className="flex items-center gap-2 pt-1">
            <input
              type="checkbox"
              id="chk-sos"
              checked={isSos}
              onChange={(e) => setIsSos(e.target.checked)}
              className="w-4 h-4 rounded text-emerald-500 bg-slate-950 border-slate-700 focus:ring-emerald-500"
            />
            <label htmlFor="chk-sos" className="text-xs font-mono text-slate-300 cursor-pointer">
              Broadcast Immediate 5KM Geofence Alert to all nearby mobile devices
            </label>
          </div>

          <div className="pt-2 flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium"
            >
              Cancel
            </button>

            <button
              type="submit"
              disabled={isSubmitting}
              className="px-5 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-xs flex items-center gap-1.5 transition-colors disabled:opacity-50"
            >
              <Send className="w-3.5 h-3.5" />
              <span>{isSubmitting ? 'Logging...' : 'Save & Broadcast'}</span>
            </button>
          </div>
        </form>

      </div>
    </div>
  );
};
