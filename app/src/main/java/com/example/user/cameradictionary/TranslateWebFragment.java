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
import android.widget.ProgressBar;

/**
 * Created by user on 7/27/2017.
 */

public class TranslateWebFragment extends Fragment {
    private static final String TRANSLATE_LINK="https://translate.google.com.vn/m/translate#";
    private static final String SEPARATOR="%20";
    private ProgressBar progressBar;
    private WebView webDisplay;
    public TranslateWebFragment() {
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View returnView=inflater.inflate(R.layout.translate_fragment_layout,container,false);
        View backButton = returnView.findViewById(R.id.back_button);
        View homeButton=returnView.findViewById(R.id.home_button);
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
        String word=args.getString(DisplayActivity.WORD_KEY);
        int fromLanguageIndex=args.getInt(DisplayActivity.DETECT_LANGUAGE_INDEX_KEY);
        int toLanguageIndex=args.getInt(DisplayActivity.TRANSLATE_LANGUAGE_INDEX_KEY);
        String[] languageCodeArray=getResources().getStringArray(R.array.language_code_array);
        String fromLanguageCode=languageCodeArray[fromLanguageIndex];
        String toLanguageCode=languageCodeArray[toLanguageIndex];

        String url=buildTranslateUrl(fromLanguageCode,toLanguageCode,word);
        webDisplay.loadUrl(url);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFragment();
            }
        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backHome();
            }
        });
        return returnView;
    }

    private String buildTranslateUrl(String fromLanguageCode,String toLanguageCode,String translateWord){
        StringBuilder builder=new StringBuilder();
        String newWord=translateWord.replaceAll(" ",SEPARATOR);
        builder.append(TRANSLATE_LINK)
                .append(fromLanguageCode)
                .append("/")
                .append(toLanguageCode)
                .append("/")
                .append(newWord);
        return builder.toString();
    }

    private void closeFragment(){
        FragmentTransaction fragTransaction=getActivity().getSupportFragmentManager().beginTransaction();
        fragTransaction.detach(this);
        fragTransaction.commit();
    }

    private void backHome(){
        DisplayActivity activity=(DisplayActivity) getActivity();
        activity.exitActivity();
    }

    // TODO: 7/28/2017 handle all fragment lifecycle
}
