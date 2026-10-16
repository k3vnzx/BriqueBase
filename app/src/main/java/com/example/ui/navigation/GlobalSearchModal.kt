package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchModal(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val products by viewModel.products.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()
    val opportunities by viewModel.opportunities.collectAsStateWithLifecycle()

    val matchedProducts = remember(query, products) {
        if (query.length < 2) emptyList()
        else products.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.internalCode.contains(query, ignoreCase = true) ||
            (it.imei?.contains(query) == true)
        }
    }

    val matchedTransactions = remember(query, transactions) {
        if (query.length < 2) emptyList()
        else transactions.filter {
            it.description.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }

    val matchedReceipts = remember(query, receipts) {
        if (query.length < 2) emptyList()
        else receipts.filter {
            it.receiptNumber.contains(query, ignoreCase = true) ||
            it.buyerName.contains(query, ignoreCase = true) ||
            it.productName.contains(query, ignoreCase = true)
        }
    }

    val matchedOpportunities = remember(query, opportunities) {
        if (query.length < 2) emptyList()
        else opportunities.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.location.contains(query, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar em todo o BRIQUEI...", color = TextMuted) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = BriqueiLime) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpar", tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (query.length < 2) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Digite pelo menos 2 caracteres para buscar em produtos, vendas, comprovantes e oportunidades.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxHeight(0.8f)
                ) {
                    // Products
                    if (matchedProducts.isNotEmpty()) {
                        item {
                            Text("📦 Produtos no Estoque (${matchedProducts.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BriqueiLime)
                        }
                        items(matchedProducts) { prod ->
                            BriqueiCard(onClick = {
                                viewModel.selectProduct(prod)
                                onDismiss()
                            }) {
                                Text(text = prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text(text = "${prod.internalCode} • R$ ${"%.2f".format(prod.announcedPrice)} • ${prod.status}", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }

                    // Transactions
                    if (matchedTransactions.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("💰 Movimentações Financeiras (${matchedTransactions.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BriqueiCyan)
                        }
                        items(matchedTransactions) { tx ->
                            BriqueiCard(onClick = {
                                viewModel.navigateTo(AppScreen.FINANCIAL)
                                onDismiss()
                            }) {
                                Text(text = tx.description, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text(text = "${tx.date} • R$ ${"%.2f".format(tx.amount)} (${tx.type})", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }

                    // Receipts
                    if (matchedReceipts.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("📄 Comprovantes de Venda (${matchedReceipts.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BriqueiAmber)
                        }
                        items(matchedReceipts) { rec ->
                            BriqueiCard(onClick = {
                                viewModel.selectReceipt(rec)
                                onDismiss()
                            }) {
                                Text(text = "#${rec.receiptNumber} - ${rec.productName}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text(text = "Comprador: ${rec.buyerName} • R$ ${"%.2f".format(rec.totalAmount)}", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }

                    // Opportunities
                    if (matchedOpportunities.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("🎯 Oportunidades do Marketplace (${matchedOpportunities.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BriqueiPurple)
                        }
                        items(matchedOpportunities) { opp ->
                            BriqueiCard(onClick = {
                                viewModel.navigateTo(AppScreen.OPPORTUNITIES)
                                onDismiss()
                            }) {
                                Text(text = opp.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text(text = "${opp.location} • R$ ${"%.2f".format(opp.announcedPrice)}", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }

                    if (matchedProducts.isEmpty() && matchedTransactions.isEmpty() && matchedReceipts.isEmpty() && matchedOpportunities.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nenhum registro encontrado para '$query'.", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
