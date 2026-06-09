const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

let serviceAccount;

try {
  if (process.env.FIREBASE_SERVICE_ACCOUNT_JSON) {
    serviceAccount = JSON.parse(
      Buffer.from(process.env.FIREBASE_SERVICE_ACCOUNT_JSON, 'base64').toString('utf8')
    );
  } else {
    const serviceAccountPath = path.join(__dirname, 'serviceAccountKey.json');
    if (fs.existsSync(serviceAccountPath)) {
      // Use fs.readFileSync to ensure clean data reading
      const rawData = fs.readFileSync(serviceAccountPath, 'utf8');
      serviceAccount = JSON.parse(rawData);
    } else {
      throw new Error("Service account file not found at " + serviceAccountPath);
    }
  }

  // Ensure the private key newlines are handled correctly
  if (serviceAccount.private_key && typeof serviceAccount.private_key === 'string') {
    serviceAccount.private_key = serviceAccount.private_key.replace(/\\n/g, '\n');
  }

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://fly-money-d4a03-default-rtdb.asia-southeast1.firebasedatabase.app",
    storageBucket: "fly-money-d4a03.firebasestorage.app"
  });

  console.log("✅ Firebase Admin initialized successfully for project:", serviceAccount.project_id);
} catch (error) {
  console.error("\n❌ FIREBASE ADMIN INITIALIZATION ERROR:");
  console.error(error);
  process.exit(1);
}

const db = admin.firestore();
const rtdb = admin.database();
const auth = admin.auth();
const storage = admin.storage();

// Test RTDB connection with error logging
rtdb.ref('.info/connected').on('value', (snapshot) => {
  if (snapshot.val() === true) {
    console.log("✅ Realtime Database connected");
  } else {
    console.log("⚠️ Realtime Database disconnected - Check your credentials or internet");
  }
}, (error) => {
  console.error("❌ RTDB Connection Error:", error);
});

module.exports = { admin, db, rtdb, auth, storage };
