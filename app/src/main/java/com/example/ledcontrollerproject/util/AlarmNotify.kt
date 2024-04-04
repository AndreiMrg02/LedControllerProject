package com.example.ledcontrollerproject.util
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.ledcontrollerproject.MainActivity
import com.example.ledcontrollerproject.R
import com.example.ledcontrollerproject.ui.bluetooth.BluetoothFragment
import com.example.ledcontrollerproject.ui.schedule.data.ScheduleItem
import com.example.ledcontrollerproject.ui.schedule.data.ScheduleRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
class AlarmNotify {
    // Funcție pentru crearea și inițializarea canalului de notificări
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarme"
            val descriptionText = "Canal pentru notificări cu privire la alarme"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("alarm_channel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Funcție pentru programarea task-ului de verificare a alarmelor
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(DelicateCoroutinesApi::class)
    fun scheduleAlarmCheck(context: Context, scheduleRepository: ScheduleRepository) {
        createNotificationChannel(context)

        GlobalScope.launch(Dispatchers.Default) {
            while (true) {
                val currentTime = Calendar.getInstance(TimeZone.getDefault())
                val currentDay = currentTime.get(Calendar.DAY_OF_WEEK)

                // Obține toate alarmele din repository
                val menuItems = scheduleRepository.getMenuItemData()

                // Verifică fiecare alarmă pentru ziua curentă și ora corespunzătoare
                for (scheduleItem in menuItems) {
                    val daysSelected = scheduleItem.daysSelected
                    val timeParts = scheduleItem.time.split(":")
                    val alarmHour = timeParts[0].toInt()
                    val alarmMinute = timeParts[1].toInt()

                    if (daysSelected.contains(getDayAbbreviation(currentDay))) {
                        Log.d("Alarm","Am gasit o alarma in acea zi")
                        Log.d("Alarm",currentTime.get(Calendar.HOUR_OF_DAY).toString() + " " + alarmHour.toString() +
                        " " + currentTime.get(Calendar.MINUTE) + " " + alarmMinute)
                        if (currentTime.get(Calendar.HOUR_OF_DAY) == alarmHour &&
                            currentTime.get(Calendar.MINUTE) == alarmMinute - 1
                        ) {
                            showNotification(context, scheduleItem)
                        }
                        else if (currentTime.get(Calendar.HOUR_OF_DAY) == alarmHour &&
                            currentTime.get(Calendar.MINUTE) == alarmMinute) {
                            if (scheduleItem.switchState == true){
                                val rgb = "255, 255, 255"
                                context.let { it1 ->
                                    BluetoothFragment.sendDataToBluetoothDevice(rgb,
                                        it1
                                    )
                                }
                            }
                            else{
                                val rgb = "0, 0, 0"
                                context.let { it1 ->
                                    BluetoothFragment.sendDataToBluetoothDevice(rgb,
                                        it1
                                    )
                                }
                            }
                        }
                    }
                }
                Log.d("Alarm", "Am trecut printr-un ciclu de verificare")
                delay(60000)
            }
        }
    }

    // Funcție pentru obținerea abrevierii zilei din Calendar.DAY_OF_WEEK
    private fun getDayAbbreviation(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "D"
            Calendar.MONDAY -> "L"
            Calendar.TUESDAY -> "Ma"
            Calendar.WEDNESDAY -> "Mi"
            Calendar.THURSDAY -> "J"
            Calendar.FRIDAY -> "V"
            Calendar.SATURDAY -> "S"
            else -> ""
        }
    }

    private fun showNotification(context: Context, scheduleItem: ScheduleItem) {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(R.drawable.img_2)
            .setContentTitle("Alarma")
            .setContentText("Alarma cu eticheta ${scheduleItem.label} va porni intr-un minut.")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, builder.build())
    }
}