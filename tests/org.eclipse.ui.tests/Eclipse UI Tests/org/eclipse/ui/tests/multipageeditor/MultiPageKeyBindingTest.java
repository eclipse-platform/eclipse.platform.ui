/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.multipageeditor;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.util.UITestCase;

/**
 * Tests that key bindings are treated correctly in a multi-page editor. This
 * ensures that key bindings are switched at the granularity of a page in a
 * multi-page editor. See Bug 37612 for references.
 * 
 * @since 3.0
 */
public class MultiPageKeyBindingTest extends UITestCase {

    /**
     * Constructs a new instance of <code>MultiPageKeyBindingTest</code>.
     * 
     * @param name
     *            The name of the test to be run.
     */
    public MultiPageKeyBindingTest(String name) {
        super(name);
    }

    public void testSwitch() throws CoreException {
        final String extension = "multi"; //$NON-NLS-1$
        final String fileName = "A." + extension; //$NON-NLS-1$

        // Open a new test window.
        IWorkbenchWindow window = openTestWindow();

        // Create a blurb file.
        IWorkbenchPage page = window.getActivePage();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject testProject = workspace.getRoot().getProject(
                "MultiPageKeyBindingTest Project"); //$NON-NLS-1$
        testProject.create(null);
        testProject.open(null);
        IFile multiFile = testProject.getFile(fileName);
        multiFile.create(new ByteArrayInputStream(new byte[0]), true, null);

        // Open a blurb file.
        IEditorInput editorInput = new FileEditorInput(multiFile);
        IEditorPart editorPart = page.openEditor(editorInput,
                "org.eclipse.ui.tests.multipageeditor.TestMultiPageEditor"); //$NON-NLS-1$
        TestMultiPageEditor multiPageEditorPart = (TestMultiPageEditor) editorPart;

        // Switch to the second tab.
        multiPageEditorPart.setPage(1);

        // Check that "Ctrl+Shift+5" is the bound key.
        IWorkbenchCommandSupport commandSupport = window.getWorkbench()
                .getCommandSupport();
        ICommandManager commandManager = commandSupport.getCommandManager();
        ICommand command = commandManager
                .getCommand("org.eclipse.ui.tests.TestCommandId"); //$NON-NLS-1$
        List keySequenceBindings = command.getKeySequenceBindings();
        System.out.println("Key sequence bindings = " + keySequenceBindings);
        //		final int expectedValue = (SWT.CTRL | SWT.SHIFT | '5');
    }
}
