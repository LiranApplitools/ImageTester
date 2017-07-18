package com.applitools.ImageTester.TestObjects;

import com.applitools.ImageTester.Patterns;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.images.Eyes;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by yanir on 30/01/2016.
 */
public class PDFTest extends Test {
    private static final Pattern pattern = Patterns.PDF;
    private float dpi_;
    private String pdfPassword_;
    private List<Integer> pagesList_;
    private String pages_;
    private PDDocument document_;
    private PDFRenderer pdfRenderer_;

    protected PDFTest(File file, String appname) {
        this(file, appname, 300f);
    }

    protected PDFTest(File file, String appname, float dpi) {
        this(file, appname, 300f,null);
    }

    public PDFTest(File file, String appname, float dpi, String pdfPassword) {
        super(file, appname);
        this.pdfPassword_ =pdfPassword;
        this.dpi_ = dpi;
        try {
            document_ = PDDocument.load(file_, pdfPassword);
            pdfRenderer_ = new PDFRenderer(document_);
        } catch (IOException e) {
            System.out.printf("Error closing test %s \nPath: %s \nReason: %s \n", e.getMessage());
            e.printStackTrace();
        }
    }

    public void setPages(String pages){
        this.pages_ = pages;
        this.pagesList_ = setPagesList(pages);
    }

    @Override
    public void run(Eyes eyes) {
        Exception ex = null;
        TestResults result = null;

        try {
            eyes.open(appname_, name());
            for (int i = 0; i < pagesList_.size(); i++) {
                BufferedImage bim = pdfRenderer_.renderImageWithDPI(pagesList_.get(i) - 1, dpi_);
                eyes.checkImage(bim, String.format("Page-%s", pagesList_.get(i)));
            }
            result = eyes.close(false);
            printTestResults(result);
            handleResultsDownload(result);
            document_.close();
        } catch (IOException e) {
            ex = e;
            System.out.printf("Error closing test %s \nPath: %s \nReason: %s \n", e.getMessage());

        } catch (Exception e) {
            System.out.println("Oops, something went wrong!");
            System.out.print(e);
            e.printStackTrace();
        } finally {
            if (ex != null) ex.printStackTrace();
            eyes.abortIfNotClosed();
        }
    }

    public static boolean supports(File file) {
        return pattern.matcher(file.getName()).matches();
    }

    protected void setDpi(float dpi) {
        this.dpi_ = dpi;
    }

    public String getPdfPassword() {
        return pdfPassword_;
    }

    public void setPdfPassword(String pdfPassword) {
        this.pdfPassword_ = pdfPassword;
    }

    public List<Integer> setPagesList(String pages) {
        if ((document_ ==null) ||(pdfRenderer_ ==null)) return null;
        if (pages != null) return parsePagesToList(pages);
        else {
            ArrayList<Integer> list = new ArrayList<Integer>();
            for (int page = 0; page < document_.getNumberOfPages(); ++page) {
                list.add(page + 1);
            }
            return list;
        }
    }

    @Override
    public String name() {
        String pagesText = "";
        if (pages_ != null) pagesText = " pages [" + pages_ + "]";
        return file_ == null ? name_ + pagesText : file_.getName() + pagesText;
    }
}
