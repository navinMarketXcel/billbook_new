package com.billbook.app.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.billbook.app.databinding.ActivityPdfFormatBinding;
import com.billbook.app.databinding.InvoicePdfFormatLayoutBinding;
import com.billbook.app.viewmodel.InvoiceItemsViewModel;
import com.billbook.app.viewmodel.InvoiceViewModel;
import com.google.gson.Gson;

import es.voghdev.pdfviewpager.library.RemotePDFViewPager;
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter;
import es.voghdev.pdfviewpager.library.remote.DownloadFile;
import es.voghdev.pdfviewpager.library.util.FileUtil;

import com.billbook.app.R;
public class PDFFormatViewActivity extends AppCompatActivity{

    PDFPagerAdapter adapter;
    RemotePDFViewPager remotePDFViewPager;
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_format);
        webView = findViewById(R.id.webView);
        initUI();

        String htmlData= "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <!-- <link rel=\"stylesheet\" href=\"bill.css\"> -->\n" +
                "    <title>Invoice</title>\n" +
                "</head>\n" +
                "<style>\n" +
                "  *,body{\n" +
                "    padding: 0;\n" +
                "    margin: 0;\n" +
                "    font-family: Helvetica, sans-serif;\n" +
                "}\n" +
                ".border-left{\n" +
                "    border-left: 1px solid rgba(128, 128, 128, 0.349);\n" +
                "}\n" +
                ".main-container{\n" +
                "    top: 30px;\n" +
                "    position: relative;\n" +
                "    margin-left: auto;\n" +
                "    margin-right: auto;\n" +
                "    padding: 0;\n" +
                "    width: 95%;\n" +
                "    border: 1px solid rgba(128, 128, 128, 0.349);\n" +
                "    display: grid;\n" +
                "    grid-template-columns: 100%;\n" +
                "    /* height: 500px; */\n" +
                "}\n" +
                "\n" +
                ".header_invoice{\n" +
                "    padding: 10px;\n" +
                "    display: grid;\n" +
                "    grid-template-columns: 1fr 2fr 1fr;\n" +
                "    /* justify-items: center; */\n" +
                "    border-bottom: 1px solid rgba(128, 128, 128, 0.349);\n" +
                "}\n" +
                "\n" +
                ".logo-img{\n" +
                "    width: 100%;\n" +
                "    max-width: 200px;\n" +
                "}\n" +
                "\n" +
                ".logo-custom{\n" +
                "    display: flex;\n" +
                "    background-color: rgba(128, 128, 128, 0.349);\n" +
                "    height: 100%;\n" +
                "    width: 100%;\n" +
                "}\n" +
                ".logo-custom-span{\n" +
                "    font-size: large;\n" +
                "    width: 100%;\n" +
                "    text-align: center;\n" +
                "    justify-self: center;\n" +
                "    align-self: center;\n" +
                "    color: white;\n" +
                "}\n" +
                "\n" +
                ".header-retailer{\n" +
                "    padding: 5px 15px;\n" +
                "    display: flex;\n" +
                "    flex-direction: column;\n" +
                "    align-self: flex-end;\n" +
                "    font-size: 12px;\n" +
                "    font-stretch: expanded;\n" +
                "    font-style: normal;\n" +
                "    text-transform: uppercase;\n" +
                "}\n" +
                "\n" +
                ".header-tax-invoice{\n" +
                "    align-self: flex-end;\n" +
                "    text-align: right;\n" +
                "    bottom: 0;\n" +
                "}\n" +
                "\n" +
                ".header-span{\n" +
                "    word-break: break-all; \n" +
                "    margin-bottom: 1px;\n" +
                "}\n" +
                "\n" +
                ".bill-info{\n" +
                "    font-size: 11px;\n" +
                "    display: grid;\n" +
                "    grid-template-columns: 50% 50%;\n" +
                "    border-bottom: 1px solid rgba(128, 128, 128, 0.349);\n" +
                "}\n" +
                "\n" +
                ".bill-info_left{\n" +
                "    padding: 10px;\n" +
                "    /* border-bottom: 2px solid rgba(128, 128, 128, 0.349); */\n" +
                "    display: grid;\n" +
                "    grid-template-columns: 50% 50%;\n" +
                "    align-self: stretch;\n" +
                "}\n" +
                ".bill-info_right{\n" +
                "    /* border-bottom: 2px solid rgba(128, 128, 128, 0.349); */\n" +
                "    border-left: 1px solid rgba(128, 128, 128, 0.349);\n" +
                "    align-self: stretch;\n" +
                "    /* border-left: 2px solid rgba(128, 128, 128, 0.349); */\n" +
                "}\n" +
                ".bill-data{\n" +
                "    text-align: right;\n" +
                "}\n" +
                "\n" +
                ".billing-address{\n" +
                "    text-transform: uppercase;\n" +
                "    letter-spacing: 0.7px;\n" +
                "    display: grid;\n" +
                "    grid-template-columns: 50% 50%;\n" +
                "    border-bottom: 1px solid rgba(128, 128, 128, 0.349);\n" +
                "}\n" +
                "\n" +
                ".bill-ship{\n" +
                "    font-size: 12px;\n" +
                "    padding-left: 5px;\n" +
                "    padding-top: 5px;\n" +
                "    padding-bottom: 5px;\n" +
                "    background-color: rgba(128, 128, 128, 0.349);\n" +
                "}\n" +
                ".customer-address{\n" +
                "    display: flex;\n" +
                "    padding: 10px;\n" +
                "    flex-direction: column;\n" +
                "    /* border-bottom: 2px solid rgba(128, 128, 128, 0.349); */\n" +
                "    align-items: stretch;\n" +
                "    justify-items: stretch;\n" +
                "    align-self: flex-start;\n" +
                "    font-size: 11px;\n" +
                "}\n" +
                "\n" +
                ".product-info{\n" +
                "    width: 100%;\n" +
                "    display: grid;\n" +
                "    grid-template-columns: 4fr 25fr 10fr 10fr 10fr 15fr 9fr 9fr 9fr 12fr;\n" +
                "}\n" +
                ".product-header{\n" +
                "    padding-top: 5px;\n" +
                "    padding-bottom: 5px;\n" +
                "    text-align: center;\n" +
                "    word-break: break-all;\n" +
                "    /* height: 40px; */\n" +
                "    font-size: 12px;\n" +
                "    background-color: rgba(128, 128, 128, 0.349);\n" +
                "}\n" +
                "\n" +
                ".product-list{\n" +
                "    text-align: center;\n" +
                "    word-break: break-all;\n" +
                "    padding: 3px;\n" +
                "    border-left: 1px solid rgba(128, 128, 128, 0.349);\n" +
                "    border-bottom: 1px solid rgba(128, 128, 128, 0.349);\n" +
                "    font-size: 12px;\n" +
                "}\n" +
                ".product-left-align{\n" +
                "    text-align: left !important;\n" +
                "}\n" +
                ".footer{\n" +
                "    padding: 11px;\n" +
                "    display: flex;\n" +
                "    justify-content: space-between;\n" +
                "    font-size: 12px;\n" +
                "}\n" +
                "\n" +
                ".footer-right{\n" +
                "    width: 50%;\n" +
                "    display: grid;\n" +
                "        margin-left: auto;\n" +
                "    grid-template-columns: 50% 50%;\n" +
                "}\n" +
                ".footer-span{\n" +
                "    margin-bottom: 5px;\n" +
                "}\n" +
                ".payment-details{\n" +
                "    font-size: 11px;\n" +
                "    padding: 10px;\n" +
                "    display: flex;\n" +
                "}\n" +
                ".payment-details-head{\n" +
                "    text-align: left;\n" +
                "    grid-column: 1/3;\n" +
                "    align-self: flex-start;\n" +
                "    font-size: 13px;\n" +
                "}\n" +
                ".payment-span{\n" +
                "    margin-bottom: 5px;\n" +
                "}\n" +
                ".payment-span-flexend{\n" +
                "    text-align: right;\n" +
                "    align-self: flex-end;\n" +
                "}\n" +
                ".payment-details-left{\n" +
                "    display: grid;\n" +
                "    grid-template-columns: 1fr 1fr;\n" +
                "    justify-items: stretch;\n" +
                "}\n" +
                ".bottom-container{\n" +
                "    top: 30px;\n" +
                "    position: relative;\n" +
                "    margin-left: auto;\n" +
                "    margin-right: auto;\n" +
                "    padding: 0;\n" +
                "    width: 95%;\n" +
                "    display: grid;\n" +
                "    grid-template-columns: 100%;\n" +
                "    /* height: 500px; */\n" +
                "}\n" +
                ".greet-container{\n" +
                "        width: 100%;\n" +
                "        margin-top:3em\n" +
                "}\n" +
                ".greet-message{\n" +
                "        text-align:center;\n" +
                "        font-size:15px;\n" +
                "        font-weight:600;\n" +
                "}\n" +
                ".tc-container{\n" +
                "        width: 100%;\n" +
                "        margin-top:3em;\n" +
                "        font-size:10px;\n" +
                "}\n" +
                ".tc-list-container{\n" +
                "        list-style-position:inside;\n" +
                "}\n" +
                "\n" +
                "</style>\n" +
                "<body>\n" +
                "    <div class=\"main-container\">\n" +
                "        <section class=\"header_invoice\">\n" +
                "            <div>\n" +
                "                \n" +
                "                    <div class=\"logo-custom\"><span class=\"logo-custom-span\"><strong>LOGO</strong></span></div>\n" +
                "                \n" +
                "            </div>\n" +
                "\n" +
                "            <div class=\"header-retailer\">\n" +
                "                <span style=\"font-size: 25px;\"><strong>test1234</strong></span><br>\n" +
                "                <span class=\"header-span\">\n" +
                "                test Andaman &amp; Nicobar Islands, Bambooflat - 411028<br /> \n" +
                "                                Bambooflat,\n" +
                "                                Andaman &amp; Nicobar Islands\n" +
                "                </span>\n" +
                "                <span class=\"header-span\">411028</span>\n" +
                "                <span class=\"header-span\">MOB : 9096603564</span>\n" +
                "                <span class=\"header-span\">fgshej</span> \n" +
                "            </div>\n" +
                "            <span class=\"header-tax-invoice\"><strong>TAX INVOICE </strong></span>\n" +
                "        </section>\n" +
                "        <section class=\"bill-info\">\n" +
                "            <div class=\"bill-info_left\">\n" +
                "                <span>Bill NO</span>\n" +
                "                <span class=\"bill-data\"><strong>1 </strong></span>\n" +
                "                <span>Bill Date</span>\n" +
                "\n" +
                "\n" +
                "                <span class=\"bill-data\"><strong>22/10/2020</strong></span>\n" +
                "                <span>Bill Type</span>\n" +
                "                <span class=\"bill-data\"><strong>Original for Recipient</strong></span>\n" +
                "            </div>\n" +
                "            <div class=\"bill-info_right\"></div>\n" +
                "        </section>\n" +
                "        <section class=\"billing-address\">\n" +
                "            <span class=\"bill-ship\"><strong>Customer Details</strong></span>\n" +
                "                        <span class=\"bill-ship border-left\"><strong> </strong></span>\n" +
                "            <div class=\"customer-address\">\n" +
                "                <span class=\"header-span\">Suresh Babu Panchal</span>\n" +
                "                <span class=\"header-span\">1010101928</span>\n" +
                "                <span class=\"header-span\"> <br /></span>\n" +
                "                <!-- <span>02222654791</span> -->\n" +
                "            </div>\n" +
                "        </section>\n" +
                "\n" +
                "        <section class=\"product-info\">\n" +
                "                <span class=\"product-header\"><strong>No</strong></span>\n" +
                "                <span class=\"product-header\"><strong>Product/ <br>Service Name</strong></span>\n" +
                "                <span class=\"product-header\"><strong>HSN/</strong><br>SAC</strong></span>\n" +
                "                <span class=\"product-header\"><strong>Unit Price</strong></span>\n" +
                "                <span class=\"product-header\"><strong>Quantity</strong></span>\n" +
                "                <span class=\"product-header\"><strong>Net Amount</strong></span>\n" +
                "                <span class=\"product-header\"><strong>IGST</strong></span>\n" +
                "                <span class=\"product-header\"><strong>CGST</strong></span>\n" +
                "                <span class=\"product-header\"><strong>SGST</strong></span>\n" +
                "                <span class=\"product-header\"><strong>Amount</strong></span>\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "                \n" +
                "                <!-- PRODUCT1 -->\n" +
                "\n" +
                "                <span class=\"product-list\">1</span>\n" +
                "                <span class=\"product-list product-left-align\">Chocolate <br>ABCD12354XYZ</span>\n" +
                "                <span class=\"product-list\"></span>\n" +
                "                <span class=\"product-list\">50.00</span>\n" +
                "                <span class=\"product-list\">2</span>\n" +
                "                <span class=\"product-list\"> 100.00 </span>\n" +
                "                <span class=\"product-list\"></span>\n" +
                "                <span class=\"product-list\"> 0.00 <br/>0% </span>\n" +
                "                <span class=\"product-list\"> 0.00 <br/>0% </span>\n" +
                "                <span class=\"product-list\">100</span>\n" +
                "\n" +
                "                \n" +
                "        </section>\n" +
                "\n" +
                "        <section class=\"footer\">\n" +
                "            <div class=\"footer-right\">\n" +
                "                <span class=\"footer-span\">TAXABLE AMOUNT : </span>\n" +
                "                <span class=\"footer-span\"><strong>100.00</strong></span>\n" +
                "                \n" +
                "                <span class=\"footer-span\">GST AMOUNT </span>\n" +
                "                <span class=\"footer-span\"><strong>0.00</strong></span>\n" +
                "                <span class=\"footer-span\">Discount</span>\n" +
                "                <span class=\"footer-span\" ><strong>15.00</strong></span>\n" +
                "                \n" +
                "                <span class=\"footer-span\">TOTAL INVOICE VALUE</span>\n" +
                "                <span class=\"footer-span\"><strong>85.00</strong></span>\n" +
                "                                <span></span>\n" +
                "\n" +
                "                                        <span class=\"footer-span\" style=\"margin-top: 4em;text-align: right; margin-right: 0%;\"><strong> Signature </strong></span>\n" +
                "\n" +
                "            </div>\n" +
                "        </section>\n" +
                "    </div>\n" +
                "        <div class=\"bottom-container\">\n" +
                "                <section >\n" +
                "                        <div class=\"greet-container\">\n" +
                "                                <h2 class=\"greet-message\"> Thank you for your purchanse!</h2>\n" +
                "                        </div>\n" +
                "                </section>\n" +
                "                <section class=\"tc-container\">\n" +
                "                        <span><strong> Terms and Conditions: </strong></span>\n" +
                "                        <ol class=\"tc-list-container\">\n" +
                "                                <li>Item once purchased cannot be return.</li>\n" +
                "                                <li>In case of a damaged product or item received please contact our toll-free number-1800-328-XXXX.</li>\n" +
                "                                <li>The product carries only manufacturer's warranty and no return or exchange will be entertained.</li>\n" +
                "                                <li>The product of a damaged product or item received please contact.</li>\n" +
                "                        </ol>\n" +
                "                </section>\n" +
                "        </div>\n" +
                "</body>\n" +
                "</html>";
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.loadData(htmlData, "text/html", "UTF-8");
    }

    private void initUI() {

    }
    private void setData(String murl) {




    }




}
