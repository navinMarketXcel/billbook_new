package com.billbook.app.activities;

import androidx.appcompat.app.AppCompatActivity;
import com.billbook.app.R;
import com.github.barteksc.pdfviewer.PDFView;

import android.net.Uri;
import android.os.Bundle;

public class TestPdfActivity extends AppCompatActivity {
    private String uri;
    private PDFView pdfView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pdf);
        uri = getIntent().getExtras().getString("uri");
        pdfView = findViewById(R.id.pdfView);
        pdfView.fromUri(Uri.parse(uri)).load();
    }
}