package org.appxi.javafx.app.dict;

import appxi.dict.DictEntry;
import appxi.dict.Dictionary;
import appxi.dict.DictionaryApi;
import appxi.dict.SearchMode;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.appxi.javafx.app.AppEvent;
import org.appxi.javafx.app.DesktopApp;
import org.appxi.javafx.helper.FxHelper;
import org.appxi.javafx.visual.MaterialIcon;
import org.appxi.javafx.workbench.WorkbenchPane;
import org.appxi.javafx.workbench.WorkbenchPart;
import org.appxi.javafx.workbench.WorkbenchPartController;
import org.appxi.util.FileHelper;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class DictionaryController extends WorkbenchPartController implements WorkbenchPart.SideTool {
    final Supplier<List<String>> webIncludesSupplier;
    final Function<String, String> htmlDocumentWrapper;

    public DictionaryController(WorkbenchPane workbench,
                                Supplier<List<String>> webIncludesSupplier,
                                Function<String, String> htmlDocumentWrapper) {
        super(workbench);

        this.id.set("DICTIONARY");
        this.title.set("查词典");
        this.tooltip.set("查词典 (Ctrl+D)");
        this.graphic.set(MaterialIcon.TRANSLATE.graphic());

        this.webIncludesSupplier = webIncludesSupplier;
        this.htmlDocumentWrapper = htmlDocumentWrapper;
    }

    @Override
    public boolean sideToolAlignTop() {
        return true;
    }

    @Override
    public void postConstruct() {
        app.eventBus.addEventHandler(AppEvent.STARTED, event -> {
            final Path dictRepo;
            if (DesktopApp.productionMode) {
                dictRepo = DesktopApp.appDir().resolve("template/dict");
            } else {
                Path tmp = Path.of("../appxi-dictionary/repo");
                if (FileHelper.notExists(tmp)) {
                    tmp = Path.of("../../appxi-dictionary/repo");
                }
                dictRepo = tmp;
            }
            DictionaryApi.setupDefaultApi(dictRepo);
        });

        app.getPrimaryScene().getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN),
                () -> this.activeViewport(false));

        app.eventBus.addEventHandler(DictionaryEvent.SEARCH,
                event -> this.lookup(null != event.text ? event.text.strip() : null));

        app.eventBus.addEventHandler(DictionaryEvent.SEARCH_EXACT, event -> {
            Dictionary dictionary = DictionaryApi.api().get(event.dictionary);
            Iterator<DictEntry.Scored> iterator = dictionary.model.search(SearchMode.TitleEquals, event.text, null);
            if (iterator.hasNext()) {
                showDictEntryWindow(iterator.next());
            }
        });
    }

    @Override
    public void activeViewport(boolean firstTime) {
        lookup(null);
    }

    private DictionaryLookupLayer lookupLayer;

    private void lookup(String text) {
        if (null == lookupLayer) {
            lookupLayer = new DictionaryLookupLayer(this);
        }
        lookupLayer.show(text != null ? text : lookupLayer.inputQuery);
    }

    void showDictEntryWindow(DictEntry item) {
        final String windowId = item.dictionary.id + " /" + item.id;

        //
        Window window = Window.getWindows().stream().filter(w -> windowId.equals(w.getScene().getUserData())).findFirst().orElse(null);
        if (null != window) {
            window.requestFocus();
            return;
        }
        //
        final DialogPane dialogPane = new DialogPane() {
            @Override
            protected Node createButtonBar() {
                return null;
            }
        };
        final Dialog<?> dialog = new Dialog<>();
        final DictionaryViewer dictViewer = new DictionaryViewer(this, item) {
            @Override
            void onSearchAllDictionaries(ActionEvent event) {
                dialog.setTitle(item.title() + " -- 全部词典  -  " + app.getAppName());
                dialogPane.getScene().setUserData(windowId + "/all");
                super.onSearchAllDictionaries(event);
            }
        };
        //
        dialogPane.setContent(dictViewer.viewport);
        dialogPane.getButtonTypes().add(ButtonType.OK);
        //
        dialog.setTitle(item.title() + " -- " + item.dictionary.getName() + "  -  " + app.getAppName());
        dialog.setDialogPane(dialogPane);
        dialog.getDialogPane().setPrefWidth(800);
        dialog.setResizable(true);
        dialog.initModality(Modality.NONE);
        dialog.initOwner(app.getPrimaryStage());
        dialog.getDialogPane().getScene().setUserData(windowId);
        dialog.setOnShown(evt -> FxHelper.runThread(100, () -> {
            dialog.setHeight(600);
            dialog.setY(dialog.getOwner().getY() + (dialog.getOwner().getHeight() - dialog.getHeight()) / 2);
            if (dialog.getX() < 0) dialog.setX(0);
            if (dialog.getY() < 0) dialog.setY(0);
            //
            dictViewer.navigate(null);
        }));
        dialog.setOnHidden(evt -> dictViewer.deinitialize());
        dialog.show();
    }
}
