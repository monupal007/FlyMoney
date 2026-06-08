const express = require('express');
const router = express.Router();
const { db, admin, rtdb } = require('../firebase/admin');
const { verifyFirebaseToken } = require('./auth');
const gameEngine = require('../game/GameEngine');
const betManager = require('../game/BetManager');

router.post('/', verifyFirebaseToken, async (req, res) => {
  const { amount, autoCashoutAt } = req.body;
  const uid = req.uid;

  // Validation
  if (gameEngine.getRoundState() !== 'WAITING') {
    return res.status(400).json({ error: 'Bets are only accepted during WAITING phase' });
  }

  const betAmount = parseFloat(amount);
  if (isNaN(betAmount) || betAmount < 1 || betAmount > 10000) {
    return res.status(400).json({ error: 'Invalid bet amount (1 - 10,000)' });
  }

  try {
    let newBalance;
    let username;

    await db.runTransaction(async (t) => {
      const userRef = db.collection('users').doc(uid);
      const userDoc = await t.get(userRef);

      if (!userDoc.exists) {
        throw new Error('User not found');
      }

      const userData = userDoc.data();
      username = userData.username;
      if (userData.walletBalance < betAmount) {
        throw new Error('Insufficient balance');
      }

      newBalance = userData.walletBalance - betAmount;
      t.update(userRef, { walletBalance: newBalance });

      const txRef = db.collection('transactions').doc();
      t.set(txRef, {
        uid,
        type: 'BET',
        amount: betAmount,
        roundId: gameEngine.getRoundId(),
        timestamp: admin.firestore.FieldValue.serverTimestamp()
      });
    });

    betManager.placeBet(uid, username, betAmount, autoCashoutAt);

    // Write to RTDB for live UI
    const roundId = gameEngine.getRoundId();
    await rtdb.ref(`rounds/${roundId}/bets/${uid}`).set({
      amount: betAmount,
      username,
      autoCashoutAt: autoCashoutAt || null,
      placedAt: Date.now()
    });

    gameEngine.broadcast({
      type: 'BET',
      uid,
      username,
      amount: betAmount
    });

    res.json({ success: true, newBalance });
  } catch (error) {
    console.error('Bet error:', error);
    res.status(400).json({ error: error.message });
  }
});

module.exports = router;
