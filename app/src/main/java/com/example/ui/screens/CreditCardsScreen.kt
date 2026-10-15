package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.data.local.entity.CreditCardEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.ui.components.ColorOptionPicker
import com.example.ui.components.DatePickerField
import com.example.ui.components.SectionHeader
import com.example.ui.components.displayToIsoDate
import com.example.ui.components.formatBRL
import com.example.ui.components.getCurrentDisplayDate
import com.example.ui.components.getCurrentIsoDate
import com.example.ui.components.isoToDisplayDate
import com.example.ui.components.parseColorHex
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CardPurple
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardsScreen(
    creditCards: List<CreditCardEntity>,
    invoices: List<InvoiceEntity>,
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    onAddCreditCard: (name: String, bankName: String?, brand: String, last4: String?, limit: Double, closingDay: Int, dueDay: Int, colorHex: String) -> Unit,
    onUpdateCreditCard: (cardId: Long, name: String, bankName: String?, brand: String, last4: String?, limit: Double, closingDay: Int, dueDay: Int, colorHex: String) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onAddCardPurchase: (cardId: Long, amount: Double, purchaseDate: String, description: String, categoryId: Long?) -> Unit,
    onPayInvoice: (invoiceId: Long, accountId: Long, amount: Double) -> Unit,
    onToggleCardActive: (cardId: Long, isActive: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Cartões, 1: Faturas
    var cardFilterStatus by remember { mutableStateOf("ACTIVE") } // ACTIVE, INACTIVE, ALL (Default is ACTIVE so inactive cards never clutter active screen)

    var showAddCardDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CreditCardEntity?>(null) }
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var payingInvoice by remember { mutableStateOf<InvoiceEntity?>(null) }

    // Confirmation Security Dialogs
    var cardToToggleActive by remember { mutableStateOf<CreditCardEntity?>(null) }

    val activeCount = creditCards.count { it.isActive }
    val inactiveCount = creditCards.count { !it.isActive }

    val filteredCards = creditCards.filter { card ->
        when (cardFilterStatus) {
            "ACTIVE" -> card.isActive
            "INACTIVE" -> !card.isActive
            else -> true
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddCardDialog = true else showPurchaseDialog = true
                },
                containerColor = CardPurple,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_card")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo")
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
                title = "Cartões de Crédito & Faturas",
                subtitle = "Gestão segura de limites, faturas e controle ativo"
            )

            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Meus Cartões (${activeCount})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Faturas (${invoices.size})", fontWeight = FontWeight.Bold) }
                )
            }

            // Quick actions & Status Filter
            if (selectedTab == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = cardFilterStatus == "ACTIVE",
                        onClick = { cardFilterStatus = "ACTIVE" },
                        label = { Text("Ativos ($activeCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    FilterChip(
                        selected = cardFilterStatus == "INACTIVE",
                        onClick = { cardFilterStatus = "INACTIVE" },
                        label = { Text("Inativos ($inactiveCount)", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = cardFilterStatus == "ALL",
                        onClick = { cardFilterStatus = "ALL" },
                        label = { Text("Todos (${creditCards.size})", fontSize = 11.sp) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showPurchaseDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CardPurple),
                    shape = RoundedCornerShape(10.dp),
                    enabled = creditCards.any { it.isActive }
                ) {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lançar Compra", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showAddCardDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Novo Cartão", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedTab == 0) {
                if (filteredCards.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (cardFilterStatus == "INACTIVE") {
                                    "Nenhum cartão inativo no momento."
                                } else {
                                    "Nenhum cartão ativo cadastrado.\nClique em \"Novo Cartão\" para cadastrar."
                                },
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (cardFilterStatus == "ACTIVE" && inactiveCount > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { cardFilterStatus = "INACTIVE" }) {
                                    Text("Ver $inactiveCount cartão(ões) inativo(s)")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Helpful banner if looking at inactive cards
                        if (cardFilterStatus == "INACTIVE") {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = ExpenseRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Cartões Inativos (Modo Protegido)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = ExpenseRed
                                            )
                                            Text(
                                                text = "Estes cartões estão ocultos na tela inicial e no lançamento rápido. Suas faturas e histórico de lançamentos continuam 100% seguros no banco local Room.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF7F1D1D)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        items(filteredCards, key = { it.id }) { card ->
                            CreditCardDisplayCard(
                                card = card,
                                onEditClick = { editingCard = card },
                                onToggleActiveClick = { cardToToggleActive = card }
                            )
                        }

                        // Informative footer when on active cards and there are hidden inactive cards
                        if (cardFilterStatus == "ACTIVE" && inactiveCount > 0) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { cardFilterStatus = "INACTIVE" },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "$inactiveCount cartão(ões) inativo(s) oculto(s)",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "Visualizar >",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            } else {
                if (invoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhuma fatura gerada no momento.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(invoices, key = { it.id }) { inv ->
                            val card = creditCards.find { it.id == inv.cardId }
                            InvoiceRowItem(
                                invoice = inv,
                                cardName = card?.name ?: "Cartão",
                                onPayClick = { payingInvoice = inv }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal: Adicionar Novo Cartão
    if (showAddCardDialog) {
        CardFormDialog(
            card = null,
            onDismiss = { showAddCardDialog = false },
            onConfirm = { name, bank, brand, last4, limit, closing, due, color ->
                onAddCreditCard(name, bank, brand, last4, limit, closing, due, color)
                showAddCardDialog = false
            }
        )
    }

    // Modal: Editar Cartão Existente (com confirmação de segurança)
    editingCard?.let { card ->
        CardFormDialog(
            card = card,
            onDismiss = { editingCard = null },
            onConfirm = { name, bank, brand, last4, limit, closing, due, color ->
                onUpdateCreditCard(card.id, name, bank, brand, last4, limit, closing, due, color)
                editingCard = null
            }
        )
    }

    // Modal de Segurança: Confirmação antes de Inativar ou Reativar
    cardToToggleActive?.let { card ->
        val isDeactivating = card.isActive
        AlertDialog(
            onDismissRequest = { cardToToggleActive = null },
            icon = {
                Icon(
                    imageVector = if (isDeactivating) Icons.Default.Warning else Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (isDeactivating) ExpenseRed else IncomeGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (isDeactivating) "Atenção: Inativar Cartão de Crédito?" else "Confirmar Reativação do Cartão?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isDeactivating) {
                            "Você está solicitando a inativação do cartão \"${card.name}\"."
                        } else {
                            "Deseja reativar o cartão \"${card.name}\"?"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (isDeactivating) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Medida de Segurança Contábil:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = ExpenseRed
                                )
                                Text(
                                    text = "• O cartão será oculto para novas despesas no lançamento rápido.\n• O cartão não aparecerá na lista de cartões ativos.\n• O histórico de compras e faturas pendentes permanecem 100% seguros e intactos no banco de dados local Room.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                                if (card.committedLimit > 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Faturas em aberto: ${formatBRL(card.committedLimit)} (Poderão ser quitadas normalmente)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberAlert
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Ao reativar, o cartão voltará a ficar disponível para novas compras no lançamento rápido e visível na aba de cartões ativos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleCardActive(card.id, !card.isActive)
                        cardToToggleActive = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDeactivating) ExpenseRed else IncomeGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isDeactivating) "Sim, Inativar Cartão" else "Sim, Reativar Cartão", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { cardToToggleActive = null }) {
                    Text(if (isDeactivating) "Cancelar e Manter Ativo" else "Cancelar")
                }
            }
        )
    }

    // Modal: Lançar Compra no Cartão
    if (showPurchaseDialog) {
        AddCardPurchaseDialog(
            cards = creditCards.filter { it.isActive },
            categories = categories.filter { it.type == "EXPENSE" && it.isActive },
            onDismiss = { showPurchaseDialog = false },
            onConfirm = { cardId, amt, date, desc, catId ->
                onAddCardPurchase(cardId, amt, date, desc, catId)
                showPurchaseDialog = false
            }
        )
    }

    // Modal: Pagar Fatura
    payingInvoice?.let { inv ->
        val card = creditCards.find { it.id == inv.cardId }
        PayInvoiceDialog(
            invoice = inv,
            cardName = card?.name ?: "Cartão",
            accounts = accounts.filter { it.isActive },
            onDismiss = { payingInvoice = null },
            onConfirm = { accId, amt ->
                onPayInvoice(inv.id, accId, amt)
                payingInvoice = null
            }
        )
    }
}

@Composable
fun CreditCardDisplayCard(
    card: CreditCardEntity,
    onEditClick: () -> Unit,
    onToggleActiveClick: () -> Unit
) {
    val cardBg = if (card.isActive) parseColorHex(card.colorHex) else Color(0xFF475569) // Muted slate gray when inactive
    val usedRatio = if (card.creditLimit > 0) (card.committedLimit / card.creditLimit).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("credit_card_${card.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (card.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (card.isActive) 3.dp else 1.dp)
    ) {
        Column {
            // Visual Credit Card Header (Dark & Realistic)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(138.dp)
                    .background(cardBg)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = card.bankName ?: "BANCO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!card.isActive) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFEF4444).copy(alpha = 0.3f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "INATIVO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFCA5A5),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = card.brand.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = card.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "•••• ${card.lastFourDigits ?: "0000"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Inactive warning banner
            if (!card.isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cartão inativo: Oculto para novas compras no caixa rápido",
                            color = ExpenseRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Card Limits and Cycles
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Limite Utilizado: ${formatBRL(card.committedLimit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Total: ${formatBRL(card.creditLimit)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = usedRatio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (usedRatio > 0.85f) ExpenseRed else CardPurple,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Disponível",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = formatBRL(card.availableLimit),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (card.isActive) IncomeGreen else Color.Gray
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Fechamento / Vencimento",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Dia ${card.closingDay} / Dia ${card.dueDay}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Explicit Edit & Security Action Buttons
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onEditClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar Parâmetros", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(
                        onClick = onToggleActiveClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (card.isActive) ExpenseRed else IncomeGreen
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (card.isActive) "Inativar" else "Reativar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceRowItem(
    invoice: InvoiceEntity,
    cardName: String,
    onPayClick: () -> Unit
) {
    val isPaid = invoice.status == "PAID"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = cardName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ref: ${invoice.referenceMonth} • Vence: ${isoToDisplayDate(invoice.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatBRL(invoice.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) IncomeGreen else ExpenseRed
                    )
                    Text(
                        text = if (isPaid) "PAGA" else "EM ABERTO",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPaid) IncomeGreen else ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!isPaid && invoice.totalAmount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onPayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Pagar", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardFormDialog(
    card: CreditCardEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, bank: String?, brand: String, last4: String?, limit: Double, closing: Int, due: Int, color: String) -> Unit
) {
    var name by remember { mutableStateOf(card?.name ?: "") }
    var bankName by remember { mutableStateOf(card?.bankName ?: "") }
    var brand by remember { mutableStateOf(card?.brand ?: "Mastercard") }
    var last4 by remember { mutableStateOf(card?.lastFourDigits ?: "") }
    var limitStr by remember {
        mutableStateOf(
            card?.creditLimit?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else String.format(java.util.Locale.US, "%.2f", it)
            } ?: ""
        )
    }
    var closingDayStr by remember { mutableStateOf(card?.closingDay?.toString() ?: "10") }
    var dueDayStr by remember { mutableStateOf(card?.dueDay?.toString() ?: "20") }
    var selectedColor by remember { mutableStateOf(card?.colorHex ?: "#820AD1") }
    var showConfirmStep by remember { mutableStateOf(false) }

    val brands = listOf("Mastercard", "Visa", "Elo", "American Express", "Hipercard", "Outra")
    var expandedBrandDropdown by remember { mutableStateOf(false) }

    val parsedLimit = limitStr.replace(",", ".").toDoubleOrNull() ?: 0.0
    val isLimitLowerThanCommitted = card != null && parsedLimit > 0 && parsedLimit < card.committedLimit

    if (showConfirmStep) {
        val newLimit = limitStr.replace(",", ".").toDoubleOrNull() ?: 0.0
        val closing = closingDayStr.toIntOrNull() ?: 10
        val due = dueDayStr.toIntOrNull() ?: 20

        AlertDialog(
            onDismissRequest = { showConfirmStep = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (card == null) "Confirmar Novo Cartão?" else "Confirmar Alterações no Cartão?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Medida de Segurança Contábil:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = "Nome: $name (${brand.uppercase()})", fontSize = 12.sp)
                    if (card != null) {
                        Text(
                            text = "Limite: ${formatBRL(card.creditLimit)} ➔ ${formatBRL(newLimit)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val newAvailable = newLimit - card.committedLimit
                        Text(
                            text = "Novo disponível recalculado: ${formatBRL(newAvailable)}",
                            fontSize = 12.sp,
                            color = IncomeGreen,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(text = "Limite inicial: ${formatBRL(newLimit)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = "Ciclo: Fechamento dia $closing / Vencimento dia $due", fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ao confirmar, os novos parâmetros serão gravados no banco de dados local Room e preservados com segurança.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmStep = false
                        onConfirm(name.trim(), bankName.trim(), brand, last4.trim(), newLimit, closing, due, selectedColor)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Sim, Confirmar e Salvar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmStep = false }) {
                    Text("Voltar e Revisar")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (card == null) "Novo Cartão de Crédito" else "Editar Cartão de Crédito",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (card != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Cartão em Edição: ${card.name}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Limite atual: ${formatBRL(card.creditLimit)} • Comprometido: ${formatBRL(card.committedLimit)}",
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Disponível atual: ${formatBRL(card.availableLimit)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IncomeGreen
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Cartão (Ex: Nubank Ultravioleta) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Banco Emissor") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedBrandDropdown,
                    onExpandedChange = { expandedBrandDropdown = !expandedBrandDropdown }
                ) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bandeira") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBrandDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedBrandDropdown,
                        onDismissRequest = { expandedBrandDropdown = false }
                    ) {
                        brands.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b) },
                                onClick = {
                                    brand = b
                                    expandedBrandDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Limite Total (R$) *") },
                    isError = isLimitLowerThanCommitted,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isLimitLowerThanCommitted) {
                    Text(
                        text = "⚠️ O limite não pode ser menor que o total já comprometido em faturas (${formatBRL(card!!.committedLimit)})",
                        color = ExpenseRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = closingDayStr,
                        onValueChange = { closingDayStr = it },
                        label = { Text("Fechamento (1-31)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dueDayStr,
                        onValueChange = { dueDayStr = it },
                        label = { Text("Vencimento (1-31)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = last4,
                    onValueChange = { last4 = it },
                    label = { Text("Últimos 4 dígitos") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Cor do Cartão:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                ColorOptionPicker(selectedColorHex = selectedColor, onColorSelected = { selectedColor = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && limit > 0 && (card == null || limit >= card.committedLimit)) {
                        showConfirmStep = true
                    }
                },
                enabled = name.isNotBlank() && parsedLimit > 0 && !isLimitLowerThanCommitted
            ) {
                Text(if (card == null) "Cadastrar Cartão" else "Salvar Alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardPurchaseDialog(
    cards: List<CreditCardEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (cardId: Long, amount: Double, purchaseDate: String, description: String, categoryId: Long?) -> Unit
) {
    var selectedCardId by remember { mutableStateOf(cards.firstOrNull()?.id ?: 0L) }
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var displayPurchaseDate by remember { mutableStateOf(getCurrentDisplayDate()) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    var expandedCardDropdown by remember { mutableStateOf(false) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lançar Compra no Cartão") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedCardDropdown,
                    onExpandedChange = { expandedCardDropdown = !expandedCardDropdown }
                ) {
                    OutlinedTextField(
                        value = cards.find { it.id == selectedCardId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cartão de Crédito *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCardDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCardDropdown,
                        onDismissRequest = { expandedCardDropdown = false }
                    ) {
                        cards.forEach { card ->
                            DropdownMenuItem(
                                text = { Text("${card.name} (Disp: ${formatBRL(card.availableLimit)})") },
                                onClick = {
                                    selectedCardId = card.id
                                    expandedCardDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Valor da Compra (R$) *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição *") },
                    modifier = Modifier.fillMaxWidth()
                )

                DatePickerField(
                    dateValue = displayPurchaseDate,
                    onDateChange = { displayPurchaseDate = it },
                    label = "Data da Compra (DD/MM/AAAA) *",
                    modifier = Modifier.fillMaxWidth()
                )

                if (categories.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedCategoryDropdown,
                        onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown }
                    ) {
                        OutlinedTextField(
                            value = categories.find { it.id == selectedCategoryId }?.name ?: "Sem Categoria",
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
                            categories.forEach { cat ->
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
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.replace(",", ".").toDoubleOrNull() ?: 0.0
                    val isoDate = displayToIsoDate(displayPurchaseDate)
                    if (amt > 0 && description.isNotBlank()) {
                        onConfirm(selectedCardId, amt, isoDate, description.trim(), selectedCategoryId)
                    }
                }
            ) {
                Text("Confirmar Compra")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayInvoiceDialog(
    invoice: InvoiceEntity,
    cardName: String,
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (accountId: Long, amount: Double) -> Unit
) {
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var expandedAccountDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pagar Fatura") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Fatura $cardName (${invoice.referenceMonth})\nTotal: ${formatBRL(invoice.totalAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = expandedAccountDropdown,
                    onExpandedChange = { expandedAccountDropdown = !expandedAccountDropdown }
                ) {
                    OutlinedTextField(
                        value = accounts.find { it.id == selectedAccountId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Conta Bancária para Débito *") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedAccountId, invoice.totalAmount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
            ) {
                Text("Confirmar Pagamento Integral")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
