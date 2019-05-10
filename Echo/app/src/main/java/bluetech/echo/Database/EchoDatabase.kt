package bluetech.echo.Database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import bluetech.echo.Songs

class EchoDatabase: SQLiteOpenHelper{

    var _songList= ArrayList<Songs>()
    object Static {
        val DB_NAME = "FavouriteDatabase"
        val DB_VERSION=1
        val TABLE_NAME = "FavouriteTable"
        val COLUMN_ID = "songId"
        val COLUMN_SONG_TITLE = "songTitle"
        val COLUMN_SONG_ARTIST = "songArtist"
        val COLUMN_SONG_PATH = "songPath"
    }
    override fun onCreate(sqliteDatabase: SQLiteDatabase?) { //when we create the table
        sqliteDatabase?.execSQL("CREATE TABLE "+ Static.TABLE_NAME+"("+Static.COLUMN_ID+" INTEGER, "+Static.COLUMN_SONG_ARTIST+" STRING, "
                +Static.COLUMN_SONG_TITLE+" STRING, "+Static.COLUMN_SONG_PATH+" STRING);")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {//when we have upgraded like added columns

    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version)
    constructor(context: Context?) : super(context, Static.DB_NAME, null, Static.DB_VERSION)

    fun storeAsFavourite(id : Int?,artist:String? ,song_title: String?, path: String?){
        val db=this.writableDatabase
        var contentValues=ContentValues()
        contentValues.put(Static.COLUMN_ID,id)
        contentValues.put(Static.COLUMN_SONG_ARTIST,artist)
        contentValues.put(Static.COLUMN_SONG_PATH,path)
        contentValues.put(Static.COLUMN_SONG_TITLE,song_title)
        db.insert(Static.TABLE_NAME,null,contentValues)
        db.close()
    }

    fun queryDBList():ArrayList<Songs>? {
        try {
            val db = this.readableDatabase
            val query_param = "SELECT * FROM " + Static.TABLE_NAME
            var cSor = db.rawQuery(query_param, null)
            if (cSor.moveToFirst()) {
                do {
                    var _id = cSor.getInt(cSor.getColumnIndexOrThrow(Static.COLUMN_ID))
                    var _title = cSor.getString(cSor.getColumnIndexOrThrow(Static.COLUMN_SONG_TITLE))
                    var _artist = cSor.getString(cSor.getColumnIndexOrThrow(Static.COLUMN_SONG_ARTIST))
                    var _path = cSor.getString(cSor.getColumnIndexOrThrow(Static.COLUMN_SONG_PATH))
                    _songList.add(Songs(_id.toLong(), _title, _artist, _path, 0))
                } while (cSor.moveToNext())

            } else
                return null
        } catch (e: Exception) {
                e.printStackTrace()
        }
        return _songList
    }

    fun checkifIDExists(_id :Int):Boolean{
        var storeId=-1069
        var db=this.readableDatabase
        var query="SELECT * FROM "+Static.TABLE_NAME+" WHERE songId='$_id'"
        var cSor=db.rawQuery(query,null)
        if(cSor.moveToFirst()){
            do {
                storeId = cSor.getInt(cSor.getColumnIndexOrThrow(Static.COLUMN_ID))
            }while(cSor.moveToNext())
        }
        else
            return false
        return storeId != -1069
    }

    fun deleteFavourite(_id:Int){
        val db=this.writableDatabase
        db.delete(Static.TABLE_NAME,Static.COLUMN_ID+"="+_id,null)
        db.close()
    }
    fun checkSize():Int{
        var counter: Int=0
        var db=this.readableDatabase
        var query="SELECT * FROM "+Static.TABLE_NAME
        var cSor=db.rawQuery(query,null)
        if(cSor.moveToFirst()){
            do{
                counter=counter+1
            }while(cSor.moveToNext())
        }
        else
            return 0
        return counter
    }
}