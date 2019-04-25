/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation
 *     Matthew Hall - bug 195222
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.tests.internal.databinding.beans.BeanPropertyListenerSupportTest.GenericListenerBean;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class BeanValuePropertyTest extends AbstractDefaultRealmTestCase {
	@Test
	public void testChangeListenerIsOnlyNotifiedWhenWatchedPropertyChanges()
			throws Exception {
		GenericListenerBean bean = new GenericListenerBean();
		IValueProperty property = BeanProperties
				.value(GenericListenerBean.class, "value");
		class Listener implements IValueChangeListener {
			private int count = 0;

			@Override
			public void handleValueChange(ValueChangeEvent event) {
				count++;
			}
		}
		Listener listener = new Listener();

		IObservableValue observable = property.observe(new CurrentRealm(true), bean);
		observable.addValueChangeListener(listener);

		assertEquals(0, listener.count);
		bean.setValue("1");
		assertEquals(1, listener.count);

		bean.setOther("2");
		assertEquals(1, listener.count);
	}
}
