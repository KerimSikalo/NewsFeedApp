package etf.ri.rma.newsfeedapp.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    initialCategories: Set<String>,
    onApplyFilters: (Set<String>, String?, List<String>) -> Unit,
    navController: NavController
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    var selectedCategories by rememberSaveable { mutableStateOf(initialCategories) }
    var unwantedInput by remember { mutableStateOf("") }
    var unwantedWords by remember { mutableStateOf(mutableListOf<String>()) }
    val focusManager = LocalFocusManager.current
    var fromDate by remember { mutableStateOf(LocalDate.now().minusDays(10)) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState()
    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    try {
                        navController.previousBackStackEntry?.savedStateHandle
                            ?.set("filters", Triple(selectedCategories, "${fromDate.format(dateFormatter)};${toDate.format(dateFormatter)}", unwantedWords.toList()))
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Log.e("FilterScreen", "Greška pri filtriranju: ${e.message}")
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("filter_apply_button")
            ) {
                Text("Primijeni filtere")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)
        ) {
            item {
                Text("Odaberite kategorije:")
                Spacer(modifier = Modifier.height(8.dp))
                CategoryFilter(
                    selectedCategories = selectedCategories,
                    onSelectionChanged = { selectedCategories = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("Period:", modifier = Modifier.testTag("filter_daterange_display"))
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { showDatePicker = !showDatePicker },
                    modifier = Modifier.testTag("filter_daterange_button")
                ) {
                    Text("${fromDate.format(dateFormatter)};${toDate.format(dateFormatter)}")
                }
                if (showDatePicker) {
                    AlertDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val start = datePickerState.selectedStartDateMillis
                                val end = datePickerState.selectedEndDateMillis
                                if (start != null && end != null) {
                                    fromDate = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate()
                                    toDate = Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalDate()
                                    showDatePicker = false
                                }
                            }) {
                                Text("Potvrdi")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Otkaži")
                            }
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Odaberite period",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                DateRangePicker(
                                    state = datePickerState,
                                    modifier = Modifier.fillMaxWidth().height(500.dp)
                                )
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Neželjene riječi:")
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    OutlinedTextField(
                        value = unwantedInput,
                        onValueChange = { unwantedInput = it },
                        placeholder = { Text("Unesi riječ") },
                        modifier = Modifier.weight(1f).testTag("filter_unwanted_input"),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                        })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val word = unwantedInput.trim()
                            if (word.isNotEmpty() && !unwantedWords.contains(word)) {
                                unwantedWords.add(word)
                                unwantedInput = ""
                            }
                        },
                        modifier = Modifier.testTag("filter_unwanted_add_button")
                    ) {
                        Text("Dodaj")
                    }
                }
                Column(modifier = Modifier.testTag("filter_unwanted_list")) {
                    unwantedWords.forEach {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(it, modifier = Modifier.testTag("unwanted_word_$it"))
                            IconButton(onClick = {
                                unwantedWords.remove(it)
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Ukloni riječ")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}









