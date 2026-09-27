package com.example.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "docs_z_notifications"
    const val CHANNEL_NAME = "Docs Z Operations"
    private const val NOTIFICATION_ID_PROGRESS = 1001
    private const val NOTIFICATION_ID_COMPLETE = 1002
    private const val NOTIFICATION_ID_OPEN = 1003

    private var cachedAppLogo: Bitmap? = null

    /**
     * Generates a custom high-res branded app logo icon for notifications
     */
    fun getAppLogoBitmap(context: Context): Bitmap {
        cachedAppLogo?.let { if (!it.isRecycled) return it }

        val size = 192
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Smooth anti-alias rendering
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Rounded Rect Background with Docs Z Teal Gradient
        val shader = LinearGradient(
            0f, 0f, size.toFloat(), size.toFloat(),
            Color.rgb(0, 137, 123), // CamScanner Teal
            Color.rgb(0, 77, 64),   // Deep Teal
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        canvas.drawRoundRect(rect, 40f, 40f, paint)
        paint.shader = null

        // 2. Inner Document Silhouette
        paint.color = Color.WHITE
        val docPath = Path().apply {
            moveTo(48f, 32f)
            lineTo(108f, 32f)
            lineTo(144f, 68f)
            lineTo(144f, 160f)
            lineTo(48f, 160f)
            close()
        }
        canvas.drawPath(docPath, paint)

        // 3. Document Folded Corner
        paint.color = Color.rgb(204, 251, 241) // Light teal accent
        val foldPath = Path().apply {
            moveTo(108f, 32f)
            lineTo(108f, 68f)
            lineTo(144f, 68f)
            close()
        }
        canvas.drawPath(foldPath, paint)

        // 4. Stylized "Z" Logo in Brand Teal
        paint.color = Color.rgb(0, 137, 123)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 14f
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND

        val zPath = Path().apply {
            moveTo(68f, 86f)
            lineTo(124f, 86f)
            lineTo(68f, 134f)
            lineTo(124f, 134f)
        }
        canvas.drawPath(zPath, paint)

        // 5. Orange accent dot for signature Docs Z flair
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(245, 158, 11)
        canvas.drawCircle(124f, 86f, 6f, paint)

        cachedAppLogo = bitmap
        return bitmap
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = "Docs Z notifications for document opening, exports, and conversions"
                setShowBadge(true)
                enableVibration(true)
                enableLights(true)
                lightColor = Color.rgb(0, 137, 123)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Sends an instant notification with the custom app logo when any PDF / document is opened
     */
    fun showPdfOpenedNotification(
        context: Context,
        title: String,
        pageCount: Int
    ) {
        try {
            createNotificationChannel(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID_OPEN,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val appLogo = getAppLogoBitmap(context)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(appLogo)
                .setContentTitle(title)
                .setContentText("Opened in Docs Z • $pageCount pages ready")
                .setSubText("Docs Z PDF")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_OPEN, builder.build())
        } catch (e: SecurityException) {
            // Handled gracefully if permission not yet granted
        } catch (e: Exception) {
            // Fallback
        }
    }

    fun showProgressNotification(
        context: Context,
        title: String,
        statusText: String,
        progress: Int, // 0 to 100
        indeterminate: Boolean = false
    ) {
        try {
            createNotificationChannel(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val appLogo = getAppLogoBitmap(context)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(appLogo)
                .setContentTitle(title)
                .setContentText(statusText)
                .setProgress(100, progress, indeterminate)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PROGRESS, builder.build())
        } catch (e: SecurityException) {
            // Handled gracefully if permission denied
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun showCompletionNotification(
        context: Context,
        title: String,
        message: String
    ) {
        try {
            createNotificationChannel(context)

            // Cancel the progress notification
            try {
                NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_PROGRESS)
            } catch (e: Exception) {}

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val appLogo = getAppLogoBitmap(context)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(appLogo)
                .setContentTitle(title)
                .setContentText(message)
                .setSubText("Docs Z")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_COMPLETE, builder.build())
        } catch (e: SecurityException) {
            // Handled gracefully if permission denied
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun cancelProgress(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_PROGRESS)
        } catch (e: Exception) {}
    }
}
