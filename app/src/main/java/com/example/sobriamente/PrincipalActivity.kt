package com.example.sobriamente

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Shield
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sobriamente.ui.theme.SobriamenteTheme
import java.util.concurrent.TimeUnit
import java.io.File
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

// --- 1. MODELO DE DATOS ---
data class SobrietyArticle(
    val id: Int,
    val title: String,
    val description: String,
    val content: String,
    val category: String,
    val icon: ImageVector
)

// Datos estáticos
val sampleSobrietyArticles = listOf(
    SobrietyArticle(
        1,
        "Manejo de Ansiedad",
        "Técnica de respiración 4-7-8.",
        "La ansiedad es una respuesta natural al estrés, pero puede volverse abrumadora en el proceso de recuperación. \n\nUna de las técnicas más efectivas es la respiración 4-7-8:\n\n1. Inhala por la nariz contando hasta 4.\n2. Mantén el aire contando hasta 7.\n3. Exhala por la boca contando hasta 8.\n\nRepite este ciclo 4 veces. Esto obliga a tu sistema nervioso a cambiar de 'lucha o huida' a un estado de calma.",
        "Salud",
        Icons.Default.Psychology
    ),
    SobrietyArticle(
        2,
        "Higiene del Sueño",
        "Cómo recuperar tu ciclo circadiano.",
        "El alcohol y las sustancias alteran profundamente la arquitectura del sueño. Es normal tener insomnio las primeras semanas.\n\nConsejos clave:\n- Acuéstate y levántate a la misma hora todos los días.\n- Evita pantallas 1 hora antes de dormir (la luz azul bloquea la melatonina).\n- Si no puedes dormir en 20 minutos, levántate y lee un libro (físico) hasta que tengas sueño.\n- Mantén tu habitación fresca y oscura.",
        "Salud",
        Icons.Default.Bedtime
    ),
    SobrietyArticle(
        3,
        "Nutrición y Cerebro",
        "Alimentos que ayudan a la recuperación.",
        "Tu cerebro necesita materia prima para reparar los neurotransmisores dañados.\n\n- Proteínas: Contienen aminoácidos esenciales para crear dopamina y serotonina.\n- Omega-3: Presente en nueces y pescado, ayuda a reducir la inflamación cerebral.\n- Agua: La hidratación es fundamental para eliminar toxinas.\n- Evita el exceso de azúcar, ya que provoca picos de energía seguidos de caídas que pueden simular la ansiedad.",
        "Salud",
        Icons.Default.LocalCafe
    ),
    SobrietyArticle(
        4,
        "Meditación Básica",
        "5 minutos de mindfulness.",
        "No necesitas ser un monje para meditar. El mindfulness es simplemente 'prestar atención a propósito'.\n\nEjercicio rápido:\nSiéntate cómodo. Cierra los ojos. Pon tu atención en cómo se siente el aire entrando y saliendo de tu nariz. Cuando tu mente se distraiga (y lo hará), amablemente regresa tu atención a la respiración. Eso es todo. Es el acto de regresar lo que entrena tu cerebro.",
        "Bienestar",
        Icons.Default.Favorite
    ),
    SobrietyArticle(
        5,
        "Identificar Detonantes",
        "Conoce qué situaciones evitar.",
        "Un detonante (trigger) es cualquier estímulo que provoca el deseo de consumo.\n\nTipos comunes:\n- Personas: Amigos con los que solías consumir.\n- Lugares: Bares, ciertas calles o incluso una habitación específica.\n- Emociones: El acrónimo HALT en inglés nos recuerda no estar demasiado: Hungry (Hambriento), Angry (Enojado), Lonely (Solo) o Tired (Cansado).",
        "Prevención",
        Icons.Default.Warning
    )
)

// --- ACTIVIDAD PRINCIPAL ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Programar la notificación diaria
        scheduleDailyNotification(this)

        setContent {
            SobriamenteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RequestNotificationPermission()
                    AppNavigation()
                }
            }
        }
    }

    private fun scheduleDailyNotification(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailyMotivationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

// Componente para pedir permisos
@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { }
        )
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(permission)
            }
        }
    }
}

// --- NAVEGACIÓN ---
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var showPanic by remember { mutableStateOf(false) }
    var hasStarted by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            if (!showPanic) {
                if (currentRoute?.startsWith("article_detail") == false) {
                    AppHeader(showPanic = showPanic, onPanicToggle = { showPanic = !showPanic })
                }
            }
        },
        bottomBar = {
            if (!showPanic && hasStarted && currentRoute?.startsWith("article_detail") == false) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    if (!hasStarted) {
                        WelcomeScreen(onStart = { hasStarted = true })
                    } else {
                        DashboardScreen()
                    }
                }
                composable("education") {
                    EducationScreen(onArticleClick = { articleId ->
                        navController.navigate("article_detail/$articleId")
                    })
                }
                composable(
                    route = "article_detail/{articleId}",
                    arguments = listOf(navArgument("articleId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val articleId = backStackEntry.arguments?.getInt("articleId")
                    ArticleDetailScreen(articleId = articleId, onBack = { navController.popBackStack() })
                }
                composable("settings") { SettingsScreen() }
            }

            AnimatedVisibility(
                visible = showPanic,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                PanicOverlay(onClose = { showPanic = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(showPanic: Boolean, onPanicToggle: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sobriamente", style = MaterialTheme.typography.titleLarge)
            }
        },
        actions = {
            Button(
                onClick = onPanicToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showPanic) Color.Red else MaterialTheme.colorScheme.errorContainer,
                    contentColor = if (showPanic) Color.White else MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showPanic) "Cerrar" else "Ayuda")
            }
        }
    )
}

@Composable
fun BottomNavigationBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Inicio") },
            selected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
            label = { Text("Aprender") },
            selected = currentRoute == "education",
            onClick = { onNavigate("education") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Ajustes") },
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") }
        )
    }
}

// --- PANTALLAS ---

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido a tu nueva vida", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStart) {
            Text("Comenzar mi viaje hoy")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("sobriety_prefs", Context.MODE_PRIVATE) }

    var startDateMillis by remember { mutableStateOf(prefs.getLong("start_date", 0L)) }
    var showDatePicker by remember { mutableStateOf(false) }

    fun calculateDays(start: Long): Long {
        if (start == 0L) return 0
        val now = System.currentTimeMillis()
        val diff = now - start
        if (diff < 0) return 0
        return TimeUnit.MILLISECONDS.toDays(diff)
    }

    val daysCount = calculateDays(startDateMillis)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (startDateMillis == 0L) "Toca el círculo para empezar" else "Tu progreso real",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(30.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.clickable { showDatePicker = true }
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(260.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 18.dp,
            )
            val progress = daysCount.toFloat() / 90f
            CircularProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.size(260.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 18.dp,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = daysCount.toString(),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text("DÍAS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            val moneySaved = daysCount * 150
            StatCard(label = "Ahorrado", value = "$$moneySaved", icon = Icons.Default.Savings)
            val health = if(daysCount > 30) "100%" else "${(daysCount * 3).coerceAtMost(100)}%"
            StatCard(label = "Salud", value = health, icon = Icons.Default.Favorite)
        }

        Spacer(modifier = Modifier.height(30.dp))
        AchievementsSection(daysSober = daysCount)
        Spacer(modifier = Modifier.height(20.dp))
        TransformationSection()
        Spacer(modifier = Modifier.height(20.dp))
        MotivationSection()
        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selection ->
                        startDateMillis = selection
                        prefs.edit().putLong("start_date", selection).apply()
                    }
                    showDatePicker = false
                }) { Text("Guardar fecha") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.size(width = 150.dp, height = 100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("sobriety_prefs", Context.MODE_PRIVATE) }

    var contactName by remember { mutableStateOf(prefs.getString("contact_name", "") ?: "") }
    var contactNumber by remember { mutableStateOf(prefs.getString("contact_number", "") ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Contacto de Emergencia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Este número se usará en el Botón de Pánico.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Nombre (ej. Padrino, Mamá)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text("Número de Teléfono") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        prefs.edit()
                            .putString("contact_name", contactName)
                            .putString("contact_number", contactNumber)
                            .apply()
                        Toast.makeText(context, "Contacto guardado", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Cambios")
                }
            }
        }
    }
}

@Composable
fun PanicOverlay(onClose: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("sobriety_prefs", Context.MODE_PRIVATE) }

    val savedName = prefs.getString("contact_name", "Contacto de confianza") ?: "Contacto"
    val savedNumber = prefs.getString("contact_number", "") ?: ""

    val gradient = Brush.verticalGradient(listOf(Color(0xFF8B0000), Color(0xFF121212)))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("RESPIRA PROFUNDO", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Esto es solo un momento. No arruines tu progreso por una emoción temporal.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Ya pasó, estoy mejor", color = Color(0xFF8B0000), style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                if (savedNumber.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$savedNumber")
                    }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Configura un número en Ajustes primero", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Call, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Llamar a $savedName")
        }
    }
}

@Composable
fun MotivationSection() {
    val quotes = listOf("Un día a la vez.", "La recuperación es un proceso.", "Eres más fuerte de lo que crees.")
    var currentQuote by remember { mutableStateOf(quotes.random()) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.FormatQuote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("\"$currentQuote\"", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { currentQuote = quotes.random() }) { Text("Nueva Frase") }
        }
    }
}

@Composable
fun AchievementsSection(daysSober: Long) {
    data class Milestone(val days: Int, val title: String, val description: String)
    val milestones = listOf(
        Milestone(1, "Primer Paso", "24 horas de claridad."),
        Milestone(7, "Una Semana", "Primera gran victoria."),
        Milestone(30, "Un Mes", "Claridad mental renovada.")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logros", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))
        milestones.forEach { milestone ->
            val isUnlocked = daysSober >= milestone.days
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.Star else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isUnlocked) MaterialTheme.colorScheme.secondary else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(milestone.title, style = MaterialTheme.typography.titleMedium)
                        Text(milestone.description, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun TransformationSection() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("sobriety_prefs", Context.MODE_PRIVATE) }
    
    var photo1Uri by remember { mutableStateOf(prefs.getString("photo1_uri", null)?.let { Uri.parse(it) }) }
    var photo2Uri by remember { mutableStateOf(prefs.getString("photo2_uri", null)?.let { Uri.parse(it) }) }
    
    var currentPhotoTarget by remember { mutableStateOf(1) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempUri != null) {
            if (currentPhotoTarget == 1) {
                photo1Uri = tempUri
                prefs.edit().putString("photo1_uri", tempUri.toString()).apply()
            } else {
                photo2Uri = tempUri
                prefs.edit().putString("photo2_uri", tempUri.toString()).apply()
            }
        }
    }

    fun takePhoto(target: Int) {
        currentPhotoTarget = target
        val file = File(context.cacheDir, "images").apply { mkdirs() }
        val newFile = File(file, "photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", newFile)
        tempUri = uri
        cameraLauncher.launch(uri)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Mi Transformación", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            PhotoCard(title = "Día 1", uri = photo1Uri, onClick = { takePhoto(1) }, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            PhotoCard(title = "Hoy", uri = photo2Uri, onClick = { takePhoto(2) }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PhotoCard(title: String, uri: Uri?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = title,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Añadir foto", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun EducationScreen(onArticleClick: (Int) -> Unit) {
    val categories = listOf("Todos", "Salud", "Bienestar", "Prevención")
    var selectedCategory by remember { mutableStateOf("Todos") }

    val filteredArticles = if (selectedCategory == "Todos") {
        sampleSobrietyArticles
    } else {
        sampleSobrietyArticles.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Centro Educativo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredArticles) { article ->
                ArticleCard(article = article, onClick = { onArticleClick(article.id) })
            }
        }
    }
}

@Composable
fun ArticleCard(article: SobrietyArticle, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(imageVector = article.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            SuggestionChip(
                onClick = { /* Solo etiqueta visual */ },
                label = { Text(article.category) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(articleId: Int?, onBack: () -> Unit) {
    val article = sampleSobrietyArticles.find { it.id == articleId }

    if (article == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Artículo no encontrado")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article.category) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Icon(
                imageVector = article.icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.description,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = article.content,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}