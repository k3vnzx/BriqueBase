package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.MetricCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    // Calculated Metrics
    val completedTransactions = remember(transactions) {
        transactions.filter { it.status == "COMPLETED" }
    }
    val totalIncome = remember(completedTransactions) {
        completedTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalExpense = remember(completedTransactions) {
        completedTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val netBalance = totalIncome - totalExpense

    val availableProducts = remember(products) {
        products.filter { it.status == "AVAILABLE" }
    }
    val soldProducts = remember(products) {
        products.filter { it.status == "SOLD" }
    }
    val totalInvestedInStock = remember(availableProducts) {
        availableProducts.sumOf { it.purchasePrice + it.additionalCosts }
    }
    val realizedProfitFromSales = remember(soldProducts) {
        soldProducts.sumOf { (it.soldPrice ?: 0.0) - (it.purchasePrice + it.additionalCosts) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = profile?.businessName ?: "BRIQUEI Revendas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Olá, ${profile?.displayName ?: "Briqueiro"} • Nível ${profile?.userLevel ?: "Mestre"}",
                        fontSize = 13.sp,
                        color = BriqueiLime
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BriqueiLime.copy(alpha = 0.15f))
                        .clickable { viewModel.navigateTo(AppScreen.RANKING) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = BriqueiLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${profile?.userRankPoints ?: 0} pts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BriqueiLime
                        )
                    }
                }
            }
        }

        // Main Financial Balance Banner
        item {
            BriqueiCard(
                backgroundColor = DarkSurfaceVariant,
                borderColor = if (netBalance >= 0) BriqueiLime.copy(alpha = 0.3f) else BriqueiRed.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SALDO EM CAIXA REALIZADO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "R$ ${"%,.2f".format(netBalance)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (netBalance >= 0) BriqueiLime else BriqueiRed
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.FINANCIAL) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Ver Financeiro",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(BriqueiLime)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Entradas", fontSize = 12.sp, color = TextSecondary)
                        }
                        Text(
                            text = "R$ ${"%,.2f".format(totalIncome)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(BriqueiRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Saídas", fontSize = 12.sp, color = TextSecondary)
                        }
                        Text(
                            text = "R$ ${"%,.2f".format(totalExpense)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(BriqueiCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Lucro Vendas", fontSize = 12.sp, color = TextSecondary)
                        }
                        Text(
                            text = "R$ ${"%,.2f".format(realizedProfitFromSales)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BriqueiCyan
                        )
                    }
                }
            }
        }

        // Metric Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Valor em Estoque",
                    value = "R$ ${"%,.0f".format(totalInvestedInStock)}",
                    subtitle = "${availableProducts.size} produtos disponíveis",
                    icon = Icons.Default.Inventory2,
                    iconColor = BriqueiLime,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.INVENTORY) }
                )
                MetricCard(
                    title = "Aparelhos Vendidos",
                    value = "${soldProducts.size}",
                    subtitle = "Vendas confirmadas",
                    icon = Icons.Default.CheckCircle,
                    iconColor = BriqueiCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.INVENTORY) }
                )
            }
        }

        // Quick Actions Row
        item {
            SectionHeader(title = "Atalhos Rápidos")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    QuickActionChip(
                        icon = Icons.Default.Add,
                        label = "Novo Produto",
                        color = BriqueiLime
                    ) { viewModel.navigateTo(AppScreen.ADD_EDIT_PRODUCT) }
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.VerifiedUser,
                        label = "Vistoria 12 Etapas",
                        color = BriqueiCyan
                    ) { viewModel.startNewInspection() }
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.AutoAwesome,
                        label = "Gerar Anúncio IA",
                        color = BriqueiPurple
                    ) { viewModel.navigateTo(AppScreen.AI_ADS) }
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.ReceiptLong,
                        label = "Emitir Comprovante",
                        color = BriqueiAmber
                    ) { viewModel.navigateTo(AppScreen.CREATE_RECEIPT) }
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Lançar Caixa",
                        color = SuccessGreen
                    ) { viewModel.navigateTo(AppScreen.ADD_EDIT_TRANSACTION) }
                }
            }
        }

        // Reseller Goals Preview
        item {
            SectionHeader(
                title = "Metas do Mês",
                actionText = "Ver Todas",
                onActionClick = { viewModel.navigateTo(AppScreen.GOALS) }
            )
            if (goals.isEmpty()) {
                BriqueiCard {
                    Text(
                        text = "Nenhuma meta definida. Crie objetivos para acelerar suas vendas!",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    goals.take(2).forEach { goal ->
                        BriqueiCard(
                            onClick = { viewModel.navigateTo(AppScreen.GOALS) }
                        ) {
                            val progress = if (goal.targetValue > 0) {
                                (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)
                            } else 0f
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goal.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BriqueiLime
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = BriqueiLime,
                                trackColor = DarkSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Atual: ${goal.currentValue.toInt()}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Alvo: ${goal.targetValue.toInt()}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Preview
        item {
            SectionHeader(
                title = "Últimas Movimentações",
                actionText = "Ver Extrato",
                onActionClick = { viewModel.navigateTo(AppScreen.FINANCIAL) }
            )
            if (transactions.isEmpty()) {
                BriqueiCard {
                    Text(
                        text = "Nenhuma movimentação registrada.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    transactions.take(4).forEach { tx ->
                        BriqueiCard(
                            onClick = { viewModel.navigateTo(AppScreen.FINANCIAL) }
                        ) {
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
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (tx.type == "INCOME") BriqueiLime.copy(alpha = 0.15f)
                                                else BriqueiRed.copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (tx.type == "INCOME") Icons.Default.ArrowDownward
                                            else Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = if (tx.type == "INCOME") BriqueiLime else BriqueiRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = tx.description,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${tx.date} • ${tx.category}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = "${if (tx.type == "INCOME") "+" else "-"} R$ ${"%.2f".format(tx.amount)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.type == "INCOME") BriqueiLime else BriqueiRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Available Inventory Highlights
        item {
            SectionHeader(
                title = "Estoque Disponível",
                actionText = "Ver Todos",
                onActionClick = { viewModel.navigateTo(AppScreen.INVENTORY) }
            )
            if (availableProducts.isEmpty()) {
                BriqueiCard {
                    Text(
                        text = "Estoque vazio. Cadastre novos aparelhos para iniciar suas vendas!",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableProducts.take(3).forEach { prod ->
                        BriqueiCard(
                            onClick = { viewModel.selectProduct(prod) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${prod.condition} • Custo: R$ ${"%.2f".format(prod.purchasePrice + prod.additionalCosts)}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "R$ ${"%.2f".format(prod.announcedPrice)}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BriqueiLime
                                    )
                                    StatusBadge(status = prod.status)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionChip(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}
