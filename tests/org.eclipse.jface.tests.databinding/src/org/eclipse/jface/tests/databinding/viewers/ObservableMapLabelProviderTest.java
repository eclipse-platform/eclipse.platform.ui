/*******************************************************************************
 * Copyright (c) 2006, 2007 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 164247
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.viewers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.examples.databinding.ModelObject;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

/**
 * @since 1.1
 */
public class ObservableMapLabelProviderTest extends AbstractDefaultRealmTestCase {

	@Test
	public void testGetColumnText() throws Exception {
		WritableSet<Item> set = new WritableSet<>(new HashSet<>(), Item.class);
		Item item = new Item();
		String value = "value";
		item.setValue(value);
		set.add(item);

		ObservableMapLabelProvider labelProvider = new ObservableMapLabelProvider(
				BeanProperties.value(Item.class, "value").observeDetail(set));
		assertEquals(item.getValue(), labelProvider.getColumnText(item, 0));
	}

	@Test
	public void testGetColumnTextNullValue() throws Exception {
		WritableSet<Item> set = new WritableSet<>(new HashSet<>(), Item.class);
		Item item = new Item();
		set.add(item);

		ObservableMapLabelProvider labelProvider = new ObservableMapLabelProvider(
				BeanProperties.value(Item.class, "value").observeDetail(set));
		assertNull(item.getValue());
		assertEquals("", labelProvider.getColumnText(item, 0));
	}

	private static class Item extends ModelObject {
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			String old = this.value;

			firePropertyChange("value", old, this.value = value);
		}
	}
}
