package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.entity.Worker

@Composable
fun AddWorkerDialog(
    workerToEdit: Worker? = null,
    onDismiss: () -> Unit,
    onSaveWorker: (name: String, phone: String, dailyWage: Double, initialAdvance: Double) -> Unit,
    onUpdateWorker: ((Worker) -> Unit)? = null
) {
    var name by remember { mutableStateOf(workerToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(workerToEdit?.phone ?: "") }
    var dailyRateText by remember {
        mutableStateOf(
            if (workerToEdit != null) {
                if (workerToEdit.dailyWage % 1.0 == 0.0) workerToEdit.dailyWage.toInt().toString()
                else workerToEdit.dailyWage.toString()
            } else "500"
        )
    }
    var initialAdvanceText by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var rateError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (workerToEdit == null) "Add New Worker" else "Edit Worker",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Worker Name *") },
                    placeholder = { Text("e.g. Rafiqul Islam") },
                    leadingIcon = {
                        Icon(Icons.Default.Badge, contentDescription = null)
                    },
                    isError = nameError,
                    supportingText = {
                        if (nameError) Text("Name is required", color = MaterialTheme.colorScheme.error)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("worker_name_input")
                )

                // Daily Rate Field
                OutlinedTextField(
                    value = dailyRateText,
                    onValueChange = {
                        dailyRateText = it
                        rateError = false
                    },
                    label = { Text("Daily Wage Rate *") },
                    placeholder = { Text("e.g. 500") },
                    leadingIcon = {
                        Icon(Icons.Default.Money, contentDescription = null)
                    },
                    isError = rateError,
                    supportingText = {
                        if (rateError) Text("Enter a valid amount", color = MaterialTheme.colorScheme.error)
                        else Text("Wage credited per full-day attendance")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("worker_rate_input")
                )

                // Phone Field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (Optional)") },
                    placeholder = { Text("e.g. 01712-XXXXXX") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("worker_phone_input")
                )

                // Initial Advance (Only on new worker)
                if (workerToEdit == null) {
                    OutlinedTextField(
                        value = initialAdvanceText,
                        onValueChange = { initialAdvanceText = it },
                        label = { Text("Initial Cash Advance (Optional)") },
                        placeholder = { Text("0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("worker_initial_advance_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val rate = dailyRateText.toDoubleOrNull()
                    if (rate == null || rate < 0) {
                        rateError = true
                        return@Button
                    }
                    val advance = initialAdvanceText.toDoubleOrNull() ?: 0.0

                    if (workerToEdit != null) {
                        onUpdateWorker?.invoke(
                            workerToEdit.copy(
                                name = name.trim(),
                                phone = phone.trim(),
                                dailyWage = rate
                            )
                        )
                    } else {
                        onSaveWorker(name.trim(), phone.trim(), rate, advance)
                    }
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_worker_button")
            ) {
                Text(if (workerToEdit == null) "Save Worker" else "Update Worker")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_worker_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(18.dp)
    )
}
