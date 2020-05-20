/*******************************************************************************
 * Copyright (c) 2020 Jens Lidestrom and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Lidestrom - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.LocalTime;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;
import org.junit.Test;

/**
 */
public class LocalTimeSelectionProperty extends AbstractSWTTestCase {
	@Test
	public void testSetInObservable() {
		DateTime control = new DateTime(getShell(), SWT.TIME);
		IObservableValue<LocalTime> time = WidgetProperties.localTimeSelection().observe(control);
		ValueChangeEventTracker<LocalTime> tracker = new ValueChangeEventTracker<>();

		time.addValueChangeListener(tracker);
		time.setValue(LocalTime.of(11, 22, 33));

		assertEquals(1, tracker.count);

		assertEquals(11, control.getHours());
		assertEquals(22, control.getMinutes());
		assertEquals(33, control.getSeconds());
	}

	@Test
	public void testSetInControl() {
		DateTime control = new DateTime(getShell(), SWT.TIME);
		IObservableValue<LocalTime> time = WidgetProperties.localTimeSelection().observe(control);
		ValueChangeEventTracker<LocalTime> tracker = new ValueChangeEventTracker<>();
		time.addValueChangeListener(tracker);

		control.setHours(11);
		control.setMinutes(22);
		control.setSeconds(33);

		control.notifyListeners(SWT.Selection, null);

		assertEquals(1, tracker.count);
		assertEquals(LocalTime.of(11, 22, 33), time.getValue());
	}

	@Test
	public void testWrongKind() {
		DateTime control = new DateTime(getShell(), SWT.DATE);
		IObservableValue<LocalTime> time = WidgetProperties.localTimeSelection().observe(control);
		try {
			time.setValue(LocalTime.of(11, 11, 11));
			fail();
		} catch (IllegalStateException exc) {
			// Expected
		}
	}

	@Test
	public void testType() {
		assertEquals(LocalTime.class, WidgetProperties.localTimeSelection().getValueType());
	}

	@Test
	public void testNullNotThrowingNullPointerException() {
		DateTime control = new DateTime(getShell(), SWT.TIME);

		try {
			WidgetProperties.localTimeSelection().setValue(control, null);
		} catch (NullPointerException notExpected) {
			fail("No NPE should be thrown, because a null value should cause the method to return silently");
		}
	}
}
