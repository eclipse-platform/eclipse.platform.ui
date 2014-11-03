/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;

/**
 * @since 3.6
 */
public class CoolBarManagerTest extends JFaceActionTest {

	private CoolBarManager coolBarManager;

	private CoolBar coolBar;

	public CoolBarManagerTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		coolBarManager = new CoolBarManager(SWT.FLAT);
		coolBar = coolBarManager.createControl(getShell());
	}

	public void testResetItemOrderBug293433() {
		IToolBarManager manager = new ToolBarManager();
		manager.add(new Action() {
		});
		coolBarManager.add(manager);
		coolBarManager.update(true);

		CoolItem[] items = coolBar.getItems();
		assertEquals(1, items.length);

		Control control = items[0].getControl();

		// reset causes items to be disposed
		coolBarManager.resetItemOrder();

		// ensure that the control was actually disposed
		assertTrue(control.isDisposed());
	}
}
