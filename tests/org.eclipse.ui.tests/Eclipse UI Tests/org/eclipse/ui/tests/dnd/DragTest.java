/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dnd;

import junit.framework.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.api.MockEditorPart;
import org.eclipse.ui.tests.util.FileUtil;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Tests a single drag-drop scenario, given a dragSource capable of initiating the drag and
 * a dropTarget that can locate the drop location. DragTestSuite will create a large number
 * of DragTests by matching combintations of drag sources with drop targets.  
 *<p></p>
 * <p>
 * <b>TEST NAMES:</b>
 * </p>
 * <p>
 * The name of the test indicates what drag scenario was being attempted. For example, the
 * test "drag Navigator to right of editor area" drags the Navigator view over
 * the editor area, dropping it near the right edge. Drag sources are described as follows:  
 * </p>
 * <p></p>  
 * <p>
 * [maximized] viewname [folder] 
 * </p>
 * <p></p>
 * <p>Drag sources prefixed by "maximized" were maximized at the time the drag started.
 * The word [folder] indicates that folder containing the given editor or view was being dragged.
 * Otherwise, the view itself was being dragged.</p>
 * <p></p>
 * <p>Drop targets are described as follows:</p> 
 * <p></p>  
 * <p>
 * [left|right|top|bottom|center] of targetname 
 * </p>
 * <p></p>
 * <p>Drop targets are always on top of the target. That is, "right of Navigator" means "on top of
 * the navigator view near its right edge" -- NOT "to the right of the Navigator view". When the
 * drop target is on an edge, it is always located 1 pixel away from the middle of that edge.</p>  
 *
 *<p></p>
 * <p>
 * <b>WHAT TO DO IF THIS TEST FAILS:</b>
 * </p>
 * <p>
 * If a drag operation did not have the intended effect, the test will fail with
 * a result like: 
 * </p>
 * <p></p>
 * <p>
 * Expecting 'layout ((((*Navigator)-active (*Problems))|layout ((Mock Editor 1, *Mock Editor 2)|active_nofocus (*Mock Editor 2)))-(*Outline, Properties))' and found 'layout ((layout ((Mock Editor 1, *Mock Editor 2)|active_nofocus (*Mock Editor 2))-(*Outline, Problems, Properties))-active (*Navigator))'
 * </p>
 * <p></p>
 * <p>
 * The expected and actual results are ASCII pictures of the layout. A stack of views or editors
 * is shown as a list enclosed in brackets, with an asterisk indicating the selected pane. The stack
 * may be prefixed by the words "active" or "active_nofocus" if they currently have the active or 
 * active_nofocus appearance. Inactive stacks have no prefix. 
 * </p>
 * <p></p>
 * <p>
 * For example, (Problems, *Console, Properties) indicates a stack containing the Problems, Console, and Properties views, 
 * where the Console view was currently selected. The root layout and editor areas are shown as "layout (...)". A vertical sash is shown as a 
 * bar "|" and a horizontal sash is shown using a dash "-".  All drag tests are done in the Drag Test 
 * Perspective.
 * </p>
 * <p>
 * The initial layout is:
 * </p>
 * <p></p>
 * <p>
 * layout (((*Navigator)|layout ((Mock Editor 1, *Mock Editor 2)|active (*Mock Editor 2)))-(*Outline, Problems, Properties))
 * </p>
 * <p></p>
 * <p>
 * Where editor 0 is "Mock Editor 1", and editors 1 and 2 are shown as "Mock Editor 2".
 * </p>
 * <p></p>
 * <p>
 * If you see a message like "dragtests.xml is out of date", this indicates that new tests
 * were added without describing their intended behavior in dragtests.xml. In that case, ensure that
 * there are currently no failures and regenerate dragtests.xml as described below.
 * </p>
 * <p></p>
 * <p>
 * <b>WHAT TO DO IF THE INTENTED BEHAVIOR CHANGES:</b>
 * </p>
 * <p>
 * If new tests are added or the existing drag/drop behavior is changed, it will
 * be necessary to update dragtests.xml. Do this as follows:</p>
 * <ul>
 * <li>include org.eclipse.ui.tests with your other plugins and launch an inner workspace</li>
 * <li>Go to customize perspective... -> commands -> Drag Test. This will add the "Drag-Drop Snapshot" item to your menubar.</li>
 * <li>Select "Drag-Drop Snapshot"</li>
 * <li>Select a file where the new shapshot will be saved, and click Okay</li>
 * <li>DO NOT use the mouse while generating dragtests.xml. This
 *     can interfere with the tests and cause an incorrect behavior to be recorded as the expected behavior.
 *     In particular, focus changes should be avoided.</li>
 * <li>Wait for several minutes while the test runs</li>
 * <li>When the test is complete, copy the file over the old data/dragtests.xml file</li> 
 * </ul>
 * <p>
 * At this point, the current drag/drop behavior will be considered the correct behavior,
 * and deviations will cause the test suites to fail.
 * </p>
 * 
 * @since 3.0
 */
public class DragTest extends UITestCase {
    TestDragSource dragSource;

    AbstractTestDropTarget dropTarget;

    String intendedResult;

    // 
    static IProject project;

    static IFile file1, file2;

    IEditorPart editor1, editor2;

    static IFile file3;

    IEditorPart editor3;

    static WorkbenchWindow window;

    static WorkbenchPage page;

    public DragTest(TestDragSource dragSource, AbstractTestDropTarget dropTarget) {
        super("drag " + dragSource.toString() + " to " + dropTarget.toString());
        this.dragSource = dragSource;
        this.dropTarget = dropTarget;
    }

    public void setExpectedResult(String intended) {
        intendedResult = intended;
    }

    protected void runTest() throws Throwable {
        String resultingLayout = internalTest();

        if (intendedResult != null) {
            if (!resultingLayout.equals(intendedResult)) {
                String errorMessage = "Expecting '" + intendedResult
                        + "' and found '" + resultingLayout + "'";

                System.out.println("Failed " + getName() + ": " + errorMessage);

                Assert.assertEquals(
                        "Drag operation resulted in incorrect layout",
                        intendedResult, resultingLayout);
            }
        } else {
            fail("data/dragtests.xml is missing data for test" + getName());
        }

        page.getActivePerspective().testInvariants();
    }

    public void doSetUp() throws Exception {
        // don't allow UITestCase to manage the deactivation of our window
        manageWindows(false);
        //window = (WorkbenchWindow)openTestWindow();

        //initialize the window
        if (window == null) {
            window = (WorkbenchWindow) fWorkbench.openWorkbenchWindow(
                    "org.eclipse.ui.tests.dnd.dragdrop", ResourcesPlugin
                            .getWorkspace());

            page = (WorkbenchPage) window.getActivePage();

            project = FileUtil.createProject("DragTest"); //$NON-NLS-1$
            file1 = FileUtil.createFile("DragTest1.txt", project); //$NON-NLS-1$
            file2 = FileUtil.createFile("DragTest2.txt", project); //$NON-NLS-1$
            file3 = FileUtil.createFile("DragTest3.txt", project); //$NON-NLS-1$
        }

        page.resetPerspective();
        page.closeAllEditors(false);
        //ensure that contentoutline is the focus part (and at the top of its stack)
        page.showView("org.eclipse.ui.views.ContentOutline");
        page.hideView(page.findView("org.eclipse.ui.internal.introview"));
        editor1 = page.openEditor(new FileEditorInput(file1),
                MockEditorPart.ID1);
        editor2 = page.openEditor(new FileEditorInput(file2),
                MockEditorPart.ID2);
        editor3 = page.openEditor(new FileEditorInput(file3),
                MockEditorPart.ID2);

        window.getShell().setActive();
        DragOperations
                .drag(editor2, new EditorDropTarget(0, SWT.CENTER), false);
        DragOperations
                .drag(editor3, new EditorAreaDropTarget(SWT.RIGHT), false);
    }

    private String internalTest() throws Exception {
        dragSource.setPage(page);
        //dropTarget.setSource(dragSource);

        dragSource.drag(dropTarget);

        return DragOperations.getLayoutDescription(page);
    }

    /**
     * Programatically run the test
     * 
     * @return
     * @throws Exception
     */
    public String performTest() throws Exception {
        setUp();
        try {
            String result = internalTest();
            return result;
        } finally {
            tearDown();
        }
    }
}