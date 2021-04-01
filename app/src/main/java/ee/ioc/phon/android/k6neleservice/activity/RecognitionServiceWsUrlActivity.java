package ee.ioc.phon.android.k6neleservice.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ee.ioc.phon.android.k6neleservice.Log;
import ee.ioc.phon.android.k6neleservice.R;
import ee.ioc.phon.android.speechutils.utils.PreferenceUtils;

public class RecognitionServiceWsUrlActivity extends AppCompatActivity {

    private static final int TIMEOUT_PING = 100;
    private final List<String> mList = new ArrayList<>();
    private ServerAdapter mAdapter;
    private Button mBScan;
    private TextView mTvServerStatus;
    private EditText mEtUrl;
    private EditText mEtScan;
    private Scan mScan;
    private String mIp;
    private WebSocket mWebSocket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recognition_service_ws_url);

        mEtUrl = findViewById(R.id.etWsServerUrl);
        mEtUrl.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String serverUri = mEtUrl.getText().toString();
                setUrl(serverUri);
            }
            return false;
        });

        mTvServerStatus = findViewById(R.id.tvServerStatus);

        final RecyclerView lvResults = findViewById(R.id.rvIpList);
        //lvResults.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        lvResults.setLayoutManager(mLayoutManager);
        mAdapter = new ServerAdapter(mList);
        lvResults.setAdapter(mAdapter);

        findViewById(R.id.bWsServerDefault1).setOnClickListener(view -> setUrl(getString(R.string.defaultWsServer1)));

        findViewById(R.id.bWsServerDefault2).setOnClickListener(view -> setUrl(getString(R.string.defaultWsServer2)));

        mEtScan = findViewById(R.id.etScanNetwork);
        mEtScan.setText(getIPAddress(true));

        mBScan = findViewById(R.id.bScanNetwork);
        mBScan.setOnClickListener(view -> {
            if (mScan == null) {
                mIp = mEtScan.getText().toString().trim();
                if (mIp.isEmpty()) {
                    toast(getString(R.string.errorNetworkUndefined));
                } else {
                    mScan = new Scan();
                    mScan.execute(mIp);
                }
            } else {
                mScan.cancel(true);
                mScan = null;
            }
        });

        findViewById(R.id.bApplyUrl).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setData(Uri.parse(mEtUrl.getText().toString()));
            setResult(RESULT_OK, intent);
            finish();
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setUrl(PreferenceUtils.getPrefString(prefs, getResources(), R.string.keyWsServer, R.string.defaultWsServer));
    }

    @Override
    public void onStop() {
        super.onStop();
        closeSocket();
    }

    /**
     * Shows the given server URI and the server status.
     * In order to construct the status URI, removes the file name and the query string from the given URI, e.g.
     * ws://10.0.0.11:8080/client/ws/speech?key=1/2 -> ws://10.0.0.11:8080/client/ws/
     */
    private void setUrl(String uri) {
        if (mEtUrl != null) {
            mEtUrl.setText(uri);
            mTvServerStatus.setText(getString(R.string.statusServerStatus));
            int last = uri.lastIndexOf('?');
            if (last > 0) {
                uri = uri.substring(0, last);
            }
            setSummaryWithStatus(uri.substring(0, uri.lastIndexOf('/') + 1) + "status");
        }
    }

    private void setScanUi() {
        mBScan.setText(getString(R.string.buttonScan));
        // We post the change otherwise onProgressUpdate might change it later (?)
        mEtScan.post(() -> mEtScan.setText(mIp));
        mEtScan.setEnabled(true);
    }

    private void setCancelUi() {
        mEtScan.setEnabled(false);
        mBScan.setText(getString(R.string.buttonCancel));
    }

    private void toast(String msg) {
        Toast.makeText(RecognitionServiceWsUrlActivity.this, msg, Toast.LENGTH_LONG).show();

    }

    /**
     * Get IP address from first non-localhost interface
     * Solution from https://stackoverflow.com/a/13007325/12547
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    private static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(ex.getLocalizedMessage());
        }
        return "";
    }

    private class Scan extends AsyncTask<String, Pair<String, Boolean>, String> {
        @Override
        protected String doInBackground(String... ips) {
            String errorMessage = null;
            int start = 0;
            int end = 255;
            for (String ip : ips) {
                String base = ip.substring(0, ip.lastIndexOf('.') + 1);
                try {
                    // TODO: review
                    NetworkInterface iFace = NetworkInterface
                            .getByInetAddress(InetAddress.getByName(ip));

                    for (int i = start; i <= end; i++) {
                        if (isCancelled()) break;
                        InetAddress pingAddr = InetAddress.getByName(base + i);
                        String result = pingAddr.getHostAddress();
                        if (pingAddr.isReachable(iFace, 200, TIMEOUT_PING)) {
                            publishProgress(new Pair<>(result, true));
                            Log.i("FOUND: " + result);
                        } else {
                            publishProgress(new Pair<>(result, false));
                        }
                    }
                } catch (UnknownHostException ex) {
                    Log.e(ex.toString());
                    errorMessage = ex.getLocalizedMessage();
                    break;
                } catch (IOException ex) {
                    Log.e(ex.toString());
                    errorMessage = ex.getLocalizedMessage();
                    break;
                }
            }
            return errorMessage;
        }

        @Override
        protected void onProgressUpdate(Pair<String, Boolean>... progress) {
            Pair<String, Boolean> pair = progress[0];
            mEtScan.setText(pair.first);
            if (pair.second) {
                mList.add(pair.first);
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mList.clear();
            setCancelUi();
        }

        @Override
        protected void onPostExecute(String errorMessage) {
            setScanUi();
            if (errorMessage != null) {
                toast(errorMessage);
            }
        }

        @Override
        protected void onCancelled(String errorMessage) {
            setScanUi();
            if (errorMessage != null) {
                toast(errorMessage);
            }
        }
    }

    private class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.MyViewHolder> {
        private final List<String> mDataset;

        private class MyViewHolder extends RecyclerView.ViewHolder {
            private final Button mView;

            private MyViewHolder(Button v) {
                super(v);
                mView = v;
            }
        }

        private ServerAdapter(List<String> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public ServerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Button v = (Button) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_server_ip, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            holder.mView.setText(mDataset.get(position));
            holder.mView.setOnClickListener(view -> setUrl("ws://" + holder.mView.getText() + ":8080/client/ws/speech"));
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    private void closeSocket() {
        if (mWebSocket != null && mWebSocket.isOpen()) {
            mWebSocket.end(); // TODO: or close?
            mWebSocket = null;
        }
    }

    private void setSummaryWithStatus(final String urlStatus) {
        closeSocket();
        AsyncHttpClient.getDefaultInstance().websocket(urlStatus, "", (ex, webSocket) -> {
            mWebSocket = webSocket;
            if (ex != null) {
                mTvServerStatus.post(() -> mTvServerStatus.setText(String.format(getString(R.string.summaryWsServerWithStatusError), ex.getLocalizedMessage())));
                return;
            }
            mWebSocket.setStringCallback(s -> {
                Log.i(s);
                try {
                    final JSONObject json = new JSONObject(s);
                    final int numOfWorkers = json.getInt("num_workers_available");
                    mTvServerStatus.post(() -> mTvServerStatus.setText(getResources().
                            getQuantityString(R.plurals.summaryWsServerWithStatus, numOfWorkers, numOfWorkers)));
                } catch (JSONException e) {
                    mTvServerStatus.post(() -> mTvServerStatus.setText(String.format(getString(R.string.summaryWsServerWithStatusError), e.getLocalizedMessage())));
                }
            });
        });
    }
}
