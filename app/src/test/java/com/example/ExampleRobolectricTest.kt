package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.CreditCardEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.FinancialRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: FinancialRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FinancialRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Controle Financeiro", appName)
    }

    @Test
    fun `add income increases account balance`() = runBlocking {
        val account = AccountEntity(
            name = "Conta Corrente",
            type = "CONTA_CORRENTE",
            institution = "Banco",
            initialBalance = 1000.0,
            currentBalance = 1000.0
        )
        val accId = repository.addAccount(account)

        val res = repository.addIncome(
            amount = 500.0,
            description = "Salário",
            date = "2026-10-10",
            accountId = accId,
            categoryId = null,
            notes = null
        )
        assertTrue(res.isSuccess)

        val updatedAccount = db.accountDao().getAccountById(accId)
        assertNotNull(updatedAccount)
        assertEquals(1500.0, updatedAccount?.currentBalance ?: 0.0, 0.001)
    }

    @Test
    fun `add expense decreases account balance`() = runBlocking {
        val account = AccountEntity(
            name = "Conta Corrente",
            type = "CONTA_CORRENTE",
            institution = "Banco",
            initialBalance = 1000.0,
            currentBalance = 1000.0
        )
        val accId = repository.addAccount(account)

        val res = repository.addExpense(
            amount = 350.0,
            description = "Supermercado",
            date = "2026-10-10",
            accountId = accId,
            categoryId = null,
            notes = null
        )
        assertTrue(res.isSuccess)

        val updatedAccount = db.accountDao().getAccountById(accId)
        assertNotNull(updatedAccount)
        assertEquals(650.0, updatedAccount?.currentBalance ?: 0.0, 0.001)
    }

    @Test
    fun `add transfer debits source and credits destination`() = runBlocking {
        val accSrc = AccountEntity(
            name = "Origem",
            type = "CONTA_CORRENTE",
            institution = "Banco A",
            initialBalance = 1000.0,
            currentBalance = 1000.0
        )
        val accDst = AccountEntity(
            name = "Destino",
            type = "POUPANCA",
            institution = "Banco B",
            initialBalance = 500.0,
            currentBalance = 500.0
        )
        val srcId = repository.addAccount(accSrc)
        val dstId = repository.addAccount(accDst)

        val res = repository.addTransfer(
            amount = 400.0,
            description = "Poupança",
            date = "2026-10-10",
            sourceAccountId = srcId,
            destinationAccountId = dstId,
            notes = null
        )
        assertTrue(res.isSuccess)

        val updatedSrc = db.accountDao().getAccountById(srcId)
        val updatedDst = db.accountDao().getAccountById(dstId)
        assertEquals(600.0, updatedSrc?.currentBalance ?: 0.0, 0.001)
        assertEquals(900.0, updatedDst?.currentBalance ?: 0.0, 0.001)
    }

    @Test
    fun `revert expense restores account balance`() = runBlocking {
        val account = AccountEntity(
            name = "Conta Corrente",
            type = "CONTA_CORRENTE",
            institution = "Banco",
            initialBalance = 1000.0,
            currentBalance = 1000.0
        )
        val accId = repository.addAccount(account)

        val res = repository.addExpense(
            amount = 200.0,
            description = "Lanche",
            date = "2026-10-10",
            accountId = accId,
            categoryId = null,
            notes = null
        )
        val txId = res.getOrThrow()

        val revertRes = repository.revertTransaction(txId)
        assertTrue(revertRes.isSuccess)

        val restoredAccount = db.accountDao().getAccountById(accId)
        assertEquals(1000.0, restoredAccount?.currentBalance ?: 0.0, 0.001)
    }

    @Test
    fun `credit card purchase and payment lifecycle`() = runBlocking {
        val bankAcc = AccountEntity(
            name = "Conta Débito",
            type = "CONTA_CORRENTE",
            institution = "Banco",
            initialBalance = 3000.0,
            currentBalance = 3000.0
        )
        val bankId = repository.addAccount(bankAcc)

        val card = CreditCardEntity(
            name = "Cartão Teste",
            bankName = "Banco",
            brand = "Mastercard",
            creditLimit = 2000.0,
            availableLimit = 2000.0,
            closingDay = 10,
            dueDay = 20
        )
        val cardId = repository.addCreditCard(card)

        val purchaseRes = repository.addCardPurchase(
            cardId = cardId,
            amount = 450.0,
            purchaseDate = "2026-10-05",
            description = "Eletrodoméstico",
            categoryId = null
        )
        assertTrue(purchaseRes.isSuccess)

        val cardAfterPurchase = db.creditCardDao().getCardById(cardId)
        assertEquals(1550.0, cardAfterPurchase?.availableLimit ?: 0.0, 0.001)
        assertEquals(450.0, cardAfterPurchase?.committedLimit ?: 0.0, 0.001)

        val invoice = db.invoiceDao().getInvoiceByCardAndMonth(cardId, "2026-10")
        assertNotNull(invoice)
        assertEquals(450.0, invoice?.totalAmount ?: 0.0, 0.001)

        // Pay invoice
        val payRes = repository.payInvoice(
            invoiceId = invoice!!.id,
            accountId = bankId,
            paymentAmount = 450.0
        )
        assertTrue(payRes.isSuccess)

        val cardAfterPay = db.creditCardDao().getCardById(cardId)
        assertEquals(2000.0, cardAfterPay?.availableLimit ?: 0.0, 0.001)
        assertEquals(0.0, cardAfterPay?.committedLimit ?: 0.0, 0.001)

        val bankAfterPay = db.accountDao().getAccountById(bankId)
        assertEquals(2550.0, bankAfterPay?.currentBalance ?: 0.0, 0.001)
    }

    @Test
    fun `financial goal contribution and completion`() = runBlocking {
        val goal = GoalEntity(
            name = "Reserva de Emergência",
            targetAmount = 1000.0,
            currentAmount = 0.0
        )
        val goalId = repository.addGoal(goal)

        val contribRes = repository.addGoalContribution(
            goalId = goalId,
            amount = 1000.0,
            type = "CONTRIBUTION",
            date = "2026-10-10",
            description = "Aporte integral"
        )
        assertTrue(contribRes.isSuccess)

        val updatedGoal = db.goalDao().getGoalById(goalId)
        assertEquals(1000.0, updatedGoal?.currentAmount ?: 0.0, 0.001)
        assertEquals("COMPLETED", updatedGoal?.status)
    }

    @Test
    fun `edit existing credit card updates attributes and recalculates limits`() = runBlocking {
        val card = CreditCardEntity(
            name = "Cartão Original",
            bankName = "Banco Antigo",
            brand = "Visa",
            creditLimit = 3000.0,
            committedLimit = 500.0,
            availableLimit = 2500.0,
            closingDay = 5,
            dueDay = 15,
            colorHex = "#820AD1"
        )
        val cardId = repository.addCreditCard(card)

        // Update card with new limit of 5000.0 (committed remains 500.0, available becomes 4500.0)
        val updatedCard = card.copy(
            id = cardId,
            name = "Cartão Atualizado",
            bankName = "Banco Novo",
            brand = "Mastercard",
            creditLimit = 5000.0,
            availableLimit = 4500.0,
            closingDay = 10,
            dueDay = 20
        )
        repository.updateCreditCard(updatedCard)

        val retrieved = db.creditCardDao().getCardById(cardId)
        assertNotNull(retrieved)
        assertEquals("Cartão Atualizado", retrieved?.name)
        assertEquals("Banco Novo", retrieved?.bankName)
        assertEquals("Mastercard", retrieved?.brand)
        assertEquals(5000.0, retrieved?.creditLimit ?: 0.0, 0.001)
        assertEquals(4500.0, retrieved?.availableLimit ?: 0.0, 0.001)
        assertEquals(500.0, retrieved?.committedLimit ?: 0.0, 0.001)
        assertEquals(10, retrieved?.closingDay)
        assertEquals(20, retrieved?.dueDay)
    }

    @Test
    fun `inactive credit card blocks new purchases and reactivates safely`() = runBlocking {
        val card = CreditCardEntity(
            name = "Cartão Secundário",
            creditLimit = 2000.0,
            availableLimit = 2000.0,
            closingDay = 10,
            dueDay = 20,
            isActive = true
        )
        val cardId = repository.addCreditCard(card)

        // Deactivate card
        repository.setCardActive(cardId, false)
        val inactiveCard = db.creditCardDao().getCardById(cardId)
        assertNotNull(inactiveCard)
        assertEquals(false, inactiveCard?.isActive)

        // Attempt purchase on inactive card -> must fail
        val purchaseRes = repository.addCardPurchase(
            cardId = cardId,
            amount = 100.0,
            purchaseDate = "2026-10-10",
            description = "Tentativa em Cartão Inativo",
            categoryId = null
        )
        assertTrue(purchaseRes.isFailure)
        assertTrue(purchaseRes.exceptionOrNull()?.message?.contains("inativo") == true)

        // Reactivate card
        repository.setCardActive(cardId, true)
        val reactivatedCard = db.creditCardDao().getCardById(cardId)
        assertEquals(true, reactivatedCard?.isActive)

        // Now purchase succeeds
        val validPurchase = repository.addCardPurchase(
            cardId = cardId,
            amount = 100.0,
            purchaseDate = "2026-10-10",
            description = "Compra após Reativação",
            categoryId = null
        )
        assertTrue(validPurchase.isSuccess)
    }
}
