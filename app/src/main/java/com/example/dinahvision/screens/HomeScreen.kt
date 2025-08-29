package com.example.dinahvision.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dinahvision.R
import com.example.dinahvision.models.PointsCalculator
import com.example.dinahvision.models.Prevision
import com.example.dinahvision.models.User
import com.example.dinahvision.models.PrevisionFilter
import com.example.dinahvision.repository.PrevisionDAO
import com.example.dinahvision.repository.UserDAO
import com.example.dinahvision.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private fun getFilterColors(filter: PrevisionFilter): Pair<Color, Color> {
    return when (filter) {
        PrevisionFilter.WRONG -> Pair(Color(0xFFED7E75), Color(0xFF623AA2))
        PrevisionFilter.CORRECT -> Pair(Color(0xFF83E89E), Color(0xFF3FCAD9))
        PrevisionFilter.CURRENT -> Pair(Color(0xFFE0D9F5), Color(0xFFE0D9F5))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val scope = rememberCoroutineScope()

    val filteredList = state.predictions.filter { p: Prevision ->
        when (state.currentFilter) {
            PrevisionFilter.CURRENT -> !p.finished
            PrevisionFilter.WRONG -> p.finished && !p.predicted
            PrevisionFilter.CORRECT -> p.finished && p.predicted
        }
    }
//
//    LaunchedEffect(Unit) {
//        viewModel.loadPredictions()
//    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.home),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        if (state.isLoading) {
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

                DinahPointsBadge(points = state.userPoints)

                // Filtros
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        Triple(PrevisionFilter.WRONG, "Erradas", R.drawable.erradashome),
                        Triple(PrevisionFilter.CURRENT, "Atuais", R.drawable.atuaishome),
                        Triple(PrevisionFilter.CORRECT, "Acertadas", R.drawable.acertadashome)
                    ).forEach { (filterType, text, icon) ->
                        val isActive = state.currentFilter == filterType
                        val (startColor, _) = getFilterColors(filterType)

                        Button(
                            onClick = { viewModel.setFilter(filterType) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) startColor else Color.LightGray.copy(alpha = 0.3f),
                                contentColor = if (isActive) Color.White else Color.Gray
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = if (isActive) 4.dp else 0.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = icon),
                                    contentDescription = text,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                if (filteredList.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("Nenhuma previsão para este filtro", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(filteredList) { prevision ->
                            PredictionCard(
                                prevision = prevision,
                                filter = state.currentFilter,
                                onPredictionUpdated = { viewModel.loadPredictions() }
                            )
                        }
                    }
                }

                // Floating Action Button
                FloatingActionButton(
                    onClick = { viewModel.showDialog(true) },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar previsão")
                }

                // Dialog
                if (state.showDialog) {
                    AlertDialog(
                        onDismissRequest = { viewModel.showDialog(false) },
                        title = { Text("Nova Previsão") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = state.newPrevision.title,
                                    onValueChange = { viewModel.updateNewPrevision(state.newPrevision.copy(title = it)) },
                                    label = { Text("Título") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = state.newPrevision.description,
                                    onValueChange = { viewModel.updateNewPrevision(state.newPrevision.copy(description = it)) },
                                    label = { Text("Descrição") },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )
                                ElevatedButton(
                                    onClick = { viewModel.showDatePicker(true) },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = state.selectedEndDate?.let { dateFormatter.format(Date(it)) } ?: "Selecione a data final"
                                    )
                                }
                                if (state.showModalError && !state.modalErrorMessage.isNullOrEmpty()) {
                                    Text(
                                        text = state.modalErrorMessage!!,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (state.selectedEndDate == null) {
                                    viewModel.showError("Selecione uma data final")
                                    return@Button
                                }

                                scope.launch {
                                    try {
                                        val finalPrevision = state.newPrevision.copy(
                                            startDate = com.google.firebase.Timestamp.now(),
                                            endDate = com.google.firebase.Timestamp(Date(state.selectedEndDate!!)),
                                            predicted = false,
                                            userId = User.currentUser!!.uid
                                        )
                                        viewModel.loadPredictions()
                                        viewModel.showDialog(false)
                                    } catch (e: Exception) {
                                        viewModel.showError("Erro ao salvar: ${e.localizedMessage}")
                                        Log.e("HomeScreen", "Erro ao salvar previsão", e)
                                    }
                                }
                            }) {
                                Text("Salvar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { viewModel.showDialog(false) }) { Text("Cancelar") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PredictionCard(
    prevision: Prevision,
    filter: PrevisionFilter,
    onPredictionUpdated: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val gradient = when (filter) {
        PrevisionFilter.WRONG   -> Brush.linearGradient(
            listOf(Color(0xFFED7E75), Color(0xFF623AA2))
        )
        PrevisionFilter.CORRECT -> Brush.linearGradient(
            listOf(Color(0xFF83E89E), Color(0xFF3FCAD9))
        )
        PrevisionFilter.CURRENT -> Brush.linearGradient(
            listOf(Color(0xFFE0D9F5), Color(0xFFE0D9F5))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(gradient)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = prevision.title,
                style = MaterialTheme.typography.titleLarge,
                color = if (filter == PrevisionFilter.CORRECT) Color.Black else Color.White,
                modifier = Modifier.padding(bottom = 8.dp),

                )
            Text(
                text = prevision.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (filter == PrevisionFilter.CORRECT) Color.Black else Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Data: ${dateFormatter.format(prevision.getEndDateAsDate())}",
                style = MaterialTheme.typography.labelMedium,
                color = if (filter == PrevisionFilter.CORRECT) Color.Black.copy(alpha = 0.7f)
                else Color.White.copy(alpha = 0.7f)
            )

            if (!prevision.finished) {
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val success = PrevisionDAO().markPredictionAsCorrect(prevision.id)
                                    if (success) {
                                        prevision.finished = true
                                        prevision.predicted = true
                                        val points = PointsCalculator.calculate(prevision)
                                        User.currentUser!!.points += points
                                        UserDAO().updateUser(User.currentUser!!)
                                        onPredictionUpdated()
                                    } else {
                                        errorMessage = "Falha ao atualizar"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Erro: ${e.localizedMessage}"
                                } finally {
                                    isLoading = false
                                }
                            }

                        },
                        enabled = !isLoading && !prevision.finished
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Acertei")
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    val success = PrevisionDAO().markPredictionAsWrong(prevision.id)
                                    if (success) {
                                        val points = PointsCalculator.calculate(prevision)
                                        User.currentUser!!.points += points
                                        UserDAO().updateUser(User.currentUser!!)
                                        onPredictionUpdated()
                                    } else {
                                        errorMessage = "Falha ao atualizar"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Erro: ${e.localizedMessage}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading && !prevision.finished
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Errei")
                        }
                    }
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun DinahPointsBadge(points: Int) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF83E89E).copy(alpha = 0.7f),
            Color(0xFF3FCAD9).copy(alpha = 0.7f)
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(gradient)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Text(
                text = "Dinah Points: $points ★",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 18.sp
                ),
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}