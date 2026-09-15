package com.mycar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mycar.app.data.util.PersianDateHelper
import com.mycar.app.data.util.PriceFormatter

/**
 * A dedicated Persian / Jalali Date Picker Dialog in Jetpack Compose.
 *
 * Supports:
 * - Month navigation (previous/next month)
 * - Year selector (quick jump across years)
 * - Day selection (with visual feedback for selected, today, and Friday weekend)
 * - Persian RTL layout
 * - Confirm ("تأیید") and Cancel ("انصراف")
 * - Quick "امروز" (Today) jump
 */
@Composable
fun PersianDatePickerDialog(
    initialTimestamp: Long,
    onDismiss: () -> Unit,
    onConfirm: (selectedTimestamp: Long) -> Unit
) {
    val todayJalali = remember { PersianDateHelper.timestampToJalali(System.currentTimeMillis()) }
    val initialJalali = remember(initialTimestamp) { PersianDateHelper.timestampToJalali(initialTimestamp) }

    var selectedYear by remember { mutableIntStateOf(initialJalali.year) }
    var selectedMonth by remember { mutableIntStateOf(initialJalali.month) }
    var selectedDay by remember { mutableIntStateOf(initialJalali.day) }

    var viewYear by remember { mutableIntStateOf(initialJalali.year) }
    var viewMonth by remember { mutableIntStateOf(initialJalali.month) }
    var isYearPickerVisible by remember { mutableStateOf(false) }

    val daysInViewMonth = remember(viewYear, viewMonth) {
        PersianDateHelper.getDaysInMonth(viewYear, viewMonth)
    }
    val firstDayOfWeek = remember(viewYear, viewMonth) {
        PersianDateHelper.getFirstDayOfWeek(viewYear, viewMonth)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: Title & Selected Date Preview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "انتخاب تاریخ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Quick "Today" Chip
                        AssistChip(
                            onClick = {
                                viewYear = todayJalali.year
                                viewMonth = todayJalali.month
                                selectedYear = todayJalali.year
                                selectedMonth = todayJalali.month
                                selectedDay = todayJalali.day
                                isYearPickerVisible = false
                            },
                            label = {
                                Text(
                                    text = "امروز",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Today,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            border = null
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Selected Date Display Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val selectedTimestamp = remember(selectedYear, selectedMonth, selectedDay) {
                                PersianDateHelper.jalaliToTimestamp(selectedYear, selectedMonth, selectedDay)
                            }
                            Text(
                                text = PersianDateHelper.formatJalaliFull(selectedTimestamp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = String.format("%04d/%02d/%02d", selectedYear, selectedMonth, selectedDay),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Month & Year Navigation Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Month button (In RTL: arrow forward points right, moving to next month; arrow back points left, moving to prev month)
                        IconButton(
                            onClick = {
                                if (viewMonth > 1) {
                                    viewMonth--
                                } else {
                                    viewMonth = 12
                                    viewYear--
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "ماه قبل",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Month & Year Display (Clickable to toggle Year Picker)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isYearPickerVisible) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isYearPickerVisible = !isYearPickerVisible }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val monthName = PersianDateHelper.PERSIAN_MONTH_NAMES.getOrElse(viewMonth - 1) { "" }
                                Text(
                                    text = "$monthName $viewYear",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isYearPickerVisible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = if (isYearPickerVisible) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = "انتخاب سال",
                                    tint = if (isYearPickerVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Next Month button
                        IconButton(
                            onClick = {
                                if (viewMonth < 12) {
                                    viewMonth++
                                } else {
                                    viewMonth = 1
                                    viewYear++
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ماه بعد",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isYearPickerVisible) {
                        // Year Selection Grid
                        val years = remember { (1380..1425).toList() }
                        val gridState = rememberLazyGridState()

                        LaunchedEffect(Unit) {
                            val targetIndex = years.indexOf(viewYear).coerceAtLeast(0)
                            gridState.scrollToItem((targetIndex - 6).coerceAtLeast(0))
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        ) {
                            Text(
                                text = "انتخاب سال:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                state = gridState,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(years) { y ->
                                    val isSelected = y == viewYear
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable {
                                                viewYear = y
                                                // Adjust selected day if month now has fewer days
                                                val maxDays = PersianDateHelper.getDaysInMonth(y, viewMonth)
                                                if (selectedDay > maxDays) selectedDay = maxDays
                                                isYearPickerVisible = false
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = y.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Days of Week Header Row (ش، ی، د، س، چ، پ، ج)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            PersianDateHelper.WEEK_DAYS_SHORT.forEachIndexed { index, dayName ->
                                val isFriday = index == 6
                                Text(
                                    text = dayName,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFriday) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Calendar Days Grid (7 columns)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val totalSlots = firstDayOfWeek + daysInViewMonth
                            val totalRows = (totalSlots + 6) / 7

                            for (row in 0 until totalRows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    for (col in 0 until 7) {
                                        val slotIndex = row * 7 + col
                                        val dayNumber = slotIndex - firstDayOfWeek + 1

                                        if (dayNumber in 1..daysInViewMonth) {
                                            val isSelected = viewYear == selectedYear && viewMonth == selectedMonth && dayNumber == selectedDay
                                            val isToday = viewYear == todayJalali.year && viewMonth == todayJalali.month && dayNumber == todayJalali.day
                                            val isFriday = col == 6

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isSelected -> MaterialTheme.colorScheme.primary
                                                            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .then(
                                                        if (isToday && !isSelected) {
                                                            Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                        } else Modifier
                                                    )
                                                    .clickable {
                                                        selectedYear = viewYear
                                                        selectedMonth = viewMonth
                                                        selectedDay = dayNumber
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = dayNumber.toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                                        isFriday -> MaterialTheme.colorScheme.error
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            }
                                        } else {
                                            // Empty cell spacer
                                            Spacer(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Action Buttons ("انصراف" و "تأیید")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "انصراف",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val finalTimestamp = PersianDateHelper.jalaliToTimestamp(
                                    selectedYear,
                                    selectedMonth,
                                    selectedDay
                                )
                                onConfirm(finalTimestamp)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تأیید",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
