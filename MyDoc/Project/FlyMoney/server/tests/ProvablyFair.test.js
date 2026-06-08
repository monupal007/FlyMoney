const { generateCrashPoint } = require('../game/ProvablyFair');

function testDistribution() {
  const iterations = 10000;
  let totalMultiplier = 0;
  let instantCrashes = 0;
  let highMultipliers = 0;

  for (let i = 0; i < iterations; i++) {
    const seed = Math.random().toString(36);
    const roundId = i.toString();
    const crashAt = generateCrashPoint(seed, roundId);

    totalMultiplier += crashAt;
    if (crashAt === 1.00) instantCrashes++;
    if (crashAt >= 10.00) highMultipliers++;
  }

  console.log(`Results over ${iterations} rounds:`);
  console.log(`Average Multiplier: ${totalMultiplier / iterations}`);
  console.log(`Instant Crashes (1.00x): ${((instantCrashes / iterations) * 100).toFixed(2)}% (Target: ~3%)`);
  console.log(`High Multipliers (>=10x): ${((highMultipliers / iterations) * 100).toFixed(2)}%`);
}

testDistribution();
