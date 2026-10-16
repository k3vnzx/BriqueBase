package com.example.ui.opportunities

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.OpportunityEntity
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.EmptyState
import com.example.ui.theme.*

@Composable
fun OpportunitiesScreen(viewModel: MainViewModel) {
    val opportunities by viewModel.opportunities.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = BriqueiLime,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Oportunidade")
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
                text = "Oportunidades do Marketplace",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Monitore anúncios com preço abaixo da média para revender com lucro.",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Legitimate integration notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = BriqueiCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Integração Ética: Sem scraping proibido. Salve anúncios manuais ou links de negociações reais.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (opportunities.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.LocalOffer,
                    title = "Nenhuma oportunidade cadastrada",
                    description = "Encontrou um aparelho barato no Facebook Marketplace ou OLX? Adicione aqui para calcular a margem de revenda.",
                    buttonText = "Adicionar Oportunidade",
                    onButtonClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(opportunities, key = { it.id }) { opp ->
                        OpportunityCard(
                            opportunity = opp,
                            onConvert = { viewModel.convertOpportunityToStock(opp.id) },
                            onToggleFavorite = { viewModel.toggleFavoriteOpportunity(opp) },
                            onDelete = { viewModel.deleteOpportunity(opp.id) },
                            onOpenLink = {
                                if (opp.sourceUrl.isNotBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(opp.sourceUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        viewModel.showFeedback("Link do anúncio: ${opp.sourceUrl}")
                                    }
                                } else {
                                    viewModel.showFeedback("Nenhum link web registrado para este anúncio.")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Smartphones") }
        var brand by remember { mutableStateOf("Apple") }
        var model by remember { mutableStateOf("") }
        var announcedPriceInput by remember { mutableStateOf("") }
        var referencePriceInput by remember { mutableStateOf("") }
        var estimatedCostsInput by remember { mutableStateOf("50.00") }
        var location by remember { mutableStateOf("São Paulo, SP") }
        var sourceUrl by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Cadastrar Nova Oportunidade", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título do Anúncio (Ex: iPhone 13 128GB Azul)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = announcedPriceInput,
                            onValueChange = { announcedPriceInput = it },
                            label = { Text("Preço Anunciado (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = referencePriceInput,
                            onValueChange = { referencePriceInput = it },
                            label = { Text("Preço Referência (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = estimatedCostsInput,
                            onValueChange = { estimatedCostsInput = it },
                            label = { Text("Custos Extras (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Localização") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = sourceUrl,
                        onValueChange = { sourceUrl = it },
                        label = { Text("Link do Anúncio (Marketplace / OLX)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações da Negociação") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val announced = announcedPriceInput.toDoubleOrNull() ?: 0.0
                        val ref = referencePriceInput.toDoubleOrNull() ?: (announced * 1.25)
                        val costs = estimatedCostsInput.toDoubleOrNull() ?: 0.0
                        val discount = if (ref > 0) ((ref - announced) / ref) * 100 else 0.0
                        val estimatedResale = ref * 0.95
                        val estimatedProfit = estimatedResale - (announced + costs)

                        val opp = OpportunityEntity(
                            title = title.ifBlank { "Oportunidade $announced" },
                            category = category,
                            brand = brand,
                            model = model.ifBlank { title },
                            announcedPrice = announced,
                            referencePrice = ref,
                            discountPercent = discount,
                            location = location,
                            sourceUrl = sourceUrl,
                            estimatedCosts = costs,
                            estimatedResalePrice = estimatedResale,
                            estimatedProfit = estimatedProfit,
                            condition = "Seminovo",
                            status = "NEW",
                            notes = notes,
                            isDemo = false
                        )
                        viewModel.saveOpportunity(opp)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiLime, contentColor = Color.Black)
                ) {
                    Text("Salvar Oportunidade", fontWeight = FontWeight.Bold)
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

@Composable
fun OpportunityCard(
    opportunity: OpportunityEntity,
    onConvert: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onOpenLink: () -> Unit
) {
    BriqueiCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (opportunity.isDemo) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Exemplo de Demonstração", fontSize = 10.sp, color = BriqueiCyan, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = opportunity.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${opportunity.location} • ${opportunity.condition}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (opportunity.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (opportunity.isFavorite) BriqueiRed else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Preço Pedido", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = "R$ ${"%.2f".format(opportunity.announcedPrice)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Ref. Mercado", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = "R$ ${"%.2f".format(opportunity.referencePrice)}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Desconto / Margem", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = "-${opportunity.discountPercent.toInt()}% (Lucro: R$ ${"%.0f".format(opportunity.estimatedProfit)})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BriqueiLime
                )
            }
        }

        if (opportunity.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Obs: ${opportunity.notes}", fontSize = 12.sp, color = TextMuted)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (opportunity.status != "CONVERTED") {
                Button(
                    onClick = onConvert,
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.5f).height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Converter em Estoque", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BriqueiLime.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✓ Convertido em Produto", fontSize = 12.sp, color = BriqueiLime, fontWeight = FontWeight.Bold)
                }
            }

            if (opportunity.sourceUrl.isNotBlank()) {
                OutlinedButton(
                    onClick = onOpenLink,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Abrir Link", fontSize = 12.sp)
                }
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remover", tint = TextMuted)
            }
        }
    }
}
