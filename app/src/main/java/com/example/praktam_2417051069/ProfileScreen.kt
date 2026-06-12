package com.example.praktam_2417051069

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.praktam_2417051069.auth.AuthManager
import com.example.praktam_2417051069.ui.theme.BluePrimary
import com.example.praktam_2417051069.ui.theme.BlueSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }

    var userName by remember { mutableStateOf(authManager.getCurrentUserName()) }
    val userEmail = authManager.getCurrentUserEmail()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditPasswordDialog by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var editError by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Logout") },
            text = { Text("Apakah kamu yakin ingin keluar dari akun?") },
            confirmButton = {
                TextButton(onClick = {
                    authManager.logout()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }) { Text("Ya, Keluar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") } }
        )
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false; editError = "" },
            title = { Text("Edit Nama", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName, onValueChange = { newName = it },
                        label = { Text("Nama Baru") }, singleLine = true, shape = RoundedCornerShape(10.dp)
                    )
                    if (editError.isNotEmpty()) Text(editError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isBlank()) { editError = "Nama tidak boleh kosong" }
                    else {
                        authManager.updateName(newName.trim())
                        userName = newName.trim()
                        showEditNameDialog = false; newName = ""; editError = ""
                    }
                }) { Text("Simpan", fontWeight = FontWeight.Bold, color = BluePrimary) }
            },
            dismissButton = { TextButton(onClick = { showEditNameDialog = false; editError = "" }) { Text("Batal") } }
        )
    }

    if (showEditPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showEditPasswordDialog = false; editError = "" },
            title = { Text("Ganti Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = oldPassword, onValueChange = { oldPassword = it },
                        label = { Text("Password Lama") }, visualTransformation = PasswordVisualTransformation(),
                        singleLine = true, shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newPassword, onValueChange = { newPassword = it },
                        label = { Text("Password Baru") }, visualTransformation = PasswordVisualTransformation(),
                        singleLine = true, shape = RoundedCornerShape(10.dp)
                    )
                    if (editError.isNotEmpty()) Text(editError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when {
                        oldPassword.isBlank() || newPassword.isBlank() -> editError = "Semua field harus diisi"
                        newPassword.length < 6 -> editError = "Password baru minimal 6 karakter"
                        else -> {
                            val success = authManager.updatePassword(oldPassword, newPassword)
                            if (success) { showEditPasswordDialog = false; oldPassword = ""; newPassword = ""; editError = "" }
                            else editError = "Password lama salah"
                        }
                    }
                }) { Text("Simpan", fontWeight = FontWeight.Bold, color = BluePrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showEditPasswordDialog = false; editError = ""; oldPassword = ""; newPassword = "" }) { Text("Batal") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(220.dp)
                .background(Brush.verticalGradient(listOf(BluePrimary, BlueSecondary)))
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 36.dp, start = 8.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Column(
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(88.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(userName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(userEmail, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Informasi Akun", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Gray)

            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                Column {
                    ProfileMenuItem(icon = Icons.Default.Person, title = "Nama", subtitle = userName, onClick = { newName = userName; showEditNameDialog = true })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ProfileMenuItem(icon = Icons.Default.Email, title = "Email", subtitle = userEmail, onClick = null)
                }
            }

            Text("Keamanan", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Gray)

            Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
                ProfileMenuItem(icon = Icons.Default.Lock, title = "Ganti Password", subtitle = "Ubah password akunmu", onClick = { showEditPasswordDialog = true })
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, subtitle: String, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(BluePrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = BluePrimary, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, color = Color.Gray)
            Text(subtitle, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
        if (onClick != null) {
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}
