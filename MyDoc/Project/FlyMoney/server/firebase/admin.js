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
      serviceAccount = require(serviceAccountPath);
    } else {
      throw new Error("Service account file not found at " + serviceAccountPath);
    }
  }

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  // Use the exact URL from your Realtime Database dashboard
  databaseURL: "https://fly-money-d4a03-default-rtdb.firebaseio.com",
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

// Test RTDB connection
rtdb.ref('.info/connected').on('value', (snapshot) => {
  if (snapshot.val() === true) {
    console.log("✅ Realtime Database connected");
  } else {
    console.log("⚠️ Realtime Database disconnected");
  }
});

module.exports = { admin, db, rtdb, auth, storage };
