package bluetech.echo.activities

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.internal.NavigationMenuItemView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import bluetech.echo.R
import bluetech.echo.adapters.NavigationDrawerAdapter
import bluetech.echo.fragments.MainScreenFragment
import bluetech.echo.fragments.SongPlayingFragment

class MainActivity : AppCompatActivity(){

    var navigationDrawerIconList :ArrayList<String> = arrayListOf("All Songs","Favourite","Settings","About Us")
    var images_for_nav_Drawer = intArrayOf(R.drawable.navigation_allsongs,R.drawable.navigation_favorites,R.drawable.navigation_settings,
            R.drawable.navigation_aboutus)
    object statified {
        var drawerLayout: DrawerLayout? = null
        var notificationManager: NotificationManager?=null
    }
    var trackNotificationBuilder : Notification?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar=findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        MainActivity.statified.drawerLayout=findViewById(R.id.drawer_layout)
        val toggle=ActionBarDrawerToggle(this@MainActivity,MainActivity.statified.drawerLayout,toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        MainActivity.statified.drawerLayout?.setDrawerListener(toggle)
        toggle.syncState()

        val mainScreenFragment= MainScreenFragment()
        this.supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_details,mainScreenFragment,"Main Screen Fragment")
                .commit()

        var _navigationAdapter= NavigationDrawerAdapter(navigationDrawerIconList,images_for_nav_Drawer,this)
        _navigationAdapter.notifyDataSetChanged()

        var navigation_recycler_view= findViewById<RecyclerView>(R.id.navigator)
        navigation_recycler_view.layoutManager= LinearLayoutManager(this)
        navigation_recycler_view.itemAnimator =DefaultItemAnimator()
        navigation_recycler_view.adapter=_navigationAdapter
        navigation_recycler_view.setHasFixedSize(true)

        val intent= Intent(this@MainActivity,MainActivity:: class.java)
        val pIntent= PendingIntent.getActivity(this@MainActivity,System.currentTimeMillis().toInt(),
                    intent,0)
        trackNotificationBuilder=Notification.Builder(this)
                .setContentTitle("Track playing in the background")
                .setSmallIcon(R.drawable.echo_logo)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setAutoCancel(true)
                .build()
        statified.notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStart() {
        super.onStart()
        try{
            statified.notificationManager?.cancel(1965)
        }
        catch(e: Exception){
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try{
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                statified.notificationManager?.notify(1965,trackNotificationBuilder)
            }
        }
        catch (e : Exception){
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try{
            statified.notificationManager?.cancel(1965)
        }
        catch(e: Exception){
            e.printStackTrace()
        }
     }
}
