package com.example.dinahvision.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dinahvision.R
import com.example.dinahvision.models.PointsCalculator
import com.example.dinahvision.models.Prevision
import com.example.dinahvision.models.User
import com.example.dinahvision.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class PrevisionFilter { CURRENT, WRONG, CORRECT }

private fun getFilterColors(filter: PrevisionFilter): Pair<Color, Color> {
    return when (filter) {
        PrevisionFilter.WRONG -> Pair(Color(0xFFED7E75), Color(0xFF623AA2))
        PrevisionFilter.CORRECT -> Pair(Color(0xFF83E89E), Color(0xFF3FCAD9))
        PrevisionFilter.CURRENT -> Pair(Color(0xFFE0D9F5), Color(0xFFE0D9F5))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Carregar dados iniciais
    LaunchedEffect(Unit) {
        viewModel.loadPrevisions()
    }

    // Filtrar previsões baseado no estado atual
    val currentFilter = PrevisionFilter.CURRENT
    val filteredList = uiState.previsions.filter { p ->
        when (currentFilter) {
            PrevisionFilter.CURRENT -> !p.finished
            PrevisionFilter.WRONG -> p.finished && !p.predicted
            PrevisionFilter.CORRECT -> p.finished && p.predicted
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.home),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.logodinah),
                    contentDescription = "Logo DinahVision",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )

                DinahPointsBadge(points = User.currentUser?.points ?: 0)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterButton(
                        text = "Em andamento",
                        isSelected = currentFilter == PrevisionFilter.CURRENT,
                        colors = getFilterColors(PrevisionFilter.CURRENT),
                        onClick = { /* Implementar filtro se necessário */ }
                    )
                    FilterButton(
                        text = "Erradas",
                        isSelected = currentFilter == PrevisionFilter.WRONG,
                        colors = getFilterColors(PrevisionFilter.WRONG),
                        onClick = { /* Implementar filtro se necessário */ }
                    )
                    FilterButton(
                        text = "Corretas",
                        isSelected = currentFilter == PrevisionFilter.CORRECT,
                        colors = getFilterColors(PrevisionFilter.CORRECT),
                        onClick = { /* Implementar filtro se necessário */ }
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(filteredList) { prevision ->
                        PrevisionCard(
                            prevision = prevision,
                            onMarkCorrect = { viewModel.markPrevisionAsCorrect(prevision.id) },
                            onMarkIncorrect = { viewModel.markPrevisionAsIncorrect(prevision.id) },
                            dateFormatter = dateFormatter
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // FAB para adicionar nova previsão
        FloatingActionButton(
            onClick = { viewModel.showAddDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar Previsão")
        }
    }

    // Dialog para adicionar nova previsão
    if (uiState.showAddDialog) {
        AddPrevisionDialog(
            uiState = uiState,
            onDismiss = { viewModel.hideAddDialog() },
            onTitleChange = { viewModel.updateNewPrevisionTitle(it) },
            onDescriptionChange = { viewModel.updateNewPrevisionDescription(it) },
            onStartDateSelected = { viewModel.updateStartDate(it) },
            onEndDateSelected = { viewModel.updateEndDate(it) },
            onShowStartDatePicker = { viewModel.showStartDatePicker() },
            onShowEndDatePicker = { viewModel.showEndDatePicker() },
            onHideStartDatePicker = { viewModel.hideStartDatePicker() },
            onHideEndDatePicker = { viewModel.hideEndDatePicker() },
            onSave = { viewModel.addPrevision() },
            dateFormatter = dateFormatter
        )
    }

    // Mostrar erro se houver
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Aqui você pode mostrar um Snackbar ou Toast
            Log.e("HomeScreen", error)
            viewModel.clearError()
        }
    }
}

@Composable
private fun DinahPointsBadge(points: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dinah Points",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = points.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3BB2F9)
            )
        }
    }
}

@Composable
private fun FilterButton(
    text: String,
    isSelected: Boolean,
    colors: Pair<Color, Color>,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = if (isSelected) colors.first else Color.White.copy(alpha = 0.8f)
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PrevisionCard(
    prevision: Prevision,
    onMarkCorrect: () -> Unit,
    onMarkIncorrect: () -> Unit,
    dateFormatter: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = prevision.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (prevision.description.isNotEmpty()) {
                Text(
                    text = prevision.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Início: ${dateFormatter.format(prevision.getStartDateAsDate())}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Fim: ${dateFormatter.format(prevision.getEndDateAsDate())}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (!prevision.finished) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onMarkCorrect,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Acertei", color = Color.White)
                    }
                    Button(
                        onClick = onMarkIncorrect,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Errei", color = Color.White)
                    }
                }
            } else {
                val statusColor = if (prevision.predicted) Color(0xFF4CAF50) else Color(0xFFF44336)
                val statusText = if (prevision.predicted) "Acertou" else "Errou"
                val points = PointsCalculator.calculate(prevision)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+$points pontos",
                        color = Color(0xFF3BB2F9),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPrevisionDialog(
    uiState: com.example.dinahvision.uistate.HomeUiState,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onStartDateSelected: (Long) -> Unit,
    onEndDateSelected: (Long) -> Unit,
    onShowStartDatePicker: () -> Unit,
    onShowEndDatePicker: () -> Unit,
    onHideStartDatePicker: () -> Unit,
    onHideEndDatePicker: () -> Unit,
    onSave: () -> Unit,
    dateFormatter: SimpleDateFormat
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Previsão") },
        text = {
            Column {
                OutlinedTextField(
                    value = uiState.newPrevisionTitle,
                    onValueChange = onTitleChange,
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = uiState.newPrevisionDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onShowStartDatePicker) {
                        Text(
                            if (uiState.selectedStartDate != null) 
                                "Início: ${dateFormatter.format(Date(uiState.selectedStartDate))}"
                            else "Selecionar início"
                        )
                    }
                    
                    TextButton(onClick = onShowEndDatePicker) {
                        Text(
                            if (uiState.selectedEndDate != null) 
                                "Fim: ${dateFormatter.format(Date(uiState.selectedEndDate))}"
                            else "Selecionar fim"
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = uiState.newPrevisionTitle.isNotBlank() && 
                         uiState.selectedStartDate != null && 
                         uiState.selectedEndDate != null
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
    
    // Date pickers
    if (uiState.showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = onHideStartDatePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onStartDateSelected(it) }
                        onHideStartDatePicker()
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onHideStartDatePicker) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (uiState.showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = onHideEndDatePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onEndDateSelected(it) }
                        onHideEndDatePicker()
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onHideEndDatePicker) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
