package odeen.newrssreader.proj.view;

import android.support.v4.app.Fragment;

/**
 * Created by Женя on 03.11.2014.
 */
public class ChannelListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ChannelListFragment();
    }
}
