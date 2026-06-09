import { initializeApp } from "firebase/app";
import { getAuth, GoogleAuthProvider } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

const firebaseConfig = {
    apiKey: "AIzaSyABR8VbTZlovQhmcJKAVIqT-6o9OVY5k7A",
    authDomain: "sobriamente-c3d97.firebaseapp.com",
    projectId: "sobriamente-c3d97",
    storageBucket: "sobriamente-c3d97.firebasestorage.app",
    messagingSenderId: "453549941696",
    appId: "1:453549941696:web:03b048aa81fa9966279e25"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();
export const db = getFirestore(app);
