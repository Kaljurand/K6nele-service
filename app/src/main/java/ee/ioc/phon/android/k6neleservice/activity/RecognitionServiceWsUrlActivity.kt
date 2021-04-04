package ee.ioc.phon.android.k6neleservice.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.WebSocket
import ee.ioc.phon.android.k6neleservice.Log.e
import ee.ioc.phon.android.k6neleservice.Log.i
import ee.ioc.phon.android.k6neleservice.R
import ee.ioc.phon.android.k6neleservice.activity.RecognitionServiceWsUrlActivity.ServerAdapter.MyViewHolder
import ee.ioc.phon.android.speechutils.utils.PreferenceUtils
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException
import java.util.*

class RecognitionServiceWsUrlActivity : AppCompatActivity() {
    private val mList: MutableList<String> = ArrayList()
    private lateinit var mAdapter: ServerAdapter
    private lateinit var mBScan: Button
    private lateinit var mTvServerStatus: TextView
    private lateinit var mEtUrl: EditText
    private lateinit var mEtScan: EditText

    private var mWebSocket: WebSocket? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recognition_service_ws_url)
        mEtUrl = findViewById(R.id.etWsServerUrl)
        mEtUrl.setOnEditorActionListener({ v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val serverUri = mEtUrl.getText().toString()
                setUrl(serverUri)
            }
            false
        })
        mTvServerStatus = findViewById(R.id.tvServerStatus)
        val lvResults = findViewById<RecyclerView>(R.id.rvIpList)
        //lvResults.setHasFixedSize(true);
        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        lvResults.layoutManager = mLayoutManager
        mAdapter = ServerAdapter(mList)
        lvResults.adapter = mAdapter
        findViewById<View>(R.id.bWsServerDefault1).setOnClickListener { view: View? -> setUrl(getString(R.string.defaultWsServer1)) }
        findViewById<View>(R.id.bWsServerDefault2).setOnClickListener { view: View? -> setUrl(getString(R.string.defaultWsServer2)) }
        mEtScan = findViewById(R.id.etScanNetwork)
        mEtScan.setText(getIPAddress(true))
        mBScan = findViewById(R.id.bScanNetwork)

        var job: Job? = null
        mBScan.setOnClickListener { view: View? ->
            if (job?.isActive == true) {
                job?.cancel()
                // TODO: restore the original IP in the EtScan field
                setScanUi("")
            } else {
                val ip = mEtScan.getText().toString().trim { it <= ' ' }
                if (ip.isEmpty()) {
                    toast(getString(R.string.errorNetworkUndefined))
                } else {
                    mList.clear()
                    //mAdapter.notifyDataSetChanged()
                    setCancelUi()
                    job = lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                workload(ip)
                            }
                        } catch (ex: UnknownHostException) {
                            e(ex.toString())
                            toast(ex.localizedMessage)
                        } catch (ex: IOException) {
                            e(ex.toString())
                            toast(ex.localizedMessage)
                        }
                        setScanUi(ip)
                    }
                }
            }
        }
        findViewById<View>(R.id.bApplyUrl).setOnClickListener { view: View? ->
            val intent = Intent()
            intent.data = Uri.parse(mEtUrl.getText().toString())
            setResult(RESULT_OK, intent)
            finish()
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        setUrl(PreferenceUtils.getPrefString(prefs, resources, R.string.keyWsServer, R.string.defaultWsServer))
    }

    public override fun onStop() {
        super.onStop()
        closeSocket()
    }

    /**
     * Shows the given server URI and the server status.
     * In order to construct the status URI, removes the file name and the query string from the given URI, e.g.
     * ws://10.0.0.11:8080/client/ws/speech?key=1/2 -> ws://10.0.0.11:8080/client/ws/
     */
    private fun setUrl(uri: String) {
        mEtUrl.setText(uri)
        mTvServerStatus.text = getString(R.string.statusServerStatus)
        val last = uri.lastIndexOf('?')
        val uri1: String
        if (last > 0) {
            uri1 = uri.substring(0, last)
        } else {
            uri1 = uri
        }
        setSummaryWithStatus(uri1.substring(0, uri1.lastIndexOf('/') + 1) + "status")
    }

    private fun setScanUi(ip: String) {
        mBScan.text = getString(R.string.buttonScan)
        mEtScan.post { mEtScan.setText(ip) }
        mEtScan.isEnabled = true
    }

    private fun setCancelUi() {
        mEtScan.isEnabled = false
        mBScan.text = getString(R.string.buttonCancel)
    }

    private fun toast(msg: String) {
        Toast.makeText(this@RecognitionServiceWsUrlActivity, msg, Toast.LENGTH_LONG).show()
    }

    suspend fun isReachable(iFace: NetworkInterface, name: String) {
        val pingAddr = InetAddress.getByName(name)
        val result = pingAddr.hostAddress
        lifecycleScope.launch(Dispatchers.Main) {
            mEtScan.setText(result)
        }
        if (pingAddr.isReachable(iFace, 200, TIMEOUT_PING)) {
            i("FOUND: $result")
            lifecycleScope.launch(Dispatchers.Main) {
                mList.add(result)
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * TODO: publish progress into the UI
     */
    suspend fun workload(ip: String) {
        val start = 0
        val end = 255
        val base = ip.substring(0, ip.lastIndexOf('.') + 1)
        // TODO: review
        val iFace = NetworkInterface
                .getByInetAddress(InetAddress.getByName(ip))
        if (iFace != null) {
            // TODO: start in separate coroutines, collect results, sort them, and show them
            for (i in start..end) {
                lifecycleScope.launch(Dispatchers.IO) {
                    isReachable(iFace, base + i)
                }
            }
        }
    }

    private inner class ServerAdapter constructor(private val mDataset: List<String>) : RecyclerView.Adapter<MyViewHolder>() {
        private inner class MyViewHolder(val mView: Button) : RecyclerView.ViewHolder(mView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_server_ip, parent, false) as Button
            return MyViewHolder(v)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.mView.text = mDataset[position]
            holder.mView.setOnClickListener { view: View? -> setUrl("ws://" + holder.mView.text + ":8080/client/ws/speech") }
        }

        override fun getItemCount(): Int {
            return mDataset.size
        }
    }

    private fun closeSocket() {
        if (mWebSocket != null && mWebSocket!!.isOpen) {
            mWebSocket!!.end() // TODO: or close?
            mWebSocket = null
        }
    }

    private fun setSummaryWithStatus(urlStatus: String) {
        closeSocket()
        AsyncHttpClient.getDefaultInstance().websocket(urlStatus, "") { ex: Exception?, webSocket: WebSocket? ->
            mWebSocket = webSocket
            if (ex != null) {
                mTvServerStatus.post { mTvServerStatus.text = String.format(getString(R.string.summaryWsServerWithStatusError), ex.localizedMessage) }
                return@websocket
            }
            mWebSocket!!.stringCallback = WebSocket.StringCallback { s: String? ->
                i(s)
                try {
                    val json = JSONObject(s)
                    val numOfWorkers = json.getInt("num_workers_available")
                    mTvServerStatus.post { mTvServerStatus.text = resources.getQuantityString(R.plurals.summaryWsServerWithStatus, numOfWorkers, numOfWorkers) }
                } catch (e: JSONException) {
                    mTvServerStatus.post { mTvServerStatus.text = String.format(getString(R.string.summaryWsServerWithStatusError), e.localizedMessage) }
                }
            }
        }
    }

    companion object {
        private const val TIMEOUT_PING = 200

        /**
         * Get IP address from first non-localhost interface
         * Solution from https://stackoverflow.com/a/13007325/12547
         *
         * @param useIPv4 true=return ipv4, false=return ipv6
         * @return address or empty string
         */
        private fun getIPAddress(useIPv4: Boolean): String {
            try {
                val interfaces: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (intf in interfaces) {
                    val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress) {
                            val sAddr = addr.hostAddress
                            val isIPv4 = sAddr.indexOf(':') < 0
                            if (useIPv4) {
                                if (isIPv4) return sAddr
                            } else {
                                if (!isIPv4) {
                                    val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                    return if (delim < 0) sAddr.toUpperCase() else sAddr.substring(0, delim).toUpperCase()
                                }
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                e(ex.localizedMessage)
            }
            return ""
        }
    }
}