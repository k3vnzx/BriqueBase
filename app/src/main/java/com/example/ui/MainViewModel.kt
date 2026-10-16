package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiAdService
import com.example.data.api.GeneratedAdResult
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.repository.BriqueiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    DASHBOARD,
    INVENTORY,
    PRODUCT_DETAIL,
    ADD_EDIT_PRODUCT,
    FINANCIAL,
    ADD_EDIT_TRANSACTION,
    INSPECTION,
    INSPECTION_RUNNER,
    INSPECTION_RESULT,
    OPPORTUNITIES,
    ADD_OPPORTUNITY,
    PRICE_ALERTS,
    ADD_PRICE_ALERT,
    GOALS,
    ADD_GOAL,
    RANKING,
    RECEIPTS,
    CREATE_RECEIPT,
    RECEIPT_DETAIL,
    AI_ADS,
    PROFILE,
    NOTIFICATIONS,
    GLOBAL_SEARCH
}

// 12-Step Inspection Check Item
data class InspectionCheckStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val whatToLookFor: String,
    var status: String = "NOT_CHECKED", // "APPROVED", "ATTENTION", "FAILED", "NOT_CHECKED"
    var note: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = BriqueiRepository(db)
    private val geminiService = GeminiAdService()

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _screenStack = mutableListOf(AppScreen.DASHBOARD)

    // Selected items for detail screens
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    private val _selectedInspection = MutableStateFlow<InspectionEntity?>(null)
    val selectedInspection: StateFlow<InspectionEntity?> = _selectedInspection.asStateFlow()

    private val _selectedReceipt = MutableStateFlow<ReceiptEntity?>(null)
    val selectedReceipt: StateFlow<ReceiptEntity?> = _selectedReceipt.asStateFlow()

    // UI Feedback message
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    // Global Search Query
    val searchQuery = MutableStateFlow("")

    // Data streams
    val products = repository.allProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactions = repository.allTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val inspections = repository.allInspections.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val opportunities = repository.allOpportunities.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val priceAlerts = repository.allAlerts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val goals = repository.allGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val receipts = repository.allReceipts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val savedAds = repository.allSavedAds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val unreadNotificationsCount = repository.unreadNotificationsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val availableCount = repository.availableCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val soldCount = repository.soldCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalInvestedInStock = repository.totalInvestedInStock.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalIncome = repository.totalIncome.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val totalExpense = repository.totalExpense.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // AI Ad Generator State
    private val _isGeneratingAd = MutableStateFlow(false)
    val isGeneratingAd: StateFlow<Boolean> = _isGeneratingAd.asStateFlow()

    private val _adResult = MutableStateFlow<GeneratedAdResult?>(null)
    val adResult: StateFlow<GeneratedAdResult?> = _adResult.asStateFlow()

    // Inspection Flow State
    val activeInspectionSteps = MutableStateFlow(getInitial12Steps())
    val activeInspectionModel = MutableStateFlow("iPhone 13 128GB")
    val activeInspectionImei = MutableStateFlow("")
    val activeInspectionSeller = MutableStateFlow("")
    val activeInspectionNotes = MutableStateFlow("")

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    // Navigation Methods
    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _screenStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_screenStack.size > 1) {
            _screenStack.removeAt(_screenStack.size - 1)
            _currentScreen.value = _screenStack.last()
            return true
        }
        return false
    }

    fun selectProduct(product: ProductEntity) {
        _selectedProduct.value = product
        navigateTo(AppScreen.PRODUCT_DETAIL)
    }

    fun selectReceipt(receipt: ReceiptEntity) {
        _selectedReceipt.value = receipt
        navigateTo(AppScreen.RECEIPT_DETAIL)
    }

    fun selectInspection(inspection: InspectionEntity) {
        _selectedInspection.value = inspection
        navigateTo(AppScreen.INSPECTION_RESULT)
    }

    fun showFeedback(message: String) {
        _feedbackMessage.value = message
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    // Product actions
    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repository.insertProduct(product)
                showFeedback("Produto cadastrado com sucesso!")
            } else {
                repository.updateProduct(product)
                showFeedback("Produto atualizado!")
            }
            navigateBack()
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
            showFeedback("Produto excluído do estoque.")
            navigateBack()
        }
    }

    fun sellProduct(
        id: Long,
        soldPrice: Double,
        buyerName: String,
        buyerPhone: String,
        paymentMethod: String,
        registerCashflow: Boolean
    ) {
        viewModelScope.launch {
            repository.sellProduct(id, soldPrice, buyerName, buyerPhone, paymentMethod, registerCashflow)
            showFeedback("Venda registrada com sucesso! +50 pontos no Ranking 🎉")
            _selectedProduct.value = repository.getProductById(id)
        }
    }

    fun reserveProduct(id: Long, clientName: String, deposit: Double, date: String) {
        viewModelScope.launch {
            repository.reserveProduct(id, clientName, deposit, date)
            showFeedback("Reserva registrada!")
            _selectedProduct.value = repository.getProductById(id)
        }
    }

    fun cancelSale(id: Long) {
        viewModelScope.launch {
            repository.cancelSale(id)
            showFeedback("Venda cancelada. Produto restaurado ao estoque.")
            _selectedProduct.value = repository.getProductById(id)
        }
    }

    fun duplicateProduct(product: ProductEntity) {
        viewModelScope.launch {
            val duplicate = product.copy(
                id = 0,
                name = "${product.name} (Cópia)",
                internalCode = "BRQ-${Random().nextInt(9000) + 1000}",
                status = "AVAILABLE",
                soldPrice = null,
                soldDate = null,
                buyerName = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertProduct(duplicate)
            showFeedback("Cadastro duplicado!")
        }
    }

    // Cashflow actions
    fun saveTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            if (transaction.id == 0L) {
                repository.insertTransaction(transaction)
                showFeedback("Movimentação registrada com sucesso!")
            } else {
                repository.updateTransaction(transaction)
                showFeedback("Movimentação atualizada!")
            }
            navigateBack()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            showFeedback("Movimentação excluída.")
        }
    }

    // Opportunities
    fun saveOpportunity(opp: OpportunityEntity) {
        viewModelScope.launch {
            if (opp.id == 0L) {
                repository.insertOpportunity(opp)
                showFeedback("Oportunidade adicionada!")
            } else {
                repository.updateOpportunity(opp)
                showFeedback("Oportunidade atualizada!")
            }
            navigateBack()
        }
    }

    fun deleteOpportunity(id: Long) {
        viewModelScope.launch {
            repository.deleteOpportunity(id)
            showFeedback("Oportunidade removida.")
        }
    }

    fun convertOpportunityToStock(oppId: Long) {
        viewModelScope.launch {
            val newProdId = repository.convertOpportunityToProduct(oppId)
            if (newProdId > 0) {
                showFeedback("Oportunidade convertida em produto no estoque! 📦")
            }
        }
    }

    fun toggleFavoriteOpportunity(opp: OpportunityEntity) {
        viewModelScope.launch {
            repository.updateOpportunity(opp.copy(isFavorite = !opp.isFavorite))
        }
    }

    // Price Alerts
    fun savePriceAlert(alert: PriceAlertEntity) {
        viewModelScope.launch {
            if (alert.id == 0L) {
                repository.insertAlert(alert)
                showFeedback("Alerta de preço ativado!")
            } else {
                repository.updateAlert(alert)
                showFeedback("Alerta atualizado!")
            }
            navigateBack()
        }
    }

    fun toggleAlertActive(alert: PriceAlertEntity) {
        viewModelScope.launch {
            repository.updateAlert(alert.copy(isActive = !alert.isActive))
            showFeedback(if (!alert.isActive) "Alerta reativado!" else "Alerta pausado.")
        }
    }

    fun deleteAlert(id: Long) {
        viewModelScope.launch {
            repository.deleteAlert(id)
            showFeedback("Alerta excluído.")
        }
    }

    // Goals
    fun saveGoal(goal: GoalEntity) {
        viewModelScope.launch {
            if (goal.id == 0L) {
                repository.insertGoal(goal)
                showFeedback("Nova meta cadastrada!")
            } else {
                repository.updateGoal(goal)
                showFeedback("Meta atualizada!")
            }
            navigateBack()
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoal(id)
            showFeedback("Meta excluída.")
        }
    }

    // Receipts
    fun saveReceipt(receipt: ReceiptEntity) {
        viewModelScope.launch {
            if (receipt.id == 0L) {
                val receiptWithNumber = if (receipt.receiptNumber.isBlank()) {
                    receipt.copy(receiptNumber = repository.getNextReceiptNumber())
                } else receipt
                val id = repository.insertReceipt(receiptWithNumber)
                _selectedReceipt.value = receiptWithNumber.copy(id = id)
                showFeedback("Comprovante #${receiptWithNumber.receiptNumber} gerado!")
                navigateTo(AppScreen.RECEIPT_DETAIL)
            } else {
                repository.insertReceipt(receipt)
                _selectedReceipt.value = receipt
                showFeedback("Comprovante atualizado!")
                navigateBack()
            }
        }
    }

    fun deleteReceipt(id: Long) {
        viewModelScope.launch {
            repository.deleteReceipt(id)
            showFeedback("Comprovante excluído.")
            navigateBack()
        }
    }

    // 12-Step Inspection Flow
    fun startNewInspection(model: String = "iPhone", imei: String = "", seller: String = "") {
        activeInspectionSteps.value = getInitial12Steps()
        activeInspectionModel.value = model
        activeInspectionImei.value = imei
        activeInspectionSeller.value = seller
        activeInspectionNotes.value = ""
        navigateTo(AppScreen.INSPECTION_RUNNER)
    }

    fun updateStepStatus(stepNumber: Int, status: String, note: String = "") {
        val list = activeInspectionSteps.value.map {
            if (it.stepNumber == stepNumber) {
                it.copy(status = status, note = if (note.isNotBlank()) note else it.note)
            } else it
        }
        activeInspectionSteps.value = list
    }

    fun finalizeInspection() {
        val steps = activeInspectionSteps.value
        val approved = steps.count { it.status == "APPROVED" }
        val attention = steps.count { it.status == "ATTENTION" }
        val failed = steps.count { it.status == "FAILED" }
        val skipped = steps.count { it.status == "NOT_CHECKED" }

        val recommendation = when {
            failed > 0 -> "ABORT" // Interromper Negociação
            attention >= 2 -> "CAUTION" // Negociar com Cautela
            else -> "PROCEED" // Continuar Avaliando / Seguro para avançar com cautela
        }

        val json = JSONObject().apply {
            steps.forEach { step ->
                put("step_${step.stepNumber}", JSONObject().apply {
                    put("title", step.title)
                    put("status", step.status)
                    put("note", step.note)
                })
            }
        }.toString()

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val entity = InspectionEntity(
            deviceModel = activeInspectionModel.value,
            imeiOrSerial = activeInspectionImei.value,
            sellerName = activeInspectionSeller.value,
            date = sdf.format(Date()),
            totalApproved = approved,
            totalAttention = attention,
            totalFailed = failed,
            totalSkipped = skipped,
            recommendation = recommendation,
            generalNotes = activeInspectionNotes.value,
            checklistData = json
        )

        viewModelScope.launch {
            val id = repository.insertInspection(entity)
            _selectedInspection.value = entity.copy(id = id)
            showFeedback("Vistoria salva com sucesso! +15 pontos no Ranking.")
            navigateTo(AppScreen.INSPECTION_RESULT)
        }
    }

    // AI Ad Generator
    fun generateAiAd(
        productName: String,
        brand: String,
        model: String,
        condition: String,
        price: Double,
        defects: String,
        accessories: String,
        platform: String,
        tone: String
    ) {
        viewModelScope.launch {
            _isGeneratingAd.value = true
            try {
                val result = geminiService.generateAd(
                    productName = productName,
                    brand = brand,
                    model = model,
                    condition = condition,
                    price = price,
                    defects = defects,
                    accessories = accessories,
                    platform = platform,
                    tone = tone
                )
                _adResult.value = result
                showFeedback("Anúncio gerado com sucesso!")
            } catch (e: Exception) {
                showFeedback("Erro ao gerar anúncio: ${e.message}")
            } finally {
                _isGeneratingAd.value = false
            }
        }
    }

    fun saveCurrentAd(title: String, brandModel: String, price: Double, platform: String, tone: String) {
        val result = _adResult.value ?: return
        viewModelScope.launch {
            repository.insertSavedAd(
                SavedAdEntity(
                    title = title,
                    brandModel = brandModel,
                    price = price,
                    platform = platform,
                    tone = tone,
                    fullAdText = result.description,
                    whatsappVersion = result.whatsappVersion,
                    instagramVersion = result.instagramVersion,
                    marketplaceVersion = result.marketplaceVersion,
                    highlights = result.highlights.joinToString(" • ")
                )
            )
            showFeedback("Anúncio salvo no histórico!")
        }
    }

    // Notification actions
    fun markNotificationAsRead(id: Long) = viewModelScope.launch { repository.markNotificationAsRead(id) }
    fun markAllNotificationsAsRead() = viewModelScope.launch { repository.markAllNotificationsAsRead() }
    fun deleteNotification(id: Long) = viewModelScope.launch { repository.deleteNotification(id) }
    fun clearAllNotifications() = viewModelScope.launch { repository.clearAllNotifications() }

    // User Profile
    fun updateProfile(profile: UserProfileEntity) = viewModelScope.launch {
        repository.updateUserProfile(profile)
        showFeedback("Perfil atualizado com sucesso!")
    }

    private fun getInitial12Steps(): List<InspectionCheckStep> {
        return listOf(
            InspectionCheckStep(
                stepNumber = 1,
                title = "IMEI e Identificação",
                instruction = "Abra Ajustes > Geral > Sobre. Verifique o IMEI do aparelho e compare com a gaveta do chip (se houver) e caixa/nota fiscal.",
                whatToLookFor = "IMEI não pode estar na blacklist/bloqueado. Números devem coincidir rigorosamente."
            ),
            InspectionCheckStep(
                stepNumber = 2,
                title = "Bloqueio de Ativação (iCloud)",
                instruction = "Peça para o proprietário desvincular a conta iCloud em sua frente e faça a redefinição: Ajustes > Geral > Transferir ou Redefinir > Apagar Conteúdo e Ajustes.",
                whatToLookFor = "Aparelho deve chegar na tela inicial 'Olá' sem solicitar senha anterior."
            ),
            InspectionCheckStep(
                stepNumber = 3,
                title = "Face ID ou Touch ID",
                instruction = "Cadastre o seu rosto no Face ID (ou digital no Touch ID) para testar os sensores TrueDepth.",
                whatToLookFor = "Não pode aparecer a mensagem 'O Face ID foi desativado' ou falhar no mapeamento 3D."
            ),
            InspectionCheckStep(
                stepNumber = 4,
                title = "Tela e Toque (Touch)",
                instruction = "Segure um ícone na tela inicial e arraste-o devagar por toda a extensão da tela. Confira True Tone na Central de Controle.",
                whatToLookFor = "Sem zonas mortas de toque, linhas verdes/brancas, fantasmas ou falta de True Tone."
            ),
            InspectionCheckStep(
                stepNumber = 5,
                title = "Câmeras e Flash",
                instruction = "Abra a câmera e teste todas as lentes (0.5x ultra-angular, 1x principal, 3x teleobjetiva), modo retrato e câmera frontal.",
                whatToLookFor = "Foco rápido, gravação de vídeo 4K 60fps sem travas, microfone na gravação e flash potente."
            ),
            InspectionCheckStep(
                stepNumber = 6,
                title = "Áudio e Microfones",
                instruction = "Grave um áudio no app Gravador falando perto e longe. Reproduza um toque alto nas configurações.",
                whatToLookFor = "Alto-falante superior (auricular) e inferior funcionando sem chiado, gravação de áudio limpa."
            ),
            InspectionCheckStep(
                stepNumber = 7,
                title = "Botões e Vibração",
                instruction = "Pressione Power, Volume +, Volume -, Chave de silencioso (ou Botão de Ação). Sinta a vibração.",
                whatToLookFor = "Botões com clique firme e responsivo. Motor Taptic Engine com vibração seca e sem barulho oco."
            ),
            InspectionCheckStep(
                stepNumber = 8,
                title = "Bateria e Aquecimento",
                instruction = "Abra Ajustes > Bateria > Saúde da Bateria. Verifique a porcentagem de capacidade máxima.",
                whatToLookFor = "Abaixo de 80% requer troca. Cuidado com mensagens de 'Peça Desconhecida' ou superaquecimento em repouso."
            ),
            InspectionCheckStep(
                stepNumber = 9,
                title = "Carregamento e Conector",
                instruction = "Conecte o carregador com cabo nos dois sentidos (lado A e lado B). Teste carregamento sem fio (MagSafe/Qi) se disponível.",
                whatToLookFor = "Conector firme sem folga, carregamento contínuo sem ficar desconectando."
            ),
            InspectionCheckStep(
                stepNumber = 10,
                title = "Conectividade (Wi-Fi, 4G/5G, BT)",
                instruction = "Conecte a uma rede Wi-Fi, ative Bluetooth e insira um chip (SIM card) ou ative eSIM para fazer uma ligação de teste.",
                whatToLookFor = "Aparelho deve encontrar redes 5GHz e reconhecer a operadora imediatamente sem sinal 'Sem Serviço'."
            ),
            InspectionCheckStep(
                stepNumber = 11,
                title = "Peças e Histórico de Reparos",
                instruction = "Ajustes > Geral > Sobre: verifique o aviso de Peças e Histórico de Reparos (tela, bateria, câmera). Inspecione parafusos Pentalobe inferiores.",
                whatToLookFor = "Avisos de 'Peça Desconhecida' indicam peças trocadas não genuínas. Parafusos espanados indicam abertura amadora."
            ),
            InspectionCheckStep(
                stepNumber = 12,
                title = "Procedência e Negociação",
                instruction = "Confira comprovantes de compra, consistência da história do vendedor, documento e realize o pagamento somente após todos os testes.",
                whatToLookFor = "Preço milagroso muito abaixo da tabela FIPE é sinal de alerta máximo. Nunca pague sinal antecipado para estranhos."
            )
        )
    }
}
