package de.x4fyr.markdownnotes;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * A placeholder fragment containing a simple view.
 */
public class ViewFragment extends Fragment {

    private WebView preview;
    private EditorActivity editorActivity;

    @Override
    public void onAttach(Context context) {
        this.editorActivity = (EditorActivity) context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view, container, false);
    }

    @Override
    public void onStart() {
        //noinspection ConstantConditions
        preview = (WebView) getView().findViewById(R.id.editor_preview);
        //noinspection HardCodedStringLiteral
        preview.loadData(editorActivity.getNote().formattedContent, "text/html", null);
        WebSettings previewWebSettings = preview.getSettings();
        previewWebSettings.setJavaScriptEnabled(true);

        super.onStart();
    }

    @Override
    public void onResume() {
        //noinspection HardCodedStringLiteral
        preview.loadData(editorActivity.getNote().formattedContent, "text/html", null);
        WebSettings previewWebSettings = preview.getSettings();
        previewWebSettings.setJavaScriptEnabled(true);
        super.onResume();
    }
}