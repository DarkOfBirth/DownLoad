package lanou.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import lanou.download.entities.FileInfo;
import lanou.download.service.DownloadService;

public class MainActivity extends AppCompatActivity {
    private TextView mTvFileName;
    private ProgressBar mPbProgress;
    private Button mBtnStart;
    private Button mBtnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvFileName = (TextView) findViewById(R.id.tv_filename);
        mPbProgress = (ProgressBar) findViewById(R.id.pbProgress);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mPbProgress.setMax(100);

        // register
        IntentFilter filter = new IntentFilter(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);

        String url = "http://www.imooc.com/mobile/mukewang.apk";

        // 创建一个文件信息对象
        final FileInfo fileInfo = new FileInfo(0, url,
                "mukewang.apk", 0, 0);
        // 添加事件点击
        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTvFileName.setText("mukewang.apk");
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.putExtra("fileInfo", fileInfo);
                intent.setAction(DownloadService.ACTION_START);
                startService(intent);
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.putExtra("fileInfo", fileInfo);
                intent.setAction(DownloadService.ACTION_STOP);
                startService(intent);
            }
        });
    }

    /**
     * UI
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                int finished = intent.getIntExtra("finished", 0);
                mPbProgress.setProgress(finished);
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
