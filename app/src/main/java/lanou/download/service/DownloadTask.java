package lanou.download.service;

import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import lanou.download.database.ThreadDAO;
import lanou.download.database.ThreadDaoImpl;
import lanou.download.entities.FileInfo;
import lanou.download.entities.ThreadInfo;

/**
 * Created by dllo on 16/10/22.
 * 下载任务类
 */
public class DownloadTask {
    private Context mContext = null;
    private FileInfo mFileInfo = null;
    private ThreadDAO mDao = null;
    private int mFinished = 0;
    public boolean isPause = false;

    public DownloadTask(Context mContext, FileInfo mFileInfo) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        mDao = new ThreadDaoImpl(mContext);
    }
    public void download(){
        // 读取数据库的线程信息
       List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if(threadInfos.size() == 0){
            // 初始化线程信息对象,
            threadInfo = new ThreadInfo(0,mFileInfo.getUrl(),0,mFileInfo.getLength(),0);
        }else {
            threadInfo = threadInfos.get(0);
        }
        // 创建子线程进行下载
        new DownloadThread(threadInfo).start();
    }
    /**
     * 下载线程
     */
    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo = null;
        private HttpURLConnection connection;
        private RandomAccessFile raf;
        private InputStream input;

        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        @Override
        public void run() {
            // 向数据库插入线程的信息
            if (!mDao.isExist(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDao.insertThread(mThreadInfo);
            }

            try {
                URL url = new URL(mThreadInfo.getUrl());

                 connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                // 设置下载位置, 即开始位置
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();

                connection.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
                // 设置写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                mFinished += mThreadInfo.getFinished();
                // 开始下载
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    // 读取数据
                    input = connection.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    Long time = System.currentTimeMillis();
                    while ((len = input.read(buffer)) != -1) {
                        // 写入文件
                        raf.write(buffer, 0, len);
                        mFinished += len;
                        if (System.currentTimeMillis() - time > 500) {
                            // 把下载进度发送广播给Activity
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            mContext.sendBroadcast(intent);
                        }
                        if (isPause) {
                            mDao.updateThread(mThreadInfo.getUrl(),mThreadInfo.getId(),mFinished);
                            return;
                        }
                    }

                    mDao.deleteThread(mThreadInfo.getUrl(),mThreadInfo.getId());
                }


                // 下载暂停时保存进度
            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {


                try {
                    connection.disconnect();
                    input.close();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        }
    }
}
