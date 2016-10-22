package lanou.download.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import lanou.download.entities.FileInfo;

/**
 * Created by dllo on 16/10/22.
 */
public class DownloadService extends Service {

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/downloads";
    public static final int MSG_INIT = 0;
    private DownloadTask mDownloadTask = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(ACTION_START.equals(intent.getAction())){
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            // 启动初始化线程

            new InitThread(fileInfo).start();


        return super.onStartCommand(intent, flags, startId);
        } else if(ACTION_STOP.equals(intent.getAction())){
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.d("DownloadService", "stop:" + fileInfo.toString());
            if(mDownloadTask!= null) {
                mDownloadTask.isPause = true;
            }
        }
        return super.onStartCommand(intent,flags,startId);
    }
    // handler
    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.d("DownloadService", fileInfo.toString());
                    // 文件信息是不变的.
                    mDownloadTask = new DownloadTask(DownloadService.this,fileInfo);
                    mDownloadTask.download();
                    // 启动下载任务
                    break;
            }
            return false;
        }
    });
    /**
     * 初始化子线程
     */
    class InitThread extends Thread{
        private FileInfo mFileInfo = null;
        private RandomAccessFile raf;
        private HttpURLConnection connection;

        public InitThread(FileInfo mFileInfo) {
            this.mFileInfo = mFileInfo;
        }

        @Override
        public void run() {
            try {
                // 链接网络文件, 获得文件长度, 在本地创建文件, 设置文件长度
                URL url = new URL(mFileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                int length = -1;
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
//                    获得文件长度,
                    length = connection.getContentLength();
                    Log.d("InitThread", "length:" + length);
                }
                if(length <= 0){
                    return;
                }
                // 在本地创建文件,
                File dir = new File(DOWNLOAD_PATH);
                Log.d("InitThread", "dir.exists():" + dir.exists());
                if(!dir.exists()){

                    dir.mkdir();
                }
                // 创建本地文件
                File file = new File(dir,mFileInfo.getFileName());
                Log.d("InitThread", "file:" + file);
                // 随机的输出流
                raf = new RandomAccessFile(file,"rwd");
                Log.d("InitThread", "raf:" + raf);
                //设置文件长度
                raf.setLength(length);
                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT,mFileInfo).sendToTarget();



            }catch (Exception e){
                e.printStackTrace();
            } finally {
                connection.disconnect();
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
