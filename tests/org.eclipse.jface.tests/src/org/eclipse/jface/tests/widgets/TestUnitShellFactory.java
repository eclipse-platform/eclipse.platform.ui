/*******************************************************************************
 * Copyright (c) 2019 Marcus Hoepfner and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marcus Hoepfner - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.widgets.ShellFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class TestUnitShellFactory extends AbstractFactoryTest {

	@Test
	public void createsShell() {
		Shell myShell = ShellFactory.newShell(SWT.BORDER).create(shell);
		assertEquals(shell, myShell.getParent());
	}

	@Test
	public void createsShellWithAllProperties() {
		Shell myShell = ShellFactory.newShell(SWT.BORDER).text("Test").enabled(false).create(shell);

		assertEquals("Test", myShell.getText());
		assertFalse(myShell.getEnabled());
	}

	@Test
	public void createsShellWithMenuBar() {
		final Menu[] menu = new Menu[1];
		Shell myShell = ShellFactory.newShell(SWT.BORDER).menuBar(shell -> {
			menu[0] = new Menu(shell, SWT.BAR);
			return menu[0];
		}).create(shell);

		assertEquals(menu[0], myShell.getMenuBar());
	}

	@Test
	public void createsMinimizedShell() {
		Shell myShell = ShellFactory.newShell(SWT.BORDER).minimized(true).create(shell);

		assertTrue(myShell.getMinimized());
	}

	@Test
	public void createsMaximizedShell() {
		Shell myShell = ShellFactory.newShell(SWT.BORDER).maximized(true).create(shell);

		assertTrue(myShell.getMaximized());
	}

	@Test
	public void createsFullScreenShell() {
		Shell myShell = ShellFactory.newShell(SWT.BORDER).fullScreen(true).create(shell);

		assertTrue(myShell.getFullScreen());
	}

	@Test
	public void addsShellActivatedListener() {
		final ShellEvent[] raisedEvents = new ShellEvent[1];
		Shell myShell = ShellFactory.newShell(SWT.BORDER).onActivate(e -> raisedEvents[0] = e).create(shell);
		myShell.notifyListeners(SWT.Activate, new Event());

		assertEquals(1, myShell.getListeners(SWT.Activate).length);
		assertNotNull(raisedEvents[0]);
	}

	@Test
	public void addsShellDeactivatedListener() {
		final ShellEvent[] raisedEvents = new ShellEvent[1];
		Shell myShell = ShellFactory.newShell(SWT.BORDER).onDeactivate(e -> raisedEvents[0] = e).create(shell);
		myShell.notifyListeners(SWT.Deactivate, new Event());

		assertEquals(1, myShell.getListeners(SWT.Deactivate).length);
		assertNotNull(raisedEvents[0]);
	}

	@Test
	public void addsShellIconfiedListener() {
		final ShellEvent[] raisedEvents = new ShellEvent[1];
		Shell myShell = ShellFactory.newShell(SWT.BORDER).onIconify(e -> raisedEvents[0] = e).create(shell);
		myShell.notifyListeners(SWT.Iconify, new Event());

		assertEquals(1, myShell.getListeners(SWT.Iconify).length);
		assertNotNull(raisedEvents[0]);
	}

	@Test
	public void addsShellDeiconfiedListener() {
		final ShellEvent[] raisedEvents = new ShellEvent[1];
		Shell myShell = ShellFactory.newShell(SWT.BORDER).onDeiconify(e -> raisedEvents[0] = e).create(shell);
		myShell.notifyListeners(SWT.Deiconify, new Event());

		assertEquals(1, myShell.getListeners(SWT.Deiconify).length);
		assertNotNull(raisedEvents[0]);
	}

	@Test
	public void addsShellClosedListener() {
		final ShellEvent[] raisedEvents = new ShellEvent[1];
		Shell myShell = ShellFactory.newShell(SWT.BORDER).onClose(e -> raisedEvents[0] = e).create(shell);
		myShell.notifyListeners(SWT.Close, new Event());

		assertEquals(1, myShell.getListeners(SWT.Close).length);
		assertNotNull(raisedEvents[0]);
	}
}