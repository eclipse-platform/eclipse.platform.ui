/*******************************************************************************
* Copyright (c) 2018 SAP SE and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     SAP SE - initial version
******************************************************************************/
package org.eclipse.jface.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.junit.Test;

public class TestUnitButtonFactory extends AbstractFactoryTest {

	@Test
	public void createsButton() {
		Button button = ButtonFactory.newButton(SWT.PUSH).create(shell);

		assertEquals(shell, button.getParent());
		assertEquals(SWT.PUSH, button.getStyle() & SWT.PUSH);
	}

	@Test
	public void createsButtonWithAllProperties() {
		final SelectionEvent[] raisedEvents = new SelectionEvent[1];
		Button button = ButtonFactory.newButton(SWT.NONE).text("Test Button").image(image)
				.onSelect(e -> raisedEvents[0] = e).create(shell);

		button.notifyListeners(SWT.Selection, new Event());

		assertEquals("Test Button", button.getText());
		assertEquals(image, button.getImage());

		assertEquals(1, button.getListeners(SWT.Selection).length);
		assertNotNull(raisedEvents[0]);
	}
}