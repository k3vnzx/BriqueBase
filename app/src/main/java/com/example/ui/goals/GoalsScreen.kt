package com.example.ui.goals

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
import com.example.data.local.entities.GoalEntity
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.EmptyState
import com.example.ui.theme.*

@Composable
fun GoalsScreen(viewModel: MainViewModel) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BriqueiLime,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nova Meta")
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
                text = "Controle de Metas",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Defina alvos de vendas, faturamento e lucro para acelerar o crescimento do seu brique.",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (goals.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Flag,
                    title = "Nenhuma meta definida",
                    description = "Crie metas semanais ou mensais para acompanhar seu progresso de faturamento e lucro.",
                    buttonText = "Definir Meta",
                    onButtonClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(goals, key = { it.id }) { goal ->
                        val progress = if (goal.targetValue > 0) {
                            (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)
                        } else 0f
                        val isComplete = progress >= 1f

                        BriqueiCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = goal.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Período: ${if (goal.targetPeriod == "MONTHLY") "Mensal" else "Semanal"} • Até ${goal.endDate}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isComplete) BriqueiLime.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isComplete) "✓ Batida!" else "${(progress * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isComplete) BriqueiLime else TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = if (isComplete) BriqueiLime else BriqueiCyan,
                                trackColor = DarkSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Atual: ${if (goal.targetType == "REVENUE" || goal.targetType == "PROFIT") "R$ %,.2f".format(goal.currentValue) else goal.currentValue.toInt().toString()}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Alvo: ${if (goal.targetType == "REVENUE" || goal.targetType == "PROFIT") "R$ %,.2f".format(goal.targetValue) else goal.targetValue.toInt().toString()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { viewModel.deleteGoal(goal.id) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Remover", tint = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var targetType by remember { mutableStateOf("SALES_COUNT") }
        var targetPeriod by remember { mutableStateOf("MONTHLY") }
        var targetValueInput by remember { mutableStateOf("5") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Cadastrar Meta", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título da Meta (Ex: Vender 8 Aparelhos)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(text = "Tipo de Objetivo:", fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = targetType == "SALES_COUNT",
                            onClick = { targetType = "SALES_COUNT" },
                            label = { Text("Vendas", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = targetType == "REVENUE",
                            onClick = { targetType = "REVENUE" },
                            label = { Text("Faturamento", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = targetType == "PROFIT",
                            onClick = { targetType = "PROFIT" },
                            label = { Text("Lucro", fontSize = 11.sp) }
                        )
                    }

                    OutlinedTextField(
                        value = targetValueInput,
                        onValueChange = { targetValueInput = it },
                        label = { Text(if (targetType == "SALES_COUNT") "Quantidade Alvo" else "Valor Alvo em R$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val value = targetValueInput.toDoubleOrNull() ?: 5.0
                        viewModel.saveGoal(
                            GoalEntity(
                                title = title.ifBlank { "Meta de Vendas" },
                                targetType = targetType,
                                targetPeriod = targetPeriod,
                                targetValue = value,
                                currentValue = 0.0,
                                startDate = "2026-10-01",
                                endDate = "2026-10-31"
                            )
                        )
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiLime, contentColor = Color.Black)
                ) {
                    Text("Salvar Meta", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}
