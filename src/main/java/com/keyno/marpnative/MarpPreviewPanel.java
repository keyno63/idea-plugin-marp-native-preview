package com.keyno.marpnative;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.jcef.JBCefBrowser;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;

public class MarpPreviewPanel {
    private final Project project;
    private final JBCefBrowser browser;
    private final MarpSlideRenderer renderer;
    private final Timer refreshTimer;

    private String lastFilePath = "";
    private long lastModificationStamp = -1L;

    public MarpPreviewPanel(@NotNull Project project) {
        this.project = project;
        this.browser = new JBCefBrowser();
        this.renderer = new MarpSlideRenderer();

        JPanel root = new JPanel(new BorderLayout());
        JBSplitter splitter = new JBSplitter(true, 1.0f);
        splitter.setFirstComponent(browser.getComponent());
        root.add(splitter, BorderLayout.CENTER);
        component = root;

        refreshTimer = new Timer(700, e -> refreshIfNeeded());
        refreshTimer.setRepeats(true);
        refreshTimer.start();

        Disposer.register(project, () -> {
            refreshTimer.stop();
            Disposer.dispose(browser);
        });

        browser.loadHTML(renderer.render(""));
    }

    private final JComponent component;

    public JComponent getComponent() {
        return component;
    }

    private void refreshIfNeeded() {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }

        VirtualFile file = FileEditorManager.getInstance(project).getSelectedFiles().length > 0
                ? FileEditorManager.getInstance(project).getSelectedFiles()[0]
                : null;
        if (file == null || !isMarkdown(file)) {
            return;
        }

        long stamp = editor.getDocument().getModificationStamp();
        String path = file.getPath();
        if (stamp == lastModificationStamp && path.equals(lastFilePath)) {
            return;
        }
        lastModificationStamp = stamp;
        lastFilePath = path;

        String markdown = editor.getDocument().getText();
        browser.loadHTML(renderer.render(markdown));
    }

    private static boolean isMarkdown(VirtualFile file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".md") || name.endsWith(".markdown") || name.endsWith(".mdown");
    }
}
