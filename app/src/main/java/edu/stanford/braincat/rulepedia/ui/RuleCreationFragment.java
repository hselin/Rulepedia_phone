package edu.stanford.braincat.rulepedia.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import edu.stanford.braincat.rulepedia.R;


public class RuleCreationFragment extends Fragment {
    private WebView mWebView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RuleCreationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RuleCreationFragment newInstance() {
        RuleCreationFragment fragment = new RuleCreationFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public RuleCreationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_rule_creation, container, false);

        mWebView = (WebView) v.findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new WebAppInterface(), "Android");
        mWebView.loadUrl("https://vast-hamlet-6003.herokuapp.com/create");

        return v;
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void noop() {
        }
    }

}
