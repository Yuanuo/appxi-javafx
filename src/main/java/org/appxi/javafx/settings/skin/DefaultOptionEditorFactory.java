package org.appxi.javafx.settings.skin;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.Callback;
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

    public OptionEditor<T> call(Option<T> option) {
        if (option.valueProperty().getValue() == null) {
            return null;
        } else {
            Class<?> type = option.valueProperty().getValue().getClass();
            if (option.editorFactoryProperty().isPresent()) {
                return (OptionEditor) ((Function) option.editorFactoryProperty().get()).apply(option);
            } else if (type == String.class) {
                return (OptionEditor<T>) createTextEditor(option);
            } else if (isNumber(type)) {
                return (OptionEditor<T>) createNumberEditor(option);
            } else if (type != Boolean.TYPE && type != Boolean.class) {
                if (type == LocalDate.class) {
                    return (OptionEditor<T>) createDateEditor(option);
                } else if (type != Color.class && type != Paint.class) {
                    if (type != null && type.isEnum()) {
                        return (OptionEditor<T>) createComboBoxEditor(option, Arrays.asList(type.getEnumConstants()));
                    } else {
                        System.err.println("type " + type + ": No editor found for " + option.getCaption());
                        return null;
                    }
                } else {
                    return (OptionEditor<T>) createColorEditor(option);
                }
            } else {
                return (OptionEditor<T>) createSwitchEditor(option);
            }
        }
    }

    public static OptionEditor<String> createTextEditor(Option option) {
        return new OptionEditorBase<String, TextField>(option, new TextField()) {
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

    public static OptionEditor<Number> createNumberEditor(final Option option) {
        return new OptionEditorBase<Number, TextField>(option, new TextField()) {
            private ObjectProperty<Number> innerValueProperty;
            private Class<? extends Number> cls = (Class<? extends Number>) option.valueProperty().getValue().getClass();
            private DecimalFormat format;
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
                    this.innerValueProperty = new SimpleObjectProperty();
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

    public static OptionEditor<Boolean> createSwitchEditor(Option option) {
        return new OptionEditorBase<Boolean, ToggleButton>(option, new ToggleButton()) {
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

    public static <T> OptionEditor<T> createComboBoxEditor(Option option, final Collection<T> items) {
        return new OptionEditorBase<T, ComboBox<T>>(option, new ComboBox<>()) {
            private ObjectProperty<T> selectedItemProperty;

            {
                this.getEditor().setItems(FXCollections.observableArrayList(items));
                this.getEditor().setEditable(false);
            }

            public ObjectProperty<T> valueProperty() {
                if (this.selectedItemProperty == null) {
                    this.selectedItemProperty = new SimpleObjectProperty<>();
                    this.getEditor().getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
                        this.selectedItemProperty.set((T) nv);
                    });
                    this.selectedItemProperty.addListener((obs, ov, nv) -> {
                        this.setValue(nv);
                    });
                }

                return this.selectedItemProperty;
            }

            public void setValue(T value) {
                this.getEditor().getSelectionModel().select(value);
            }
        };
    }

    public static OptionEditor<Color> createColorEditor(Option option) {
        return new OptionEditorBase<Color, ColorPicker>(option, new ColorPicker()) {
            public ObjectProperty<Color> valueProperty() {
                return this.getEditor().valueProperty();
            }

            public void setValue(Color value) {
                this.getEditor().setValue(value);
            }
        };
    }

    public static OptionEditor<LocalDate> createDateEditor(Option option) {
        return new OptionEditorBase<LocalDate, DatePicker>(option, new DatePicker()) {
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
            Class[] var1 = numericTypes;
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                Class<?> cls = var1[var3];
                if (type == cls) {
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
