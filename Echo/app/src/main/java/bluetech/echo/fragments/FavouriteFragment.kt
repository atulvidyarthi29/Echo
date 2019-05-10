package bluetech.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import bluetech.echo.Database.EchoDatabase
import bluetech.echo.R
import bluetech.echo.Songs
import bluetech.echo.adapters.FavouriteAdapter


class FavouriteFragment : Fragment() {

    var myActivity:Activity?=null

    var noFavourites : TextView?=null
    var nowPlayingBottomBar: RelativeLayout?=null
    var playPauseButton : ImageButton?=null
    var songTitle : TextView? = null
    var recyclerView : RecyclerView?=null
    var trackPosition: Int=0
    var favouriteContent: EchoDatabase?=null

    var refreshList: ArrayList<Songs>?=null
    var getListFromDatabase: ArrayList<Songs>?=null

    object Statified{
        var mediaPlayer : MediaPlayer?=null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view= inflater.inflate(R.layout.fragment_favourite, container, false)
        activity.title="Favorites"
        noFavourites=view?.findViewById(R.id.noFavourites)
        nowPlayingBottomBar=view?.findViewById(R.id.hiddenBarFavScreen)
        playPauseButton=view?.findViewById(R.id.playPauseFavButton)
        songTitle=view?.findViewById(R.id.songTitleFavScreen)
        recyclerView=view?.findViewById(R.id.favouriteRecycler)

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity=context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity=activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favouriteContent=EchoDatabase(myActivity)
        display_favourites_by_searching()
        bottomBarSetup()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item=menu?.findItem(R.id.action_sort)
        item?.isVisible=false
    }

    fun getSongsFromPhone():ArrayList<Songs>{
        var arrayList=ArrayList<Songs>()
        var contentResolver= myActivity?.contentResolver
        var songUri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
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
            else{
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
            Statified.mediaPlayer=SongPlayingFragment.Statified.mediaPlayer
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

    fun display_favourites_by_searching(){
        if(favouriteContent?.checkSize() as Int > 0){
            refreshList=ArrayList<Songs>()
            getListFromDatabase=favouriteContent?.queryDBList()
            var fetchList= getSongsFromPhone()
            if(fetchList!=null){
                for(i in 0..fetchList?.size-1){
                    for(j in 0..getListFromDatabase?.size as Int -1 ){
                        if((getListFromDatabase?.get(j)?.songID)== (fetchList?.get(i)?.songID)){
                            refreshList?.add((getListFromDatabase as ArrayList<Songs>)[j])
                        }
                    }
                }
            }
            if(refreshList==null){
                recyclerView?.visibility=View.INVISIBLE
                noFavourites?.visibility=View.VISIBLE
            }

            else{
                var favouriteAdapter= FavouriteAdapter(refreshList as ArrayList<Songs>,myActivity as Context)
                val mLayoutManager=LinearLayoutManager(activity)
                recyclerView?.layoutManager=mLayoutManager
                recyclerView?.itemAnimator=DefaultItemAnimator()
                recyclerView?.adapter=favouriteAdapter
                recyclerView?.setHasFixedSize(true)
            }
        }
        else{
            recyclerView?.visibility=View.INVISIBLE
            noFavourites?.visibility=View.VISIBLE
        }
    }
}
