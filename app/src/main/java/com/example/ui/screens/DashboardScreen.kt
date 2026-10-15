package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.ui.components.SectionHeader
import com.example.ui.components.SummaryStatCard
import com.example.ui.components.formatBRL
import com.example.ui.theme.CardPurple
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TransferBlue
import com.example.ui.viewmodel.FinancialSummaryState

@Composable
fun DashboardScreen(
    summaryState: FinancialSummaryState,
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    onQuickExpenseClick: () -> Unit,
    onQuickIncomeClick: () -> Unit,
    onQuickTransferClick: () -> Unit,
    onViewAccountsClick: () -> Unit,
    onViewTransactionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Quick Action Banner (Resilient and Responsive)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_actions_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "LANÇAMENTO RÁPIDO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Registre despesas e movimentações em segundos no banco local",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onQuickExpenseClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_expense"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ExpenseRed
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Despesa",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = onQuickIncomeClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_income"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IncomeGreen
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Receita",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = onQuickTransferClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quick_transfer"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Transf.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Summary Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStatCard(
                        title = "Patrimônio Líquido",
                        value = formatBRL(summaryState.netWorth),
                        subtitle = "${summaryState.activeAccountsCount} conta(s) ativa(s)",
                        icon = Icons.Default.Wallet,
                        iconBgColor = IncomeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "Receitas",
                        value = formatBRL(summaryState.totalIncome),
                        subtitle = "Entradas confirmadas",
                        icon = Icons.Default.ArrowDownward,
                        iconBgColor = IncomeGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStatCard(
                        title = "Despesas",
                        value = formatBRL(summaryState.totalExpense),
                        subtitle = "Saídas debitadas",
                        icon = Icons.Default.ArrowUpward,
                        iconBgColor = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStatCard(
                        title = "Cartões de Crédito",
                        value = formatBRL(summaryState.totalCommittedLimit),
                        subtitle = "Disp: ${formatBRL(summaryState.totalAvailableLimit)}",
                        icon = Icons.Default.CreditCard,
                        iconBgColor = CardPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Active Accounts Section
        item {
            SectionHeader(
                title = "Contas Bancárias",
                subtitle = "Saldos no banco de dados local",
                actionText = "Ver todas",
                onActionClick = onViewAccountsClick
            )
        }

        if (accounts.isEmpty()) {
            item {
                Text(
                    text = "Nenhuma conta cadastrada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(accounts.take(3), key = { it.id }) { account ->
                AccountDashboardCard(account = account)
            }
        }

        // Recent Transactions Section
        item {
            SectionHeader(
                title = "Últimas Movimentações",
                subtitle = "Histórico de lançamentos",
                actionText = "Ver extrato",
                onActionClick = onViewTransactionsClick
            )
        }

        if (transactions.isEmpty()) {
            item {
                Text(
                    text = "Nenhuma movimentação registrada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(transactions.take(5), key = { it.id }) { tx ->
                TransactionRowItem(transaction = tx)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AccountDashboardCard(account: AccountEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        .background(com.example.ui.components.parseColorHex(account.colorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = account.institution ?: account.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatBRL(account.currentBalance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (account.currentBalance < 0) ExpenseRed else IncomeGreen
                )
                Text(
                    text = "Saldo Atual",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun TransactionRowItem(transaction: TransactionEntity) {
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCancelled) Color.Gray else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${transaction.date} • ${if (isCancelled) "CANCELADO" else transaction.type}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Text(
                text = "${if (isIncome) "+ " else if (isExpense) "- " else ""}${formatBRL(transaction.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isCancelled) Color.Gray else iconColor
            )
        }
    }
}
