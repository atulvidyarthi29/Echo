package bluetech.echo.fragments


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import bluetech.echo.R

class AboutUsFragment : Fragment() {


    var textData: TextView?=null
    var fbButton : ImageButton?=null
    var linkedlnButton: ImageButton?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity.title="About Us"
        var view= inflater.inflate(R.layout.fragment_about_us, container, false)
        textData=view?.findViewById(R.id.textDetails)
        fbButton=view?.findViewById(R.id.fb)
        linkedlnButton=view?.findViewById(R.id.linked)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        var temp=ScrollingMovementMethod()
        textData?.setMovementMethod(temp)
        fbButton?.setOnClickListener {
            var intent= Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/atul.vidyarthi.50"))
            startActivity(intent)
        }
        linkedlnButton?.setOnClickListener {
            var intent= Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/atul-vidyarthi-320a18150/"))
            startActivity(intent)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item=menu?.findItem(R.id.action_sort)
        item?.isVisible=false
    }
}
