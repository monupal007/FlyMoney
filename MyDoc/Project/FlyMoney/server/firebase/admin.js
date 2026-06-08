const admin = require('firebase-admin');

const serviceAccount = JSON.parse(
  Buffer.from(process.env.FIREBASE_SERVICE_ACCOUNT_JSON, 'base64').toString('utf8')
);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: `https://${serviceAccount.project_id}-default-rtdb.firebaseio.com`,
  storageBucket: `${serviceAccount.project_id}.appspot.com`
});

const db = admin.firestore();
const rtdb = admin.database();
const auth = admin.auth();
const storage = admin.storage();

module.exports = { admin, db, rtdb, auth, storage };
