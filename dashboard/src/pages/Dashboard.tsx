import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  LogOut,
  User,
  Shield,
  Radio,
  Satellite,
  Volume2,
  VolumeX,
  Camera,
  AlertTriangle,
  Trees,
  Truck,
  Activity,
  MessageSquare,
  Trash2,
  Sliders,
  Check,
  X
} from 'lucide-react';

declare const L: any;
declare const cocoSsd: any;

export const Dashboard: React.FC = () => {
  const { user, isDemoSession, logout } = useAuth();
  const navigate = useNavigate();

  // Dashboard Telemetry & Alert State
  const [isAlertActive, setIsAlertActive] = useState<boolean>(false);
  const [confidence, setConfidence] = useState<number>(0);
  const [detectedClass, setDetectedClass] = useState<string>('Awaiting Target');
  const [defconLevel, setDefconLevel] = useState<string>('DEFCON 4');
  const [highwaySpeed, setHighwaySpeed] = useState<string>('60 KM/H');
  const [isAudioMuted, setIsAudioMuted] = useState<boolean>(false);
  const [isThermalSimActive, setIsThermalSimActive] = useState<boolean>(false);
  const [mockTriggerEnabled, setMockTriggerEnabled] = useState<boolean>(true);

  // Modals
  const [isSmsModalOpen, setIsSmsModalOpen] = useState<boolean>(false);
  const [isQrtModalOpen, setIsQrtModalOpen] = useState<boolean>(false);

  // Logs
  const [logs, setLogs] = useState<Array<{ id: string; time: string; type: string; title: string; desc: string; highlight?: string }>>([
    {
      id: '1',
      time: new Date().toLocaleTimeString('en-IN', { hour12: false }),
      type: 'SYSTEM READY',
      title: 'Corridor Matrix Initialized',
      desc: 'All 16 optical & acoustic edge nodes synced with Dalma Control Room. Geofence buffers active.',
    }
  ]);

  // Clock
  const [currentTime, setCurrentTime] = useState<string>('');
  const [currentDate, setCurrentDate] = useState<string>('');

  // Refs
  const mapContainerRef = useRef<HTMLDivElement | null>(null);
  const mapInstanceRef = useRef<any>(null);
  const hazardCircleRef = useRef<any>(null);
  const preWarningCircleRef = useRef<any>(null);
  const alertMarkerRef = useRef<any>(null);
  const villagesLayerRef = useRef<any>(null);

  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const infraCanvasRef = useRef<HTMLCanvasElement | null>(null);
  const audioCtxRef = useRef<AudioContext | null>(null);
  const sirenOsc1Ref = useRef<OscillatorNode | null>(null);
  const sirenOsc2Ref = useRef<OscillatorNode | null>(null);
  const sirenGainRef = useRef<GainNode | null>(null);
  const sirenIntervalRef = useRef<any>(null);
  const thermalAnimRef = useRef<number | null>(null);
  const elephantSimXRef = useRef<number>(-80);

  const DALMA_COORDS = [22.8942, 86.2081]; // Dalma Pass, NH-33

  // =========================================================================
  // 1. Clock updater
  // =========================================================================
  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setCurrentTime(now.toLocaleTimeString('en-IN', { hour12: false }) + ' IST');
      setCurrentDate(now.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }).toUpperCase());
    };
    updateTime();
    const timer = setInterval(updateTime, 1000);
    return () => clearInterval(timer);
  }, []);

  // =========================================================================
  // 2. Audio Siren Implementation (Web Audio API)
  // =========================================================================
  const initAudioContext = () => {
    if (!audioCtxRef.current) {
      const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
      audioCtxRef.current = new AudioCtx();
    }
    if (audioCtxRef.current.state === 'suspended') {
      audioCtxRef.current.resume();
    }
  };

  const startSiren = () => {
    if (isAudioMuted) return;
    try {
      initAudioContext();
      stopSiren();

      const ctx = audioCtxRef.current!;
      const gain = ctx.createGain();
      gain.gain.setValueAtTime(0.12, ctx.currentTime);
      gain.connect(ctx.destination);
      sirenGainRef.current = gain;

      const osc1 = ctx.createOscillator();
      const osc2 = ctx.createOscillator();
      osc1.type = 'sawtooth';
      osc2.type = 'square';
      osc1.connect(gain);
      osc2.connect(gain);
      osc1.start();
      osc2.start();
      sirenOsc1Ref.current = osc1;
      sirenOsc2Ref.current = osc2;

      let toggle = false;
      sirenIntervalRef.current = setInterval(() => {
        if (!audioCtxRef.current || !sirenOsc1Ref.current || !sirenOsc2Ref.current) return;
        const now = audioCtxRef.current.currentTime;
        sirenOsc1Ref.current.frequency.setTargetAtTime(toggle ? 920 : 640, now, 0.08);
        sirenOsc2Ref.current.frequency.setTargetAtTime(toggle ? 460 : 320, now, 0.08);
        toggle = !toggle;
      }, 220);
    } catch (e) {
      console.warn("Audio Context init suppressed:", e);
    }
  };

  const stopSiren = () => {
    if (sirenIntervalRef.current) {
      clearInterval(sirenIntervalRef.current);
      sirenIntervalRef.current = null;
    }
    if (sirenOsc1Ref.current) {
      try { sirenOsc1Ref.current.stop(); sirenOsc1Ref.current.disconnect(); } catch (e) {}
      sirenOsc1Ref.current = null;
    }
    if (sirenOsc2Ref.current) {
      try { sirenOsc2Ref.current.stop(); sirenOsc2Ref.current.disconnect(); } catch (e) {}
      sirenOsc2Ref.current = null;
    }
    if (sirenGainRef.current) {
      try { sirenGainRef.current.disconnect(); } catch (e) {}
      sirenGainRef.current = null;
    }
  };

  const speakVoiceAlert = () => {
    if ('speechSynthesis' in window && !isAudioMuted) {
      window.speechSynthesis.cancel();
      const msg = new SpeechSynthesisUtterance("Warning! Elephant detected on highway corridor! Slow down.");
      msg.rate = 1.0;
      msg.pitch = 1.05;
      window.speechSynthesis.speak(msg);
    }
  };

  // =========================================================================
  // 3. Leaflet Map Initialization
  // =========================================================================
  useEffect(() => {
    if (!mapContainerRef.current || mapInstanceRef.current) return;

    if (typeof L !== 'undefined') {
      const map = L.map(mapContainerRef.current, {
        center: DALMA_COORDS,
        zoom: 13,
        zoomControl: true,
        attributionControl: false
      });

      L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        maxZoom: 19,
        subdomains: 'abcd',
      }).addTo(map);

      // Dalma Sanctuary Core Polygon
      const dalmaBoundary = [
        [22.9250, 86.1700],
        [22.9400, 86.2200],
        [22.9150, 86.2600],
        [22.8850, 86.2500],
        [22.8750, 86.1900],
      ];
      L.polygon(dalmaBoundary, {
        color: '#10b981',
        fillColor: '#10b981',
        fillOpacity: 0.12,
        weight: 1.5,
        dashArray: '5, 5'
      }).addTo(map).bindPopup("<b style='color:#10b981'>Dalma Wildlife Sanctuary</b><br><span style='font-size:11px'>Elephant Natural Migration Core Area</span>");

      // NH-33 Corridor Line
      const nh33Path = [
        [22.8550, 86.1600],
        [22.8720, 86.1820],
        [22.8942, 86.2081],
        [22.9100, 86.2250],
        [22.9350, 86.2400],
        [22.9600, 86.2600],
      ];
      L.polyline(nh33Path, {
        color: '#06b6d4',
        weight: 4,
        opacity: 0.85,
        dashArray: '8, 8'
      }).addTo(map).bindPopup("<b style='color:#06b6d4'>National Highway NH-33</b><br><span style='font-size:11px'>Ranchi-Jamshedpur Corridor (Crossing Point)</span>");

      // Sensor Nodes
      const sensors = [
        { name: "Node CAM-01 (Pardih)", coords: [22.8720, 86.1820] },
        { name: "Node CAM-04 (Dalma Pass)", coords: [22.8942, 86.2081] },
        { name: "Node CAM-07 (Asanbani)", coords: [22.9100, 86.2250] },
      ];
      sensors.forEach(s => {
        L.circleMarker(s.coords, {
          radius: 5,
          color: '#06b6d4',
          fillColor: '#22d3ee',
          fillOpacity: 0.9
        }).addTo(map).bindPopup(`<b>${s.name}</b><br>Optical Edge AI & Infrasound`);
      });

      // Villages
      const villages = [
        { name: "Pardih Village", coords: [22.8780, 86.1750], pop: "280 residents" },
        { name: "Dimna Cluster", coords: [22.8650, 86.2300], pop: "410 residents" },
        { name: "Asanbani Basti", coords: [22.9050, 86.2350], pop: "190 residents" },
        { name: "Mirzapur Hamlet", coords: [22.8880, 86.1950], pop: "150 residents" },
        { name: "Bota Forest Post", coords: [22.9200, 86.2100], pop: "210 residents" },
      ];
      const vGroup = L.layerGroup();
      villages.forEach(v => {
        const marker = L.marker(v.coords, {
          icon: L.divIcon({
            className: 'custom-icon',
            html: `<div style="background:#f59e0b; width:10px; height:10px; border-radius:50%; border:2px solid #fff; box-shadow:0 0 8px #f59e0b;"></div>`,
            iconSize: [10, 10],
            iconAnchor: [5, 5]
          })
        }).bindPopup(`<b>${v.name}</b><br>Pop: ${v.pop}`);
        vGroup.addLayer(marker);
      });
      vGroup.addTo(map);
      villagesLayerRef.current = vGroup;

      mapInstanceRef.current = map;
    }

    return () => {
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }
    };
  }, []);

  // Draw Geo-Fences on Alert
  const renderAlertGeofence = (coords: number[]) => {
    const map = mapInstanceRef.current;
    if (!map || typeof L === 'undefined') return;

    clearAlertGeofence();

    // 1000m Red Hazard Zone
    hazardCircleRef.current = L.circle(coords, {
      radius: 1000,
      color: '#ef4444',
      fillColor: '#ef4444',
      fillOpacity: 0.28,
      weight: 2.5,
      dashArray: '6, 6'
    }).addTo(map).bindPopup("<b style='color:#ef4444'>CRITICAL COLLISION ZONE (1000m)</b><br>Speed limit restricted to 20 km/h.");

    // 2500m Amber Buffer Zone
    preWarningCircleRef.current = L.circle(coords, {
      radius: 2500,
      color: '#f59e0b',
      fillColor: '#f59e0b',
      fillOpacity: 0.12,
      weight: 2,
      dashArray: '8, 8'
    }).addTo(map).bindPopup("<b style='color:#f59e0b'>VILLAGE BUFFER ZONE (2500m)</b><br>SMS Cell Broadcast Dispatched.");

    // Pulsing Marker
    alertMarkerRef.current = L.marker(coords, {
      icon: L.divIcon({
        className: 'pulse-marker-hazard',
        html: '<div class="pulse-marker-hazard-core"></div>',
        iconSize: [32, 32],
        iconAnchor: [16, 16]
      })
    }).addTo(map).bindPopup(`
      <div style="font-family:monospace; font-size:11px;">
        <b style="color:#ef4444; font-size:12px;">🚨 ELEPHANT SIGHTING ACTIVE</b><br>
        <b>Coords:</b> ${coords[0].toFixed(4)}°N, ${coords[1].toFixed(4)}°E<br>
        <b>Herd Size:</b> 3-5 Adults + 1 Calf<br>
        <b>Velocity:</b> 4.2 km/h North-East
      </div>
    `).openPopup();

    map.flyTo(coords, 14, { animate: true, duration: 1.5 });
  };

  const clearAlertGeofence = () => {
    const map = mapInstanceRef.current;
    if (!map) return;
    if (hazardCircleRef.current) { map.removeLayer(hazardCircleRef.current); hazardCircleRef.current = null; }
    if (preWarningCircleRef.current) { map.removeLayer(preWarningCircleRef.current); preWarningCircleRef.current = null; }
    if (alertMarkerRef.current) { map.removeLayer(alertMarkerRef.current); alertMarkerRef.current = null; }
  };

  // =========================================================================
  // 4. Infrasound Acoustic Canvas Loop
  // =========================================================================
  useEffect(() => {
    const canvas = infraCanvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animId: number;
    let phase = 0;

    const render = () => {
      const w = (canvas.width = canvas.offsetWidth);
      const h = (canvas.height = canvas.offsetHeight);

      ctx.fillStyle = '#020617';
      ctx.fillRect(0, 0, w, h);

      // Grid
      ctx.strokeStyle = 'rgba(51, 68, 121, 0.25)';
      ctx.lineWidth = 1;
      ctx.beginPath();
      for (let x = 0; x < w; x += 30) { ctx.moveTo(x, 0); ctx.lineTo(x, h); }
      for (let y = 0; y < h; y += 15) { ctx.moveTo(0, y); ctx.lineTo(w, y); }
      ctx.stroke();

      // Waveform
      const amp = isAlertActive ? 18 : 5;
      const freq = isAlertActive ? 0.08 : 0.03;
      const col = isAlertActive ? '#ef4444' : '#06b6d4';

      ctx.beginPath();
      ctx.strokeStyle = col;
      ctx.lineWidth = isAlertActive ? 2.5 : 1.5;
      for (let x = 0; x < w; x++) {
        const y = h / 2 + Math.sin(x * freq + phase) * amp + Math.cos(x * 0.02 - phase * 1.5) * (amp * 0.4);
        if (x === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      }
      ctx.stroke();

      phase += isAlertActive ? 0.15 : 0.04;
      animId = requestAnimationFrame(render);
    };

    render();
    return () => cancelAnimationFrame(animId);
  }, [isAlertActive]);

  // =========================================================================
  // 5. Alert Trigger & Reset Handlers
  // =========================================================================
  const triggerAlert = (source = "Thermal Camera Node CAM-04", conf = 0.94) => {
    initAudioContext();
    setIsAlertActive(true);
    setConfidence(conf * 100);
    setDetectedClass('ELEPHANT');
    setDefconLevel('DEFCON 1');
    setHighwaySpeed('20 KM/H');

    renderAlertGeofence(DALMA_COORDS);
    startSiren();
    speakVoiceAlert();

    const timeStr = new Date().toLocaleTimeString('en-IN', { hour12: false });
    setLogs(prev => [
      {
        id: Date.now().toString(),
        time: timeStr,
        type: 'CRITICAL SIGHTING',
        title: 'Elephant Herd Crossing NH-33',
        desc: `Detection: ${source} • Confidence: ${(conf * 100).toFixed(0)}% • Loc: Dalma Wildlife Pass (22.8942° N, 86.2081° E)`,
        highlight: 'SMS Broadcast Sent (1,240 nodes) • VMS Speed: 20 km/h'
      },
      ...prev
    ]);
  };

  const resetAlert = () => {
    setIsAlertActive(false);
    setConfidence(0);
    setDetectedClass('Awaiting Target');
    setDefconLevel('DEFCON 4');
    setHighwaySpeed('60 KM/H');
    stopSiren();
    clearAlertGeofence();

    if (mapInstanceRef.current) {
      mapInstanceRef.current.flyTo(DALMA_COORDS, 13, { animate: true, duration: 1.2 });
    }

    const timeStr = new Date().toLocaleTimeString('en-IN', { hour12: false });
    setLogs(prev => [
      {
        id: Date.now().toString(),
        time: timeStr,
        type: 'STATUS UPDATE',
        title: 'Corridor All Clear / Alarm Dismissed',
        desc: `Authorized by: ${user?.name || 'Operator'} (${user?.role || 'Officer'})`,
      },
      ...prev
    ]);
  };

  // =========================================================================
  // 6. Thermal Simulation Animation Loop
  // =========================================================================
  const startThermalSimulation = () => {
    initAudioContext();
    setIsThermalSimActive(true);
  };

  const stopThermalSimulation = () => {
    setIsThermalSimActive(false);
    if (thermalAnimRef.current) cancelAnimationFrame(thermalAnimRef.current);
  };

  useEffect(() => {
    if (!isThermalSimActive || !canvasRef.current) return;
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const renderThermal = () => {
      const w = (canvas.width = 640);
      const h = (canvas.height = 480);

      // Gradient
      const grad = ctx.createLinearGradient(0, 0, 0, h);
      grad.addColorStop(0, '#090a1f');
      grad.addColorStop(0.5, '#120d2a');
      grad.addColorStop(1, '#05030d');
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, w, h);

      // Trees
      ctx.fillStyle = '#081226';
      ctx.beginPath();
      ctx.moveTo(0, h * 0.65);
      for (let i = 0; i <= w; i += 40) {
        ctx.lineTo(i, h * 0.65 - (Math.sin(i * 0.05) * 20 + Math.cos(i * 0.1) * 15));
      }
      ctx.lineTo(w, h);
      ctx.lineTo(0, h);
      ctx.fill();

      // Road
      ctx.fillStyle = '#1c1538';
      ctx.beginPath();
      ctx.moveTo(w * 0.2, h);
      ctx.lineTo(w * 0.45, h * 0.6);
      ctx.lineTo(w * 0.55, h * 0.6);
      ctx.lineTo(w * 0.8, h);
      ctx.fill();

      // Elephant Silhouette
      elephantSimXRef.current += 1.2;
      if (elephantSimXRef.current > w + 100) elephantSimXRef.current = -100;
      const ex = elephantSimXRef.current;
      const ey = h * 0.65;

      // Draw Elephant
      ctx.save();
      ctx.shadowColor = '#f97316';
      ctx.shadowBlur = 18;
      ctx.fillStyle = '#ea580c';
      ctx.beginPath();
      ctx.ellipse(ex + 40, ey - 20, 45, 30, 0, 0, Math.PI * 2);
      ctx.fill();
      ctx.beginPath();
      ctx.ellipse(ex + 85, ey - 30, 22, 22, 0, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();

      // Bounding Box
      if (ex > 120 && ex < w - 120) {
        ctx.strokeStyle = '#ef4444';
        ctx.lineWidth = 3;
        ctx.strokeRect(ex - 20, ey - 70, 140, 110);
        ctx.fillStyle = 'rgba(239, 68, 68, 0.9)';
        ctx.fillRect(ex - 20, ey - 94, 150, 22);
        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 12px "Orbitron", monospace';
        ctx.fillText('ELEPHANT 94%', ex - 14, ey - 78);

        if (!isAlertActive) {
          triggerAlert('Thermal Optics CAM-04 (Simulated Elephant)', 0.94);
        }
      }

      thermalAnimRef.current = requestAnimationFrame(renderThermal);
    };

    renderThermal();
    return () => {
      if (thermalAnimRef.current) cancelAnimationFrame(thermalAnimRef.current);
    };
  }, [isThermalSimActive, isAlertActive]);

  // Handle Logout
  const handleLogout = () => {
    stopSiren();
    logout();
    navigate('/login', { replace: true });
  };

  const getRoleBadgeColor = (role?: string) => {
    switch (role) {
      case 'Administrator': return 'bg-purple-950/80 text-purple-300 border-purple-700/60';
      case 'Forest Officer': return 'bg-emerald-950/80 text-emerald-300 border-emerald-700/60';
      case 'QRT Operator': return 'bg-red-950/80 text-red-300 border-red-700/60';
      case 'Analyst': return 'bg-cyan-950/80 text-cyan-300 border-cyan-700/60';
      default: return 'bg-slate-900 text-slate-300 border-slate-700';
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-between bg-[#020617] text-slate-100 selection:bg-cyan-500 selection:text-black">
      
      {/* ========================================================================= */}
      {/* 1. TOP TACTICAL HEADER WITH AUTHENTICATED USER SESSION BADGE */}
      {/* ========================================================================= */}
      <header className="sticky top-0 z-50 glass-panel border-b border-slate-800/80 px-4 lg:px-8 py-3 backdrop-blur-xl">
        <div className="max-w-[1920px] mx-auto flex flex-wrap items-center justify-between gap-4">
          
          {/* Logo & Title */}
          <div className="flex items-center gap-3.5">
            <div className="relative flex items-center justify-center w-11 h-11 rounded-xl bg-gradient-to-br from-slate-900 to-slate-800 border border-cyan-500/40 shadow-lg shadow-cyan-950/50">
              <Satellite className="w-5 h-5 text-cyan-400 animate-pulse" />
              <span className="absolute -top-1 -right-1 flex h-2.5 w-2.5">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
                <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500" />
              </span>
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-tactical tracking-widest text-lg lg:text-xl font-black bg-clip-text text-transparent bg-gradient-to-r from-cyan-400 via-emerald-300 to-amber-300">
                  SEEMS-AI
                </span>
                <span className="text-[10px] px-2 py-0.5 rounded font-mono font-semibold bg-cyan-950/80 text-cyan-300 border border-cyan-700/50">
                  DALMA CORRIDOR NH-33
                </span>
                {isDemoSession && (
                  <span className="text-[10px] px-2 py-0.5 rounded font-mono font-bold bg-amber-950/90 text-amber-300 border border-amber-500/50 animate-pulse">
                    DEMO SESSION
                  </span>
                )}
              </div>
              <p className="text-[11px] text-slate-400 font-medium tracking-wide">
                Autonomous Bio-Spatial Detection & Highway Geo-Fencing Matrix
              </p>
            </div>
          </div>

          {/* Right Session Controls & Telemetry */}
          <div className="flex items-center gap-3 flex-wrap">
            
            {/* Clock */}
            <div className="hidden sm:flex items-center gap-2.5 px-3 py-1.5 rounded-xl bg-slate-950/80 border border-slate-800 font-mono text-xs">
              <div className="text-right">
                <div className="font-tactical font-bold text-cyan-300">{currentTime || '00:00:00 IST'}</div>
                <div className="text-[9px] text-slate-500">{currentDate || '02 SEP 2026'}</div>
              </div>
            </div>

            {/* System Status Pill */}
            <div
              className={`flex items-center gap-2 px-3.5 py-1.5 rounded-xl border text-xs font-tactical font-bold uppercase transition-all duration-300 ${
                isAlertActive
                  ? 'bg-red-950/80 border-red-500 text-red-200 shadow-xl shadow-red-950 animate-pulse'
                  : 'bg-emerald-950/60 border-emerald-500/50 text-emerald-300'
              }`}
            >
              <span className="relative flex h-2.5 w-2.5">
                <span className={`animate-ping absolute inline-flex h-full w-full rounded-full ${isAlertActive ? 'bg-red-400' : 'bg-emerald-400'} opacity-75`} />
                <span className={`relative inline-flex rounded-full h-2.5 w-2.5 ${isAlertActive ? 'bg-red-500' : 'bg-emerald-500'}`} />
              </span>
              <span>{isAlertActive ? 'CRITICAL INTRUSION' : 'ZONE SECURE'}</span>
            </div>

            {/* Action Buttons */}
            <button
              onClick={() => isAlertActive ? resetAlert() : triggerAlert("Manual Sighting Demo", 0.96)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-amber-500/20 hover:bg-amber-500/30 text-amber-300 border border-amber-500/40 font-tactical font-bold text-xs tracking-wider transition active:scale-95"
            >
              <AlertTriangle className="w-3.5 h-3.5 text-amber-400" />
              <span>{isAlertActive ? 'RESET ALERT' : 'SIMULATE SIGHTING'}</span>
            </button>

            <button
              onClick={() => setIsAudioMuted(!isAudioMuted)}
              title="Toggle Warning Audio"
              className="p-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 border border-slate-700 transition"
            >
              {isAudioMuted ? <VolumeX className="w-4 h-4 text-red-400" /> : <Volume2 className="w-4 h-4 text-cyan-400" />}
            </button>

            {/* Authenticated User Profile & Logout */}
            <div className="flex items-center gap-2.5 pl-2 border-l border-slate-800">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-lg bg-cyan-950 border border-cyan-500/40 flex items-center justify-center text-cyan-300 text-xs font-bold">
                  <User className="w-4 h-4" />
                </div>
                <div className="hidden md:block text-left text-xs leading-tight">
                  <div className="font-semibold text-slate-200 truncate max-w-[130px]">
                    {user?.name || 'Officer'}
                  </div>
                  <span className={`inline-block px-1.5 py-0.2 text-[9px] font-mono rounded border ${getRoleBadgeColor(user?.role)}`}>
                    {user?.role || 'Forest Officer'}
                  </span>
                </div>
              </div>

              <button
                onClick={handleLogout}
                title="Sign out of SEEMS-AI"
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-red-950/60 hover:bg-red-900/80 text-red-300 border border-red-800/50 font-mono text-xs font-bold transition shadow-sm"
              >
                <LogOut className="w-3.5 h-3.5" />
                <span className="hidden sm:inline">LOGOUT</span>
              </button>
            </div>

          </div>

        </div>
      </header>

      {/* ========================================================================= */}
      {/* 2. CRITICAL THREAT EMERGENCY BANNER (Unhides on elephant detection) */}
      {/* ========================================================================= */}
      {isAlertActive && (
        <div className="relative z-40 bg-gradient-to-r from-red-950 via-red-900 to-amber-950 border-b-2 border-red-500 px-4 py-3 shadow-2xl animate-pulse">
          <div className="max-w-[1920px] mx-auto flex flex-col md:flex-row items-center justify-between gap-3 text-red-100">
            <div className="flex items-center gap-3">
              <div className="p-2 rounded-lg bg-red-600/30 border border-red-400">
                <AlertTriangle className="w-5 h-5 text-red-300" />
              </div>
              <div>
                <span className="font-tactical font-black text-xs md:text-sm tracking-widest text-red-300 uppercase block">
                  CRITICAL INTRUSION PROTOCOL LEVEL-1 ACTIVATED
                </span>
                <p className="text-xs text-red-100 font-medium">
                  Elephant herd detected crossing NH-33 Dalma Pass! Highway speed restricted to <span className="font-bold text-amber-300 underline">20 km/h</span>. Automated cellular broadcast dispatched to <span className="font-bold text-amber-300">1,240 villagers</span> across 5 clusters.
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2 shrink-0">
              <button
                onClick={() => setIsSmsModalOpen(true)}
                className="px-3 py-1.5 rounded-lg bg-red-950 hover:bg-red-900 border border-red-400/60 text-xs font-mono font-bold text-red-200 transition flex items-center gap-1.5"
              >
                <MessageSquare className="w-3.5 h-3.5" />
                VIEW SMS PAYLOAD
              </button>
              <button
                onClick={() => setIsQrtModalOpen(true)}
                className="px-3 py-1.5 rounded-lg bg-amber-600 hover:bg-amber-500 text-slate-950 font-tactical font-bold text-xs transition flex items-center gap-1.5 shadow-md"
              >
                <Truck className="w-3.5 h-3.5" />
                DISPATCH QRT
              </button>
              <button
                onClick={resetAlert}
                className="px-3 py-1.5 rounded-lg bg-slate-900/80 hover:bg-slate-800 border border-slate-600 text-xs font-mono text-slate-300 transition"
              >
                DISMISS
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ========================================================================= */}
      {/* 3. MAIN DASHBOARD CONTENT */}
      {/* ========================================================================= */}
      <main className="max-w-[1920px] w-full mx-auto p-4 lg:p-6 space-y-6 flex-1">
        
        {/* Top 4 Telemetry Stat Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          
          {/* Card 1: Edge Nodes */}
          <div className="glass-panel rounded-2xl p-4 relative overflow-hidden hud-corner">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-mono uppercase tracking-wider text-slate-400">EDGE NODES ONLINE</span>
              <span className="p-2 rounded-lg bg-cyan-950/80 text-cyan-400 border border-cyan-800/40 text-xs">
                <Radio className="w-3.5 h-3.5" />
              </span>
            </div>
            <div className="flex items-baseline gap-2">
              <span className="font-tactical text-2xl lg:text-3xl font-black text-cyan-300">16 / 16</span>
              <span className="text-xs text-emerald-400 font-mono flex items-center gap-1">
                <Check className="w-3 h-3" /> 100% OPERATIONAL
              </span>
            </div>
            <div className="mt-3 flex items-center justify-between text-[11px] text-slate-400 border-t border-slate-800/80 pt-2 font-mono">
              <span>Optical Grid: 8</span>
              <span>Thermal IR: 4</span>
              <span>Infrasound: 4</span>
            </div>
          </div>

          {/* Card 2: Detection Confidence */}
          <div className="glass-panel rounded-2xl p-4 relative overflow-hidden hud-corner">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-mono uppercase tracking-wider text-slate-400">DETECTION CONFIDENCE</span>
              <span className="p-2 rounded-lg bg-emerald-950/80 text-emerald-400 border border-emerald-800/40 text-xs">
                <Activity className="w-3.5 h-3.5" />
              </span>
            </div>
            <div className="flex items-baseline gap-2">
              <span className={`font-tactical text-2xl lg:text-3xl font-black ${confidence > 50 ? 'text-red-400' : 'text-slate-300'}`}>
                {confidence.toFixed(1)}%
              </span>
              <span className={`text-xs font-mono uppercase px-2 py-0.5 rounded border ${confidence > 50 ? 'bg-red-950 text-red-300 border-red-800' : 'bg-slate-900 text-slate-400 border-slate-800'}`}>
                {detectedClass}
              </span>
            </div>
            <div className="mt-3 w-full bg-slate-950 rounded-full h-2 overflow-hidden border border-slate-800">
              <div
                className="h-full bg-gradient-to-r from-cyan-500 via-emerald-400 to-red-500 transition-all duration-300"
                style={{ width: `${confidence}%` }}
              />
            </div>
          </div>

          {/* Card 3: Defcon Level */}
          <div className="glass-panel rounded-2xl p-4 relative overflow-hidden hud-corner">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-mono uppercase tracking-wider text-slate-400">CORRIDOR DEFCON LEVEL</span>
              <span className={`p-2 rounded-lg text-xs border ${isAlertActive ? 'bg-red-950 text-red-400 border-red-800' : 'bg-slate-900 text-emerald-400 border-slate-800'}`}>
                <Shield className="w-3.5 h-3.5" />
              </span>
            </div>
            <div className="flex items-baseline gap-2">
              <span className={`font-tactical text-2xl lg:text-3xl font-black ${isAlertActive ? 'text-red-400 animate-pulse' : 'text-emerald-400'}`}>
                {defconLevel}
              </span>
              <span className="text-xs text-slate-400 font-mono">
                {isAlertActive ? 'HIGHWAY INTERCEPTION' : 'NORMAL PATROL'}
              </span>
            </div>
            <div className="mt-3 flex items-center justify-between text-[11px] text-slate-400 border-t border-slate-800/80 pt-2 font-mono">
              <span>NH-33 Speed: <b className={isAlertActive ? 'text-red-400 font-black' : 'text-cyan-300'}>{highwaySpeed}</b></span>
              <span className={isAlertActive ? 'text-red-400 font-bold' : 'text-emerald-400 font-bold'}>
                {isAlertActive ? 'RESTRICTED' : 'ALL CLEAR'}
              </span>
            </div>
          </div>

          {/* Card 4: Cell Broadcast Gateway */}
          <div className="glass-panel rounded-2xl p-4 relative overflow-hidden hud-corner">
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-mono uppercase tracking-wider text-slate-400">CELL BROADCAST GATEWAY</span>
              <span className="p-2 rounded-lg bg-amber-950/80 text-amber-400 border border-amber-800/40 text-xs">
                <Radio className="w-3.5 h-3.5" />
              </span>
            </div>
            <div className="flex items-baseline gap-2">
              <span className="font-tactical text-2xl lg:text-3xl font-black text-amber-300">
                {isAlertActive ? 'DISPATCHED' : 'STANDBY'}
              </span>
              <span className="text-xs text-slate-400 font-mono">5 TOWERS SYNCED</span>
            </div>
            <div className="mt-3 flex items-center justify-between text-[11px] text-slate-400 border-t border-slate-800/80 pt-2 font-mono">
              <span>Villages: Pardih, Dimna +3</span>
              <button onClick={() => setIsSmsModalOpen(true)} className="text-cyan-400 hover:text-cyan-300 underline">
                Preview SMS
              </button>
            </div>
          </div>

        </div>

        {/* Middle Split: Left = Computer Vision Optics & Right = Spatial Radar Map */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          
          {/* Left Column: Computer Vision & Acoustic HUD (5 Cols) */}
          <div className="lg:col-span-5 space-y-4">
            
            {/* Optical Container Card */}
            <div className="glass-panel rounded-3xl p-4 border border-slate-800/80 shadow-2xl relative overflow-hidden">
              <div className="flex items-center justify-between pb-3 border-b border-slate-800/80 text-xs font-mono">
                <div className="flex items-center gap-2">
                  <span className="flex h-2 w-2 rounded-full bg-cyan-400 animate-ping" />
                  <span className="font-tactical font-bold text-cyan-300 uppercase">CAM-04 (NH-33 DALMA CORRIDOR)</span>
                </div>
                <span className="text-[10px] text-emerald-400 bg-emerald-950/80 px-2 py-0.5 rounded border border-emerald-800">
                  COCO-SSD / THERMAL READY
                </span>
              </div>

              {/* Video Box */}
              <div className="relative mt-3 rounded-2xl overflow-hidden bg-slate-950 aspect-video flex items-center justify-center border border-slate-800 shadow-inner">
                <canvas ref={canvasRef} className="absolute inset-0 w-full h-full object-cover z-10" />

                {!isThermalSimActive && (
                  <div className="absolute inset-0 z-20 flex flex-col items-center justify-center text-center p-6 bg-gradient-to-b from-slate-900 to-slate-950 text-slate-400">
                    <div className="w-14 h-14 rounded-2xl bg-cyan-950/40 border border-cyan-500/30 flex items-center justify-center text-cyan-400 text-2xl mb-3 shadow-lg shadow-cyan-950">
                      <Camera className="w-6 h-6" />
                    </div>
                    <p className="font-tactical tracking-wider text-sm font-bold text-slate-200 mb-1">
                      EDGE OPTICS STANDBY
                    </p>
                    <p className="text-xs text-slate-400 max-w-xs mb-4">
                      Click below to launch the simulated infrared thermal wildlife tracking feed.
                    </p>
                    <button
                      onClick={startThermalSimulation}
                      className="px-4 py-2 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-slate-950 font-tactical font-bold text-xs tracking-wider transition shadow-lg shadow-cyan-950"
                    >
                      RUN THERMAL SIMULATION
                    </button>
                  </div>
                )}

                {isThermalSimActive && (
                  <div className="absolute top-3 right-3 z-30 flex items-center gap-2">
                    <button
                      onClick={stopThermalSimulation}
                      className="px-2.5 py-1 rounded-lg bg-red-600/90 text-white font-mono text-[10px] font-bold"
                    >
                      STOP SIM
                    </button>
                  </div>
                )}
              </div>

              {/* Desk Testing Mock Options */}
              <div className="mt-3 p-3 rounded-xl bg-slate-950/70 border border-slate-800 flex items-center justify-between text-xs font-mono">
                <div className="flex items-center gap-2 text-slate-300">
                  <Sliders className="w-3.5 h-3.5 text-cyan-400" />
                  <span>Desk Trigger:</span>
                </div>
                <label className="flex items-center gap-2 cursor-pointer select-none text-cyan-300 font-bold">
                  <input
                    type="checkbox"
                    checked={mockTriggerEnabled}
                    onChange={(e) => setMockTriggerEnabled(e.target.checked)}
                    className="rounded bg-slate-900 border-slate-700 text-cyan-600"
                  />
                  <span>All Targets (Person / Animal / Elephant)</span>
                </label>
              </div>

            </div>

            {/* Smart VMS Billboard */}
            <div className="glass-panel rounded-2xl p-4 border border-slate-800/80">
              <div className="flex items-center justify-between mb-2 text-xs font-mono text-slate-400">
                <span>HIGHWAY NH-33 SMART VMS BILLBOARD</span>
                <span className="text-[10px] text-emerald-400 bg-emerald-950/80 px-2 py-0.5 rounded border border-emerald-800">
                  NODE #04 ACTIVE
                </span>
              </div>
              <div className={`rounded-xl p-3 text-center border-2 transition-all duration-500 ${isAlertActive ? 'bg-black border-red-600 animate-pulse' : 'bg-black border-slate-800'}`}>
                <div className={`font-tactical font-black text-sm lg:text-base tracking-widest uppercase ${isAlertActive ? 'text-red-500' : 'text-amber-400'}`}>
                  {isAlertActive ? '⚠️ ELEPHANT CROSSING AHEAD ⚠️' : 'NH-33 DALMA CORRIDOR CLEAR'}
                </div>
                <div className={`font-mono text-xs mt-1 ${isAlertActive ? 'text-amber-300 font-bold' : 'text-emerald-400'}`}>
                  {isAlertActive ? 'SPEED RESTRICTED: 20 KM/H | FOREST SQUAD EN ROUTE' : 'STANDARD SPEED LIMIT: 60 KM/H | DRIVE SAFELY'}
                </div>
              </div>
            </div>

            {/* Infrasound Acoustic Canvas */}
            <div className="glass-panel rounded-2xl p-4 border border-slate-800/80">
              <div className="flex items-center justify-between mb-2 text-xs font-mono text-slate-400">
                <span>INFRASOUND SEISMIC SENSORS (14-24 Hz)</span>
                <span className={`text-[10px] px-2 py-0.5 rounded border ${isAlertActive ? 'bg-red-950 text-red-300 border-red-800 font-bold' : 'bg-slate-900 text-slate-400 border-slate-800'}`}>
                  {isAlertActive ? 'SEISMIC RUMBLE (18 Hz)' : 'LOW RUMBLE'}
                </span>
              </div>
              <canvas ref={infraCanvasRef} className="w-full h-14 bg-slate-950 rounded-xl border border-slate-800" />
            </div>

          </div>

          {/* Right Column: Spatial Radar Map & Logs (7 Cols) */}
          <div className="lg:col-span-7 space-y-4">
            
            {/* Map Card */}
            <div className="glass-panel rounded-3xl p-4 border border-slate-800/80 shadow-2xl relative overflow-hidden">
              <div className="flex items-center justify-between pb-3 border-b border-slate-800/80">
                <div className="flex items-center gap-2">
                  <Trees className="w-4 h-4 text-emerald-400" />
                  <span className="font-tactical font-bold text-sm tracking-wider text-slate-200">
                    DALMA SANCTUARY GEOSPATIAL RADAR (NH-33)
                  </span>
                </div>
                <div className="text-xs font-mono text-slate-400">
                  <button
                    onClick={() => mapInstanceRef.current?.flyTo(DALMA_COORDS, 13)}
                    className="px-2.5 py-1 rounded-lg bg-slate-900 hover:bg-slate-800 border border-slate-700 text-cyan-300 transition"
                  >
                    RECENTER
                  </button>
                </div>
              </div>

              {/* Leaflet Map Box */}
              <div className="relative mt-3 rounded-2xl overflow-hidden border border-slate-800 h-[420px] lg:h-[480px]">
                <div ref={mapContainerRef} className="w-full h-full z-0" />
                
                {/* Legend */}
                <div className="absolute bottom-3 left-3 z-[1000] bg-slate-950/90 backdrop-blur-md p-2.5 rounded-xl border border-slate-800 text-[10px] font-mono space-y-1 shadow-xl">
                  <div className="font-tactical font-bold text-slate-300 mb-1">RADAR LEGEND</div>
                  <div className="flex items-center gap-2 text-red-300">
                    <span className="w-2.5 h-2.5 rounded-full bg-red-500/80" />
                    <span>1000m Collision Hazard Zone</span>
                  </div>
                  <div className="flex items-center gap-2 text-amber-300">
                    <span className="w-2.5 h-2.5 rounded-full bg-amber-500/60" />
                    <span>2500m Village Pre-Warning Buffer</span>
                  </div>
                  <div className="flex items-center gap-2 text-cyan-300">
                    <span className="w-2.5 h-0.5 bg-cyan-400" />
                    <span>NH-33 Highway Corridor</span>
                  </div>
                </div>
              </div>

            </div>

            {/* Live Logs Stream */}
            <div className="glass-panel rounded-3xl p-4 border border-slate-800/80 shadow-2xl">
              <div className="flex items-center justify-between pb-3 border-b border-slate-800/80">
                <div className="flex items-center gap-2 text-slate-200 font-tactical font-bold text-sm tracking-wider">
                  <Activity className="w-4 h-4 text-cyan-400" />
                  <span>LIVE INCIDENT & MITIGATION DISPATCH STREAM</span>
                </div>
                <button
                  onClick={() => setLogs([])}
                  className="text-xs font-mono text-slate-400 hover:text-red-400 transition flex items-center gap-1"
                >
                  <Trash2 className="w-3.5 h-3.5" /> CLEAR
                </button>
              </div>

              <div className="mt-3 space-y-2.5 max-h-56 overflow-y-auto pr-1">
                {logs.map((log) => (
                  <div
                    key={log.id}
                    className={`p-3 rounded-xl border flex items-start justify-between text-xs font-mono ${
                      log.type === 'CRITICAL SIGHTING'
                        ? 'bg-red-950/80 border-red-900/80 text-red-100'
                        : 'bg-slate-950/70 border-slate-800/90 text-slate-300'
                    }`}
                  >
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className={`px-1.5 py-0.5 rounded text-[10px] font-bold border ${
                          log.type === 'CRITICAL SIGHTING' ? 'bg-red-950 text-red-400 border-red-800 animate-pulse' : 'bg-emerald-950 text-emerald-400 border-emerald-800'
                        }`}>
                          {log.type}
                        </span>
                        <span className="font-semibold">{log.title}</span>
                      </div>
                      <p className="text-slate-400 text-[11px]">{log.desc}</p>
                      {log.highlight && (
                        <div className="text-[10px] text-amber-300">{log.highlight}</div>
                      )}
                    </div>
                    <span className="text-[10px] text-slate-500 shrink-0">{log.time}</span>
                  </div>
                ))}
              </div>
            </div>

          </div>

        </div>

      </main>

      {/* ========================================================================= */}
      {/* 4. MODALS (SMS & QRT) */}
      {/* ========================================================================= */}

      {/* SMS Modal */}
      {isSmsModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="glass-panel max-w-lg w-full rounded-3xl border border-cyan-500/40 p-6 shadow-2xl relative">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2.5">
                <div className="p-2 rounded-lg bg-amber-500/20 text-amber-400 border border-amber-500/40">
                  <Radio className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="font-tactical font-bold text-slate-100 text-sm">CELL BROADCAST SMS PAYLOAD</h3>
                  <p className="text-[11px] font-mono text-slate-400">Targeted geo-fenced cell towers (Jharkhand Telecom)</p>
                </div>
              </div>
              <button onClick={() => setIsSmsModalOpen(false)} className="p-1 rounded-lg bg-slate-900 text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="mt-4 p-4 rounded-2xl bg-slate-950 border border-slate-800 space-y-3 font-sans">
              <div className="p-3.5 rounded-xl bg-gradient-to-r from-red-950/60 to-amber-950/60 border border-red-700/60 text-slate-100 text-xs">
                <div className="font-bold text-amber-300 mb-1">
                  ⚠️ झारखंड वन विभाग आपातकालीन चेतावनी (DALMA NH-33)
                </div>
                <p className="font-medium text-slate-200 text-[13px] mb-2">
                  "सावधान! दलमा NH-33 के पास हाथियों का झुंड देखा गया है। वाहन की गति 20 किमी/घंटा रखें और जंगल की ओर न जाएं। (Jharkhand Forest Alert)"
                </p>
                <div className="text-[11px] text-slate-300 font-mono border-t border-red-900/60 pt-1.5">
                  <b>English:</b> Caution! Wild elephant herd detected near Dalma NH-33 pass. Restrict speed to 20 km/h and avoid entering sanctuary edge.
                </div>
              </div>

              <div className="grid grid-cols-2 gap-2 text-[11px] font-mono text-slate-400">
                <div><b>Recipients:</b> 1,240 Verified Nodes</div>
                <div><b>Latency:</b> 1.4s</div>
                <div><b>Towers:</b> 5 (Pardih, Dimna)</div>
                <div><b>Gateway:</b> CAP v1.2 Protocol</div>
              </div>
            </div>

            <div className="mt-5 flex gap-3">
              <button
                onClick={() => {
                  alert("✅ Cell broadcast re-dispatched to 1,240 subscribers!");
                  setIsSmsModalOpen(false);
                }}
                className="flex-1 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-tactical font-bold text-xs tracking-wider transition"
              >
                RE-TRIGGER BROADCAST
              </button>
              <button
                onClick={() => setIsSmsModalOpen(false)}
                className="px-5 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 font-mono text-xs border border-slate-700"
              >
                CLOSE
              </button>
            </div>
          </div>
        </div>
      )}

      {/* QRT Modal */}
      {isQrtModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="glass-panel max-w-lg w-full rounded-3xl border border-red-500/50 p-6 shadow-2xl relative">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <div className="flex items-center gap-2.5">
                <div className="p-2 rounded-lg bg-red-500/20 text-red-400 border border-red-500/40">
                  <Truck className="w-4 h-4" />
                </div>
                <div>
                  <h3 className="font-tactical font-bold text-slate-100 text-sm">FOREST QUICK RESPONSE SQUAD (QRT)</h3>
                  <p className="text-[11px] font-mono text-slate-400">Dalma Wildlife Sanctuary Patrol Units</p>
                </div>
              </div>
              <button onClick={() => setIsQrtModalOpen(false)} className="p-1 rounded-lg bg-slate-900 text-slate-400 hover:text-white">
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="mt-4 space-y-3 font-mono text-xs">
              <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 flex items-center justify-between">
                <div>
                  <div className="font-tactical font-bold text-cyan-300">SQUAD ALPHA (Jamshedpur North)</div>
                  <div className="text-[11px] text-slate-400">Gypsy 4x4 + Siren Beacon | ETA: 4 Mins</div>
                </div>
                <span className="px-2 py-1 rounded bg-emerald-950 text-emerald-300 border border-emerald-800 text-[10px] font-bold">
                  DISPATCHED
                </span>
              </div>

              <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 flex items-center justify-between">
                <div>
                  <div className="font-tactical font-bold text-amber-300">SQUAD BRAVO (Chandil Forest Post)</div>
                  <div className="text-[11px] text-slate-400">Dalma Interceptor 02 | ETA: 7 Mins</div>
                </div>
                <span className="px-2 py-1 rounded bg-amber-950 text-amber-300 border border-amber-800 text-[10px] font-bold">
                  EN ROUTE
                </span>
              </div>
            </div>

            <div className="mt-5 flex gap-3">
              <button
                onClick={() => {
                  alert("🚨 Quick Response Squad deployment confirmed!");
                  setIsQrtModalOpen(false);
                }}
                className="flex-1 py-2.5 rounded-xl bg-red-600 hover:bg-red-500 text-white font-tactical font-bold text-xs tracking-wider transition"
              >
                CONFIRM DEPLOYMENT
              </button>
              <button
                onClick={() => setIsQrtModalOpen(false)}
                className="px-5 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-slate-300 font-mono text-xs border border-slate-700"
              >
                CLOSE
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Footer */}
      <footer className="glass-panel border-t border-slate-800/80 px-4 lg:px-8 py-3 text-xs text-slate-400 font-mono backdrop-blur-xl">
        <div className="max-w-[1920px] mx-auto flex flex-col sm:flex-row items-center justify-between gap-2 text-center sm:text-left">
          <div className="flex items-center gap-2">
            <span className="w-2 h-2 rounded-full bg-cyan-400" />
            <span>SEEMS-AI v5.2 • Wildlife Intelligence Matrix (Jharkhand Corridor NH-33)</span>
          </div>
          <div className="text-slate-500 text-[11px]">
            Authenticated User: <b className="text-slate-300">{user?.name}</b> • Role: <b className="text-cyan-300">{user?.role}</b>
          </div>
        </div>
      </footer>

    </div>
  );
};
