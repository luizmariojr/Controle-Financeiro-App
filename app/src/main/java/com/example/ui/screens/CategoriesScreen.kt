package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.local.entity.CategoryEntity
import com.example.ui.components.ColorOptionPicker
import com.example.ui.components.SectionHeader
import com.example.ui.components.getIconVector
import com.example.ui.components.parseColorHex
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@Composable
fun CategoriesScreen(
    categories: List<CategoryEntity>,
    onAddCategory: (name: String, type: String, parentCategoryId: Long?, colorHex: String, iconName: String) -> Unit,
    onDeleteCategory: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // ALL, EXPENSE, INCOME
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredCategories = categories.filter { cat ->
        selectedTypeFilter == "ALL" || cat.type == selectedTypeFilter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_category")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nova Categoria")
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
                title = "Categorias Financeiras",
                subtitle = "Classificação hierárquica de receitas e despesas"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == "ALL",
                    onClick = { selectedTypeFilter = "ALL" },
                    label = { Text("Todas (${categories.size})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedTypeFilter == "EXPENSE",
                    onClick = { selectedTypeFilter = "EXPENSE" },
                    label = { Text("Despesas (${categories.count { it.type == "EXPENSE" }})", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedTypeFilter == "INCOME",
                    onClick = { selectedTypeFilter = "INCOME" },
                    label = { Text("Receitas (${categories.count { it.type == "INCOME" }})", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCategories, key = { it.id }) { category ->
                    CategoryRowItem(
                        category = category,
                        onDeleteClick = { onDeleteCategory(category.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            parentCategories = categories.filter { it.parentCategoryId == null && it.isActive },
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, parentId, color, icon ->
                onAddCategory(name, type, parentId, color, icon)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CategoryRowItem(
    category: CategoryEntity,
    onDeleteClick: () -> Unit
) {
    val color = parseColorHex(category.colorHex)
    val isExpense = category.type == "EXPENSE"

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getIconVector(category.iconName),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isExpense) "Despesa" else "Receita",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isExpense) ExpenseRed else IncomeGreen
                    )
                }
            }

            if (!category.isSystem) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    parentCategories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, parentCategoryId: Long?, colorHex: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    var parentCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedColor by remember { mutableStateOf("#0284C7") }

    var expandedTypeDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Categoria") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Categoria *") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedTypeDropdown,
                    onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = if (type == "EXPENSE") "Despesa" else "Receita",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo Financeiro") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Despesa") },
                            onClick = {
                                type = "EXPENSE"
                                expandedTypeDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Receita") },
                            onClick = {
                                type = "INCOME"
                                expandedTypeDropdown = false
                            }
                        )
                    }
                }

                Text("Cor:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                ColorOptionPicker(selectedColorHex = selectedColor, onColorSelected = { selectedColor = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), type, parentCategoryId, selectedColor, "Category")
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
