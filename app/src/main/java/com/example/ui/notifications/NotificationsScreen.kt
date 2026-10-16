package com.example.ui.notifications

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.AppNotificationEntity
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.EmptyState
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: MainViewModel) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Central de Notificações", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = "Marcar todas como lidas", tint = BriqueiLime)
                        }
                        IconButton(onClick = { viewModel.clearAllNotifications() }) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Limpar todas", tint = TextMuted)
                        }
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            if (notifications.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.NotificationsNone,
                    title = "Nenhuma notificação",
                    description = "Eventos de vendas, estoque e alertas de preço aparecerão aqui."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(notifications, key = { it.id }) { item ->
                        NotificationCard(
                            notification = item,
                            onClick = {
                                viewModel.markNotificationAsRead(item.id)
                                when (item.actionRoute) {
                                    "inventory" -> viewModel.navigateTo(AppScreen.INVENTORY)
                                    "opportunities" -> viewModel.navigateTo(AppScreen.OPPORTUNITIES)
                                    "receipts" -> viewModel.navigateTo(AppScreen.RECEIPTS)
                                    "inspections" -> viewModel.navigateTo(AppScreen.INSPECTION)
                                    else -> {}
                                }
                            },
                            onDelete = { viewModel.deleteNotification(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: AppNotificationEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, color) = when (notification.type) {
        "SALE" -> Pair(Icons.Default.MonetizationOn, BriqueiLime)
        "STOCK" -> Pair(Icons.Default.Inventory2, BriqueiCyan)
        "ALERT" -> Pair(Icons.Default.NotificationsActive, BriqueiAmber)
        "INSPECTION" -> Pair(Icons.Default.VerifiedUser, BriqueiPurple)
        "RECEIPT" -> Pair(Icons.Default.ReceiptLong, BriqueiAmber)
        else -> Pair(Icons.Default.Info, TextSecondary)
    }

    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(notification.timestamp))

    BriqueiCard(
        onClick = onClick,
        backgroundColor = if (notification.isRead) DarkSurface else DarkSurfaceVariant
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = notification.title,
                            fontSize = 14.sp,
                            fontWeight = if (notification.isRead) FontWeight.SemiBold else FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (!notification.isRead) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(BriqueiLime)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = notification.message,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Remover", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}
