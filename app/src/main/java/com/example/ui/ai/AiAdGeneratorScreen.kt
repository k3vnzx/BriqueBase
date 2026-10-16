package com.example.ui.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.SectionHeader
import com.example.ui.inventory.FilterTabChip
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAdGeneratorScreen(viewModel: MainViewModel) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingAd.collectAsStateWithLifecycle()
    val adResult by viewModel.adResult.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var productName by remember { mutableStateOf("iPhone 13 128GB Estelar") }
    var brand by remember { mutableStateOf("Apple") }
    var model by remember { mutableStateOf("iPhone 13") }
    var condition by remember { mutableStateOf("Seminovo Excelente") }
    var priceInput by remember { mutableStateOf("2950.00") }
    var defects by remember { mutableStateOf("Nenhum defeito, 100% testado na vistoria de 12 etapas, bateria 89%") }
    var accessories by remember { mutableStateOf("Cabo original, carregador 20W e capinha anti-impacto") }
    var selectedPlatform by remember { mutableStateOf("Todos") }
    var selectedTone by remember { mutableStateOf("Profissional") }

    var activeTab by remember { mutableStateOf("WHATSAPP") } // WHATSAPP, INSTAGRAM, MARKETPLACE, HIGHLIGHTS

    val tones = listOf("Profissional", "Direto", "Premium", "Informal", "Focado em Conversão")

    Scaffold(
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Screen Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BriqueiPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = BriqueiPurple, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "IA Insight — Gerador de Anúncios",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Gemini 3.5 Flash integrado com copywriting de revenda",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Quick Fill from Stock
            if (products.isNotEmpty()) {
                var stockDropdownExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { stockDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preencher com produto do estoque...")
                    }
                    DropdownMenu(
                        expanded = stockDropdownExpanded,
                        onDismissRequest = { stockDropdownExpanded = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text("${prod.name} (R$ ${"%.2f".format(prod.announcedPrice)})") },
                                onClick = {
                                    productName = prod.name
                                    brand = prod.brand
                                    model = prod.model
                                    condition = prod.condition
                                    priceInput = prod.announcedPrice.toString()
                                    defects = if (prod.description.isNotBlank()) prod.description else "Aparelho 100% revisado"
                                    stockDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Input Fields Card
            BriqueiCard {
                SectionHeader(title = "Dados para o Anúncio")

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Nome do Aparelho") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = condition,
                        onValueChange = { condition = it },
                        label = { Text("Estado / Condição") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Preço (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = defects,
                    onValueChange = { defects = it },
                    label = { Text("Detalhes, Saúde da Bateria e Estado") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = accessories,
                    onValueChange = { accessories = it },
                    label = { Text("Acessórios Incluídos") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Estilo / Tom de Voz:", fontSize = 12.sp, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tones.take(3).forEach { t ->
                        FilterChip(
                            selected = selectedTone == t,
                            onClick = { selectedTone = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tones.drop(3).forEach { t ->
                        FilterChip(
                            selected = selectedTone == t,
                            onClick = { selectedTone = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val price = priceInput.toDoubleOrNull() ?: 0.0
                        viewModel.generateAiAd(
                            productName = productName,
                            brand = brand,
                            model = model,
                            condition = condition,
                            price = price,
                            defects = defects,
                            accessories = accessories,
                            platform = selectedPlatform,
                            tone = selectedTone
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiPurple, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Criando Anúncio com IA...")
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gerar Anúncio com IA", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Output Result Card
            if (adResult != null) {
                val res = adResult!!

                BriqueiCard(
                    borderColor = BriqueiPurple.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (res.isFromGeminiAi) "✨ GERADO VIA GEMINI 3.5 FLASH" else "★ COPYWRITING ESTRUTURADO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (res.isFromGeminiAi) BriqueiPurple else BriqueiCyan
                        )
                        IconButton(
                            onClick = {
                                val currentText = when (activeTab) {
                                    "WHATSAPP" -> res.whatsappVersion
                                    "INSTAGRAM" -> res.instagramVersion
                                    "MARKPLACE" -> res.marketplaceVersion
                                    else -> res.description
                                }
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Anúncio BRIQUEI", currentText))
                                viewModel.showFeedback("Texto copiado para a área de transferência!")
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copiar", tint = BriqueiLime)
                        }
                    }

                    if (res.notice != null) {
                        Text(text = res.notice, fontSize = 11.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = res.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Platform selector tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterTabChip("WhatsApp", selected = activeTab == "WHATSAPP") { activeTab = "WHATSAPP" }
                        FilterTabChip("Instagram", selected = activeTab == "INSTAGRAM") { activeTab = "INSTAGRAM" }
                        FilterTabChip("Marketplace", selected = activeTab == "MARKETPLACE") { activeTab = "MARKETPLACE" }
                        FilterTabChip("Destaques", selected = activeTab == "HIGHLIGHTS") { activeTab = "HIGHLIGHTS" }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val displayText = when (activeTab) {
                        "WHATSAPP" -> res.whatsappVersion
                        "INSTAGRAM" -> res.instagramVersion
                        "MARKETPLACE" -> res.marketplaceVersion
                        else -> res.highlights.joinToString("\n• ", prefix = "• ") + "\n\n" + res.callToAction
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceVariant)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = displayText,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 19.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, displayText)
                                    type = "text/plain"
                                    setPackage("com.whatsapp")
                                }
                                try {
                                    context.startActivity(sendIntent)
                                } catch (e: Exception) {
                                    val fallback = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, displayText)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(fallback, "Enviar Anúncio"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.3f).height(42.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enviar no WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val price = priceInput.toDoubleOrNull() ?: 0.0
                                viewModel.saveCurrentAd(
                                    title = res.title,
                                    brandModel = "$brand $model",
                                    price = price,
                                    platform = selectedPlatform,
                                    tone = selectedTone
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salvar", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
