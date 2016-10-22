package lanou.download.database;

import java.util.List;

import lanou.download.entities.ThreadInfo;

/**
 * 数据访问接口
 * Created by dllo on 16/10/22.
 */
public interface ThreadDAO  {
    /**
     * 插入线程信息
     * @param threadInfo
     */
    public void insertThread(ThreadInfo threadInfo);

    /**
     * 删除线程
     * @param url
     * @param thread_id
     */
    public void deleteThread(String url, int thread_id);

    /**
     * 更新线程进度
     * @param url
     * @param thread_id
     * @param finished
     */
    public void updateThread(String url, int thread_id,int finished);

    /**
     * 查询文件所有的线程信息
     * @param url
     * @return
     */
    public List<ThreadInfo> getThreads(String url);

    /**
     * 判断线程信息是否存在
     * @param url
     * @param thread_id
     * @return
     */
    public boolean isExist(String url, int thread_id);

}
