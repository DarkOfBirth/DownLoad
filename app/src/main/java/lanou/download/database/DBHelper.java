package lanou.download.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类,主要用来创建数据库
 * Created by dllo on 16/10/22.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME =  "download.db";
    private static final int VERSION = 1;
    private static final String SQL_CREATE = "create table thread_info(id integer primary" +
            "autoincrement,thread_id integer, url text, start integer,end integer, finished integer)";
    // 删除表的语法
    private static final String SQL_DROP ="drop table if exits thread_info";
    public DBHelper(Context context){
        super(context,DB_NAME,null,VERSION);
    };

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DROP);
    }
}
