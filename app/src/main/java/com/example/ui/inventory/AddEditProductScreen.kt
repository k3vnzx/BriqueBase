package com.example.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ProductEntity
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(viewModel: MainViewModel) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Smartphones") }
    var brand by remember { mutableStateOf("Apple") }
    var model by remember { mutableStateOf("") }
    var internalCode by remember { mutableStateOf("BRQ-${Random().nextInt(9000) + 1000}") }
    var imei by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Seminovo Excelente") }
    var purchasePriceInput by remember { mutableStateOf("") }
    var additionalCostsInput by remember { mutableStateOf("0.00") }
    var announcedPriceInput by remember { mutableStateOf("") }
    var minPriceInput by remember { mutableStateOf("") }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var purchaseDate by remember { mutableStateOf(today) }
    var origin by remember { mutableStateOf("Marketplace") }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Live margin calculations
    val purchasePrice = purchasePriceInput.toDoubleOrNull() ?: 0.0
    val additionalCosts = additionalCostsInput.toDoubleOrNull() ?: 0.0
    val announcedPrice = announcedPriceInput.toDoubleOrNull() ?: 0.0
    val totalCost = purchasePrice + additionalCosts
    val estimatedProfit = announcedPrice - totalCost
    val marginPercent = if (totalCost > 0) (estimatedProfit / totalCost) * 100 else 0.0

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Cadastrar Produto no Estoque", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = BriqueiRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Margin live preview banner
            BriqueiCard(
                backgroundColor = DarkSurfaceVariant,
                borderColor = if (estimatedProfit >= 0) BriqueiLime.copy(alpha = 0.4f) else BriqueiRed.copy(alpha = 0.4f)
            ) {
                Text(text = "CÁLCULO AUTOMÁTICO DE MARGEM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Custo Total", fontSize = 11.sp, color = TextMuted)
                        Text(text = "R$ ${"%.2f".format(totalCost)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Preço Venda", fontSize = 11.sp, color = TextMuted)
                        Text(text = "R$ ${"%.2f".format(announcedPrice)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Lucro Líquido Estimado", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "+ R$ ${"%.2f".format(estimatedProfit)} (${marginPercent.toInt()}%)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (estimatedProfit >= 0) BriqueiLime else BriqueiRed
                        )
                    }
                }
            }

            // Basic Info
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Produto (Ex: iPhone 13 128GB Estelar)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Marca") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Modelo") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categoria") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Estado de Conservação") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = internalCode,
                    onValueChange = { internalCode = it },
                    label = { Text("Código / SKU") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = imei,
                    onValueChange = { imei = it },
                    label = { Text("IMEI / Serial (Opcional)") },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true
                )
            }

            // Financial Values
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = purchasePriceInput,
                    onValueChange = { purchasePriceInput = it },
                    label = { Text("Preço de Compra (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = additionalCostsInput,
                    onValueChange = { additionalCostsInput = it },
                    label = { Text("Custos Extras (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = announcedPriceInput,
                    onValueChange = { announcedPriceInput = it },
                    label = { Text("Preço Anunciado (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = minPriceInput,
                    onValueChange = { minPriceInput = it },
                    label = { Text("Preço Mínimo (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    label = { Text("Origem (Ex: Marketplace)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = purchaseDate,
                    onValueChange = { purchaseDate = it },
                    label = { Text("Data da Compra") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição e Detalhes do Produto") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observações Internas (Não aparecem no anúncio)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Por favor, informe o nome do produto."
                        return@Button
                    }
                    if (purchasePrice <= 0) {
                        errorMessage = "Informe um preço de compra válido."
                        return@Button
                    }
                    if (announcedPrice <= 0) {
                        errorMessage = "Informe um preço anunciado válido."
                        return@Button
                    }

                    val newProduct = ProductEntity(
                        name = name,
                        category = category.ifBlank { "Smartphones" },
                        brand = brand.ifBlank { "Apple" },
                        model = model.ifBlank { name },
                        internalCode = internalCode,
                        imei = imei.ifBlank { null },
                        condition = condition,
                        description = description,
                        purchasePrice = purchasePrice,
                        additionalCosts = additionalCosts,
                        announcedPrice = announcedPrice,
                        minSalePrice = minPriceInput.toDoubleOrNull() ?: (totalCost + 50.0),
                        purchaseDate = purchaseDate,
                        origin = origin,
                        notes = notes,
                        status = "AVAILABLE"
                    )
                    viewModel.saveProduct(newProduct)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BriqueiLime, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar Produto no Estoque", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
