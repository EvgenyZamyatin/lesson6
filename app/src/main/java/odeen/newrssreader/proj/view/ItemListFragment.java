package odeen.newrssreader.proj.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import odeen.newrssreader.R;
import odeen.newrssreader.proj.conroller.ChannelContentProvider;
import odeen.newrssreader.proj.conroller.ChannelManager;
import odeen.newrssreader.proj.conroller.ItemFetcherService;
import odeen.newrssreader.proj.conroller.SQLiteCursorLoader;
import odeen.newrssreader.proj.conroller.ServiceHelper;
import odeen.newrssreader.proj.model.Item;

/**
 * Created by Женя on 03.11.2014.
 */
public class ItemListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ItemListFragment";

    private long mIdChannel;
    private boolean mIsLoading;
    private String mUrlChannel;


    private BroadcastReceiver mOnListUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateStop();
        }
    };

    public static ItemListFragment newInstance(long id, String url) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        args.putLong(ItemListActivity.EXTRA_CHANNEL_ID, id);
        args.putString(ItemListActivity.EXTRA_CHANNEL_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIdChannel = getArguments().getLong(ItemListActivity.EXTRA_CHANNEL_ID);
        mUrlChannel = getArguments().getString(ItemListActivity.EXTRA_CHANNEL_URL);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mIsLoading = ChannelManager.get(getActivity()).getChannelLoading(mIdChannel);
        if (mIsLoading)
            getActivity().getActionBar().setSubtitle("Loading...");
        if (getListAdapter() == null && !getLoaderManager().hasRunningLoaders())
            getLoaderManager().restartLoader(0, null, this);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ItemFetcherService.ACTION_ITEMS_UPDATED);
        getActivity().registerReceiver(mOnListUpdate, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mOnListUpdate);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.item_list_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_refresh:
                if (!mIsLoading) {
                    setListAdapter(null);
                    ServiceHelper.get(getActivity()).refreshChannel(mIdChannel);
                    updateStart();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent intent = new Intent(getActivity(), ItemDetailActivity.class);
        intent.putExtra(ItemDetailActivity.EXTRA_ITEM_LINK, ((ItemCursorAdapter) getListAdapter()).get(position).getLink());
        startActivity(intent);
        Item item = ((ItemCursorAdapter)getListAdapter()).get(position);
        item.setWatched(true);
        ChannelManager.get(getActivity()).updateItem(mIdChannel, item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new ItemListCursorLoader(getActivity().getApplicationContext(), mIdChannel);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        ChannelContentProvider.ItemCursor c = new ChannelContentProvider.ItemCursor(cursor, getActivity());
        setListAdapter(new ItemCursorAdapter(getActivity(), c));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        setListAdapter(null);
    }

    private void updateStart() {
        mIsLoading = true;
        getActivity().getActionBar().setSubtitle("Loading...");
    }

    private void updateStop() {
        mIsLoading = false;
        getActivity().getActionBar().setSubtitle(null);
        getLoaderManager().restartLoader(0, null, this);
    }



    private static class ItemListCursorLoader extends SQLiteCursorLoader {
        private long mId;
        public ItemListCursorLoader(Context context, long id) {
            super(context);
            mId = id;
        }
        @Override
        protected Cursor loadCursor() {
            return ChannelManager.get(getContext()).queryItems(mId);
        }
    }



    private static class ItemCursorAdapter extends CursorAdapter {
        private SimpleDateFormat format = new SimpleDateFormat("dd LLL HH:mm");
        private ChannelContentProvider.ItemCursor mCursor;

        public ItemCursorAdapter(Context context, ChannelContentProvider.ItemCursor cursor) {
            super(context, cursor, true);
            mCursor = cursor;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.fragment_channel_list_item, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            Item item = mCursor.getItem();
            TextView title = (TextView) view.findViewById(R.id.channel_list_item_channelName);
            title.setText(item.getTitle());
            if (!item.isWatched()) {
                title.setTypeface(null, Typeface.BOLD);
            } else {
                title.setTypeface(null, Typeface.NORMAL);
            }
            TextView pubdate = (TextView) view.findViewById(R.id.channel_list_item_channelLink);
            if (item.getPubDate() != null)
                pubdate.setText(format.format(item.getPubDate()));
        }

        public Item get(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getItem();
        }
    }


}

/*
private class ItemArrayAdapter extends ArrayAdapter<Item> {
        private SimpleDateFormat format = new SimpleDateFormat("dd LLL HH:mm");
        public ItemArrayAdapter(ArrayList<Item> items) {
            super(getActivity(), 0, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.fragment_channel_list_item, null);
            }
            Item item = getItem(position);
            TextView title = (TextView) convertView.findViewById(R.id.channel_list_item_channelName);
            title.setText(item.getTitle());
            if (!item.isWatched()) {
                title.setTypeface(null, Typeface.BOLD);
            } else {
                title.setTypeface(null, Typeface.NORMAL);
            }

            TextView pubdate = (TextView) convertView.findViewById(R.id.channel_list_item_channelLink);
            if (item.getPubDate() != null)
                pubdate.setText(format.format(item.getPubDate()));
            return convertView;
        }
    }
 */
