package lanou.download.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import lanou.download.entities.ThreadInfo;

/**
 * 数据访问接口的实现
 * Created by dllo on 16/10/22.
 */

public class ThreadDaoImpl implements ThreadDAO {

    private DBHelper mHelper = null;

    public ThreadDaoImpl(Context mContext) {
        mHelper = new DBHelper(mContext);

    }

    @Override
    public void insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("thread_id", threadInfo.getId());
        values.put("url", threadInfo.getUrl());
        values.put("start", threadInfo.getStart());
        values.put("end", threadInfo.getEnd());
        values.put("finished", threadInfo.getFinished());
        db.insert("thread_info", null, values);
        // 数据库使用后关闭
        db.close();

    }

    @Override
    public void deleteThread(String url, int thread_id) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ? and thread_id = ?",
                new Object[]{url, thread_id});

        // 数据库使用后关闭
        db.close();
    }

    @Override
    public void updateThread(String url, int thread_id, int finished) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("update  thread_info set finished = ? where url = ? and " +
                "thread_id = ?", new Object[]{finished, url, thread_id});

        // 数据库使用后关闭
        db.close();
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        List<ThreadInfo> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ?", new String[]{url});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ThreadInfo threadInfo = new ThreadInfo();
                threadInfo.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
                threadInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                threadInfo.setStart(cursor.getInt(cursor.getColumnIndex("start")));
                threadInfo.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
                threadInfo.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
                list.add(threadInfo);
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    @Override
    public boolean isExist(String url, int thread_id) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?",
                new String[]{url,thread_id + ""});
        boolean exists = cursor.moveToFirst();
        db.close();
        return exists;
    }
}
