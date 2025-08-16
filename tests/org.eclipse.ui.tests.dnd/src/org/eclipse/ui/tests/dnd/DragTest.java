package org.eclipse.ui.tests.dnd;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.api.MockEditorPart;
import org.eclipse.ui.tests.harness.util.FileUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DragTest {

    TestDragSource dragSource;
    TestDropLocation dropTarget;

    static IProject project;
    static IFile file1, file2, file3;

    IEditorPart editor1, editor2, editor3;

    static WorkbenchWindow window;
    static WorkbenchPage page;

    public DragTest(TestDragSource dragSource, TestDropLocation dropTarget) {
        this.dragSource = dragSource;
        this.dropTarget = dropTarget;
    }

    @AfterEach
    public void doSetUp() throws Exception {
        if (window == null) {
            window = (WorkbenchWindow) PlatformUI.getWorkbench();
            page = (WorkbenchPage) window.getActivePage();

            project = FileUtil.createProject("DragTest");
            file1 = FileUtil.createFile("DragTest1.txt", project);
            file2 = FileUtil.createFile("DragTest2.txt", project);
            file3 = FileUtil.createFile("DragTest3.txt", project);
        }

        page.resetPerspective();
        page.closeAllEditors(false);

        page.showView("org.eclipse.ui.views.ContentOutline");
        page.hideView(page.findView("org.eclipse.ui.internal.introview"));
        editor1 = page.openEditor(new FileEditorInput(file1), MockEditorPart.ID1);
        editor2 = page.openEditor(new FileEditorInput(file2), MockEditorPart.ID2);
        editor3 = page.openEditor(new FileEditorInput(file3), MockEditorPart.ID2);

        window.getShell().setActive();
        DragOperations.drag(editor2, new EditorDropTarget(new ExistingWindowProvider(window), 0, SWT.CENTER), false);
        DragOperations.drag(editor3, new EditorAreaDropTarget(new ExistingWindowProvider(window), SWT.RIGHT), false);
    }

    @Test
    @DisplayName("drag editor2 to right")
    public void stallTest() {
        String[] testNames = {};
        boolean testNameMatches = false;
        for (String testName : testNames) {
            if (testName.equals("drag editor2 to right")) {
                testNameMatches = true;
                break;
            }
        }

        if (testNames.length == 0 || testNameMatches) {
            Display display = Display.getCurrent();
            Shell loopShell = new Shell(display, SWT.SHELL_TRIM);
            loopShell.setBounds(0, 0, 200, 100);
            loopShell.setText("Test Stall Shell");
            loopShell.setVisible(true);

            while (loopShell != null && !loopShell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        }
    }

    @Test
    public void performTest() throws Throwable {
        dragSource.setPage(page);
        dragSource.drag(dropTarget);
    }
}
