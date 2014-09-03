package com.mengdd.hellowebview;

import java.util.Set;
import com.mengdd.download.DownloadManager;
import com.mengdd.download.OnDownloadChangedAdapter;
import com.mengdd.hellowebview.utils.BrowserUtils;
import com.mengdd.hellowebview.utils.DeviceUtils;
import com.mengdd.hellowebview.utils.LogUtil;
import com.mengdd.hellowebview.utils.MediaUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebStorage.QuotaUpdater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {
    private static final String LOG_TAG = "WebViewActivity";

    // webview framelayout
    private FrameLayout mWebViewRoot = null;
    // progress bar
    private ProgressBar mProgessBar = null;
    // webview
    private WebView mWebView = null;

    // address
    private ViewGroup mTitleLayout = null;
    private EditText mAddress = null;
    private Button mGoToAddressBtn = null;
    // toolbar
    @SuppressWarnings("unused")
    private View mToolbar = null;
    private View mGoBackBtn = null;
    private View mForwardBtn = null;
    private View mRefreshBtn = null;

    private static Context mContext = null;

    // for file upload
    private ValueCallback<Uri> mUploadMessage = null;
    private int FILECHOOSER_RESULTCODE = 0x1;

    // messages

    private static final int MSG_BASE = 100;
    private static final int MSG_SHOW_TOAST = MSG_BASE + 1;

    // handler
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {

            case MSG_SHOW_TOAST:
                LogUtil.i(LOG_TAG, "MSG_SHOW_TOAST");
                if (!isFinishing()) {
                    String toast = (String) msg.obj;
                    Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
                }

                break;
            default:
                break;
            }

        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        initWebView();
        initTitlebar();
        initToolbar();
        gotoAddress();

        processExtraData();


    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        LogUtil.i(LOG_TAG, "----- onNewIntent ---");
        setIntent(newIntent);
        Uri intentUri = getIntent().getData();
        mAddress.setText(intentUri.toString());
        gotoAddress();

        processExtraData();

    }

    private void processExtraData() {
        Intent intent = getIntent();
        String action = intent.getAction();
        Set<String> categories = intent.getCategories();
        if (action == null) {
            action = "";
        }

        // TODO deal with various ways of entering if needed

    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onDestroy() {

        mWebViewRoot.removeView(mWebView);
        mWebView.destroy();

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    private void initTitlebar() {

        mProgessBar = (ProgressBar) findViewById(R.id.progress_bar);
        mAddress = (EditText) findViewById(R.id.address);
        mAddress.setOnFocusChangeListener(new AddressFocusChange());
        mAddress.setOnEditorActionListener(new EditorAction());
        Uri intentUri = getIntent().getData();
        if (null != intentUri) {
            mAddress.setText(intentUri.toString());
        }
        else {
            mAddress.setText(Constants.DEFAULT_TEST_URL);
        }
        mGoToAddressBtn = (Button) findViewById(R.id.go_button);
        mGoToAddressBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                gotoAddress();
            }
        });

        mTitleLayout = (ViewGroup) findViewById(R.id.address_layout);

    }

    private void gotoAddress() {
        String addr = mAddress.getText().toString();
        StringBuffer fullAddr = new StringBuffer();
        if (!addr.equals("")) {
            if (addr.indexOf("http://") != -1 || addr.indexOf("https://") != -1) {
                fullAddr.append(addr);
            }
            else {
                fullAddr.append("http://");
                fullAddr.append(addr);
            }
            mAddress.setText(fullAddr.toString());
            mWebView.loadUrl(fullAddr.toString());
            mWebView.requestFocus();
        }
    }

    private class AddressFocusChange implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                mGoToAddressBtn.setVisibility(View.VISIBLE);
            }
            else {
                mGoToAddressBtn.setVisibility(View.GONE);
            }
        }

    }

    private class EditorAction implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            switch (actionId) {
            case EditorInfo.IME_ACTION_GO:
                gotoAddress();
                break;
            }
            return true;
        }

    }

    @SuppressLint("JavascriptInterface")
    private void initWebView() {
        mWebViewRoot = (FrameLayout) findViewById(R.id.webview_layout);
        mWebView = (WebView) findViewById(R.id.webview);

        // Settings
        WebSettings settings = mWebView.getSettings();
        settings.setDefaultTextEncodingName("GBK");
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        // settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // set page cache
        BrowserUtils.setWebViewPageCache(settings, 10);

        if (DeviceUtils.getSDKVersion() > 11) {
            settings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        }
        else {
            settings.setPluginState(WebSettings.PluginState.ON);
        }

        // set appcache and database
        settings.setDatabaseEnabled(true);
        settings.setDatabasePath(getApplicationContext().getDir("database",
                MODE_PRIVATE).getPath());
        settings.setAppCacheEnabled(true);
        settings.setAppCacheMaxSize(1024 * 1024 * 8);
        settings.setAppCachePath(getApplicationContext().getDir("app_cache",
                MODE_PRIVATE).getPath());

        // set cookie
        CookieSyncManager.createInstance(mContext);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        mWebView.requestFocus();

        // download in web
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                    String contentDisposition, String mimetype,
                    long contentLength) {

                LogUtil.d(url);

                if (MediaUtil.isVideoType(mimetype)) {
                    MediaUtil.playMedia(mContext, url, mimetype);
                    return;
                }

                final String type = mimetype;

                // TODO
                DownloadManager.getInstance().addDownloadTask(mContext, url,
                        new OnDownloadChangedAdapter() {
                        });

            }
        });
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view,
                    SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                LogUtil.d(url);
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                isHalfLoadNotified = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                mAddress.setText(url);

                updateToolbarButtons();
                loadShareJavascript(view);
                Log.d(LOG_TAG, "url   = " + url + " inject  = over ");

            }
        });

        mWebView.setWebChromeClient(new MyWebChromeClient());

    }

    private void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        mGoBackBtn = findViewById(R.id.go_back);
        mGoBackBtn.setOnClickListener(mToolbarItemClickListener);
        mForwardBtn = findViewById(R.id.forward);
        mForwardBtn.setOnClickListener(mToolbarItemClickListener);
        mRefreshBtn = findViewById(R.id.refresh);
        mRefreshBtn.setOnClickListener(mToolbarItemClickListener);

        updateToolbarButtons();

    }

    private OnClickListener mToolbarItemClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (R.id.go_back == id) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                }
                else {
                    finish();
                }
            }
            else if (R.id.forward == id) {
                if (mWebView.canGoForward()) {
                    mWebView.goForward();
                }
            }
            else if (R.id.refresh == id) {
                mWebView.reload();

            }

        }

    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (mWebView.canGoBack()) {
                // webview back
                mWebView.goBack();
                return true;
            }
            else {
                finish();
                return true;

            }

        }
        // If it wasn't the Back key or there's no web page history, bubble up
        // to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    private void updateToolbarButtons() {

        LogUtil.i(LOG_TAG, "update buttons");
        mGoBackBtn.setEnabled(true);
        mForwardBtn.setEnabled(mWebView.canGoForward());

    }

    private void loadShareJavascript(WebView view) {
        // TODO load javascript here
        // try {
        // Context applicationContext = mContext.getApplicationContext();
        // AssetManager assetManager = applicationContext.getResources()
        // .getAssets();

        // String js = new String(IOUtils.readStream(assetManager
        // .open("js/default_webview_js.js")));
        // view.loadUrl("javascript:" + js);
        // }
        // catch (IOException e) {
        // e.printStackTrace();
        // }

    }

    private boolean isHalfLoadNotified = false;

    /**
     * do something when the web page loaded half
     */
    private void onPageLoadedHalf() {

        // TODO

    }

    private class MyWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {

            if (!isHalfLoadNotified && newProgress > 50) {
                isHalfLoadNotified = true;
                LogUtil.i(LOG_TAG, "on page load half! -- " + newProgress);
                onPageLoadedHalf();
            }

            if (newProgress > 0 && newProgress < 100) {
                mProgessBar.setVisibility(View.VISIBLE);
                mProgessBar.setProgress(newProgress);
            }
            else if (newProgress == 100) {
                isHalfLoadNotified = false;
                mProgessBar.setVisibility(View.GONE);
                setProgress(0);
                // sync cookie to disk
                CookieSyncManager.getInstance().sync();
            }

        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                android.webkit.GeolocationPermissions.Callback callback) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            callback.invoke(origin, true, false);
        }

        // For Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                String acceptType) {
            if (mUploadMessage != null) {
                return;
            }
            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, mContext
                    .getResources()
                    .getString(R.string.webview_file_upload_hint)),
                    FILECHOOSER_RESULTCODE);
        }

        // For Android < 3.0
        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            openFileChooser(uploadMsg, "");
        }

        // For Android > 4.1.1
        @SuppressWarnings("unused")
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                String acceptType, String capture) {
            openFileChooser(uploadMsg, acceptType);
        }

        // for app cache exceed space
        @Override
        public void onReachedMaxAppCacheSize(long requiredStorage, long quota,
                QuotaUpdater quotaUpdater) {
            quotaUpdater.updateQuota(requiredStorage * 2);
        }

        // for websql exceed space
        @Override
        public void onExceededDatabaseQuota(String url,
                String databaseIdentifier, long quota,
                long estimatedDatabaseSize, long totalQuota,
                QuotaUpdater quotaUpdater) {
            quotaUpdater.updateQuota(estimatedDatabaseSize * 2);
        }
    }

}
