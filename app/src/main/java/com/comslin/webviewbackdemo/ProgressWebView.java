package com.comslin.webviewbackdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class ProgressWebView extends LinearLayout {
    private static final String TAG = ProgressWebView.class.getName();

    public WebView mWebView;
    private ProgressBar mProgressBar;
    private LinearLayout refreshPageLayout;
    private OnTitleChangeListener onTitleChangeListener = null;
    private TextView tvErrorMsg;
    private Context mContext;
    private String hostUrl;
    private MySwipeRefreshLayout swipeRefresh;
    private ArrayList<String> titleList = new ArrayList<>();

    public ProgressWebView(Context context) {
        this(context, null);
    }

    public ProgressWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initView();
    }

    private void initView() {
        View.inflate(mContext, R.layout.view_web_progress, this);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mWebView = (WebView) findViewById(R.id.web_view);
        refreshPageLayout = (LinearLayout) findViewById(R.id.refresh_page_layout);
        tvErrorMsg = (TextView) findViewById(R.id.tv_error_msg);
        swipeRefresh = (MySwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        refreshPageLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPageLayout.setVisibility(INVISIBLE);
                titleList.clear();
                loadUrl(hostUrl);
            }
        });

        mWebView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return true;
            }
        });

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
            }
        });
    }

    public boolean canGoBack(){
        if(mWebView!=null){
            return mWebView.canGoBack();
        }
        return false;
    }
    public void goBack(){
        if(mWebView!=null){
            mWebView.goBack();
        }
    }
    public void loadUrl(String url) {
        if (checkNetworkState()) {
            url = checkWebViewUrl(url);
            if (!TextUtils.isEmpty(url)) {
                initWebview(url);
            } else {
                errorHandler();
            }
        } else {
            errorHandler();
        }
    }

    public void reload(String url) {
        if (mWebView != null) {
            url = checkWebViewUrl(url);
            if (TextUtils.isEmpty(url)) {
                mWebView.reload();
            } else {
                mWebView.loadUrl(url);
            }
        }
    }

    public void reload() {
        if (mWebView != null) {
            mWebView.reload();
        }
        if (swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void initWebView(Context context, WebView mWebView) {
        if (mWebView != null) {
            mWebView.setDrawingCacheBackgroundColor(0x00000000);
            mWebView.setFocusableInTouchMode(true);
            mWebView.setFocusable(true);
            mWebView.setAnimationCacheEnabled(false);
            mWebView.setDrawingCacheEnabled(true);
            mWebView.setBackgroundColor(context.getResources().getColor(
                    android.R.color.white));
            mWebView.getRootView().setBackgroundDrawable(null);
            mWebView.setWillNotCacheDrawing(false);
            mWebView.setAlwaysDrawnWithCacheEnabled(true);
            mWebView.setScrollbarFadingEnabled(true);
            mWebView.setHorizontalScrollBarEnabled(false);
            mWebView.setVerticalScrollBarEnabled(true);
            mWebView.setSaveEnabled(true);
            //硬件加速开启后e人e本某些平板pdf.js渲染不出来
//            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

            initializeSettings(mWebView.getSettings(), context);
        }
    }

    private static int API = Build.VERSION.SDK_INT;

    private void initializeSettings(WebSettings settings, Context context) {
        settings.setJavaScriptEnabled(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        settings.setUseWideViewPort(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
//        settings.setUserAgentString(settings.getUserAgentString() + DB.USER_AGENT);
        settings.setSupportMultipleWindows(true);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);//设置为true,系统会弹出AlertDialog确认框
        if (API < 18) {
            settings.setAppCacheMaxSize(Long.MAX_VALUE);
        }
        if (API < 17) {
            settings.setEnableSmoothTransition(true);
        }
        if (API < 19) {
            settings.setDatabasePath(context.getFilesDir().getAbsolutePath()
                    + "/databases");
        }
        settings.setDomStorageEnabled(true);
        settings.setAppCachePath(context.getCacheDir().toString());
        settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setGeolocationDatabasePath(context.getCacheDir()
                .getAbsolutePath());
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowContentAccess(true);
        settings.setDefaultTextEncodingName("utf-8");
        /*if (API > 16) {
            settings.setAllowFileAccessFromFileURLs(false);
			settings.setAllowUniversalAccessFromFileURLs(false);
		}*/
    }

    private void initWebview(String url) {
        refreshPageLayout.setVisibility(INVISIBLE);
        mWebView.setVisibility(VISIBLE);
        initWebView(mContext, mWebView);
        mWebView.loadUrl(url);

        // 设置WebViewClient
        mWebView.setWebViewClient(new WebViewClient() {
            // url拦截
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                hostUrl = url;
                if (checkNetworkState()) {
                    return false;
                } else {
                    errorHandler();
                    return true;
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.d(TAG, errorCode + ":" + description);
                //网络错误
                if (errorCode == -2) {
                    errorHandler();
                }
                //不支持协议?
                if (errorCode == -10) {
                }
                //无法连接到该网页
                if (errorCode == -6) {
                    errorHandler();
                }
            }

            //Https设置
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            // 页面开始加载
            @Override
            public void onPageStarted(final WebView view, String url, Bitmap favicon) {
                Log.e(TAG, "onPageStarted: " + url);
                removeLastTitle();
                mProgressBar.setVisibility(VISIBLE);
                mProgressBar.setProgress(10);
                super.onPageStarted(view, url, favicon);
            }

            // 页面加载完成
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e(TAG, "onPageFinished ");
                mProgressBar.setProgress(0);
                mProgressBar.setVisibility(GONE);
                super.onPageFinished(view, url);
            }

            // WebView加载的所有资源url
            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }
        });

        // 设置WebChromeClient
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void getVisitedHistory(ValueCallback<String[]> callback) {
                super.getVisitedHistory(callback);
            }

            @Override
            // 处理javascript中的alert
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            // 处理javascript中的confirm
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            // 处理javascript中的prompt
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            // 设置网页加载的进度条
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress > 10) {
                    mProgressBar.setVisibility(VISIBLE);
                    mProgressBar.setProgress(newProgress);
                    if (newProgress == 100) {
                        mProgressBar.setProgress(0);
                        mProgressBar.setVisibility(GONE);
                    }
                }
                super.onProgressChanged(view, newProgress);
            }

            // 设置程序的Title
            @Override
            public void onReceivedTitle(WebView view, String title) {
                addTitle(title);
                super.onReceivedTitle(view, title);
            }
        });
        mWebView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        return backHandler(); // 后退
                    }
                }
                return false;
            }
        });
    }

    //检查网络状态
    public boolean checkNetworkState() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return info != null && info.isAvailable();
    }

    //检测网址
    public String checkWebViewUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        if (!url.startsWith("http:") && !url.startsWith("https:") && !url.startsWith("javascript:") && !url.startsWith("file:")) {
            return "http://" + url;
        }
        return url;
    }

    //后退方法 --- 外部可调用
    public boolean backHandler() {
        if (mWebView.canGoBack()) {
            removeLastTitle();
//            mWebView.goBack();
            Log.d(TAG, titleList.toString());
            return false;
        } else {
            titleList.clear();
            titleList.add(mWebView.getTitle());
            Activity activity = (Activity) mContext;
//            activity.finish();
//            Toast.makeText(mContext, "这是初始页面!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    //错误信息设置
    private void errorHandler() {
        addTitle("错误页面");
        mProgressBar.setProgress(0);
        mWebView.setVisibility(INVISIBLE);
        refreshPageLayout.setVisibility(VISIBLE);
    }

    private void addTitle(String title) {
        if (onTitleChangeListener != null) {
            onTitleChangeListener.titleChange(title);

        }
        titleList.add(title);
    }

    private void removeLastTitle() {
        if (titleList.size() != 0) {
            titleList.remove(titleList.size() - 1);
        }
    }

    public void setOnTitleChangeListener(OnTitleChangeListener onTitleChangeListener) {
        this.onTitleChangeListener = onTitleChangeListener;
    }

    public interface OnTitleChangeListener {
        void titleChange(String s);
    }
}
