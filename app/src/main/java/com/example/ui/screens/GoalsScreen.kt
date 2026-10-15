package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GoalEntity
import com.example.ui.components.ColorOptionPicker
import com.example.ui.components.DatePickerField
import com.example.ui.components.SectionHeader
import com.example.ui.components.displayToIsoDate
import com.example.ui.components.formatBRL
import com.example.ui.components.getCurrentDisplayDate
import com.example.ui.components.getCurrentIsoDate
import com.example.ui.components.isoToDisplayDate
import com.example.ui.components.parseColorHex
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@Composable
fun GoalsScreen(
    goals: List<GoalEntity>,
    onAddGoal: (name: String, description: String?, targetAmount: Double, targetDate: String?, colorHex: String) -> Unit,
    onAddContribution: (goalId: Long, amount: Double, type: String, date: String, description: String?) -> Unit,
    onDeleteGoal: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var activeGoalForContribution by remember { mutableStateOf<GoalEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_goal")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nova Meta")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            SectionHeader(
                title = "Metas Financeiras & Reservas",
                subtitle = "Objetivos de economia com tracking de aportes"
            )

            if (goals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma meta cadastrada. Clique no botão + para criar.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(goals, key = { it.id }) { goal ->
                        GoalItemCard(
                            goal = goal,
                            onAddContributionClick = { activeGoalForContribution = goal },
                            onDeleteClick = { onDeleteGoal(goal.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { name, desc, target, date, color ->
                onAddGoal(name, desc, target, date, color)
                showAddGoalDialog = false
            }
        )
    }

    activeGoalForContribution?.let { goal ->
        AddContributionDialog(
            goal = goal,
            onDismiss = { activeGoalForContribution = null },
            onConfirm = { amount, type, date, desc ->
                onAddContribution(goal.id, amount, type, date, desc)
                activeGoalForContribution = null
            }
        )
    }
}

@Composable
fun GoalItemCard(
    goal: GoalEntity,
    onAddContributionClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val goalColor = parseColorHex(goal.colorHex)
    val ratio = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val isCompleted = goal.status == "COMPLETED" || ratio >= 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(goalColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Flag,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!goal.targetDate.isNullOrEmpty()) {
                            Text(
                                text = "Prazo: ${isoToDisplayDate(goal.targetDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = ratio,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isCompleted) IncomeGreen else goalColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Acumulado: ${formatBRL(goal.currentAmount)} (${String.format("%.1f", ratio * 100)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCompleted) IncomeGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Alvo: ${formatBRL(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onAddContributionClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aporte / Resgate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?, targetAmount: Double, targetDate: String?, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetAmountStr by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#3B82F6") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Meta Financeira") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Meta (Ex: Reserva de Emergência) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetAmountStr,
                    onValueChange = { targetAmountStr = it },
                    label = { Text("Valor Alvo (R$) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                DatePickerField(
                    dateValue = targetDate,
                    onDateChange = { targetDate = it },
                    label = "Data Alvo (DD/MM/AAAA)",
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição / Observações") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Cor de Destaque:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                ColorOptionPicker(selectedColorHex = selectedColor, onColorSelected = { selectedColor = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetAmountStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val isoTargetDate = if (targetDate.isNotBlank()) displayToIsoDate(targetDate) else null
                    if (name.isNotBlank() && target > 0) {
                        onConfirm(name.trim(), description.ifEmpty { null }, target, isoTargetDate, selectedColor)
                    }
                }
            ) {
                Text("Salvar Meta")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddContributionDialog(
    goal: GoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, type: String, date: String, description: String?) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("CONTRIBUTION") } // CONTRIBUTION or WITHDRAWAL
    var displayDate by remember { mutableStateOf(getCurrentDisplayDate()) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aporte ou Resgate - ${goal.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { type = "CONTRIBUTION" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "CONTRIBUTION") IncomeGreen else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Aporte", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { type = "WITHDRAWAL" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "WITHDRAWAL") ExpenseRed else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("- Resgate", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Valor (R$) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                DatePickerField(
                    dateValue = displayDate,
                    onDateChange = { displayDate = it },
                    label = "Data da Operação (DD/MM/AAAA) *",
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Observação") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val isoDate = displayToIsoDate(displayDate)
                    if (amt > 0) {
                        onConfirm(amt, type, isoDate, description.ifEmpty { null })
                    }
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
