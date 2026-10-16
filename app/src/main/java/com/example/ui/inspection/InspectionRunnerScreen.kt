package com.example.ui.inspection

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionRunnerScreen(viewModel: MainViewModel) {
    val steps by viewModel.activeInspectionSteps.collectAsStateWithLifecycle()
    val model by viewModel.activeInspectionModel.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var currentStepIndex by remember { mutableStateOf(0) }
    val step = steps.getOrNull(currentStepIndex) ?: return

    val progress = ((currentStepIndex + 1).toFloat() / 12f)

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Vistoria: $model", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancelar", tint = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.finalizeInspection() }) {
                        Text("Finalizar", color = BriqueiCyan, fontWeight = FontWeight.Bold)
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
            // Step Progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ETAPA ${step.stepNumber} DE 12",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BriqueiCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% Concluído",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = BriqueiCyan,
                    trackColor = DarkSurfaceVariant
                )
            }

            // Step Card
            BriqueiCard(
                borderColor = BriqueiCyan.copy(alpha = 0.5f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BriqueiCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${step.stepNumber}",
                            fontWeight = FontWeight.Bold,
                            color = BriqueiCyan,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = step.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Como Testar:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = step.instruction,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BriqueiAmber.copy(alpha = 0.1f))
                        .border(1.dp, BriqueiAmber.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = BriqueiAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "O que reprova o item:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BriqueiAmber)
                            Text(text = step.whatToLookFor, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }

                // Interactive test trigger for vibration step
                if (step.stepNumber == 7) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator?.vibrate(300)
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BriqueiLime),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testar Motor de Vibração Agora")
                    }
                }
            }

            // Status selection
            Text(text = "Resultado desta Etapa:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusSelectButton(
                    label = "Aprovado",
                    icon = Icons.Default.Check,
                    color = BriqueiLime,
                    isSelected = step.status == "APPROVED",
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.updateStepStatus(step.stepNumber, "APPROVED")
                }

                StatusSelectButton(
                    label = "Atenção",
                    icon = Icons.Default.PriorityHigh,
                    color = BriqueiAmber,
                    isSelected = step.status == "ATTENTION",
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.updateStepStatus(step.stepNumber, "ATTENTION")
                }

                StatusSelectButton(
                    label = "Reprovado",
                    icon = Icons.Default.Close,
                    color = BriqueiRed,
                    isSelected = step.status == "FAILED",
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.updateStepStatus(step.stepNumber, "FAILED")
                }
            }

            // Step Note
            var noteInput by remember(step.stepNumber) { mutableStateOf(step.note) }
            OutlinedTextField(
                value = noteInput,
                onValueChange = {
                    noteInput = it
                    viewModel.updateStepStatus(step.stepNumber, step.status, it)
                },
                placeholder = { Text("Observação desta etapa (ex: Bateria em 83%, tela trocada...)", color = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                maxLines = 2
            )

            // Step Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentStepIndex > 0) {
                    OutlinedButton(
                        onClick = { currentStepIndex-- },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Anterior")
                    }
                }

                if (currentStepIndex < 11) {
                    Button(
                        onClick = { currentStepIndex++ },
                        colors = ButtonDefaults.buttonColors(containerColor = BriqueiCyan, contentColor = Color.Black),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Próxima Etapa", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = { viewModel.finalizeInspection() },
                        colors = ButtonDefaults.buttonColors(containerColor = BriqueiLime, contentColor = Color.Black),
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Concluir e Gerar Laudo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusSelectButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color else DarkSurface)
            .border(1.dp, if (isSelected) color else DarkBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else TextPrimary
            )
        }
    }
}
