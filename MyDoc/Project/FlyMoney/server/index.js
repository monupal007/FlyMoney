require('dotenv').config();
const express = require('express');
const http = require('http');
const WebSocket = require('ws');
const cors = require('cors');
const gameEngine = require('./game/GameEngine');
const betRoutes = require('./routes/bet');
const cashoutRoutes = require('./routes/cashout');

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

app.use(cors());
app.use(express.json());

// WebSocket handling
wss.on('connection', (ws) => {
  console.log('New client connected');

  ws.on('message', (message) => {
    try {
      const data = JSON.parse(message);
      gameEngine.handleWsMessage(ws, data);
    } catch (e) {
      console.error('Invalid WS message', e);
    }
  });

  ws.on('close', () => {
    console.log('Client disconnected');
  });
});

// Pass WSS to game engine for broadcasts
gameEngine.init(wss);

// Routes
app.use('/api/bet', betRoutes);
app.use('/api/cashout', cashoutRoutes);

const PORT = process.env.PORT || 3000;
server.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  gameEngine.start();
});
