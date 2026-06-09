require('dotenv').config();
const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const cors = require('cors');
const gameEngine = require('./game/GameEngine');
const betRoutes = require('./routes/bet');
const cashoutRoutes = require('./routes/cashout');

// GLOBAL ERROR LOGGING
process.on('uncaughtException', (err) => {
  console.error('❌ UNCAUGHT EXCEPTION:', err);
});

process.on('unhandledRejection', (reason, promise) => {
  console.error('❌ UNHANDLED REJECTION at:', promise, 'reason:', reason);
});

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

app.use(cors());
app.use(express.json());

// WebSocket handling with error logs
wss.on('connection', (ws) => {
  console.log('✅ New client connected');

  ws.on('message', (message) => {
    try {
      const data = JSON.parse(message);
      gameEngine.handleWsMessage(ws, data);
    } catch (e) {
      console.error('❌ Invalid WS message received:', message, 'Error:', e.message);
    }
  });

  ws.on('error', (error) => {
    console.error('❌ WebSocket error:', error);
  });

  ws.on('close', () => {
    console.log('ℹ️ Client disconnected');
  });
});

gameEngine.init(wss);

app.use('/api/bet', betRoutes);
app.use('/api/cashout', cashoutRoutes);

// Catch-all for 404s
app.use((req, res) => {
  console.warn(`⚠️ 404 Not Found: ${req.method} ${req.url}`);
  res.status(404).json({ error: 'Endpoint not found' });
});

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`🚀 Server running on port ${PORT}`);
  gameEngine.start();
});
