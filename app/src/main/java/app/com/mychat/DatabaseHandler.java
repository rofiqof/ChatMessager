package app.com.mychat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contactsManager";

    // Contacts table name
    private static final String TABLE_CHAT = "chat";
    public static final String TABLE_USER = "user";

    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_IMAGE = "image";

    // Contacts Table Columns names

    private static final String KEY_ID = "id";
    private static final String KEY_FILE = "file";
    private static final String KEY_MESSAGE = "messaeg";
    private static final String KEY_TYPE = "type";
    private static final String KEY_TIME = "time";
    private static final String KEY_FROMID = "fromId";
    private static final String KEY_TOID = "toId";
    private static final String KEY_MSGID = "msgId";
    private static final String KEY_VDO = "vdo";
    private static final String KEY_THUMB = "thumb";
    private static final String KEY_VD = "_vdo";
    private static final String KEY_AUDIO = "_audio";
    private static final String KEY_READ = "_read";



    public DatabaseHandler(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String CREATE_CHAT_TABLE = "CREATE TABLE " + TABLE_CHAT
                + "("

                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_MSGID + " TEXT,"
                + KEY_TIME + " TEXT,"
                + KEY_TYPE + " TEXT,"
                + KEY_FROMID + " TEXT,"
                + KEY_TOID + " TEXT,"
                + KEY_MESSAGE + " TEXT,"
                + KEY_FILE + " TEXT,"
                + KEY_VDO + " TEXT,"
                + KEY_THUMB + " TEXT,"
                + KEY_VD + " TEXT,"
                + KEY_AUDIO + " TEXT,"
                + KEY_READ + " TEXT"

                + ")";

        db.execSQL(CREATE_CHAT_TABLE);

        /*
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER
                + "("

                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_USER_ID + " TEXT,"
                + KEY_PHONE + " TEXT,"
                + KEY_IMAGE + " TEXT"

                + ")";

        db.execSQL(CREATE_USER_TABLE);
        */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        // Create tables again
        onCreate(db);
       /* db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT);
        // Create tables again
        onCreate(db);*/
    }

    public void addChat(ChatModel c)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_MSGID, c.get_msgId());
        values.put(KEY_TIME, c.get_time());
        values.put(KEY_TYPE, c.get_type());
        values.put(KEY_FROMID, c.get_fromId());
        values.put(KEY_TOID, c.get_toId());
        values.put(KEY_MESSAGE, c.get_messaeg());
        values.put(KEY_FILE, c.get_file());
        values.put(KEY_VDO, c.get_video());
        values.put(KEY_THUMB, c.get_thumb());
        values.put(KEY_VD, ""+c.getVdo());
        values.put(KEY_AUDIO, ""+c.get_audio());
        values.put(KEY_READ, ""+c.get_audio());
        db.insert(TABLE_CHAT, null, values);
        db.close();
    }

    /*  public void addUSER(ChatModel c)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_MSGID, c.get_msgId());
        values.put(KEY_TIME, c.get_time());
        values.put(KEY_TYPE, c.get_type());
        values.put(KEY_FROMID, c.get_fromId());
        values.put(KEY_TOID, c.get_toId());
        values.put(KEY_MESSAGE, c.get_messaeg());
        values.put(KEY_FILE, c.get_file());

        // Inserting Row

        db.insert(TABLE_CHAT, null, values);
        db.close();
    }
   */


    public List<ChatModel> getAllContacts()
    {
        List<ChatModel> contactList = new ArrayList<ChatModel>();
        Bitmap b = null;
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CHAT;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list

        if (cursor.moveToFirst())
        {
            do
            {
                ChatModel contact = new ChatModel();
                contact.set_msgId(cursor.getString(1));
                contact.set_time(cursor.getString(2));
                contact.set_type(cursor.getString(3));
                contact.set_fromId(cursor.getString(4));
                contact.set_toId(cursor.getString(5));
                contact.set_messaeg(cursor.getString(6));
                contact.set_file(cursor.getString(7));
                contact.set_video(cursor.getString(8));
                contact.set_thumb(cursor.getString(9));
                contact.setVdo(""+b);
                contact.set_audio(cursor.getString(11));
                contact.set_read(cursor.getString(12));
                contactList.add(contact);
            }
            while (cursor.moveToNext());
        }
        // return contact list
        return contactList;
    }

    public void delete()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+TABLE_CHAT);
        db.close();
    }

    public void delete_row(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println("the message delete id is :" + id);
        db.delete(TABLE_CHAT, KEY_MSGID+"='"+id+"'", null);
        db.close();
    }

    public boolean presentId(String _msgid) throws SQLException
    {
        int count = -1;
        Cursor c = null;
        SQLiteDatabase db = this.getWritableDatabase();
        try
        {
            String query = "SELECT COUNT(*) FROM " + TABLE_CHAT + " WHERE " + KEY_MSGID + " = ?";
            c = db.rawQuery(query, new String[] {_msgid});
            if (c.moveToFirst())
            {
                count = c.getInt(0);
            }
            return count > 0;
        }
        finally
        {
            if (c != null)
            {
                c.close();
            }
        }
    }

    public void updateREAD(String _msgid) throws SQLException
    {
        System.out.println("rr update is :" + _msgid);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_READ, "read");
        db.update(TABLE_CHAT, cv, KEY_MSGID+"='"+_msgid+"'", null);
        db.close();
    }

    /*
    public String last_Id()
    {
        String selectQuery = "SELECT  * FROM " + TABLE_CHAT;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToLast();
        System.out.println("the last message id :" +cursor.getString(1));
        String str =  cursor.getString(1);
        return str;
    }
    */
}
