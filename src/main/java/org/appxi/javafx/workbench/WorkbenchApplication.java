package org.appxi.javafx.workbench;

import org.appxi.javafx.desktop.DesktopApplication;
import org.appxi.javafx.theme.Theme;
import org.appxi.javafx.theme.ThemeSet;

import java.net.URL;
import java.util.function.Function;

public abstract class WorkbenchApplication extends DesktopApplication {

    @Override
    public WorkbenchPane getPrimaryViewport() {
        return (WorkbenchPane) super.getPrimaryViewport();
    }

    protected final void initThemes() {
        final Function<String, String[]> resourcesGetter = path -> {
            final URL commonUrl = getResource("/appxi/javafx/".concat(path));
            final URL customUrl = getResource("/appxi/".concat(getApplicationId()).concat("/").concat(path));
            //
            if (null == commonUrl && null == customUrl)
                return new String[0];
            if (null == commonUrl)
                return new String[]{customUrl.toExternalForm()};
            if (null == customUrl)
                return new String[]{commonUrl.toExternalForm()};
            return new String[]{commonUrl.toExternalForm(), customUrl.toExternalForm()};

        };
        themeProvider.addTheme(ThemeSet.light("light", "Light", "#e9e9eb")
                .addStylesheet(resourcesGetter.apply("themes/theme-light-default-app.css"))
                .addTheme(Theme.light("default", "Default", "#e9e9eb")
                        .addStylesheet(resourcesGetter.apply("themes/base-web.css"))
                        .addStylesheet(resourcesGetter.apply("themes/theme-light-default-web.css"))
                )
        );

        themeProvider.addTheme(ThemeSet.light("light-2", "Green", "#328291")
                .addStylesheet(resourcesGetter.apply("themes/theme-light-extend-app.css"))
                .addTheme(Theme.light("default", "Default", "#328291")
                        .addStylesheet(resourcesGetter.apply("themes/base-web.css"))
                        .addStylesheet(resourcesGetter.apply("themes/theme-light-extend-web.css"))
                )
        );

        themeProvider.addTheme(ThemeSet.light("light-javafx", "JavaFX Light", "#dddddd")
                .addStylesheet(resourcesGetter.apply("themes/theme-light-javafx-app.css"))
                .addTheme(Theme.light("default", "Default", "#e9e9eb")
                        .addStylesheet(resourcesGetter.apply("themes/base-web.css"))
                        .addStylesheet(resourcesGetter.apply("themes/theme-light-javafx-web.css"))
                )
        );

        themeProvider.addTheme(ThemeSet.dark("dark", "Dark", "#3b3b3b")
                .addStylesheet(resourcesGetter.apply("themes/theme-dark-default-app.css"))
                .addTheme(Theme.dark("default", "Default", "#3b3b3b")
                        .addStylesheet(resourcesGetter.apply("themes/base-web.css"))
                        .addStylesheet(resourcesGetter.apply("themes/theme-dark-default-web.css"))
                )
        );
    }

    protected abstract URL getResource(String path);

    @Override
    protected void start() {
        getPrimaryStage().getScene().getStylesheets()
                .add(getResource("/appxi/javafx/themes/workbench.css").toExternalForm());
    }

    @Override
    protected abstract WorkbenchPrimaryController createPrimaryController();
}
