package edu.stanford.braincat.rulepedia.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Collection;

import edu.stanford.braincat.rulepedia.R;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.CompositeTrigger;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.service.Callback;
import edu.stanford.braincat.rulepedia.service.RuleExecutor;

public class BrowseFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String LOG_TAG = "rulepedia.UI.Install";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private WebView mWebView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BrowseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BrowseFragment newInstance(String param1, String param2) {
        BrowseFragment fragment = new BrowseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public BrowseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_browse, container, false);

        mWebView = (WebView) v.findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new WebAppInterface(), "Android");
        mWebView.loadUrl("https://vast-hamlet-6003.herokuapp.com/create");

        return v;
    }

    private void reportInstallationSuccess(Rule rule) {
        mWebView.evaluateJavascript("Rulepedia.Android.installationSuccess();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                // nothing to do
            }
        });
    }

    private void reportInstallationError(Exception error) {
        mWebView.evaluateJavascript("Rulepedia.Android.installationError('" + error.getMessage().replace("'", "\\'") + "');",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                    // nothing to do
                    }
                });
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
                                reportInstallationSuccess(result);

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

                                getActivity().startActivityForResult(new Intent(getActivity(), GoogleFitAuthActivity.class), 0);
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

    public class WebAppInterface {
        @JavascriptInterface
        public void installRule(String ruleJSON) {
            sendIntentToRuleEngine(ruleJSON);
        }
    }
}
