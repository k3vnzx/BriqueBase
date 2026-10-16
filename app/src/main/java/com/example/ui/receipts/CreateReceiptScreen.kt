package com.example.ui.receipts

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.ReceiptEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReceiptScreen(viewModel: MainViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()

    var sellerName by remember(profile) { mutableStateOf(profile?.businessName ?: "BRIQUEI Imports") }
    var sellerPhone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var sellerDocument by remember { mutableStateOf("12.345.678/0001-90") }

    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var buyerDocument by remember { mutableStateOf("") }

    var productName by remember { mutableStateOf("") }
    var brandModel by remember { mutableStateOf("") }
    var imeiOrSerial by remember { mutableStateOf("") }
    var totalAmountInput by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Pix") }
    var paymentStatus by remember { mutableStateOf("PAGO") }
    var warrantyTerms by remember {
        mutableStateOf("Garantia de 90 dias contra defeitos de funcionamento elétrico/placa. Não cobre quedas, vidros trincados ou danos por água.")
    }
    val today = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(today) }
    var notes by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Emitir Novo Comprovante", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
                Text(text = errorMessage!!, color = BriqueiRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // Quick Fill from Stock
            if (products.isNotEmpty()) {
                Text(text = "Preenchimento Rápido pelo Estoque:", fontSize = 12.sp, color = TextSecondary)
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Selecionar produto do estoque...")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text("${prod.name} (R$ ${"%.2f".format(prod.announcedPrice)})") },
                                onClick = {
                                    productName = prod.name
                                    brandModel = "${prod.brand} ${prod.model}"
                                    imeiOrSerial = prod.imei ?: ""
                                    totalAmountInput = (if (prod.status == "SOLD") prod.soldPrice ?: prod.announcedPrice else prod.announcedPrice).toString()
                                    buyerName = prod.buyerName ?: buyerName
                                    buyerPhone = prod.buyerPhone ?: buyerPhone
                                    paymentMethod = prod.paymentMethod ?: paymentMethod
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Product Details
            Text(text = "Dados do Aparelho Vendido:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BriqueiLime)

            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Nome do Aparelho (Ex: iPhone 13 128GB Estelar)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = imeiOrSerial,
                    onValueChange = { imeiOrSerial = it },
                    label = { Text("IMEI / Número de Série") },
                    singleLine = true,
                    modifier = Modifier.weight(1.3f)
                )
                OutlinedTextField(
                    value = totalAmountInput,
                    onValueChange = { totalAmountInput = it },
                    label = { Text("Valor Total (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Forma de Pagamento (Ex: Pix)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Buyer Details
            Text(text = "Dados do Comprador:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BriqueiCyan)

            OutlinedTextField(
                value = buyerName,
                onValueChange = { buyerName = it },
                label = { Text("Nome Completo do Cliente") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = buyerPhone,
                    onValueChange = { buyerPhone = it },
                    label = { Text("WhatsApp / Telefone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = buyerDocument,
                    onValueChange = { buyerDocument = it },
                    label = { Text("CPF (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Warranty Terms
            Text(text = "Termo de Garantia e Condições:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BriqueiAmber)
            OutlinedTextField(
                value = warrantyTerms,
                onValueChange = { warrantyTerms = it },
                label = { Text("Cláusula de Garantia") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observações (Acessórios entregues, local de entrega...)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val amount = totalAmountInput.toDoubleOrNull() ?: 0.0
                    if (productName.isBlank()) {
                        errorMessage = "Informe o produto vendido."
                        return@Button
                    }
                    if (buyerName.isBlank()) {
                        errorMessage = "Informe o nome do comprador."
                        return@Button
                    }
                    if (amount <= 0) {
                        errorMessage = "Informe um valor de venda válido."
                        return@Button
                    }

                    viewModel.saveReceipt(
                        ReceiptEntity(
                            receiptNumber = "", // Auto-generated
                            sellerName = sellerName,
                            sellerPhone = sellerPhone,
                            sellerDocument = sellerDocument,
                            buyerName = buyerName,
                            buyerPhone = buyerPhone,
                            buyerDocument = buyerDocument,
                            productName = productName,
                            brandModel = brandModel,
                            imeiOrSerial = imeiOrSerial,
                            totalAmount = amount,
                            paymentMethod = paymentMethod,
                            paymentStatus = paymentStatus,
                            warrantyTerms = warrantyTerms,
                            date = date,
                            notes = notes
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BriqueiLime, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gerar e Visualizar Comprovante", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
