package com.example.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.ProductEntity
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(viewModel: MainViewModel) {
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val product = selectedProduct

    if (product == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(DarkBg),
            contentAlignment = Alignment.Center
        ) {
            Text("Produto não encontrado.", color = TextSecondary)
        }
        return
    }

    var showImei by remember { mutableStateOf(false) }
    var showSellDialog by remember { mutableStateOf(false) }
    var showReserveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val totalCost = product.purchasePrice + product.additionalCosts
    val profit = if (product.status == "SOLD") {
        (product.soldPrice ?: 0.0) - totalCost
    } else {
        product.announcedPrice - totalCost
    }
    val marginPercent = if (totalCost > 0) (profit / totalCost) * 100 else 0.0

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text(product.internalCode, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.duplicateProduct(product) }) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicar", tint = TextSecondary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = BriqueiRed)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            BriqueiCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.category,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BriqueiLime
                    )
                    StatusBadge(status = product.status)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "${product.brand} • ${product.model} • ${product.condition}",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            // Sale Information (if sold)
            if (product.status == "SOLD") {
                BriqueiCard(
                    backgroundColor = DarkSurfaceVariant,
                    borderColor = BriqueiCyan.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DADOS DA VENDA REALIZADA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BriqueiCyan
                        )
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BriqueiCyan)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vendido por: R$ ${"%.2f".format(product.soldPrice ?: 0.0)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Comprador: ${product.buyerName ?: "Não informado"}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    if (!product.buyerPhone.isNullOrBlank()) {
                        Text(
                            text = "Contato: ${product.buyerPhone}",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "Data: ${product.soldDate ?: "Recente"} • Pagamento: ${product.paymentMethod ?: "Pix"}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lucro Líquido Realizado: + R$ ${"%.2f".format(profit)} (${marginPercent.toInt()}%)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BriqueiLime
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.cancelSale(product.id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BriqueiRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancelar Venda / Devolver ao Estoque")
                    }
                }
            }

            // Financial Breakdown Card
            BriqueiCard {
                SectionHeader(title = "Raio-X Financeiro")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Preço de Compra", fontSize = 12.sp, color = TextMuted)
                        Text(text = "R$ ${"%.2f".format(product.purchasePrice)}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Column {
                        Text(text = "Custos Adicionais", fontSize = 12.sp, color = TextMuted)
                        Text(text = "R$ ${"%.2f".format(product.additionalCosts)}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Custo Total", fontSize = 12.sp, color = TextMuted)
                        Text(text = "R$ ${"%.2f".format(totalCost)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Preço Anunciado", fontSize = 12.sp, color = TextMuted)
                        Text(text = "R$ ${"%.2f".format(product.announcedPrice)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BriqueiLime)
                    }
                    Column {
                        Text(text = "Preço Mínimo", fontSize = 12.sp, color = TextMuted)
                        Text(text = "R$ ${"%.2f".format(product.minSalePrice)}", fontSize = 14.sp, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Margem Potencial", fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = "+ R$ ${"%.2f".format(profit)} (${marginPercent.toInt()}%)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (profit >= 0) BriqueiLime else BriqueiRed
                        )
                    }
                }
            }

            // Identification & IMEI (with security toggle)
            BriqueiCard {
                SectionHeader(title = "Identificação e Segurança")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "IMEI / Número de Série", fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = if (product.imei.isNullOrBlank()) "Não registrado"
                            else if (showImei) product.imei
                            else "•••• •••• •••• ${product.imei.takeLast(4)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    if (!product.imei.isNullOrBlank()) {
                        IconButton(onClick = { showImei = !showImei }) {
                            Icon(
                                imageVector = if (showImei) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Revelar IMEI",
                                tint = BriqueiCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Origem: ${product.origin} • Comprado em: ${product.purchaseDate}", fontSize = 12.sp, color = TextSecondary)

                if (product.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Descrição:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(text = product.description, fontSize = 13.sp, color = TextPrimary)
                }

                if (product.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Observações internas:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(text = product.notes, fontSize = 13.sp, color = TextMuted)
                }
            }

            // Primary Reseller Action Buttons
            if (product.status != "SOLD") {
                Button(
                    onClick = { showSellDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar Venda Deste Produto", fontWeight = FontWeight.Bold)
                }

                if (product.status == "AVAILABLE") {
                    OutlinedButton(
                        onClick = { showReserveDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BriqueiAmber),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Bookmark, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reservar para Cliente (com sinal)")
                    }
                }
            }

            // Quick integrations buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.AI_ADS) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BriqueiPurple)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Anúncio IA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.CREATE_RECEIPT) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BriqueiCyan)
                ) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Comprovante", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Sell Product Dialog
    if (showSellDialog) {
        var soldPriceInput by remember { mutableStateOf(product.announcedPrice.toString()) }
        var buyerNameInput by remember { mutableStateOf("") }
        var buyerPhoneInput by remember { mutableStateOf("") }
        var paymentMethodInput by remember { mutableStateOf("Pix") }
        var autoRegisterCashflow by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showSellDialog = false },
            title = { Text("Registrar Venda do Produto", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = product.name, fontSize = 14.sp, color = BriqueiLime, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = soldPriceInput,
                        onValueChange = { soldPriceInput = it },
                        label = { Text("Valor Final da Venda (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = buyerNameInput,
                        onValueChange = { buyerNameInput = it },
                        label = { Text("Nome do Comprador") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = buyerPhoneInput,
                        onValueChange = { buyerPhoneInput = it },
                        label = { Text("Telefone / WhatsApp Comprador") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Forma de Pagamento:", fontSize = 12.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Pix", "Dinheiro", "Cartão", "Outro").forEach { method ->
                            FilterChip(
                                selected = paymentMethodInput == method,
                                onClick = { paymentMethodInput = method },
                                label = { Text(method, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = autoRegisterCashflow,
                            onCheckedChange = { autoRegisterCashflow = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lançar receita automaticamente no Fluxo de Caixa (sem duplicidade)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = soldPriceInput.toDoubleOrNull() ?: product.announcedPrice
                        val buyer = buyerNameInput.ifBlank { "Comprador Particular" }
                        viewModel.sellProduct(
                            id = product.id,
                            soldPrice = price,
                            buyerName = buyer,
                            buyerPhone = buyerPhoneInput,
                            paymentMethod = paymentMethodInput,
                            registerCashflow = autoRegisterCashflow
                        )
                        showSellDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiLime, contentColor = Color.Black)
                ) {
                    Text("Confirmar Venda", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSellDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // Reserve Product Dialog
    if (showReserveDialog) {
        var clientNameInput by remember { mutableStateOf("") }
        var depositInput by remember { mutableStateOf("100.00") }
        var dateInput by remember { mutableStateOf("Em até 3 dias") }

        AlertDialog(
            onDismissRequest = { showReserveDialog = false },
            title = { Text("Reservar Produto", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = product.name, fontSize = 13.sp, color = BriqueiAmber)

                    OutlinedTextField(
                        value = clientNameInput,
                        onValueChange = { clientNameInput = it },
                        label = { Text("Nome do Cliente") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = depositInput,
                        onValueChange = { depositInput = it },
                        label = { Text("Valor do Sinal (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text("Prazo da Reserva") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deposit = depositInput.toDoubleOrNull() ?: 0.0
                        viewModel.reserveProduct(
                            id = product.id,
                            clientName = clientNameInput.ifBlank { "Cliente Reserva" },
                            deposit = deposit,
                            date = dateInput
                        )
                        showReserveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiAmber, contentColor = Color.Black)
                ) {
                    Text("Confirmar Reserva", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReserveDialog = false }) {
                    Text("Voltar", color = TextSecondary)
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Produto?", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza que deseja excluir ${product.name}? Esta operação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(product.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiRed)
                ) {
                    Text("Excluir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}
