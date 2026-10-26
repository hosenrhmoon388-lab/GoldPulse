# GoldPulse Android project

## Overview
GoldPulse is a dark-mode RTL Android app for monitoring XAUUSD in demo mode. It is designed for a one-handed mobile workflow and intentionally avoids automatic execution of MT5 orders.

## Features implemented
- RTL Arabic UI with dark navy-and-gold theme
- Live / delayed / disconnected market status
- H1 trend and M15 confirmation indicators
- Manual signal selection and risk options
- Entry, SL, TP1/2/3 calculations
- Confidence and conditional failure display
- News impact card
- Manual review workflow with confirmation before any action
- Bottom navigation: Monitoring, Notifications, Activity log
- Local mock market data for offline/demo development
- Backend-ready API model for future integration

## Build instructions
1. Open the project in Android Studio.
2. Ensure Android SDK 34 or newer is installed.
3. Sync Gradle.
4. Build the APK using Build > Build Bundle(s)/APK(s) > Build APK(s).

## Safety and architecture notes
- No API key or MT5 credentials are included in the app.
- No automatic order execution is implemented.
- This demo version uses local mock data until a secure backend is connected.
- The app is prepared for future integration with backend endpoints:
  - GET /api/market/xauusd
  - GET /api/market/xauusd/candles?timeframe=H1&limit=200
  - GET /api/market/xauusd/candles?timeframe=M15&limit=200

## Files included
- app/src/main/AndroidManifest.xml
- app/src/main/java/com/goldpulse/MainActivity.kt
- app/src/main/res/values/strings.xml
- app/src/main/res/values/themes.xml

## Notes
This repository intentionally contains a working Android project structure ready for export and APK generation, while avoiding any embedded live data or unsafe trade execution logic.
