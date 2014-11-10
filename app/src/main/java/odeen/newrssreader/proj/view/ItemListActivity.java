package odeen.newrssreader.proj.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by Женя on 03.11.2014.
 */
public class ItemListActivity extends SingleFragmentActivity {
    private static final String TAG = "ItemListActivity";


    public static final String EXTRA_CHANNEL_ID = "CHANNEL_ID";
    public static final String EXTRA_CHANNEL_NAME = "CHANNEL_NAME";
    public static final String EXTRA_CHANNEL_URL = "CHANNEL_URL";

    private String mChannelName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChannelName = getIntent().getStringExtra(EXTRA_CHANNEL_NAME);
        if (mChannelName == null && savedInstanceState != null)
            mChannelName = savedInstanceState.getString(EXTRA_CHANNEL_NAME);
        if (mChannelName != null && getActionBar() != null)
            getActionBar().setTitle(mChannelName);
        Log.d(TAG, TAG + " created " + mChannelName);
    }

    @Override
    protected Fragment createFragment() {
        long channelId = getIntent().getLongExtra(EXTRA_CHANNEL_ID, -1);
        String channelUrl = getIntent().getStringExtra(EXTRA_CHANNEL_URL);
        return ItemListFragment.newInstance(channelId, channelUrl);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_CHANNEL_NAME, mChannelName);
    }
}