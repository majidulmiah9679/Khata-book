package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.entity.Worker
import com.example.ui.model.CurrencyFormatter
import com.example.ui.model.KhataDateUtils
import com.example.ui.model.WorkerMonthlySummary
import com.example.ui.theme.DueAmber
import com.example.ui.theme.KhataTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDialog(
    initialWorkerId: Int? = null,
    workers: List<Worker>,
    workerSummaries: List<WorkerMonthlySummary>,
    currentDate: String,
    onDismiss: () -> Unit,
    onSavePayment: (workerId: Int, date: String, amount: Double, note: String) -> Unit
) {
    var selectedWorker by remember {
        mutableStateOf(
            if (initialWorkerId != null) {
                workers.find { it.id == initialWorkerId } ?: workers.firstOrNull()
            } else {
                workers.firstOrNull()
            }
        )
    }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf(currentDate) }
    var workerDropdownExpanded by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val currentWorkerSummary = selectedWorker?.let { w ->
        workerSummaries.find { it.worker.id == w.id }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = KhataTertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Add Advance / Payment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Record cash advance or wage payment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Worker selector
                ExposedDropdownMenuBox(
                    expanded = workerDropdownExpanded,
                    onExpandedChange = { workerDropdownExpanded = !workerDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedWorker?.name ?: "Select Worker",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Worker *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workerDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .testTag("payment_worker_selector")
                    )

                    ExposedDropdownMenu(
                        expanded = workerDropdownExpanded,
                        onDismissRequest = { workerDropdownExpanded = false }
                    ) {
                        workers.forEach { worker ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(worker.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Daily Rate: ${CurrencyFormatter.formatTaka(worker.dailyWage)}/day",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedWorker = worker
                                    workerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Balance Info Box if worker selected
                if (currentWorkerSummary != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Earned This Month",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatTaka(currentWorkerSummary.totalEarned),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Balance Due",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatTaka(currentWorkerSummary.balanceDue),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentWorkerSummary.balanceDue >= 0) DueAmber else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = false
                    },
                    label = { Text("Payment Amount *") },
                    placeholder = { Text("e.g. 1000") },
                    leadingIcon = { Icon(Icons.Default.Money, contentDescription = null, tint = KhataTertiary) },
                    isError = amountError,
                    supportingText = {
                        if (amountError) Text("Enter valid payment amount", color = MaterialTheme.colorScheme.error)
                        else Text("Cash advance or payment given to worker")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input")
                )

                // Date Field
                OutlinedTextField(
                    value = KhataDateUtils.formatDisplayDate(paymentDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Date") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Note Field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description (Optional)") },
                    placeholder = { Text("e.g. Weekly expense / Urgent advance") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val worker = selectedWorker
                    val amount = amountText.toDoubleOrNull()
                    if (worker == null) return@Button
                    if (amount == null || amount <= 0) {
                        amountError = true
                        return@Button
                    }
                    onSavePayment(
                        worker.id,
                        paymentDate,
                        amount,
                        if (note.isBlank()) "Cash Advance" else note.trim()
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = KhataTertiary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Save Payment")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_payment_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(18.dp)
    )
}
