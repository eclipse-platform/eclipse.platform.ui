/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.internal.databinding.beans.BeanPropertyListenerSupport;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.1
 */
public class BeanPropertyListenerSupportTest extends
		AbstractDefaultRealmTestCase {
	private PropertyChangeListenerStub listener;
	private String propertyName;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		listener = new PropertyChangeListenerStub();
		propertyName = "value";
	}

	@Test
	public void testAddPropertyChangeListenerWithPropertyName()
			throws Exception {
		SpecificListenerBean bean = new SpecificListenerBean();

		assertFalse(bean.changeSupport.hasListeners(propertyName));

		BeanPropertyListenerSupport.hookListener(bean, propertyName, listener);
		assertTrue("has listeners", bean.changeSupport
				.hasListeners(propertyName));
	}

	@Test
	public void testAddPropertyChangeListenerWithoutPropertyName()
			throws Exception {
		GenericListenerBean bean = new GenericListenerBean();

		assertFalse(bean.changeSupport.hasListeners(propertyName));

		BeanPropertyListenerSupport.hookListener(bean, propertyName, listener);
		assertTrue("has listeners", bean.changeSupport
				.hasListeners(propertyName));
	}

	@Test
	public void testLogStatusWhenAddPropertyChangeListenerMethodIsNotFound()
			throws Exception {
		class BeanStub {
		}

		class Log implements ILogger {
			int count;
			IStatus status;

			@Override
			public void log(IStatus status) {
				count++;
				this.status = status;
			}
		}

		Log log = new Log();
		Policy.setLog(log);

		BeanStub bean = new BeanStub();

		assertEquals(0, log.count);
		BeanPropertyListenerSupport.hookListener(bean, "value", listener);
		assertEquals(1, log.count);
		assertEquals(IStatus.WARNING, log.status.getSeverity());
	}

	@Test
	public void testRemovePropertyChangeListenerWithPropertyName()
			throws Exception {
		SpecificListenerBean bean = new SpecificListenerBean();
		BeanPropertyListenerSupport.hookListener(bean, propertyName, listener);

		assertTrue(bean.changeSupport.hasListeners(propertyName));

		BeanPropertyListenerSupport
				.unhookListener(bean, propertyName, listener);
		assertFalse("has listeners", bean.changeSupport
				.hasListeners(propertyName));
	}

	@Test
	public void testRemovePropertyChangeListenerWithoutPropertyName()
			throws Exception {
		GenericListenerBean bean = new GenericListenerBean();
		BeanPropertyListenerSupport.hookListener(bean, propertyName, listener);

		assertTrue(bean.changeSupport.hasListeners(propertyName));

		BeanPropertyListenerSupport
				.unhookListener(bean, propertyName, listener);
		assertFalse("has listeners", bean.changeSupport
				.hasListeners(propertyName));
	}

	@Test
	public void testLogStatusWhenRemovePropertyChangeListenerMethodIsNotFound()
			throws Exception {
		class InvalidBean {
		}

		class Log implements ILogger {
			int count;
			IStatus status;

			@Override
			public void log(IStatus status) {
				count++;
				this.status = status;
			}
		}

		Log log = new Log();
		Policy.setLog(log);

		InvalidBean bean = new InvalidBean();

		BeanPropertyListenerSupport.hookListener(bean, "value", listener);
		log.count = 0;
		log.status = null;
		assertEquals(0, log.count);
		BeanPropertyListenerSupport.unhookListener(bean, "value", listener);
		assertEquals(1, log.count);
		assertEquals(IStatus.WARNING, log.status.getSeverity());
	}

	static class GenericListenerBean {
		private String other;
		PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			changeSupport.firePropertyChange("value", this.value,
					this.value = value);
		}

		public String getOther() {
			return other;
		}

		public void setOther(String other) {
			changeSupport.firePropertyChange("other", this.other,
					this.other = other);
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.removePropertyChangeListener(listener);
		}
	}

	static class SpecificListenerBean {
		PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
		String propertyName;
		String value;

		public void addPropertyChangeListener(String name,
				PropertyChangeListener listener) {
			this.propertyName = name;
			changeSupport.addPropertyChangeListener(name, listener);
		}

		public void removePropertyChangeListener(String name,
				PropertyChangeListener listener) {
			changeSupport.removePropertyChangeListener(name, listener);
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	static class PropertyChangeListenerStub implements PropertyChangeListener {
		PropertyChangeEvent event;
		int count;

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			count++;
			this.event = evt;
		}
	}
}
