package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class BriqueiRepository(private val db: AppDatabase) {

    private val productDao = db.productDao()
    private val transactionDao = db.transactionDao()
    private val inspectionDao = db.inspectionDao()
    private val opportunityDao = db.opportunityDao()
    private val priceAlertDao = db.priceAlertDao()
    private val goalDao = db.goalDao()
    private val receiptDao = db.receiptDao()
    private val savedAdDao = db.savedAdDao()
    private val notificationDao = db.appNotificationDao()
    private val userProfileDao = db.userProfileDao()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    // Products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val availableCount: Flow<Int> = productDao.getAvailableCount()
    val soldCount: Flow<Int> = productDao.getSoldCount()
    val totalInvestedInStock: Flow<Double?> = productDao.getTotalInvestedInStock()

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)
    suspend fun insertProduct(product: ProductEntity): Long {
        val id = productDao.insertProduct(product)
        // Add expense for stock purchase if new
        if (product.id == 0L && product.purchasePrice > 0) {
            transactionDao.insertTransaction(
                TransactionEntity(
                    type = "EXPENSE",
                    description = "Compra de Estoque: ${product.name}",
                    amount = product.purchasePrice + product.additionalCosts,
                    date = product.purchaseDate.ifBlank { dateFormat.format(Date()) },
                    category = "Compra de Estoque",
                    paymentMethod = "Pix",
                    status = "COMPLETED",
                    productId = id,
                    notes = "Registro automático de compra de estoque"
                )
            )
        }
        addPoints(10)
        notificationDao.insertNotification(
            AppNotificationEntity(
                title = "Novo Produto Cadastrado",
                message = "${product.name} foi adicionado ao seu estoque.",
                type = "STOCK",
                actionRoute = "inventory"
            )
        )
        return id
    }

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)
    suspend fun deleteProduct(id: Long) = productDao.deleteProductById(id)

    suspend fun reserveProduct(id: Long, clientName: String, deposit: Double, date: String) {
        val product = productDao.getProductById(id) ?: return
        val updated = product.copy(
            status = "RESERVED",
            reservationClient = clientName,
            reservationDeposit = deposit,
            reservationDate = date,
            updatedAt = System.currentTimeMillis()
        )
        productDao.updateProduct(updated)
        if (deposit > 0) {
            transactionDao.insertTransaction(
                TransactionEntity(
                    type = "INCOME",
                    description = "Sinal de Reserva: ${product.name} ($clientName)",
                    amount = deposit,
                    date = dateFormat.format(Date()),
                    category = "Sinal de Reserva",
                    paymentMethod = "Pix",
                    status = "COMPLETED",
                    productId = id,
                    notes = "Sinal para reserva do produto"
                )
            )
        }
        notificationDao.insertNotification(
            AppNotificationEntity(
                title = "Produto Reservado",
                message = "${product.name} reservado para $clientName com sinal de R$ ${"%.2f".format(deposit)}.",
                type = "STOCK",
                actionRoute = "inventory"
            )
        )
    }

    suspend fun sellProduct(
        id: Long,
        soldPrice: Double,
        buyerName: String,
        buyerPhone: String,
        paymentMethod: String,
        registerCashflow: Boolean
    ) {
        val product = productDao.getProductById(id) ?: return
        val today = dateFormat.format(Date())
        val updated = product.copy(
            status = "SOLD",
            soldPrice = soldPrice,
            soldDate = today,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            paymentMethod = paymentMethod,
            updatedAt = System.currentTimeMillis()
        )
        productDao.updateProduct(updated)

        val totalCost = product.purchasePrice + product.additionalCosts
        val deposit = product.reservationDeposit ?: 0.0
        val remainingAmount = (soldPrice - deposit).coerceAtLeast(0.0)

        if (registerCashflow && remainingAmount > 0) {
            transactionDao.insertTransaction(
                TransactionEntity(
                    type = "INCOME",
                    description = "Venda: ${product.name} ($buyerName)",
                    amount = remainingAmount,
                    date = today,
                    category = "Venda de Estoque",
                    paymentMethod = paymentMethod,
                    status = "COMPLETED",
                    productId = id,
                    notes = "Lucro líquido calculado: R$ ${"%.2f".format(soldPrice - totalCost)}"
                )
            )
        }

        addPoints(50)
        notificationDao.insertNotification(
            AppNotificationEntity(
                title = "Venda Realizada! 🎉",
                message = "${product.name} vendido para $buyerName por R$ ${"%.2f".format(soldPrice)} (Lucro: R$ ${"%.2f".format(soldPrice - totalCost)}).",
                type = "SALE",
                actionRoute = "inventory"
            )
        )
    }

    suspend fun cancelSale(id: Long) {
        val product = productDao.getProductById(id) ?: return
        val updated = product.copy(
            status = "AVAILABLE",
            soldPrice = null,
            soldDate = null,
            buyerName = null,
            buyerPhone = null,
            reservationClient = null,
            reservationDeposit = null,
            updatedAt = System.currentTimeMillis()
        )
        productDao.updateProduct(updated)
        notificationDao.insertNotification(
            AppNotificationEntity(
                title = "Venda Cancelada / Estorno",
                message = "${product.name} retornou ao estoque disponível.",
                type = "STOCK",
                actionRoute = "inventory"
            )
        )
    }

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val totalIncome: Flow<Double?> = transactionDao.getTotalIncome()
    val totalExpense: Flow<Double?> = transactionDao.getTotalExpense()

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        val id = transactionDao.insertTransaction(transaction)
        addPoints(5)
        return id
    }
    suspend fun updateTransaction(transaction: TransactionEntity) = transactionDao.updateTransaction(transaction)
    suspend fun deleteTransaction(id: Long) = transactionDao.deleteTransactionById(id)

    // Inspections
    val allInspections: Flow<List<InspectionEntity>> = inspectionDao.getAllInspections()
    suspend fun getInspectionById(id: Long): InspectionEntity? = inspectionDao.getInspectionById(id)
    suspend fun insertInspection(inspection: InspectionEntity): Long {
        val id = inspectionDao.insertInspection(inspection)
        addPoints(15)
        notificationDao.insertNotification(
            AppNotificationEntity(
                title = "Vistoria Realizada",
                message = "Vistoria do ${inspection.deviceModel}: ${inspection.recommendation}.",
                type = "INSPECTION",
                actionRoute = "inspections"
            )
        )
        return id
    }
    suspend fun deleteInspection(id: Long) = inspectionDao.deleteInspectionById(id)

    // Opportunities
    val allOpportunities: Flow<List<OpportunityEntity>> = opportunityDao.getAllOpportunities()
    suspend fun getOpportunityById(id: Long): OpportunityEntity? = opportunityDao.getOpportunityById(id)
    suspend fun insertOpportunity(opp: OpportunityEntity): Long {
        val id = opportunityDao.insertOpportunity(opp)
        checkAlertsForOpportunity(opp)
        return id
    }
    suspend fun updateOpportunity(opp: OpportunityEntity) = opportunityDao.updateOpportunity(opp)
    suspend fun deleteOpportunity(id: Long) = opportunityDao.deleteOpportunityById(id)

    suspend fun convertOpportunityToProduct(opportunityId: Long): Long {
        val opp = opportunityDao.getOpportunityById(opportunityId) ?: return -1L
        val newProduct = ProductEntity(
            name = opp.title,
            category = opp.category,
            brand = opp.brand,
            model = opp.model,
            internalCode = "BRQ-${Random().nextInt(9000) + 1000}",
            condition = opp.condition,
            purchasePrice = opp.announcedPrice,
            additionalCosts = opp.estimatedCosts,
            announcedPrice = opp.estimatedResalePrice.takeIf { it > 0 } ?: (opp.announcedPrice * 1.25),
            minSalePrice = opp.announcedPrice + opp.estimatedCosts + 100.0,
            purchaseDate = dateFormat.format(Date()),
            origin = "Marketplace",
            notes = "Convertido de oportunidade: ${opp.notes}",
            status = "AVAILABLE"
        )
        val prodId = insertProduct(newProduct)
        opportunityDao.updateOpportunity(opp.copy(status = "CONVERTED"))
        notificationDao.insertNotification(
            AppNotificationEntity(
                title = "Oportunidade Convertida em Estoque",
                message = "${opp.title} virou produto no estoque por R$ ${"%.2f".format(opp.announcedPrice)}.",
                type = "STOCK",
                actionRoute = "inventory"
            )
        )
        return prodId
    }

    // Price Alerts
    val allAlerts: Flow<List<PriceAlertEntity>> = priceAlertDao.getAllAlerts()
    suspend fun insertAlert(alert: PriceAlertEntity): Long {
        val id = priceAlertDao.insertAlert(alert)
        addPoints(5)
        return id
    }
    suspend fun updateAlert(alert: PriceAlertEntity) = priceAlertDao.updateAlert(alert)
    suspend fun deleteAlert(id: Long) = priceAlertDao.deleteAlertById(id)

    private suspend fun checkAlertsForOpportunity(opp: OpportunityEntity) {
        val alerts = priceAlertDao.getActiveAlerts()
        val titleLower = opp.title.lowercase()
        val brandLower = opp.brand.lowercase()
        val modelLower = opp.model.lowercase()

        for (alert in alerts) {
            val alertName = alert.productName.lowercase()
            val matchesProduct = titleLower.contains(alertName) ||
                    brandLower.contains(alertName) ||
                    modelLower.contains(alertName)

            val matchesPrice = opp.announcedPrice <= alert.targetPrice

            if (matchesProduct && matchesPrice) {
                priceAlertDao.updateAlert(alert.copy(
                    matchCount = alert.matchCount + 1,
                    lastChecked = displayDateFormat.format(Date())
                ))
                notificationDao.insertNotification(
                    AppNotificationEntity(
                        title = "Alerta de Preço Correspondido! 🎯",
                        message = "Encontrado '${opp.title}' por R$ ${"%.2f".format(opp.announcedPrice)} (Seu alvo: R$ ${"%.2f".format(alert.targetPrice)}).",
                        type = "ALERT",
                        actionRoute = "opportunities"
                    )
                )
            }
        }
    }

    // Goals
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()
    suspend fun insertGoal(goal: GoalEntity): Long = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: GoalEntity) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(id: Long) = goalDao.deleteGoalById(id)

    // Receipts
    val allReceipts: Flow<List<ReceiptEntity>> = receiptDao.getAllReceipts()
    suspend fun getReceiptById(id: Long): ReceiptEntity? = receiptDao.getReceiptById(id)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long {
        val id = receiptDao.insertReceipt(receipt)
        addPoints(10)
        notificationDao.insertNotification(
            AppNotificationEntity(
                title = "Comprovante Emitido",
                message = "Comprovante #${receipt.receiptNumber} para ${receipt.buyerName} gerado com sucesso.",
                type = "RECEIPT",
                actionRoute = "receipts"
            )
        )
        return id
    }
    suspend fun deleteReceipt(id: Long) = receiptDao.deleteReceiptById(id)
    suspend fun getNextReceiptNumber(): String {
        val count = receiptDao.getCount() + 1
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return "BRQ-$year-${"%04d".format(count)}"
    }

    // Saved Ads
    val allSavedAds: Flow<List<SavedAdEntity>> = savedAdDao.getAllSavedAds()
    suspend fun insertSavedAd(ad: SavedAdEntity): Long = savedAdDao.insertAd(ad)
    suspend fun deleteSavedAd(id: Long) = savedAdDao.deleteAdById(id)

    // Notifications
    val allNotifications: Flow<List<AppNotificationEntity>> = notificationDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = notificationDao.getUnreadCount()
    suspend fun markNotificationAsRead(id: Long) = notificationDao.markAsRead(id)
    suspend fun markAllNotificationsAsRead() = notificationDao.markAllAsRead()
    suspend fun deleteNotification(id: Long) = notificationDao.deleteNotificationById(id)
    suspend fun clearAllNotifications() = notificationDao.clearAll()

    // User Profile & Ranking
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfile()
    suspend fun updateUserProfile(profile: UserProfileEntity) = userProfileDao.insertOrUpdate(profile)

    suspend fun addPoints(points: Int) {
        val current = userProfileDao.getUserProfileOnce() ?: UserProfileEntity()
        val newPoints = current.userRankPoints + points
        val newLevel = when {
            newPoints >= 1000 -> "Lenda do Brique"
            newPoints >= 600 -> "Tubarão do Marketplace"
            newPoints >= 300 -> "Mestre do Brique"
            newPoints >= 100 -> "Negociador Ativo"
            else -> "Briqueiro Iniciante"
        }
        userProfileDao.insertOrUpdate(current.copy(userRankPoints = newPoints, userLevel = newLevel))
    }

    // Seed Initial Realistic Reseller Demo Data
    suspend fun checkAndSeedInitialData() {
        val currentProfile = userProfileDao.getUserProfileOnce()
        if (currentProfile != null && currentProfile.demoLoaded) {
            return
        }

        // Initialize user profile
        userProfileDao.insertOrUpdate(
            UserProfileEntity(
                id = 1,
                displayName = "Kevin Revendas",
                businessName = "BRIQUEI Imports & Eletrônicos",
                phone = "(11) 98765-4321",
                pixKey = "11987654321",
                currency = "BRL",
                userRankPoints = 320,
                userLevel = "Mestre do Brique",
                notificationsEnabled = true,
                demoLoaded = true
            )
        )

        // Seed Sample Products
        val p1 = ProductEntity(
            name = "iPhone 13 128GB Estelar",
            category = "Smartphones",
            brand = "Apple",
            model = "iPhone 13",
            internalCode = "BRQ-1042",
            imei = "354892019284712",
            condition = "Seminovo Excelente",
            description = "Bateria 89%, Face ID perfeito, sem marcas de uso, completo na caixa.",
            quantity = 1,
            purchasePrice = 2400.0,
            additionalCosts = 60.0, // Película + capa
            announcedPrice = 2950.0,
            minSalePrice = 2800.0,
            purchaseDate = "2026-10-02",
            origin = "Marketplace",
            notes = "Comprado de particular em shopping, laudo 100% aprovado.",
            status = "AVAILABLE"
        )
        val p2 = ProductEntity(
            name = "Samsung Galaxy S23 256GB Grafite",
            category = "Smartphones",
            brand = "Samsung",
            model = "Galaxy S23",
            internalCode = "BRQ-1043",
            imei = "356781290384751",
            condition = "Seminovo",
            description = "Aparelho nacional Anatel, tela 120Hz perfeita, 256GB armazenamento.",
            quantity = 1,
            purchasePrice = 2100.0,
            additionalCosts = 40.0,
            announcedPrice = 2690.0,
            minSalePrice = 2500.0,
            purchaseDate = "2026-10-05",
            origin = "OLX",
            notes = "Garantia até Dezembro.",
            status = "AVAILABLE"
        )
        val p3 = ProductEntity(
            name = "iPad Air 4ª Geração 64GB Wi-Fi Cinza Espacial",
            category = "Tablets",
            brand = "Apple",
            model = "iPad Air 4",
            internalCode = "BRQ-1044",
            imei = "DMPCK78QG5V9",
            condition = "Muito Bom",
            description = "Acompanha Apple Pencil réplica funcional e Smart Folio case.",
            quantity = 1,
            purchasePrice = 1850.0,
            additionalCosts = 50.0,
            announcedPrice = 2450.0,
            minSalePrice = 2300.0,
            purchaseDate = "2026-10-08",
            origin = "Indicação",
            notes = "Cliente comprou um iPad Pro e passou esse.",
            status = "RESERVED",
            reservationClient = "Lucas Andrade",
            reservationDeposit = 200.0,
            reservationDate = "2026-10-14"
        )
        val p4 = ProductEntity(
            name = "iPhone 12 64GB Preto",
            category = "Smartphones",
            brand = "Apple",
            model = "iPhone 12",
            internalCode = "BRQ-1039",
            imei = "359182736451920",
            condition = "Bom",
            description = "Bateria 84%, pequenas marcas nas bordas, tela e câmeras 100%.",
            quantity = 1,
            purchasePrice = 1600.0,
            additionalCosts = 80.0,
            announcedPrice = 2150.0,
            minSalePrice = 2000.0,
            purchaseDate = "2026-09-25",
            origin = "Marketplace",
            notes = "Venda rápida.",
            status = "SOLD",
            soldPrice = 2100.0,
            soldDate = "2026-10-10",
            buyerName = "Mariana Silva",
            buyerPhone = "(11) 97123-4567",
            paymentMethod = "Pix"
        )

        productDao.insertProduct(p1)
        productDao.insertProduct(p2)
        productDao.insertProduct(p3)
        productDao.insertProduct(p4)

        // Seed Sample Transactions
        transactionDao.insertTransaction(
            TransactionEntity(
                type = "INCOME",
                description = "Venda: iPhone 12 64GB Preto (Mariana Silva)",
                amount = 2100.0,
                date = "2026-10-10",
                category = "Venda de Estoque",
                paymentMethod = "Pix",
                status = "COMPLETED",
                notes = "Lucro de R$ 420,00"
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                type = "INCOME",
                description = "Sinal de Reserva: iPad Air 4 (Lucas Andrade)",
                amount = 200.0,
                date = "2026-10-14",
                category = "Sinal de Reserva",
                paymentMethod = "Pix",
                status = "COMPLETED",
                notes = "Sinal para segurar até sábado"
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                description = "Compra de Estoque: iPhone 13 128GB Estelar",
                amount = 2460.0,
                date = "2026-10-02",
                category = "Compra de Estoque",
                paymentMethod = "Pix",
                status = "COMPLETED",
                notes = "Aparelho + película"
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                description = "Compra de Estoque: Galaxy S23 256GB",
                amount = 2140.0,
                date = "2026-10-05",
                category = "Compra de Estoque",
                paymentMethod = "Pix",
                status = "COMPLETED"
            )
        )
        transactionDao.insertTransaction(
            TransactionEntity(
                type = "EXPENSE",
                description = "Lote de 5 Películas 3D e Capinhas Anti-impacto",
                amount = 120.0,
                date = "2026-10-03",
                category = "Peças e Reparo",
                paymentMethod = "Cartão de Crédito",
                status = "COMPLETED"
            )
        )

        // Seed Sample Opportunities
        opportunityDao.insertOpportunity(
            OpportunityEntity(
                title = "iPhone 14 Pro 128GB Roxo Profundo",
                category = "Smartphones",
                brand = "Apple",
                model = "iPhone 14 Pro",
                announcedPrice = 3600.0,
                referencePrice = 4500.0,
                discountPercent = 20.0,
                location = "São Paulo, SP (Metrô Tatuapé)",
                sourceUrl = "https://facebook.com/marketplace/item/demo14pro",
                estimatedCosts = 50.0,
                estimatedResalePrice = 4300.0,
                estimatedProfit = 650.0,
                condition = "Seminovo Excelente",
                status = "NEW",
                isFavorite = true,
                notes = "Vendedor urgente por viagem. Verificar Face ID e iCloud na entrega.",
                isDemo = true
            )
        )
        opportunityDao.insertOpportunity(
            OpportunityEntity(
                title = "MacBook Air M1 8GB 256GB Cinza",
                category = "Notebooks",
                brand = "Apple",
                model = "MacBook Air M1",
                announcedPrice = 2800.0,
                referencePrice = 3600.0,
                discountPercent = 22.2,
                location = "Campinas, SP",
                sourceUrl = "https://facebook.com/marketplace/item/demo-m1",
                estimatedCosts = 100.0,
                estimatedResalePrice = 3450.0,
                estimatedProfit = 550.0,
                condition = "Bateria 92% ciclos 180",
                status = "NEW",
                notes = "Carregador original incluso.",
                isDemo = true
            )
        )
        opportunityDao.insertOpportunity(
            OpportunityEntity(
                title = "PlayStation 5 Slim com 2 Controles",
                category = "Videogames",
                brand = "Sony",
                model = "PS5 Slim",
                announcedPrice = 2400.0,
                referencePrice = 3200.0,
                discountPercent = 25.0,
                location = "Santo André, SP",
                sourceUrl = "https://olx.com.br/item/demo-ps5",
                estimatedCosts = 0.0,
                estimatedResalePrice = 3000.0,
                estimatedProfit = 600.0,
                condition = "Completo na caixa",
                status = "ANALYZED",
                notes = "Verificado número de série e teste de jogos.",
                isDemo = true
            )
        )

        // Seed Sample Price Alerts
        priceAlertDao.insertAlert(
            PriceAlertEntity(
                productName = "iPhone 13",
                brand = "Apple",
                model = "13",
                category = "Smartphones",
                targetPrice = 2500.0,
                location = "São Paulo, SP",
                includeKeywords = "128gb, bateria boa, sem detalhes",
                excludeKeywords = "bloqueado, bypass, quebrado",
                isActive = true,
                lastChecked = "15/10/2026 14:30",
                matchCount = 2
            )
        )
        priceAlertDao.insertAlert(
            PriceAlertEntity(
                productName = "Galaxy S23",
                brand = "Samsung",
                model = "S23",
                category = "Smartphones",
                targetPrice = 2200.0,
                location = "SP",
                includeKeywords = "256gb",
                excludeKeywords = "trincado",
                isActive = true,
                lastChecked = "15/10/2026 12:00",
                matchCount = 1
            )
        )

        // Seed Sample Goals
        goalDao.insertGoal(
            GoalEntity(
                title = "Vender 6 Aparelhos no Mês",
                targetType = "SALES_COUNT",
                targetPeriod = "MONTHLY",
                targetValue = 6.0,
                currentValue = 1.0,
                startDate = "2026-10-01",
                endDate = "2026-10-31",
                isCompleted = false
            )
        )
        goalDao.insertGoal(
            GoalEntity(
                title = "Faturar R$ 15.000 em Outubro",
                targetType = "REVENUE",
                targetPeriod = "MONTHLY",
                targetValue = 15000.0,
                currentValue = 2300.0,
                startDate = "2026-10-01",
                endDate = "2026-10-31",
                isCompleted = false
            )
        )
        goalDao.insertGoal(
            GoalEntity(
                title = "Lucro Líquido de R$ 3.000 no Mês",
                targetType = "PROFIT",
                targetPeriod = "MONTHLY",
                targetValue = 3000.0,
                currentValue = 420.0,
                startDate = "2026-10-01",
                endDate = "2026-10-31",
                isCompleted = false
            )
        )

        // Seed Sample Receipt
        receiptDao.insertReceipt(
            ReceiptEntity(
                receiptNumber = "BRQ-2026-0001",
                sellerName = "BRIQUEI Imports & Eletrônicos",
                sellerPhone = "(11) 98765-4321",
                sellerDocument = "12.345.678/0001-90",
                buyerName = "Mariana Silva",
                buyerPhone = "(11) 97123-4567",
                buyerDocument = "123.456.789-00",
                productName = "iPhone 12 64GB Preto",
                brandModel = "Apple iPhone 12",
                imeiOrSerial = "359182736451920",
                totalAmount = 2100.0,
                paymentMethod = "Pix",
                paymentStatus = "PAGO",
                warrantyTerms = "Garantia de 90 dias para funcionamento de placa, câmeras e biometria. Não cobre danos por queda ou líquido.",
                date = "10/10/2026",
                notes = "Entrega realizada no Shopping Eldorado."
            )
        )

        // Seed Sample Inspection
        inspectionDao.insertInspection(
            InspectionEntity(
                deviceModel = "iPhone 13 Pro 128GB",
                imeiOrSerial = "354910293847561",
                sellerName = "Rodrigo Mendes",
                date = "12/10/2026 16:45",
                totalApproved = 11,
                totalAttention = 1,
                totalFailed = 0,
                totalSkipped = 0,
                recommendation = "CAUTION",
                generalNotes = "Aparelho em excelente estado estético. Atenção apenas para a saúde da bateria em 81% que precisará de substituição em breve.",
                checklistData = """{"bateria":"ATENCAO","geral":"Aprovado"}"""
            )
        )

        // Initial Notification
        notificationDao.insertNotification(
            AppNotificationEntity(
                title = "Bem-vindo ao BRIQUEI! 🚀",
                message = "Seu aplicativo completo para compra, vistoria e revenda de usados está pronto.",
                type = "SYSTEM",
                actionRoute = "dashboard"
            )
        )
    }
}
