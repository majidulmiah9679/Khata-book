package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.KhataDateUtils
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedLight
import com.example.ui.theme.HalfOrange
import com.example.ui.theme.HalfOrangeLight
import com.example.ui.theme.KhataPrimary
import com.example.ui.theme.KhataTertiary
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenLight

@Composable
fun DateSelectorBar(
    currentDate: String,
    selectedYear: Int,
    selectedMonth: Int,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onDateSelect: (String) -> Unit,
    onMonthSelect: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMonthMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("date_selector_bar"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Month / Year Selector line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        onClick = { showMonthMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.testTag("month_selector_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Month picker",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${KhataDateUtils.getMonthName(selectedMonth)} $selectedYear",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showMonthMenu,
                        onDismissRequest = { showMonthMenu = false }
                    ) {
                        KhataDateUtils.monthNames.forEachIndexed { index, monthName ->
                            val monthNum = index + 1
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "$monthName $selectedYear",
                                        fontWeight = if (monthNum == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                                        color = if (monthNum == selectedMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onMonthSelect(selectedYear, monthNum)
                                    showMonthMenu = false
                                }
                            )
                        }
                    }
                }

                // Quick Today Chip
                val todayIso = KhataDateUtils.getTodayIso()
                val isToday = currentDate == todayIso
                Surface(
                    onClick = { onDateSelect(todayIso) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("today_chip_button")
                ) {
                    Text(
                        text = if (isToday) "• Today" else "Go to Today",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onPreviousDay,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .testTag("previous_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Day",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = KhataDateUtils.formatDisplayDate(currentDate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = KhataDateUtils.formatFullDate(currentDate).substringBefore(","),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onNextDay,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .testTag("next_day_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Day",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String?,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when (status) {
        "Present", "1.0" -> Triple(PresentGreenLight, PresentGreen, "১.০ হাজিরা (Full)")
        "OneAndHalf", "1.5" -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), "১.৫ হাজিরা (1.5)")
        "Double", "2.0" -> Triple(Color(0xFFEDE9FE), Color(0xFF7C3AED), "২.০ হাজিরা (Double)")
        "Half", "0.5" -> Triple(HalfOrangeLight, HalfOrange, "০.৫ হাজিরা (Half)")
        "Absent" -> Triple(AbsentRedLight, AbsentRed, "অনুপস্থিত (Absent)")
        else -> Triple(Color(0xFFF3F4F6), Color(0xFF6B7280), "বাকি আছে (Unmarked)")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AttendanceToggleGroup(
    currentStatus: String?,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1.0 Present button
        val isPresent = currentStatus == "Present" || currentStatus == "1.0"
        Surface(
            onClick = { onStatusChange("Present") },
            shape = RoundedCornerShape(8.dp),
            color = if (isPresent) PresentGreen else Color.Transparent,
            border = if (isPresent) null else androidx.compose.foundation.BorderStroke(1.dp, PresentGreen),
            modifier = Modifier
                .height(36.dp)
                .weight(1.1f)
                .testTag("status_present_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Text(
                    text = "১.০ পুরো",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPresent) Color.White else PresentGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 1.5 One and Half button
        val isOneAndHalf = currentStatus == "OneAndHalf" || currentStatus == "1.5"
        Surface(
            onClick = { onStatusChange("1.5") },
            shape = RoundedCornerShape(8.dp),
            color = if (isOneAndHalf) Color(0xFF0284C7) else Color.Transparent,
            border = if (isOneAndHalf) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
            modifier = Modifier
                .height(36.dp)
                .weight(1.1f)
                .testTag("status_one_and_half_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Text(
                    text = "১.৫ দেড়",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOneAndHalf) Color.White else Color(0xFF0284C7),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 0.5 Half button
        val isHalf = currentStatus == "Half" || currentStatus == "0.5"
        Surface(
            onClick = { onStatusChange("Half") },
            shape = RoundedCornerShape(8.dp),
            color = if (isHalf) HalfOrange else Color.Transparent,
            border = if (isHalf) null else androidx.compose.foundation.BorderStroke(1.dp, HalfOrange),
            modifier = Modifier
                .height(36.dp)
                .weight(1f)
                .testTag("status_half_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Text(
                    text = "০.৫ হাফ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isHalf) Color.White else HalfOrange,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Absent button
        val isAbsent = currentStatus == "Absent"
        Surface(
            onClick = { onStatusChange("Absent") },
            shape = RoundedCornerShape(8.dp),
            color = if (isAbsent) AbsentRed else Color.Transparent,
            border = if (isAbsent) null else androidx.compose.foundation.BorderStroke(1.dp, AbsentRed),
            modifier = Modifier
                .height(36.dp)
                .weight(0.9f)
                .testTag("status_absent_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Text(
                    text = "০ ছুটি",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAbsent) Color.White else AbsentRed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SummaryStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    cardColor: Color = MaterialTheme.colorScheme.surface,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
