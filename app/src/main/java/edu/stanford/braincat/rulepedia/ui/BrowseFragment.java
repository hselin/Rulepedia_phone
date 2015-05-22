package edu.stanford.braincat.rulepedia.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import edu.stanford.braincat.rulepedia.R;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.CompositeTrigger;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.service.Callback;
import edu.stanford.braincat.rulepedia.service.RuleExecutor;

public class BrowseFragment extends Fragment {
    public static final String LOG_TAG = "rulepedia.UI.Install";

    private WebView mWebView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BrowseFragment.
     */
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

    /*
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) { if(mWebView != null) mWebView.reload(); }
        else {  }
    }
    */

    private void reportInstallationSuccess() {
        ((MainActivity)getActivity()).onRuleInstalled();
    }

    private void reportInstallationError(Exception error) {
        ((MainActivity)getActivity()).onRuleInstallationError(error);
    }

    private void sendIntentToRuleEngine(String ruleJSON) {
        RuleExecutor executor = ((MainActivity) getActivity()).getRuleExecutor();
        if (executor == null)
            return;

        try {
            JSONObject jsonRule = (JSONObject) new JSONTokener(ruleJSON).nextValue();

            executor.installRule(jsonRule, new Callback<Rule>() {
                private void getChannels(Trigger trigger, Collection<Channel> ctx) {
                    if (trigger instanceof CompositeTrigger) {
                        for (Trigger t : ((CompositeTrigger) trigger).getChildren())
                            getChannels(t, ctx);
                    } else {
                        ctx.add(trigger.getChannel());
                    }
                }

                @Override
                public void run(final Rule result, final Exception error) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            if (result != null) {
                                reportInstallationSuccess();

                                /*
                                Collection<ObjectPool.Object> placeholders =  result.getPlaceholders();

                                Collection<Channel> channels = new ArrayList<>();
                                getChannels(result.getTrigger(), channels);
                                for (Action a : result.getActions())
                                    channels.add(a.getChannel());

                                for (ObjectPool.Object placeholder : placeholders) {
                                    placeholder.toHumanString();

                                    //placeholder.getUrl() //key to store



                                }
                                */
                            } else {
                                reportInstallationError(error);
                            }
                        }
                    });
                }
            });
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to parse rule JSON: " + e.getMessage());
            reportInstallationError(e);
        }
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void installRule(String ruleJSON) {
            sendIntentToRuleEngine(ruleJSON);
        }
    }

    private Timer autoUpdate;

    @Override
    public void onResume() {
        super.onResume();
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        mWebView.loadUrl("javascript:pollRules(false);");
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
