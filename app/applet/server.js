const http = require("http");
const fs = require("fs");
const path = require("path");
const url = require("url");

const PORT = parseInt(process.env.DEFAULT_APP_PORT || "3000", 10);
const APP_DIR = process.env.APP_DIR || "/app/applet";

const APK_PATHS = [
  path.join(APP_DIR, ".build-outputs", "app-debug.apk"),
  path.join(APP_DIR, "app", "build", "outputs", "apk", "debug", "app-debug.apk"),
];

function getApkInfo() {
  for (const apkPath of APK_PATHS) {
    if (fs.existsSync(apkPath)) {
      const stat = fs.statSync(apkPath);
      return {
        exists: true,
        path: apkPath,
        sizeBytes: stat.size,
        sizeMb: (stat.size / (1024 * 1024)).toFixed(1),
        modified: stat.mtime.toISOString(),
      };
    }
  }
  return { exists: false, path: null, sizeBytes: 0, sizeMb: "0", modified: null };
}

const htmlContent = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>RakshaVaani | Android App Preview (Jetpack Compose)</title>
  <style>
    :root {
      --navy-dark: #070D16;
      --navy-bg: #0B131F;
      --navy-card: #131F30;
      --navy-card-light: #1B2B42;
      --navy-border: #223652;
      --navy-border-light: #324D73;
      --tactical-olive: #556B2F;
      --tactical-olive-light: #6E8B3D;
      --tactical-green: #22C55E;
      --tactical-green-dark: #15803D;
      --tactical-amber: #F4B400;
      --tactical-red: #EF4444;
      --tactical-cyan: #38BDF8;
      --text-primary: #F3F4F6;
      --text-secondary: #94A3B8;
      --text-muted: #64748B;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; user-select: none; }
    body {
      background-color: #050911;
      color: var(--text-primary);
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: flex-start;
      padding: 10px;
      overflow-x: hidden;
    }

    /* Top Platform Toolbar */
    .top-toolbar {
      width: 100%;
      max-width: 440px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 6px 12px;
      background: rgba(19, 31, 48, 0.95);
      border: 1px solid var(--navy-border);
      border-radius: 10px;
      margin-bottom: 10px;
      font-size: 11px;
      gap: 8px;
    }
    .platform-status {
      display: flex;
      align-items: center;
      gap: 6px;
      color: var(--tactical-green);
      font-weight: 600;
    }
    .status-pulse {
      width: 8px;
      height: 8px;
      background: var(--tactical-green);
      border-radius: 50%;
      box-shadow: 0 0 8px var(--tactical-green);
      animation: pulse-dot 1.5s infinite;
    }
    @keyframes pulse-dot {
      0% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.4; transform: scale(0.85); }
      100% { opacity: 1; transform: scale(1); }
    }
    .btn-download-quick {
      background: var(--tactical-olive);
      color: #FFF;
      text-decoration: none;
      padding: 4px 10px;
      border-radius: 6px;
      font-weight: 700;
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 11px;
      transition: background 0.2s;
    }
    .btn-download-quick:hover { background: var(--tactical-olive-light); }

    /* Android Phone Frame */
    .phone-wrapper {
      width: 100%;
      max-width: 412px;
      height: 840px;
      max-height: 94vh;
      background: #000;
      border-radius: 44px;
      border: 5px solid #202938;
      box-shadow: 0 25px 60px rgba(0,0,0,0.9), 0 0 0 1px #334155;
      display: flex;
      flex-direction: column;
      position: relative;
      overflow: hidden;
    }

    /* Android Camera Notch & Speaker */
    .phone-notch {
      position: absolute;
      top: 10px;
      left: 50%;
      transform: translateX(-50%);
      width: 12px;
      height: 12px;
      background: #111;
      border: 2px solid #222;
      border-radius: 50%;
      z-index: 999;
      pointer-events: none;
    }
    .phone-speaker {
      position: absolute;
      top: 5px;
      left: 50%;
      transform: translateX(-50%);
      width: 50px;
      height: 3px;
      background: #252b36;
      border-radius: 2px;
      z-index: 999;
      pointer-events: none;
    }

    /* Android Status Bar */
    .android-status-bar {
      height: 36px;
      padding: 6px 20px 0 20px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: var(--navy-bg);
      font-size: 12px;
      font-weight: 600;
      color: var(--text-secondary);
      z-index: 100;
    }
    .status-icons {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 11px;
    }

    /* Screen viewport inside Android */
    .app-screen-viewport {
      flex: 1;
      background: var(--navy-bg);
      display: flex;
      flex-direction: column;
      overflow-y: auto;
      overflow-x: hidden;
      position: relative;
    }

    /* Android Bottom Navigation Bar (Gesture bar / 3-button) */
    .android-system-nav {
      height: 38px;
      background: var(--navy-dark);
      display: flex;
      justify-content: space-around;
      align-items: center;
      padding: 0 40px;
      z-index: 100;
      border-top: 1px solid rgba(255,255,255,0.05);
    }
    .nav-btn-sys {
      background: none;
      border: none;
      color: var(--text-muted);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 8px;
      transition: color 0.15s;
    }
    .nav-btn-sys:hover { color: var(--text-primary); }

    /* Screen Layout Standard */
    .screen {
      display: none;
      flex-direction: column;
      min-height: 100%;
      position: relative;
    }
    .screen.active {
      display: flex;
      animation: fadeIn 0.2s ease-out;
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(4px); }
      to { opacity: 1; transform: translateY(0); }
    }

    /* Tactical Top App Bar */
    .tactical-top-bar {
      background: var(--navy-card);
      border-bottom: 1px solid var(--navy-border);
      padding: 10px 14px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      position: sticky;
      top: 0;
      z-index: 50;
    }
    .top-bar-left {
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .top-bar-title-block {
      display: flex;
      flex-direction: column;
    }
    .top-bar-title {
      font-size: 14px;
      font-weight: 900;
      letter-spacing: 0.8px;
      color: var(--text-primary);
    }
    .top-bar-sub {
      font-size: 9px;
      font-weight: 700;
      letter-spacing: 0.6px;
      color: var(--tactical-amber);
    }
    .icon-btn {
      background: none;
      border: none;
      color: var(--text-secondary);
      cursor: pointer;
      padding: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
    }
    .icon-btn:hover { background: rgba(255,255,255,0.08); color: var(--text-primary); }

    /* Tactical Bottom App Bar */
    .tactical-bottom-nav {
      background: var(--navy-card);
      border-top: 1px solid var(--navy-border);
      display: flex;
      justify-content: space-around;
      align-items: center;
      padding: 8px 12px;
      position: sticky;
      bottom: 0;
      z-index: 50;
    }
    .nav-item {
      background: none;
      border: none;
      color: var(--text-muted);
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 3px;
      font-size: 10px;
      font-weight: 700;
      cursor: pointer;
      padding: 4px 16px;
      border-radius: 8px;
      transition: all 0.15s;
    }
    .nav-item.active {
      color: var(--tactical-cyan);
      background: rgba(56, 189, 248, 0.1);
    }

    /* Screen Content Padding */
    .screen-content {
      padding: 14px;
      display: flex;
      flex-direction: column;
      gap: 14px;
      flex: 1;
    }

    /* Tactical Cards */
    .tactical-card {
      background: var(--navy-card);
      border: 1px solid var(--navy-border);
      border-radius: 14px;
      padding: 14px;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .tactical-card-title {
      font-size: 11px;
      font-weight: 800;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.6px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    /* Banner Image */
    .banner-img-box {
      width: 100%;
      height: 110px;
      border-radius: 12px;
      overflow: hidden;
      position: relative;
      border: 1px solid var(--navy-border);
      background: #0D1624;
    }
    .banner-img-box img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .banner-overlay {
      position: absolute;
      inset: 0;
      background: linear-gradient(180deg, rgba(7,13,22,0.2) 0%, rgba(7,13,22,0.85) 100%);
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
      padding: 10px;
    }

    /* Waveform Box */
    .waveform-box {
      background: #060B12;
      border: 1px solid var(--navy-border);
      border-radius: 10px;
      height: 80px;
      position: relative;
      overflow: hidden;
      display: flex;
      align-items: center;
    }
    .waveform-box canvas {
      width: 100%;
      height: 100%;
    }
    .waveform-badge {
      position: absolute;
      top: 6px;
      left: 8px;
      font-size: 9px;
      font-family: monospace;
      color: var(--tactical-cyan);
      font-weight: 700;
      background: rgba(0,0,0,0.5);
      padding: 2px 6px;
      border-radius: 4px;
    }

    /* Buttons */
    .btn-tactical {
      background: var(--navy-card-light);
      border: 1px solid var(--navy-border);
      color: var(--text-primary);
      font-size: 12px;
      font-weight: 700;
      padding: 10px 14px;
      border-radius: 8px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      transition: all 0.15s;
    }
    .btn-tactical:hover { background: var(--navy-border); }
    .btn-primary-action {
      background: var(--tactical-olive);
      border: 1px solid rgba(85,107,47,0.8);
      color: #FFF;
      font-size: 13px;
      font-weight: 800;
      padding: 12px;
      border-radius: 10px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      box-shadow: 0 4px 12px rgba(85,107,47,0.3);
      transition: all 0.2s;
    }
    .btn-primary-action:hover { background: var(--tactical-olive-light); transform: translateY(-1px); }
    .btn-rec-active {
      background: rgba(239, 68, 68, 0.25) !important;
      border-color: var(--tactical-red) !important;
      color: var(--tactical-red) !important;
      animation: pulse-red 1.5s infinite;
    }

    /* Grid of Chips */
    .chips-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 8px;
    }
    .chip-item {
      background: var(--navy-card-light);
      border: 1px solid var(--navy-border);
      border-radius: 8px;
      padding: 10px;
      font-size: 11px;
      font-weight: 600;
      color: var(--text-secondary);
      cursor: pointer;
      text-align: center;
      transition: all 0.15s;
    }
    .chip-item.selected {
      background: rgba(85,107,47,0.35);
      border-color: var(--tactical-olive);
      color: var(--text-primary);
      font-weight: 700;
    }

    /* Telemetry Pill */
    .telemetry-row {
      display: flex;
      gap: 8px;
    }
    .telemetry-pill {
      flex: 1;
      background: var(--navy-card-light);
      border: 1px solid var(--navy-border);
      border-radius: 8px;
      padding: 8px;
      text-align: center;
    }
    .pill-value {
      font-size: 14px;
      font-weight: 800;
      color: var(--tactical-green);
      font-family: monospace;
    }
    .pill-label {
      font-size: 9px;
      color: var(--text-muted);
      margin-top: 2px;
      text-transform: uppercase;
    }

    /* Radar Sonar Animation */
    .radar-box {
      width: 160px;
      height: 160px;
      border-radius: 50%;
      border: 2px solid var(--navy-border-light);
      margin: 20px auto;
      position: relative;
      overflow: hidden;
      background: radial-gradient(circle, #0c1829 0%, #060b13 100%);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .radar-ring {
      position: absolute;
      border: 1px solid rgba(56, 189, 248, 0.2);
      border-radius: 50%;
    }
    .ring-1 { width: 40px; height: 40px; }
    .ring-2 { width: 80px; height: 80px; }
    .ring-3 { width: 120px; height: 120px; }
    .radar-sweep {
      position: absolute;
      width: 50%;
      height: 50%;
      top: 0;
      right: 0;
      transform-origin: bottom left;
      background: linear-gradient(45deg, rgba(34, 197, 94, 0.4) 0%, transparent 80%);
      animation: sweep 2s linear infinite;
    }
    @keyframes sweep {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  </style>
</head>
<body>

  <!-- Top Platform Header -->
  <div class="top-toolbar">
    <div class="platform-status">
      <div class="status-pulse"></div>
      <span>RakshaVaani Mobile Preview (Active)</span>
    </div>
    <div style="display: flex; gap: 6px;">
      <a href="/app-debug.apk" class="btn-download-quick" download="RakshaVaani-debug.apk">
        <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/></svg>
        <span>Download APK (24 MB)</span>
      </a>
    </div>
  </div>

  <!-- Android Device Frame -->
  <div class="phone-wrapper">
    <div class="phone-speaker"></div>
    <div class="phone-notch"></div>

    <!-- Android Status Bar -->
    <div class="android-status-bar">
      <span id="android-clock">14:30</span>
      <div class="status-icons">
        <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M12 3c-4.97 0-9 4.03-9 9 0 2.12.74 4.07 1.97 5.61L4.35 18.23C5.7 19.33 7.39 20 9.24 20h5.52c1.85 0 3.54-.67 4.89-1.77l-.62-.62C20.26 16.07 21 14.12 21 12c0-4.97-4.03-9-9-9z"/></svg>
        <span>5G</span>
        <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M15.67 4H14V2h-4v2H8.33C7.6 4 7 4.6 7 5.33v15.33C7 21.4 7.6 22 8.33 22h7.33c.74 0 1.34-.6 1.34-1.33V5.33C17 4.6 16.4 4 15.67 4z"/></svg>
      </div>
    </div>

    <!-- Screen Viewport inside Phone -->
    <div class="app-screen-viewport" id="viewport">

      <!-- SCREEN 1: SPLASH SCREEN -->
      <div class="screen active" id="screen-splash">
        <div class="banner-img-box" style="height: 180px; border-radius: 0;">
          <img src="/images/tactical_banner.jpg" alt="DRDO Tactical Banner" onerror="this.style.display='none'">
          <div class="banner-overlay">
            <span style="font-size: 10px; color: var(--tactical-amber); font-weight: 800;">DRDO | DEFENCE RESEARCH ESTABLISHMENT</span>
            <span style="font-size: 13px; font-weight: 900; color: #FFF;">MINISTRY OF DEFENCE</span>
          </div>
        </div>

        <div class="screen-content" style="align-items: center; text-align: center; justify-content: center; gap: 16px;">
          <div style="width: 100px; height: 100px; border-radius: 20px; overflow: hidden; border: 2px solid var(--navy-border-light); box-shadow: 0 8px 24px rgba(0,0,0,0.6);">
            <img src="/images/rakshavaani_logo.jpg" alt="RakshaVaani Logo" style="width: 100%; height: 100%; object-fit: cover;">
          </div>

          <div>
            <h1 style="font-size: 24px; font-weight: 900; letter-spacing: 1.5px; color: #FFF;">RAKSHA VAANI</h1>
            <p style="font-size: 11px; font-weight: 700; color: var(--tactical-amber); margin-top: 4px; letter-spacing: 1px;">
              TACTICAL SPEECH ENHANCEMENT
            </p>
            <p style="font-size: 10px; color: var(--text-muted); margin-top: 4px;">
              MIL-STD-810H Acoustic Neural Filter
            </p>
          </div>

          <div style="background: var(--navy-card); border: 1px solid var(--navy-border); border-radius: 10px; padding: 10px 16px; width: 100%; max-width: 280px;">
            <div style="display: flex; justify-content: space-between; font-size: 10px; font-family: monospace;">
              <span style="color: var(--text-muted);">SECURITY CLEARANCE:</span>
              <span style="color: var(--tactical-green); font-weight: bold;">LEVEL-4 RESTRICTED</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 10px; font-family: monospace; margin-top: 4px;">
              <span style="color: var(--text-muted);">NEURAL CORE:</span>
              <span style="color: var(--tactical-cyan);">GTCRN-v2 (FP16)</span>
            </div>
          </div>

          <button class="btn-primary-action" style="width: 100%; max-width: 280px; margin-top: 8px;" onclick="navigateTo('screen-home')">
            <span>INITIALIZE CONSOLE</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z"/></svg>
          </button>
        </div>
      </div>

      <!-- SCREEN 2: HOME SCREEN (FIELD AUDIO CONSOLE) -->
      <div class="screen" id="screen-home">
        <div class="tactical-top-bar">
          <div class="top-bar-left">
            <div style="width: 24px; height: 24px; border-radius: 6px; overflow: hidden;">
              <img src="/images/rakshavaani_logo.jpg" style="width: 100%; height: 100%; object-fit: cover;">
            </div>
            <div class="top-bar-title-block">
              <span class="top-bar-title">RAKSHA VAANI</span>
              <span class="top-bar-sub">FIELD ACOUSTIC CONSOLE</span>
            </div>
          </div>
          <div style="display: flex; align-items: center; gap: 4px;">
            <span style="font-size: 9px; font-family: monospace; color: var(--tactical-green); background: rgba(34,197,94,0.15); padding: 2px 6px; border-radius: 4px; border: 1px solid rgba(34,197,94,0.3);">DRDO ONLINE</span>
            <button class="icon-btn" onclick="navigateTo('screen-settings')">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
            </button>
          </div>
        </div>

        <div class="screen-content">
          <!-- Tactical Hero Card -->
          <div class="banner-img-box">
            <img src="/images/tactical_banner.jpg" alt="Tactical Banner" onerror="this.style.display='none'">
            <div class="banner-overlay">
              <span style="font-size: 9px; color: var(--tactical-amber); font-weight: 800;">DRDO ACOUSTIC SHIELD</span>
              <span style="font-size: 13px; font-weight: 900; color: #FFF;">Battlefield Speech Enhancement</span>
              <span style="font-size: 9px; color: var(--text-secondary);">Real-Time Low-SNR Transmission Filter</span>
            </div>
          </div>

          <!-- Waveform & Mic Capture -->
          <div class="tactical-card">
            <div class="tactical-card-title">
              <span>FIELD ACOUSTIC INPUT</span>
              <span style="color: var(--tactical-cyan); font-family: monospace;" id="home-signal-tag">STANDBY | 16 kHz</span>
            </div>

            <div class="waveform-box">
              <canvas id="homeWaveform"></canvas>
              <div class="waveform-badge" id="home-rec-badge">INPUT: IDLE</div>
            </div>

            <div style="display: flex; gap: 8px;">
              <button class="btn-tactical" id="btn-home-mic" onclick="toggleHomeMic()" style="flex: 1.2;">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 14c1.66 0 2.99-1.34 2.99-3L15 5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z"/></svg>
                <span id="home-mic-text">Record Mic</span>
              </button>
              <button class="btn-tactical" onclick="loadTacticalPreset('drone')" style="flex: 1;">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M22 16v-2l-8.5-5V3.5c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5V9L2 14v2l8.5-2.5V19L8 20.5V22l4-1 4 1v-1.5L13.5 19v-5.5L22 16z"/></svg>
                <span>UAV Drone</span>
              </button>
              <button class="btn-tactical" onclick="loadTacticalPreset('convoy')" style="flex: 1;">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M18.92 6.01C18.72 5.42 18.16 5 17.5 5h-11c-.66 0-1.21.42-1.42 1.01L3 12v8c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h12v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-8l-2.08-5.99zM6.85 7h10.29l1.04 3H5.81l1.04-3zM19 17H5v-4.66l.12-.34h13.77l.11.34V17z"/></svg>
                <span>Convoy</span>
              </button>
            </div>
          </div>

          <!-- Noise Profile Matrix -->
          <div class="tactical-card">
            <div class="tactical-card-title">
              <span>TARGET NOISE PROFILE</span>
              <span style="color: var(--tactical-amber); font-family: monospace;" id="selected-profile-tag">UAV QUADCOPTER</span>
            </div>

            <div class="chips-grid">
              <div class="chip-item selected" onclick="setProfile(this, 'UAV Quadcopter', -15)">UAV / Drone</div>
              <div class="chip-item" onclick="setProfile(this, 'T-90 Tank Convoy', -10)">T-90 Convoy</div>
              <div class="chip-item" onclick="setProfile(this, 'High Altitude Wind', -8)">High Altitude Wind</div>
              <div class="chip-item" onclick="setProfile(this, 'Artillery / Gunfire', -18)">Artillery Gunfire</div>
            </div>
          </div>

          <button class="btn-primary-action" onclick="navigateTo('screen-noise')">
            <span>PROCEED TO NOISE CONFIGURATION</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z"/></svg>
          </button>
        </div>

        <!-- Tactical Bottom Bar -->
        <div class="tactical-bottom-nav">
          <button class="nav-item active" onclick="navigateTo('screen-home')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 14c1.66 0 2.99-1.34 2.99-3L15 5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z"/></svg>
            <span>CONSOLE</span>
          </button>
          <button class="nav-item" onclick="navigateTo('screen-history')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/></svg>
            <span>HISTORY</span>
          </button>
          <button class="nav-item" onclick="navigateTo('screen-settings')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
            <span>SETTINGS</span>
          </button>
        </div>
      </div>

      <!-- SCREEN 3: NOISE SETTINGS SCREEN -->
      <div class="screen" id="screen-noise">
        <div class="tactical-top-bar">
          <div class="top-bar-left">
            <button class="icon-btn" onclick="navigateTo('screen-home')">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>
            </button>
            <div class="top-bar-title-block">
              <span class="top-bar-title">NOISE MATRIX</span>
              <span class="top-bar-sub">GTCRN PARAMETERS</span>
            </div>
          </div>
        </div>

        <div class="screen-content">
          <div class="tactical-card">
            <div class="tactical-card-title">
              <span>SUPPRESSION DEPTH</span>
              <span style="color: var(--tactical-green);" id="noise-depth-val">85% (Aggressive)</span>
            </div>
            <input type="range" min="30" max="100" value="85" style="width: 100%; accent-color: var(--tactical-olive);" oninput="document.getElementById('noise-depth-val').textContent = this.value + '% (Aggressive)'">
            <div style="display: flex; justify-content: space-between; font-size: 9px; color: var(--text-muted);">
              <span>MILD (30%)</span>
              <span>BALANCED (60%)</span>
              <span>TACTICAL MAX (100%)</span>
            </div>
          </div>

          <div class="tactical-card">
            <div class="tactical-card-title">
              <span>NEURAL ARCHITECTURE</span>
            </div>
            <div class="chips-grid">
              <div class="chip-item selected">GTCRN Dual-Path</div>
              <div class="chip-item">Conformer-Lite</div>
              <div class="chip-item">Sub-Band Conv</div>
              <div class="chip-item">Hybrid STFT+TCN</div>
            </div>
          </div>

          <div class="tactical-card">
            <div class="tactical-card-title">
              <span>DEFENCE BANDWIDTH SHIELD</span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; font-size: 11px;">
              <span>Real-Time Spectrum Masking</span>
              <span style="color: var(--tactical-green); font-weight: bold;">ENABLED</span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; font-size: 11px; margin-top: 6px;">
              <span>Edge GPU Acceleration</span>
              <span style="color: var(--tactical-cyan); font-weight: bold;">FP16 TENSORRT</span>
            </div>
          </div>

          <button class="btn-primary-action" onclick="navigateTo('screen-preview')">
            <span>PREVIEW ACOUSTIC SAMPLE</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z"/></svg>
          </button>
        </div>
      </div>

      <!-- SCREEN 4: PREVIEW SCREEN -->
      <div class="screen" id="screen-preview">
        <div class="tactical-top-bar">
          <div class="top-bar-left">
            <button class="icon-btn" onclick="navigateTo('screen-noise')">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>
            </button>
            <div class="top-bar-title-block">
              <span class="top-bar-title">SAMPLE PREVIEW</span>
              <span class="top-bar-sub">READY FOR INFERENCE</span>
            </div>
          </div>
        </div>

        <div class="screen-content">
          <div class="tactical-card">
            <div class="tactical-card-title">
              <span>RAW CAPTURE WAVEFORM</span>
              <span style="color: var(--tactical-amber);">EST. SNR: -14 dB</span>
            </div>
            <div class="waveform-box">
              <canvas id="previewWaveform"></canvas>
              <div class="waveform-badge">LOW-SNR INPUT (3.4s)</div>
            </div>
            <button class="btn-tactical" onclick="playSimulatedAudio('raw')">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
              <span>Play Raw Audio</span>
            </button>
          </div>

          <div class="tactical-card">
            <div class="tactical-card-title">
              <span>METADATA DISPATCH</span>
            </div>
            <div style="font-size: 11px; font-family: monospace; display: flex; flex-direction: column; gap: 6px;">
              <div style="display: flex; justify-content: space-between;"><span style="color: var(--text-muted);">SOURCE:</span><span>MIC_DRDO_FIELD_01.WAV</span></div>
              <div style="display: flex; justify-content: space-between;"><span style="color: var(--text-muted);">SAMPLE RATE:</span><span>16,000 Hz (16-bit PCM)</span></div>
              <div style="display: flex; justify-content: space-between;"><span style="color: var(--text-muted);">DURATION:</span><span>3.42 seconds</span></div>
              <div style="display: flex; justify-content: space-between;"><span style="color: var(--text-muted);">PAYLOAD SIZE:</span><span>108.4 KB</span></div>
            </div>
          </div>

          <button class="btn-primary-action" onclick="startTransmitting()">
            <span>DISPATCH TO GTCRN ENGINE</span>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
          </button>
        </div>
      </div>

      <!-- SCREEN 5: TRANSMITTING & INFERENCE SCREEN -->
      <div class="screen" id="screen-transmitting">
        <div class="tactical-top-bar">
          <div class="top-bar-left">
            <div class="top-bar-title-block">
              <span class="top-bar-title">TACTICAL TRANSMISSION</span>
              <span class="top-bar-sub">DRDO EDGE GATEWAY</span>
            </div>
          </div>
        </div>

        <div class="screen-content" style="align-items: center; justify-content: center; text-align: center;">
          <div class="radar-box">
            <div class="radar-ring ring-1"></div>
            <div class="radar-ring ring-2"></div>
            <div class="radar-ring ring-3"></div>
            <div class="radar-sweep"></div>
            <div style="position: absolute; width: 8px; height: 8px; background: var(--tactical-cyan); border-radius: 50%; box-shadow: 0 0 10px var(--tactical-cyan);"></div>
          </div>

          <div>
            <h2 style="font-size: 16px; font-weight: 800; color: var(--tactical-green);" id="trans-state-title">NEURAL INFERENCE IN PROGRESS</h2>
            <p style="font-size: 11px; color: var(--text-secondary); margin-top: 4px;" id="trans-state-sub">Executing GTCRN Model on Local Edge Node</p>
          </div>

          <div style="width: 100%; max-width: 280px; background: var(--navy-card); border: 1px solid var(--navy-border); border-radius: 10px; padding: 12px; margin-top: 14px;">
            <div style="display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 6px; font-family: monospace;">
              <span>PROGRESS:</span>
              <span id="trans-pct" style="color: var(--tactical-green); font-weight: bold;">0%</span>
            </div>
            <div style="width: 100%; height: 6px; background: var(--navy-dark); border-radius: 3px; overflow: hidden;">
              <div id="trans-bar" style="width: 0%; height: 100%; background: var(--tactical-green); transition: width 0.1s;"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- SCREEN 6: RESULT SCREEN (A/B BENCH) -->
      <div class="screen" id="screen-result">
        <div class="tactical-top-bar">
          <div class="top-bar-left">
            <button class="icon-btn" onclick="navigateTo('screen-home')">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>
            </button>
            <div class="top-bar-title-block">
              <span class="top-bar-title">ENHANCEMENT VERIFIED</span>
              <span class="top-bar-sub">A/B ACOUSTIC BENCH</span>
            </div>
          </div>
        </div>

        <div class="screen-content">
          <!-- A/B Switcher -->
          <div class="tactical-card">
            <div class="tactical-card-title">
              <span>AUDIO AUDIT (INSTANT TOGGLE)</span>
            </div>
            <div style="display: flex; gap: 8px;">
              <button class="btn-tactical" id="res-btn-raw" onclick="playBench('raw')" style="flex: 1;">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
                <span>Raw (-14 dB)</span>
              </button>
              <button class="btn-tactical" id="res-btn-enh" onclick="playBench('enhanced')" style="flex: 1; background: var(--tactical-green-dark); border-color: var(--tactical-green);">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
                <span>Clean (+12 dB)</span>
              </button>
            </div>
          </div>

          <!-- Telemetry Gains -->
          <div class="telemetry-row">
            <div class="telemetry-pill">
              <div class="pill-value">+14.2 dB</div>
              <div class="pill-label">SNR Gain</div>
            </div>
            <div class="telemetry-pill">
              <div class="pill-value">3.42</div>
              <div class="pill-label">PESQ Score</div>
            </div>
            <div class="telemetry-pill">
              <div class="pill-value">+48%</div>
              <div class="pill-label">STOI Gain</div>
            </div>
          </div>

          <div class="telemetry-row">
            <div class="telemetry-pill">
              <div class="pill-value">+12.8 dB</div>
              <div class="pill-label">SI-SNR</div>
            </div>
            <div class="telemetry-pill">
              <div class="pill-value">0.38x</div>
              <div class="pill-label">Inference RTF</div>
            </div>
            <div class="telemetry-pill">
              <div class="pill-value">16 kHz</div>
              <div class="pill-label">Bandwidth</div>
            </div>
          </div>

          <div class="tactical-card">
            <div class="tactical-card-title">
              <span>DRDO FIELD ACTIONS</span>
            </div>
            <button class="btn-primary-action" onclick="alert('Clean audio stream transmitted securely via Tactical Radio Gateway!')">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
              <span>TRANSMIT CLEAN COMM</span>
            </button>
            <button class="btn-tactical" onclick="navigateTo('screen-home')" style="margin-top: 4px;">
              <span>RETURN TO CONSOLE</span>
            </button>
          </div>
        </div>
      </div>

      <!-- SCREEN 7: HISTORY SCREEN -->
      <div class="screen" id="screen-history">
        <div class="tactical-top-bar">
          <div class="top-bar-left">
            <button class="icon-btn" onclick="navigateTo('screen-home')">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>
            </button>
            <div class="top-bar-title-block">
              <span class="top-bar-title">TACTICAL HISTORY</span>
              <span class="top-bar-sub">LOCAL ROOM DATABASE</span>
            </div>
          </div>
        </div>

        <div class="screen-content">
          <div class="tactical-card">
            <div style="display: flex; justify-content: space-between; font-size: 11px; font-weight: 700;">
              <span>CAPTURED COMM #084</span>
              <span style="color: var(--tactical-green);">+14.2 dB GAIN</span>
            </div>
            <div style="font-size: 10px; color: var(--text-muted); margin-top: 2px;">Today 14:15 | Profile: UAV Drone | 3.4s</div>
            <button class="btn-tactical" onclick="navigateTo('screen-result')" style="margin-top: 6px; padding: 6px;">Audit Capture</button>
          </div>

          <div class="tactical-card">
            <div style="display: flex; justify-content: space-between; font-size: 11px; font-weight: 700;">
              <span>CAPTURED COMM #083</span>
              <span style="color: var(--tactical-green);">+11.8 dB GAIN</span>
            </div>
            <div style="font-size: 10px; color: var(--text-muted); margin-top: 2px;">Today 11:32 | Profile: Convoy | 4.8s</div>
            <button class="btn-tactical" onclick="navigateTo('screen-result')" style="margin-top: 6px; padding: 6px;">Audit Capture</button>
          </div>

          <div class="tactical-card">
            <div style="display: flex; justify-content: space-between; font-size: 11px; font-weight: 700;">
              <span>CAPTURED COMM #082</span>
              <span style="color: var(--tactical-green);">+16.5 dB GAIN</span>
            </div>
            <div style="font-size: 10px; color: var(--text-muted); margin-top: 2px;">Yesterday 19:40 | Profile: Artillery | 2.9s</div>
            <button class="btn-tactical" onclick="navigateTo('screen-result')" style="margin-top: 6px; padding: 6px;">Audit Capture</button>
          </div>
        </div>

        <!-- Tactical Bottom Bar -->
        <div class="tactical-bottom-nav">
          <button class="nav-item" onclick="navigateTo('screen-home')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 14c1.66 0 2.99-1.34 2.99-3L15 5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z"/></svg>
            <span>CONSOLE</span>
          </button>
          <button class="nav-item active" onclick="navigateTo('screen-history')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/></svg>
            <span>HISTORY</span>
          </button>
          <button class="nav-item" onclick="navigateTo('screen-settings')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
            <span>SETTINGS</span>
          </button>
        </div>
      </div>

      <!-- SCREEN 8: SETTINGS SCREEN -->
      <div class="screen" id="screen-settings">
        <div class="tactical-top-bar">
          <div class="top-bar-left">
            <button class="icon-btn" onclick="navigateTo('screen-home')">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg>
            </button>
            <div class="top-bar-title-block">
              <span class="top-bar-title">SYSTEM SETTINGS</span>
              <span class="top-bar-sub">DRDO EDGE GATEWAY</span>
            </div>
          </div>
        </div>

        <div class="screen-content">
          <div class="tactical-card">
            <div class="tactical-card-title"><span>DRDO EDGE ENDPOINT</span></div>
            <input type="text" value="http://192.168.1.10:8000" style="background: var(--navy-dark); border: 1px solid var(--navy-border); color: #FFF; padding: 10px; border-radius: 8px; font-family: monospace; font-size: 12px; width: 100%;">
            <div style="font-size: 9px; color: var(--text-muted);">Configured target for low-latency field inferencing.</div>
          </div>

          <div class="tactical-card">
            <div class="tactical-card-title"><span>NATIVE ANDROID APK PACKAGE</span></div>
            <div style="font-size: 11px; color: var(--text-secondary); line-height: 1.4;">
              Compiled native APK ready for physical device testing:
            </div>
            <a href="/app-debug.apk" class="btn-primary-action" download="RakshaVaani-debug.apk" style="margin-top: 4px;">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/></svg>
              <span>Download Native APK (24.2 MB)</span>
            </a>
          </div>

          <div class="tactical-card">
            <div class="tactical-card-title"><span>OFFLINE FIELD MODE</span></div>
            <div style="display: flex; justify-content: space-between; align-items: center; font-size: 11px;">
              <span>Store captures locally in Room DB</span>
              <span style="color: var(--tactical-green); font-weight: bold;">ACTIVE</span>
            </div>
          </div>
        </div>

        <!-- Tactical Bottom Bar -->
        <div class="tactical-bottom-nav">
          <button class="nav-item" onclick="navigateTo('screen-home')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 14c1.66 0 2.99-1.34 2.99-3L15 5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z"/></svg>
            <span>CONSOLE</span>
          </button>
          <button class="nav-item" onclick="navigateTo('screen-history')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M13 3c-4.97 0-9 4.03-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42C8.27 19.99 10.51 21 13 21c4.97 0 9-4.03 9-9s-4.03-9-9-9zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/></svg>
            <span>HISTORY</span>
          </button>
          <button class="nav-item active" onclick="navigateTo('screen-settings')">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>
            <span>SETTINGS</span>
          </button>
        </div>
      </div>

    </div>

    <!-- Android System Navigation (Back, Home, Recents) -->
    <div class="android-system-nav">
      <button class="nav-btn-sys" onclick="androidGoBack()" title="Back">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/></svg>
      </button>
      <button class="nav-btn-sys" onclick="navigateTo('screen-home')" title="Home">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="12" r="9"/></svg>
      </button>
      <button class="nav-btn-sys" onclick="navigateTo('screen-history')" title="Recents">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z"/></svg>
      </button>
    </div>
  </div>

  <script>
    // Navigation History
    let screenHistory = ['screen-splash'];

    function navigateTo(screenId) {
      document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
      const target = document.getElementById(screenId);
      if (target) {
        target.classList.add('active');
        screenHistory.push(screenId);
        document.getElementById('viewport').scrollTop = 0;
      }
    }

    function androidGoBack() {
      if (screenHistory.length > 1) {
        screenHistory.pop(); // remove current
        const prev = screenHistory[screenHistory.length - 1];
        document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
        document.getElementById(prev).classList.add('active');
      }
    }

    // Auto advance splash after 2s
    setTimeout(() => {
      const splash = document.getElementById('screen-splash');
      if (splash && splash.classList.contains('active')) {
        navigateTo('screen-home');
      }
    }, 2400);

    // Audio Context
    let audioCtx = null;
    let micRecording = false;
    let micStream = null;
    let currentAudioSource = null;

    function initWaveforms() {
      const hCanvas = document.getElementById('homeWaveform');
      if (hCanvas) {
        hCanvas.width = hCanvas.parentElement.clientWidth;
        hCanvas.height = hCanvas.parentElement.clientHeight;
        drawStaticWaveform(hCanvas, '#223652');
      }
      const pCanvas = document.getElementById('previewWaveform');
      if (pCanvas) {
        pCanvas.width = pCanvas.parentElement.clientWidth;
        pCanvas.height = pCanvas.parentElement.clientHeight;
        drawStaticWaveform(pCanvas, '#F4B400');
      }
    }
    window.addEventListener('resize', initWaveforms);
    setTimeout(initWaveforms, 200);

    function drawStaticWaveform(cvs, color) {
      const ctx = cvs.getContext('2d');
      ctx.clearRect(0, 0, cvs.width, cvs.height);
      ctx.strokeStyle = color;
      ctx.lineWidth = 2;
      ctx.beginPath();
      const mid = cvs.height / 2;
      ctx.moveTo(0, mid);
      for (let x = 0; x < cvs.width; x += 4) {
        const y = mid + Math.sin(x * 0.1) * 12 * (Math.random() * 0.4 + 0.6);
        ctx.lineTo(x, y);
      }
      ctx.stroke();
    }

    async function toggleHomeMic() {
      const btn = document.getElementById('btn-home-mic');
      const text = document.getElementById('home-mic-text');
      const badge = document.getElementById('home-rec-badge');

      if (!micRecording) {
        try {
          if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
          micStream = await navigator.mediaDevices.getUserMedia({ audio: true });
          micRecording = true;
          btn.classList.add('btn-rec-active');
          text.textContent = 'Stop Mic';
          badge.textContent = 'REC: LIVE 16k PCM';
          badge.style.color = 'var(--tactical-red)';
        } catch(e) {
          alert('Microphone permission or hardware unavailable in browser preview.');
        }
      } else {
        micRecording = false;
        if (micStream) micStream.getTracks().forEach(t => t.stop());
        btn.classList.remove('btn-rec-active');
        text.textContent = 'Record Mic';
        badge.textContent = 'SIGNAL CAPTURED (3.4s)';
        badge.style.color = 'var(--tactical-green)';
      }
    }

    function loadTacticalPreset(type) {
      const badge = document.getElementById('home-rec-badge');
      badge.textContent = 'LOADED: ' + type.toUpperCase() + '_TEST.WAV';
      badge.style.color = 'var(--tactical-green)';
      const hCanvas = document.getElementById('homeWaveform');
      if (hCanvas) drawStaticWaveform(hCanvas, '#22C55E');
    }

    function setProfile(el, name, snr) {
      document.querySelectorAll('.chip-item').forEach(c => c.classList.remove('selected'));
      el.classList.add('selected');
      document.getElementById('selected-profile-tag').textContent = name.toUpperCase();
    }

    function startTransmitting() {
      navigateTo('screen-transmitting');
      let pct = 0;
      const bar = document.getElementById('trans-bar');
      const pctText = document.getElementById('trans-pct');
      const interval = setInterval(() => {
        pct += 5;
        if (pct > 100) {
          clearInterval(interval);
          setTimeout(() => {
            navigateTo('screen-result');
          }, 300);
        } else {
          bar.style.width = pct + '%';
          pctText.textContent = pct + '%';
        }
      }, 70);
    }

    function playBench(mode) {
      if (currentAudioSource) {
        try { currentAudioSource.stop(); } catch(e){}
        currentAudioSource = null;
      }
      if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();

      const duration = 3.0;
      const buffer = audioCtx.createBuffer(1, audioCtx.sampleRate * duration, audioCtx.sampleRate);
      const data = buffer.getChannelData(0);

      for (let i = 0; i < data.length; i++) {
        const t = i / audioCtx.sampleRate;
        const voice = (Math.sin(2 * Math.PI * 220 * t) + 0.4 * Math.sin(2 * Math.PI * 440 * t)) * (0.5 + 0.5 * Math.sin(2 * Math.PI * 2 * t));
        const drone = Math.sin(2 * Math.PI * 130 * t) + 0.7 * Math.sin(2 * Math.PI * 260 * t);
        const noise = (Math.random() * 2 - 1) * 0.7 + drone * 0.5;

        if (mode === 'raw') {
          data[i] = 0.25 * voice + 0.5 * noise;
        } else {
          data[i] = 0.7 * voice + 0.02 * noise;
        }
      }

      currentAudioSource = audioCtx.createBufferSource();
      currentAudioSource.buffer = buffer;
      currentAudioSource.connect(audioCtx.destination);
      currentAudioSource.start();
    }

    function playSimulatedAudio(mode) {
      playBench(mode);
    }

    // Android Clock Update
    function updateClock() {
      const now = new Date();
      const h = String(now.getHours()).padStart(2, '0');
      const m = String(now.getMinutes()).padStart(2, '0');
      const el = document.getElementById('android-clock');
      if (el) el.textContent = h + ':' + m;
    }
    setInterval(updateClock, 1000);
    updateClock();
  </script>
</body>
</html>
`;

const server = http.createServer((req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  // Serve image assets from app/src/main/res/drawable/
  if (pathname.startsWith("/images/")) {
    const imageName = path.basename(pathname);
    const imagePath = path.join(APP_DIR, "app", "src", "main", "res", "drawable", imageName);
    if (fs.existsSync(imagePath)) {
      const ext = path.extname(imagePath).toLowerCase();
      const contentType = ext === ".png" ? "image/png" : ext === ".webp" ? "image/webp" : "image/jpeg";
      res.writeHead(200, { "Content-Type": contentType, "Cache-Control": "public, max-age=86400" });
      fs.createReadStream(imagePath).pipe(res);
      return;
    }
  }

  // Serve APK binary
  if (pathname === "/app-debug.apk" || pathname === "/download-apk" || pathname === "/download/app-debug.apk") {
    const apkInfo = getApkInfo();
    if (apkInfo.exists) {
      res.writeHead(200, {
        "Content-Type": "application/vnd.android.package-archive",
        "Content-Disposition": 'attachment; filename="RakshaVaani-debug.apk"',
        "Content-Length": apkInfo.sizeBytes,
        "Cache-Control": "no-cache",
      });
      fs.createReadStream(apkInfo.path).pipe(res);
      return;
    } else {
      res.writeHead(404, { "Content-Type": "text/plain" });
      res.end("APK not found.");
      return;
    }
  }

  // Status API
  if (pathname === "/api/status") {
    const apkInfo = getApkInfo();
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ status: "ready", app: "RakshaVaani", apk: apkInfo }));
    return;
  }

  // Health
  if (pathname === "/health") {
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ status: "healthy" }));
    return;
  }

  // Default: Android Device Interactive Simulator
  res.writeHead(200, {
    "Content-Type": "text/html; charset=utf-8",
    "Cache-Control": "no-cache",
  });
  res.end(htmlContent);
});

server.listen(PORT, "0.0.0.0", () => {
  console.log(`[RakshaVaani] Android Preview Simulator active on 0.0.0.0:${PORT}`);
});
