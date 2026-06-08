const express = require('express');
const router = express.Router();
const { db, admin, rtdb } = require('../firebase/admin');
const { verifyFirebaseToken } = require('./auth');
const gameEngine = require('../game/GameEngine');
const betManager = require('../game/BetManager');

router.post('/', verifyFirebaseToken, async (req, res) => {
  const uid = req.uid;

  if (gameEngine.getRoundState() !== 'RUNNING') {
    return res.status(400).json({ error: 'Cash out only allowed during RUNNING phase' });
  }

  try {
    const currentMultiplier = gameEngine.getCurrentMultiplier();
    const result = betManager.cashOut(uid, currentMultiplier);

    let newBalance;

    await db.runTransaction(async (t) => {
      const userRef = db.collection('users').doc(uid);
      const userDoc = await t.get(userRef);

      if (!userDoc.exists) {
        throw new Error('User not found');
      }

      newBalance = userDoc.data().walletBalance + result.winnings;
      t.update(userRef, {
        walletBalance: newBalance,
        totalWins: admin.firestore.FieldValue.increment(1),
        biggestWin: admin.firestore.FieldValue.arrayUnion(result.winnings) // Logic for max would be better but simplified for now
      });

      const txRef = db.collection('transactions').doc();
      t.set(txRef, {
        uid,
        type: 'WIN',
        amount: result.winnings,
        multiplier: currentMultiplier,
        roundId: gameEngine.getRoundId(),
        timestamp: admin.firestore.FieldValue.serverTimestamp()
      });
    });

    // Write to RTDB
    await rtdb.ref(`rounds/${gameEngine.getRoundId()}/cashouts/${uid}`).set({
      multiplier: currentMultiplier,
      winnings: result.winnings,
      cashedOutAt: Date.now()
    });

    gameEngine.broadcast({
      type: 'CASHOUT',
      uid,
      username: result.username,
      multiplier: currentMultiplier,
      winnings: result.winnings
    });

    res.json({
      success: true,
      multiplier: currentMultiplier,
      winnings: result.winnings,
      newBalance
    });
  } catch (error) {
    console.error('Cashout error:', error);
    res.status(400).json({ error: error.message });
  }
});

module.exports = router;
