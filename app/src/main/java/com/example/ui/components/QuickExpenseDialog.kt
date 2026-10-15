package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CreditCardEntity
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TransferBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExpenseDialog(
    initialType: String = "EXPENSE", // EXPENSE, INCOME, TRANSFER
    accounts: List<AccountEntity>,
    creditCards: List<CreditCardEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirmAccountExpense: (amount: Double, description: String, date: String, accountId: Long, categoryId: Long?, notes: String?) -> Unit,
    onConfirmCardExpense: (cardId: Long, amount: Double, purchaseDate: String, description: String, categoryId: Long?) -> Unit,
    onConfirmIncome: (amount: Double, description: String, date: String, accountId: Long, categoryId: Long?, notes: String?) -> Unit = { _, _, _, _, _, _ -> },
    onConfirmTransfer: (amount: Double, description: String, date: String, sourceAccountId: Long, destAccountId: Long, notes: String?) -> Unit = { _, _, _, _, _, _ -> }
) {
    var activeType by remember { mutableStateOf(initialType) } // EXPENSE, INCOME, TRANSFER
    var rawDigits by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val activeExpenseCategories = categories.filter { it.type == "EXPENSE" && it.isActive }
    val activeIncomeCategories = categories.filter { it.type == "INCOME" && it.isActive }

    var selectedCategoryId by remember(activeType) {
        mutableStateOf(
            if (activeType == "EXPENSE") activeExpenseCategories.firstOrNull()?.id
            else if (activeType == "INCOME") activeIncomeCategories.firstOrNull()?.id
            else null
        )
    }

    var paymentType by remember { mutableStateOf("ACCOUNT") } // ACCOUNT or CARD
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var destAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id ?: 0L) }
    var selectedCardId by remember { mutableStateOf(creditCards.firstOrNull()?.id ?: 0L) }

    var expandedAccountDropdown by remember { mutableStateOf(false) }
    var expandedDestAccountDropdown by remember { mutableStateOf(false) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    val formattedAmount = formatRawDigits(rawDigits)
    val numericAmount = parseRawDigitsToDouble(rawDigits)

    val activeColor = when (activeType) {
        "INCOME" -> IncomeGreen
        "TRANSFER" -> TransferBlue
        else -> ExpenseRed
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lançamento Rápido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                // Segmented Tabs: Despesa | Receita | Transferência
                TabRow(
                    selectedTabIndex = when (activeType) {
                        "INCOME" -> 1
                        "TRANSFER" -> 2
                        else -> 0
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentColor = activeColor
                ) {
                    Tab(
                        selected = activeType == "EXPENSE",
                        onClick = { activeType = "EXPENSE" },
                        text = { Text("Despesa", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeType == "INCOME",
                        onClick = { activeType = "INCOME" },
                        text = { Text("Receita", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeType == "TRANSFER",
                        onClick = { activeType = "TRANSFER" },
                        text = { Text("Transf.", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Large Amount Display with Sign
                Text(
                    text = when (activeType) {
                        "INCOME" -> "VALOR DA RECEITA"
                        "TRANSFER" -> "VALOR DA TRANSFERÊNCIA"
                        else -> "VALOR DA DESPESA"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )

                val displayPrefix = when (activeType) {
                    "INCOME" -> "+ "
                    "EXPENSE" -> "- "
                    else -> ""
                }

                // Direct editable input field for the amount
                OutlinedTextField(
                    value = rawDigits,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == ',' || it == '.' }
                            .replace(".", ",")
                        // Limit to at most one comma and 2 decimal places
                        val commaCount = filtered.count { it == ',' }
                        if (commaCount <= 1) {
                            if (filtered.contains(",")) {
                                val before = filtered.substringBefore(",")
                                val after = filtered.substringAfter(",")
                                if (after.length <= 2 && before.length <= 7) {
                                    rawDigits = filtered
                                }
                            } else {
                                if (filtered.length <= 7) {
                                    rawDigits = filtered
                                }
                            }
                        }
                    },
                    label = { Text("Digitar Valor (R$) *") },
                    placeholder = { Text("0,00") },
                    prefix = { Text("R$ ", fontWeight = FontWeight.Bold, color = activeColor) },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = activeColor,
                        textAlign = TextAlign.Start
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_quick_amount")
                )

                Text(
                    text = "$displayPrefix$formattedAmount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = activeColor.copy(alpha = 0.85f),
                    modifier = Modifier.testTag("display_quick_amount")
                )

                // Optional Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = {
                        Text(
                            text = when (activeType) {
                                "INCOME" -> "Descrição da receita (Ex: Salário)"
                                "TRANSFER" -> "Descrição da transferência"
                                else -> "Descrição da despesa (opcional)"
                            },
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Mode-specific payment/account selector
                if (activeType == "EXPENSE") {
                    // Payment Method Selector for Expense: Conta vs Cartão
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            selected = paymentType == "ACCOUNT",
                            onClick = { paymentType = "ACCOUNT" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (paymentType == "ACCOUNT") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(imageVector = Icons.Default.Wallet, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Conta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (creditCards.isNotEmpty()) {
                            Surface(
                                selected = paymentType == "CARD",
                                onClick = { paymentType = "CARD" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (paymentType == "CARD") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cartão", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Account / Card Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedAccountDropdown,
                        onExpandedChange = { expandedAccountDropdown = !expandedAccountDropdown }
                    ) {
                        OutlinedTextField(
                            value = if (paymentType == "ACCOUNT") {
                                accounts.find { it.id == selectedAccountId }?.name ?: "Selecione Conta"
                            } else {
                                creditCards.find { it.id == selectedCardId }?.name ?: "Selecione Cartão"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (paymentType == "ACCOUNT") "Conta de Débito" else "Cartão de Crédito") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAccountDropdown,
                            onDismissRequest = { expandedAccountDropdown = false }
                        ) {
                            if (paymentType == "ACCOUNT") {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text("${acc.name} (${formatBRL(acc.currentBalance)})") },
                                        onClick = {
                                            selectedAccountId = acc.id
                                            expandedAccountDropdown = false
                                        }
                                    )
                                }
                            } else {
                                creditCards.forEach { card ->
                                    DropdownMenuItem(
                                        text = { Text("${card.name} (Disp: ${formatBRL(card.availableLimit)})") },
                                        onClick = {
                                            selectedCardId = card.id
                                            expandedAccountDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Category dropdown for Expense
                    if (activeExpenseCategories.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedCategoryDropdown,
                            onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown }
                        ) {
                            OutlinedTextField(
                                value = activeExpenseCategories.find { it.id == selectedCategoryId }?.name ?: "Sem Categoria",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoria de Despesa") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedCategoryDropdown,
                                onDismissRequest = { expandedCategoryDropdown = false }
                            ) {
                                activeExpenseCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategoryId = cat.id
                                            expandedCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else if (activeType == "INCOME") {
                    // Income: Account Dropdown (where the money goes)
                    ExposedDropdownMenuBox(
                        expanded = expandedAccountDropdown,
                        onExpandedChange = { expandedAccountDropdown = !expandedAccountDropdown }
                    ) {
                        OutlinedTextField(
                            value = accounts.find { it.id == selectedAccountId }?.name ?: "Selecione Conta",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Conta de Destino (Recebimento) *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAccountDropdown,
                            onDismissRequest = { expandedAccountDropdown = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.name} (${formatBRL(acc.currentBalance)})") },
                                    onClick = {
                                        selectedAccountId = acc.id
                                        expandedAccountDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Category dropdown for Income
                    if (activeIncomeCategories.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedCategoryDropdown,
                            onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown }
                        ) {
                            OutlinedTextField(
                                value = activeIncomeCategories.find { it.id == selectedCategoryId }?.name ?: "Sem Categoria",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoria de Receita") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedCategoryDropdown,
                                onDismissRequest = { expandedCategoryDropdown = false }
                            ) {
                                activeIncomeCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategoryId = cat.id
                                            expandedCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Transfer: Source Account + Destination Account
                    ExposedDropdownMenuBox(
                        expanded = expandedAccountDropdown,
                        onExpandedChange = { expandedAccountDropdown = !expandedAccountDropdown }
                    ) {
                        OutlinedTextField(
                            value = accounts.find { it.id == selectedAccountId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Conta de Origem (Debitar) *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedAccountDropdown,
                            onDismissRequest = { expandedAccountDropdown = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.name} (${formatBRL(acc.currentBalance)})") },
                                    onClick = {
                                        selectedAccountId = acc.id
                                        expandedAccountDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = expandedDestAccountDropdown,
                        onExpandedChange = { expandedDestAccountDropdown = !expandedDestAccountDropdown }
                    ) {
                        OutlinedTextField(
                            value = accounts.find { it.id == destAccountId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Conta de Destino (Creditar) *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDestAccountDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDestAccountDropdown,
                            onDismissRequest = { expandedDestAccountDropdown = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text("${acc.name} (${formatBRL(acc.currentBalance)})") },
                                    onClick = {
                                        destAccountId = acc.id
                                        expandedDestAccountDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Keypad 3x4
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf(",", "0", "DEL")
                    )

                    keys.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            when (key) {
                                                "DEL" -> {
                                                    if (rawDigits.isNotEmpty()) {
                                                        rawDigits = rawDigits.dropLast(1)
                                                    }
                                                }
                                                "," -> {
                                                    if (!rawDigits.contains(",")) {
                                                        rawDigits += if (rawDigits.isEmpty()) "0," else ","
                                                    }
                                                }
                                                else -> {
                                                    if (rawDigits.contains(",")) {
                                                        val dec = rawDigits.substringAfter(",")
                                                        if (dec.length < 2) rawDigits += key
                                                    } else {
                                                        if (rawDigits.length < 7) rawDigits += key
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (numericAmount > 0) {
                        val date = getCurrentIsoDate()
                        when (activeType) {
                            "EXPENSE" -> {
                                val cat = activeExpenseCategories.find { it.id == selectedCategoryId }
                                val desc = description.ifEmpty { cat?.name ?: "Despesa" }
                                if (paymentType == "ACCOUNT") {
                                    onConfirmAccountExpense(numericAmount, desc, date, selectedAccountId, selectedCategoryId, null)
                                } else {
                                    onConfirmCardExpense(selectedCardId, numericAmount, date, desc, selectedCategoryId)
                                }
                            }
                            "INCOME" -> {
                                val cat = activeIncomeCategories.find { it.id == selectedCategoryId }
                                val desc = description.ifEmpty { cat?.name ?: "Receita" }
                                onConfirmIncome(numericAmount, desc, date, selectedAccountId, selectedCategoryId, null)
                            }
                            "TRANSFER" -> {
                                val desc = description.ifEmpty { "Transferência entre contas" }
                                onConfirmTransfer(numericAmount, desc, date, selectedAccountId, destAccountId, null)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_save_quick_transaction"),
                colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                val actionLabel = when (activeType) {
                    "INCOME" -> "Salvar Receita (+ ${formatBRL(numericAmount)})"
                    "TRANSFER" -> "Confirmar Transferência (${formatBRL(numericAmount)})"
                    else -> "Salvar Despesa (- ${formatBRL(numericAmount)})"
                }
                Text(text = actionLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = null
    )
}

fun formatRawDigits(raw: String): String {
    if (raw.isEmpty()) return "R$ 0,00"
    val doubleVal = parseRawDigitsToDouble(raw)
    return formatBRL(doubleVal)
}

fun parseRawDigitsToDouble(raw: String): Double {
    if (raw.isEmpty()) return 0.0
    val clean = raw.replace(",", ".")
    return clean.toDoubleOrNull() ?: 0.0
}
