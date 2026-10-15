package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.BalanceAdjustmentDialog
import com.example.ui.components.QuickExpenseDialog
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CreditCardsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.ControleFinanceiroTheme
import com.example.ui.viewmodel.FinancialViewModel
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Início", Icons.Default.Wallet)
    object Accounts : Screen("accounts", "Contas", Icons.Default.AccountBalance)
    object Transactions : Screen("transactions", "Extrato", Icons.Default.SwapHoriz)
    object CreditCards : Screen("credit_cards", "Cartões", Icons.Default.CreditCard)
    object More : Screen("more", "Mais", Icons.Default.MoreHoriz)
    object Categories : Screen("categories", "Categorias", Icons.Default.Category)
    object Budgets : Screen("budgets", "Orçamentos", Icons.Default.PieChart)
    object Goals : Screen("goals", "Metas", Icons.Default.Flag)
}

class MainActivity : ComponentActivity() {

    private val viewModel: FinancialViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ControleFinanceiroTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                // Quick Transaction Dialog state (null = closed, or "EXPENSE", "INCOME", "TRANSFER")
                var quickDialogType by remember { mutableStateOf<String?>(null) }
                var showBalanceAdjustmentMoreDialog by remember { mutableStateOf(false) }

                // StateFlow observation
                val summaryState by viewModel.summaryState.collectAsStateWithLifecycle()
                val accounts by viewModel.accounts.collectAsStateWithLifecycle()
                val activeAccounts by viewModel.activeAccounts.collectAsStateWithLifecycle()
                val transactions by viewModel.transactions.collectAsStateWithLifecycle()
                val categories by viewModel.categories.collectAsStateWithLifecycle()
                val creditCards by viewModel.creditCards.collectAsStateWithLifecycle()
                val invoices by viewModel.invoices.collectAsStateWithLifecycle()
                val budgets by viewModel.budgets.collectAsStateWithLifecycle()
                val goals by viewModel.goals.collectAsStateWithLifecycle()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

                val navigateToTab: (String) -> Unit = { route ->
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }

                if (currentRoute != Screen.Dashboard.route) {
                    BackHandler {
                        if (currentRoute == Screen.Categories.route || currentRoute == Screen.Budgets.route || currentRoute == Screen.Goals.route) {
                            navigateToTab(Screen.More.route)
                        } else {
                            navigateToTab(Screen.Dashboard.route)
                        }
                    }
                }

                // Clean 5-item Bottom Navigation Bar
                val bottomNavItems = listOf(
                    Screen.Dashboard,
                    Screen.Accounts,
                    Screen.Transactions,
                    Screen.CreditCards,
                    Screen.More
                )

                val allScreens = listOf(
                    Screen.Dashboard,
                    Screen.Accounts,
                    Screen.Transactions,
                    Screen.CreditCards,
                    Screen.More,
                    Screen.Categories,
                    Screen.Budgets,
                    Screen.Goals
                )

                // Message Toast / Snackbar events
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    viewModel.messageEvents.collectLatest { msg ->
                        snackbarHostState.showSnackbar(msg)
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = allScreens.find { it.route == currentRoute }?.title ?: "Controle Financeiro",
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            bottomNavItems.forEach { screen ->
                                val isSelected = currentRoute == screen.route ||
                                        (screen == Screen.More && (currentRoute == Screen.Categories.route || currentRoute == Screen.Budgets.route || currentRoute == Screen.Goals.route))

                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    selected = isSelected,
                                    alwaysShowLabel = true,
                                    onClick = { navigateToTab(screen.route) },
                                    modifier = Modifier.testTag("nav_item_${screen.route}")
                                )
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Dashboard.route,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable(Screen.Dashboard.route) {
                                DashboardScreen(
                                    summaryState = summaryState,
                                    accounts = activeAccounts,
                                    transactions = transactions,
                                    onQuickExpenseClick = { quickDialogType = "EXPENSE" },
                                    onQuickIncomeClick = { quickDialogType = "INCOME" },
                                    onQuickTransferClick = { quickDialogType = "TRANSFER" },
                                    onViewAccountsClick = { navigateToTab(Screen.Accounts.route) },
                                    onViewTransactionsClick = { navigateToTab(Screen.Transactions.route) }
                                )
                            }

                            composable(Screen.Accounts.route) {
                                AccountsScreen(
                                    accounts = accounts,
                                    onAddAccount = { name, type, inst, bal, color ->
                                        viewModel.addAccount(name, type, inst, bal, color)
                                    },
                                    onUpdateAccount = { acc -> viewModel.updateAccount(acc) },
                                    onToggleActive = { id, isActive -> viewModel.toggleAccountActive(id, isActive) },
                                    onAdjustBalance = { id, newBal, date, notes -> viewModel.adjustAccountBalance(id, newBal, date, notes) },
                                    onDeleteAccount = { id -> viewModel.deleteAccount(id) }
                                )
                            }

                            composable(Screen.Transactions.route) {
                                TransactionsScreen(
                                    transactions = transactions,
                                    accounts = activeAccounts,
                                    categories = categories,
                                    onAddIncome = { amt, desc, date, accId, catId, notes ->
                                        viewModel.addIncome(amt, desc, date, accId, catId, notes)
                                    },
                                    onAddExpense = { amt, desc, date, accId, catId, notes ->
                                        viewModel.addExpense(amt, desc, date, accId, catId, notes)
                                    },
                                    onAddTransfer = { amt, desc, date, srcId, dstId, notes ->
                                        viewModel.addTransfer(amt, desc, date, srcId, dstId, notes)
                                    },
                                    onRevertTransaction = { id -> viewModel.revertTransaction(id) }
                                )
                            }

                            composable(Screen.CreditCards.route) {
                                CreditCardsScreen(
                                    creditCards = creditCards,
                                    invoices = invoices,
                                    accounts = activeAccounts,
                                    categories = categories,
                                    onAddCreditCard = { name, bank, brand, last4, limit, closing, due, color ->
                                        viewModel.addCreditCard(name, bank, brand, last4, limit, closing, due, color)
                                    },
                                    onUpdateCreditCard = { cardId, name, bank, brand, last4, limit, closing, due, color ->
                                        viewModel.updateCreditCard(cardId, name, bank, brand, last4, limit, closing, due, color)
                                    },
                                    onAddCardPurchase = { cardId, amt, date, desc, catId ->
                                        viewModel.addCardPurchase(cardId, amt, date, desc, catId)
                                    },
                                    onPayInvoice = { invId, accId, amt ->
                                        viewModel.payInvoice(invId, accId, amt)
                                    },
                                    onToggleCardActive = { cardId, isActive ->
                                        viewModel.toggleCardActive(cardId, isActive)
                                    }
                                )
                            }

                            composable(Screen.More.route) {
                                MoreScreen(
                                    onNavigateToBudgets = { navigateToTab(Screen.Budgets.route) },
                                    onNavigateToGoals = { navigateToTab(Screen.Goals.route) },
                                    onNavigateToCategories = { navigateToTab(Screen.Categories.route) },
                                    onOpenBalanceAdjustment = { showBalanceAdjustmentMoreDialog = true }
                                )
                            }

                            composable(Screen.Categories.route) {
                                CategoriesScreen(
                                    categories = categories,
                                    onAddCategory = { name, type, parentId, color, icon ->
                                        viewModel.addCategory(name, type, parentId, color, icon)
                                    },
                                    onDeleteCategory = { id -> viewModel.deleteCategory(id) }
                                )
                            }

                            composable(Screen.Budgets.route) {
                                BudgetsScreen(
                                    budgets = budgets,
                                    categories = categories,
                                    transactions = transactions,
                                    onAddBudget = { catId, subId, amt, month, threshold ->
                                        viewModel.addBudget(catId, subId, amt, month, threshold)
                                    },
                                    onDeleteBudget = { id -> viewModel.deleteBudget(id) }
                                )
                            }

                            composable(Screen.Goals.route) {
                                GoalsScreen(
                                    goals = goals,
                                    onAddGoal = { name, desc, target, date, color ->
                                        viewModel.addGoal(name, desc, target, date, color)
                                    },
                                    onAddContribution = { goalId, amt, type, date, desc ->
                                        viewModel.addGoalContribution(goalId, amt, type, date, desc)
                                    },
                                    onDeleteGoal = { id -> viewModel.deleteGoal(id) }
                                )
                            }
                        }

                        quickDialogType?.let { type ->
                            QuickExpenseDialog(
                                initialType = type,
                                accounts = activeAccounts,
                                creditCards = creditCards.filter { it.isActive },
                                categories = categories,
                                onDismiss = { quickDialogType = null },
                                onConfirmAccountExpense = { amt, desc, date, accId, catId, notes ->
                                    viewModel.addExpense(amt, desc, date, accId, catId, notes)
                                    quickDialogType = null
                                },
                                onConfirmCardExpense = { cardId, amt, date, desc, catId ->
                                    viewModel.addCardPurchase(cardId, amt, date, desc, catId)
                                    quickDialogType = null
                                },
                                onConfirmIncome = { amt, desc, date, accId, catId, notes ->
                                    viewModel.addIncome(amt, desc, date, accId, catId, notes)
                                    quickDialogType = null
                                },
                                onConfirmTransfer = { amt, desc, date, srcId, dstId, notes ->
                                    viewModel.addTransfer(amt, desc, date, srcId, dstId, notes)
                                    quickDialogType = null
                                }
                            )
                        }
                        if (showBalanceAdjustmentMoreDialog) {
                            BalanceAdjustmentDialog(
                                accounts = accounts,
                                onDismiss = { showBalanceAdjustmentMoreDialog = false },
                                onConfirm = { accId, newBal, date, notes ->
                                    viewModel.adjustAccountBalance(accId, newBal, date, notes)
                                    showBalanceAdjustmentMoreDialog = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
