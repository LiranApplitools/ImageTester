package com.applitools.ImageTester.TestObjects;

import com.applitools.ImageTester.ImageTester;
import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.images.Eyes;
import com.applitools.ImageTester.Interfaces.IResultsReporter;
import com.applitools.eyes.metadata.Image;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Batch extends TestUnit {
    private BatchInfo batch_;
    private Queue<Test> tests_ = new LinkedList<Test>();
    private IResultsReporter reporter_;

    public Batch(File file, IResultsReporter reporter) {
        super(file);
        reporter_ = reporter;
    }

    public Batch(BatchInfo batch, IResultsReporter reporter) {
        super(batch.getName());
        batch_ = batch;
        reporter_ = reporter;
    }

    // Original

//    public void run(Eyes eyes) {
//        batch_ = batch_ == null ? new BatchInfo(name()) : batch_;
//        eyes.setBatch(batch_);
//        System.out.printf("Batch: %s\n", name());
//        for (Test test : tests_) {
//            try {
//                test.run(eyes);
//            } finally {
//                test.dispose();
//            }
//        }
//        reporter_.onBatchFinished(batch_.getName());
//        eyes.setBatch(null);
//    }

    public void run(Eyes eyes) {
        batch_ = batch_ == null ? new BatchInfo(name()) : batch_;
        for (Test test : tests_) {
            try {
                test.setEyes(ImageTester.getConfiguredEyes());
                test.setBatch(batch_);

                // This is the added line that isn't working
                if(test instanceof PDFTest) {
                    if (ImageTester.isPDFParallelPerPage) {
                        List<PDFPageStep> pageStepList = ((PDFTest) test).getPDFPageSteps();
                        for (PDFPageStep step : pageStepList) {
                            ImageTester.parallelRunsHandler.addRunnable(step);
                        }
                    } else {
                        ImageTester.parallelRunsHandler.addRunnable(test);
                    }
                }
                // in case it is not a PDF Test
                else{
                    test.run(eyes);
                }



            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                test.dispose();
            }
        }
        reporter_.onBatchFinished(batch_.getName());
        eyes.setBatch(null);
    }

    public void addTest(Test test) {
        tests_.add(test);
    }

    public void dispose() {
        if (tests_ == null) return;
        for (Test test : tests_) {
            test.dispose();
        }
    }

    @Override
    public void run() {
        run(eyes);
    }
}
