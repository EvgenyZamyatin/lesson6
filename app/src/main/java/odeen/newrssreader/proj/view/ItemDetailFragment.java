package odeen.newrssreader.proj.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import odeen.newrssreader.R;

/**
 * Created by Женя on 08.11.2014.
 */
public class ItemDetailFragment extends Fragment {

    private String mLink;

    public static ItemDetailFragment newInstance(String link) {
        ItemDetailFragment fragment = new ItemDetailFragment();
        Bundle args = new Bundle();
        args.putString(ItemDetailActivity.EXTRA_ITEM_LINK, link);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLink = getArguments().getString(ItemDetailActivity.EXTRA_ITEM_LINK);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail_item, container, false);
        WebView webView = (WebView)v.findViewById(R.id.news_webView);
        webView.loadUrl(mLink);
        webView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        return v;
    }




}
