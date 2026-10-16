package com.example.ui.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

data class LeaderboardUser(
    val position: Int,
    val name: String,
    val city: String,
    val points: Int,
    val level: String,
    val isCurrentUser: Boolean = false
)

@Composable
fun RankingScreen(viewModel: MainViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val inspections by viewModel.inspections.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    val userPoints = profile?.userRankPoints ?: 320
    val userLevel = profile?.userLevel ?: "Mestre do Brique"

    val communityLeaderboard = remember(userPoints) {
        listOf(
            LeaderboardUser(1, "Rodrigo Imports SP", "São Paulo, SP", 1420, "Lenda do Brique"),
            LeaderboardUser(2, "Camila iPhones RJ", "Rio de Janeiro, RJ", 980, "Tubarão do Marketplace"),
            LeaderboardUser(3, profile?.displayName ?: "Kevin Revendas", "São Paulo, SP", userPoints, userLevel, true),
            LeaderboardUser(4, "Marcos Tech Curitiba", "Curitiba, PR", 280, "Negociador Ativo"),
            LeaderboardUser(5, "Felipe Briques Poa", "Porto Alegre, RS", 190, "Negociador Ativo"),
            LeaderboardUser(6, "Bruna Eletrônicos", "Belo Horizonte, MG", 140, "Briqueiro Iniciante")
        ).sortedByDescending { it.points }.mapIndexed { index, item -> item.copy(position = index + 1) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Rank de Briqueiros",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Suba de nível registrando vendas, vistorias rigorosas e movimentações reais.",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        // Current User Profile Banner
        item {
            BriqueiCard(
                backgroundColor = DarkSurfaceVariant,
                borderColor = GoldRank.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GoldRank.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = GoldRank, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = profile?.displayName ?: "Briqueiro Pro",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = userLevel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BriqueiLime
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$userPoints",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldRank
                        )
                        Text(text = "Pontos Totais", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        // Transparent Scoring Rules Card
        item {
            BriqueiCard {
                SectionHeader(title = "Regras Transparentes de Pontuação")
                Text(text = "Pontuação gerada exclusivamente por ações reais salvas no banco:", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))

                ScoringRuleRow(icon = Icons.Default.MonetizationOn, label = "Venda Realizada no Estoque", points = "+50 pts", color = BriqueiLime)
                ScoringRuleRow(icon = Icons.Default.VerifiedUser, label = "Vistoria 12 Etapas Concluída", points = "+15 pts", color = BriqueiCyan)
                ScoringRuleRow(icon = Icons.Default.ReceiptLong, label = "Comprovante Emitido", points = "+10 pts", color = BriqueiAmber)
                ScoringRuleRow(icon = Icons.Default.Inventory2, label = "Novo Produto Cadastrado", points = "+10 pts", color = SuccessGreen)
                ScoringRuleRow(icon = Icons.Default.AccountBalanceWallet, label = "Movimentação Financeira Registrada", points = "+5 pts", color = TextSecondary)
            }
        }

        // Achievements List
        item {
            SectionHeader(title = "Conquistas Desbloqueadas")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val hasSale = products.any { it.status == "SOLD" }
                val hasInspection = inspections.isNotEmpty()
                val hasInventory = products.size >= 2
                val hasCashflow = transactions.size >= 3

                AchievementBadge(
                    title = "Primeira Venda Fechada",
                    desc = "Vendeu um produto e registrou o lucro no sistema.",
                    isUnlocked = hasSale,
                    icon = Icons.Default.Star
                )
                AchievementBadge(
                    title = "Perito em iPhone",
                    desc = "Concluiu a vistoria completa de 12 etapas.",
                    isUnlocked = hasInspection,
                    icon = Icons.Default.VerifiedUser
                )
                AchievementBadge(
                    title = "Estoque Profissional",
                    desc = "Cadastrou 2 ou mais produtos com SKU e controle de custos.",
                    isUnlocked = hasInventory,
                    icon = Icons.Default.Inventory2
                )
                AchievementBadge(
                    title = "Mestre do Caixa",
                    desc = "Registrou 3 ou mais movimentações no fluxo financeiro.",
                    isUnlocked = hasCashflow,
                    icon = Icons.Default.AccountBalanceWallet
                )
            }
        }

        // Leaderboard
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ranking da Comunidade (Demonstração)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Text(
                text = "Classificação de demonstração de revendedores verificados.",
                fontSize = 11.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                communityLeaderboard.forEach { user ->
                    LeaderboardRow(user = user)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ScoringRuleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    points: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 12.sp, color = TextPrimary)
        }
        Text(text = points, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AchievementBadge(
    title: String,
    desc: String,
    isUnlocked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    BriqueiCard(
        borderColor = if (isUnlocked) BriqueiLime.copy(alpha = 0.4f) else DarkBorder
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) BriqueiLime.copy(alpha = 0.2f) else DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isUnlocked) BriqueiLime else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) TextPrimary else TextSecondary
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            if (isUnlocked) {
                Text(text = "Desbloqueado", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BriqueiLime)
            }
        }
    }
}

@Composable
fun LeaderboardRow(user: LeaderboardUser) {
    val rankColor = when (user.position) {
        1 -> GoldRank
        2 -> SilverRank
        3 -> BronzeRank
        else -> TextSecondary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (user.isCurrentUser) BriqueiLime.copy(alpha = 0.1f) else DarkSurface)
            .border(
                1.dp,
                if (user.isCurrentUser) BriqueiLime else DarkBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(rankColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${user.position}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = rankColor
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (user.isCurrentUser) "${user.name} (Você)" else user.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.isCurrentUser) BriqueiLime else TextPrimary
                    )
                    Text(
                        text = "${user.city} • ${user.level}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "${user.points} pts",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (user.isCurrentUser) BriqueiLime else TextPrimary
            )
        }
    }
}
