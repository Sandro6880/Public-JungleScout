package at.htl.android_frontend.network

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


/**
 * This class handles the state of the map that the client is using. This is needed because
 * the saveOnInstance method already provided by Android only works when the app crashes.
 * Because of that, we save the necessary key components in the database to allow the user to
 * always come back to the same state of map that he was on previously.
 */
class DataBaseHandlerSaveMapInstance(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    var context: Context
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
        db.execSQL(CREATE_TABLE_INDEX)
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DROP_TABLE)
        db.execSQL(DROP_TABLE_INDEX)
        onCreate(db)
    }

    fun deleteEntry(row: Long) {
        val sqLiteDatabase = writableDatabase
        sqLiteDatabase.delete(TABLE_NAME_LAT_LNG, KEY_ID + "=" + row, null)
    }

    companion object {
        const val DATABASE_NAME = "dataManager"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME_LAT_LNG = "TBL_LAT_LNG"
        const val TABLE_NAME_INDEX = "TBL_INDEX"
        const val KEY_ID = "id"
        const val KEY_LAT = "lat"
        const val KEY_LNG = "lng"
        const val KEY_INDEX = "in_num"

        const val CREATE_TABLE = "CREATE TABLE " + TABLE_NAME_LAT_LNG + "(" + KEY_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_LAT + " REAL," + KEY_LNG + " REAL," + KEY_INDEX + " INTEGER" + ")"

        const val CREATE_TABLE_INDEX = "CREATE TABLE " + TABLE_NAME_INDEX + "(" + KEY_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_INDEX + " INTEGER" + ")"

        const val DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME_LAT_LNG + ""
        const val DROP_TABLE_INDEX = "DROP TABLE IF EXISTS " + TABLE_NAME_INDEX + ""
    }

    init {
        this.context = context
        //Toast.makeText(context, "Constructor called", Toast.LENGTH_LONG).show();
    }
}