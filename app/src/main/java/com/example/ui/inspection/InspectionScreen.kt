package com.example.ui.inspection

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.InspectionEntity
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun InspectionScreen(viewModel: MainViewModel) {
    val inspections by viewModel.inspections.collectAsStateWithLifecycle()
    var showNewModal by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewModal = true },
                containerColor = BriqueiCyan,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nova Vistoria")
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
                text = "Vistoria Antigolpe iPhone",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Checklist rigoroso de 12 etapas para negociações presenciais seguras.",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Start Card
            BriqueiCard(
                backgroundColor = DarkSurfaceVariant,
                borderColor = BriqueiCyan.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vai fechar um brique agora?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Execute a vistoria de 3 minutos e evite comprar aparelho com iCloud preso, Face ID queimado ou tela paralela.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showNewModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar Vistoria 12 Etapas", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Histórico de Vistorias Realizadas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (inspections.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.VerifiedUser,
                    title = "Nenhuma vistoria salva",
                    description = "Inicie a avaliação de um iPhone para gerar laudos e relatórios de negociação.",
                    buttonText = "Iniciar Vistoria",
                    onButtonClick = { showNewModal = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(inspections, key = { it.id }) { item ->
                        InspectionHistoryCard(
                            inspection = item,
                            onClick = { viewModel.selectInspection(item) }
                        )
                    }
                }
            }
        }
    }

    if (showNewModal) {
        var modelInput by remember { mutableStateOf("iPhone 13 128GB") }
        var imeiInput by remember { mutableStateOf("") }
        var sellerInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewModal = false },
            title = { Text("Dados do Aparelho a Avaliar", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Informe o modelo e os dados do vendedor antes de iniciar os testes.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = modelInput,
                        onValueChange = { modelInput = it },
                        label = { Text("Modelo do iPhone (Ex: iPhone 13 128GB)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = imeiInput,
                        onValueChange = { imeiInput = it },
                        label = { Text("IMEI (Disponível em Ajustes > Sobre)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sellerInput,
                        onValueChange = { sellerInput = it },
                        label = { Text("Nome do Vendedor / Anunciante") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.startNewInspection(
                            model = modelInput.ifBlank { "iPhone" },
                            imei = imeiInput,
                            seller = sellerInput
                        )
                        showNewModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiCyan, contentColor = Color.Black)
                ) {
                    Text("Começar Checklist", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewModal = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun InspectionHistoryCard(
    inspection: InspectionEntity,
    onClick: () -> Unit
) {
    val (recText, recColor) = when (inspection.recommendation) {
        "ABORT" -> Pair("Interromper Negociação", BriqueiRed)
        "CAUTION" -> Pair("Negociar com Cautela", BriqueiAmber)
        else -> Pair("Avançar com Segurança", BriqueiLime)
    }

    BriqueiCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = inspection.deviceModel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Vendedor: ${inspection.sellerName.ifBlank { "Não informado" }} • ${inspection.date}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(recColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = recText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = recColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "✓ ${inspection.totalApproved} Aprovados", fontSize = 12.sp, color = BriqueiLime, fontWeight = FontWeight.SemiBold)
            Text(text = "⚠ ${inspection.totalAttention} Atenção", fontSize = 12.sp, color = BriqueiAmber, fontWeight = FontWeight.SemiBold)
            Text(text = "✗ ${inspection.totalFailed} Reprovados", fontSize = 12.sp, color = BriqueiRed, fontWeight = FontWeight.SemiBold)
        }
    }
}
