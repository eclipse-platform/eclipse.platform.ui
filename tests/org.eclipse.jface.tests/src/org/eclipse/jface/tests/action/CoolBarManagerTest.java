/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * @since 3.6
 */
public class CoolBarManagerTest {

	private CoolBarManager coolBarManager;

	private CoolBar coolBar;

	@RegisterExtension
	public JFaceActionExtension rule = new JFaceActionExtension();

	@BeforeEach
	public void setUp() throws Exception {
		coolBarManager = new CoolBarManager(SWT.FLAT);
		coolBar = coolBarManager.createControl(rule.getShell());
	}

	@Test
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
