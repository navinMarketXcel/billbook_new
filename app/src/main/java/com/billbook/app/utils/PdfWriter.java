package com.billbook.app.utils;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Shailendra on 05-08-2017.
 */

public class PdfWriter {

    private static final int MILS_IN_INCH = 1000;
    private int mRenderPageWidth;
    private int mRenderPageHeight;

    private Context mPrintContext;
    private ViewGroup contentViewGroup;
    private int currentPage;
    private int pageContentHeight;
    private float scale;
    private PdfDocument.Page page;
    private File pdfFile;

    public PdfWriter(Context context, ViewGroup contentViewGroup) {
        this.mPrintContext = context;
        this.contentViewGroup = contentViewGroup;
    }

    public File exportPDF(final String filePath) {

        DisplayMetrics metrics = mPrintContext.getResources().getDisplayMetrics();

        //        DisplayMetrics dm = new DisplayMetrics();
        //        contentViewGroup.getDisplay().getMetrics(dm);

        // will either be DENSITY_LOW, DENSITY_MEDIUM or DENSITY_HIGH
        //        int dpiClassification = dm.densityDpi;

        // these will return the actual dpi horizontally and vertically
        float xDpi = metrics.xdpi;
        float yDpi = metrics.ydpi;
        //        float xDpi = metrics.densityDpi;
        //        float yDpi = metrics.densityDpi;

        //        dpi = (Pixel Â· 1 Inch [25.4mm])/ LÃ¤nge [mm]
        //        10dpi = (5px Â· 25.4mm) / 12.7mm

        int margin = (int) ((50 * 25.4) / 12.7);

        // create a new document
        final PrintAttributes newAttributes =
                new PrintAttributes.Builder().setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
                        .setResolution(
                                new PrintAttributes.Resolution("res1", "Resolution", (int) xDpi, (int) yDpi))
                        //                .setMinMargins(new PrintAttributes.Margins(100, 100, 100, 100))
                        //                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .setMinMargins(new PrintAttributes.Margins(margin, margin, margin, margin))
                        .build();

        if (contentViewGroup.getChildCount() > 0) {
            final PrintedPdfDocument mPdfDocument = new PrintedPdfDocument(mPrintContext, newAttributes);

            final int density = Math.max(newAttributes.getResolution().getHorizontalDpi(),
                    newAttributes.getResolution().getVerticalDpi());

            // Note that we are using the PrintedPdfDocument class which creates
            // a PDF generating canvas whose size is in points (1/72") not screen
            // pixels. Hence, this canvas is pretty small compared to the screen.
            // The recommended way is to layout the content in the desired size,
            // in this case as large as the printer can do, and set a translation
            // to the PDF canvas to shrink in. Note that PDF is a vector format
            // and you will not lose data during the transformation.

            // The content width is equal to the page width minus the margins times
            // the horizontal printer density. This way we get the maximal number
            // of pixels the printer can put horizontally.
            int marginLeft =
                    (int) (density * (float) newAttributes.getMinMargins().getLeftMils() / MILS_IN_INCH);
            int marginRight =
                    (int) (density * (float) newAttributes.getMinMargins().getRightMils() / MILS_IN_INCH);
            int contentWidth =
                    (int) (density * (float) newAttributes.getMediaSize().getWidthMils() / MILS_IN_INCH)
                            - marginLeft
                            - marginRight;
            if (mRenderPageWidth != contentWidth) {
                mRenderPageWidth = contentWidth;
                //                            layoutNeeded = true;
            }

            // The content height is equal to the page height minus the margins times
            // the vertical printer resolution. This way we get the maximal number
            // of pixels the printer can put vertically.
            int marginTop =
                    (int) (density * (float) newAttributes.getMinMargins().getTopMils() / MILS_IN_INCH);
            int marginBottom =
                    (int) (density * (float) newAttributes.getMinMargins().getBottomMils() / MILS_IN_INCH);
            int contentHeight =
                    (int) (density * (float) newAttributes.getMediaSize().getHeightMils() / MILS_IN_INCH)
                            - marginTop
                            - marginBottom;
            if (mRenderPageHeight != contentHeight) {
                mRenderPageHeight = contentHeight;
                //                            layoutNeeded = true;
            }

            //                        // If no layout is needed that we did a layout at least once and
            //                        // the document info is not null, also the second argument is false
            //                        // to notify the system that the content did not change. This is
            //                        // important as if the system has some pages and the content didn't
            //                        // change the system will ask, the application to write them again.

            //            if (mPrintContext == null || mPrintContext.getResources()
            //                    .getConfiguration().densityDpi != density) {
            //                Configuration configuration = new Configuration();
            //                configuration.densityDpi = density;
            //                mPrintContext = mPrintContext.createConfigurationContext(
            //                        configuration);
            //                mPrintContext.setTheme(R.style.Theme_AppCompat_Light);
            ////                mPrintContext.setTheme(R.style.AppTheme_Pdf_Dialog);
            //
            //            }
            //

            currentPage = -1;
            pageContentHeight = 0;
            page = null;
            // The content is laid out and rendered in screen pixels with
            // the width and height of the paper size times the print
            // density but the PDF canvas size is in points which are 1/72",
            // so we will scale down the content.
            scale = Math.min((float) mPdfDocument.getPageContentRect().width() / mRenderPageWidth,
                    (float) mPdfDocument.getPageContentRect().height() / mRenderPageHeight);

            final int itemCount = contentViewGroup.getChildCount();
            for (int i = 0; i < itemCount; i++) {

                View itemView = contentViewGroup.getChildAt(i);
                if (itemView instanceof LinearLayout) {
                    LinearLayout llv = (LinearLayout) itemView;
                    if (llv.getOrientation() == LinearLayout.VERTICAL && llv.getChildCount() > 0) {
                        for (int j = 0; j < llv.getChildCount(); j++) {
                            View view = llv.getChildAt(j);
                            //                            if (view instanceof ListView) {
                            //                                ListView listview = (ListView) view;
                            //                                for (int k = 0; k < listview.getAdapter().getCount(); k++) {
                            //                                    View item = listview.getAdapter().getView(k, view, contentViewGroup);
                            //                                    measureViewAndDraw(item, mPdfDocument);
                            //                                }
                            //                            } else {
                            measureViewAndDraw(view, mPdfDocument);
                            //                            }
                        }
                    } else {
                        measureViewAndDraw(llv, mPdfDocument);
                    }
                } else {
                    measureViewAndDraw(itemView, mPdfDocument);
                }
            }

            //            // Done with the last page.
            if (page != null) {
                mPdfDocument.finishPage(page);
            }

            pdfFile = new File(filePath);
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
                //                        write the document viewGroup
                mPdfDocument.writeTo(fileOutputStream);
                fileOutputStream.close();
                mPdfDocument.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return pdfFile;
    }

    private void measureViewAndDraw(View view, PrintedPdfDocument mPdfDocument) {
        // Measure the next view
        measureView(view);

        // Add the height but if the view crosses the page
        // boundary we will put it to the next one.
        pageContentHeight += view.getMeasuredHeight();
        if (currentPage < 0 || pageContentHeight > mRenderPageHeight) {
            pageContentHeight = view.getMeasuredHeight();
            currentPage++;
            // Done with the current page - finish it.
            if (page != null) {
                mPdfDocument.finishPage(page);
            }
            page = mPdfDocument.startPage(currentPage);
            page.getCanvas().scale(scale, scale);
        }

        // If the current view is on a requested page, render it.
        if (page != null) {
            // Layout an render the content.
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            view.draw(page.getCanvas());
            // Move the canvas for the next view.
            page.getCanvas().translate(0, view.getHeight());
        }
    }

    private void measureView(View view) {
        final int widthMeasureSpec = ViewGroup.getChildMeasureSpec(
                View.MeasureSpec.makeMeasureSpec(mRenderPageWidth, View.MeasureSpec.EXACTLY), 0,
                view.getLayoutParams().width);
        final int heightMeasureSpec = ViewGroup.getChildMeasureSpec(
                View.MeasureSpec.makeMeasureSpec(mRenderPageHeight, View.MeasureSpec.EXACTLY), 0,
                view.getLayoutParams().height);
        view.measure(widthMeasureSpec, heightMeasureSpec);
    }
}
