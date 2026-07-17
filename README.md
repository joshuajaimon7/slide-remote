# Slide Remote — volume buttons control slides on your Mac

## How it works
Your phone's volume keys → Android Accessibility Service intercepts them →
sends "next"/"prev" over WebSocket → Node server on your Mac → simulates
right/left arrow key presses via AppleScript → advances whatever's focused
(PowerPoint, Keynote, Google Slides in browser, PDF viewer, anything that
uses arrow keys to navigate).

## 1. Run the Mac server
```bash
cd mac-server
npm install
npm start
```
It'll print your local IP(s), e.g. `192.168.1.42`. Keep this terminal open.

First time you run it, macOS will prompt for **Accessibility permission**:
System Settings → Privacy & Security → Accessibility → enable your terminal
app (Terminal/iTerm). Without this, the simulated key presses won't fire.

## 2. Build the Android app
Open `android-app/` in Android Studio, let it sync Gradle, then run it on
your phone (USB debugging or wireless debugging).

## 3. Connect
1. On the phone app, type in your Mac's IP from step 1, tap **Connect**.
   Status should flip to "Connected ✓".
2. Tap **Enable Volume-Key Control** — this opens Settings → Accessibility.
   Find "Slide Remote" and turn it on. Android requires this to be done
   manually; there's no way around it for accessibility services.
3. Open your slides on the Mac, click into the presentation window so it
   has focus, then press volume up/down on the phone.

## Notes / things to tune
- **Both devices must be on the same WiFi network.** No internet/cloud
  relay involved — it's a direct local connection, so latency is basically
  zero.
- If your Mac's IP changes (different WiFi network, DHCP renewal), you'll
  need to reconnect with the new IP. For v2, mDNS/Bonjour discovery would
  remove this step entirely.
- The debounce in `VolumeAccessibilityService.kt` is set to 300ms — bump it
  up if double-fires happen, or down if it feels laggy on rapid presses.
- Right now it's arrow-key-based, so it works with literally any app that
  uses arrows to navigate slides — not just PowerPoint/Keynote.
- For a hardened v2: add a shared secret/PIN exchanged on connect so random
  devices on the same network can't send commands to your Mac.
