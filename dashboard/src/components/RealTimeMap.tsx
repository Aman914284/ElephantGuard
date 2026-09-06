import React, { useEffect, useRef, useState } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { MapPin, Maximize2, Compass } from 'lucide-react';
import { IncidentReport, AlertRecord } from '../types';

interface RealTimeMapProps {
  incidents: IncidentReport[];
  alerts?: AlertRecord[];
  onSelectIncident: (incident: IncidentReport) => void;
  onRequestNewIncident?: (coords: { latitude: number; longitude: number }) => void;
}

type MapLayerType = 'satellite' | 'street' | 'topo';

export const RealTimeMap: React.FC<RealTimeMapProps> = ({
  incidents,
  onSelectIncident,
  onRequestNewIncident
}) => {
  const mapContainerRef = useRef<HTMLDivElement | null>(null);
  const mapInstanceRef = useRef<L.Map | null>(null);
  const tileLayerRef = useRef<L.TileLayer | null>(null);
  const markersLayerGroupRef = useRef<L.LayerGroup | null>(null);
  const geofenceLayerGroupRef = useRef<L.LayerGroup | null>(null);

  const [activeLayer, setActiveLayer] = useState<MapLayerType>('satellite');

  // Dalma Elephant Sanctuary Corridor coordinates
  const DALMA_CENTER: [number, number] = [22.8942, 86.2081];

  const TILE_URLS: Record<MapLayerType, { url: string; attribution: string; maxZoom: number }> = {
    satellite: {
      url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
      attribution: 'Esri World Imagery | Jharkhand Forest Dept Dalma Wildlife Division',
      maxZoom: 18,
    },
    street: {
      url: 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      attribution: '&copy; OpenStreetMap contributors | Jharkhand Wildlife Command',
      maxZoom: 19,
    },
    topo: {
      url: 'https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png',
      attribution: 'OpenTopoMap | SRTM Elevation Contours',
      maxZoom: 17,
    },
  };

  // 1. Initialize Map
  useEffect(() => {
    if (!mapContainerRef.current || mapInstanceRef.current) return;

    // Fix standard Leaflet default icon paths in Vite
    delete (L.Icon.Default.prototype as any)._getIconUrl;
    L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    });

    const map = L.map(mapContainerRef.current, {
      center: DALMA_CENTER,
      zoom: 12,
      zoomControl: false,
    });

    // Custom Zoom control in bottom right
    L.control.zoom({ position: 'bottomright' }).addTo(map);

    // Initial Tile Layer
    tileLayerRef.current = L.tileLayer(TILE_URLS.satellite.url, {
      attribution: TILE_URLS.satellite.attribution,
      maxZoom: TILE_URLS.satellite.maxZoom,
    }).addTo(map);

    markersLayerGroupRef.current = L.layerGroup().addTo(map);
    geofenceLayerGroupRef.current = L.layerGroup().addTo(map);

    mapInstanceRef.current = map;

    // Map click handler
    map.on('click', (e: L.LeafletMouseEvent) => {
      if (onRequestNewIncident) {
        const { lat, lng } = e.latlng;
        // If click-to-report mode or double click, trigger
        L.popup()
          .setLatLng(e.latlng)
          .setContent(`
            <div style="font-family: sans-serif; font-size: 12px; color: #0f172a; padding: 4px;">
              <strong style="color: #059669;">📍 Coordinates Selected</strong><br/>
              <span style="font-family: monospace; font-size: 11px;">${lat.toFixed(4)}° N, ${lng.toFixed(4)}° E</span><br/>
              <button id="btn-quick-log" style="
                margin-top: 6px;
                background: #059669;
                color: white;
                border: none;
                padding: 4px 8px;
                border-radius: 4px;
                cursor: pointer;
                font-weight: bold;
                font-size: 11px;
                width: 100%;
              ">
                + Report Sighting Here
              </button>
            </div>
          `)
          .openOn(map);

        setTimeout(() => {
          const btn = document.getElementById('btn-quick-log');
          if (btn) {
            btn.onclick = () => {
              map.closePopup();
              onRequestNewIncident({ latitude: lat, longitude: lng });
            };
          }
        }, 50);
      }
    });

    // Invalidate size after layout settles
    setTimeout(() => {
      map.invalidateSize();
    }, 250);

    return () => {
      map.remove();
      mapInstanceRef.current = null;
    };
  }, []);

  // 2. Change Tile Layer when activeLayer changes
  useEffect(() => {
    if (!mapInstanceRef.current || !tileLayerRef.current) return;
    const map = mapInstanceRef.current;
    map.removeLayer(tileLayerRef.current);

    const config = TILE_URLS[activeLayer];
    tileLayerRef.current = L.tileLayer(config.url, {
      attribution: config.attribution,
      maxZoom: config.maxZoom,
    }).addTo(map);
  }, [activeLayer]);

  // 3. Render Incidents, Geofences & Dynamic Hotspots
  useEffect(() => {
    if (!mapInstanceRef.current || !markersLayerGroupRef.current || !geofenceLayerGroupRef.current) return;

    markersLayerGroupRef.current.clearLayers();
    geofenceLayerGroupRef.current.clearLayers();

    // A. Dalma Core Sanctuary Polygon (Real GIS boundary)
    const sanctuaryCore = L.polygon([
      [22.9550, 86.1550],
      [22.9650, 22.9650 > 80 ? 86.2350 : 86.2350],
      [22.9400, 86.2650],
      [22.8850, 86.2680],
      [22.8550, 86.2100],
      [22.8750, 86.1450],
      [22.9250, 86.1350]
    ], {
      color: '#10b981',
      fillColor: '#10b981',
      fillOpacity: activeLayer === 'satellite' ? 0.12 : 0.08,
      weight: 2,
    });
    sanctuaryCore.bindTooltip('Dalma Wildlife Sanctuary Core Forest Reserve', { sticky: true, className: 'dalma-tooltip' });
    geofenceLayerGroupRef.current.addLayer(sanctuaryCore);

    // B. NH-33 Elephant Crossing Corridor Hazard Ribbon
    const highwayCrossing = L.polyline([
      [22.8800, 86.1750],
      [22.8942, 86.2081],
      [22.9050, 86.2350],
      [22.9200, 86.2700]
    ], {
      color: '#f43f5e',
      weight: 4,
      dashArray: '8, 8',
      opacity: 0.85
    });
    highwayCrossing.bindTooltip('⚠️ NH-33 Elephant Migration Corridor (Asanbani - Pardih Pass)', { sticky: true });
    geofenceLayerGroupRef.current.addLayer(highwayCrossing);

    // C. Plot Real Incidents from Backend
    const markers: L.Marker[] = [];

    incidents.forEach((inc) => {
      if (!inc.latitude || !inc.longitude) return;

      const getThreatColor = (level: string) => {
        switch (level) {
          case 'CRITICAL': return '#f43f5e'; // Rose Red
          case 'HIGH': return '#f97316';     // Amber Orange
          case 'CAUTION': return '#eab308';  // Golden Yellow
          default: return '#10b981';         // Emerald Green
        }
      };

      const color = getThreatColor(inc.threatLevel);
      const isCritical = inc.threatLevel === 'CRITICAL';
      const isHigh = inc.threatLevel === 'HIGH';

      // Sighting Marker Icon with Pulsing Radar Beacon
      const markerIcon = L.divIcon({
        className: 'custom-live-incident-pin',
        html: `
          <div style="position: relative; width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;">
            ${(isCritical || isHigh) ? `
              <div style="
                position: absolute;
                inset: 0;
                border-radius: 50%;
                background: ${color};
                opacity: 0.4;
                animation: map-pulse 2s infinite ease-out;
              "></div>
            ` : ''}
            <div style="
              position: relative;
              background: ${color};
              color: #ffffff;
              border: 2.5px solid #ffffff;
              border-radius: 50%;
              width: 32px;
              height: 32px;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 15px;
              box-shadow: 0 0 16px ${color}99, 0 4px 6px rgba(0,0,0,0.4);
              cursor: pointer;
              transition: transform 0.2s ease;
            ">
              🐘
            </div>
            <div style="
              position: absolute;
              bottom: -4px;
              right: -2px;
              background: #0f172a;
              color: #ffffff;
              font-family: monospace;
              font-size: 9px;
              font-weight: 800;
              padding: 1px 4px;
              border-radius: 6px;
              border: 1px solid ${color};
            ">
              ${inc.elephantCount}
            </div>
          </div>
        `,
        iconSize: [36, 36],
        iconAnchor: [18, 18],
      });

      const marker = L.marker([inc.latitude, inc.longitude], { icon: markerIcon });
      markers.push(marker);

      // Add Hazard Detection Buffer Circle
      if (isCritical || isHigh) {
        const hazardCircle = L.circle([inc.latitude, inc.longitude], {
          radius: isCritical ? 1500 : 900,
          color: color,
          fillColor: color,
          fillOpacity: 0.12,
          weight: 1.5,
          dashArray: '4, 4'
        });
        geofenceLayerGroupRef.current?.addLayer(hazardCircle);
      }

      // Popup Content
      const popupDiv = document.createElement('div');
      popupDiv.style.fontFamily = 'sans-serif';
      popupDiv.style.fontSize = '12px';
      popupDiv.style.color = '#0f172a';
      popupDiv.style.minWidth = '230px';

      popupDiv.innerHTML = `
        <div style="border-bottom: 1px solid #e2e8f0; padding-bottom: 5px; margin-bottom: 6px;">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <strong style="color: #0f172a; font-size: 13px;">${inc.id}</strong>
            <span style="
              background: ${color};
              color: white;
              padding: 2px 7px;
              border-radius: 5px;
              font-weight: bold;
              font-size: 10px;
              font-family: monospace;
            ">${inc.threatLevel}</span>
          </div>
          <div style="color: #64748b; font-size: 11px; margin-top: 2px;">
            ${inc.timestamp}
          </div>
        </div>
        
        <div style="line-height: 1.55; margin-bottom: 8px; font-size: 11px; color: #334155;">
          <div><strong>🐘 Herd Count:</strong> <span style="font-weight: bold; color: #0f172a;">${inc.elephantCount} Elephants</span></div>
          <div><strong>🤖 AI Confidence:</strong> ${(inc.confidence * 100).toFixed(1)}%</div>
          <div><strong>🚨 Risk Score:</strong> <span style="font-weight: bold; color: ${color};">${inc.riskScore} / 100</span></div>
          <div><strong>📍 GPS:</strong> ${inc.latitude.toFixed(4)}° N, ${inc.longitude.toFixed(4)}° E</div>
          <div><strong>👤 Reporter:</strong> ${inc.reporterName || 'Wildlife Guard'}</div>
          <div><strong>⚡ Status:</strong> ${inc.verificationStatus} • ${inc.responseStatus}</div>
        </div>

        ${inc.notes ? `
          <div style="
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 6px;
            padding: 5px 8px;
            margin-bottom: 8px;
            font-size: 10px;
            color: #475569;
            font-style: italic;
          ">
            "${inc.notes}"
          </div>
        ` : ''}

        <button id="btn-inspect-${inc.id}" style="
          width: 100%;
          background: #0f172a;
          color: #ffffff;
          border: none;
          padding: 6px 12px;
          border-radius: 6px;
          cursor: pointer;
          font-weight: 600;
          font-size: 11px;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 4px;
        ">
          Inspect Sighting Details →
        </button>
      `;

      marker.bindPopup(popupDiv);

      marker.on('popupopen', () => {
        const btn = document.getElementById(`btn-inspect-${inc.id}`);
        if (btn) {
          btn.onclick = () => {
            onSelectIncident(inc);
          };
        }
      });

      markersLayerGroupRef.current?.addLayer(marker);
    });

  }, [incidents, activeLayer, onSelectIncident]);

  // Center Dalma Core
  const handleCenterDalma = () => {
    if (mapInstanceRef.current) {
      mapInstanceRef.current.flyTo(DALMA_CENTER, 12, { duration: 1.2 });
    }
  };

  // Fit All Incidents Bounds
  const handleFitAll = () => {
    if (!mapInstanceRef.current || incidents.length === 0) return;
    const validCoords = incidents
      .filter((i) => i.latitude && i.longitude)
      .map((i) => [i.latitude, i.longitude] as [number, number]);

    if (validCoords.length > 0) {
      const bounds = L.latLngBounds(validCoords);
      mapInstanceRef.current.fitBounds(bounds, { padding: [50, 50], maxZoom: 15 });
    }
  };

  return (
    <div className="relative w-full h-[600px] lg:h-[680px] rounded-3xl overflow-hidden border border-slate-800 shadow-2xl bg-slate-950">
      
      {/* Real-time Leaflet Container */}
      <div ref={mapContainerRef} className="w-full h-full z-0" />

      {/* Top Left: GIS Layer Switcher & Tool Controls */}
      <div className="absolute top-4 left-4 z-10 flex flex-wrap items-center gap-2 pointer-events-auto">
        
        {/* Layer Switcher */}
        <div className="bg-slate-900/90 backdrop-blur-md p-1 rounded-xl border border-slate-700/80 shadow-lg flex items-center gap-1 text-xs">
          <button
            onClick={() => setActiveLayer('satellite')}
            className={`px-3 py-1.5 rounded-lg font-mono font-medium transition-all flex items-center gap-1.5 ${
              activeLayer === 'satellite'
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <span>🛰️ Satellite</span>
          </button>

          <button
            onClick={() => setActiveLayer('street')}
            className={`px-3 py-1.5 rounded-lg font-mono font-medium transition-all flex items-center gap-1.5 ${
              activeLayer === 'street'
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <span>🗺️ GIS Streets</span>
          </button>

          <button
            onClick={() => setActiveLayer('topo')}
            className={`px-3 py-1.5 rounded-lg font-mono font-medium transition-all flex items-center gap-1.5 ${
              activeLayer === 'topo'
                ? 'bg-emerald-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'
            }`}
          >
            <span>⛰️ Topo</span>
          </button>
        </div>

        {/* Quick Navigation Action Buttons */}
        <div className="bg-slate-900/90 backdrop-blur-md p-1 rounded-xl border border-slate-700/80 shadow-lg flex items-center gap-1 text-xs">
          <button
            onClick={handleCenterDalma}
            title="Center Dalma Sanctuary Pass"
            className="p-1.5 rounded-lg text-slate-300 hover:text-white hover:bg-slate-800 transition-colors flex items-center gap-1 px-2.5 font-mono"
          >
            <Compass className="w-3.5 h-3.5 text-emerald-400" />
            <span className="hidden sm:inline">Center Dalma</span>
          </button>

          <button
            onClick={handleFitAll}
            title="Fit All Live Incidents"
            className="p-1.5 rounded-lg text-slate-300 hover:text-white hover:bg-slate-800 transition-colors flex items-center gap-1 px-2.5 font-mono"
          >
            <Maximize2 className="w-3.5 h-3.5 text-cyan-400" />
            <span className="hidden sm:inline">Fit All ({incidents.length})</span>
          </button>
        </div>

      </div>

      {/* Top Right: Corridor Map Legend */}
      <div className="absolute top-4 right-4 z-10 bg-slate-900/95 backdrop-blur-md p-3.5 rounded-2xl border border-slate-700/80 shadow-xl text-xs space-y-2 text-slate-200 pointer-events-auto max-w-[240px]">
        <div className="flex items-center justify-between border-b border-slate-800 pb-1.5">
          <div className="font-bold text-[11px] font-mono uppercase text-slate-300 flex items-center gap-1.5">
            <MapPin className="w-3.5 h-3.5 text-emerald-400" />
            <span>Dalma GIS Grid</span>
          </div>
          <span className="text-[10px] text-emerald-400 font-mono font-bold">LIVE GPS</span>
        </div>

        <div className="space-y-1.5 text-[11px]">
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-rose-500 shadow-sm shrink-0 animate-pulse" />
            <span>Critical Threat (NH-33 Crossing)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-amber-500 shadow-sm shrink-0" />
            <span>High Threat (Village Boundary)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-yellow-500 shadow-sm shrink-0" />
            <span>Caution (Corridor Perimeter)</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-emerald-500 shadow-sm shrink-0" />
            <span>Safe / Sanctuary Reserve</span>
          </div>
          <div className="flex items-center gap-2 pt-1 border-t border-slate-800/80">
            <span className="w-3.5 h-1 rounded bg-rose-500/80 shrink-0" />
            <span className="text-[10px] text-rose-300">NH-33 Elephant Pass</span>
          </div>
        </div>

        <div className="pt-1.5 border-t border-slate-800 text-[10px] text-slate-400 font-mono">
          💡 Click anywhere on map to log GPS sighting
        </div>
      </div>

      {/* Bottom Left: Live Telemetry Status Banner */}
      <div className="absolute bottom-4 left-4 z-10 bg-slate-900/90 backdrop-blur-md px-4 py-2.5 rounded-2xl border border-slate-700/80 shadow-lg text-xs flex flex-wrap items-center gap-4 text-slate-300 font-mono pointer-events-auto">
        <div className="flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
          <span>Active Telemetry Pins:</span>
          <strong className="text-emerald-400 font-bold">{incidents.length}</strong>
        </div>
        <div className="h-3 w-px bg-slate-700 hidden sm:block" />
        <div className="hidden sm:flex items-center gap-1.5">
          <span>Corridor:</span>
          <strong className="text-cyan-400">Dalma Range (NH-33)</strong>
        </div>
        <div className="h-3 w-px bg-slate-700 hidden md:block" />
        <div className="hidden md:flex items-center gap-1.5 text-slate-400 text-[11px]">
          <span>Layer:</span>
          <span className="uppercase text-slate-200">{activeLayer}</span>
        </div>
      </div>

      <style>{`
        @keyframes map-pulse {
          0% {
            transform: scale(0.95);
            opacity: 0.8;
          }
          70% {
            transform: scale(2.2);
            opacity: 0;
          }
          100% {
            transform: scale(2.4);
            opacity: 0;
          }
        }
        .custom-live-incident-pin:hover {
          transform: scale(1.15);
          z-index: 9999 !important;
        }
        .dalma-tooltip {
          background: #0f172a;
          color: #10b981;
          border: 1px solid #10b981;
          font-family: monospace;
          font-size: 11px;
          border-radius: 6px;
          padding: 3px 8px;
        }
      `}</style>

    </div>
  );
};
