package com.example.dinahvision.screens
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dinahvision.models.Prevision
import com.example.dinahvision.models.User
import com.example.dinahvision.models.User.Companion.currentUser
import com.example.dinahvision.repository.PrevisionDAO
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier) {
    // Estados principais
    var listPredictions by remember { mutableStateOf<List<Prevision>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var newPrevision by remember { mutableStateOf(Prevision()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedEndDate by remember { mutableStateOf<Date?>(null) }

    // Estados para tratamento de erros no modal
    var modalErrorMessage by remember { mutableStateOf<String?>(null) }
    var showModalError by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val scope = rememberCoroutineScope()

    // Função para carregar previsões
    fun loadPredictions() {
        scope.launch {
            try {
                listPredictions = PrevisionDAO().listPredictionsByUser()
                isLoading = false
            } catch (e: Exception) {
                // Tratar erro de carregamento se necessário
            }
        }
    }

    // Carrega as previsões inicialmente
    LaunchedEffect(Unit) {
        loadPredictions()
    }

    // Corrige o problema do fuso horário (timezone)
    fun adjustTimezone(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR_OF_DAY, 3) // Ajuste para o timezone do Brasil (UTC-3)
        return calendar.time
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Conteúdo principal da tela
        if (isLoading) {
            CircularProgressIndicator()
        } else if (listPredictions.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Nenhuma previsão encontrada", color = Color.Gray)
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Adicionar Previsão")
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Olá ${currentUser!!.username}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(listPredictions) { prevision ->
                        PredictionCard(prevision = prevision)
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
        }

        // Dialog para adicionar nova previsão
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    showModalError = false
                },
                title = { Text("Nova Previsão") },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Campos do formulário
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

                        // Botão para selecionar data (com ajuste de timezone)
                        ElevatedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = selectedEndDate?.let { dateFormatter.format(adjustTimezone(it)) }
                                    ?: "Selecione a data final"
                            )
                        }

                        // Exibe erro do modal (se houver)
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
                    TextButton(
                        onClick = {
                            showDialog = false
                            showModalError = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // DatePicker com ajuste de timezone
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedEndDate?.time ?: System.currentTimeMillis()
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                // Aplica o ajuste de timezone ao selecionar a data
                                val calendar = Calendar.getInstance().apply {
                                    timeInMillis = it
                                    add(Calendar.HOUR_OF_DAY, 3) // Ajuste para UTC-3
                                }
                                selectedEndDate = calendar.time
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePicker = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
@Composable
fun PredictionCard(prevision: Prevision) {
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = prevision.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = prevision.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Data: ${dateFormatter.format(prevision.getEndDateAsDate())}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { /* TODO */ }) {
                    Text("Acertei")
                }
                Button(onClick = { /* TODO */ }) {
                    Text("Errei")
                }
            }
        }
    }
}