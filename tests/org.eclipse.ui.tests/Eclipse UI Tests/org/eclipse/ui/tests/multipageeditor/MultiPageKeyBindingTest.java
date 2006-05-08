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
package org.eclipse.ui.tests.multipageeditor;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.ParseException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.UITestCase;

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

    /**
     * Tests that the key bindings are updated when the page is switched in a
     * multi-page editor part.
     * 
     * @throws CoreException
     *             If the project or file cannot be created.
     * @throws ParseException
     *             The expected key sequence cannot be parsed.
     */
    public void testSwitch() throws CoreException, ParseException {
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

        // Switch to the second tab
        window.getShell().forceActive();
        Display display = Display.getCurrent();
        while (display.readAndDispatch())
            ;
        multiPageEditorPart.setPage(1);

        // Check that "Ctrl+Shift+5" is the bound key.
        IWorkbenchCommandSupport commandSupport = window.getWorkbench()
                .getCommandSupport();
        ICommandManager commandManager = commandSupport.getCommandManager();
        KeySequence expectedKeyBinding = KeySequence
                .getInstance("Ctrl+Shift+5"); //$NON-NLS-1$
        String commandId = commandManager.getPerfectMatch(expectedKeyBinding);
        assertEquals("org.eclipse.ui.tests.TestCommandId", commandId); //$NON-NLS-1$
    }
}
