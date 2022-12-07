/*******************************************************************************
 * Copyright (c) 2017, Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.)
 ******************************************************************************/
package org.eclipse.ui.tests.keys;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.commands.CheckInvokedHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DispatcherTest {

	private KeyBindingDispatcher dispatcher;
	private IProject p;

	@Before
	public void doSetUp() throws CoreException {
		this.dispatcher = PlatformUI.getWorkbench().getService(KeyBindingDispatcher.class);
		CheckInvokedHandler.invoked = false;
		p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(new NullProgressMonitor());
		p.open(new NullProgressMonitor());
	}

	@After
	public void doTearDown() throws Exception {
		p.delete(true, new NullProgressMonitor());
	}

	@Test
	public void testDispatcherMultipleDisabledCommands() throws Exception {
		IFile file = p.getFile("test.whatever");
		try (ByteArrayInputStream stream = new ByteArrayInputStream("hello".getBytes())) {
			file.create(stream, true, new NullProgressMonitor());
		}
		IEditorPart part = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		try {
			int shellCount = Display.getCurrent().getShells().length;
			dispatcher.press(Arrays.asList(KeyStroke.getInstance(SWT.CTRL, 'O')), null);
			Assert.assertFalse("No handler should have been invoked", CheckInvokedHandler.invoked);
			Assert.assertEquals("No Shell should have been added", shellCount, Display.getCurrent().getShells().length);
		} finally {
			if (part != null) {
				part.getEditorSite().getPage().closeEditor(part, false);
			}
		}
	}

	@Test
	public void testDispatcherMultipleCommandsOnlyOneEnabled() throws Exception {
		IFile file = p.getFile("test.content-type2");
		try (ByteArrayInputStream stream = new ByteArrayInputStream("hello".getBytes())) {
			file.create(stream, true, new NullProgressMonitor());
		}
		IEditorPart part = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		try {
			int shellCount = Display.getCurrent().getShells().length;
			dispatcher.press(Arrays.asList(KeyStroke.getInstance(SWT.CTRL, 'O')), null);
			Assert.assertTrue("Handler should have been invoked", CheckInvokedHandler.invoked);
			Assert.assertEquals("No Shell should have been added", shellCount, Display.getCurrent().getShells().length);
		} finally {
			if (part != null) {
				part.getEditorSite().getPage().closeEditor(part, false);
			}
		}
	}

	@Test
	public void testSelectionVariableHandlerConflicts() throws Exception {
		IFile file = p.getFile("test.whatever");
		try (ByteArrayInputStream stream = new ByteArrayInputStream("hello".getBytes())) {
			file.create(stream, true, new NullProgressMonitor());
		}
		CheckInvokedHandler.invoked = false;
		IViewPart projectExplorer = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView(IPageLayout.ID_PROJECT_EXPLORER);
		projectExplorer.getSite().getSelectionProvider().setSelection(new StructuredSelection(file));
		ISelection selection = projectExplorer.getSite().getSelectionProvider().getSelection();

		// select a file in the project explorer and assert that CTRL+C invokes our test
		// handler
		{
			dispatcher.press(Arrays.asList(KeyStroke.getInstance(SWT.CTRL, 'C')), null);
			// in the Project Explorer, the custom copy handler should have been called
			Assert.assertTrue("Handler should have been invoked", CheckInvokedHandler.invoked);
		}

		// invalidate the 'selection' variable and trigger copy operation again
		{
			// simulate that some user or platform code triggered a change in the evaluation
			// context
			// in real scenario, this would be opening a new Shell, for example
			{
				IEclipseContext ectx = projectExplorer.getSite().getService(IEclipseContext.class);
				ectx.set(ISources.ACTIVE_CURRENT_SELECTION_NAME, null);
			}

			// the original selection should not have changed
			Assert.assertSame(selection, projectExplorer.getSite().getSelectionProvider().getSelection());

			CheckInvokedHandler.invoked = false;
			dispatcher.press(Arrays.asList(KeyStroke.getInstance(SWT.CTRL, 'C')), null);

			// our handler was not called, assume the key binding has been dispatched to
			// another handler
			Assert.assertFalse("Handler should NOT have been invoked", CheckInvokedHandler.invoked);
		}
	}
}
