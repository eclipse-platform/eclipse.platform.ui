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
 *     Matthew Hall - initial API and implementation (bug 268336)
 *******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.internal.databinding.beans.BeanPropertyListener;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.1
 */
public class BeanPropertyListenerTest extends AbstractDefaultRealmTestCase {
	private PropertyStub property;
	private PropertyDescriptor propertyDescriptor;
	private SimplePropertyListenerStub simpleListener;
	private BeanPropertyListenerStub listener;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		property = new PropertyStub();
		propertyDescriptor = new PropertyDescriptor("value", Bean.class);
		simpleListener = new SimplePropertyListenerStub();
		listener = new BeanPropertyListenerStub(property, propertyDescriptor,
				simpleListener);
	}

	@Test
	public void testPropertyChange_ExpectedPropertyName() {
		Object source = new Object();
		String propertyName = "value";
		Object oldValue = new Object();
		Object newValue = new Object();
		listener.propertyChange(new PropertyChangeEvent(source, propertyName,
				oldValue, newValue));

		SimplePropertyEvent expectedEvent = new SimplePropertyEvent(
				SimplePropertyEvent.CHANGE, source, property, Diffs
						.createValueDiff(oldValue, newValue));
		assertEquals(Collections.singletonList(expectedEvent),
				simpleListener.log);
	}

	@Test
	public void testPropertyChange_OtherPropertyName() {
		Object source = new Object();
		String propertyName = "other";
		Object oldValue = new Object();
		Object newValue = new Object();
		listener.propertyChange(new PropertyChangeEvent(source, propertyName,
				oldValue, newValue));

		assertEquals(Collections.EMPTY_LIST, simpleListener.log);
	}

	@Test
	public void testPropertyChange_NullPropertyName() {
		Object source = new Object();
		String propertyName = null;
		Object oldValue = null;
		Object newValue = null;
		listener.propertyChange(new PropertyChangeEvent(source, propertyName,
				oldValue, newValue));

		SimplePropertyEvent expectedEvent = new SimplePropertyEvent(
				SimplePropertyEvent.CHANGE, source, property, null);
		assertEquals(Collections.singletonList(expectedEvent),
				simpleListener.log);
	}

	@Test
	public void testPropertyChange_NullPropertyName_IgnoreOldAndNewValues() {
		Object source = new Object();
		String propertyName = null;
		Object oldValue = new Object();
		Object newValue = new Object();
		listener.propertyChange(new PropertyChangeEvent(source, propertyName,
				oldValue, newValue));

		SimplePropertyEvent expectedEvent = new SimplePropertyEvent(
				SimplePropertyEvent.CHANGE, source, property, null);
		assertEquals(Collections.singletonList(expectedEvent),
				simpleListener.log);
	}

	private static class PropertyStub implements IProperty {
	}

	private static class SimplePropertyListenerStub implements
			ISimplePropertyListener {
		public List log = new ArrayList();

		@Override
		public void handleEvent(SimplePropertyEvent event) {
			log.add(event);
		}
	}

	private static class BeanPropertyListenerStub extends BeanPropertyListener {
		BeanPropertyListenerStub(IProperty property,
				PropertyDescriptor propertyDescriptor,
				ISimplePropertyListener listener) {
			super(property, propertyDescriptor, listener);
		}

		@Override
		protected IDiff computeDiff(Object oldValue, Object newValue) {
			return Diffs.createValueDiff(oldValue, newValue);
		}
	}
}
