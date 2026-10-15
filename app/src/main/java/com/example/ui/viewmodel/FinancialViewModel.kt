package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CreditCardEntity
import com.example.data.local.entity.GoalContributionEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.FinancialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

data class FinancialSummaryState(
    val netWorth: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalCommittedLimit: Double = 0.0,
    val totalAvailableLimit: Double = 0.0,
    val activeAccountsCount: Int = 0,
    val activeCardsCount: Int = 0
)

class FinancialViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FinancialRepository(db)

    val accounts: StateFlow<List<AccountEntity>> = repository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAccounts: StateFlow<List<AccountEntity>> = repository.activeAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val creditCards: StateFlow<List<CreditCardEntity>> = repository.creditCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.invoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = repository.budgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalEntity>> = repository.goals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summaryState: StateFlow<FinancialSummaryState> = combine(
        accounts,
        transactions,
        creditCards
    ) { accList, txList, cardList ->
        val activeAccs = accList.filter { it.isActive }
        val netWorth = activeAccs.sumOf { it.currentBalance }

        val validTxs = txList.filter { it.status == "COMPLETED" }
        val totalInc = validTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExp = validTxs.filter { it.type == "EXPENSE" && !it.isInvoicePayment }.sumOf { it.amount }

        val activeCrds = cardList.filter { it.isActive }
        val totalCommitted = activeCrds.sumOf { it.committedLimit }
        val totalAvailable = activeCrds.sumOf { it.availableLimit }

        FinancialSummaryState(
            netWorth = netWorth,
            totalIncome = totalInc,
            totalExpense = totalExp,
            totalCommittedLimit = totalCommitted,
            totalAvailableLimit = totalAvailable,
            activeAccountsCount = activeAccs.size,
            activeCardsCount = activeCrds.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummaryState())

    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents: SharedFlow<String> = _messageEvents

    init {
        viewModelScope.launch {
            repository.checkAndSeedDefaults()
        }
    }

    // Account Actions
    fun addAccount(name: String, type: String, institution: String?, initialBalance: Double, colorHex: String) {
        viewModelScope.launch {
            val account = AccountEntity(
                name = name,
                type = type,
                institution = if (institution.isNull_orBlank()) null else institution,
                initialBalance = initialBalance,
                currentBalance = initialBalance,
                colorHex = colorHex
            )
            repository.addAccount(account)
            _messageEvents.emit("Conta '$name' criada com sucesso")
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.updateAccount(account)
            _messageEvents.emit("Conta '${account.name}' atualizada")
        }
    }

    fun toggleAccountActive(accountId: Long, isActive: Boolean) {
        viewModelScope.launch {
            repository.toggleAccountActive(accountId, isActive)
            _messageEvents.emit(if (isActive) "Conta reativada com sucesso" else "Conta inativada com sucesso")
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            repository.deleteAccount(id)
            _messageEvents.emit("Conta excluída")
        }
    }

    fun adjustAccountBalance(accountId: Long, newBalance: Double, date: String, notes: String?) {
        viewModelScope.launch {
            val res = repository.adjustAccountBalance(accountId, newBalance, date, notes)
            if (res.isSuccess) {
                _messageEvents.emit("Saldo ajustado com sucesso para R$ ${String.format(Locale.getDefault(), "%.2f", newBalance)}")
            } else {
                _messageEvents.emit(res.exceptionOrNull()?.message ?: "Erro ao ajustar saldo")
            }
        }
    }

    // Transaction Actions
    fun addIncome(amount: Double, description: String, date: String, accountId: Long, categoryId: Long?, notes: String?) {
        viewModelScope.launch {
            val res = repository.addIncome(amount, description, date, accountId, categoryId, notes)
            if (res.isSuccess) {
                _messageEvents.emit("Receita de R$ ${String.format(Locale.getDefault(), "%.2f", amount)} registrada!")
            } else {
                _messageEvents.emit(res.exceptionOrNull()?.message ?: "Erro ao registrar receita")
            }
        }
    }

    fun addExpense(amount: Double, description: String, date: String, accountId: Long, categoryId: Long?, notes: String?) {
        viewModelScope.launch {
            val res = repository.addExpense(amount, description, date, accountId, categoryId, notes)
            if (res.isSuccess) {
                _messageEvents.emit("Despesa de R$ ${String.format(Locale.getDefault(), "%.2f", amount)} registrada!")
            } else {
                _messageEvents.emit(res.exceptionOrNull()?.message ?: "Erro ao registrar despesa")
            }
        }
    }

    fun addTransfer(amount: Double, description: String, date: String, sourceAccountId: Long, destAccountId: Long, notes: String?) {
        viewModelScope.launch {
            val res = repository.addTransfer(amount, description, date, sourceAccountId, destAccountId, notes)
            if (res.isSuccess) {
                _messageEvents.emit("Transferência de R$ ${String.format(Locale.getDefault(), "%.2f", amount)} realizada!")
            } else {
                _messageEvents.emit(res.exceptionOrNull()?.message ?: "Erro na transferência")
            }
        }
    }

    fun revertTransaction(transactionId: Long) {
        viewModelScope.launch {
            val res = repository.revertTransaction(transactionId)
            if (res.isSuccess) {
                _messageEvents.emit("Transação estornada com sucesso")
            } else {
                _messageEvents.emit(res.exceptionOrNull()?.message ?: "Erro ao estornar")
            }
        }
    }

    // Credit Cards Actions
    fun addCreditCard(name: String, bankName: String?, brand: String, last4: String?, limit: Double, closingDay: Int, dueDay: Int, colorHex: String) {
        viewModelScope.launch {
            val card = CreditCardEntity(
                name = name,
                bankName = if (bankName.isNull_orBlank()) null else bankName,
                brand = brand,
                lastFourDigits = if (last4.isNull_orBlank()) null else last4,
                creditLimit = limit,
                availableLimit = limit,
                closingDay = closingDay,
                dueDay = dueDay,
                colorHex = colorHex
            )
            repository.addCreditCard(card)
            _messageEvents.emit("Cartão de crédito '$name' cadastrado")
        }
    }

    fun addCardPurchase(cardId: Long, amount: Double, purchaseDate: String, description: String, categoryId: Long?) {
        viewModelScope.launch {
            val res = repository.addCardPurchase(cardId, amount, purchaseDate, description, categoryId)
            if (res.isSuccess) {
                _messageEvents.emit("Compra de R$ ${String.format(Locale.getDefault(), "%.2f", amount)} registrada no cartão!")
            } else {
                _messageEvents.emit(res.exceptionOrNull()?.message ?: "Erro na compra no cartão")
            }
        }
    }

    fun payInvoice(invoiceId: Long, accountId: Long, amount: Double) {
        viewModelScope.launch {
            val res = repository.payInvoice(invoiceId, accountId, amount)
            if (res.isSuccess) {
                _messageEvents.emit("Fatura paga com sucesso!")
            } else {
                _messageEvents.emit(res.exceptionOrNull()?.message ?: "Erro ao pagar fatura")
            }
        }
    }

    fun toggleCardActive(cardId: Long, isActive: Boolean) {
        viewModelScope.launch {
            repository.setCardActive(cardId, isActive)
            _messageEvents.emit(if (isActive) "Cartão reativado com sucesso" else "Cartão inativado com sucesso")
        }
    }

    fun updateCreditCard(
        cardId: Long,
        name: String,
        bankName: String?,
        brand: String,
        last4: String?,
        limit: Double,
        closingDay: Int,
        dueDay: Int,
        colorHex: String
    ) {
        viewModelScope.launch {
            val existing = creditCards.value.find { it.id == cardId }
            if (existing != null) {
                if (limit < existing.committedLimit) {
                    _messageEvents.emit("Aviso: O novo limite (R$ ${String.format(Locale.getDefault(), "%.2f", limit)}) não pode ser inferior ao limite já comprometido (R$ ${String.format(Locale.getDefault(), "%.2f", existing.committedLimit)})")
                    return@launch
                }
                val newAvailable = limit - existing.committedLimit
                val updated = existing.copy(
                    name = name,
                    bankName = if (bankName.isNull_orBlank()) null else bankName,
                    brand = brand,
                    lastFourDigits = if (last4.isNull_orBlank()) null else last4,
                    creditLimit = limit,
                    availableLimit = newAvailable,
                    closingDay = closingDay,
                    dueDay = dueDay,
                    colorHex = colorHex
                )
                repository.updateCreditCard(updated)
                _messageEvents.emit("Cartão '${updated.name}' atualizado com sucesso!")
            } else {
                _messageEvents.emit("Erro: Cartão não encontrado")
            }
        }
    }

    fun deleteCreditCard(cardId: Long) {
        viewModelScope.launch {
            val card = creditCards.value.find { it.id == cardId }
            if (card != null && card.committedLimit > 0) {
                _messageEvents.emit("Aviso: Não é recomendável excluir um cartão com faturas pendentes. Inative-o em vez de excluir.")
                return@launch
            }
            repository.setCardActive(cardId, false)
            _messageEvents.emit("Cartão inativado")
        }
    }

    // Category Actions
    fun addCategory(name: String, type: String, parentCategoryId: Long?, colorHex: String, iconName: String) {
        viewModelScope.launch {
            val cat = CategoryEntity(
                name = name,
                type = type,
                parentCategoryId = parentCategoryId,
                colorHex = colorHex,
                iconName = iconName
            )
            repository.addCategory(cat)
            _messageEvents.emit("Categoria '$name' criada")
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id)
            _messageEvents.emit("Categoria desativada")
        }
    }

    // Budget Actions
    fun addBudget(categoryId: Long, subcategoryId: Long?, amount: Double, monthYear: String, alertThreshold: Int) {
        viewModelScope.launch {
            val budget = BudgetEntity(
                categoryId = categoryId,
                subcategoryId = subcategoryId,
                amount = amount,
                monthYear = monthYear,
                alertThresholdPercentage = alertThreshold
            )
            repository.addBudget(budget)
            _messageEvents.emit("Orçamento definido com sucesso")
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
            _messageEvents.emit("Orçamento removido")
        }
    }

    // Goal Actions
    fun addGoal(name: String, description: String?, targetAmount: Double, targetDate: String?, colorHex: String) {
        viewModelScope.launch {
            val goal = GoalEntity(
                name = name,
                description = if (description.isNull_orBlank()) null else description,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                targetDate = if (targetDate.isNull_orBlank()) null else targetDate,
                colorHex = colorHex
            )
            repository.addGoal(goal)
            _messageEvents.emit("Meta '$name' criada")
        }
    }

    fun addGoalContribution(goalId: Long, amount: Double, type: String, date: String, description: String?) {
        viewModelScope.launch {
            val res = repository.addGoalContribution(goalId, amount, type, date, description)
            if (res.isSuccess) {
                _messageEvents.emit(if (type == "CONTRIBUTION") "Aporte de R$ ${String.format(Locale.getDefault(), "%.2f", amount)} realizado!" else "Resgate realizado com sucesso")
            } else {
                _messageEvents.emit(res.exceptionOrNull()?.message ?: "Erro ao registrar aporte")
            }
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoal(id)
            _messageEvents.emit("Meta removida")
        }
    }

    fun getGoalContributions(goalId: Long): Flow<List<GoalContributionEntity>> {
        return repository.getGoalContributions(goalId)
    }

    private fun String?.isNull_orBlank(): Boolean = this.isNullOrBlank()
}
