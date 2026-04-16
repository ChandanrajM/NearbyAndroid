package com.nearby.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nearby.app.data.SupabaseClient
import com.nearby.app.ui.components.LoadingScreen
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
private data class QrCode(
    val id: String = "",
    val shop_id: String = "",
    val code: String = ""
)

@Composable
fun ScanRedirectScreen(
    qrCode: String,
    onShopFound: (String) -> Unit,
    onNotFound: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(qrCode) {
        scope.launch {
            try {
                val result = SupabaseClient.client.postgrest["qr_codes"]
                    .select { filter { eq("code", qrCode) } }
                    .decodeSingleOrNull<QrCode>()

                if (result != null) {
                    onShopFound(result.shop_id)
                } else {
                    error = "QR code not recognized"
                    isLoading = false
                }
            } catch (e: Exception) {
                error = e.message ?: "Error resolving QR code"
                isLoading = false
            }
        }
    }

    if (isLoading && error == null) {
        LoadingScreen("Resolving QR code...")
    } else if (error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Text("Oops!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(error ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNotFound) { Text("Go Home") }
            }
        }
    }
}
