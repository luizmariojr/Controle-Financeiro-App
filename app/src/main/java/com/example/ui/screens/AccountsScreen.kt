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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.Switch
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
import com.example.data.local.entity.AccountEntity
import com.example.ui.components.BalanceAdjustmentDialog
import com.example.ui.components.ColorOptionPicker
import com.example.ui.components.SectionHeader
import com.example.ui.components.formatBRL
import com.example.ui.components.parseColorHex
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

fun getAccountTypeLabel(type: String): String {
    return when (type) {
        "CONTA_CORRENTE" -> "Conta Corrente"
        "POUPANCA" -> "Poupança"
        "DINHEIRO" -> "Dinheiro em Espécie"
        "CARTEIRA_DIGITAL" -> "Carteira Digital"
        "INVESTIMENTO" -> "Investimentos"
        else -> "Outra"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    accounts: List<AccountEntity>,
    onAddAccount: (name: String, type: String, institution: String?, initialBalance: Double, colorHex: String) -> Unit,
    onUpdateAccount: (AccountEntity) -> Unit,
    onToggleActive: (accountId: Long, isActive: Boolean) -> Unit,
    onAdjustBalance: (accountId: Long, newBalance: Double, date: String, notes: String?) -> Unit,
    onDeleteAccount: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var adjustingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var confirmingToggleAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var filterStatus by remember { mutableStateOf("ALL") } // "ALL", "ACTIVE", "INACTIVE"

    val activeCount = accounts.count { it.isActive }
    val inactiveCount = accounts.count { !it.isActive }

    val filteredAccounts = remember(accounts, filterStatus) {
        when (filterStatus) {
            "ACTIVE" -> accounts.filter { it.isActive }
            "INACTIVE" -> accounts.filter { !it.isActive }
            else -> accounts
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_account")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nova Conta")
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
                title = "Contas Bancárias & Carteiras",
                subtitle = "Gerencie seus saldos e contas ativas ou inativas"
            )

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterStatus == "ALL",
                    onClick = { filterStatus = "ALL" },
                    label = { Text("Todas (${accounts.size})") }
                )
                FilterChip(
                    selected = filterStatus == "ACTIVE",
                    onClick = { filterStatus = "ACTIVE" },
                    label = { Text("Ativas ($activeCount)") },
                    leadingIcon = {
                        if (filterStatus == "ACTIVE") {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                )
                FilterChip(
                    selected = filterStatus == "INACTIVE",
                    onClick = { filterStatus = "INACTIVE" },
                    label = { Text("Inativas ($inactiveCount)") },
                    leadingIcon = {
                        if (filterStatus == "INACTIVE") {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }

            if (filteredAccounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (accounts.isEmpty()) {
                            "Nenhuma conta cadastrada. Clique no botão + para adicionar."
                        } else {
                            "Nenhuma conta encontrada com este filtro."
                        },
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAccounts, key = { it.id }) { account ->
                        AccountItemCard(
                            account = account,
                            onEditClick = { editingAccount = account },
                            onAdjustBalanceClick = { adjustingAccount = account },
                            onToggleActiveClick = { confirmingToggleAccount = account }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AccountFormDialog(
            account = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, inst, initialBal, isActive, color ->
                onAddAccount(name, type, inst, initialBal, color)
                showAddDialog = false
            }
        )
    }

    editingAccount?.let { acc ->
        AccountFormDialog(
            account = acc,
            onDismiss = { editingAccount = null },
            onConfirm = { name, type, inst, initialBal, isActive, color ->
                onUpdateAccount(
                    acc.copy(
                        name = name,
                        type = type,
                        institution = inst,
                        initialBalance = initialBal,
                        isActive = isActive,
                        colorHex = color
                    )
                )
                editingAccount = null
            }
        )
    }

    adjustingAccount?.let { acc ->
        BalanceAdjustmentDialog(
            accounts = accounts,
            initialAccountId = acc.id,
            onDismiss = { adjustingAccount = null },
            onConfirm = { accId, newBal, date, notes ->
                onAdjustBalance(accId, newBal, date, notes)
                adjustingAccount = null
            }
        )
    }

    // Safety Confirmation Dialog for Activation/Inactivation
    confirmingToggleAccount?.let { acc ->
        AlertDialog(
            onDismissRequest = { confirmingToggleAccount = null },
            title = {
                Text(text = if (acc.isActive) "Confirmar Inativação" else "Confirmar Reativação")
            },
            text = {
                Text(
                    text = if (acc.isActive) {
                        "Deseja realmente inativar a conta '${acc.name}'? Ela não aparecerá mais como opção em novos lançamentos de receita e despesa."
                    } else {
                        "Deseja reativar a conta '${acc.name}'? Ela voltará a ficar disponível para seleções e lançamentos."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleActive(acc.id, !acc.isActive)
                        confirmingToggleAccount = null
                    }
                ) {
                    Text(if (acc.isActive) "Sim, Inativar" else "Sim, Reativar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingToggleAccount = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AccountItemCard(
    account: AccountEntity,
    onEditClick: () -> Unit,
    onAdjustBalanceClick: () -> Unit,
    onToggleActiveClick: () -> Unit
) {
    val cardColor = parseColorHex(account.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("account_card_${account.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (account.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (account.isActive) cardColor else cardColor.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Active / Inactive Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (account.isActive) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (account.isActive) "Ativa" else "Inativa",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (account.isActive) IncomeGreen else ExpenseRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = getAccountTypeLabel(account.type) + if (!account.institution.isNullOrEmpty()) " • ${account.institution}" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = onToggleActiveClick) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = if (account.isActive) "Inativar" else "Reativar",
                            tint = if (account.isActive) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Saldo Inicial: ${formatBRL(account.initialBalance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onAdjustBalanceClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajustar Saldo", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Text(
                    text = formatBRL(account.currentBalance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (account.currentBalance < 0) ExpenseRed else IncomeGreen
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountFormDialog(
    account: AccountEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, institution: String?, initialBalance: Double, isActive: Boolean, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var type by remember { mutableStateOf(account?.type ?: "CONTA_CORRENTE") }
    var institution by remember { mutableStateOf(account?.institution ?: "") }
    var initialBalanceStr by remember { mutableStateOf(account?.initialBalance?.toString() ?: "0.0") }
    var isActive by remember { mutableStateOf(account?.isActive ?: true) }
    var selectedColor by remember { mutableStateOf(account?.colorHex ?: "#10B981") }
    var expandedTypeDropdown by remember { mutableStateOf(false) }

    val accountTypes = listOf(
        "CONTA_CORRENTE" to "Conta Corrente",
        "POUPANCA" to "Poupança",
        "DINHEIRO" to "Dinheiro em Espécie",
        "CARTEIRA_DIGITAL" to "Carteira Digital",
        "INVESTIMENTO" to "Investimentos",
        "OUTRA" to "Outra"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (account == null) "Nova Conta Bancária" else "Editar Cadastro da Conta")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Conta *") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedTypeDropdown,
                    onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = accountTypes.find { it.first == type }?.second ?: type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Conta") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false }
                    ) {
                        accountTypes.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.second) },
                                onClick = {
                                    type = item.first
                                    expandedTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("Instituição Bancária (Ex: Itaú, Nubank)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (account == null) {
                    OutlinedTextField(
                        value = initialBalanceStr,
                        onValueChange = { initialBalanceStr = it },
                        label = { Text("Saldo Inicial de Abertura (R$)") },
                        supportingText = {
                            Text("Saldo inicial com o qual esta conta começará.")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "💡 Para alterar o saldo atual da conta, utilize a opção 'Ajuste de Saldo' na aba 'Mais' ou no cartão da conta. Isso gerará um lançamento de controle registrado no extrato.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status: ${if (isActive) "Conta Ativa" else "Conta Inativa"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }

                Text(
                    text = "Cor de Destaque:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                ColorOptionPicker(
                    selectedColorHex = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val initialBal = if (account != null) account.initialBalance else (initialBalanceStr.replace(",", ".").toDoubleOrNull() ?: 0.0)
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), type, institution.trim(), initialBal, isActive, selectedColor)
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
