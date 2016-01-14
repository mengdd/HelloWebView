package com.mengdd.hellowebview;

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
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mengdd.download.DownloadManager;
import com.mengdd.download.OnDownloadChangedAdapter;
import com.mengdd.hellowebview.utils.BrowserUtils;
import com.mengdd.hellowebview.utils.DeviceUtils;
import com.mengdd.hellowebview.utils.LogUtil;
import com.mengdd.hellowebview.utils.MediaUtil;

import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {
    private static final String LOG_TAG = "WebViewActivity";

    @Bind(R.id.webview_layout)
    FrameLayout webViewRoot;
    @Bind(R.id.progress_bar)
    ProgressBar progressBar;
    @Bind(R.id.webview)
    WebView webView;

    @Bind(R.id.address)
    EditText addressEditText;
    @Bind(R.id.go_button)
    Button goToAddressButton;
    @Bind(R.id.go_back)
    View goBackButton;
    @Bind(R.id.forward)
    View forwardButton;

    private static Context mContext = null;

    // for file upload
    private ValueCallback<Uri> mUploadMessage = null;
    private int FILE_CHOOSER_RESULT_CODE = 0x1;

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

        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initWebView();

        Uri intentUri = getIntent().getData();
        addressEditText.setText(intentUri != null ? intentUri.toString() : Constants.DEFAULT_TEST_URL);

        updateToolbarButtons();

        gotoAddress();

        processExtraData();

    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        LogUtil.i(LOG_TAG, "----- onNewIntent ---");
        setIntent(newIntent);
        Uri intentUri = getIntent().getData();
        if (intentUri != null) {
            addressEditText.setText(intentUri.toString());
            gotoAddress();
        }

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
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {

        webViewRoot.removeView(webView);
        webView.destroy();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == mUploadMessage) {
                return;
            }
            Uri result = intent == null || resultCode != RESULT_OK ? null
                    : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    @OnFocusChange(R.id.address)
    void onAddressFocusChanged(View v, boolean hasFocus) {
        goToAddressButton.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
    }

    @OnEditorAction(R.id.address)
    boolean onAddressEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (actionId) {
            case EditorInfo.IME_ACTION_GO:
                gotoAddress();
                break;
        }
        return true;
    }

    @OnClick(R.id.go_button)
    void gotoAddress() {
        String address = addressEditText.getText().toString();
        StringBuffer fullAddr = new StringBuffer();
        if (!address.equals("")) {
            if (address.indexOf("http://") != -1 || address.indexOf("https://") != -1) {
                fullAddr.append(address);
            } else {
                fullAddr.append("http://");
                fullAddr.append(address);
            }
            addressEditText.setText(fullAddr.toString());
            webView.loadUrl(fullAddr.toString());
            webView.requestFocus();
        }
    }

    @SuppressLint("JavascriptInterface")
    private void initWebView() {

        // Settings
        WebSettings settings = webView.getSettings();
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
        } else {
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

        webView.requestFocus();

        // download in web
        webView.setDownloadListener(new DownloadListener() {
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
        webView.setWebViewClient(new WebViewClient() {

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

                addressEditText.setText(url);

                updateToolbarButtons();
                loadShareJavascript(view);
                Log.d(LOG_TAG, "url   = " + url + " inject  = over ");

            }
        });

        webView.setWebChromeClient(new MyWebChromeClient());

    }

    @OnClick(R.id.go_back)
    void goBack() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    @OnClick(R.id.forward)
    void goForward() {
        if (webView.canGoForward()) {
            webView.goForward();
        }
    }

    @OnClick(R.id.refresh)
    void refresh() {
        webView.reload();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
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
        goBackButton.setEnabled(true);
        forwardButton.setEnabled(webView.canGoForward());

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
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
            } else if (newProgress == 100) {
                isHalfLoadNotified = false;
                progressBar.setVisibility(View.GONE);
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
                    FILE_CHOOSER_RESULT_CODE);
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
