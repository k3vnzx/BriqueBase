package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.ui.ai.AiAdGeneratorScreen
import com.example.ui.alerts.PriceAlertsScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.financial.AddEditTransactionScreen
import com.example.ui.financial.FinancialScreen
import com.example.ui.goals.GoalsScreen
import com.example.ui.inspection.InspectionResultScreen
import com.example.ui.inspection.InspectionRunnerScreen
import com.example.ui.inspection.InspectionScreen
import com.example.ui.inventory.AddEditProductScreen
import com.example.ui.inventory.InventoryScreen
import com.example.ui.inventory.ProductDetailScreen
import com.example.ui.navigation.GlobalSearchModal
import com.example.ui.navigation.MoreMenuBottomSheet
import com.example.ui.notifications.NotificationsScreen
import com.example.ui.opportunities.OpportunitiesScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.ranking.RankingScreen
import com.example.ui.receipts.CreateReceiptScreen
import com.example.ui.receipts.ReceiptDetailScreen
import com.example.ui.receipts.ReceiptsScreen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BriqueiMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BriqueiMainApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val unreadNotifications by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSearchModal by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    // Intercept hardware / system Back gesture
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        viewModel.navigateBack()
    }

    // Trigger feedback message into Snackbar
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearFeedback()
            }
        }
    }

    val showBottomBar = currentScreen in listOf(
        AppScreen.DASHBOARD,
        AppScreen.INVENTORY,
        AppScreen.FINANCIAL,
        AppScreen.INSPECTION,
        AppScreen.OPPORTUNITIES,
        AppScreen.PRICE_ALERTS,
        AppScreen.GOALS,
        AppScreen.RANKING,
        AppScreen.RECEIPTS,
        AppScreen.AI_ADS,
        AppScreen.PROFILE
    )

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showBottomBar) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(BriqueiLime)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BRIQUEI",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                letterSpacing = 2.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearchModal = true }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Busca Global", tint = TextPrimary)
                        }
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) }) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifications > 0) {
                                        Badge(containerColor = BriqueiLime, contentColor = Color.Black) {
                                            Text("$unreadNotifications", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notificações", tint = TextPrimary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.DASHBOARD,
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Início") },
                        label = { Text("Início", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = BriqueiLime,
                            indicatorColor = BriqueiLime,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.INVENTORY,
                        onClick = { viewModel.navigateTo(AppScreen.INVENTORY) },
                        icon = { Icon(imageVector = Icons.Default.Inventory2, contentDescription = "Estoque") },
                        label = { Text("Estoque", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = BriqueiLime,
                            indicatorColor = BriqueiLime,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.FINANCIAL,
                        onClick = { viewModel.navigateTo(AppScreen.FINANCIAL) },
                        icon = { Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "Financeiro") },
                        label = { Text("Financeiro", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = BriqueiLime,
                            indicatorColor = BriqueiLime,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.INSPECTION,
                        onClick = { viewModel.navigateTo(AppScreen.INSPECTION) },
                        icon = { Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = "Vistoria") },
                        label = { Text("Vistoria", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = BriqueiLime,
                            indicatorColor = BriqueiLime,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )

                    NavigationBarItem(
                        selected = currentScreen in listOf(
                            AppScreen.OPPORTUNITIES,
                            AppScreen.PRICE_ALERTS,
                            AppScreen.GOALS,
                            AppScreen.RANKING,
                            AppScreen.RECEIPTS,
                            AppScreen.AI_ADS,
                            AppScreen.PROFILE
                        ),
                        onClick = { showMoreMenu = true },
                        icon = { Icon(imageVector = Icons.Default.Menu, contentDescription = "Mais") },
                        label = { Text("Mais", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = BriqueiLime,
                            indicatorColor = BriqueiLime,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppScreen.INVENTORY -> InventoryScreen(viewModel = viewModel)
                AppScreen.PRODUCT_DETAIL -> ProductDetailScreen(viewModel = viewModel)
                AppScreen.ADD_EDIT_PRODUCT -> AddEditProductScreen(viewModel = viewModel)
                AppScreen.FINANCIAL -> FinancialScreen(viewModel = viewModel)
                AppScreen.ADD_EDIT_TRANSACTION -> AddEditTransactionScreen(viewModel = viewModel)
                AppScreen.INSPECTION -> InspectionScreen(viewModel = viewModel)
                AppScreen.INSPECTION_RUNNER -> InspectionRunnerScreen(viewModel = viewModel)
                AppScreen.INSPECTION_RESULT -> InspectionResultScreen(viewModel = viewModel)
                AppScreen.OPPORTUNITIES, AppScreen.ADD_OPPORTUNITY -> OpportunitiesScreen(viewModel = viewModel)
                AppScreen.PRICE_ALERTS, AppScreen.ADD_PRICE_ALERT -> PriceAlertsScreen(viewModel = viewModel)
                AppScreen.GOALS, AppScreen.ADD_GOAL -> GoalsScreen(viewModel = viewModel)
                AppScreen.RANKING -> RankingScreen(viewModel = viewModel)
                AppScreen.RECEIPTS -> ReceiptsScreen(viewModel = viewModel)
                AppScreen.CREATE_RECEIPT -> CreateReceiptScreen(viewModel = viewModel)
                AppScreen.RECEIPT_DETAIL -> ReceiptDetailScreen(viewModel = viewModel)
                AppScreen.AI_ADS -> AiAdGeneratorScreen(viewModel = viewModel)
                AppScreen.PROFILE -> ProfileScreen(viewModel = viewModel)
                AppScreen.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                else -> DashboardScreen(viewModel = viewModel)
            }
        }
    }

    if (showSearchModal) {
        GlobalSearchModal(
            viewModel = viewModel,
            onDismiss = { showSearchModal = false }
        )
    }

    if (showMoreMenu) {
        MoreMenuBottomSheet(
            viewModel = viewModel,
            onDismiss = { showMoreMenu = false }
        )
    }
}
