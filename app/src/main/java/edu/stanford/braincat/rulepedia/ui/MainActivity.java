package edu.stanford.braincat.rulepedia.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import edu.stanford.braincat.rulepedia.R;
import edu.stanford.braincat.rulepedia.channels.Util;
import edu.stanford.braincat.rulepedia.service.AutoStarter;
import edu.stanford.braincat.rulepedia.service.RuleExecutor;
import edu.stanford.braincat.rulepedia.service.RuleExecutorService;


public class MainActivity extends ActionBarActivity {
    public static final String LOG_TAG = "rulepedia.UI";

    private RuleExecutorConnection executorConnection;
    private OmletServiceConnection omletServiceConnection;
    private BluetoothScanner bluetoothScanner;
    //private RandomQuoteSender randomQuoteSender;

    public RuleExecutor getRuleExecutor() {
        return executorConnection.getExecutor();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentPagerAdapter pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        // ensure the service is running
        AutoStarter.startExecutorService(this);
        executorConnection = new RuleExecutorConnection(this);
        omletServiceConnection = new OmletServiceConnection(this);
        bluetoothScanner = new BluetoothScanner(this);
        //randomQuoteSender = new RandomQuoteSender(this);
    }

    @Override
    protected void onStop() {
        executorConnection.stop();
        omletServiceConnection.stop();
        bluetoothScanner.stop();
        //randomQuoteSender.stop();
        super.onStop();
    }

    private void processRuleURL(Uri data) {
        try {
            String json = Util.parseEncodedRule(data.getLastPathSegment());
            RuleInstaller.installRuleConfirm(this, json);
        } catch (Exception e) {
            Log.w(LOG_TAG, "Failed to act on received rule URL", e);
        }
    }

    private void processOmletWebhookURL(Uri data) {
        try {
            String webhook = new String(Base64.decode(data.getLastPathSegment(), Base64.URL_SAFE));
            omletServiceConnection.setWebhook(webhook);

            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Log.w(LOG_TAG, "Failed to act on received webhook URL", e);
        }
    }

    private void doActionView(Uri data) {
        if (!data.getScheme().equals("https") ||
                (!data.getHost().equals("vast-hamlet-6003.herokuapp.com") &&
                        !data.getHost().equals("rulepedia.stanford.edu"))) {
            Log.w(LOG_TAG, "Received spurious intent for URL " + data);
            return;
        }

        if (data.getPath().startsWith("/rule/")) {
            processRuleURL(data);
        } else if (data.getPath().startsWith("/webhook/hook/")) {
            processOmletWebhookURL(data);
        } else {
            Log.w(LOG_TAG, "Received spurious intent for URL " + data);
        }
    }

    private void doInstallRule(Intent intent) {
        try {
            String json = intent.getStringExtra("json");
            RuleInstaller.installRuleConfirm(this, json);
        } catch (Exception e) {
            Log.w(LOG_TAG, "Failed to act on received rule install intent", e);
        }
    }

    private void doActionMain() {
        // ensure that we can run the Omlet UI
        omletServiceConnection.ensureOmlet();
    }

    @Override
    protected void onStart() {
        super.onStart();
        executorConnection.start();
        omletServiceConnection.start();
        bluetoothScanner.start();
        //randomQuoteSender.start();

        Intent startIntent = getIntent();

        if (startIntent == null)
            return;

        switch (startIntent.getAction()) {
            case Intent.ACTION_VIEW:
                doActionView(startIntent.getData());
                return;

            case RuleExecutorService.INSTALL_RULE_INTENT:
                doInstallRule(startIntent);
                return;

            case Intent.ACTION_MAIN:
                doActionMain();
                return;

            default:
                Log.w(LOG_TAG, "Received spurious intent " + startIntent.getAction());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothScanner.resume();
        //randomQuoteSender.resume();
    }

    @Override
    public void onPause() {
        //randomQuoteSender.pause();
        bluetoothScanner.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        omletServiceConnection = null;
        executorConnection = null;
        bluetoothScanner = null;
        //randomQuoteSender = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);

        /*
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_browse:
                //openSearch();
                return true;
            case R.id.action_install:
                return true;
            case R.id.action_manage:
                //composeMessage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        */
    }

}
