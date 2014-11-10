package odeen.newrssreader.proj.conroller;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import odeen.newrssreader.proj.view.ItemListActivity;

/**
 * Created by Женя on 06.11.2014.
 */
public class ServiceHelper {
    private Context mContext;
    private static ServiceHelper sHelper;
    private ServiceHelper(Context context) {
        mContext = context;
    }

    public static ServiceHelper get(Context context) {
        if (sHelper == null) {
            sHelper = new ServiceHelper(context.getApplicationContext());
        }
        return sHelper;
    }

    public void refreshChannel(long id) {
        if (!isNetworkAvailable()) {
            Toast.makeText(mContext, "Check your internet connection", Toast.LENGTH_SHORT).show();
        }
        Intent i = new Intent(mContext, ItemFetcherService.class);
        i.putExtra(ItemListActivity.EXTRA_CHANNEL_ID, id);
        mContext.startService(i);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;
        return isNetworkAvailable;
    }


}
