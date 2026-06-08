const RoundState = {
  WAITING: 'WAITING',
  RUNNING: 'RUNNING',
  CRASHED: 'CRASHED'
};

class RoundManager {
  constructor() {
    this.state = RoundState.WAITING;
    this.currentRoundId = null;
    this.multiplier = 1.00;
    this.crashAt = null;
    this.startTime = null;
  }

  setWaiting(roundId, startTime) {
    this.state = RoundState.WAITING;
    this.currentRoundId = roundId;
    this.multiplier = 1.00;
    this.crashAt = null;
    this.startTime = startTime;
  }

  setRunning(startTime) {
    this.state = RoundState.RUNNING;
    this.startTime = startTime;
  }

  setCrashed(crashAt) {
    this.state = RoundState.CRASHED;
    this.multiplier = crashAt;
    this.crashAt = crashAt;
  }

  updateMultiplier(m) {
    this.multiplier = m;
  }

  getState() {
    return this.state;
  }

  getMultiplier() {
    return this.multiplier;
  }
}

module.exports = { RoundManager, RoundState };
