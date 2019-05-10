package bluetech.echo.fragments


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import bluetech.echo.Database.EchoDatabase
import bluetech.echo.R
import bluetech.echo.Songs
import bluetech.echo.currentSongHelper
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import kotlinx.android.synthetic.main.fragment_song_playing.*
import java.util.*
import java.util.concurrent.TimeUnit


class SongPlayingFragment : Fragment() {

    object Statified{
        var myActivity :Activity?=null
        var mediaPlayer:MediaPlayer?=null
        var startTimeText : TextView?=null
        var endTimeText : TextView?=null
        var playPauseImageButton: ImageButton?=null
        var previousImageButton: ImageButton?=null
        var nextImageButton: ImageButton?=null
        var loopImageButton: ImageButton?=null
        var seekbar: SeekBar?=null
        var songArtistView: TextView?=null
        var songTitleView: TextView?=null
        var shuffleImageButton: ImageButton?=null
        var audioVisualization : AudioVisualization?=null
        var glView:GLAudioVisualizationView?=null
        var fab: ImageButton?=null

        var currentPosition:Int=0
        var fetchSongs: ArrayList<Songs>?=null
        var CurrentSongHelper: currentSongHelper?=null
        var favouriteContent:EchoDatabase?=null
        var mSensorManager :SensorManager?=null
        var mSensorListener: SensorEventListener?=null

        var MY_PREFS_NAME="ShakeFeature"

        var updateSongTime=object: Runnable{
            override fun run() {
                var getCurrent=mediaPlayer?.currentPosition
                startTimeText?.setText(String.format("%d: %d",
                        TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong()as Long),
                        TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent.toLong())))

                )
                seekbar?.setProgress(getCurrent?.toInt() as Int)
                Handler().postDelayed(this,1000)
            }
        }

    }



    object Staticated{
        var MY_PREFS_SHUFFLE="Shuffle feature"
        var MY_PREFS_LOOP="Loop feature"

        fun OnSongComplete(){
            if(Statified.CurrentSongHelper?.isShuffle as Boolean){
                playNext("PlayNextLikeNormalShuffle")
                Statified.CurrentSongHelper?.isPlaying=true
            }
            else{
                if(Statified.CurrentSongHelper?.isLoop as Boolean){
                    Statified.CurrentSongHelper?.isPlaying=true
                    var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)

                    Statified.CurrentSongHelper?.songPath = nextSong?.songData
                    Statified.CurrentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.CurrentSongHelper?.songArtist = nextSong?.artist
                    Statified.CurrentSongHelper?.songId = nextSong?.songID as Long
                    Statified.CurrentSongHelper?.currentPosition = Statified.currentPosition

                    updateTextViews(Statified.CurrentSongHelper?.songTitle as String,Statified.CurrentSongHelper?.songArtist as String)

                    Statified.mediaPlayer?.reset()
                    try {
                        Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.CurrentSongHelper?.songPath))
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()
                        processInformation(Statified.mediaPlayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                else{
                    playNext("PlayNextNormal")
                    Statified.CurrentSongHelper?.isPlaying=true
                }
            }
            if(Statified.favouriteContent?.checkifIDExists(Statified.CurrentSongHelper?.songId?.toInt() as Int ) as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_on))
            }
            else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_off))
            }
        }

        fun updateTextViews(songTitle: String,songArtist:String){
            var songTitleUpdated=songTitle
            var songArtistUpdated=songArtist
            if(songTitleUpdated.equals("<unknown>",ignoreCase = true))
                songTitleUpdated="unknown"
            if(songArtistUpdated.equals("<unknown>",ignoreCase = true))
                songArtistUpdated="unknown"
            Statified.songTitleView?.setText(songTitleUpdated)
            Statified.songArtistView?.setText(songArtistUpdated)

        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            Statified.seekbar?.max = finalTime
            Statified.startTimeText?.setText(String.format("%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong())))
            )
            Statified.endTimeText?.setText(String.format("%d: %d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong())))
            )
            Statified.seekbar?.setProgress(startTime)
            Handler().postDelayed(Statified.updateSongTime, 1000)
        }


        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true)) {
                Statified.currentPosition = Statified.currentPosition + 1
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(Statified.fetchSongs?.size?.plus(1) as Int)
                Statified.currentPosition = randomPosition
            }
            if (Statified.currentPosition == Statified.fetchSongs?.size) {
                Statified.currentPosition = 0
            }
            Statified.CurrentSongHelper?.isLoop = false

            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.CurrentSongHelper?.songPath = nextSong?.songData
            Statified.CurrentSongHelper?.songTitle = nextSong?.songTitle
            Statified.CurrentSongHelper?.songArtist = nextSong?.artist
            Statified.CurrentSongHelper?.songId = nextSong?.songID as Long
            Statified.CurrentSongHelper?.currentPosition = Statified.currentPosition

            updateTextViews(Statified.CurrentSongHelper?.songTitle as String,Statified.CurrentSongHelper?.songArtist as String)

            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.CurrentSongHelper?.songPath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                processInformation(Statified.mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if(Statified.favouriteContent?.checkifIDExists(Statified.CurrentSongHelper?.songId?.toInt() as Int ) as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_on))
            }
            else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_off))
            }
        }
    }

    var mAcceleration: Float=0f
    var mAccelerationCurrent: Float=0f
    var mAccelerationLast: Float=0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        activity.title="Now Playing"
        var view= inflater.inflate(R.layout.fragment_song_playing, container, false)
        Statified.seekbar=view?.findViewById(R.id.seekBar)
        Statified.startTimeText=view?.findViewById(R.id.startTime)
        Statified.endTimeText=view?.findViewById(R.id.endTime)
        Statified.playPauseImageButton=view?.findViewById(R.id.playPauseButton)
        Statified.previousImageButton=view?.findViewById(R.id.previousButton)
        Statified.nextImageButton=view?.findViewById(R.id.nextButton)
        Statified.loopImageButton=view?.findViewById(R.id.loopButton)
        Statified.songArtistView=view?.findViewById(R.id.songArtist)
        Statified.songTitleView=view?.findViewById(R.id.songTitle)
        Statified.shuffleImageButton=view?.findViewById(R.id.shuffleButton)
        Statified.fab=view?.findViewById(R.id.favouriteIcon)
        Statified.fab?.alpha=0.8f
        Statified.favouriteContent=EchoDatabase(Statified.myActivity)
        Statified.glView=view?.findViewById(R.id.visualizer_view)

        return view;
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization=Statified.glView as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myActivity=context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity=activity
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualization?.onResume()
        Statified.mSensorManager?.registerListener(Statified.mSensorListener,
                Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        Statified.audioVisualization?.onPause()
        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Statified.audioVisualization?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager=Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration=0.0f
        mAccelerationLast=SensorManager.GRAVITY_EARTH
        mAccelerationCurrent=SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem?= menu?.findItem(R.id.action_redirect)
        item?.isVisible=true
        val item2: MenuItem?= menu?.findItem(R.id.action_sort)
        item2?.isVisible=false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_redirect-> {
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Statified.CurrentSongHelper= currentSongHelper()
        Statified.CurrentSongHelper?.isPlaying=true
        Statified.CurrentSongHelper?.isLoop=false
        Statified.CurrentSongHelper?.isShuffle=false
        var path : String?=null
        var _songTitle: String?=null
        var _songArtist: String?=null
        var songId : Long=0
        try {
            path=arguments.getString("path")
            _songArtist=arguments.getString("songArtist")
            _songTitle=arguments.getString("songTitle")
            songId=arguments.getInt("songId").toLong()

            Statified.currentPosition=arguments.getInt("songPosition")
            Statified.fetchSongs=arguments.getParcelableArrayList("songData")

            Statified.CurrentSongHelper?.songPath=path
            Statified.CurrentSongHelper?.songTitle=_songTitle
            Statified.CurrentSongHelper?.songArtist=_songArtist
            Statified.CurrentSongHelper?.songId=songId
            Statified.CurrentSongHelper?.currentPosition=Statified.currentPosition

            Staticated.updateTextViews(Statified.CurrentSongHelper?.songTitle as String,Statified.CurrentSongHelper?.songArtist as String)

        }catch (e: Exception){
            e.printStackTrace()
        }
        var fromFavourite=arguments.get("FavBottomBar") as? String
        if(fromFavourite !=null){
            Statified.mediaPlayer=FavouriteFragment.Statified.mediaPlayer
        }
        else {
            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(path))
                Statified.mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
        }
        Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        if(Statified.CurrentSongHelper?.isPlaying as Boolean){
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        }
        else{
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.mediaPlayer?.setOnCompletionListener {
            Staticated.OnSongComplete()
        }
        clickHandler()
        var visualizationHanndler=DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context,0)
        Statified.audioVisualization?.linkTo(visualizationHanndler)

        var prefsForShuffle=Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)
        var isShuffleAllowed=prefsForShuffle?.getBoolean("feature",false)
        if(isShuffleAllowed as Boolean){
            Statified.CurrentSongHelper?.isShuffle=true
            Statified.CurrentSongHelper?.isLoop=false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }
        else{
            Statified.CurrentSongHelper?.isShuffle=false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            }

        var prefsForLoop=Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)
        var isLoopAllowed=prefsForLoop?.getBoolean("feature",false)
        if(isLoopAllowed as Boolean){
            Statified.CurrentSongHelper?.isShuffle=false
            Statified.CurrentSongHelper?.isLoop=true
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        }
        else{
            Statified.CurrentSongHelper?.isLoop=false
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        }
        if(Statified.favouriteContent?.checkifIDExists(Statified.CurrentSongHelper?.songId?.toInt() as Int ) as Boolean){
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_on))
        }
        else{
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_off))
        }
    }


    fun clickHandler(){
        Statified.fab?.setOnClickListener({
            if(Statified.favouriteContent?.checkifIDExists(Statified.CurrentSongHelper?.songId?.toInt() as Int ) as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_off))
                Statified.favouriteContent?.deleteFavourite(Statified.CurrentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity,"Removed from favourites",Toast.LENGTH_SHORT).show()
            }
            else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_on))
                Statified.favouriteContent?.storeAsFavourite(Statified.CurrentSongHelper?.songId?.toInt(),Statified.CurrentSongHelper?.songArtist,
                        Statified.CurrentSongHelper?.songTitle,Statified.CurrentSongHelper?.songPath)
                Toast.makeText(Statified.myActivity,"Added to favourites",Toast.LENGTH_SHORT).show()
            }

        })
        Statified.shuffleImageButton?.setOnClickListener({
            var editorShuffle=Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)?.edit()
            var editorLoop=Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,Context.MODE_PRIVATE)?.edit()
            if(Statified.CurrentSongHelper?.isShuffle as Boolean){ //disable
                Statified.CurrentSongHelper?.isShuffle=false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature",false)
                editorShuffle?.apply()
                Toast.makeText(Statified.myActivity,"Shuffle Disabled",Toast.LENGTH_SHORT).show()

            }else{//enable
                Statified.CurrentSongHelper?.isLoop=false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                Statified.CurrentSongHelper?.isShuffle=true
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                editorShuffle?.putBoolean("feature",true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature",false)
                editorLoop?.apply()
                Toast.makeText(Statified.myActivity,"Shuffle Enabled",Toast.LENGTH_SHORT).show()
            }
        })
        Statified.nextImageButton?.setOnClickListener({
            Statified.CurrentSongHelper?.isPlaying=true
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if(Statified.CurrentSongHelper?.isShuffle as Boolean){
                Staticated.playNext("PlayNextLikeNormalShuffle")
            }
            else{
                Staticated.playNext("PlayNextNormal")
            }
        })

        Statified.previousImageButton?.setOnClickListener({
            Statified.CurrentSongHelper?.isPlaying=true
            if(Statified.CurrentSongHelper?.isLoop as Boolean){
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        })

        Statified.loopImageButton?.setOnClickListener({
            var editorShuffle=Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE,Context.MODE_PRIVATE)?.edit()
            var editorLoop=Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP,Context.MODE_PRIVATE)?.edit()
            if(Statified.CurrentSongHelper?.isLoop as Boolean){
                Statified.CurrentSongHelper?.isLoop=false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature",false)
                editorLoop?.apply()
                Toast.makeText(Statified.myActivity,"Loop Disabled",Toast.LENGTH_SHORT).show()
            }
            else{
                Statified.CurrentSongHelper?.isLoop=true
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.CurrentSongHelper?.isShuffle=false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorLoop?.putBoolean("feature",true)
                editorLoop?.apply()
                editorShuffle?.putBoolean("feature",false)
                editorShuffle?.apply()
                Toast.makeText(Statified.myActivity,"Loop Enabled",Toast.LENGTH_SHORT).show()
            }
        })

        Statified.playPauseImageButton?.setOnClickListener({
                if(Statified.mediaPlayer?.isPlaying as Boolean){
                    Statified.mediaPlayer?.pause()
                    Statified.CurrentSongHelper?.isPlaying=false
                    Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                }
            else
                {
                    Statified.mediaPlayer?.start()
                    Statified.CurrentSongHelper?.isPlaying=true
                    Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                }
        })
    }



        fun playPrevious() {
            Statified.currentPosition = Statified.currentPosition - 1

            if (Statified.currentPosition == -1) {
                Statified.currentPosition = 0
            }
            if (Statified.CurrentSongHelper?.isPlaying as Boolean) {
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            } else {
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            }

            Statified.CurrentSongHelper?.isLoop = false

            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.CurrentSongHelper?.songPath = nextSong?.songData
            Statified.CurrentSongHelper?.songTitle = nextSong?.songTitle
            Statified.CurrentSongHelper?.songArtist = nextSong?.artist
            Statified.CurrentSongHelper?.songId = nextSong?.songID as Long
            Statified.CurrentSongHelper?.currentPosition = Statified.currentPosition

            Staticated.updateTextViews(Statified.CurrentSongHelper?.songTitle as String,Statified.CurrentSongHelper?.songArtist as String)

            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.CurrentSongHelper?.songPath))
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if(Statified.favouriteContent?.checkifIDExists(Statified.CurrentSongHelper?.songId?.toInt() as Int ) as Boolean){
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_on))
            }
            else{
                Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity,R.drawable.favorite_off))
            }
        }


    fun bindShakeListener(){
        Statified.mSensorListener=object:SensorEventListener{
            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

            }

            override fun onSensorChanged(p0: SensorEvent) {
                val x=p0.values[0]
                val y=p0.values[1]
                val z=p0.values[2]
                mAccelerationLast=mAccelerationCurrent
                mAccelerationCurrent=Math.sqrt(((x*x+y*y+z*z).toDouble())).toFloat()
                val delta=mAccelerationCurrent-mAccelerationLast
                mAcceleration= mAcceleration*0.9f + delta

                if(mAcceleration > 12){
                    var pref=Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME,Context.MODE_PRIVATE)
                    var isAllowed=pref?.getBoolean("feature",false)
                    if(isAllowed as Boolean) {
                        Staticated.playNext("PlayNextNormal")
                    }
                }
            }

        }
    }

}
