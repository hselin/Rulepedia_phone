package edu.stanford.braincat.rulepedia.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Timer;
import java.util.TimerTask;

import edu.stanford.braincat.rulepedia.R;

public class BrowseFragment extends Fragment {
    public static final String LOG_TAG = "rulepedia.UI.Install";

    private WebView mWebView;
    private Timer autoUpdate;

    public static BrowseFragment newInstance() {
        BrowseFragment fragment = new BrowseFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public BrowseFragment() {
        // Required empty public constructor
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_browse, container, false);

        mWebView = (WebView) v.findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new WebAppInterface(), "Android");
        mWebView.loadUrl("https://vast-hamlet-6003.herokuapp.com/browse");

        return v;
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void installRule(String json) {
            RuleInstaller.installRule((MainActivity)getActivity(), json);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mWebView.loadUrl("javascript:" +
                                "try { pollRules(false);} catch (e) {}");
                        //updateHTML();
                        //loadRules(getActivity().getApplicationContext());
                    }
                });
            }
        }, 0, 5000); // updates each 40 secs
    }

    @Override
    public void onPause() {
        autoUpdate.cancel();
        super.onPause();
    }
}
