package com.example.ui.inspection

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionResultScreen(viewModel: MainViewModel) {
    val selectedInspection by viewModel.selectedInspection.collectAsStateWithLifecycle()
    val inspection = selectedInspection
    val context = LocalContext.current

    if (inspection == null) {
        Box(modifier = Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
            Text("Nenhum laudo selecionado.", color = TextSecondary)
        }
        return
    }

    val (decisionTitle, decisionDesc, decisionColor, decisionIcon) = when (inspection.recommendation) {
        "ABORT" -> Quadruple(
            "INTERROMPER NEGOCIAÇÃO",
            "Foram detectadas reprovações críticas (como bloqueio de ativação, IMEI ou defeito estrutural grave). Não compre este aparelho.",
            BriqueiRed,
            Icons.Default.Cancel
        )
        "CAUTION" -> Quadruple(
            "NEGOCIAR COM CAUTELA",
            "O aparelho possui itens que exigem atenção (ex: bateria baixa ou tela substituída). Negocie desconto proporcional antes de fechar.",
            BriqueiAmber,
            Icons.Default.Warning
        )
        else -> Quadruple(
            "CONTINUAR AVALIANDO COM SEGURANÇA",
            "Aparelho aprovado em todos os testes principais presenciais. Proceda com a compra com cautela e conferência de documentos.",
            BriqueiLime,
            Icons.Default.CheckCircle
        )
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Relatório da Vistoria", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.INSPECTION) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val reportText = buildString {
                            appendLine("📋 LAUDO DE VISTORIA ANTIGOLPE - BRIQUEI")
                            appendLine("Dispositivo: ${inspection.deviceModel}")
                            if (inspection.imeiOrSerial.isNotBlank()) appendLine("IMEI/Serial: ${inspection.imeiOrSerial}")
                            appendLine("Data: ${inspection.date}")
                            appendLine("Parecer Técnico: $decisionTitle")
                            appendLine("Aprovados: ${inspection.totalApproved}/12 | Atenção: ${inspection.totalAttention} | Reprovados: ${inspection.totalFailed}")
                            appendLine("\n*Aviso Legal: A vistoria presencial reduz riscos mas não garante 100% contra bloqueios futuros de operadora.*")
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Laudo de Vistoria"))
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Compartilhar", tint = BriqueiLime)
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
            // Recommendation Banner
            BriqueiCard(
                backgroundColor = DarkSurfaceVariant,
                borderColor = decisionColor.copy(alpha = 0.5f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(decisionColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = decisionIcon, contentDescription = null, tint = decisionColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "DECISÃO RECOMENDADA:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = decisionColor)
                        Text(text = decisionTitle, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = decisionDesc, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
            }

            // Summary Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResultMetricCard("Aprovados", "${inspection.totalApproved}", BriqueiLime, Modifier.weight(1f))
                ResultMetricCard("Atenção", "${inspection.totalAttention}", BriqueiAmber, Modifier.weight(1f))
                ResultMetricCard("Reprovados", "${inspection.totalFailed}", BriqueiRed, Modifier.weight(1f))
            }

            // Mandatory Disclaimer Warning Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(14.dp)
            ) {
                Row {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = BriqueiCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AVISO LEGAL IMPORTANTE: Testes físicos presenciais não garantem 100% que o aparelho não receberá bloqueio posterior de operadora (ex: inadimplência em planos pós-pagos ou sinistro fraudulento). Sempre exija nota fiscal e verifique a procedência do vendedor.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            // Device Info Details
            BriqueiCard {
                SectionHeader(title = "Dados da Vistoria")
                Text(text = "Aparelho: ${inspection.deviceModel}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (inspection.imeiOrSerial.isNotBlank()) {
                    Text(text = "IMEI / Serial: ${inspection.imeiOrSerial}", fontSize = 13.sp, color = TextSecondary)
                }
                if (inspection.sellerName.isNotBlank()) {
                    Text(text = "Vendedor: ${inspection.sellerName}", fontSize = 13.sp, color = TextSecondary)
                }
                Text(text = "Data e Hora: ${inspection.date}", fontSize = 12.sp, color = TextMuted)

                if (inspection.generalNotes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Notas gerais:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(text = inspection.generalNotes, fontSize = 13.sp, color = TextPrimary)
                }
            }

            // Share & Done Buttons
            Button(
                onClick = {
                    val reportText = buildString {
                        appendLine("📋 LAUDO DE VISTORIA ANTIGOLPE - BRIQUEI")
                        appendLine("Dispositivo: ${inspection.deviceModel}")
                        appendLine("Data: ${inspection.date}")
                        appendLine("Parecer Técnico: $decisionTitle")
                        appendLine("Aprovados: ${inspection.totalApproved}/12 | Atenção: ${inspection.totalAttention} | Reprovados: ${inspection.totalFailed}")
                        appendLine("\n*Aviso Legal: A vistoria presencial reduz riscos mas não substitui laudo pericial oficial.*")
                    }
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, reportText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Laudo"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = BriqueiCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compartilhar Laudo com o Vendedor / Comprador", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.navigateTo(AppScreen.INSPECTION) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Voltar ao Menu de Vistorias")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun ResultMetricCard(
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
        }
    }
}
