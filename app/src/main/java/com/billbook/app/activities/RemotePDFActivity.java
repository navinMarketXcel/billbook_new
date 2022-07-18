/*
 * Copyright (C) 2016 Olmo Gallegos Hernández.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.billbook.app.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.billbook.app.R;
import com.billbook.app.networkcommunication.ApiClient;
import com.billbook.app.networkcommunication.ApiInterface;
import com.billbook.app.networkcommunication.DialogUtils;
import com.billbook.app.utils.Util;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RemotePDFActivity extends AppCompatActivity  {
    private JSONObject invoice;


    LinearLayout root;
    LinearLayout lnFormat;
    Button btnShortFormat,btnLongFormat,btnShare,btnPrint,btnHome;
    WebView webViewLong,webViewShort;
    private String filepath = null;
    private File pdfFile;
    private int invoiceNumber=1023;
    WebView printWeb;
    WebView printLong,printShort;
    ImageView ivToolBarBack;
    LinearLayout lnHelp,lnYouTube;
     String pdfLink;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setTitle(R.string.remote_pdf_example);
        setContentView(R.layout.activity_remote_pdf);
        ivToolBarBack = findViewById(R.id.ivToolBarBack);
        lnHelp = findViewById(R.id.lnHelp);
        lnYouTube = findViewById(R.id.lnYouTube);
        root = findViewById(R.id.remote_pdf_root);
        lnFormat = findViewById(R.id.lnFormat);
        btnShortFormat = findViewById(R.id.btnShortFormat);
        btnLongFormat = findViewById(R.id.btnLongFormat);
        btnShare = findViewById(R.id.btnShare);
        btnPrint = findViewById(R.id.btnPrint);
        btnHome = findViewById(R.id.btnHome);
        webViewLong = findViewById(R.id.webViewLong);
        webViewShort = findViewById(R.id.webViewShort);

        webViewLong.getSettings().setBuiltInZoomControls(true);
        webViewLong.getSettings().setSupportZoom(true);
        String unencodedHtml =getIntent().getExtras().getString("longHtml");
        String encodedHtml = Base64.encodeToString(unencodedHtml.getBytes(),
                Base64.NO_PADDING);
        webViewLong.loadData(encodedHtml, "text/html", "base64");
        webViewLong.getSettings().setUseWideViewPort(true);



        webViewShort.getSettings().setJavaScriptEnabled(true);
        webViewShort.getSettings().setBuiltInZoomControls(true);
        webViewShort.getSettings().setSupportZoom(true);
        String unencodedshortHtml = getIntent().getExtras().getString("shortHtml");
        String encodedHtmlShort = Base64.encodeToString(unencodedshortHtml.getBytes(),
                Base64.NO_PADDING);
        webViewShort.loadData(encodedHtmlShort, "text/html", "base64");
        webViewShort.getSettings().setUseWideViewPort(true);
        webViewShort.getSettings().setJavaScriptEnabled(true);

        webViewLong.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //initializing the printWeb Object
                printLong=webViewLong;
            }
        });
        webViewShort.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //initializing the printWeb Object
                printShort=webViewShort;
            }
        });
        setData();
        setonClick();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private void setData() {
        invoiceNumber=getIntent().getExtras().getInt("invoiceId");
        fetchCutlyLinkfromApi(invoiceNumber);
      /*  try {
            localInvoiceId = getIntent().getExtras().getLong("localInvId");
        }catch (Exception e) {
            e.printStackTrace();
        }*/


    }
    public void setonClick(){
        ivToolBarBack.setOnClickListener(v -> {
            finish();
        });

        lnHelp.setOnClickListener(v -> {
            Util. startHelpActivity(RemotePDFActivity.this);
        });
        lnYouTube.setOnClickListener(v -> {
            Util. startYoutubeActivity(RemotePDFActivity.this);
        });

        btnShortFormat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLongFormat.setBackground(getResources().getDrawable(R.drawable.white_button));
                btnShortFormat.setBackground(getResources().getDrawable(R.drawable.save_button));
                btnLongFormat.setTextColor(getResources().getColor(R.color.black));
                btnShortFormat.setTextColor(getResources().getColor(R.color.white));
                webViewLong.setVisibility(View.GONE);
                webViewShort.setVisibility(View.VISIBLE);
                //setDownloadButtonListener("https://github.github.com/training-kit/downloads/github-git-cheat-sheet.pdf");
            }
        });
        btnLongFormat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLongFormat.setBackground(getResources().getDrawable(R.drawable.save_button));
                btnShortFormat.setBackground(getResources().getDrawable(R.drawable.white_button));
                btnLongFormat.setTextColor(getResources().getColor(R.color.white));
                btnShortFormat.setTextColor(getResources().getColor(R.color.black));
                webViewLong.setVisibility(View.VISIBLE);
                webViewShort.setVisibility(View.GONE);
                //setDownloadButtonListener("https://storage.googleapis.com/billbook/billpdfs/upload_79c90d8474dd899e1bea8bd4493b8528.pdf");

            }
        });
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharePdf(pdfLink);
            }
        });
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("call on click");
                if(webViewLong.getVisibility() == View.VISIBLE){
                    printWeb=webViewLong;
                }else{
                    printWeb=webViewShort;

                }
                if(printWeb!=null)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        //Calling createWebPrintJob()
                        PrintTheWebPage(printWeb);
                    }else
                    {
                        //Showing Toast message to user
                        Toast.makeText(RemotePDFActivity.this, "Not available for device below Android LOLLIPOP", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    //Showing Toast message to user
                    Toast.makeText(RemotePDFActivity.this, "WebPage not fully loaded", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RemotePDFActivity.this, BottomNavigationActivity.class);
                startActivity(intent);
                finish();

            }
        });

    }

    public void fetchCutlyLinkfromApi(int invID ){
        ApiInterface apiService = ApiClient.getClient(this).create(ApiInterface.class);
        Map<String, String> headerMap = new HashMap<>();
        Call<Object> call = null;
        call = apiService.getCutlyUrl(headerMap,invID);

        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                try{
                    if(response.code()==200){
                        JSONObject body = new JSONObject(new Gson().toJson(response.body()));
                        JSONObject data = body.getJSONObject("data");

                        if(data.has("pdfLink")&&data.getString("pdfLink")!=null){
                            pdfLink=data.getString("pdfLink");
                             }
                        else{
                            //createWhatsppsmsToShare(invoice.getString("pdfLink"));
                        }
                    }
                    else{

                    }

                }
                catch(Exception e)
                { e.printStackTrace(); }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable throwable) {
                DialogUtils.showToast(RemotePDFActivity.this, "Please Check Your Internet Connection");
            }
        });
    }
    protected void sharePdf(String Url) {
        Intent i=new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Subject test");
        i.putExtra(android.content.Intent.EXTRA_TEXT, Url);
        startActivity(Intent.createChooser(i,"Share via"));

    }
    //object of print job
    PrintJob printJob;

    //a boolean to check the status of printing
    boolean printBtnPressed=false;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void PrintTheWebPage(WebView webView) {
        //set printBtnPressed true
        printBtnPressed=true;
        // Creating  PrintManager instance
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);
        //setting the name of job
        String jobName = getString(R.string.app_name) + " webpage"+webView.getUrl();
        // Creating  PrintDocumentAdapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
        // Create a print job with name and adapter instance
        assert printManager != null;
        printJob = printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(printJob!=null &&printBtnPressed) {
            if (printJob.isCompleted()) {
                //Showing Toast Message
                Toast.makeText(this, "Completed", Toast.LENGTH_SHORT).show();
            } else if (printJob.isStarted()) {
                //Showing Toast Message
                Toast.makeText(this, "isStarted", Toast.LENGTH_SHORT).show();

            } else if (printJob.isBlocked()) {
                //Showing Toast Message
                Toast.makeText(this, "isBlocked", Toast.LENGTH_SHORT).show();

            } else if (printJob.isCancelled()) {
                //Showing Toast Message
                Toast.makeText(this, "isCancelled", Toast.LENGTH_SHORT).show();

            } else if (printJob.isFailed()) {
                //Showing Toast Message
                Toast.makeText(this, "isFailed", Toast.LENGTH_SHORT).show();

            } else if (printJob.isQueued()) {
                //Showing Toast Message
                Toast.makeText(this, "isQueued", Toast.LENGTH_SHORT).show();

            }
            //set printBtnPressed false
            printBtnPressed=false;
        }
    }

}

