package com.comslin.webviewbackdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class MainActivity extends BaseActivity {

    private ProgressWebView mWeb;
    //    private String url = "http://baidu.com";
    private String url = "http://m.hgzz22.com/?qr=http://wap.hgzz333.com/index/appdownload#module/common/action/home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mWeb.loadUrl(url);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.webview, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            mWeb.reload(url);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mWeb = (ProgressWebView) findViewById(R.id.web);
    }

    private long exitTime = 0;

    private void back() {
        try {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                moveTaskToBack(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //返回键，将activity 退到后台，不被销毁
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWeb.canGoBack()) {
                mWeb.goBack();
            } else {
                back();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mWeb.loadUrl("www.baidu.com");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
