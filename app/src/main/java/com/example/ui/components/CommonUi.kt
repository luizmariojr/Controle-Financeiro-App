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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.CalendarMonth
import com.example.data.local.entity.AccountEntity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatBRL(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return format.format(amount)
}

fun getCurrentIsoDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

fun getCurrentDisplayDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date())
}

fun isoToDisplayDate(isoDate: String): String {
    if (isoDate.isBlank()) return getCurrentDisplayDate()
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val parsed = isoFormat.parse(isoDate.trim())
        if (parsed != null) displayFormat.format(parsed) else isoDate
    } catch (e: Exception) {
        isoDate
    }
}

fun displayToIsoDate(displayDate: String): String {
    if (displayDate.isBlank()) return getCurrentIsoDate()
    return try {
        val trimmed = displayDate.trim()
        if (trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            return trimmed
        }
        val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsed = displayFormat.parse(trimmed)
        if (parsed != null) isoFormat.format(parsed) else getCurrentIsoDate()
    } catch (e: Exception) {
        getCurrentIsoDate()
    }
}

fun getCurrentMonthYear(): String {
    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    return sdf.format(Date())
}

fun parseColorHex(hex: String?, defaultColor: Color = Color(0xFF10B981)): Color {
    if (hex.isNullOrEmpty()) return defaultColor
    return try {
        val colorInt = android.graphics.Color.parseColor(hex)
        Color(colorInt)
    } catch (e: Exception) {
        defaultColor
    }
}

fun getIconVector(iconName: String?): ImageVector {
    return when (iconName) {
        "Home" -> Icons.Default.Home
        "Restaurant" -> Icons.Default.Restaurant
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "DirectionsBus" -> Icons.Default.DirectionsBus
        "LocalHospital" -> Icons.Default.LocalHospital
        "School" -> Icons.Default.School
        "ConfirmationNumber" -> Icons.Default.ConfirmationNumber
        "ShoppingBag" -> Icons.Default.ShoppingBag
        "CreditCard" -> Icons.Default.CreditCard
        "Work", "Briefcase" -> Icons.Default.Work
        "TrendingUp" -> Icons.Default.TrendingUp
        "AccountBalance" -> Icons.Default.AccountBalance
        "Savings" -> Icons.Default.Savings
        "Wallet" -> Icons.Default.Wallet
        "PieChart" -> Icons.Default.PieChart
        "Payment" -> Icons.Default.Payment
        "AddCircle" -> Icons.Default.AddCircle
        "Target" -> Icons.Default.Flag
        else -> Icons.Default.Category
    }
}

@Composable
fun SummaryStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBgColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconBgColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onActionClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

data class BankColorPreset(val name: String, val hex: String)

val BRAZIL_BANK_COLORS = listOf(
    BankColorPreset("Nubank", "#820AD1"),
    BankColorPreset("Itaú", "#EC7000"),
    BankColorPreset("Bradesco", "#CC092F"),
    BankColorPreset("Santander", "#EC0000"),
    BankColorPreset("Banco do Brasil", "#0038A8"),
    BankColorPreset("Caixa", "#005CA9"),
    BankColorPreset("Inter", "#FF7A00"),
    BankColorPreset("C6 Bank", "#242424"),
    BankColorPreset("BTG Pactual", "#001E3C"),
    BankColorPreset("Next", "#00D657"),
    BankColorPreset("PicPay", "#11C76F"),
    BankColorPreset("Mercado Pago", "#009EE3"),
    BankColorPreset("PagBank", "#00A859"),
    BankColorPreset("Neon", "#00E5FF"),
    BankColorPreset("XP", "#1B1B1B"),
    BankColorPreset("Safra", "#D4AF37"),
    BankColorPreset("Sicoob", "#00363A"),
    BankColorPreset("Sicredi", "#3FA535"),
    BankColorPreset("Will Bank", "#FFD600"),
    BankColorPreset("Banrisul", "#0054A6"),
    BankColorPreset("Sofisa", "#002F6C"),
    BankColorPreset("Wise", "#25C974"),
    BankColorPreset("Nomad", "#2B2D42"),
    BankColorPreset("Avenue", "#0A192F")
)

val GENERAL_COLOR_PALETTE = listOf(
    "#10B981", "#059669", "#15803D", "#84CC16", "#22C55E",
    "#3B82F6", "#2563EB", "#0284C7", "#0EA5E9", "#06B6D4",
    "#8B5CF6", "#7C3AED", "#6366F1", "#A855F7", "#EC4899", "#D946EF",
    "#EF4444", "#DC2626", "#F97316", "#EA580C", "#F59E0B", "#EAB308",
    "#64748B", "#475569", "#334155", "#1E293B", "#0F172A", "#78716C"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorOptionPicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit
) {
    var showExpandedPicker by remember { mutableStateOf(false) }

    // Popular bank + standard colors for quick row
    val quickColors = remember {
        listOf(
            "#820AD1", // Nubank
            "#EC7000", // Itaú
            "#CC092F", // Bradesco
            "#EC0000", // Santander
            "#0038A8", // BB
            "#FF7A00", // Inter
            "#005CA9", // Caixa
            "#242424", // C6 Bank
            "#10B981", // Green
            "#3B82F6", // Blue
            "#EF4444"  // Red
        )
    }

    val selectedBank = remember(selectedColorHex) {
        BRAZIL_BANK_COLORS.find { it.hex.equals(selectedColorHex, ignoreCase = true) }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        // Horizontal scrollable quick selection row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickColors.forEach { hex ->
                val color = parseColorHex(hex)
                val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 2.5.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }

            // "Mais Cores" button
            Surface(
                onClick = { showExpandedPicker = true },
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.height(34.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Mais cores",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "+ Mais Cores",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Indicator displaying current selected bank or color code
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(parseColorHex(selectedColorHex))
            )
            Text(
                text = if (selectedBank != null) "Banco: ${selectedBank.name}" else "Cor: $selectedColorHex",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (showExpandedPicker) {
        ExpandedColorPickerDialog(
            initialColorHex = selectedColorHex,
            onDismiss = { showExpandedPicker = false },
            onColorSelected = {
                onColorSelected(it)
                showExpandedPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedColorPickerDialog(
    initialColorHex: String,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    var selectedHex by remember { mutableStateOf(initialColorHex) }
    var selectedTab by remember { mutableStateOf(0) }
    var customHexInput by remember { mutableStateOf(initialColorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Cores dos Bancos e Cartões",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(parseColorHex(selectedHex))
                            .border(1.dp, Color.LightGray, CircleShape)
                    )
                    val bankName = BRAZIL_BANK_COLORS.find { it.hex.equals(selectedHex, ignoreCase = true) }?.name
                    Text(
                        text = if (bankName != null) "Padrão: $bankName ($selectedHex)" else "Selecionada: $selectedHex",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Bancos", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Paleta", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Código HEX", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(BRAZIL_BANK_COLORS) { bank ->
                                val isSelected = selectedHex.equals(bank.hex, ignoreCase = true)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedHex = bank.hex },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(parseColorHex(bank.hex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = bank.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(GENERAL_COLOR_PALETTE) { hex ->
                                val isSelected = selectedHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(parseColorHex(hex))
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedHex = hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Informe a cor exata do seu banco ou preferências personalizadas:",
                                style = MaterialTheme.typography.bodySmall
                            )

                            OutlinedTextField(
                                value = customHexInput,
                                onValueChange = { input ->
                                    val formatted = if (!input.startsWith("#") && input.isNotBlank()) "#$input" else input
                                    customHexInput = formatted
                                    if (formatted.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$"))) {
                                        selectedHex = formatted
                                    }
                                },
                                label = { Text("Código HEX (#RRGGBB)") },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(parseColorHex(selectedHex))
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "💡 Você pode inserir qualquer código hexadecimal de cor de 6 dígitos para combinar perfeitamente com a identidade visual da sua instituição financeira.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(selectedHex)
                }
            ) {
                Text("Usar esta Cor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Reusable Date Picker component that displays and edits dates in DD/MM/YYYY format
 * and allows clicking the calendar icon or input to open an interactive mini calendar dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    dateValue: String, // Accepts both DD/MM/YYYY or YYYY-MM-DD
    onDateChange: (String) -> Unit, // Returns in DD/MM/YYYY format
    label: String = "Data (DD/MM/AAAA)",
    modifier: Modifier = Modifier
) {
    var showModal by remember { mutableStateOf(false) }

    // Parse current date to epoch millis for DatePicker initial state
    val initialMillis = remember(dateValue) {
        try {
            val iso = displayToIsoDate(dateValue)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.parse(iso)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    val displayString = if (dateValue.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
        isoToDisplayDate(dateValue)
    } else {
        dateValue
    }

    OutlinedTextField(
        value = displayString,
        onValueChange = { input ->
            // Allow manual typing of DD/MM/YYYY
            val filtered = input.filter { it.isDigit() || it == '/' }.take(10)
            onDateChange(filtered)
        },
        label = { Text(label) },
        placeholder = { Text("DD/MM/AAAA") },
        trailingIcon = {
            IconButton(onClick = { showModal = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Selecionar data no calendário",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        singleLine = true,
        modifier = modifier
    )

    if (showModal) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showModal = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            // DatePicker millis is in UTC. Format to dd/MM/yyyy
                            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                            cal.timeInMillis = selectedMillis
                            val day = String.format(Locale.getDefault(), "%02d", cal.get(java.util.Calendar.DAY_OF_MONTH))
                            val month = String.format(Locale.getDefault(), "%02d", cal.get(java.util.Calendar.MONTH) + 1)
                            val year = cal.get(java.util.Calendar.YEAR)
                            onDateChange("$day/$month/$year")
                        }
                        showModal = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showModal = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Selecione a data",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceAdjustmentDialog(
    accounts: List<AccountEntity>,
    initialAccountId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (accountId: Long, newBalance: Double, date: String, notes: String?) -> Unit
) {
    var selectedAccountId by remember {
        mutableStateOf(initialAccountId ?: accounts.firstOrNull()?.id ?: 0L)
    }
    var expandedAccountDropdown by remember { mutableStateOf(false) }

    val selectedAccount = accounts.find { it.id == selectedAccountId } ?: accounts.firstOrNull()

    var newBalanceStr by remember(selectedAccount) {
        mutableStateOf(selectedAccount?.currentBalance?.toString() ?: "0.0")
    }
    var displayDate by remember { mutableStateOf(getCurrentDisplayDate()) }
    var notes by remember { mutableStateOf("") }

    val currentBal = selectedAccount?.currentBalance ?: 0.0
    val newBal = newBalanceStr.replace(",", ".").toDoubleOrNull() ?: currentBal
    val diff = newBal - currentBal

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Lançamento / Ajuste de Saldo", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (accounts.size > 1) {
                    ExposedDropdownMenuBox(
                        expanded = expandedAccountDropdown,
                        onExpandedChange = { expandedAccountDropdown = !expandedAccountDropdown }
                    ) {
                        OutlinedTextField(
                            value = selectedAccount?.name ?: "Selecione uma conta",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Conta Bancária") },
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
                } else if (selectedAccount != null) {
                    Text(
                        text = "Conta: ${selectedAccount.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Saldo Atual Registrado:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatBRL(currentBal),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = newBalanceStr,
                    onValueChange = { newBalanceStr = it },
                    label = { Text("Novo Saldo Final Desejado (R$) *") },
                    supportingText = {
                        val diffFormatted = formatBRL(Math.abs(diff))
                        if (diff > 0.001) {
                            Text("Ajuste: +$diffFormatted (Receita de Ajuste)", color = com.example.ui.theme.IncomeGreen)
                        } else if (diff < -0.001) {
                            Text("Ajuste: -$diffFormatted (Despesa de Ajuste)", color = com.example.ui.theme.ExpenseRed)
                        } else {
                            Text("Sem alteração de valor")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                DatePickerField(
                    dateValue = displayDate,
                    onDateChange = { displayDate = it },
                    label = "Data do Ajuste"
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observação / Motivo (Opcional)") },
                    placeholder = { Text("Ex: Conferência de extrato mensal") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAccount != null) {
                        val isoDate = displayToIsoDate(displayDate)
                        onConfirm(selectedAccount.id, newBal, isoDate, notes)
                    }
                }
            ) {
                Text("Confirmar Ajuste")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

