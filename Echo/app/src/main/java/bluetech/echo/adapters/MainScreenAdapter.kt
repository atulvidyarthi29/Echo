package bluetech.echo.adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import bluetech.echo.R
import bluetech.echo.Songs
import bluetech.echo.activities.MainActivity
import bluetech.echo.fragments.MainScreenFragment
import bluetech.echo.fragments.SongPlayingFragment

class MainScreenAdapter(_songDetails :ArrayList<Songs>,context:Context):RecyclerView.Adapter<MainScreenAdapter.MyViewHolder>(){


    var songDetails:ArrayList<Songs>?=null
    var mContext:Context?=null

    init{
        songDetails=_songDetails
        mContext=context
    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyViewHolder {
        val itemView=LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_custom_mainscreen_adapter,parent,false)

        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if(songDetails==null) {
            return 0
        }
        else {
            return (songDetails as ArrayList<Songs>).size
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val songObject=songDetails?.get(position)
        holder.trackTitle?.text=songObject?.songTitle
        holder.trackArtist?.text=songObject?.artist
        holder.contentHolder?.setOnClickListener(
                {
                    try {
                        if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                            SongPlayingFragment.Statified.mediaPlayer?.reset()
                        }
                    }
                    catch(e : Exception){
                        e.printStackTrace()
                    }
                    var songPlayingFragment= SongPlayingFragment()
                    var args= Bundle()
                    args.putString("songArtist",songObject?.artist)
                    args.putString("songTitle",songObject?.songTitle)
                    args.putString("path",songObject?.songData)
                    args.putInt("songId",songObject?.songID?.toInt() as Int)
                    args.putInt("songPosition",position)
                    args.putParcelableArrayList("songData",songDetails)
                    songPlayingFragment.arguments=args
                    (mContext as FragmentActivity).supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment_details,songPlayingFragment)
                            .addToBackStack("SongPlayingFragment")
                            .commit()
                }
        )

    }

    class MyViewHolder(view: View):RecyclerView.ViewHolder(view){
        var trackTitle: TextView?=null
        var trackArtist:TextView?=null
        var contentHolder: RelativeLayout?=null
        init{
            trackTitle=view?.findViewById(R.id.trackTitle)
            trackArtist=view?.findViewById(R.id.trackArtist)
            contentHolder=view?.findViewById(R.id.contentRow)
        }
    }

}