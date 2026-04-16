package com.nearby.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nearby.app.ui.components.LoadingScreen
import com.nearby.app.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) { LoadingScreen("Loading Dashboard..."); return }

    val shop = uiState.shop
    if (shop == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Store, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No shop registered", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onBack) { Text("Go Back") }
            }
        }
        return
    }

    val tabs = listOf("Orders", "Inventory", "Settings")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(shop.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Dashboard", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (shop.isOnline) Color(0xFF2E7D32).copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            if (shop.isOnline) "ONLINE" else "OFFLINE",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (shop.isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tabs
            TabRow(selectedTabIndex = uiState.activeTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.activeTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = { Text(title, fontWeight = if (uiState.activeTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (uiState.activeTab) {
                0 -> DashboardOrdersTab()
                1 -> DashboardInventoryTab(uiState.products, onDelete = { viewModel.deleteProduct(it) })
                2 -> DashboardSettingsTab(
                    shopName = uiState.shopName,
                    isOnline = uiState.isOnline,
                    imageUrl = uiState.imageUrl,
                    category = uiState.category,
                    onNameChange = viewModel::updateShopName,
                    onToggleOnline = { viewModel.toggleOnline() },
                    onImageUrlChange = viewModel::updateImageUrl,
                    onCategoryChange = viewModel::updateCategory,
                    onSave = { viewModel.saveShopSettings() }
                )
            }
        }
    }
}

@Composable
private fun DashboardOrdersTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Receipt, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Orders will appear here", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("When customers place orders", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun DashboardInventoryTab(
    products: List<com.nearby.app.data.model.Product>,
    onDelete: (String) -> Unit
) {
    if (products.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No products listed", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold)
                            Text("₹${product.price.toInt()} · Stock: ${product.stock}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDelete(product.id) }) {
                            Icon(Icons.Filled.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardSettingsTab(
    shopName: String,
    isOnline: Boolean,
    imageUrl: String,
    category: String,
    onNameChange: (String) -> Unit,
    onToggleOnline: () -> Unit,
    onImageUrlChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Online toggle
        Card(shape = RoundedCornerShape(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Shop Status", fontWeight = FontWeight.SemiBold)
                    Text(if (isOnline) "Accepting orders" else "Not accepting orders", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isOnline, onCheckedChange = { onToggleOnline() })
            }
        }

        OutlinedTextField(value = shopName, onValueChange = onNameChange, label = { Text("Shop Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = category, onValueChange = onCategoryChange, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = imageUrl, onValueChange = onImageUrlChange, label = { Text("Shop Image URL") }, modifier = Modifier.fillMaxWidth())

        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(48.dp), shape = MaterialTheme.shapes.medium) {
            Text("Save Changes", fontWeight = FontWeight.SemiBold)
        }
    }
}
