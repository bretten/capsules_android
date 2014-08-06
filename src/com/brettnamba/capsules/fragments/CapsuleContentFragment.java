package com.brettnamba.capsules.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.brettnamba.capsules.R;
import com.brettnamba.capsules.http.RequestContract;

/**
 * Fragment for displaying the web content of a Capsule.
 * 
 * @author Brett Namba
 *
 */
public class CapsuleContentFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_capsule_content, container, false);
        // Get the WebView
        WebView webView = (WebView) view.findViewById(R.id.fragment_capsule_content_web_view);
        webView.loadUrl(RequestContract.BASE_URL);
        webView.setWebViewClient(new FragmentWebViewClient());
        return view;
    }

    /**
     * Overrides WebViewClient so it can be used in a Fragment.
     */
    private class FragmentWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

    }

}
