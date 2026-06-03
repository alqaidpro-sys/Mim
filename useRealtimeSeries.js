// useRealtimeSeries.js - مخصّص (Custom Hook) لجلب بيانات المسلسلات لحظياً من Firestore
// الإصدار المستخدم: React 16.8+ & Firebase v9+ Modular SDK

import { useState, useEffect } from "react";
import { collection, query, orderBy, onSnapshot } from "firebase/firestore";
import { db } from "./firebase"; // استيراد قاعدة البيانات المهيأة مسبقاً

/**
 * Custom Hook لجلب المسلسلات في الوقت الفعلي (Real-time) من Firestore مرتبة تنازلياً حسب تاريخ الترتيب.
 * @returns {Object} { series, isLoading, error }
 */
export function useRealtimeSeries() {
  const [series, setSeries] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    setIsLoading(true);
    let unsubscribe = () => {};

    try {
      // 1. تحديد المجموعة (Collection) المستهدفة: "series"
      const seriesCollectionRef = collection(db, "series");

      // 2. إنشاء الاستعلام مع فرز البيانات تنازلياً (desc) بناءً على الحقل "sorting_date"
      // لكي يظهر المسلسل الأحدث في المقدمة دائماً تلقائياً من جانب خادم Firestore
      const seriesQuery = query(
        seriesCollectionRef,
        orderBy("sorting_date", "desc")
      );

      // 3. بدء الاستماع اللحظي (Real-time listener) للتغييرات باستخدام onSnapshot
      unsubscribe = onSnapshot(
        seriesQuery,
        (snapshot) => {
          const fetchedSeries = [];
          
          snapshot.forEach((doc) => {
            // جلب البيانات مع دمج معرف المستند (id)
            fetchedSeries.push({
              id: doc.id,
              ...doc.data(),
            });
          });

          // تحديث الحالة بالبيانات الجديدة المرتبة
          setSeries(fetchedSeries);
          setIsLoading(false);
          setError(null);
        },
        (err) => {
          console.error("حدث خطأ أثناء الاتصال بقاعدة البياناتFirestore:", err);
          setError(err.message || "فشل جلب البيانات اللحظية.");
          setIsLoading(false);
        }
      );
    } catch (err) {
      console.error("خطأ في إعداد مستمع Firestore:", err);
      setError(err.message || "تعذر إعداد الاستماع اللحظي.");
      setIsLoading(false);
    }

    // 4. دالة التنظيف (Cleanup function) للتأكد من إيقاف المستمع (Unsubscribe) 
    // عند تدمير المكون (Unmount) لمنع حدوث تسريب في الذاكرة (Memory Leak)
    return () => {
      unsubscribe();
    };
  }, []);

  return { series, isLoading, error };
}

export default useRealtimeSeries;
