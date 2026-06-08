const betManager = require('../game/BetManager');

function testBetManager() {
  console.log('Testing BetManager...');

  // 1. Place Bet
  betManager.placeBet('user1', 'player1', 100, 2.0);
  console.log('Bet placed for user1');

  try {
    betManager.placeBet('user1', 'player1', 100, 2.0);
  } catch (e) {
    console.log('Successfully caught duplicate bet error');
  }

  // 2. Check auto-cashouts
  const autoCashouts = betManager.checkAutoCashouts(2.0);
  if (autoCashouts.length === 1 && autoCashouts[0].uid === 'user1') {
    console.log('Auto-cashout logic works');
  } else {
    console.error('Auto-cashout logic failed');
  }

  // 3. Manual Cashout
  betManager.clear();
  betManager.placeBet('user2', 'player2', 50, null);
  const result = betManager.cashOut('user2', 1.5);
  if (result.winnings === 75) {
    console.log('Manual cashout calculation works');
  } else {
    console.error('Manual cashout calculation failed', result.winnings);
  }

  // 4. Remaining bets
  betManager.placeBet('user3', 'player3', 200, 5.0);
  const remaining = betManager.getRemainingBets();
  if (remaining.length === 1 && remaining[0].uid === 'user3') {
    console.log('getRemainingBets works');
  } else {
    console.error('getRemainingBets failed');
  }

  betManager.clear();
  console.log('BetManager tests completed.');
}

testBetManager();
