package jp.msaitoappdev.caregiver.humanmed

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.getSystemService
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import jp.msaitoappdev.caregiver.humanmed.notifications.ReminderNotifier
// ★ 追加: Remote Config
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

@HiltAndroidApp
class CaregiverApp : Application() {
  override fun onCreate() {
    super.onCreate()
    createReminderChannel()
    MobileAds.initialize(this)

    // ★ Remote Config: 既定値を適用し、起動時に fetch&activate
    val rc = Firebase.remoteConfig
    rc.setDefaultsAsync(R.xml.remote_config_defaults)
    rc.fetchAndActivate()
  }

  private fun createReminderChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        ReminderNotifier.CHANNEL_ID,
        "学習リマインド",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply { description = "毎日3問のリマインド通知" }
      getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }
  }
}
