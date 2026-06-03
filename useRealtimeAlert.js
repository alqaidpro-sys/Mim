// useRealtimeAlert.js - نظام الاستماع اللحظي لإشعارات وتنبيهات النظام في MiM Plus
// الإصدار المستخدم: React 16.8+ & Firebase v9+ Modular SDK

import { useState, useEffect } from "react";
import { doc, onSnapshot } from "firebase/firestore";
import { db } from "./firebase"; // استيراد قاعدة البيانات المهيأة مسبقاً

/**
 * مخصص (Custom Hook) للاستماع اللحظي لـ "system_alert" من Firestore.
 * بمجرد تغيير الحالة من لوحة التحكم، سينعكس التأثير فورياً لدى جميع المستخدمين النشطين.
 * 
 * @returns {Object} { alertData, isActive, isLoading, error }
 */
export function useRealtimeAlert() {
  const [alertData, setAlertData] = useState(null);
  const [isActive, setIsActive] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setIsLoading(true);
    let unsubscribe = () => {};

    try {
      // نقوم بالاستماع لمستند "system_alert" داخل مجموعة الإعدادات "settings"
      // ملاحظة: يمكنك تغيير اسم المجموعة "settings" لتطابق هيكل قاعدة البيانات الخاصة بك في Firestore
      const docRef = doc(db, "settings", "system_alert");

      unsubscribe = onSnapshot(
        docRef,
        (docSnap) => {
          if (docSnap.exists()) {
            const data = docSnap.data();
            
            setAlertData({
              id: docSnap.id,
              title: data.title || "تنبيه من الإدارة",
              message: data.message || "",
              isActive: !!data.isActive,
              buttonText: data.buttonText || "",
              buttonUrl: data.buttonUrl || "",
              type: data.type || "warning", // يدعم: warning, info, success, danger
              allowDismiss: data.allowDismiss !== undefined ? data.allowDismiss : true,
            });
            
            // تحديث حالة النشاط لحظياً
            setIsActive(!!data.isActive);
          } else {
            // في حال تم حذف المستند أو لم يكن موجوداً
            setAlertData(null);
            setIsActive(false);
          }
          setIsLoading(false);
          setError(null);
        },
        (err) => {
          console.error("حدث خطأ أثناء جلب التنبيه اللحظي من Firestore:", err);
          setError(err.message || "فشلت عملية الاتصال اللحظي.");
          setIsLoading(false);
        }
      );
    } catch (err) {
      console.error("خطأ غير متوقع في إعداد مستمع التنبيهات:", err);
      setError(err.message || "تعذر تفعيل مستمع التنبيهات.");
      setIsLoading(false);
    }

    // دالة التنظيف (Cleanup) لمنع تسريب الذاكرة عند مغادرة الصفحة أو إغلاق المكون
    return () => {
      unsubscribe();
    };
  }, []);

  return { alertData, isActive, isLoading, error };
}

export default useRealtimeAlert;
