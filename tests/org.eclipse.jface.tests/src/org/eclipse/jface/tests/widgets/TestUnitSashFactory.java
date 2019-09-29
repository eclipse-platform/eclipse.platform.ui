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
import static org.junit.Assert.assertNotNull;

import org.eclipse.jface.widgets.SashFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Sash;
import org.junit.Test;

public class TestUnitSashFactory extends AbstractFactoryTest {

	@Test
	public void createSash() {
		Sash sash = SashFactory.newSash(SWT.HORIZONTAL).create(shell);

		assertEquals(shell, sash.getParent());
		assertEquals(SWT.HORIZONTAL, sash.getStyle() & SWT.HORIZONTAL);
	}

	@Test
	public void addSelectionListener() {
		final SelectionEvent[] raisedEvents = new SelectionEvent[1];

		Sash sash = SashFactory.newSash(SWT.HORIZONTAL).onSelect(e -> raisedEvents[0] = e).create(shell);

		sash.notifyListeners(SWT.Selection, new Event());
		assertEquals(1, sash.getListeners(SWT.Selection).length);
		assertNotNull(raisedEvents[0]);
	}
}