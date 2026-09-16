package com.mycar.app.data.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mycar.app.MainActivity
import com.mycar.app.R
import com.mycar.app.data.model.ReminderIntervalType
import com.mycar.app.data.repository.ReminderItem
import com.mycar.app.data.repository.ReminderStatus
import java.text.NumberFormat
import java.util.Locale

object ReminderNotificationHelper {

    const val CHANNEL_ID = "car_service_reminders"
    const val CHANNEL_NAME = "یادآوری سرویس‌های خودرو"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "هشدار و اعلان سررسید سرویس‌ها، بیمه‌نامه و معاینه فنی"
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Builds a human-readable title for a reminder notification.
     */
    fun getReminderNotificationTitle(item: ReminderItem): String {
        return when (item.status) {
            ReminderStatus.OVERDUE -> "هشدار موعد سرویس: ${item.partName}"
            ReminderStatus.APPROACHING -> "نزدیک موعد سرویس: ${item.partName}"
            ReminderStatus.HEALTHY -> "وضعیت مطلوب: ${item.partName}"
        }
    }

    /**
     * Builds a human-readable Persian description for a reminder, strictly respecting interval type.
     * Guaranteed NOT to show irrelevant mileage for time-only services.
     */
    fun getReminderNotificationBody(item: ReminderItem): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        return when (item.intervalType) {
            ReminderIntervalType.TIME -> {
                val formattedDate = PersianDateHelper.formatJalali(item.dueDateTimestamp)
                when {
                    item.daysRemaining < 0 ->
                        "موعد ${item.partName} گذشته است (${numberFormat.format(Math.abs(item.daysRemaining))} روز قبل در تاریخ $formattedDate)."
                    item.daysRemaining == 0 ->
                        "امروز موعد تمدید / انجام ${item.partName} است (تاریخ: $formattedDate)."
                    else ->
                        "${numberFormat.format(item.daysRemaining)} روز تا موعد ${item.partName} باقی مانده است (سررسید: $formattedDate)."
                }
            }

            ReminderIntervalType.MILEAGE -> {
                when {
                    item.kmRemaining <= 0 ->
                        "کارکرد سرویس ${item.partName} سپری شده است (${numberFormat.format(Math.abs(item.kmRemaining))} کیلومتر گذشته، در کیلومتر ${numberFormat.format(item.dueMileage)})."
                    else ->
                        "${numberFormat.format(item.kmRemaining)} کیلومتر تا موعد سرویس ${item.partName} باقی مانده است (در کیلومتر ${numberFormat.format(item.dueMileage)})."
                }
            }

            ReminderIntervalType.COMBINED -> {
                val formattedDate = PersianDateHelper.formatJalali(item.dueDateTimestamp)
                when {
                    item.kmRemaining <= 0 && item.daysRemaining < 0 ->
                        "سرویس ${item.partName} از نظر کارکرد و تاریخ هر دو منقضی شده است (${numberFormat.format(Math.abs(item.kmRemaining))} کیلومتر و ${numberFormat.format(Math.abs(item.daysRemaining))} روز گذشته)."
                    item.kmRemaining <= 0 ->
                        "سرویس ${item.partName} بر اساس کارکرد منقضی شده است (${numberFormat.format(Math.abs(item.kmRemaining))} کیلومتر گذشته)."
                    item.daysRemaining < 0 ->
                        "سرویس ${item.partName} بر اساس تاریخ منقضی شده است (${numberFormat.format(Math.abs(item.daysRemaining))} روز گذشته، موعد: $formattedDate)."
                    else -> {
                        "${numberFormat.format(item.kmRemaining)} کیلومتر یا ${numberFormat.format(item.daysRemaining)} روز تا موعد سرویس ${item.partName} (سررسید: $formattedDate)."
                    }
                }
            }

            ReminderIntervalType.NONE -> ""
        }
    }

    fun showReminderNotification(context: Context, item: ReminderItem, notificationId: Int) {
        if (item.status == ReminderStatus.HEALTHY || item.intervalType == ReminderIntervalType.NONE) {
            return
        }

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getReminderNotificationTitle(item))
            .setContentText(getReminderNotificationBody(item))
            .setStyle(NotificationCompat.BigTextStyle().bigText(getReminderNotificationBody(item)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Notification permission might not be granted by user yet
        }
    }
}
