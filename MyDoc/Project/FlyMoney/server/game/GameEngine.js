const { rtdb, db, admin } = require('../firebase/admin');
const ProvablyFair = require('./ProvablyFair');
const betManager = require('./BetManager');

class GameEngine {
  constructor() {
    this.wss = null;
    this.currentRound = {
      roundId: null,
      state: 'WAITING',
      multiplier: 1.00,
      crashAt: null,
      startTime: null,
      countdownSec: 5,
      serverSeed: null,
      serverSeedHash: null
    };
    this.tickInterval = null;
  }

  init(wss) {
    this.wss = wss;
  }

  async start() {
    this.newRound();
  }

  async newRound() {
    const roundId = ProvablyFair.generateRoundId();
    const serverSeed = ProvablyFair.generateServerSeed();
    const serverSeedHash = ProvablyFair.hashSeed(serverSeed);
    const crashAt = ProvablyFair.generateCrashPoint(serverSeed, roundId);

    this.currentRound = {
      roundId,
      state: 'WAITING',
      multiplier: 1.00,
      crashAt,
      startTime: Date.now() + 5000,
      countdownSec: 5,
      serverSeed,
      serverSeedHash
    };

    betManager.clear();

    // Update RTDB
    await rtdb.ref('currentRound').set({
      roundId: this.currentRound.roundId,
      state: 'WAITING',
      multiplier: 1.00,
      startTime: this.currentRound.startTime,
      countdownSec: 5,
      serverSeedHash: this.currentRound.serverSeedHash
    });

    this.broadcast({ type: 'WAITING', countdownSec: 5 });

    let remaining = 5;
    const countdownInterval = setInterval(() => {
      remaining--;
      if (remaining > 0) {
        rtdb.ref('currentRound/countdownSec').set(remaining);
        this.broadcast({ type: 'WAITING', countdownSec: remaining });
      } else {
        clearInterval(countdownInterval);
        this.runRound();
      }
    }, 1000);
  }

  async runRound() {
    this.currentRound.state = 'RUNNING';
    this.currentRound.startTime = Date.now();

    await rtdb.ref('currentRound').update({
      state: 'RUNNING',
      startTime: this.currentRound.startTime
    });

    this.tickInterval = setInterval(async () => {
      const elapsedMs = Date.now() - this.currentRound.startTime;
      // Multiplier formula: e^(0.00006 * t)
      let multiplier = Math.pow(Math.E, 0.00006 * elapsedMs);
      multiplier = Math.round(multiplier * 100) / 100;

      if (multiplier >= this.currentRound.crashAt) {
        this.crash(this.currentRound.crashAt);
      } else {
        this.currentRound.multiplier = multiplier;
        rtdb.ref('currentRound/multiplier').set(multiplier);
        this.broadcast({ type: 'TICK', multiplier });

        // Check auto-cashouts
        const autoCashouts = betManager.checkAutoCashouts(multiplier);
        for (const cashout of autoCashouts) {
          this.handleAutoCashout(cashout);
        }
      }
    }, 100);
  }

  async crash(crashAt) {
    clearInterval(this.tickInterval);
    this.currentRound.state = 'CRASHED';
    this.currentRound.multiplier = crashAt;

    await rtdb.ref('currentRound').update({
      state: 'CRASHED',
      multiplier: crashAt,
      crashAt: crashAt
    });

    this.broadcast({ type: 'CRASHED', crashAt });

    await this.settleRound();

    setTimeout(() => {
      this.newRound();
    }, 2000);
  }

  async handleAutoCashout(cashout) {
    const { uid, multiplier, winnings } = cashout;

    try {
      await db.runTransaction(async (t) => {
        const userRef = db.collection('users').doc(uid);
        const userDoc = await t.get(userRef);
        const newBalance = userDoc.data().walletBalance + winnings;
        t.update(userRef, { walletBalance: newBalance });

        const txRef = db.collection('transactions').doc();
        t.set(txRef, {
          uid,
          type: 'WIN',
          amount: winnings,
          multiplier,
          roundId: this.currentRound.roundId,
          timestamp: admin.firestore.FieldValue.serverTimestamp()
        });
      });

      rtdb.ref(`rounds/${this.currentRound.roundId}/cashouts/${uid}`).set({
        multiplier,
        winnings,
        cashedOutAt: Date.now()
      });

      this.broadcast({
        type: 'CASHOUT',
        uid,
        username: cashout.username,
        multiplier,
        winnings
      });
    } catch (e) {
      console.error('Auto cashout transaction failed', e);
    }
  }

  async settleRound() {
    const bustedBets = betManager.getRemainingBets();
    const roundId = this.currentRound.roundId;

    const batch = db.batch();
    let totalBetAmount = 0;
    const allBets = betManager.getAllBets();

    for (const bet of allBets) {
      totalBetAmount += bet.amount;
    }

    for (const bet of bustedBets) {
      const txRef = db.collection('transactions').doc();
      batch.set(txRef, {
        uid: bet.uid,
        type: 'BUST',
        amount: bet.amount,
        roundId: roundId,
        timestamp: admin.firestore.FieldValue.serverTimestamp()
      });

      const userRef = db.collection('users').doc(bet.uid);
      batch.update(userRef, {
        totalBets: admin.firestore.FieldValue.increment(1)
      });
    }

    const roundRef = db.collection('rounds').doc(roundId);
    batch.set(roundRef, {
      roundId,
      crashPoint: this.currentRound.crashAt,
      serverSeed: this.currentRound.serverSeed,
      serverSeedHash: this.currentRound.serverSeedHash,
      totalBetAmount,
      totalPlayers: allBets.length,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });

    await batch.commit();
    // updateLeaderboard() logic would go here
  }

  broadcast(message) {
    if (!this.wss) return;
    const payload = JSON.stringify(message);
    this.wss.clients.forEach((client) => {
      if (client.readyState === 1) { // WebSocket.OPEN
        client.send(payload);
      }
    });
  }

  handleWsMessage(ws, data) {
    if (data.type === 'AUTH') {
      admin.auth().verifyIdToken(data.idToken)
        .then((decodedToken) => {
          ws.uid = decodedToken.uid;
          ws.send(JSON.stringify({ type: 'AUTH_SUCCESS' }));
        })
        .catch(() => {
          ws.send(JSON.stringify({ type: 'AUTH_ERROR' }));
        });
    } else if (data.type === 'CHAT') {
      if (ws.uid) {
        // BroadCast chat (we'd ideally fetch username here)
        this.broadcast({
          type: 'CHAT',
          uid: ws.uid,
          message: data.message,
          timestamp: Date.now()
        });
      }
    } else if (data.type === 'PING') {
      ws.send(JSON.stringify({ type: 'PONG' }));
    }
  }

  getCurrentMultiplier() {
    return this.currentRound.multiplier;
  }

  getRoundState() {
    return this.currentRound.state;
  }

  getRoundId() {
    return this.currentRound.roundId;
  }
}

module.exports = new GameEngine();
