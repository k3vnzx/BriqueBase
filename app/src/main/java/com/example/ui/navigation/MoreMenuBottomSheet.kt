package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMuted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Módulos e Recursos BRIQUEI",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MoreMenuItem(
                    icon = Icons.Default.LocalOffer,
                    color = BriqueiCyan,
                    title = "Oportunidades do Marketplace",
                    subtitle = "Descubra briques abaixo da tabela"
                ) {
                    viewModel.navigateTo(AppScreen.OPPORTUNITIES)
                    onDismiss()
                }

                MoreMenuItem(
                    icon = Icons.Default.NotificationsActive,
                    color = BriqueiAmber,
                    title = "Alertas de Preço",
                    subtitle = "Rastreie produtos com teto de compra"
                ) {
                    viewModel.navigateTo(AppScreen.PRICE_ALERTS)
                    onDismiss()
                }

                MoreMenuItem(
                    icon = Icons.Default.Flag,
                    color = BriqueiLime,
                    title = "Controle de Metas",
                    subtitle = "Objetivos semanais e mensais de faturamento"
                ) {
                    viewModel.navigateTo(AppScreen.GOALS)
                    onDismiss()
                }

                MoreMenuItem(
                    icon = Icons.Default.EmojiEvents,
                    color = GoldRank,
                    title = "Rank de Briqueiros",
                    subtitle = "Pontuação, conquistas e evolução"
                ) {
                    viewModel.navigateTo(AppScreen.RANKING)
                    onDismiss()
                }

                MoreMenuItem(
                    icon = Icons.Default.ReceiptLong,
                    color = BriqueiCyan,
                    title = "Gerador de Comprovantes",
                    subtitle = "Recibos e termos de garantia profissionais"
                ) {
                    viewModel.navigateTo(AppScreen.RECEIPTS)
                    onDismiss()
                }

                MoreMenuItem(
                    icon = Icons.Default.AutoAwesome,
                    color = BriqueiPurple,
                    title = "IA Insight — Gerador de Anúncios",
                    subtitle = "Copywriting automático com Gemini 3.5 Flash"
                ) {
                    viewModel.navigateTo(AppScreen.AI_ADS)
                    onDismiss()
                }

                MoreMenuItem(
                    icon = Icons.Default.Settings,
                    color = TextSecondary,
                    title = "Perfil e Configurações",
                    subtitle = "Dados da loja, chave Pix e backup JSON"
                ) {
                    viewModel.navigateTo(AppScreen.PROFILE)
                    onDismiss()
                }
            }
        }
    }
}

@Composable
fun MoreMenuItem(
    icon: ImageVector,
    color: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
        }
    }
}
