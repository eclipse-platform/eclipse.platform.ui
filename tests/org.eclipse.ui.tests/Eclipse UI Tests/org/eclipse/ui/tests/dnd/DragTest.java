/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dnd;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.dnd.TestDropLocation;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.api.MockEditorPart;
import org.eclipse.ui.tests.autotests.AbstractTestLogger;
import org.eclipse.ui.tests.autotests.UITestCaseWithResult;
import org.eclipse.ui.tests.harness.util.FileUtil;

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
public class DragTest extends UITestCaseWithResult {
    TestDragSource dragSource;

    TestDropLocation dropTarget;

    String intendedResult;

    // 
    static IProject project;

    static IFile file1, file2;

    IEditorPart editor1, editor2;

    static IFile file3;

    IEditorPart editor3;

    static WorkbenchWindow window;

    static WorkbenchPage page;

    public DragTest(TestDragSource dragSource, TestDropLocation dropTarget, AbstractTestLogger log, String suffix) {
        super("drag " + dragSource.toString() + " to " + dropTarget.toString() + suffix, log);
        this.dragSource = dragSource;
        this.dropTarget = dropTarget;
    }

    public DragTest(TestDragSource dragSource, TestDropLocation dropTarget, AbstractTestLogger log) {
    	this(dragSource, dropTarget, log, "");
    }
    
    public void doSetUp() throws Exception {
        // don't allow UITestCase to manage the deactivation of our window
        manageWindows(false);
        //window = (WorkbenchWindow)openTestWindow();

        //initialize the window
        if (window == null) {
            window = (WorkbenchWindow) fWorkbench.openWorkbenchWindow(
            	"org.eclipse.ui.tests.dnd.dragdrop", getPageInput());

            page = (WorkbenchPage) window.getActivePage();

            project = FileUtil.createProject("DragTest"); //$NON-NLS-1$
            file1 = FileUtil.createFile("DragTest1.txt", project); //$NON-NLS-1$
            file2 = FileUtil.createFile("DragTest2.txt", project); //$NON-NLS-1$
            file3 = FileUtil.createFile("DragTest3.txt", project); //$NON-NLS-1$

            // Disable animations since they occur concurrently and can interferre
            // with locating drop targets
            IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
            apiStore.setValue(
                    IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS,
                    false);
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
                .drag(editor2, new EditorDropTarget(new ExistingWindowProvider(window), 0, SWT.CENTER), false);
        DragOperations
                .drag(editor3, new EditorAreaDropTarget(new ExistingWindowProvider(window), SWT.RIGHT), false);
    }

    /**
     * This method is useful in order to 'freeze' the test environment after a particular test in order to 
     * manipulate the environment to figure out what's going on. It essentially opens a new shell and enters
     * a modal loop on it, preventing the tests from continuing until the 'stall' shell is closed. Note that
     * using a dialog would prevent us from manipulating the shell that the drag and drop tests are being performed in
     */
    public void stallTest() {
    	// Add the explicit test names that you want to stall on here...
    	// (It's probably easiest to cut them directly from the exected results file)
    	String[] testNames = {
    	};
    	
    	// Does the name match any of the explicit test names??
    	boolean testNameMatches = false;
    	for (int i = 0; i < testNames.length; i++) {
    		if (testNames[i].equals(this.getName())) {
    			testNameMatches = true;
    			break;
    		}
    	}
    	
    	// Stall always if no explicit names are supplied. Otherwise only stall when there's a
    	// match.
    	if (testNames.length == 0 || testNameMatches) {
	    	Display display = Display.getCurrent();
	    	Shell loopShell = new Shell(display, SWT.SHELL_TRIM);
	    	loopShell.setBounds(0,0,200,100);
	    	loopShell.setText("Test Stall Shell");
	    	loopShell.setVisible(true);
	    	
	    	while (loopShell != null && !loopShell.isDisposed()) {
	    		if (!display.readAndDispatch())
	    			display.sleep();
	    	}
    	}
    }
    
    public String performTest() throws Throwable {
        // Uncomment the following line to 'stall' the tests here...
        //stallTest();

    	// KLUDGE!! revert to the old min/max when dragging maximized views
    	// see bug 180242. This code should disappear before release...
        IPreferenceStore apiStore = PrefUtil.getAPIPreferenceStore();
        boolean curMinMaxState = apiStore.getBoolean(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX);
    	if (getName().indexOf("drag maximized") >= 0)
    		apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, false);
    		
    	dragSource.setPage(page);

        dragSource.drag(dropTarget);

        // Ensure that the model is sane
        // page.testInvariants();
        
        // Uncomment the following line to 'stall' the tests here...
        //stallTest();
        
        // KLUDGE!! Restore the min/max pref
		apiStore.setValue(IWorkbenchPreferenceConstants.ENABLE_NEW_MIN_MAX, curMinMaxState);
        
        return DragOperations.getLayoutDescription(page);
    }
}
