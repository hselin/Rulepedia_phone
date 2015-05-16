package edu.stanford.braincat.rulepedia.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Collection;

import edu.stanford.braincat.rulepedia.R;
import edu.stanford.braincat.rulepedia.model.Action;
import edu.stanford.braincat.rulepedia.model.Channel;
import edu.stanford.braincat.rulepedia.model.CompositeTrigger;
import edu.stanford.braincat.rulepedia.model.ObjectPool;
import edu.stanford.braincat.rulepedia.model.Rule;
import edu.stanford.braincat.rulepedia.model.RuleDatabase;
import edu.stanford.braincat.rulepedia.model.Trigger;
import edu.stanford.braincat.rulepedia.service.Callback;
import edu.stanford.braincat.rulepedia.service.RuleExecutor;

import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.plus.Plus;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BrowseFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BrowseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class BrowseFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String LOG_TAG = "rulepedia.UI.Install";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void reportInstallationSuccess(Rule rule) {
        /*
        mWebView.evaluateJavascript("Rulepedia.Android.installationSuccess();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                // nothing to do
            }
        });
        */
    }

    private void reportInstallationError(Exception error) {
        /*
        mWebView.evaluateJavascript("Rulepedia.Android.installationError('" + error.getMessage().replace("'", "\\'") + "');",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                    // nothing to do
                    }
                });
         */
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
                public void run(final Rule result, Exception error) {
                    if (result != null) {
                        reportInstallationSuccess(result);

                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {

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

                                GFitness gf = new GFitness(getActivity());
                                //GFitness_OLD gfo = new GFitness_OLD(getActivity(), getActivity().getApplicationContext());
                            }
                        });



                    }
                    else {
                        reportInstallationError(error);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to parse rule JSON: " + e.getMessage());
            reportInstallationError(e);
        }
    }


    public class GFitness extends FragmentActivity
            implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnDataPointListener {
        private static final int REQUEST_OAUTH = 1001;
        private GoogleApiClient mGoogleApiClient;
        private Activity activity;

        public GFitness(Activity activity){
            this.activity = activity;
            create();
        }

        public void create()
        {
            mGoogleApiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
                    .addApi(Fitness.SENSORS_API)  // Required for SensorsApi calls
                            // Optional: specify more APIs used with additional calls to addApi
                    .useDefaultAccount()
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mGoogleApiClient.connect();
        }

        @Override
        public void onConnected(Bundle connectionHint) {
            // Connected to Google Fit Client.
            Log.d("!!!!!!!!!!!!!!!!", "CONNECTED TO GOOGLE FIT!!!!!");

            Fitness.SensorsApi.add(
                    mGoogleApiClient,
                    new SensorRequest.Builder()
                            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                            .build(),
                    this);
        }




        @Override
        public void onDataPoint(DataPoint dataPoint) {
            // Do cool stuff that matters.
        }

        @Override
        public void onConnectionSuspended(int cause) {
            // The connection has been interrupted. Wait until onConnected() is called.
        }

        /*
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            // Error while connecting. Try to resolve using the pending intent returned.
            Log.d("!!!!!!!!!!!!!!!!", "ERROR connect " + result.getErrorCode());

            if (result.getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED) {
                try {
                    result.startResolutionForResult(activity, ConnectionResult.SIGN_IN_REQUIRED);
                } catch (IntentSender.SendIntentException e) {
                    Log.e("1", "1", e);
                }
            }


            if (result.getErrorCode() == FitnessStatusCodes.NEEDS_OAUTH_PERMISSIONS) {
                try {
                    result.startResolutionForResult(activity, REQUEST_OAUTH);
                } catch (IntentSender.SendIntentException e) {
                    Log.e("1", "1", e);
                }
            }
        }
        */


        // Request code to use when launching the resolution activity
        private static final int REQUEST_RESOLVE_ERROR = 1001;
        // Unique tag for the error dialog fragment
        private static final String DIALOG_ERROR = "dialog_error";
        // Bool to track whether the app is already resolving an error
        private boolean mResolvingError = false;


        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.d("!!!!", "ERROR" + result.getErrorCode());

            if (mResolvingError) {
                // Already attempting to resolve an error.
                return;
            } else if (result.hasResolution()) {
                try {
                    mResolvingError = true;
                    result.startResolutionForResult(activity, REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    // There was an error with the resolution intent. Try again.
                    mGoogleApiClient.connect();
                }
            } else {
                // Show dialog using GooglePlayServicesUtil.getErrorDialog()
                showErrorDialog(result.getErrorCode());
                mResolvingError = true;
            }
        }

        // The rest of this code is all about building the error dialog

        /* Creates a dialog for an error message */
        private void showErrorDialog(int errorCode) {
            // Create a fragment for the error dialog
            ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
            // Pass the error that should be displayed
            Bundle args = new Bundle();
            args.putInt(DIALOG_ERROR, errorCode);
            dialogFragment.setArguments(args);
            dialogFragment.show(getSupportFragmentManager(), "errordialog");
        }

        /* Called from ErrorDialogFragment when the dialog is dismissed. */
        public void onDialogDismissed() {
            mResolvingError = false;
        }

        /* A fragment to display an error dialog */
        public class ErrorDialogFragment extends DialogFragment {
            public ErrorDialogFragment() { }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                // Get the error code and retrieve the appropriate dialog
                int errorCode = this.getArguments().getInt(DIALOG_ERROR);
                return GooglePlayServicesUtil.getErrorDialog(errorCode,
                        this.getActivity(), REQUEST_RESOLVE_ERROR);
            }

            /*
            @Override
            public void onDismiss(DialogInterface dialog) {
                //(activity).onDialogDismissed();
            }
            */
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == REQUEST_RESOLVE_ERROR) {
                mResolvingError = false;
                if (resultCode == RESULT_OK) {
                    // Make sure the app is not already connected or attempting to connect
                    if (!mGoogleApiClient.isConnecting() &&
                            !mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.connect();
                    }
                }
            }
        }
    }




    public class GFitness_OLD {
        private static final int REQUEST_OAUTH = 1;
        private Context ctx;
        private Activity activity;

        public GFitness_OLD(Activity activity, Context ctx){
            /*
            if (savedInstanceState != null) {
                authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
            }
            */

            this.activity = activity;
            this.ctx = ctx;

            buildFitnessClient();
        }

        /**
         *  Track whether an authorization activity is stacking over the current activity, i.e. when
         *  a known auth error is being resolved, such as showing the account chooser or presenting a
         *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
         */
        private static final String AUTH_PENDING = "auth_state_pending";
        private boolean authInProgress = false;

        private GoogleApiClient mClient = null;




        /**
         *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
         *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
         *  (see documentation for details). Authentication will occasionally fail intentionally,
         *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
         *  can address. Examples of this include the user never having signed in before, or having
         *  multiple accounts on the device and needing to specify which account to use, etc.
         */
        private void buildFitnessClient() {
            // Create the Google API Client
            mClient = new GoogleApiClient.Builder(ctx)
                    .addApi(Fitness.SENSORS_API)
                    .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {

                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.d("!", "Connected!!!");
                                    // Now you can make calls to the Fitness APIs.
                                    // Put application specific code here.
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.d("!", "Connection lost.  Cause: Network Lost.");
                                    } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.d("!", "Connection lost.  Reason: Service Disconnected");
                                    }
                                }
                            }
                    )
                    .addOnConnectionFailedListener(
                            new GoogleApiClient.OnConnectionFailedListener() {
                                // Called whenever the API client fails to connect.
                                @Override
                                public void onConnectionFailed(ConnectionResult result) {
                                    Log.d("!", "Connection failed. Cause: " + result.toString());
                                    if (!result.hasResolution()) {
                                        // Show the localized error dialog
                                        GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                                activity, 0).show();
                                        return;
                                    }
                                    // The failure has a resolution. Resolve it.
                                    // Called typically when the app is not yet authorized, and an
                                    // authorization dialog is displayed to the user.
                                    if (!authInProgress) {
                                        try {
                                            Log.d("!", "Attempting to resolve failed connection");
                                            authInProgress = true;
                                            result.startResolutionForResult(activity,
                                                    REQUEST_OAUTH);
                                        } catch (IntentSender.SendIntentException e) {
                                            Log.e("!",
                                                    "Exception while starting resolution activity", e);
                                        }
                                    }
                                }
                            }
                    )
                    .build();
        }
    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        /*
        WebAppInterface(Context c) {
            mContext = c;
        }*/

        @JavascriptInterface
        public void installRule(String ruleJSON) {
            //Log.d("myTag", ruleJSON);

            sendIntentToRuleEngine(ruleJSON);
        }
    }


}
