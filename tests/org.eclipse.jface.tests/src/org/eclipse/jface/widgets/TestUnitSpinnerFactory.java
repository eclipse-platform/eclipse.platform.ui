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
package org.eclipse.jface.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Spinner;
import org.junit.Test;

public class TestUnitSpinnerFactory extends AbstractFactoryTest {

	@Test
	public void createsSpinner() {
		Spinner spinner = SpinnerFactory.newSpinner(SWT.READ_ONLY).create(shell);

		assertEquals(shell, spinner.getParent());
		assertEquals(SWT.READ_ONLY, spinner.getStyle() & SWT.READ_ONLY);
	}

	@Test
	public void createsSpinnerWithAllProperties() {
		final TypedEvent[] raisedEvents = new TypedEvent[2];

		Spinner spinner = SpinnerFactory.newSpinner(SWT.NONE).bounds(20, 120).increment(2, 10).limitTo(3)
				.onSelect(e -> raisedEvents[0] = e).onModify(e -> raisedEvents[1] = e).create(shell);

		spinner.notifyListeners(SWT.Selection, new Event());
		spinner.notifyListeners(SWT.Modify, new Event());

		assertEquals(2, spinner.getIncrement());
		assertEquals(10, spinner.getPageIncrement());
		assertEquals(20, spinner.getMinimum());
		assertEquals(120, spinner.getMaximum());
		assertEquals(3, spinner.getTextLimit());

		assertEquals(1, spinner.getListeners(SWT.Selection).length);
		assertNotNull(raisedEvents[0]);

		assertEquals(1, spinner.getListeners(SWT.Modify).length);
		assertNotNull(raisedEvents[1]);
	}

}
