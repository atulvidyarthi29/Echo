package bluetech.echo.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import bluetech.echo.R
import bluetech.echo.activities.MainActivity
import bluetech.echo.fragments.AboutUsFragment
import bluetech.echo.fragments.FavouriteFragment
import bluetech.echo.fragments.MainScreenFragment
import bluetech.echo.fragments.SettingsFragment

class NavigationDrawerAdapter(_contentList:ArrayList<String>,_getImages:IntArray,_context: Context): RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>() {
    var contentList:ArrayList<String>?=null
    var getImages:IntArray?=null
    var mContext:Context?=null
    init{
        this.contentList= _contentList
        this.getImages=_getImages
        this.mContext=_context
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): NavViewHolder {
        var itemView=LayoutInflater.from(parent?.context)
                .inflate(R.layout.row_custom_navigationdrawer,parent,false)
        val returnThis=NavViewHolder(itemView)
        return returnThis
    }

    override fun getItemCount(): Int {
        return (contentList as ArrayList).size
    }

    override fun onBindViewHolder(holder: NavViewHolder?, position: Int) {
        holder?.icon_GET?.setBackgroundResource(getImages?.get(position)as Int)
        holder?.text_GET?.setText(contentList?.get(position))
        holder?.contentHolder?.setOnClickListener({
            if(position==0){
                var mainScreenFragment=MainScreenFragment()
                (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_details,mainScreenFragment)
                        .commit()
            }
            else if(position==1){
                var favouriteFragment=FavouriteFragment()
                (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_details,favouriteFragment)
                        .commit()
            }
            else if(position==2) {
                var settingFragment = SettingsFragment()
                (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_details, settingFragment)
                        .commit()
            }
            else if(position == 3){
                var aboutusFragment = AboutUsFragment()
                (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_details, aboutusFragment)
                        .commit()
            }
            MainActivity.statified.drawerLayout?.closeDrawers()
        })
    }

    class NavViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView){
        var icon_GET : ImageView?=null
        var text_GET : TextView?=null
        var contentHolder :RelativeLayout?=null
        init{
            icon_GET=itemView?.findViewById(R.id.icon_navdrawer)
            text_GET=itemView?.findViewById(R.id.text_navdrawer)
            contentHolder=itemView?.findViewById(R.id.navdrawer_item_content_holder)
        }

    }
}