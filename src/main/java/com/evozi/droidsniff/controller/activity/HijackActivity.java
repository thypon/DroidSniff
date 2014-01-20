/*
 * HijackActivity.java is the WebView Activity setting up the cookies Copyright
 * (C) 2011 Andreas Koch <koch.trier@gmail.com>
 * 
 * This software was supported by the University of Trier
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.evozi.droidsniff.controller.activity;

import android.content.Context;
import android.content.Intent;
import butterknife.ButterKnife;
import butterknife.InjectView;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.http.cookie.Cookie;

import com.actionbarsherlock.app.SherlockActivity;
import com.evozi.droidsniff.model.auth.Auth;
import com.evozi.droidsniff.model.Session;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.view.MenuInflater;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;
import com.evozi.droidsniff.R;

@CommonsLog
public final class HijackActivity extends SherlockActivity {
    @InjectView(R.id.webviewhijack)
	WebView webview;

    @Getter(lazy = true)
	private final Args args = BundleConverter.getArgs(this.getIntent().getExtras());

    @Value(staticConstructor = "of")
    public static class Args {
        @NonNull Auth auth;
        @NonNull Boolean mobile;
    }

    public static void start(Context context, Args args) {
        Bundle bundle = BundleConverter.getBundle(args);

        Intent intent = new Intent(context, HijackActivity.class);
        intent.putExtras(bundle);

        context.startActivity(intent);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.webview);
        ButterKnife.inject(this);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setSupportProgressBarIndeterminateVisibility(false);

		CookieSyncManager.createInstance(this);
	}


    @Override
    protected void onStart() {
        super.onStart();

        if (getArgs().getAuth() == null) {
            Toast.makeText(this,
                    R.string.authentication_error,
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String url = getArgs().getMobile() ?
                getArgs().getAuth().getMobileUrl() :
                getArgs().getAuth().getUrl();

        setupWebView();
        setupCookies();

        webview.loadUrl(url);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
			webview.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.webview_menu, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case R.id.webview_menu_back:
			if (webview.canGoBack())
				webview.goBack();
			break;
		case R.id.webview_menu_forward:
			if (webview.canGoForward())
				webview.goForward();
			break;
		case R.id.webview_menu_refresh:
			webview.reload();
			break;
		case R.id.webview_menu_changeurl:
			selectURL();
			break;
		}
		return false;
	}

    private final static class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private final static class BundleConverter {
        private static String AUTH = "AUTH";
        private static String MOBILE = "MOBILE";

        public static Args getArgs(Bundle bundle) {
            return Args.of(
                    (Auth) bundle.getSerializable(AUTH),
                    bundle.getBoolean(MOBILE, false));
        }

        public static Bundle getBundle(Args args) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(AUTH, args.getAuth());
            bundle.putBoolean(MOBILE, args.getMobile());
            return bundle;
        }
    }

    private void selectURL() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.changeurl));
        alert.setMessage(getString(R.string.customurl));

        // Set an EditText view to get user input
        final EditText inputName = new EditText(this);
        inputName.setText(HijackActivity.this.webview.getUrl());
        alert.setView(inputName);

        alert.setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                HijackActivity.this.webview.loadUrl(inputName.getText()
                        .toString());
            }
        });

        alert.show();
    }

    private void setupCookies() {
        log.info("######################## COOKIE SETUP ###############################");
        CookieManager manager = CookieManager.getInstance();
        log.info("Cookiemanager has cookies: " + (manager.hasCookies() ? "YES" : "NO"));
        if (manager.hasCookies()) {
            manager.removeAllCookie();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            log.info("Cookiemanager has still cookies: " + (manager.hasCookies() ? "YES" : "NO"));
        }
        log.info("######################## COOKIE SETUP START ###############################");
        for (Session session : getArgs().getAuth().getSessions()) {
            Cookie cookie = session.getCookie();
            String cookieString = cookie.getName() + "=" + cookie.getValue()
                    + "; domain=" + cookie.getDomain() + "; Path="
                    + cookie.getPath();
            log.info("Setting up cookie: " + cookieString);
            manager.setCookie(cookie.getDomain(), cookieString);
        }
        CookieSyncManager.getInstance().sync();
        log.info("######################## COOKIE SETUP DONE ###############################");
    }

    private void setupWebView() {
        webview.setWebViewClient(new MyWebViewClient());
        WebSettings webSettings = webview.getSettings();

        webSettings
                .setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(false);
        webSettings.setBuiltInZoomControls(true);
        webview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                getSupportActionBar().setSubtitle(
                        HijackActivity.this.webview.getUrl());
                setSupportProgressBarIndeterminateVisibility(true);

                // Normalize our progress along the progress bar's scale
                int mmprogress = (Window.PROGRESS_END - Window.PROGRESS_START)
                        / 100 * progress;
                setSupportProgress(mmprogress);

                if (progress == 100) {
                    setSupportProgressBarIndeterminateVisibility(false);
                }

            }
        });
    }
}

