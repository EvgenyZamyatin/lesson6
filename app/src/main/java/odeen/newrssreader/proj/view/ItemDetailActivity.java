package odeen.newrssreader.proj.view;

import android.support.v4.app.Fragment;

/**
 * Created by Женя on 08.11.2014.
 */
public class ItemDetailActivity extends SingleFragmentActivity {
    public static final String EXTRA_ITEM_LINK = "ITEM_LINK";

    @Override
    protected Fragment createFragment() {
        String link = getIntent().getStringExtra(EXTRA_ITEM_LINK);
        return ItemDetailFragment.newInstance(link);
    }
}
