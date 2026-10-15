package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.TransactionEntity
import com.example.ui.components.DatePickerField
import com.example.ui.components.SectionHeader
import com.example.ui.components.displayToIsoDate
import com.example.ui.components.formatBRL
import com.example.ui.components.getCurrentDisplayDate
import com.example.ui.components.getCurrentIsoDate
import com.example.ui.components.isoToDisplayDate
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TransferBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    onAddIncome: (amount: Double, description: String, date: String, accountId: Long, categoryId: Long?, notes: String?) -> Unit,
    onAddExpense: (amount: Double, description: String, date: String, accountId: Long, categoryId: Long?, notes: String?) -> Unit,
    onAddTransfer: (amount: Double, description: String, date: String, sourceAccountId: Long, destAccountId: Long, notes: String?) -> Unit,
    onRevertTransaction: (transactionId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterType by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE, TRANSFER
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var showAddDialogType by remember { mutableStateOf<String?>(null) } // INCOME, EXPENSE, TRANSFER
    var revertingTxId by remember { mutableStateOf<Long?>(null) }

    val filteredTxs = transactions.filter { tx ->
        val typeMatch = filterType == "ALL" || tx.type == filterType
        val accMatch = selectedAccountId == null || tx.accountId == selectedAccountId || tx.destinationAccountId == selectedAccountId
        typeMatch && accMatch
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialogType = "EXPENSE" },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_transaction")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nova Transação")
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
                title = "Lançamentos Financeiros",
                subtitle = "Extrato unificado de movimentações"
            )

            // Type Filter Chips & Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = filterType == "ALL",
                    onClick = { filterType = "ALL" },
                    label = { Text("Todas", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = filterType == "EXPENSE",
                    onClick = { filterType = "EXPENSE" },
                    label = { Text("Despesas", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = filterType == "INCOME",
                    onClick = { filterType = "INCOME" },
                    label = { Text("Receitas", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = filterType == "TRANSFER",
                    onClick = { filterType = "TRANSFER" },
                    label = { Text("Transf.", fontSize = 12.sp) }
                )
            }

            // Quick Add Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { showAddDialogType = "EXPENSE" },
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Despesa",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { showAddDialogType = "INCOME" },
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Receita",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = { showAddDialogType = "TRANSFER" },
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Transf.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredTxs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma movimentação encontrada.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTxs, key = { it.id }) { tx ->
                        val accountName = accounts.find { it.id == tx.accountId }?.name ?: "Conta"
                        val destAccountName = tx.destinationAccountId?.let { id -> accounts.find { it.id == id }?.name }
                        val categoryName = categories.find { it.id == tx.categoryId }?.name

                        TransactionCardDetail(
                            transaction = tx,
                            accountName = accountName,
                            destAccountName = destAccountName,
                            categoryName = categoryName,
                            onRevertClick = { revertingTxId = tx.id }
                        )
                    }
                }
            }
        }
    }

    // Add Transaction Dialogs
    showAddDialogType?.let { type ->
        AddTransactionDialog(
            type = type,
            accounts = accounts,
            categories = categories,
            onDismiss = { showAddDialogType = null },
            onConfirmIncome = { amt, desc, date, accId, catId, notes ->
                onAddIncome(amt, desc, date, accId, catId, notes)
                showAddDialogType = null
            },
            onConfirmExpense = { amt, desc, date, accId, catId, notes ->
                onAddExpense(amt, desc, date, accId, catId, notes)
                showAddDialogType = null
            },
            onConfirmTransfer = { amt, desc, date, srcId, dstId, notes ->
                onAddTransfer(amt, desc, date, srcId, dstId, notes)
                showAddDialogType = null
            }
        )
    }

    revertingTxId?.let { id ->
        AlertDialog(
            onDismissRequest = { revertingTxId = null },
            title = { Text("Estornar Lançamento?") },
            text = { Text("O valor será estornado e o saldo da conta será recomposto automaticamente.") },
            confirmButton = {
                Button(
                    onClick = {
                        onRevertTransaction(id)
                        revertingTxId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Confirmar Estorno")
                }
            },
            dismissButton = {
                TextButton(onClick = { revertingTxId = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TransactionCardDetail(
    transaction: TransactionEntity,
    accountName: String,
    destAccountName: String?,
    categoryName: String?,
    onRevertClick: () -> Unit
) {
    val isIncome = transaction.type == "INCOME"
    val isExpense = transaction.type == "EXPENSE"
    val isCancelled = transaction.status == "CANCELLED"

    val iconColor = when {
        isCancelled -> Color.Gray
        isIncome -> IncomeGreen
        isExpense -> ExpenseRed
        else -> TransferBlue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isIncome -> Icons.Default.ArrowDownward
                            isExpense -> Icons.Default.ArrowUpward
                            else -> Icons.Default.SwapHoriz
                        },
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isCancelled) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            transaction.type == "TRANSFER" -> "De $accountName para $destAccountName"
                            categoryName != null -> "$accountName • $categoryName"
                            else -> accountName
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = isoToDisplayDate(transaction.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+ " else if (isExpense) "- " else ""}${formatBRL(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCancelled) Color.Gray else iconColor
                )

                if (!isCancelled) {
                    IconButton(
                        onClick = onRevertClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateLeft,
                            contentDescription = "Estornar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Text(
                        text = "CANCELADO",
                        style = MaterialTheme.typography.labelSmall,
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    type: String, // INCOME, EXPENSE, TRANSFER
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirmIncome: (amount: Double, description: String, date: String, accountId: Long, categoryId: Long?, notes: String?) -> Unit,
    onConfirmExpense: (amount: Double, description: String, date: String, accountId: Long, categoryId: Long?, notes: String?) -> Unit,
    onConfirmTransfer: (amount: Double, description: String, date: String, sourceAccountId: Long, destAccountId: Long, notes: String?) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var displayDate by remember { mutableStateOf(getCurrentDisplayDate()) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var destAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id ?: 0L) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var notes by remember { mutableStateOf("") }

    var expandedAccountDropdown by remember { mutableStateOf(false) }
    var expandedDestAccountDropdown by remember { mutableStateOf(false) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    val filteredCategories = categories.filter { it.type == type && it.isActive }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (type) {
                    "INCOME" -> "Nova Receita"
                    "EXPENSE" -> "Nova Despesa"
                    else -> "Nova Transferência"
                }
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Valor (R$) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição *") },
                    modifier = Modifier.fillMaxWidth()
                )

                DatePickerField(
                    dateValue = displayDate,
                    onDateChange = { displayDate = it },
                    label = "Data da Transação (DD/MM/AAAA) *",
                    modifier = Modifier.fillMaxWidth()
                )

                if (type == "TRANSFER") {
                    // Source Account
                    ExposedDropdownMenuBox(
                        expanded = expandedAccountDropdown,
                        onExpandedChange = { expandedAccountDropdown = !expandedAccountDropdown }
                    ) {
                        OutlinedTextField(
                            value = accounts.find { it.id == selectedAccountId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Conta de Origem *") },
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
                                    text = { Text(acc.name) },
                                    onClick = {
                                        selectedAccountId = acc.id
                                        expandedAccountDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Destination Account
                    ExposedDropdownMenuBox(
                        expanded = expandedDestAccountDropdown,
                        onExpandedChange = { expandedDestAccountDropdown = !expandedDestAccountDropdown }
                    ) {
                        OutlinedTextField(
                            value = accounts.find { it.id == destAccountId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Conta de Destino *") },
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
                                    text = { Text(acc.name) },
                                    onClick = {
                                        destAccountId = acc.id
                                        expandedDestAccountDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Single Account
                    ExposedDropdownMenuBox(
                        expanded = expandedAccountDropdown,
                        onExpandedChange = { expandedAccountDropdown = !expandedAccountDropdown }
                    ) {
                        OutlinedTextField(
                            value = accounts.find { it.id == selectedAccountId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Conta Bancária *") },
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

                    // Category
                    if (filteredCategories.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedCategoryDropdown,
                            onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown }
                        ) {
                            OutlinedTextField(
                                value = filteredCategories.find { it.id == selectedCategoryId }?.name ?: "Sem Categoria",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoria") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedCategoryDropdown,
                                onDismissRequest = { expandedCategoryDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sem Categoria") },
                                    onClick = {
                                        selectedCategoryId = null
                                        expandedCategoryDropdown = false
                                    }
                                )
                                filteredCategories.forEach { cat ->
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
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val isoDate = displayToIsoDate(displayDate)
                    if (amt > 0 && description.isNotBlank()) {
                        when (type) {
                            "INCOME" -> onConfirmIncome(amt, description.trim(), isoDate, selectedAccountId, selectedCategoryId, notes.ifEmpty { null })
                            "EXPENSE" -> onConfirmExpense(amt, description.trim(), isoDate, selectedAccountId, selectedCategoryId, notes.ifEmpty { null })
                            "TRANSFER" -> onConfirmTransfer(amt, description.trim(), isoDate, selectedAccountId, destAccountId, notes.ifEmpty { null })
                        }
                    }
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
