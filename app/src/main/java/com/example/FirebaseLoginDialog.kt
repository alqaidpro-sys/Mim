package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseLoginDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = MyFirebaseManager.getAuth()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, BorderColor, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1311)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Exit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color.White.copy(0.05f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "اغلاق",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = if (isSignUp) "إنشاء حساب جديد ✨" else "تسجيل دخول المشرف 🔐",
                        color = TextPri,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // App Visual Logo/Avatar check
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(0.03f), CircleShape)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSignUp) "👤" else "🔑",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isSignUp) 
                        "قم بإنشاء حساب لتخصيص تجربتك والوصول للميزات الإضافية"
                    else 
                        "سجل الدخول بحساب المشرف للتعديل المباشر وإضافة نتائج وبث المباريات",
                    color = TextSec,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                // Info for admins
                if (!isSignUp) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TealDeepColor.copy(0.2f), RoundedCornerShape(8.dp))
                            .border(0.5.dp, TealColor.copy(0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "💡 للتجربة وملاحظة التغيير:\nاستخدم بريدك alqaidpro@gmail.com لظهور زر ولوحة المشرف بالكامل بمجرد تسجيل الدخول.",
                            color = TealLightColor,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red.copy(0.08f), RoundedCornerShape(8.dp))
                            .border(0.5.dp, Color.Red.copy(0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("البريد الإلكتروني", color = TextSec) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPri,
                        unfocusedTextColor = TextPri,
                        focusedBorderColor = TealLightColor,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = CardColor,
                        unfocusedContainerColor = CardColor
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("كلمة المرور", color = TextSec) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPri,
                        unfocusedTextColor = TextPri,
                        focusedBorderColor = TealLightColor,
                        unfocusedBorderColor = BorderColor,
                        focusedContainerColor = CardColor,
                        unfocusedContainerColor = CardColor
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Submit Button
                Button(
                    onClick = {
                        val authInstance = auth
                        if (authInstance == null) {
                            errorMessage = "خطأ: لم يتم تهيئة فيربيز بشكل كامل بعد."
                            return@Button
                        }
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "الرجاء كتابة البريد وكلمة المرور."
                            return@Button
                        }
                        if (password.length < 6) {
                            errorMessage = "يجب ألا تقل كلمة المرور عن 6 أحرف."
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null

                        if (isSignUp) {
                            authInstance.createUserWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        onLoginSuccess()
                                    } else {
                                        val exMsg = task.exception?.localizedMessage ?: "فشل تسجيل حساب جديد."
                                        errorMessage = when {
                                            exMsg.contains("email address is already in use") -> "البريد الإلكتروني مسجل بالفعل لمستخدم آخر."
                                            exMsg.contains("badly formatted") -> "صيغة البريد الإلكتروني غير صحيحة."
                                            else -> exMsg
                                        }
                                    }
                                }
                        } else {
                            authInstance.signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        onLoginSuccess()
                                    } else {
                                        val exMsg = task.exception?.localizedMessage ?: "فشل تسجيل الدخول."
                                        errorMessage = when {
                                            exMsg.contains("no user record") || exMsg.contains("wrong-password") || exMsg.contains("invalid-credential") -> 
                                                "البريد الإلكتروني أو كلمة المرور غير صحيحة"
                                            else -> exMsg
                                        }
                                    }
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealLightColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isSignUp) "إنشاء حساب الآن 🚀" else "دخول المشرف 🔓",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Account Prefill for testing Admin
                if (!isSignUp) {
                    TextButton(
                        onClick = {
                            email = "alqaidpro@gmail.com"
                            password = "password123"
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🔹 تعبئة تلقائية لبريد المشرف (للتبسيط)",
                            color = TealLightColor.copy(0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Switch SignUp / Login
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            isSignUp = !isSignUp
                            errorMessage = null
                        }
                    ) {
                        Text(
                            text = if (isSignUp) "لديك حساب بالفعل؟ سجل دخولك" else "ليس لديك حساب؟ أنشئ حساباً جديداً",
                            color = TealColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
