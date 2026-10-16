package com.example.ui.financial

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
import com.example.data.local.entities.TransactionEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(viewModel: MainViewModel) {
    var type by remember { mutableStateOf("INCOME") } // INCOME or EXPENSE
    var description by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(today) }
    var category by remember { mutableStateOf("Outros") }
    var paymentMethod by remember { mutableStateOf("Pix") }
    var status by remember { mutableStateOf("COMPLETED") } // COMPLETED or PLANNED
    var notes by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = if (type == "INCOME") {
        listOf("Venda de Estoque", "Sinal de Reserva", "Serviço / Manutenção", "Outros")
    } else {
        listOf("Compra de Estoque", "Peças e Reparo", "Transporte / Frete", "Embalagem", "Marketing", "Taxas Máquina", "Outros")
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Lançar Movimentação Financeira", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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

            // Type Toggle (Entrada vs Saída)
            Text(text = "Tipo de Operação:", fontSize = 13.sp, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        type = "INCOME"
                        category = "Venda de Estoque"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == "INCOME") BriqueiLime else DarkSurfaceVariant,
                        contentColor = if (type == "INCOME") Color.Black else TextSecondary
                    ),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ Entrada (Receita)", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        type = "EXPENSE"
                        category = "Peças e Reparo"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == "EXPENSE") BriqueiRed else DarkSurfaceVariant,
                        contentColor = if (type == "EXPENSE") Color.White else TextSecondary
                    ),
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("- Saída (Despesa)", fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição (Ex: Troca de tela iPhone 11)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Valor (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1.2f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Data") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Category Selection
            Text(text = "Categoria:", fontSize = 12.sp, color = TextSecondary)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                categories.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            // Payment Method
            Text(text = "Forma de Pagamento:", fontSize = 12.sp, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Pix", "Dinheiro", "Cartão Crédito", "Cartão Débito").forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        label = { Text(method, fontSize = 11.sp) }
                    )
                }
            }

            // Status (Realizada vs Prevista)
            Text(text = "Situação da Movimentação:", fontSize = 12.sp, color = TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = status == "COMPLETED",
                    onClick = { status = "COMPLETED" },
                    label = { Text("Realizada (Já paga/recebida)", fontWeight = FontWeight.Bold) }
                )
                FilterChip(
                    selected = status == "PLANNED",
                    onClick = { status = "PLANNED" },
                    label = { Text("Prevista (A receber/pagar)", fontWeight = FontWeight.Bold) }
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observações (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    if (description.isBlank()) {
                        errorMessage = "Por favor, digite a descrição da movimentação."
                        return@Button
                    }
                    if (amount <= 0) {
                        errorMessage = "Informe um valor maior que zero."
                        return@Button
                    }

                    viewModel.saveTransaction(
                        TransactionEntity(
                            type = type,
                            description = description,
                            amount = amount,
                            date = date,
                            category = category,
                            paymentMethod = paymentMethod,
                            status = status,
                            notes = notes
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "INCOME") BriqueiLime else BriqueiRed,
                    contentColor = if (type == "INCOME") Color.Black else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirmar Lançamento", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
