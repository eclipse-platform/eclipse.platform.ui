/*******************************************************************************
 * Copyright (c) 2018 Jens Lidestrom and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.jface.tests.databinding.viewers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.ViewerCell;
import org.junit.Test;

/**
 * Tests {@link ObservableMapCellLabelProvider}.
 */
public class ObservableMapCellLabelProviderTest extends AbstractDefaultRealmTestCase {
	@Test
	public void testUpdateWithValue() {
		Object key = new Object();
		WritableMap<Object, Object> m = new WritableMap<>();
		m.put(key, "value");
		ObservableMapCellLabelProvider labelProvider = new ObservableMapCellLabelProvider(m);

		ViewerCell cell = mock(ViewerCell.class);
		when(cell.getElement()).thenReturn(key);

		// Return a large arbitrary value, it should not affect the result
		when(cell.getColumnIndex()).thenReturn(17);

		labelProvider.update(cell);

		verify(cell).setText("value");
	}

	@Test
	public void testUpdateNullValue() {
		Object key = new Object();
		WritableMap<Object, Object> m = new WritableMap<>();
		m.put(key, null);
		ObservableMapCellLabelProvider labelProvider = new ObservableMapCellLabelProvider(m);

		ViewerCell cell = mock(ViewerCell.class);
		when(cell.getElement()).thenReturn(key);

		labelProvider.update(cell);

		verify(cell).setText("");
	}

	@Test
	public void testListenerNotification() throws Exception {
		List<LabelProviderChangedEvent> events = new ArrayList<>();

		WritableMap<Object, Object> m1 = new WritableMap<>();
		WritableMap<Object, Object> m2 = new WritableMap<>();
		Object key1 = new Object();
		Object key2 = new Object();

		// Extend class to test with multiple maps
		class ObservableMapCellLabelProviderExtension extends ObservableMapCellLabelProvider {
			public ObservableMapCellLabelProviderExtension(IObservableMap<?, ?>... maps) {
				super(maps);
			}
		}

		ObservableMapCellLabelProvider labelProvider = new ObservableMapCellLabelProviderExtension(m1, m2);

		labelProvider.addListener(e -> events.add(e));

		// Add an element an verify response
		m1.put(key1, "value1");

		assertEquals(1, events.size());
		assertEquals(labelProvider, events.get(0).getSource());

		// When a key is added there is no element in the event
		// (Note: The author of this test in not sure whether this is the
		// correct behavior.)
		assertTrue(events.get(0).getElements() == null || events.get(0).getElements().length == 0);

		// Change value and verify that the right element gets an update
		m1.put(key1, "value2");

		assertEquals(2, events.size());

		// When a key is changed there is an element in the event
		assertEquals(1, events.get(1).getElements().length);
		assertEquals(key1, events.get(1).getElements()[0]);

		// Add another key and verify that first key don't get an update
		m1.put(key2, "value3");

		assertEquals(3, events.size());
		assertEquals(labelProvider, events.get(2).getSource());
		assertTrue(events.get(2).getElements() == null || events.get(2).getElements().length == 0);
	}
}
