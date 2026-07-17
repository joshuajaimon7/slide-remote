const WebSocket = require('ws');
const { exec } = require('child_process');
const os = require('os');

const PORT = 8765;

// --- Simulate arrow key presses via AppleScript (works with PowerPoint, Keynote, browser Slides, PDF viewers, etc) ---
function pressKey(direction) {
  const keyCode = direction === 'next' ? 124 : 123; // 124 = right arrow, 123 = left arrow
  const script = `tell application "System Events" to key code ${keyCode}`;
  exec(`osascript -e '${script}'`, (err) => {
    if (err) console.error('osascript error:', err.message);
    else console.log(`-> ${direction} (key code ${keyCode})`);
  });
}

// --- Print local IPs so it's easy to know what to type into the phone app ---
function printLocalIPs() {
  const nets = os.networkInterfaces();
  console.log('\nYour Mac local IP address(es) — enter one of these in the Android app:');
  for (const name of Object.keys(nets)) {
    for (const net of nets[name]) {
      if (net.family === 'IPv4' && !net.internal) {
        console.log(`  ${net.address}`);
      }
    }
  }
  console.log('');
}

const wss = new WebSocket.Server({ port: PORT });

wss.on('connection', (ws) => {
  console.log('Phone connected!');

  ws.on('message', (data) => {
    const msg = data.toString().trim();
    if (msg === 'next' || msg === 'prev') {
      pressKey(msg);
    } else {
      console.log('Unknown message:', msg);
    }
  });

  ws.on('close', () => console.log('Phone disconnected'));

  ws.send('connected'); // handshake ack so the phone knows it's live
});

console.log(`Slide remote server running on port ${PORT}`);
printLocalIPs();
console.log('Waiting for phone to connect...\n');
console.log('NOTE: First run will need macOS Accessibility permission for');
console.log('Terminal (or iTerm/your terminal app) under:');
console.log('System Settings > Privacy & Security > Accessibility\n');
