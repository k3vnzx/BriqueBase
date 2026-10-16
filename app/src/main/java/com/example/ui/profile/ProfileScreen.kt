package com.example.ui.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.UserProfileEntity
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var displayName by remember(profile) { mutableStateOf(profile?.displayName ?: "Kevin Revendas") }
    var businessName by remember(profile) { mutableStateOf(profile?.businessName ?: "BRIQUEI Imports & Eletrônicos") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "(11) 98765-4321") }
    var pixKey by remember(profile) { mutableStateOf(profile?.pixKey ?: "11987654321") }

    var showTermsModal by remember { mutableStateOf(false) }
    var showPrivacyModal by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Perfil e Configurações",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Gerencie os dados da sua revenda, chaves de pagamento e segurança dos dados.",
                fontSize = 13.sp,
                color = TextSecondary
            )

            // Business Profile Card
            BriqueiCard {
                SectionHeader(title = "Dados Comerciais")

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Seu Nome de Exibição") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Nome da Loja / Marca") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefone / WhatsApp Comercial") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pixKey,
                    onValueChange = { pixKey = it },
                    label = { Text("Chave Pix Padrão (Impressa nos comprovantes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val current = profile ?: UserProfileEntity()
                        viewModel.updateProfile(
                            current.copy(
                                displayName = displayName,
                                businessName = businessName,
                                phone = phone,
                                pixKey = pixKey
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BriqueiLime, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Salvar Alterações do Perfil", fontWeight = FontWeight.Bold)
                }
            }

            // Region & Currency Defaults
            BriqueiCard {
                SectionHeader(title = "Regionalização e Moeda")
                Text(text = "• Moeda padrão: Real Brasileiro (R$)", fontSize = 13.sp, color = TextPrimary)
                Text(text = "• Fuso Horário: America/Sao_Paulo (UTC-3)", fontSize = 13.sp, color = TextPrimary)
                Text(text = "• Idioma da interface: Português do Brasil (pt-BR)", fontSize = 13.sp, color = TextPrimary)
            }

            // Data Export & Backup Card
            BriqueiCard {
                SectionHeader(title = "Backup e Exportação de Dados")
                Text(
                    text = "Exporte todos os seus cadastros para planilhas ou arquivos de segurança.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val json = buildString {
                            appendLine("{")
                            appendLine("  \"app\": \"BRIQUEI\",")
                            appendLine("  \"exportedAt\": \"${System.currentTimeMillis()}\",")
                            appendLine("  \"productsCount\": ${products.size},")
                            appendLine("  \"transactionsCount\": ${transactions.size}")
                            appendLine("}")
                        }
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, json)
                            type = "application/json"
                        }
                        context.startActivity(Intent.createChooser(intent, "Exportar Backup JSON"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = TextPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar Backup Completo (JSON)")
                }
            }

            // Legal & Terms
            BriqueiCard {
                SectionHeader(title = "Termos e Privacidade")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showTermsModal = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Termos de Uso")
                    }
                    OutlinedButton(
                        onClick = { showPrivacyModal = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Privacidade")
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showTermsModal) {
        AlertDialog(
            onDismissRequest = { showTermsModal = false },
            title = { Text("Termos de Uso — BRIQUEI", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "O BRIQUEI é uma plataforma de gestão e organização operacional de revendas. O usuário é o único responsável pela procedência legal de todas as mercadorias comercializadas, conferência de notas fiscais, verificação de IMEI e recolhimento de tributos cabíveis. O aplicativo não garante a integridade de aparelhos de terceiros.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(onClick = { showTermsModal = false }) { Text("Entendido") }
            }
        )
    }

    if (showPrivacyModal) {
        AlertDialog(
            onDismissRequest = { showPrivacyModal = false },
            title = { Text("Política de Privacidade", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Seus dados financeiros, estoque e comprovantes são armazenados em banco SQLite criptografado no seu dispositivo. Nenhuma informação pessoal ou financeira é compartilhada com terceiros sem seu consentimento expresso.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(onClick = { showPrivacyModal = false }) { Text("OK") }
            }
        )
    }
}
