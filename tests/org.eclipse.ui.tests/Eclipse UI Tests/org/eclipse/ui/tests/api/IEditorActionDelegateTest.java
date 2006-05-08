/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.ActionUtil;
import org.eclipse.ui.tests.harness.util.FileUtil;

/**
 * Tests the lifecycle for an editor action delegate.
 */
public class IEditorActionDelegateTest extends IActionDelegateTest {

    public static String EDITOR_ID = "org.eclipse.ui.tests.api.IEditorActionDelegateTest";

    private MockEditorPart editor;

    /**
     * Constructor for IWorkbenchWindowActionDelegateTest
     */
    public IEditorActionDelegateTest(String testName) {
        super(testName);
    }

    public void testSetActiveEditor() throws Throwable {
        // When an action delegate is run the
        // setActiveEditor, selectionChanged, and run methods should
        // be called, in that order.

        // Run the action.
        testRun();

        // Verify lifecycle.
        MockActionDelegate delegate = getDelegate();
        assertNotNull(delegate);
        assertTrue(delegate.callHistory.verifyOrder(new String[] {
                "setActiveEditor", "selectionChanged", "run" }));
    }

    /**
     * @see IActionDelegateTest#createActionWidget()
     */
    protected Object createActionWidget() throws Throwable {
        editor = openEditor(fPage, "X");
        return editor;
    }

    /**
     * @see IActionDelegateTest#runAction()
     */
    protected void runAction(Object widget) throws Throwable {
        MockEditorPart editor = (MockEditorPart) widget;
        MockEditorActionBarContributor contributor = (MockEditorActionBarContributor) editor
                .getEditorSite().getActionBarContributor();
        IMenuManager mgr = contributor.getActionBars().getMenuManager();
        ActionUtil.runActionWithLabel(this, mgr, "Mock Action");
    }

    /**
     * @see IActionDelegateTest#fireSelection()
     */
    protected void fireSelection(Object widget) throws Throwable {
        MockEditorPart editor = (MockEditorPart) widget;
        editor.fireSelection();
    }

    protected MockEditorPart openEditor(IWorkbenchPage page, String suffix)
            throws Throwable {
        IProject proj = FileUtil.createProject("IEditorActionDelegateTest");
        IFile file = FileUtil.createFile("test" + suffix + ".txt", proj);
        return (MockEditorPart) page.openEditor(new FileEditorInput(file),
                EDITOR_ID);
    }

}

