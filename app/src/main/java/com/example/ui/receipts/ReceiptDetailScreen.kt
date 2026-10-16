package com.example.ui.receipts

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(viewModel: MainViewModel) {
    val selectedReceipt by viewModel.selectedReceipt.collectAsStateWithLifecycle()
    val receipt = selectedReceipt
    val context = LocalContext.current

    if (receipt == null) {
        Box(modifier = Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
            Text("Comprovante não encontrado.", color = TextSecondary)
        }
        return
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Comprovante #${receipt.receiptNumber}", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.RECEIPTS) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.deleteReceipt(receipt.id) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = BriqueiRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Authentic Commercial Receipt Voucher Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, BriqueiAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Receipt Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BRIQUEI COMPROVANTE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BriqueiLime,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "#${receipt.receiptNumber}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                        StatusBadge(status = receipt.paymentStatus)
                    }

                    HorizontalDivider(color = DarkBorder, thickness = 1.dp)

                    // Seller info
                    Column {
                        Text(text = "EMITENTE / VENDEDOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text(text = receipt.sellerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        if (receipt.sellerDocument.isNotBlank()) Text(text = "CNPJ/CPF: ${receipt.sellerDocument}", fontSize = 12.sp, color = TextSecondary)
                        if (receipt.sellerPhone.isNotBlank()) Text(text = "Contato: ${receipt.sellerPhone}", fontSize = 12.sp, color = TextSecondary)
                    }

                    // Buyer info
                    Column {
                        Text(text = "CLIENTE / COMPRADOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text(text = receipt.buyerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        if (receipt.buyerDocument.isNotBlank()) Text(text = "Documento: ${receipt.buyerDocument}", fontSize = 12.sp, color = TextSecondary)
                        if (receipt.buyerPhone.isNotBlank()) Text(text = "Telefone: ${receipt.buyerPhone}", fontSize = 12.sp, color = TextSecondary)
                    }

                    HorizontalDivider(color = DarkBorder, thickness = 1.dp)

                    // Product & Price
                    Column {
                        Text(text = "PRODUTO ADQUIRIDO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text(text = receipt.productName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        if (receipt.imeiOrSerial.isNotBlank()) {
                            Text(text = "IMEI/Serial: ${receipt.imeiOrSerial}", fontSize = 12.sp, color = BriqueiCyan)
                        }
                        Text(text = "Data da Compra: ${receipt.date} • Forma: ${receipt.paymentMethod}", fontSize = 12.sp, color = TextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "VALOR TOTAL:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Text(
                                text = "R$ ${"%.2f".format(receipt.totalAmount)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BriqueiLime
                            )
                        }
                    }

                    // Warranty conditions
                    Column {
                        Text(text = "TERMOS DE GARANTIA E CONDIÇÕES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BriqueiAmber)
                        Text(
                            text = receipt.warrantyTerms,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                    }

                    if (receipt.notes.isNotBlank()) {
                        Column {
                            Text(text = "OBSERVAÇÕES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Text(text = receipt.notes, fontSize = 12.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Documento emitido eletronicamente para fins de controle entre as partes.",
                        fontSize = 10.sp,
                        color = TextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Action Buttons
            Button(
                onClick = {
                    val msg = buildString {
                        appendLine("📄 *COMPROVANTE DE VENDA & GARANTIA*")
                        appendLine("Código: *#${receipt.receiptNumber}*")
                        appendLine("📅 Data: ${receipt.date}")
                        appendLine("🏢 Vendedor: *${receipt.sellerName}*")
                        appendLine("👤 Comprador: *${receipt.buyerName}*")
                        appendLine("📱 Aparelho: *${receipt.productName}*")
                        if (receipt.imeiOrSerial.isNotBlank()) appendLine("🔍 IMEI: ${receipt.imeiOrSerial}")
                        appendLine("💰 Valor: *R$ ${"%.2f".format(receipt.totalAmount)}* (${receipt.paymentMethod})")
                        appendLine("🛡️ Termos: ${receipt.warrantyTerms}")
                        appendLine("\n_Agradecemos pela confiança e preferência!_")
                    }
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, msg)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Enviar Comprovante ao Cliente"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enviar Comprovante no WhatsApp", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    val textToShare = buildString {
                        appendLine("==========================================")
                        appendLine("COMPROVANTE DE VENDA #${receipt.receiptNumber}")
                        appendLine("==========================================")
                        appendLine("Vendedor: ${receipt.sellerName}")
                        appendLine("Comprador: ${receipt.buyerName}")
                        appendLine("Produto: ${receipt.productName}")
                        appendLine("Valor: R$ ${"%.2f".format(receipt.totalAmount)}")
                        appendLine("Pagamento: ${receipt.paymentMethod}")
                        appendLine("Data: ${receipt.date}")
                        appendLine("Garantia: ${receipt.warrantyTerms}")
                        appendLine("==========================================")
                    }
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, textToShare)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Comprovante"))
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compartilhar Texto Completo")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
