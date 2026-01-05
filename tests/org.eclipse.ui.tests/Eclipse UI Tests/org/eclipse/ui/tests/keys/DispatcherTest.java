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
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.commands.CheckInvokedHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DispatcherTest {

	private KeyBindingDispatcher dispatcher;
	private IProject p;

	@BeforeEach
	public void doSetUp() throws CoreException {
		this.dispatcher = PlatformUI.getWorkbench().getService(KeyBindingDispatcher.class);
		CheckInvokedHandler.invoked = false;
		p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(new NullProgressMonitor());
		p.open(new NullProgressMonitor());
	}

	@AfterEach
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
			Assertions.assertFalse(CheckInvokedHandler.invoked, "No handler should have been invoked");
			Assertions.assertEquals(shellCount, Display.getCurrent().getShells().length, "No Shell should have been added");
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
			Assertions.assertTrue(CheckInvokedHandler.invoked, "Handler should have been invoked");
			Assertions.assertEquals(shellCount, Display.getCurrent().getShells().length, "No Shell should have been added");
		} finally {
			if (part != null) {
				part.getEditorSite().getPage().closeEditor(part, false);
			}
		}
	}

}
