package com.example.ui.receipts

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.ReceiptEntity
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun ReceiptsScreen(viewModel: MainViewModel) {
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.CREATE_RECEIPT) },
                containerColor = BriqueiAmber,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Comprovante")
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
                text = "Gerador de Comprovantes",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Emita recibos e termos de garantia profissionais para enviar aos compradores via WhatsApp.",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (receipts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.ReceiptLong,
                    title = "Nenhum comprovante gerado",
                    description = "Gere recibos de venda para dar mais segurança e profissionalismo aos seus clientes.",
                    buttonText = "Emitir Novo Comprovante",
                    onButtonClick = { viewModel.navigateTo(AppScreen.CREATE_RECEIPT) }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(receipts, key = { it.id }) { receipt ->
                        ReceiptItemCard(
                            receipt = receipt,
                            onClick = { viewModel.selectReceipt(receipt) },
                            onShareWhatsApp = {
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
                                    appendLine("\n_Agradecemos a preferência!_")
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, msg)
                                    type = "text/plain"
                                    setPackage("com.whatsapp")
                                }
                                try {
                                    context.startActivity(sendIntent)
                                } catch (e: Exception) {
                                    // Fallback to any share app
                                    val fallbackIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, msg)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(fallbackIntent, "Enviar Comprovante"))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptItemCard(
    receipt: ReceiptEntity,
    onClick: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    BriqueiCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${receipt.receiptNumber}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BriqueiAmber
                )
                Text(
                    text = receipt.productName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Comprador: ${receipt.buyerName} • ${receipt.date}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = "R$ ${"%.2f".format(receipt.totalAmount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BriqueiLime
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(status = receipt.paymentStatus)

            Button(
                onClick = onShareWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
