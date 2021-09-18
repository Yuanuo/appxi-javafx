//package org.appxi.javafx.helper;
//
//import javafx.animation.KeyFrame;
//import javafx.animation.KeyValue;
//import javafx.animation.Timeline;
//import javafx.scene.Scene;
//import javafx.scene.control.Label;
//import javafx.scene.layout.StackPane;
//import javafx.scene.paint.Color;
//import javafx.stage.Stage;
//import javafx.stage.StageStyle;
//import javafx.util.Duration;
//import org.appxi.javafx.desktop.DesktopApplication;
//
//public abstract class ToastHelper {
//    public static void toast(DesktopApplication application, String message) {
//        toast(application, application.getPrimaryStage(), message);
//    }
//
//    public static void toast(DesktopApplication application, Stage owner, String message) {
//        toast(application, owner, message, 2000, 300, 500);
//    }
//
//    public static void toast(DesktopApplication application, String message, int toastDelay, int fadeInDelay, int fadeOutDelay) {
//        toast(application, application.getPrimaryStage(), message, toastDelay, fadeInDelay, fadeOutDelay);
//    }
//
//    public static void toast(DesktopApplication application, Stage owner, String message, int toastDelay, int fadeInDelay, int fadeOutDelay) {
//        final Stage stage = new Stage(StageStyle.TRANSPARENT);
//        stage.initOwner(owner);
//        stage.setResizable(false);
//
//        final Label label = new Label(message);
//        label.setWrapText(true);
//
//        final StackPane root = new StackPane(label);
//        root.getStyleClass().add("toast-pane");
//        root.setStyle("-fx-padding: 2em;");
//        root.setFocusTraversable(false);
//
//        final Scene scene = FxHelper.withTheme(application, new Scene(root));
//        scene.setFill(Color.TRANSPARENT);
//        stage.setScene(scene);
//        stage.show();
//        owner.requestFocus();
//
//        final Timeline fadeInTimeline = new Timeline();
//        fadeInTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(fadeInDelay), new KeyValue(root.opacityProperty(), 1)));
//        fadeInTimeline.setOnFinished((ae) ->
//                new Thread(() -> {
//                    try {
//                        Thread.sleep(toastDelay);
//                    } catch (InterruptedException ignore) {
//                    }
//                    final Timeline fadeOutTimeline = new Timeline();
//                    fadeOutTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(fadeOutDelay), new KeyValue(root.opacityProperty(), 0)));
//                    fadeOutTimeline.setOnFinished((aeb) -> stage.close());
//                    fadeOutTimeline.play();
//                }).start());
//        fadeInTimeline.play();
//    }
//
//    private ToastHelper() {
//    }
//}