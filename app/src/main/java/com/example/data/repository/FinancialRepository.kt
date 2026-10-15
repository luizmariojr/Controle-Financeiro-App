package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CreditCardEntity
import com.example.data.local.entity.GoalContributionEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.InvoiceEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FinancialRepository(private val db: AppDatabase) {

    private val accountDao = db.accountDao()
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val creditCardDao = db.creditCardDao()
    private val invoiceDao = db.invoiceDao()
    private val budgetDao = db.budgetDao()
    private val goalDao = db.goalDao()

    val accounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    val activeAccounts: Flow<List<AccountEntity>> = accountDao.getActiveAccounts()
    val transactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val creditCards: Flow<List<CreditCardEntity>> = creditCardDao.getAllCards()
    val invoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()
    val budgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val goals: Flow<List<GoalEntity>> = goalDao.getAllGoals()

    suspend fun checkAndSeedDefaults() {
        if (categoryDao.getCategoryCount() == 0) {
            val defaultCategories = listOf(
                // DESPESAS
                CategoryEntity(name = "Moradia", type = "EXPENSE", iconName = "Home", colorHex = "#3B82F6", isSystem = true),
                CategoryEntity(name = "Alimentação", type = "EXPENSE", iconName = "Restaurant", colorHex = "#F97316", isSystem = true),
                CategoryEntity(name = "Transporte", type = "EXPENSE", iconName = "DirectionsCar", colorHex = "#EAB308", isSystem = true),
                CategoryEntity(name = "Saúde & Cuidados", type = "EXPENSE", iconName = "LocalHospital", colorHex = "#EF4444", isSystem = true),
                CategoryEntity(name = "Educação", type = "EXPENSE", iconName = "School", colorHex = "#8B5CF6", isSystem = true),
                CategoryEntity(name = "Lazer & Cultura", type = "EXPENSE", iconName = "ConfirmationNumber", colorHex = "#EC4899", isSystem = true),
                CategoryEntity(name = "Compras & Pessoais", type = "EXPENSE", iconName = "ShoppingBag", colorHex = "#10B981", isSystem = true),
                CategoryEntity(name = "Serviços & Assinaturas", type = "EXPENSE", iconName = "CreditCard", colorHex = "#6366F1", isSystem = true),
                CategoryEntity(name = "Outras Despesas", type = "EXPENSE", iconName = "Category", colorHex = "#64748B", isSystem = true),

                // RECEITAS
                CategoryEntity(name = "Salário & Emprego", type = "INCOME", iconName = "Work", colorHex = "#059669", isSystem = true),
                CategoryEntity(name = "Negócios & Autônomo", type = "INCOME", iconName = "TrendingUp", colorHex = "#14B8A6", isSystem = true),
                CategoryEntity(name = "Investimentos", type = "INCOME", iconName = "AccountBalance", colorHex = "#0284C7", isSystem = true),
                CategoryEntity(name = "Outras Receitas", type = "INCOME", iconName = "AddCircle", colorHex = "#84CC16", isSystem = true)
            )
            categoryDao.insertCategories(defaultCategories)
        }

        // Seed initial sample accounts if empty
        if (accountDao.getAccountCount() == 0) {
            val acc1 = AccountEntity(
                name = "Conta Corrente Itaú",
                type = "CONTA_CORRENTE",
                institution = "Itaú Unibanco",
                initialBalance = 2500.0,
                currentBalance = 2500.0,
                colorHex = "#EC7000",
                iconName = "AccountBalance"
            )
            val acc2 = AccountEntity(
                name = "Reserva Nubank",
                type = "POUPANCA",
                institution = "Nubank",
                initialBalance = 5000.0,
                currentBalance = 5000.0,
                colorHex = "#820AD1",
                iconName = "Savings"
            )
            accountDao.insertAccounts(listOf(acc1, acc2))

            // Seed sample credit card
            val card = CreditCardEntity(
                name = "Nubank Ultravioleta",
                bankName = "Nubank",
                brand = "Mastercard",
                lastFourDigits = "4321",
                creditLimit = 4000.0,
                availableLimit = 4000.0,
                closingDay = 10,
                dueDay = 20,
                colorHex = "#820AD1"
            )
            creditCardDao.insertCard(card)
        }
    }

    // --- Accounts ---
    suspend fun addAccount(account: AccountEntity): Long {
        return accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account)
    }

    suspend fun toggleAccountActive(accountId: Long, isActive: Boolean) {
        accountDao.setAccountActive(accountId, isActive)
    }

    suspend fun deleteAccount(id: Long) {
        accountDao.deleteAccountById(id)
    }

    suspend fun adjustAccountBalance(
        accountId: Long,
        newBalance: Double,
        date: String,
        notes: String?
    ): Result<Unit> {
        val account = accountDao.getAccountById(accountId)
            ?: return Result.failure(Exception("Conta não encontrada"))

        val diff = newBalance - account.currentBalance
        if (Math.abs(diff) < 0.001) {
            return Result.success(Unit)
        }

        val type = if (diff > 0) "INCOME" else "EXPENSE"
        val absAmt = Math.abs(diff)
        val description = "Ajuste de Saldo (${account.name})"

        val tx = TransactionEntity(
            type = type,
            amount = absAmt,
            description = description,
            date = date,
            accountId = accountId,
            notes = notes?.ifBlank { null } ?: "Ajuste de saldo de R$ ${String.format(Locale.getDefault(), "%.2f", account.currentBalance)} para R$ ${String.format(Locale.getDefault(), "%.2f", newBalance)}",
            status = "COMPLETED"
        )

        transactionDao.insertTransaction(tx)
        accountDao.updateBalance(accountId, newBalance)
        return Result.success(Unit)
    }

    // --- Transactions & Balance Logic ---
    suspend fun addIncome(
        amount: Double,
        description: String,
        date: String,
        accountId: Long,
        categoryId: Long?,
        notes: String?
    ): Result<Long> {
        val account = accountDao.getAccountById(accountId)
            ?: return Result.failure(Exception("Conta não encontrada"))

        val tx = TransactionEntity(
            type = "INCOME",
            amount = amount,
            description = description,
            date = date,
            accountId = accountId,
            categoryId = categoryId,
            notes = notes,
            status = "COMPLETED"
        )

        val txId = transactionDao.insertTransaction(tx)
        accountDao.updateBalance(accountId, account.currentBalance + amount)
        return Result.success(txId)
    }

    suspend fun addExpense(
        amount: Double,
        description: String,
        date: String,
        accountId: Long,
        categoryId: Long?,
        notes: String?
    ): Result<Long> {
        val account = accountDao.getAccountById(accountId)
            ?: return Result.failure(Exception("Conta não encontrada"))

        val tx = TransactionEntity(
            type = "EXPENSE",
            amount = amount,
            description = description,
            date = date,
            accountId = accountId,
            categoryId = categoryId,
            notes = notes,
            status = "COMPLETED"
        )

        val txId = transactionDao.insertTransaction(tx)
        accountDao.updateBalance(accountId, account.currentBalance - amount)
        return Result.success(txId)
    }

    suspend fun addTransfer(
        amount: Double,
        description: String,
        date: String,
        sourceAccountId: Long,
        destinationAccountId: Long,
        notes: String?
    ): Result<Long> {
        if (sourceAccountId == destinationAccountId) {
            return Result.failure(Exception("As contas de origem e destino devem ser diferentes"))
        }

        val sourceAccount = accountDao.getAccountById(sourceAccountId)
            ?: return Result.failure(Exception("Conta de origem não encontrada"))
        val destAccount = accountDao.getAccountById(destinationAccountId)
            ?: return Result.failure(Exception("Conta de destino não encontrada"))

        val tx = TransactionEntity(
            type = "TRANSFER",
            amount = amount,
            description = description.ifEmpty { "Transferência de ${sourceAccount.name} para ${destAccount.name}" },
            date = date,
            accountId = sourceAccountId,
            destinationAccountId = destinationAccountId,
            notes = notes,
            status = "COMPLETED"
        )

        val txId = transactionDao.insertTransaction(tx)
        accountDao.updateBalance(sourceAccountId, sourceAccount.currentBalance - amount)
        accountDao.updateBalance(destinationAccountId, destAccount.currentBalance + amount)
        return Result.success(txId)
    }

    suspend fun revertTransaction(transactionId: Long): Result<Unit> {
        val tx = transactionDao.getTransactionById(transactionId)
            ?: return Result.failure(Exception("Transação não encontrada"))

        if (tx.status == "CANCELLED") {
            return Result.failure(Exception("Transação já foi cancelada anteriormente"))
        }

        when (tx.type) {
            "INCOME" -> {
                tx.accountId?.let { accId ->
                    val acc = accountDao.getAccountById(accId)
                    acc?.let { accountDao.updateBalance(accId, it.currentBalance - tx.amount) }
                }
            }
            "EXPENSE" -> {
                tx.accountId?.let { accId ->
                    val acc = accountDao.getAccountById(accId)
                    acc?.let { accountDao.updateBalance(accId, it.currentBalance + tx.amount) }
                }
            }
            "TRANSFER" -> {
                val srcAcc = tx.accountId?.let { accountDao.getAccountById(it) }
                val destAcc = tx.destinationAccountId?.let { accountDao.getAccountById(it) }

                srcAcc?.let { accountDao.updateBalance(it.id, it.currentBalance + tx.amount) }
                destAcc?.let { accountDao.updateBalance(it.id, it.currentBalance - tx.amount) }
            }
        }

        transactionDao.cancelTransaction(transactionId)
        return Result.success(Unit)
    }

    // --- Credit Cards & Invoices ---
    suspend fun addCreditCard(card: CreditCardEntity): Long {
        return creditCardDao.insertCard(card)
    }

    suspend fun updateCreditCard(card: CreditCardEntity) {
        creditCardDao.updateCard(card)
    }

    suspend fun setCardActive(cardId: Long, isActive: Boolean) {
        creditCardDao.setCardActive(cardId, isActive)
    }

    suspend fun addCardPurchase(
        cardId: Long,
        amount: Double,
        purchaseDate: String, // YYYY-MM-DD
        description: String,
        categoryId: Long?
    ): Result<Long> {
        val card = creditCardDao.getCardById(cardId)
            ?: return Result.failure(Exception("Cartão de crédito não encontrado"))

        if (!card.isActive) {
            return Result.failure(Exception("Não é possível realizar compras em um cartão inativo"))
        }

        if (amount > card.availableLimit) {
            return Result.failure(Exception("Limite indisponível no cartão. Disponível: R$ ${String.format("%.2f", card.availableLimit)}"))
        }

        // Determine Invoice cycle (YYYY-MM)
        val cycle = determineInvoiceCycle(purchaseDate, card.closingDay, card.dueDay)
        var invoice = invoiceDao.getInvoiceByCardAndMonth(cardId, cycle.referenceMonth)

        if (invoice == null) {
            invoice = InvoiceEntity(
                cardId = cardId,
                referenceMonth = cycle.referenceMonth,
                closingDate = cycle.closingDate,
                dueDate = cycle.dueDate,
                totalAmount = amount,
                status = "OPEN"
            )
            val invId = invoiceDao.insertInvoice(invoice)
            invoice = invoice.copy(id = invId)
        } else {
            val updatedTotal = invoice.totalAmount + amount
            invoiceDao.updateInvoice(invoice.copy(totalAmount = updatedTotal))
        }

        // Update card limit
        val newCommitted = card.committedLimit + amount
        val newAvailable = card.creditLimit - newCommitted
        creditCardDao.updateLimits(cardId, newCommitted, newAvailable)

        // Insert Transaction
        val tx = TransactionEntity(
            type = "EXPENSE",
            amount = amount,
            description = description,
            date = purchaseDate,
            cardId = cardId,
            invoiceId = invoice.id,
            categoryId = categoryId,
            status = "COMPLETED"
        )

        val txId = transactionDao.insertTransaction(tx)
        return Result.success(txId)
    }

    suspend fun payInvoice(invoiceId: Long, accountId: Long, paymentAmount: Double): Result<Unit> {
        val invoice = invoiceDao.getInvoiceById(invoiceId)
            ?: return Result.failure(Exception("Fatura não encontrada"))

        if (invoice.status == "PAID") {
            return Result.failure(Exception("Esta fatura já foi paga"))
        }

        val account = accountDao.getAccountById(accountId)
            ?: return Result.failure(Exception("Conta de pagamento não encontrada"))

        if (account.currentBalance < paymentAmount) {
            return Result.failure(Exception("Saldo insuficiente na conta ${account.name}"))
        }

        val card = creditCardDao.getCardById(invoice.cardId)
            ?: return Result.failure(Exception("Cartão associado não encontrado"))

        // Debit account
        accountDao.updateBalance(accountId, account.currentBalance - paymentAmount)

        // Mark invoice as PAID
        invoiceDao.updateInvoice(invoice.copy(paidAmount = paymentAmount, status = "PAID"))

        // Release credit limit
        val newCommitted = Math.max(0.0, card.committedLimit - paymentAmount)
        val newAvailable = card.creditLimit - newCommitted
        creditCardDao.updateLimits(card.id, newCommitted, newAvailable)

        // Add payment transaction record
        val tx = TransactionEntity(
            type = "EXPENSE",
            amount = paymentAmount,
            description = "Pagamento Fatura ${card.name} (${invoice.referenceMonth})",
            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            accountId = accountId,
            cardId = card.id,
            invoiceId = invoiceId,
            isInvoicePayment = true,
            status = "COMPLETED"
        )
        transactionDao.insertTransaction(tx)

        return Result.success(Unit)
    }

    private fun determineInvoiceCycle(dateStr: String, closingDay: Int, dueDay: Int): InvoiceCycle {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = try { sdf.parse(dateStr) ?: Date() } catch (e: Exception) { Date() }

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1 // 1-12
        val day = cal.get(Calendar.DAY_OF_MONTH)

        var closingYear = year
        var closingMonth = month
        if (day >= closingDay) {
            closingMonth += 1
            if (closingMonth > 12) {
                closingMonth = 1
                closingYear += 1
            }
        }

        var dueYear = closingYear
        var dueMonth = closingMonth
        if (dueDay <= closingDay) {
            dueMonth += 1
            if (dueMonth > 12) {
                dueMonth = 1
                dueYear += 1
            }
        }

        val refMonth = String.format("%04d-%02d", dueYear, dueMonth)
        val closingDate = String.format("%04d-%02d-%02d", closingYear, closingMonth, Math.min(closingDay, 28))
        val dueDate = String.format("%04d-%02d-%02d", dueYear, dueMonth, Math.min(dueDay, 28))

        return InvoiceCycle(refMonth, closingDate, dueDate)
    }

    private data class InvoiceCycle(
        val referenceMonth: String,
        val closingDate: String,
        val dueDate: String
    )

    // --- Categories ---
    suspend fun addCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(id: Long) {
        categoryDao.softDeleteCategory(id)
    }

    // --- Budgets ---
    suspend fun addBudget(budget: BudgetEntity): Long {
        return budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: BudgetEntity) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(id: Long) {
        budgetDao.deleteBudget(id)
    }

    // --- Goals ---
    suspend fun addGoal(goal: GoalEntity): Long {
        return goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(id: Long) {
        goalDao.deleteContributionsForGoal(id)
        goalDao.deleteGoal(id)
    }

    suspend fun addGoalContribution(
        goalId: Long,
        amount: Double,
        type: String, // CONTRIBUTION or WITHDRAWAL
        date: String,
        description: String?
    ): Result<Unit> {
        val goal = goalDao.getGoalById(goalId)
            ?: return Result.failure(Exception("Meta financeira não encontrada"))

        if (type == "WITHDRAWAL" && amount > goal.currentAmount) {
            return Result.failure(Exception("Valor do resgate não pode exceder o saldo acumulado"))
        }

        val newAmount = if (type == "CONTRIBUTION") {
            goal.currentAmount + amount
        } else {
            goal.currentAmount - amount
        }

        val newStatus = if (newAmount >= goal.targetAmount) "COMPLETED" else "ACTIVE"

        goalDao.insertContribution(
            GoalContributionEntity(
                goalId = goalId,
                amount = amount,
                type = type,
                date = date,
                description = description
            )
        )

        goalDao.updateGoal(goal.copy(currentAmount = newAmount, status = newStatus))
        return Result.success(Unit)
    }

    fun getGoalContributions(goalId: Long): Flow<List<GoalContributionEntity>> {
        return goalDao.getContributionsForGoal(goalId)
    }
}
