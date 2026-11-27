package com.fitcast.cuacagaya.ui.onboarding

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val titles = listOf(
        "Cek Cuaca Real-Time",
        "Lemari Digital Cerdas",
        "Mix & Match Anti Ribet"
    )
    val descriptions = listOf(
        "FitCast membaca suhu, kelembapan, dan peluang hujan di lokasimu agar tidak salah kostum atau lupa payung.",
        "Foto bajumu sekali — AI ML Kit otomatis menghapus latar belakang dan mendeteksi warna pakaianmu.",
        "Dapatkan rekomendasi outfit sesuai cuaca, harmoni warna, dan status kebersihan cucianmu setiap pagi."
    )
    val icons = listOf(Icons.Default.WbSunny, Icons.Default.Checkroom, Icons.Default.AutoAwesome)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(24.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icons[page], contentDescription = null, modifier = Modifier.size(96.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(24.dp))
                Text(titles[page], fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(descriptions[page], textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onFinish) { Text("Lewati") }
                Button(onClick = { if (page < 2) page++ else onFinish() }) {
                    Text(if (page < 2) "Lanjut" else "Mulai Sekarang")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val privacyPolicyUrl = "https://github.com/fitcast-privacy-policy"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tentang FitCast") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("FitCast (CuacaGaya) v1.0", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Asisten lemari digital pintar berbasis cuaca harian. Seluruh foto pakaianmu disimpan 100% secara lokal di perangkat dan tidak pernah diunggah ke server eksternal.")
            HorizontalDivider()
            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, privacyPolicyUrl.toUri()))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PrivacyTip, null)
                Spacer(Modifier.width(8.dp))
                Text("Buka Kebijakan Privasi (Privacy Policy)")
            }
        }
    }
}
