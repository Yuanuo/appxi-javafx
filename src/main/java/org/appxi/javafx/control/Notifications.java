package org.appxi.javafx.control;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.util.Duration;
import org.appxi.javafx.visual.MaterialIcon;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Notifications {
    private Notifications() {
    }

    public static Notification.Builder of() {
        return new Notification.Builder();
    }

    private static class NotificationsImpl {
        private static final NotificationsImpl INSTANCE = new NotificationsImpl();
        private static final Object AK_FINAL_ANCHOR_Y = new Object();
        private static final double PADDING = 15, SPACING = 15;

        private double startX, startY, screenWidth, screenHeight;
        private boolean isShowing;

        private final Map<Pos, List<Popup>> popupsMap = new HashMap<>();
        private final ParallelTransition parallelTransition = new ParallelTransition();

        void show(final Notification notification) {
            Window ownerWindow = prepareOwnerWindow(notification.owner);
            while (ownerWindow instanceof PopupWindow) {
                ownerWindow = ((PopupWindow) ownerWindow).getOwnerWindow();
            }

            final Popup popup = new Popup();
            popup.setAutoFix(false);
            if (null != ownerWindow && null != ownerWindow.getScene()) {
                popup.getScene().getRoot().setStyle(ownerWindow.getScene().getRoot().getStyle());
            }

            final Notification notificationToShow;
            final List<Popup> popups = popupsMap.get(notification.position);
            if (notification.threshold > 0 && popups != null && popups.size() >= notification.threshold) {
                for (Popup popupElement : popups) {
                    popupElement.hide();
                }
                notificationToShow = notification.thresholdNotification;
            } else {
                notificationToShow = notification;
            }

            final NotificationView notificationView = new NotificationView(notificationToShow) {
                @Override
                public void hide() {
                    isShowing = false;

                    // this would slide the notification bar out of view,
                    // but I prefer the fade out below
                    // doHide();

                    // animate out the popup by fading it
                    createHideTimeline(popup, this, data.position, Duration.ZERO).play();
                }

                @Override
                public boolean isShowFromTop() {
                    return NotificationsImpl.this.isShowFromTop(notificationToShow.position);
                }

                @Override
                public double getContainerHeight() {
                    return startY + screenHeight;
                }

                @Override
                public void relocateInParent(double x, double y) {
                    // this allows for us to slide the notification upwards
                    switch (data.position) {
                        case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> popup.setAnchorY(y - PADDING);
                        default -> {
                        }
                        // no-op
                    }
                }

                @Override
                public boolean isShowing() {
                    return isShowing;
                }
            };
            notificationView.setMinWidth(400);
            notificationView.setMaxSize(480, 720);
            popup.getContent().add(notificationView);
            popup.show(ownerWindow, 0, 0);

            // determine location for the popup
            final double viewWidth = notificationView.getWidth();
            final double viewHeight = notificationView.getHeight();

            // get anchorX
            final double anchorX = switch (notification.position) {
                case TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT -> PADDING + startX;
                case TOP_CENTER, CENTER, BOTTOM_CENTER -> startX + (screenWidth / 2.0) - viewWidth / 2.0 - PADDING / 2.0;
                case TOP_RIGHT, CENTER_RIGHT, BOTTOM_RIGHT -> startX + screenWidth - viewWidth - PADDING;
                default -> 0;
            };

            // get anchorY
            final double anchorY = switch (notification.position) {
                case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> PADDING + startY;
                case CENTER_LEFT, CENTER, CENTER_RIGHT -> startY + (screenHeight / 2.0) - viewHeight / 2.0 - PADDING / 2.0;
                case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> startY + screenHeight - viewHeight - PADDING;
                default -> 0;
            };

            popup.setAnchorX(anchorX);
            setFinalAnchorY(popup, anchorY);
            popup.setAnchorY(anchorY);

            isShowing = true;
            notificationView.doShow();

            final List<Popup> popups1 = popupsMap.computeIfAbsent(notification.position, k -> new LinkedList<>());

            doAnimation(notification.position, popup);

            // add the popup to the list so it is kept in memory and can be accessed later on
            popups1.add(popup);

            // begin a timeline to get rid of the popup
            createHideTimeline(popup, notificationView, notification.position, notification.hideAfterDuration).play();
        }

        private Window prepareOwnerWindow(Window owner) {
            Window window = null;
            if (owner == null) {
                for (Window w : Window.getWindows()) {
                    if (w.isFocused() && !(w instanceof PopupWindow)) {
                        window = w;
                        break;
                    }
                }
                Screen screen = null;
                if (null != window) {
                    screen = Screen.getScreensForRectangle(window.getX(), window.getY(), window.getWidth(), window.getHeight())
                            .stream()
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null);
                }
                if (null == screen)
                    screen = Screen.getPrimary();

                Rectangle2D screenBounds = screen.getBounds();
                startX = screenBounds.getMinX();
                startY = screenBounds.getMinY();
                screenWidth = screenBounds.getWidth();
                screenHeight = screenBounds.getHeight();
            } else {
                startX = owner.getX();
                startY = owner.getY();
                screenWidth = owner.getWidth();
                screenHeight = owner.getHeight();
                window = owner;
            }
            return window;
        }

        private Timeline createHideTimeline(final Popup popup, NotificationView view, final Pos position, Duration startDelay) {
            KeyValue fadeOutBegin = new KeyValue(view.opacityProperty(), 1.0);
            KeyValue fadeOutEnd = new KeyValue(view.opacityProperty(), 0.0);

            KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
            KeyFrame kfEnd = new KeyFrame(Duration.millis(500), fadeOutEnd);

            Timeline timeline = new Timeline(kfBegin, kfEnd);
            timeline.setDelay(startDelay);
            timeline.setOnFinished(e -> {
                popup.hide();
                if (popupsMap.containsKey(position)) {
                    popupsMap.get(position).remove(popup);
                }
            });

            return timeline;
        }

        private void doAnimation(Pos position, Popup changedPopup) {
            List<Popup> popups = popupsMap.get(position);
            if (popups == null) {
                return;
            }

            parallelTransition.stop();
            parallelTransition.getChildren().clear();

            final boolean isShowFromTop = isShowFromTop(position);

            // animate all other popups in the list upwards so that the new one
            // is in the 'new' area.
            // firstly, we need to determine the target positions for all popups
            double sum = 0;
            double[] targetAnchors = new double[popups.size()];
            for (int i = popups.size() - 1; i >= 0; i--) {
                Popup _popup = popups.get(i);

                final NotificationView notificationView = (NotificationView) _popup.getContent().get(0);
                final double popupHeight = notificationView.minHeight(notificationView.getWidth());

                if (isShowFromTop) {
                    if (i == popups.size() - 1) {
                        sum = getFinalAnchorY(changedPopup) + popupHeight + SPACING;
                    } else {
                        sum += popupHeight + SPACING;
                    }
                    targetAnchors[i] = sum;
                    _popup.setAnchorY(sum - popupHeight);
                } else {
                    if (i == popups.size() - 1) {
                        sum = getFinalAnchorY(changedPopup) - (popupHeight + SPACING);
                    } else {
                        sum -= (popupHeight + SPACING);
                    }

                    targetAnchors[i] = sum;
                    _popup.setAnchorY(sum + popupHeight);
                }
            }

            // then we set up animations for each popup to animate towards the
            // target
            for (int i = popups.size() - 1; i >= 0; i--) {
                final Popup _popup = popups.get(i);
                _popup.setAnchorX(changedPopup.getAnchorX());
                final double anchorYTarget = targetAnchors[i];
                if (anchorYTarget < 0) {
                    _popup.hide();
                }
                final double oldAnchorY = getFinalAnchorY(_popup);
                final double distance = anchorYTarget - oldAnchorY;

                setFinalAnchorY(_popup, oldAnchorY + distance);
                Transition t = new CustomTransition(_popup, oldAnchorY, distance);
                t.setCycleCount(1);
                parallelTransition.getChildren().add(t);
            }
            parallelTransition.play();
        }

        private double getFinalAnchorY(Popup popup) {
            return (double) popup.getProperties().get(AK_FINAL_ANCHOR_Y);
        }

        private void setFinalAnchorY(Popup popup, double anchorY) {
            popup.getProperties().put(AK_FINAL_ANCHOR_Y, anchorY);
        }

        private boolean isShowFromTop(Pos position) {
            return switch (position) {
                case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> true;
                default -> false;
            };
        }

        static class CustomTransition extends Transition {
            private final WeakReference<Popup> popupWeakReference;
            private final double oldAnchorY;
            private final double distance;

            CustomTransition(Popup popup, double oldAnchorY, double distance) {
                popupWeakReference = new WeakReference<>(popup);
                this.oldAnchorY = oldAnchorY;
                this.distance = distance;
                setCycleDuration(Duration.millis(350.0));
            }

            @Override
            protected void interpolate(double frac) {
                Popup popup = popupWeakReference.get();
                if (popup != null) {
                    double newAnchorY = oldAnchorY + distance * frac;
                    popup.setAnchorY(newAnchorY);
                }
            }
        }
    }

    public static final class Notification {
        private String title, description;
        private Node graphic;
        private Window owner;
        private Pos position = Pos.BOTTOM_RIGHT;
        private Duration hideAfterDuration = Duration.seconds(5);
        private boolean closeable;
        private final List<Button> actions = new ArrayList<>();
        private int threshold;
        private Notification thresholdNotification;

        public static final class Builder {
            private final Notification data = new Notification();

            public Builder title(String title) {
                data.title = title;
                return this;
            }

            public Builder description(String description) {
                data.description = description;
                return this;
            }

            public Builder graphic(Node graphic) {
                data.graphic = graphic;
                return this;
            }

            public Builder owner(Window owner) {
                data.owner = owner;
                return this;
            }

            public Builder closeable() {
                data.closeable = true;
                return this;
            }

            public Builder actions(Button... actions) {
                data.actions.addAll(List.of(actions));
                return this;
            }

            public Builder actions(Collection<Button> actions) {
                data.actions.addAll(actions);
                return this;
            }

            public Builder position(Pos position) {
                data.position = position;
                return this;
            }

            public Builder hideAfter(Duration duration) {
                data.hideAfterDuration = duration;
                return this;
            }

            public Builder hideAfter(int seconds) {
                return this.hideAfter(Duration.seconds(seconds));
            }

            public Builder threshold(int threshold, Notification thresholdNotification) {
                data.threshold = threshold;
                data.thresholdNotification = thresholdNotification;
                return this;
            }

            public void show() {
                NotificationsImpl.INSTANCE.show(this.data);
            }

            public void showWarning() {
                graphic(MaterialIcon.WARNING.graphic()); //$NON-NLS-1$
                show();
            }

            public void showInformation() {
                graphic(MaterialIcon.INFO.graphic()); //$NON-NLS-1$
                show();
            }

            public void showError() {
                graphic(MaterialIcon.ERROR.graphic()); //$NON-NLS-1$
                show();
            }

            public void showConfirm() {
                graphic(MaterialIcon.HELP.graphic()); //$NON-NLS-1$
                show();
            }
        }
    }

    abstract static class NotificationView extends BorderPane {
        public static final EventType<Event> ON_SHOWING = new EventType<>(Event.ANY, "NOTIFICATION_ON_SHOWING");
        public static final EventType<Event> ON_SHOWN = new EventType<>(Event.ANY, "NOTIFICATION_ON_SHOWN");
        public static final EventType<Event> ON_HIDING = new EventType<>(Event.ANY, "NOTIFICATION_ON_HIDING");
        public static final EventType<Event> ON_HIDDEN = new EventType<>(Event.ANY, "NOTIFICATION_ON_HIDDEN");

        protected final DoubleProperty transition = new SimpleDoubleProperty() {
            @Override
            protected void invalidated() {
                layoutChildren();
            }
        };

        protected final Notification data;

        protected NotificationView(Notification data) {
            this.data = data;
            this.getStyleClass().add("notification-popup");
            this.setVisible(isShowing());
            //
            Button close = null;
            if (data.closeable) {
                close = new Button("X");
                close.getStyleClass().add("close");
                close.setFocusTraversable(false);
                close.opacityProperty().bind(transition);
                close.setOnAction(event -> this.hide());
            }
            //
            if (null != data.title && !data.title.isBlank()) {
                final Label title = new Label(data.title);
                title.getStyleClass().add("title");
                title.setWrapText(false);
                title.setEllipsisString("...");
                title.opacityProperty().bind(transition);
                title.setMaxWidth(Double.MAX_VALUE);
                //
                final HBox box = new HBox(5, title);
                box.getStyleClass().add("top");
                box.setAlignment(Pos.CENTER_LEFT);
                box.opacityProperty().bind(transition);
                if (null != close) {
                    box.getChildren().add(close);
                }
                this.setTop(box);
            } else if (null != close) {
                final VBox box = new VBox(close);
                box.getStyleClass().add("right");
                box.setAlignment(Pos.CENTER);
                box.opacityProperty().bind(transition);
                this.setRight(box);
            }
            //
            final Label description = new Label(data.description, data.graphic);
            if (null == data.description || !data.description.isBlank() && null == data.graphic) {
                description.setText("<NO MESSAGE && NO GRAPHIC>");
            }
            description.getStyleClass().add("description");
            description.setAlignment(Pos.TOP_LEFT);
            description.setWrapText(true);
            description.setMinHeight(Region.USE_PREF_SIZE);
            description.setMaxWidth(Double.MAX_VALUE);
            description.opacityProperty().bind(transition);
            this.setCenter(description);
            //
            if (!data.actions.isEmpty()) {
                final HBox box = new HBox(5);
                box.getStyleClass().add("bottom");
                box.setAlignment(Pos.CENTER_RIGHT);
                box.opacityProperty().bind(transition);
                data.actions.forEach(btn -> btn.setFocusTraversable(false));
                box.getChildren().setAll(data.actions);
                this.setBottom(box);
            }
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            final double width = getWidth();
            if (isShowFromTop()) {
                // place at top of area
                relocateInParent(0, (transition.get() - 1) * minHeight(width));
            } else {
                // place at bottom of area
                relocateInParent(0, getContainerHeight() - prefHeight(width));
            }
        }

        public abstract boolean isShowFromTop();

        public abstract double getContainerHeight();

        public abstract void relocateInParent(double x, double y);

        public abstract boolean isShowing();

        public abstract void hide();

        @Override
        protected double computeMinHeight(double width) {
            return Math.max(super.computePrefHeight(width), 60);
        }

        @Override
        protected double computePrefHeight(double width) {
            return Math.max(super.computePrefHeight(width), minHeight(width)) * transition.get();
        }

        public void doShow() {
            transitionStartValue = 0;
            doAnimationTransition();
        }

//        public void doHide() {
//            transitionStartValue = 1;
//            doAnimationTransition();
//        }

        // --- animation timeline code
        private final Duration TRANSITION_DURATION = new Duration(350.0);
        private Timeline timeline;
        private double transitionStartValue;

        private void doAnimationTransition() {
            Duration duration;

            if (timeline != null && (timeline.getStatus() != Animation.Status.STOPPED)) {
                duration = timeline.getCurrentTime();

                // fix for #70 - the notification pane freezes up as it has zero
                // duration to expand / contract
                duration = duration == Duration.ZERO ? TRANSITION_DURATION : duration;
                transitionStartValue = transition.get();
                // --- end of fix

                timeline.stop();
            } else {
                duration = TRANSITION_DURATION;
            }

            timeline = new Timeline();
            timeline.setCycleCount(1);

            KeyFrame k1, k2;

            if (isShowing()) {
                k1 = new KeyFrame(Duration.ZERO,
                        event -> {
                            // start expand
                            setCache(true);
                            setVisible(true);

                            this.fireEvent(new Event(ON_SHOWING));
                        },
                        new KeyValue(transition, transitionStartValue)
                );

                k2 = new KeyFrame(duration,
                        event -> {
                            // end expand
                            this.setCache(false);

                            this.fireEvent(new Event(ON_SHOWN));
                        },
                        new KeyValue(transition, 1, Interpolator.EASE_OUT)

                );
            } else {
                k1 = new KeyFrame(Duration.ZERO,
                        event -> {
                            // Start collapse
                            this.setCache(true);

                            this.fireEvent(new Event(ON_HIDING));
                        },
                        new KeyValue(transition, transitionStartValue)
                );

                k2 = new KeyFrame(duration,
                        event -> {
                            // end collapse
                            setCache(false);
                            setVisible(false);

                            this.fireEvent(new Event(ON_HIDDEN));
                        },
                        new KeyValue(transition, 0, Interpolator.EASE_IN)
                );
            }

            timeline.getKeyFrames().setAll(k1, k2);
            timeline.play();
        }
    }
}
