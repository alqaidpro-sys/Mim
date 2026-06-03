// firebase.js - تهيئة خدمات Firebase وتصدير قاعدة بيانات Cloud Firestore
// الإصدار المستخدم: Firebase v9+ Modular SDK

import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
// يمكن هنا استيراد أي خدمات إضافية إذا لزم الأمر مثل الموثق (getAuth)
import { getAuth } from "firebase/auth";

// إعدادات وبيانات مشروع Firebase الخاص بك
const firebaseConfig = {
  apiKey: "AIzaSyAKuXowKNURE_Zmvp0GLU3Qlf74sbhJ_Pw",
  authDomain: "mimpro-3b84d.firebaseapp.com",
  projectId: "mimpro-3b84d",
  storageBucket: "mimpro-3b84d.firebasestorage.app",
  messagingSenderId: "68938107461",
  appId: "1:68938107461:web:578e606097d09a0afb712c",
  measurementId: "G-TJFSHPLNLJ"
};

// تهيئة تطبيق Firebase
const app = initializeApp(firebaseConfig);

// تهيئة وتصدير قاعدة بيانات Cloud Firestore للاستخدام في بقية ملفات المشروع
export const db = getFirestore(app);

// تهيئة وتصدير خدمة المصادقة (Authentication)
export const auth = getAuth(app);

export default app;
