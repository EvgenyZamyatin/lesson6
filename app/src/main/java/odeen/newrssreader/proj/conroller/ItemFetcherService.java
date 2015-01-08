package odeen.newrssreader.proj.conroller;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import odeen.newrssreader.proj.model.Channel;
import odeen.newrssreader.proj.model.Item;
import odeen.newrssreader.proj.parser.Parser;
import odeen.newrssreader.proj.view.ItemListActivity;

/**
 * Created by Женя on 04.11.2014.
 */
public class ItemFetcherService extends IntentService {
    public static final String ACTION_ITEMS_UPDATED = "odeen.ITEMS_UPDATED";

    private final static String TAG = "ItemFetcherService";

    private long mIdChannel;

    private String mLinkChannel;

    public ItemFetcherService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mIdChannel = intent.getLongExtra(ItemListActivity.EXTRA_CHANNEL_ID, -1);
        ChannelManager.get(getApplicationContext()).setChannelLoading(mIdChannel, true);
        /*
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */

        Log.d(TAG, "Channel id is " + intent.getLongExtra(ItemListActivity.EXTRA_CHANNEL_ID, -1));
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;
        if (!isNetworkAvailable) {
            ChannelManager.get(getApplicationContext()).setChannelLoading(mIdChannel, false);
            sendBroadcast(new Intent(ACTION_ITEMS_UPDATED));
            return;
        }
        long currentSessionId = ChannelManager.get(getApplicationContext()).getSessionId(mIdChannel);
        Channel channel = ChannelManager.get(getApplicationContext()).getChannelById(mIdChannel);
        if (channel == null)
            return;
        mLinkChannel = channel.getChannelLink();
        List<Item> items = null;
        Parser parser = new Parser();
        InputStream stream = null;
        try {
            stream = downloadUrl(mLinkChannel);
            items = parser.parse(stream);
        } catch (IOException e) {
        } catch (XmlPullParserException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (items == null) {
            Log.d(TAG, "cannot fetch news");
            sendBroadcast(new Intent(ACTION_ITEMS_UPDATED));
            ChannelManager.get(getApplicationContext()).setChannelLoading(mIdChannel, false);
            return;
        }
        for (Item i : items) {
            i.setSessionId(currentSessionId + 1);
            i.setWatched(false);
        }

        Log.d(TAG, "successfully fetch " + items.size() + " items");
        ChannelManager.get(getApplicationContext()).insertFreshItems(items, mIdChannel, currentSessionId + 1);
        ChannelManager.get(getApplicationContext()).removeItems(mIdChannel, currentSessionId);
        ChannelManager.get(getApplicationContext()).setChannelLoading(mIdChannel, false);
        sendBroadcast(new Intent(ACTION_ITEMS_UPDATED));
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }
}
