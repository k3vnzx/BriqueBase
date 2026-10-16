package com.example.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.PriceAlertEntity
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.EmptyState
import com.example.ui.theme.*

@Composable
fun PriceAlertsScreen(viewModel: MainViewModel) {
    val alerts by viewModel.priceAlerts.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = BriqueiAmber,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Criar Alerta")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Alertas de Preço",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Seja notificado automaticamente quando surgir um produto abaixo do seu teto de compra.",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (alerts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.NotificationsActive,
                    title = "Nenhum alerta cadastrado",
                    description = "Crie alertas para modelos desejados (ex: iPhone 13 por menos de R$ 2.400) e receba notificações imediatas.",
                    buttonText = "Criar Primeiro Alerta",
                    onButtonClick = { showCreateDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(alerts, key = { it.id }) { alert ->
                        BriqueiCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = alert.productName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (alert.matchCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(BriqueiLime.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${alert.matchCount} achados",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BriqueiLime
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Teto Máximo: R$ ${"%.2f".format(alert.targetPrice)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BriqueiAmber
                                    )
                                    if (alert.includeKeywords.isNotBlank()) {
                                        Text(
                                            text = "Palavras-chave: ${alert.includeKeywords}",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    if (alert.lastChecked.isNotBlank()) {
                                        Text(
                                            text = "Última verificação: ${alert.lastChecked}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Switch(
                                    checked = alert.isActive,
                                    onCheckedChange = { viewModel.toggleAlertActive(alert) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = BriqueiLime, checkedTrackColor = Color(0xFF064E3B))
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { viewModel.deleteAlert(alert.id) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = BriqueiRed)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Excluir", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var targetPriceInput by remember { mutableStateOf("") }
        var includeKeywords by remember { mutableStateOf("") }
        var excludeKeywords by remember { mutableStateOf("bloqueado, quebrado, bypass") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Criar Alerta de Preço", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Modelo do Aparelho (Ex: iPhone 13 128GB)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetPriceInput,
                        onValueChange = { targetPriceInput = it },
                        label = { Text("Preço-Alvo Máximo (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = includeKeywords,
                        onValueChange = { includeKeywords = it },
                        label = { Text("Palavras-chave Incluídas (Ex: bateria 85%, caixa)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = excludeKeywords,
                        onValueChange = { excludeKeywords = it },
                        label = { Text("Palavras-chave Excluídas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = targetPriceInput.toDoubleOrNull() ?: 2000.0
                        viewModel.savePriceAlert(
                            PriceAlertEntity(
                                productName = name.ifBlank { "iPhone 13" },
                                targetPrice = price,
                                includeKeywords = includeKeywords,
                                excludeKeywords = excludeKeywords,
                                isActive = true
                            )
                        )
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiAmber, contentColor = Color.Black)
                ) {
                    Text("Ativar Alerta", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}
