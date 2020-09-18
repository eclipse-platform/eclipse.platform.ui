/*******************************************************************************
* Copyright (c) 2020 vogella GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Lars Vogel - initial version
******************************************************************************/
package org.eclipse.jface.tests.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jface.widgets.DateTimeFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.junit.Test;

public class TestUnitDateTimeFactory extends AbstractFactoryTest {

	@Test
	public void createsDateTime() {
		DateTime dateTime = DateTimeFactory.newDateTime(SWT.BORDER).create(shell);

		assertEquals(shell, dateTime.getParent());
		assertEquals(SWT.BORDER, dateTime.getStyle() & SWT.BORDER);
	}

	@Test
	public void createDateTimeWithAllProperties() {
		final SelectionEvent[] raisedEvents = new SelectionEvent[2];
		DateTime dateTime = DateTimeFactory.newDateTime(SWT.BORDER).onSelect(e -> raisedEvents[0] = e)
				.onDefaultSelect(e -> raisedEvents[1] = e).create(shell);

		dateTime.notifyListeners(SWT.Selection, new Event());

		assertEquals(1, dateTime.getListeners(SWT.Selection).length);
		assertEquals(1, dateTime.getListeners(SWT.DefaultSelection).length);
		assertNotNull(raisedEvents[0]);
		assertNotNull(raisedEvents[1]);
	}
}