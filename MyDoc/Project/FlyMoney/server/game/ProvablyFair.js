const crypto = require('crypto');

/**
 * Generates a crash point using a provably fair algorithm.
 * @param {string} serverSeed - The secret server seed.
 * @param {string} roundId - The unique ID of the round.
 * @returns {number} - The multiplier at which the game crashes.
 */
function generateCrashPoint(serverSeed, roundId) {
  const hmac = crypto.createHmac('sha256', serverSeed);
  hmac.update(roundId.toString());
  const hash = hmac.digest('hex');

  // Use first 8 characters (32 bits) of the hash
  const h = parseInt(hash.slice(0, 8), 16);
  const e = Math.pow(2, 32);

  // House edge 3%: if h % 33 == 0, crash at 1.00
  if (h % 33 === 0) return 1.00;

  // Standard crash game formula: (100 * e - h) / (e - h)
  const multiplier = Math.floor((100 * e - h) / (e - h)) / 100;
  return Math.max(1.00, multiplier);
}

/**
 * Generates a unique round ID.
 */
function generateRoundId() {
  return Date.now().toString() + Math.random().toString(36).slice(2, 7);
}

/**
 * Generates a random 32-byte server seed in hex format.
 */
function generateServerSeed() {
  return crypto.randomBytes(32).toString('hex');
}

/**
 * Hashes a seed using SHA-256 for public disclosure before the round.
 */
function hashSeed(seed) {
  return crypto.createHash('sha256').update(seed).digest('hex');
}

module.exports = {
  generateCrashPoint,
  generateRoundId,
  generateServerSeed,
  hashSeed
};
