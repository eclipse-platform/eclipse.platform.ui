/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.action;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

public class ToolBarManagerTest extends JFaceActionTest {

	private static final int DEFAULT_STYLE = SWT.WRAP | SWT.FLAT | SWT.RIGHT;

	public ToolBarManagerTest(String name) {
		super(name);
	}

	public void testSetStyleWhenToolBarDoesNotExist() {
		Composite parent = createComposite();
		ToolBarManager manager = new ToolBarManager(DEFAULT_STYLE | SWT.HORIZONTAL);

		manager.setStyle( DEFAULT_STYLE | SWT.VERTICAL);
		ToolBar toolBar = manager.createControl(parent);
		assertFalse(toolBar.isDisposed());
		verifyOrientation(toolBar, SWT.VERTICAL);
	}

	public void testSetStyleWhenToolBarExists() {
		Composite parent = createComposite();
		ToolBar toolBar = new ToolBar(parent, DEFAULT_STYLE | SWT.VERTICAL);
		ToolBarManager manager = new ToolBarManager(toolBar);

		manager.setStyle(DEFAULT_STYLE | SWT.HORIZONTAL);
		assertSame(toolBar, manager.createControl(parent));
		assertFalse(toolBar.isDisposed());

		toolBar.dispose();
		ToolBar newToolBar = manager.createControl(parent);
		assertNotSame(toolBar, newToolBar);
		assertFalse(newToolBar.isDisposed());
		verifyOrientation(newToolBar, SWT.HORIZONTAL);
	}

	public void testCreateControlWhenParentNull() {
		Composite parent = createComposite();
		ToolBarManager manager = new ToolBarManager(DEFAULT_STYLE | SWT.VERTICAL);

		assertNull(manager.createControl(null));

		ToolBar toolBar = manager.createControl(parent);
		assertNotNull(toolBar);
		assertSame(toolBar, manager.createControl(null));
	}

	public void testDispose() {
		Composite parent = createComposite();
		ToolBar toolBar = new ToolBar(parent, DEFAULT_STYLE | SWT.VERTICAL);
		ToolBarManager manager = new ToolBarManager(toolBar);

		manager.dispose();
		assertTrue(toolBar.isDisposed());
	}


	private Composite createComposite() {
		return new Composite(getShell(), SWT.DEFAULT);
	}

	private void verifyOrientation(ToolBar toolBar, int expected) {
		assertTrue((toolBar.getStyle() & expected) != 0);

		int opposite = (expected & SWT.HORIZONTAL) != 0 ? SWT.VERTICAL : SWT.HORIZONTAL;
		assertFalse((toolBar.getStyle() & opposite) != 0);
	}

}
