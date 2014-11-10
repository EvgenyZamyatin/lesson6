package odeen.newrssreader.proj.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import odeen.newrssreader.R;
import odeen.newrssreader.proj.conroller.ChannelContentProvider;
import odeen.newrssreader.proj.conroller.ChannelManager;
import odeen.newrssreader.proj.conroller.SQLiteCursorLoader;
import odeen.newrssreader.proj.conroller.ServiceHelper;
import odeen.newrssreader.proj.model.Channel;

/**
 * Created by Женя on 03.11.2014.
 */
public class ChannelListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = "ChannelListFragment";

    private static final int REQUEST_CHANNEL = 0;
    private static final String DIALOG_CHANNEL = "channel";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(0, null, this);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);
        ListView listView = (ListView) v.findViewById(android.R.id.list);
        registerForContextMenu(listView);
        //TODO: If change orientation data never loads
        if (getListAdapter() == null && !getLoaderManager().hasRunningLoaders()) {
            getLoaderManager().restartLoader(0, null, this);
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.channel_list_options, menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.channel_list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        Channel channel = ((ChannelCursorAdapter)getListAdapter()).get(position);
        switch (item.getItemId()) {
            case R.id.menu_item_delete_channel:
                ChannelManager.get(getActivity()).removeChannel(channel.getId());
                ChannelManager.get(getActivity()).removeItemsByChannelId(channel.getId());
                ((ChannelCursorAdapter)getListAdapter()).notifyDataSetChanged();
                return true;
            case R.id.menu_item_edit_channel:
                showEditDialog(channel);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void showEditDialog(Channel channel) {
        String name = null;
        String link = null;
        long id = -1;
        if (channel != null) {
            name = channel.getChannelName();
            link = channel.getChannelLink();
            id = channel.getId();
        }
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ChannelPickerFragment dialog = ChannelPickerFragment.newInstance(id, name, link);
        dialog.setTargetFragment(ChannelListFragment.this, REQUEST_CHANNEL);
        dialog.show(fm, DIALOG_CHANNEL);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_channel:
                showEditDialog(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Channel channel = ((ChannelCursorAdapter) getListAdapter()).get(position);
        Intent i = new Intent(getActivity(), ItemListActivity.class);
        i.putExtra(ItemListActivity.EXTRA_CHANNEL_ID, channel.getId());
        i.putExtra(ItemListActivity.EXTRA_CHANNEL_NAME, channel.getChannelName());
        i.putExtra(ItemListActivity.EXTRA_CHANNEL_URL, channel.getChannelLink());
        startActivity(i);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_CHANNEL) {
            String name = data.getStringExtra(ChannelPickerFragment.EXTRA_CHANNEL_NAME);
            String link = data.getStringExtra(ChannelPickerFragment.EXTRA_CHANNEL_LINK);
            long id = data.getLongExtra(ChannelPickerFragment.EXTRA_CHANNEL_ID, -1);
            id = ChannelManager.get(getActivity()).insertOrUpdateChannel(id, name, link);
            ServiceHelper.get(getActivity()).refreshChannel(id);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        ChannelListCursorLoader loader = new ChannelListCursorLoader(getActivity());
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        setListAdapter(new ChannelCursorAdapter(getActivity(), (ChannelContentProvider.ChannelCursor) cursor));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        setListAdapter(null);
    }

    private static class ChannelCursorAdapter extends CursorAdapter {

        private ChannelContentProvider.ChannelCursor mCursor;

        public ChannelCursorAdapter(Context context, ChannelContentProvider.ChannelCursor cursor) {
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
            Channel channel = mCursor.getChannel();
            TextView name = (TextView) view.findViewById(R.id.channel_list_item_channelName);
            name.setText(channel.getChannelName());
            TextView link = (TextView) view.findViewById(R.id.channel_list_item_channelLink);
            link.setText(channel.getChannelLink());
        }

        public Channel get(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getChannel();
        }
    }

    private static class ChannelListCursorLoader extends SQLiteCursorLoader {
        public ChannelListCursorLoader(Context context) {
            super(context);
        }
        @Override
        protected Cursor loadCursor() {
            return ChannelManager.get(getContext()).queryChannels();
        }
    }

}

/*
    private class ChannelArrayAdapter extends ArrayAdapter<Channel> {
        public ChannelArrayAdapter(ArrayList<Channel> channels) {
            super(getActivity(), 0, channels);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.fragment_channel_list_item, null);
            }
            Channel c = getItem(position);
            TextView name = (TextView) convertView.findViewById(R.id.channel_list_item_channelName);
            name.setText(c.getChannelName());
            TextView link = (TextView) convertView.findViewById(R.id.channel_list_item_channelLink);
            link.setText(c.getChannelLink());
            return convertView;
        }

    }

 */
