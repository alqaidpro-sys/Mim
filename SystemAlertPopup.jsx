// SystemAlertPopup.jsx - مكون النافذة المنبثقة اللحظية لتطبيق MiM Plus
// يتميز بتصميم عصري (Glassmorphism) مائل للفخامة والذكاء الاصطناعي مع دعم الاتجاه من اليمين إلى اليسار (RTL)

import React, { useState, useEffect } from "react";

/**
 * مكون النافذة المنبثقة اللحظية (SystemAlertPopup)
 * 
 * @param {Object} props
 * @param {Object} props.alertData - بيانات التنبيه المستلمة من Firestore
 * @param {boolean} props.isActive - حالة تنشيط التنبيه من خادم التحكم
 * @param {Function} [props.onUserDismiss] - حدث إضافي يتم استدعاؤه عند الإغلاق اليدوي من المستخدم
 */
export function SystemAlertPopup({ alertData, isActive, onUserDismiss }) {
  // للتحكم اليدوي المؤقت في حال سمح المشرف للمستخدم بإغلاق النافذة
  const [localDismiss, setLocalDismiss] = useState(false);

  // إعادة ضبط حالة الإغلاق المحلي عندما تتغير البيانات اللحظية أو يتم إرسال تنبيه جديد تماماً
  useEffect(() => {
    setLocalDismiss(false);
  }, [alertData?.id, alertData?.title, alertData?.message, isActive]);

  // إذا لم يكن المستند نشطاً، أو كان المستخدم قد أغلق النافذة يدوياً بموافقة المشرف، فلا يعرض شيئاً
  if (!isActive || !alertData || localDismiss) {
    return null;
  }

  const { title, message, type, buttonText, buttonUrl, allowDismiss } = alertData;

  // تحديد الرمز واللون بناءً على نوع التنبيه لضمان استجابة بصرية مذهلة
  let iconTheme = {
    emoji: "📢",
    gradient: "linear-gradient(135deg, #00F2FE 0%, #4FACFE 100%)", // Teal/Blue Info
    borderColor: "#00F2FE"
  };

  if (type === "warning") {
    iconTheme = {
      emoji: "⚠️",
      gradient: "linear-gradient(135deg, #FF9F43 0%, #FF5252 100%)", // Orange/Red warning
      borderColor: "#FF9F43"
    };
  } else if (type === "success") {
    iconTheme = {
      emoji: "🎉",
      gradient: "linear-gradient(135deg, #10B981 0%, #059669 100%)", // Emerald Green success
      borderColor: "#10B981"
    };
  } else if (type === "danger" || type === "error") {
    iconTheme = {
      emoji: "🛑",
      gradient: "linear-gradient(135deg, #EF4444 0%, #B91C1C 100%)", // Red high urgency
      borderColor: "#EF4444"
    };
  } else if (type === "gift") {
    iconTheme = {
      emoji: "🎁",
      gradient: "linear-gradient(135deg, #FBBF24 0%, #D97706 100%)", // Golden Gold prize
      borderColor: "#FBBF24"
    };
  }

  const handleClose = () => {
    if (allowDismiss) {
      setLocalDismiss(true);
      if (onUserDismiss) onUserDismiss();
    }
  };

  return (
    <div style={styles.backdrop}>
      <div style={{ ...styles.modalContainer, border: `1px solid ${iconTheme.borderColor}` }}>
        
        {/* شريط الإغلاق العلوي (يظهر فقط إذا سمح المشرف للمستخدم بالإغلاق) */}
        {allowDismiss && (
          <button 
            onClick={handleClose} 
            style={styles.closeButton}
            title="إغلاق التنبيه"
          >
            ✕
          </button>
        )}

        <div style={styles.contentWrapper}>
          {/* وعاء الرمز اللمعان الخلفي */}
          <div style={{ ...styles.iconWrapper, background: iconTheme.gradient }}>
            <span style={styles.iconEmoji}>{iconTheme.emoji}</span>
          </div>

          <h2 style={styles.title}>{title}</h2>
          
          <p style={styles.message}>{message}</p>

          {/* الأزرار والإجراءات التفاعلية */}
          <div style={styles.actionsContainer}>
            {buttonText && buttonUrl && (
              <a 
                href={buttonUrl} 
                target="_blank" 
                rel="noopener noreferrer" 
                style={{ ...styles.primaryBtn, background: iconTheme.gradient }}
              >
                {buttonText}
              </a>
            )}
            
            {allowDismiss && (
              <button onClick={handleClose} style={styles.secondaryBtn}>
                تخطي التنبيه
              </button>
            )}
          </div>
        </div>

        {/* مؤشر البث اللحظي السفلي */}
        <div style={styles.liveIndicatorContainer}>
          <span style={styles.pulsePoint}></span>
          <span style={styles.indicatorText}>تنبيه مباشر ومحدث لحظياً من الخادم</span>
        </div>

      </div>
    </div>
  );
}

// تصميم مخصص أنيق ومنسق ومتماشي مع شاشات الجوال والحاسوب (Responsive Layout CSS-in-JS)
const styles = {
  backdrop: {
    position: "fixed",
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: "rgba(3, 8, 8, 0.88)",
    backdropFilter: "blur(12px)",
    WebkitBackdropFilter: "blur(12px)",
    zIndex: 999999,
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    padding: "20px",
    direction: "rtl" as const,
    animation: "fadeIn 0.3s ease-out"
  },
  modalContainer: {
    backgroundColor: "#0D1311",
    boxShadow: "0 25px 50px -12px rgba(0, 0, 0, 0.7)",
    borderRadius: "24px",
    width: "100%",
    maxWidth: "460px",
    position: "relative" as const,
    overflow: "hidden",
    fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
    paddingTop: "24px",
    display: "flex",
    flexDirection: "column" as const,
    animation: "slideUp 0.35s cubic-bezier(0.16, 1, 0.3, 1)"
  },
  closeButton: {
    position: "absolute" as const,
    top: "16px",
    left: "16px",
    background: "rgba(255, 255, 255, 0.06)",
    color: "#E2E8F0",
    border: "none",
    width: "32px",
    height: "32px",
    borderRadius: "50%",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "14px",
    fontWeight: "bold",
    transition: "background 0.2s ease-in-out, transform 0.2s ease-in-out",
    boxShadow: "0 2px 4px rgba(0,0,0,0.2)"
  },
  contentWrapper: {
    padding: "24px 32px 16px 32px",
    display: "flex",
    flexDirection: "column" as const,
    alignItems: "center",
    textAlign: "center" as const
  },
  iconWrapper: {
    width: "72px",
    height: "72px",
    borderRadius: "20px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    boxShadow: "0 10px 20px -5px rgba(0, 0, 0, 0.3)",
    marginBottom: "18px",
    animation: "bounceIn 0.5s ease"
  },
  iconEmoji: {
    fontSize: "36px"
  },
  title: {
    fontSize: "22px",
    fontWeight: "bold" as const,
    color: "#FFFFFF",
    margin: "0 0 10px 0",
    lineHeight: "1.4",
    letterSpacing: "-0.5px"
  },
  message: {
    fontSize: "14px",
    color: "#94A3B8",
    margin: "0 0 24px 0",
    lineHeight: "1.7",
    fontWeight: "400"
  },
  actionsContainer: {
    display: "flex",
    flexDirection: "column" as const,
    width: "100%",
    gap: "10px"
  },
  primaryBtn: {
    padding: "12px 24px",
    fontSize: "14px",
    fontWeight: "bold" as const,
    color: "#030808",
    borderRadius: "14px",
    border: "none",
    cursor: "pointer",
    textAlign: "center" as const,
    textDecoration: "none",
    boxShadow: "0 4px 15px rgba(0,0,0,0.15)",
    transition: "transform 0.15s ease",
    display: "block"
  },
  secondaryBtn: {
    padding: "11px 24px",
    fontSize: "13px",
    fontWeight: "500",
    color: "#94A3B8",
    backgroundColor: "rgba(255, 255, 255, 0.04)",
    borderRadius: "14px",
    border: "1px solid rgba(255, 255, 255, 0.08)",
    cursor: "pointer",
    transition: "all 0.2s ease"
  },
  liveIndicatorContainer: {
    backgroundColor: "rgba(255,255,255,0.02)",
    borderTop: "1px solid rgba(255, 255, 255, 0.05)",
    padding: "12px 24px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "8px"
  },
  pulsePoint: {
    width: "7px",
    height: "7px",
    backgroundColor: "#10B981",
    borderRadius: "50%",
    display: "inline-block",
    animation: "pulseGlow 1.2s infinite alternate"
  },
  indicatorText: {
    color: "#475569",
    fontSize: "10px",
    fontWeight: "bold" as const,
    letterSpacing: "0.2px"
  }
};

export default SystemAlertPopup;
