package chat.tox.antox.activities

import java.io.{IOException, PrintWriter, _}
import java.text.SimpleDateFormat
import java.util.{Calendar, List, Locale, Scanner}
import java.io.InputStreamReader
import java.util

import android.app.{ActivityManager, AlertDialog, Application, NotificationManager}
import android.content.res.Configuration
import android.content._
import android.net.ConnectivityManager
import android.os.{Build, Bundle, Environment}
import android.preference.PreferenceManager
import android.support.multidex.MultiDex
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.{MenuItem, View, WindowManager}
import chat.tox.antox.{CrashActivity, R}
import chat.tox.antox.data.State
import chat.tox.antox.fragments.MainDrawerFragment
import chat.tox.antox.theme.ThemeManager
import chat.tox.antox.utils._

class MainActivity extends AppCompatActivity {


  var request: View = _

  var preferences: SharedPreferences = _

  protected override def onCreate(savedInstanceState: Bundle) {

    super.onCreate(savedInstanceState)

    preferences = PreferenceManager.getDefaultSharedPreferences(this)
    ThemeManager.init(getApplicationContext)

    // Set the right language
    selectLanguage()

    setContentView(R.layout.activity_main)

    // Use a toolbar so that the drawer goes above the action bar
    val toolbar = findViewById(R.id.toolbar).asInstanceOf[Toolbar]
    setSupportActionBar(toolbar)

    getSupportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu)
    getSupportActionBar.setDisplayHomeAsUpEnabled(true)
    ThemeManager.applyTheme(this, getSupportActionBar)

    // Fix for Android 4.1.x
    if (Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN &&
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      getWindow.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
    }

    // Check to see if Internet is potentially available and show a warning if it isn't
    if (!isNetworkConnected) {
      showAlertDialog(MainActivity.this, getString(R.string.main_no_internet), getString(R.string.main_not_connected))
    }

    // Give ToxSingleton an instance of notification manager for use in displaying notifications from callbacks
    AntoxNotificationManager.mNotificationManager =
      Some(getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager])

    if (preferences.getBoolean("notifications_persistent", false)) {
      AntoxNotificationManager.createPersistentNotification(getApplicationContext)
    }

    // Initialise the bitmap manager for storing bitmaps in a cache
    new BitmapManager()

    // Removes the drop shadow from the actionbar as it overlaps the tabs
    getSupportActionBar.setElevation(0)

    // set autoaccept option on startup
    State.setAutoAcceptFt(preferences.getBoolean("autoacceptft", false))
    // System.out.println("load autoacceptft options : "+State.getAutoAcceptFt());

    Options.videoCallStartWithNoVideo = preferences.getBoolean("videocallstartwithnovideo", false)
    // System.out.println("load videocallstartwithnovideo options : "+Options.videoCallStartWithNoVideo);

    State.setBatterySavingMode(preferences.getBoolean("batterysavingmode", false))
  }

  override protected def attachBaseContext(base: Context) {
    super.attachBaseContext(base)
    MultiDex.install(this)
  }

  def onClickAdd(v: View) {
    val intent = new Intent(this, classOf[AddActivity])
    startActivityForResult(intent, Constants.ADD_FRIEND_REQUEST_CODE)
  }

  override def onBackPressed(): Unit = {
    val drawerFragment = getSupportFragmentManager.findFragmentById(R.id.drawer).asInstanceOf[MainDrawerFragment]
    if (drawerFragment.isDrawerOpen) {
      drawerFragment.closeDrawer()
    } else {
      super.onBackPressed()
    }
  }

  override def onPause() {
    super.onPause()
  }

  override def onDestroy() {
    super.onDestroy()
  }

  /**
    * Displays a generic dialog using the strings passed in.
    * TODO: Should maybe be refactored into separate class and used for other dialogs?
    */
  def showAlertDialog(context: Context, title: String, message: String) {
    val alertDialog = new AlertDialog.Builder(context).create()
    alertDialog.setTitle(title)
    alertDialog.setMessage(message)
    alertDialog.setIcon(R.drawable.ic_launcher)
    alertDialog.setButton("OK", (dialog: DialogInterface, which: Int) => {
    })
    alertDialog.show()
  }

  /**
    * Checks to see if Wifi or Mobile have a network connection
    */
  private def isNetworkConnected: Boolean = {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    val networkInfo = connectivityManager.getAllNetworkInfo

    for (info <- networkInfo) {
      if ("WIFI".equalsIgnoreCase(info.getTypeName) && info.isConnected) {
        return true
      } else if ("MOBILE".equalsIgnoreCase(info.getTypeName) && info.isConnected) {
        return true
      }
    }

    false
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val id = item.getItemId
    if (id == android.R.id.home) {
      val drawer = getSupportFragmentManager.findFragmentById(R.id.drawer).asInstanceOf[MainDrawerFragment]
      drawer.openDrawer()
      true
    } else {
      super.onOptionsItemSelected(item)
    }
  }

  private def selectLanguage() {
    val localeString = preferences.getString("locale", "-1")
    val locale = getResources.getConfiguration.locale

    if (localeString == "-1") {
      val editor = preferences.edit()
      val currentLanguage = locale.getLanguage.toLowerCase
      val currentCountry = locale.getCountry

      editor.putString("locale", currentLanguage + "_" + currentCountry)
      editor.apply()
    } else {
      val locale = if (localeString.contains("_")) {
        val (language, country) = localeString.splitAt(localeString.indexOf("_"))
        new Locale(language, country)
      } else {
        new Locale(localeString)
      }

      Locale.setDefault(locale)
      val config = new Configuration()
      config.locale = locale
      getApplicationContext.getResources.updateConfiguration(config, getApplicationContext.getResources.getDisplayMetrics)
    }
  }


}
