package com.example.ui.financial

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.TransactionEntity
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun FinancialScreen(viewModel: MainViewModel) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE, PLANNED
    var searchQuery by remember { mutableStateOf("") }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val completedTransactions = remember(transactions) {
        transactions.filter { it.status == "COMPLETED" }
    }
    val plannedTransactions = remember(transactions) {
        transactions.filter { it.status == "PLANNED" }
    }

    val totalIncome = remember(completedTransactions) {
        completedTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(completedTransactions) {
        completedTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val finalBalance = totalIncome - totalExpense

    val filteredTransactions = remember(transactions, selectedTypeFilter, searchQuery) {
        transactions.filter { tx ->
            val matchesFilter = when (selectedTypeFilter) {
                "INCOME" -> tx.type == "INCOME" && tx.status == "COMPLETED"
                "EXPENSE" -> tx.type == "EXPENSE" && tx.status == "COMPLETED"
                "PLANNED" -> tx.status == "PLANNED"
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    tx.description.contains(searchQuery, ignoreCase = true) ||
                    tx.category.contains(searchQuery, ignoreCase = true) ||
                    tx.paymentMethod.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.ADD_EDIT_TRANSACTION) },
                containerColor = BriqueiLime,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Lançar Movimentação")
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

            // Screen Header with CSV Export
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fluxo de Caixa",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(
                    onClick = {
                        val csv = buildString {
                            appendLine("ID,Tipo,Data,Descricao,Categoria,Valor,FormaPagamento,Status")
                            transactions.forEach { t ->
                                appendLine("${t.id},${t.type},${t.date},\"${t.description}\",\"${t.category}\",${t.amount},\"${t.paymentMethod}\",${t.status}")
                            }
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csv)
                            type = "text/csv"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Exportar Movimentações CSV"))
                    }
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Exportar CSV", tint = BriqueiLime)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cashflow Balance Card
            BriqueiCard(
                backgroundColor = DarkSurfaceVariant,
                borderColor = if (finalBalance >= 0) BriqueiLime.copy(alpha = 0.4f) else BriqueiRed.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "SALDO LÍQUIDO REALIZADO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text(
                            text = "R$ ${"%,.2f".format(finalBalance)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (finalBalance >= 0) BriqueiLime else BriqueiRed
                        )
                    }
                    if (plannedTransactions.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BriqueiAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${plannedTransactions.size} Previstas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BriqueiAmber
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Total de Entradas", fontSize = 11.sp, color = TextMuted)
                        Text(text = "+ R$ ${"%,.2f".format(totalIncome)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BriqueiLime)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Total de Saídas", fontSize = 11.sp, color = TextMuted)
                        Text(text = "- R$ ${"%,.2f".format(totalExpense)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BriqueiRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simple visual cashflow bar comparison
            if (totalIncome > 0 || totalExpense > 0) {
                val totalVolume = (totalIncome + totalExpense).coerceAtLeast(1.0)
                val incomeRatio = (totalIncome / totalVolume).toFloat()

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Proporção Entradas / Saídas", fontSize = 11.sp, color = TextMuted)
                        Text(text = "${(incomeRatio * 100).toInt()}% Entradas", fontSize = 11.sp, color = BriqueiLime, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(BriqueiRed)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = incomeRatio)
                                .fillMaxHeight()
                                .background(BriqueiLime)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FinancialFilterChip("Todas", selected = selectedTypeFilter == "ALL") { selectedTypeFilter = "ALL" }
                FinancialFilterChip("Entradas", selected = selectedTypeFilter == "INCOME") { selectedTypeFilter = "INCOME" }
                FinancialFilterChip("Saídas", selected = selectedTypeFilter == "EXPENSE") { selectedTypeFilter = "EXPENSE" }
                FinancialFilterChip("Previstas", selected = selectedTypeFilter == "PLANNED") { selectedTypeFilter = "PLANNED" }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transactions List
            if (filteredTransactions.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = "Nenhuma movimentação",
                    description = "Registre despesas e receitas para acompanhar seu saldo com precisão.",
                    buttonText = "Lançar Movimentação",
                    onButtonClick = { viewModel.navigateTo(AppScreen.ADD_EDIT_TRANSACTION) }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionRowCard(
                            transaction = tx,
                            onMarkCompleted = {
                                viewModel.saveTransaction(tx.copy(status = "COMPLETED"))
                            },
                            onDelete = {
                                transactionToDelete = tx
                            }
                        )
                    }
                }
            }
        }
    }

    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Excluir Movimentação?", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente excluir '${tx.description}' de R$ ${"%.2f".format(tx.amount)}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(tx.id)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiRed)
                ) {
                    Text("Excluir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun FinancialFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) BriqueiLime else DarkSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.Black else TextSecondary
        )
    }
}

@Composable
fun TransactionRowCard(
    transaction: TransactionEntity,
    onMarkCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    BriqueiCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (transaction.type == "INCOME") BriqueiLime.copy(alpha = 0.15f)
                            else BriqueiRed.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.type == "INCOME") Icons.Default.ArrowDownward
                        else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (transaction.type == "INCOME") BriqueiLime else BriqueiRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${transaction.date} • ${transaction.category} • ${transaction.paymentMethod}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    if (transaction.notes.isNotBlank()) {
                        Text(
                            text = transaction.notes,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == "INCOME") "+" else "-"} R$ ${"%.2f".format(transaction.amount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (transaction.type == "INCOME") BriqueiLime else BriqueiRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(status = transaction.status)
            }
        }

        if (transaction.status == "PLANNED") {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onMarkCompleted,
                    colors = ButtonDefaults.textButtonColors(contentColor = BriqueiLime)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Marcar como Realizada", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
