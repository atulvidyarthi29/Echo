package bluetech.echo.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import bluetech.echo.R
import bluetech.echo.Songs
import bluetech.echo.adapters.MainScreenAdapter
import java.util.*


/**
 * A simple [Fragment] subclass.
 *
 */
class MainScreenFragment : Fragment() {

    var getSongsList:ArrayList<Songs>?=null
    var nowPlayingBottomBar:RelativeLayout?=null
    var playPauseButton: ImageButton?=null
    var songTitle: TextView?=null
    var visibleLayout:RelativeLayout?=null
    var noSongs: TextView?=null
    var recyclerView: RecyclerView?=null
    var myActivity: Activity?=null
    var _mainScreenAdapter: MainScreenAdapter?=null
    var trackPosition: Int=0


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        activity.title="All Songs"
        var view= inflater!!.inflate(R.layout.fragment_main_screen, container, false)
        visibleLayout=view?.findViewById(R.id.visibleLayout)
        noSongs=view?.findViewById(R.id.noSongs)
        nowPlayingBottomBar=view?.findViewById<RelativeLayout>(R.id.hiddenBarMainScreen)
        songTitle=view?.findViewById<TextView>(R.id.songTitleMainScreen)
        playPauseButton=view?.findViewById<ImageButton>(R.id.playPauseButton)
        recyclerView=view?.findViewById<RecyclerView>(R.id.contentMain)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.main,menu)
        return
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val switcher=item?.itemId

        if(switcher==R.id.action_sort_recent){
            val editor= myActivity?.getSharedPreferences("action_sort",Context.MODE_PRIVATE)?.edit()
            editor?.putString("action_sort_ascending","true")
            editor?.putString("action_sort_recent","false")
            editor?.apply()
            if(getSongsList!=null){
                Collections.sort(getSongsList,Songs.Statified.dateComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }

        else if(switcher== R.id.action_sort_ascending){
            val editorTwo= myActivity?.getSharedPreferences("action_sort",Context.MODE_PRIVATE)?.edit()
            editorTwo?.putString("action_sort_ascending","false")
            editorTwo?.putString("action_sort_recent","true")
            editorTwo?.apply()
            if(getSongsList!=null){
                Collections.sort(getSongsList,Songs.Statified.nameComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
            return false
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getSongsList=getSongsFromPhone()
        val prefs=myActivity?.getSharedPreferences("action_sort",Context.MODE_PRIVATE)
        val action_sort_ascending=prefs?.getString("action_sort_ascending","true")
        val action_sort_recent=prefs?.getString("action_sort_recent","false")

        if(getSongsList?.size as Int <=0){
            visibleLayout?.visibility=View.INVISIBLE
            noSongs?.visibility=View.VISIBLE
        }
        else{
            _mainScreenAdapter=MainScreenAdapter(getSongsList as ArrayList<Songs>,myActivity as Context)
            var mLayoutManager= LinearLayoutManager(myActivity)
            recyclerView?.layoutManager=mLayoutManager
            recyclerView?.itemAnimator=DefaultItemAnimator()
            recyclerView?.adapter=_mainScreenAdapter
        }
        if(getSongsList !=null) {
            if( action_sort_ascending!!.equals("true",ignoreCase = true)){
                Collections.sort(getSongsList,Songs.Statified.nameComparator)
            }
            else if (action_sort_recent!!.equals("true",ignoreCase = true)){
                Collections.sort(getSongsList,Songs.Statified.nameComparator)
            }
            _mainScreenAdapter?.notifyDataSetChanged()
        }
        bottomBarSetup()
    }

    fun bottomBarSetup(){
        try{
            BottombarClickHandler()
            if(SongPlayingFragment.Statified.mediaPlayer != null){
                nowPlayingBottomBar?.visibility = View.VISIBLE
            }
            else {
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
            else {
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }
            songTitle?.setText(SongPlayingFragment.Statified.CurrentSongHelper?.songTitle)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener({
                songTitle?.setText(SongPlayingFragment.Statified.CurrentSongHelper?.songTitle)
                SongPlayingFragment.Staticated.OnSongComplete()

            })

        }
        catch(e : Exception){
            e.printStackTrace()
        }
    }

    fun BottombarClickHandler(){
        nowPlayingBottomBar?.setOnClickListener({
            FavouriteFragment.Statified.mediaPlayer=SongPlayingFragment.Statified.mediaPlayer
            var songPlayingFragment= SongPlayingFragment()
            var args= Bundle()
            args.putString("songArtist",SongPlayingFragment.Statified.CurrentSongHelper?.songArtist)
            args.putString("songTitle",SongPlayingFragment.Statified.CurrentSongHelper?.songTitle)
            args.putString("path",SongPlayingFragment.Statified.CurrentSongHelper?.songPath)
            args.putInt("songId",SongPlayingFragment.Statified.CurrentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition",SongPlayingFragment.Statified.CurrentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData",SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar","Sucess")
            songPlayingFragment.arguments=args
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_details,songPlayingFragment)
                    .addToBackStack("SongPlayingFragment")
                    .commit()
        })
        playPauseButton?.setOnClickListener({
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition=SongPlayingFragment.Statified.mediaPlayer?.getCurrentPosition() as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }
            else{
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity=context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity=activity
    }
    fun getSongsFromPhone():ArrayList<Songs>{
        var arrayList=ArrayList<Songs>()
        var contentResolver= myActivity?.contentResolver
        var songUri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor=contentResolver?.query(songUri,null,null,null,null)
        if(songCursor!=null && songCursor.moveToFirst()){
            val songId=songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle=songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist=songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData=songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex=songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while(songCursor.moveToNext()){
                var currentId=songCursor.getLong(songId)
                var currentTitle=songCursor.getString(songTitle)
                var currentArtist=songCursor.getString(songArtist)
                var currentData=songCursor.getString(songData)
                var currentDate=songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId,currentTitle,currentArtist,currentData,currentDate))
            }
        }

        return arrayList
    }

}
