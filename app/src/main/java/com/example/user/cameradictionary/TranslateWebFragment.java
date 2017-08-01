package com.example.user.cameradictionary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by user on 7/27/2017.
 */

public class TranslateWebFragment extends Fragment {
    private static final String TRANSLATE_LINK_EN_VI="https://translate.google.com.vn/m/translate#en/vi/";
    private View backButton;
    private ProgressBar progressBar;
    private WebView webDisplay;
    public TranslateWebFragment() {
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View returnView=inflater.inflate(R.layout.translate_fragment_layout,container,false);
        backButton=returnView.findViewById(R.id.image_button);
        progressBar=(ProgressBar)returnView.findViewById(R.id.progressBar);

        webDisplay=(WebView)returnView.findViewById(R.id.web_view);
        webDisplay.getSettings().setJavaScriptEnabled(true);
        webDisplay.setVerticalScrollBarEnabled(true);
        webDisplay.setScrollbarFadingEnabled(true);
        webDisplay.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress==100){
                    progressBar.setVisibility(View.GONE);
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        Bundle args=getArguments();
        String word=args.getString(MainActivity.WORD_KEY);
        updateTranslateURL(word);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
            }
        });
        return returnView;
    }

    private void updateTranslateURL(String orgWord){
        webDisplay.loadUrl(TRANSLATE_LINK_EN_VI+orgWord);
    }

    private void closeFragment(){
        FragmentTransaction fragTransaction=getActivity().getSupportFragmentManager().beginTransaction();
        fragTransaction.detach(this);
        fragTransaction.commit();
    }

    // TODO: 7/28/2017 handle all fragment lifecycle
}
