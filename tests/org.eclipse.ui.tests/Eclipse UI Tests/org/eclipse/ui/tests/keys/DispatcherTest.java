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
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.tests.commands.CheckInvokedHandler;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class DispatcherTest extends UITestCase {

	private KeyBindingDispatcher dispatcher;
	private IProject p;

	@Override
	public void doSetUp() throws CoreException {
		this.dispatcher = getWorkbench().getService(KeyBindingDispatcher.class);
		CheckInvokedHandler.invoked = false;
		p = ResourcesPlugin.getWorkspace().getRoot().getProject(getClass().getName() + System.currentTimeMillis());
		p.create(new NullProgressMonitor());
		p.open(new NullProgressMonitor());
	}

	@Override
	protected void doTearDown() throws Exception {
		p.delete(true, new NullProgressMonitor());
	}

	public DispatcherTest() {
		super(DispatcherTest.class.getSimpleName());
	}

	@Test
	public void testDispatcherMultipleDisabledCommands() throws Exception {
		IFile file = p.getFile("test.whatever");
		try (ByteArrayInputStream stream = new ByteArrayInputStream("hello".getBytes())) {
			file.create(stream, true, new NullProgressMonitor());
		}
		IEditorPart part = IDE.openEditor(getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		try {
			int shellCount = Display.getCurrent().getShells().length;
			dispatcher.press(Arrays.asList(new KeyStroke[] { KeyStroke.getInstance(SWT.CTRL, 'O') }), null);
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
		IEditorPart part = IDE.openEditor(getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		try {
			int shellCount = Display.getCurrent().getShells().length;
			dispatcher.press(Arrays.asList(new KeyStroke[] { KeyStroke.getInstance(SWT.CTRL, 'O') }), null);
			Assert.assertTrue("Handler should have been invoked", CheckInvokedHandler.invoked);
			Assert.assertEquals("No Shell should have been added", shellCount, Display.getCurrent().getShells().length);
		} finally {
			if (part != null) {
				part.getEditorSite().getPage().closeEditor(part, false);
			}
		}
	}

}
