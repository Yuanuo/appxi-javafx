package org.appxi.javafx.app.web;

import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import org.appxi.javafx.visual.MaterialIcon;

public class WebToolPrinter extends WebTool {
    public WebToolPrinter(WebViewer webViewer) {
        super(webViewer);
        //
        Button button = MaterialIcon.PRINT.flatButton();
        button.setText("打印");
        button.setTooltip(new Tooltip("添加到系统默认打印机"));
        button.setOnAction(event -> {
            Printer printer = Printer.getDefaultPrinter();
            if (null == printer) {
                app.toastError("打印机不可用");
                return;
            }

            PrinterJob job = PrinterJob.createPrinterJob(printer);
            PageLayout pageLayout = printer.createPageLayout(Paper.A4, PageOrientation.PORTRAIT,
                    .4, .4, .4, .4);
            job.getJobSettings().setPageLayout(pageLayout);
            webPane.webEngine().print(job);
            job.endJob();
            app.toast("已添加到系统打印队列，请检查打印结果！");
        });
        //
        webPane.getTopBar().addLeft(button);
    }
}
