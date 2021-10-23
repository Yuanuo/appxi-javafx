package org.appxi.javafx.control;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class CardChooser {
    private String title, headerText;
    private Node headerGraphic;
    private Window owner;
    private boolean cancelable = true, resizable = true, buttonStyle = true;
    private double prefWidth = 640, prefHeight = 480;
    private final List<Card> cards = new ArrayList<>();

    private CardChooser() {
    }

    public static CardChooser of(String title) {
        return new CardChooser().title(title);
    }

    public static Card.Builder ofCard(String title) {
        return new Card.Builder().title(title);
    }

    public CardChooser title(String title) {
        this.title = title;
        return this;
    }

    public CardChooser header(String text, Node graphic) {
        this.headerText = (null != graphic && null == text) ? "" : text;
        this.headerGraphic = graphic;
        return this;
    }

    public CardChooser owner(Window owner) {
        this.owner = owner;
        return this;
    }

    public CardChooser notCancelable() {
        this.cancelable = false;
        return this;
    }

    public CardChooser notResizable() {
        this.resizable = false;
        return this;
    }

    public CardChooser notButtonStyle() {
        this.buttonStyle = false;
        return this;
    }

    public CardChooser prefSize(double prefWidth, double prefHeight) {
        this.prefWidth = prefWidth;
        this.prefHeight = prefHeight;
        return this;
    }

    public CardChooser cards(Card... cards) {
        this.cards.addAll(List.of(cards));
        return this;
    }

    public CardChooser cards(Collection<Card> cards) {
        this.cards.addAll(cards);
        return this;
    }

    public Optional<Card> showAndWait() {
        final Dialog<Card> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setResizable(resizable);

        final DialogPane dialogPane = new DialogPane() {
            @Override
            protected Node createButtonBar() {
                return null;
            }
        };
        dialogPane.getStyleClass().add("card-chooser");
        dialogPane.setHeaderText(headerText);
        dialogPane.setGraphic(headerGraphic);

        final List<CardView> cardViews = this.cards.stream().map(CardView::new).toList();
        cardViews.forEach(cardView -> {
            if (this.buttonStyle)
                cardView.getStyleClass().add("button");
            if (!cardView.hasActions()) {
                cardView.setOnMouseReleased(evt -> {
                    if (isPicked(evt.getPickResult().getIntersectedNode(), cardView)) {
                        dialog.setResult(cardView.card);
                        dialog.hide();
                    }
                });
            }
        });

        final VBox vBox = new VBox(10, cardViews.toArray(new Node[0]));
        vBox.getStyleClass().add("cards");
        final ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setFitToWidth(true);

        dialogPane.setContent(scrollPane);
        dialogPane.setPrefSize(prefWidth, prefHeight);
        if (this.cancelable) {
            dialogPane.getButtonTypes().add(ButtonType.CANCEL);
            dialog.setResultConverter(v -> null);
        }
        dialog.setDialogPane(dialogPane);

        Window owner = this.owner;
        if (owner == null)
            owner = Window.getWindows().filtered(w -> w.isFocused() && !(w instanceof PopupWindow)).stream().findFirst().orElse(null);
        if (owner == null)
            owner = Window.getWindows().filtered(w -> !(w instanceof PopupWindow)).stream().findFirst().orElse(null);
        if (null != owner && null != owner.getScene()) {
            dialogPane.getScene().getRoot().setStyle(owner.getScene().getRoot().getStyle());
        }
        dialog.initOwner(owner);
        dialog.setOnShown(event -> {
            if (dialog.getX() < 0) dialog.setX(0);
            if (dialog.getY() < 0) dialog.setY(0);
        });
        return dialog.showAndWait();
    }

    private boolean isPicked(Node node, Node shouldBe) {
        while (node != null) {
            if (node == shouldBe) return true;
            node = node.getParent();
        }
        return false;
    }

    public static final class Card {
        private String title, description;
        private Node graphic;
        private boolean focused;
        private final List<Button> actions = new ArrayList<>();
        private Object userData;

        public <T> T userData() {
            return (T) userData;
        }

        public static final class Builder {
            private final Card data = new Card();

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

            public Builder focused(boolean focused) {
                data.focused = focused;
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

            public Builder userData(Object userData) {
                data.userData = userData;
                return this;
            }

            public Card get() {
                return data;
            }
        }
    }

    static final class CardView extends HBox {
        public final CardChooser.Card card;

        public CardView(CardChooser.Card data) {
            super(10);
            this.card = data;
            this.getStyleClass().add("card-view");
            if (data.focused)
                this.getStyleClass().add("focused");
            //
            final Label title = new Label(data.title);
            title.getStyleClass().add("title");
            title.setWrapText(false);
            title.setEllipsisString("...");
            //
            final VBox vBox = new VBox(5, title);
            HBox.setHgrow(vBox, Priority.ALWAYS);
            //
            if (null != data.description && !data.description.isBlank()) {
                final Label description = new Label(data.description);
                description.getStyleClass().add("description");
                description.setWrapText(true);
                VBox.setVgrow(description, Priority.ALWAYS);
                vBox.getChildren().add(description);
            } else {
                title.setAlignment(Pos.CENTER_LEFT);
                VBox.setVgrow(title, Priority.ALWAYS);
            }
            //
            if (this.hasActions()) {
                final HBox bar = new HBox(5);
                bar.getStyleClass().add("actions");
                bar.setAlignment(Pos.CENTER_RIGHT);
                data.actions.forEach(btn -> btn.setFocusTraversable(false));
                bar.getChildren().setAll(data.actions);
                vBox.getChildren().add(bar);
            }
            //
            if (null != data.graphic) {
                data.graphic.getStyleClass().add("logo");
                this.getChildren().setAll(data.graphic, vBox);
            } else this.getChildren().setAll(vBox);
        }

        public boolean hasActions() {
            return !this.card.actions.isEmpty();
        }
    }
}
