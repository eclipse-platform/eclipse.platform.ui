/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 173735)
 *     Matthew Hall - bug 262407
 *******************************************************************************/

package org.eclipse.core.tests.databinding.observable.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.DuplexingObservableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.0
 *
 */
public class DuplexingObservableValueTest extends AbstractDefaultRealmTestCase {
	private IObservableList list;
	private DuplexingObservableValue observable;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		list = new WritableList(new ArrayList(), String.class);
	}

	@Test
	public void testValueType_InheritFromTargetList() throws Exception {
		observable = new DuplexingObservableValue(list) {
			@Override
			protected Object coalesceElements(Collection elements) {
				return null;
			}
		};
		assertEquals(
				"value type should be the element type of the target list",
				String.class, observable.getValueType());
	}

	@Test
	public void testValueType_ProvidedInConstructor() throws Exception {
		observable = new DuplexingObservableValue(list, Object.class) {
			@Override
			protected Object coalesceElements(Collection elements) {
				return null;
			}
		};
		assertEquals("value type should be the type passed to constructor",
				Object.class, observable.getValueType());
	}

	@Test
	public void test_getValue() throws Exception {
		observable = DuplexingObservableValue.withDefaults(list, null,
				"<Multiple Values>");
		assertNull(observable.getValue());
		list.add("42");
		assertEquals("Value should be \"42\"", "42", observable.getValue());
		list.add("42");
		assertEquals("Value should be \"42\"", "42", observable.getValue());
		list.add("watermelon");
		assertEquals("<Multiple Values>", observable.getValue());
		list.remove(2);
		assertEquals("Value should be \"42\"", "42", observable.getValue());
		list.clear();
		assertNull(observable.getValue());
	}
}
