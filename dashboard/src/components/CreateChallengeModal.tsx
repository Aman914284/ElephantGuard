import React, { useState, useEffect } from 'react';
import { X, Sparkles, AlertTriangle, Send } from 'lucide-react';
import { IncidentReport, ThreatLevel, ChallengeStatus } from '../types';

interface CreateChallengeModalProps {
  isOpen: boolean;
  initialIncident?: IncidentReport | null;
  onClose: () => void;
  onSubmitChallenge: (challengeData: {
    incidentId?: string;
    title: string;
    description: string;
    location: string;
    latitude?: number;
    longitude?: number;
    riskLevel: ThreatLevel;
    status: ChallengeStatus;
  }) => Promise<void>;
}

export const CreateChallengeModal: React.FC<CreateChallengeModalProps> = ({
  isOpen,
  initialIncident,
  onClose,
  onSubmitChallenge
}) => {
  if (!isOpen) return null;

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');
  const [riskLevel, setRiskLevel] = useState<ThreatLevel>('HIGH');
  const [status, setStatus] = useState<ChallengeStatus>('Open for Solutions');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  useEffect(() => {
    if (initialIncident) {
      setTitle(`Mitigate Elephant Conflict at Corridor (Lat ${initialIncident.latitude.toFixed(3)}, Lon ${initialIncident.longitude.toFixed(3)})`);
      setDescription(initialIncident.notes || `Recurring elephant sightings (${initialIncident.elephantCount} animals) near highway/village zone. Urgent need for automated deterrent, GIS prediction, or smart fencing.`);
      setLocation(`Dalma Corridor NH-33 / GPS (${initialIncident.latitude.toFixed(4)}, ${initialIncident.longitude.toFixed(4)})`);
      setRiskLevel(initialIncident.threatLevel);
      setStatus('Open for Solutions');
    } else {
      setTitle('');
      setDescription('');
      setLocation('Dalma Wildlife Corridor NH-33');
      setRiskLevel('HIGH');
      setStatus('Open for Solutions');
    }
  }, [initialIncident, isOpen]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !description.trim() || !location.trim()) {
      setErrorMsg('Please fill in all required challenge fields.');
      return;
    }

    setIsSubmitting(true);
    setErrorMsg(null);

    try {
      await onSubmitChallenge({
        incidentId: initialIncident?.id,
        title: title.trim(),
        description: description.trim(),
        location: location.trim(),
        latitude: initialIncident?.latitude,
        longitude: initialIncident?.longitude,
        riskLevel,
        status
      });
      onClose();
    } catch (err: any) {
      setErrorMsg(err.message || 'Failed to publish societal challenge.');
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
            <div className="p-2 rounded-xl bg-indigo-950 border border-indigo-700/60 text-indigo-400">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-100 font-sans">
                Crowdsource Societal Challenge
              </h3>
              <div className="text-xs text-slate-400 font-mono">
                SIH26043 Problem Solving & Multi-Stakeholder Hub
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

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">
              Challenge Title *
            </label>
            <input
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Prevent Night Collision with Elephant Herds on NH-33"
              className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3.5 py-2.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-indigo-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">
              Location / Habitat Zone *
            </label>
            <input
              type="text"
              required
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              placeholder="e.g. NH-33 Asanbani to Mirzadih Fringe Corridor"
              className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3.5 py-2.5 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-indigo-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Risk Level
              </label>
              <select
                value={riskLevel}
                onChange={(e) => setRiskLevel(e.target.value as ThreatLevel)}
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-indigo-500"
              >
                <option value="CRITICAL">CRITICAL</option>
                <option value="HIGH">HIGH</option>
                <option value="CAUTION">CAUTION</option>
                <option value="SAFE">SAFE</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1">
                Challenge Status
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value as ChallengeStatus)}
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-indigo-500"
              >
                <option value="Reported">Reported</option>
                <option value="Verified">Verified</option>
                <option value="Open for Solutions">Open for Solutions</option>
                <option value="In Development">In Development</option>
                <option value="Field Testing">Field Testing</option>
                <option value="Implemented">Implemented</option>
                <option value="Resolved">Resolved</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1">
              Problem Description & Requirements for Universities/Industry *
            </label>
            <textarea
              required
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Detail the exact societal/conflict issue, wildlife behavior, physical bottlenecks, and desired technological innovations..."
              className="w-full bg-slate-950 border border-slate-700 rounded-lg p-3 text-xs text-slate-100 placeholder-slate-500 focus:outline-none focus:border-indigo-500"
            />
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
              className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-xs flex items-center gap-1.5 transition-colors disabled:opacity-50"
            >
              <Send className="w-3.5 h-3.5" />
              <span>{isSubmitting ? 'Publishing...' : 'Publish Challenge'}</span>
            </button>
          </div>
        </form>

      </div>
    </div>
  );
};
