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
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.junit.Test;

public class TestUnitTextFactory extends AbstractFactoryTest {

	@Test
	public void createsText() {
		Text text = TextFactory.newText(SWT.MULTI).create(shell);

		assertEquals(shell, text.getParent());
		assertEquals(SWT.MULTI, text.getStyle() & SWT.MULTI);
	}

	@Test
	public void createsTextWithAllProperties() {
		final TypedEvent[] raisedEvents = new TypedEvent[3];
		Text text = TextFactory.newText(SWT.NONE).text("Test Text").message("message").limitTo(10)
				.onSelect(e -> raisedEvents[0] = e)
				.onModify(e -> raisedEvents[1] = e)
				.onVerify(e -> raisedEvents[2] = e)

				.create(shell);

		text.notifyListeners(SWT.Selection, new Event());
		text.notifyListeners(SWT.Modify, new Event());
		text.notifyListeners(SWT.Verify, new Event());

		assertEquals("Test Text", text.getText());
		assertEquals("message", text.getMessage());
		assertEquals(10, text.getTextLimit());

		assertEquals(1, text.getListeners(SWT.Selection).length);
		assertNotNull(raisedEvents[0]);

		assertEquals(1, text.getListeners(SWT.Modify).length);
		assertNotNull(raisedEvents[1]);

		assertEquals(1, text.getListeners(SWT.Verify).length);
		assertNotNull(raisedEvents[2]);
	}
}