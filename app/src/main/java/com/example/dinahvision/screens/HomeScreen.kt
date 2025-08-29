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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavHostController
import com.example.dinahvision.R
import com.example.dinahvision.models.PointsCalculator
import com.example.dinahvision.models.Prevision
import com.example.dinahvision.models.User
import com.example.dinahvision.models.User.Companion.currentUser
import com.example.dinahvision.repository.PrevisionDAO
import com.example.dinahvision.repository.UserDAO
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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
fun HomeScreen(modifier: Modifier, navController: NavHostController) {
    var listPredictions by remember { mutableStateOf<List<Prevision>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var newPrevision by remember { mutableStateOf(Prevision()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedEndDate by remember { mutableStateOf<Date?>(null) }
    var userPoints by remember { mutableStateOf(User.currentUser?.points ?: 0) }

    var modalErrorMessage by remember { mutableStateOf<String?>(null) }
    var showModalError by remember { mutableStateOf(false) }

    var currentFilter by remember { mutableStateOf(PrevisionFilter.CURRENT) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val scope = rememberCoroutineScope()

    fun loadPredictions() {
        scope.launch {
            try {
                listPredictions = PrevisionDAO().listPredictionsByUser()
                User.currentUser?.let { user ->
                    val updatedUser = UserDAO().getUser(user.uid)
                    updatedUser?.let {
                        userPoints = it.points
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeScreen", "Erro ao carregar previsões", e)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPredictions()
    }

    val filteredList = listPredictions.filter { p ->
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
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Column(modifier = Modifier.fillMaxSize()
                //.padding(top = 140.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logodinah),
                    contentDescription = "Logo DinahVision",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        //.padding(top = 10.dp, bottom = 12.dp)
                )
//                Text(
//                    text = "Olá ${currentUser?.username ?: "Usuário"}",
//                    modifier = Modifier.padding(16.dp),
//                    style = MaterialTheme.typography.titleLarge
//                )

                DinahPointsBadge(points = userPoints)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        Triple(PrevisionFilter.WRONG,   "Erradas",   R.drawable.erradashome),
                        Triple(PrevisionFilter.CURRENT, "Atuais",    R.drawable.atuaishome),
                        Triple(PrevisionFilter.CORRECT, "Acertadas", R.drawable.acertadashome)
                    ).forEach { (filterType, text, icon) ->
                        val isActive = currentFilter == filterType
                        val (startColor, _) = getFilterColors(filterType)

                        Button(
                            onClick = { currentFilter = filterType },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) startColor else Color.LightGray.copy(alpha = 0.3f),
                                contentColor   = if (isActive) Color.White else Color.Gray
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = if (isActive) 4.dp else 0.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
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
                                    softWrap = false,
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
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                    ) {
                        items(filteredList) { prevision ->
                            PredictionCard(
                                prevision = prevision,
                                filter = currentFilter,
                                onPredictionUpdated = {
                                    loadPredictions()
                                }
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    showDialog = true
                    modalErrorMessage = null
                    showModalError = false
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar previsão")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        showModalError = false
                    },
                    title = { Text("Nova Previsão") },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = newPrevision.title,
                                onValueChange = { newPrevision = newPrevision.copy(title = it) },
                                label = { Text("Título") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = newPrevision.description,
                                onValueChange = { newPrevision = newPrevision.copy(description = it) },
                                label = { Text("Descrição") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                            ElevatedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text(
                                    text = selectedEndDate?.let {
                                        dateFormatter.format(it)
                                    } ?: "Selecione a data final"
                                )
                            }
                            if (showModalError && !modalErrorMessage.isNullOrEmpty()) {
                                Text(
                                    text = modalErrorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (selectedEndDate == null) {
                                    modalErrorMessage = "Selecione uma data final"
                                    showModalError = true
                                    return@Button
                                }
                                scope.launch {
                                    try {
                                        val finalPrevision = newPrevision.copy(
                                            startDate = Timestamp.now(),
                                            endDate = Timestamp(selectedEndDate!!),
                                            predicted = false,
                                            userId = currentUser!!.uid
                                        )
                                        PrevisionDAO().savePrevision(finalPrevision)
                                        loadPredictions()
                                        showDialog = false
                                        newPrevision = Prevision()
                                        selectedEndDate = null
                                        showModalError = false
                                    } catch (e: Exception) {
                                        modalErrorMessage = "Erro ao salvar: ${e.localizedMessage}"
                                        showModalError = true
                                        Log.e("HomeScreen", "Erro ao salvar previsão", e)
                                    }
                                }
                            }
                        ) {
                            Text("Salvar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            showModalError = false
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedEndDate?.time ?: System.currentTimeMillis()
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        Button(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val calendar = Calendar.getInstance().apply {
                                    timeInMillis = it
                                    add(Calendar.HOUR_OF_DAY, 3)
                                }
                                selectedEndDate = calendar.time
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                    }
                ) {
                    DatePicker(state = datePickerState)
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