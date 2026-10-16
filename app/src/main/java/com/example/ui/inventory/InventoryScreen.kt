package com.example.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.ProductEntity
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.BriqueiCard
import com.example.ui.components.EmptyState
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun InventoryScreen(viewModel: MainViewModel) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, AVAILABLE, RESERVED, SOLD
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("TODAS") }

    val categories = listOf("TODAS", "Smartphones", "iPhones", "Tablets", "Notebooks", "Smartwatches", "Videogames", "Outros")

    val filteredProducts = remember(products, selectedFilter, searchQuery, selectedCategory) {
        products.filter { prod ->
            val matchesFilter = when (selectedFilter) {
                "AVAILABLE" -> prod.status == "AVAILABLE"
                "RESERVED" -> prod.status == "RESERVED"
                "SOLD" -> prod.status == "SOLD"
                else -> true
            }
            val matchesCategory = selectedCategory == "TODAS" || prod.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.brand.contains(searchQuery, ignoreCase = true) ||
                    prod.model.contains(searchQuery, ignoreCase = true) ||
                    prod.internalCode.contains(searchQuery, ignoreCase = true) ||
                    (prod.imei?.contains(searchQuery) == true)

            matchesFilter && matchesCategory && matchesSearch
        }
    }

    val totalStockValue = remember(products) {
        products.filter { it.status == "AVAILABLE" }.sumOf { it.purchasePrice + it.additionalCosts }
    }
    val potentialProfit = remember(products) {
        products.filter { it.status == "AVAILABLE" }.sumOf { it.announcedPrice - (it.purchasePrice + it.additionalCosts) }
    }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.ADD_EDIT_PRODUCT) },
                containerColor = BriqueiLime,
                contentColor = Color.Black
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Cadastrar Produto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Header & Search
            Text(
                text = "Controle de Estoque",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Pesquisar produto, IMEI ou código...", color = TextMuted) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpar", tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BriqueiLime,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Summary banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Investimento Disponível", fontSize = 11.sp, color = TextSecondary)
                    Text(text = "R$ ${"%,.2f".format(totalStockValue)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Margem Potencial", fontSize = 11.sp, color = TextSecondary)
                    Text(text = "+ R$ ${"%,.2f".format(potentialProfit)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BriqueiLime)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTabChip("Todos", selected = selectedFilter == "ALL") { selectedFilter = "ALL" }
                FilterTabChip("Disponíveis", selected = selectedFilter == "AVAILABLE") { selectedFilter = "AVAILABLE" }
                FilterTabChip("Reservados", selected = selectedFilter == "RESERVED") { selectedFilter = "RESERVED" }
                FilterTabChip("Vendidos", selected = selectedFilter == "SOLD") { selectedFilter = "SOLD" }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Chips Row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) BriqueiCyan.copy(alpha = 0.2f) else DarkSurface)
                            .border(1.dp, if (isSelected) BriqueiCyan else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            color = if (isSelected) BriqueiCyan else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Product List
            if (filteredProducts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Inventory2,
                    title = "Nenhum produto encontrado",
                    description = if (searchQuery.isNotEmpty()) "Tente buscar com outros termos." else "Cadastre seu primeiro produto para gerenciar o estoque.",
                    buttonText = if (searchQuery.isEmpty()) "Adicionar Produto" else null,
                    onButtonClick = { viewModel.navigateTo(AppScreen.ADD_EDIT_PRODUCT) }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            onClick = { viewModel.selectProduct(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) BriqueiLime else DarkSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.Black else TextSecondary
        )
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    onClick: () -> Unit
) {
    val totalCost = product.purchasePrice + product.additionalCosts
    val profit = if (product.status == "SOLD") {
        (product.soldPrice ?: 0.0) - totalCost
    } else {
        product.announcedPrice - totalCost
    }
    val marginPercent = if (totalCost > 0) (profit / totalCost) * 100 else 0.0

    BriqueiCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.internalCode,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BriqueiCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${product.origin}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${product.condition} • ${product.category}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            StatusBadge(status = product.status)
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Custo Total", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = "R$ ${"%.2f".format(totalCost)}",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (product.status == "SOLD") "Preço Venda" else "Preço Anunciado",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = "R$ ${"%.2f".format(if (product.status == "SOLD") product.soldPrice ?: 0.0 else product.announcedPrice)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (product.status == "SOLD") BriqueiCyan else TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (product.status == "SOLD") "Lucro Realizado" else "Margem Estimada",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = "+ R$ ${"%.2f".format(profit)} (${marginPercent.toInt()}%)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (profit >= 0) BriqueiLime else BriqueiRed
                )
            }
        }
    }
}
