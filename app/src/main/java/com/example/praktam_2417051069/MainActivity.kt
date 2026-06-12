package com.example.praktam_2417051069

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.praktam_2417051069.auth.AuthManager
import com.example.praktam_2417051069.data.model.LocalTask
import com.example.praktam_2417051069.data.model.StudentPlanner
import com.example.praktam_2417051069.data.repository.LocalTaskRepository
import com.example.praktam_2417051069.data.repository.Studentrepository
import com.example.praktam_2417051069.notification.NotificationHelper
import com.example.praktam_2417051069.ui.theme.*
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createChannel(this)
        setContent {
            PrakTAM_2417051069Theme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val localRepo = remember { LocalTaskRepository(context) }
    var apiTips by remember { mutableStateOf<List<StudentPlanner>>(emptyList()) }
    var localTasks by remember { mutableStateOf<List<LocalTask>>(localRepo.getAllTasks()) }
    val startDestination = if (authManager.isLoggedIn()) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") {
            HomeScreen(
                navController = navController,
                localRepo = localRepo,
                localTasks = localTasks,
                onLocalTasksChanged = { localTasks = localRepo.getAllTasks() },
                onTipsLoaded = { apiTips = it }
            )
        }
        composable("tips_detail/{judul}") { backStackEntry ->
            val judul = backStackEntry.arguments?.getString("judul")
            val tips = apiTips.find { it.nama == judul }
            if (tips != null) TipsDetailScreen(tips, navController)
        }
        composable("detail_local/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val task = localTasks.find { it.id == id }
            if (task != null) {
                DetailLocalScreen(
                    task = task,
                    onToggleDone = {
                        localRepo.toggleDone(task.id)
                        localTasks = localRepo.getAllTasks()
                    },
                    navController = navController
                )
            }
        }
        composable("profile") { ProfileScreen(navController) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    localRepo: LocalTaskRepository,
    localTasks: List<LocalTask>,
    onLocalTasksChanged: () -> Unit,
    onTipsLoaded: (List<StudentPlanner>) -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val apiRepo = remember { Studentrepository() }

    var apiTips by remember { mutableStateOf<List<StudentPlanner>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        isLoading = true
        val result = apiRepo.getStudents()
        apiTips = result
        onTipsLoaded(result)
        onLocalTasksChanged()
        isLoading = false
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { task ->
                localRepo.addTask(task)
                onLocalTasksChanged()
                // Jadwalkan alarm otomatis di hari deadline jam 08:00
                NotificationHelper.scheduleDeadlineAlarm(
                    context, task.judul, task.deadline, task.id.hashCode()
                )
                showAddDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Student Planner", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Halo, ${authManager.getCurrentUserName()} 👋",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                authManager.getCurrentUserName().firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BluePrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Tugas", tint = Color.White)
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BluePrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item { SummaryBanner(localTasks) }

                // Section Tips & Motivasi dari API — relevan untuk semua user
                if (apiTips.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Tips & Motivasi Belajar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Konten harian untuk bantu kamu tetap produktif",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(apiTips) { tips ->
                                    TipsCard(tips) {
                                        navController.navigate("tips_detail/${tips.nama}")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FormatListBulleted,
                            null,
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tugas Saya", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        val doneCount = localTasks.count { it.isDone }
                        Text("$doneCount/${localTasks.size} selesai", fontSize = 12.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (localTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Assignment,
                                    null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Belum ada tugas", color = Color.Gray, fontSize = 14.sp)
                                Text(
                                    "Tap + untuk menambah tugas baru",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    items(localTasks, key = { it.id }) { task ->
                        LocalTaskItem(
                            task = task,
                            onToggleDone = {
                                localRepo.toggleDone(task.id)
                                onLocalTasksChanged()
                            },
                            onDelete = {
                                // Batalkan alarm saat tugas dihapus
                                NotificationHelper.cancelDeadlineAlarm(context, task.id.hashCode())
                                localRepo.deleteTask(task.id)
                                onLocalTasksChanged()
                            },
                            onRemind = {
                                // Tombol "Set Pengingat" → notifikasi langsung sebagai konfirmasi
                                NotificationHelper.showDeadlineReminder(
                                    context, task.judul, task.deadline, task.id.hashCode()
                                )
                            },
                            onClick = { navController.navigate("detail_local/${task.id}") }
                        )
                    }
                }
            }
        }
    }
}

// Card untuk Tips — tidak ada deadline, pakai kategori sebagai label
@Composable
fun TipsCard(tips: StudentPlanner, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(180.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = tips.imageUrl,
                contentDescription = tips.nama,
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(10.dp)) {
                // Label kategori (Motivasi / Produktivitas / dll)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BluePrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(tips.deadline, fontSize = 10.sp, color = BluePrimary) // field deadline dipakai untuk kategori
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    tips.nama,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SummaryBanner(tasks: List<LocalTask>) {
    val total = tasks.size
    val done = tasks.count { it.isDone }
    val pending = total - done

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(BluePrimary, BlueSecondary)))
            .padding(20.dp)
    ) {
        Column {
            Text("Ringkasan Tugasmu", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                SummaryItem("Total", "$total")
                SummaryItem("Selesai", "$done")
                SummaryItem("Pending", "$pending")
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}

@Composable
fun LocalTaskItem(
    task: LocalTask,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
    onRemind: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Tugas?") },
            text = { Text("\"${task.judul}\" akan dihapus permanen.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isDone) GreenDone.copy(alpha = 0.05f) else Color.White
        )
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onToggleDone() },
                colors = CheckboxDefaults.colors(checkedColor = GreenDone)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.judul,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isDone) Color.Gray else Color.Black
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = OrangeDeadline, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(task.deadline, fontSize = 12.sp, color = OrangeDeadline)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BluePrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(task.kategori, fontSize = 10.sp, color = BluePrimary)
                    }
                }
                if (task.deskripsi.isNotBlank()) {
                    Text(
                        task.deskripsi,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onRemind, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Notifications, null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (LocalTask) -> Unit) {
    val context = LocalContext.current
    var judul by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("Tugas") }
    var error by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val kategoriList = listOf("Tugas", "Ujian", "Proyek", "Kegiatan", "Lainnya")

    val calendar = Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            deadline = "%02d/%02d/%04d".format(day, month + 1, year)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddTask, null, tint = BluePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Tugas Baru", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = judul, onValueChange = { judul = it },
                    label = { Text("Judul Tugas") },
                    leadingIcon = { Icon(Icons.Default.Title, null, tint = BluePrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        focusedLabelColor = BluePrimary
                    )
                )
                OutlinedTextField(
                    value = deskripsi, onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi (opsional)") },
                    leadingIcon = { Icon(Icons.Default.Notes, null, tint = BluePrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        focusedLabelColor = BluePrimary
                    )
                )
                OutlinedTextField(
                    value = deadline, onValueChange = {},
                    label = { Text("Deadline") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = BluePrimary) },
                    modifier = Modifier.fillMaxWidth().clickable { datePicker.show() },
                    shape = RoundedCornerShape(10.dp),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = BluePrimary.copy(alpha = 0.5f),
                        disabledLabelColor = Color.Gray,
                        disabledTextColor = Color.Black,
                        disabledLeadingIconColor = BluePrimary
                    )
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = kategori, onValueChange = {}, readOnly = true,
                        label = { Text("Kategori") },
                        leadingIcon = { Icon(Icons.Default.Category, null, tint = BluePrimary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BluePrimary,
                            focusedLabelColor = BluePrimary
                        )
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        kategoriList.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = { kategori = item; expanded = false }
                            )
                        }
                    }
                }
                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        judul.isBlank() -> error = "Judul tidak boleh kosong"
                        deadline.isBlank() -> error = "Pilih deadline terlebih dahulu"
                        else -> onAdd(
                            LocalTask(
                                id = System.currentTimeMillis().toString(),
                                judul = judul.trim(),
                                deskripsi = deskripsi.trim(),
                                deadline = deadline,
                                kategori = kategori
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Tambah") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
        shape = RoundedCornerShape(20.dp)
    )
}

// Detail screen untuk Tips — hanya baca, tidak ada Tandai Selesai
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsDetailScreen(tips: StudentPlanner, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tips.nama, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                AsyncImage(
                    model = tips.imageUrl,
                    contentDescription = tips.nama,
                    placeholder = painterResource(id = R.drawable.ic_launcher_background),
                    error = painterResource(id = R.drawable.ic_launcher_background),
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentScale = ContentScale.Crop
                )
            }
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Label kategori
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BluePrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(tips.deadline, fontSize = 12.sp, color = BluePrimary, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(tips.nama, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BlueLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Isi Tips", fontWeight = FontWeight.Bold, color = BluePrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(tips.deskripsi, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 22.sp)
                        }
                    }
                }
            }
        }
    }
}

// Detail screen untuk Tugas Saya
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailLocalScreen(task: LocalTask, onToggleDone: () -> Unit, navController: NavController) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(task.judul, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BluePrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BlueLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = OrangeDeadline, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Deadline: ${task.deadline}", color = OrangeDeadline, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Category, null, tint = BluePrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kategori: ${task.kategori}", color = BluePrimary)
                    }
                    if (task.deskripsi.isNotBlank()) {
                        HorizontalDivider()
                        Text("Deskripsi", fontWeight = FontWeight.Bold, color = BluePrimary)
                        Text(task.deskripsi, fontSize = 14.sp, color = Color.DarkGray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    onToggleDone()
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (!task.isDone) "Tugas ditandai selesai ✅" else "Tugas dibuka kembali"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (task.isDone) GreenDone else BluePrimary
                )
            ) {
                Icon(
                    if (task.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (task.isDone) "Selesai ✓" else "Tandai Selesai",
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    NotificationHelper.showDeadlineReminder(
                        context, task.judul, task.deadline, task.id.hashCode()
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Notifications, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Pengingat")
            }
        }
    }
}