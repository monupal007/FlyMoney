const express = require('express');
const router = express.Router();
const { db, admin, rtdb } = require('../firebase/admin');
const { verifyFirebaseToken } = require('./auth');
const gameEngine = require('../game/GameEngine');
const betManager = require('../game/BetManager');

router.post('/', verifyFirebaseToken, async (req, res) => {
  const { amount, autoCashoutAt } = req.body;
  const uid = req.uid;

  if (gameEngine.getRoundState() !== 'WAITING') {
    return res.status(400).json({ error: 'Bets are only accepted during WAITING phase' });
  }

  const betAmount = parseFloat(amount);
  if (isNaN(betAmount) || betAmount < 1) {
    return res.status(400).json({ error: 'Invalid bet amount' });
  }

  try {
    let newBalance;
    let username;

    await db.runTransaction(async (t) => {
      const userRef = db.collection('users').doc(uid);
      const userDoc = await t.get(userRef);

      let userData;
      if (!userDoc.exists) {
        // AUTO-CREATE Profile if missing (Fixes 'User not found' error)
        userData = {
          uid: uid,
          username: `Player_${uid.substring(0, 5)}`,
          walletBalance: 0,
          createdAt: admin.firestore.FieldValue.serverTimestamp()
        };
        t.set(userRef, userData);
      } else {
        userData = userDoc.data();
      }

      username = userData.username;
      const currentBalance = userData.walletBalance || 0;

      if (currentBalance < betAmount) {
        throw new Error(`Insufficient balance. Current: ₹${currentBalance}`);
      }

      newBalance = currentBalance - betAmount;
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
    console.error('❌ Bet Error:', error.message);
    res.status(400).json({ error: error.message });
  }
});

module.exports = router;
