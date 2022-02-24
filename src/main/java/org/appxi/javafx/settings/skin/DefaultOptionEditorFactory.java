package org.appxi.javafx.settings.skin;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
import org.appxi.javafx.settings.DefaultOptions;
import org.appxi.javafx.settings.Option;
import org.appxi.javafx.settings.OptionEditor;
import org.appxi.javafx.settings.OptionEditorBase;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;

public class DefaultOptionEditorFactory<T> implements Callback<Option<T>, OptionEditor<T>> {
    private static final Class<?>[] numericTypes;

    public DefaultOptionEditorFactory() {
    }

    /**
     * @noinspection rawtypes, unchecked
     */
    public OptionEditor<T> call(Option<T> option) {
        if (option.valueProperty().getValue() == null) {
            return null;
        } else if (option instanceof DefaultOptions options) {
            return createChoiceBoxEditor(option, options.getValues());
        } else {
            Class<?> type = option.valueProperty().getValue().getClass();
            if (option.editorFactoryProperty().isPresent()) {
                return (OptionEditor) ((Function) option.editorFactoryProperty().get()).apply(option);
            } else if (type == String.class) {
                return (OptionEditor<T>) createTextEditor((Option<String>) option);
            } else if (isNumber(type)) {
                return (OptionEditor<T>) createNumberEditor((Option<Number>) option);
            } else if (type != Boolean.TYPE && type != Boolean.class) {
                if (type == LocalDate.class) {
                    return (OptionEditor<T>) createDateEditor((Option<LocalDate>) option);
                } else if (type != Color.class && type != Paint.class) {
                    if (type != null && type.isEnum()) {
                        return createChoiceBoxEditor((Option) option, Arrays.asList(type.getEnumConstants()));
                    } else {
                        System.err.println("type " + type + ": No editor found for " + option.getCaption());
                        return null;
                    }
                } else {
                    return (OptionEditor<T>) createColorEditor((Option<Color>) option);
                }
            } else {
                return (OptionEditor<T>) createSwitchEditor((Option<Boolean>) option);
            }
        }
    }

    public static OptionEditor<String> createTextEditor(Option<String> option) {
        return new OptionEditorBase<>(option, new TextField()) {
            {
                DefaultOptionEditorFactory.enableAutoSelectAll(this.getEditor());
            }

            public StringProperty valueProperty() {
                return this.getEditor().textProperty();
            }

            public void setValue(String value) {
                this.getEditor().setText(value);
            }
        };
    }

    public static OptionEditor<Number> createNumberEditor(final Option<Number> option) {
        return new OptionEditorBase<>(option, new TextField()) {
            private ObjectProperty<Number> innerValueProperty;
            private final Class<? extends Number> cls = option.valueProperty().getValue().getClass();
            private final DecimalFormat format;
            private boolean editing = false;

            {
                DefaultOptionEditorFactory.enableAutoSelectAll(this.getEditor());
                NumberFormat nf = NumberFormat.getNumberInstance();
                this.format = (DecimalFormat) nf;
                if (this.cls != Byte.TYPE && this.cls != Byte.class && this.cls != Short.TYPE && this.cls != Short.class && this.cls != Integer.TYPE && this.cls != Integer.class && this.cls != Long.TYPE && this.cls != Long.class && this.cls != BigInteger.class) {
                    this.format.setGroupingUsed(false);
                    this.getEditor().setTextFormatter(new TextFormatter<>((c) -> {
                        if (c.getControlNewText().isEmpty()) {
                            return c;
                        } else {
                            ParsePosition parsePosition = new ParsePosition(0);
                            Number num = this.format.parse(c.getControlNewText().toUpperCase(Locale.ROOT), parsePosition);
                            if (num != null && parsePosition.getIndex() >= c.getControlNewText().length()) {
                                this.editing = true;
                                this.innerValueProperty.setValue(num.doubleValue());
                                this.editing = false;
                                return c;
                            } else {
                                return null;
                            }
                        }
                    }));
                } else {
                    this.format.setParseIntegerOnly(true);
                    this.format.setMaximumFractionDigits(0);
                    this.format.setGroupingUsed(false);
                    this.format.setDecimalSeparatorAlwaysShown(false);
                    this.getEditor().setTextFormatter(new TextFormatter<>((c) -> {
                        if (c.getControlNewText().isEmpty()) {
                            return c;
                        } else {
                            ParsePosition parsePosition = new ParsePosition(0);
                            Number num = this.format.parse(c.getControlNewText(), parsePosition);
                            if (num != null && parsePosition.getIndex() >= c.getControlNewText().length()) {
                                this.editing = true;
                                if (this.cls != Long.TYPE && this.cls != Long.class) {
                                    this.innerValueProperty.setValue(num.intValue());
                                } else {
                                    this.innerValueProperty.setValue(num.longValue());
                                }

                                this.editing = false;
                                return c;
                            } else {
                                return null;
                            }
                        }
                    }));
                }

            }

            public ObjectProperty<Number> valueProperty() {
                if (this.innerValueProperty == null) {
                    this.innerValueProperty = new SimpleObjectProperty<>();
                    this.innerValueProperty.addListener((obs, ov, nv) -> {
                        if (nv != null && this.format != null && !this.editing) {
                            this.setValue(nv);
                        }

                    });
                }

                return this.innerValueProperty;
            }

            public void setValue(Number value) {
                this.getEditor().setText(this.format.format(value));
            }

            public Number getValue() {
                return this.innerValueProperty.get();
            }
        };
    }

    public static OptionEditor<Boolean> createSwitchEditor(Option<Boolean> option) {
        return new OptionEditorBase<>(option, new ToggleButton()) {
            {
                this.getEditor().getStyleClass().add("switch");
            }

            public BooleanProperty valueProperty() {
                return this.getEditor().selectedProperty();
            }

            public void setValue(Boolean value) {
                this.getEditor().setSelected(value);
            }
        };
    }

    public static <T> OptionEditor<T> createChoiceBoxEditor(Option<T> option, final Collection<T> items) {
        return new OptionEditorBase<T, ChoiceBox<T>>(option, new ChoiceBox<>()) {
            private ObjectProperty<T> selectedItemProperty;

            {
                this.getEditor().setItems(FXCollections.observableArrayList(items));
            }

            public ObjectProperty<T> valueProperty() {
                if (this.selectedItemProperty == null) {
                    this.selectedItemProperty = new SimpleObjectProperty<>();
                    this.getEditor().getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> this.selectedItemProperty.set(nv));
                    this.selectedItemProperty.addListener((obs, ov, nv) -> this.setValue(nv));
                }

                return this.selectedItemProperty;
            }

            public void setValue(T value) {
                this.getEditor().getSelectionModel().select(value);
            }
        };
    }

    public static OptionEditor<Color> createColorEditor(Option<Color> option) {
        return new OptionEditorBase<>(option, new ColorPicker()) {
            public ObjectProperty<Color> valueProperty() {
                return this.getEditor().valueProperty();
            }

            public void setValue(Color value) {
                this.getEditor().setValue(value);
            }
        };
    }

    public static OptionEditor<LocalDate> createDateEditor(Option<LocalDate> option) {
        return new OptionEditorBase<>(option, new DatePicker()) {
            public ObjectProperty<LocalDate> valueProperty() {
                return this.getEditor().valueProperty();
            }

            public void setValue(LocalDate value) {
                this.getEditor().setValue(value);
            }
        };
    }

    private static void enableAutoSelectAll(TextInputControl control) {
        control.focusedProperty().addListener((o, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(control::selectAll);
            }
        });
    }

    private static boolean isNumber(Class<?> type) {
        if (type == null) {
            return false;
        } else {
            for (int i = 0; i < numericTypes.length; ++i) {
                if (type == ((Class<?>[]) numericTypes)[i]) {
                    return true;
                }
            }

            return false;
        }
    }

    static {
        numericTypes = new Class[]{Byte.TYPE, Byte.class, Short.TYPE, Short.class, Integer.TYPE, Integer.class, Long.TYPE, Long.class, Float.TYPE, Float.class, Double.TYPE, Double.class, BigInteger.class, BigDecimal.class};
    }
}
