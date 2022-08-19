package com.billbook.app.activities;

import androidx.appcompat.app.AppCompatActivity;
import com.billbook.app.R;
import com.billbook.app.utils.Util;
import com.github.barteksc.pdfviewer.PDFView;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class TestPdfActivity extends AppCompatActivity {
    private String uri;
    private PDFView pdfView;
    private ImageView ivToolBarBack;
    private LinearLayout lnHelp,lnYouTube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pdf);
        uri = getIntent().getExtras().getString("uri");
        pdfView = findViewById(R.id.pdfView);
        pdfView.fromUri(Uri.parse(uri)).load();
        ivToolBarBack = findViewById(R.id.ivToolBarBack);
        lnHelp = findViewById(R.id.lnHelp);
        lnYouTube = findViewById(R.id.lnYouTube);
        setonClick();

    }
    public void setonClick(){
       ivToolBarBack.setOnClickListener(v -> {
            finish();
        });
        lnHelp.setOnClickListener(v -> {
            Util. startHelpActivity(TestPdfActivity.this);
        });
        lnYouTube.setOnClickListener(v -> {
            Util. startYoutubeActivity(TestPdfActivity.this);
        });


    }
}