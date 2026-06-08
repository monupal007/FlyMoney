class BetManager {
  constructor() {
    this.bets = new Map(); // userId -> { amount, autoCashoutAt, placedAt, uid, username }
    this.cashouts = new Map(); // userId -> { multiplier, winnings, cashedOutAt }
  }

  placeBet(uid, username, amount, autoCashoutAt) {
    if (this.bets.has(uid)) {
      throw new Error('Bet already placed for this round');
    }
    this.bets.set(uid, {
      uid,
      username,
      amount,
      autoCashoutAt: autoCashoutAt ? parseFloat(autoCashoutAt) : null,
      placedAt: Date.now()
    });
  }

  cashOut(uid, currentMultiplier) {
    if (!this.bets.has(uid)) {
      throw new Error('No active bet found');
    }
    if (this.cashouts.has(uid)) {
      throw new Error('Already cashed out');
    }

    const bet = this.bets.get(uid);
    const winnings = Math.floor(bet.amount * currentMultiplier * 100) / 100;

    const cashoutData = {
      uid,
      multiplier: currentMultiplier,
      winnings,
      cashedOutAt: Date.now()
    };

    this.cashouts.set(uid, cashoutData);
    return { ...bet, ...cashoutData };
  }

  checkAutoCashouts(currentMultiplier) {
    const autoCashedOut = [];
    for (const [uid, bet] of this.bets.entries()) {
      if (!this.cashouts.has(uid) && bet.autoCashoutAt && currentMultiplier >= bet.autoCashoutAt) {
        try {
          const result = this.cashOut(uid, bet.autoCashoutAt);
          autoCashedOut.push(result);
        } catch (e) {
          console.error(`Auto-cashout failed for ${uid}:`, e.message);
        }
      }
    }
    return autoCashedOut;
  }

  getRemainingBets() {
    const remaining = [];
    for (const [uid, bet] of this.bets.entries()) {
      if (!this.cashouts.has(uid)) {
        remaining.push(bet);
      }
    }
    return remaining;
  }

  getAllBets() {
    return Array.from(this.bets.values());
  }

  clear() {
    this.bets.clear();
    this.cashouts.clear();
  }
}

module.exports = new BetManager();
