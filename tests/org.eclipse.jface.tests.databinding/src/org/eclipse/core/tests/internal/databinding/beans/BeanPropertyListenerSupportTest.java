/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.internal.databinding.beans.BeanPropertyListenerSupport;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 1.1
 */
public class BeanPropertyListenerSupportTest extends
		AbstractDefaultRealmTestCase {
	private PropertyChangeListenerStub listener;
	private String propertyName;

	protected void setUp() throws Exception {
		super.setUp();

		listener = new PropertyChangeListenerStub();
		propertyName = "value";
	}

	public void testAddPropertyChangeListenerWithPropertyName()
			throws Exception {
		SpecificListenerBean bean = new SpecificListenerBean();

		assertFalse(bean.changeSupport.hasListeners(propertyName));

		BeanPropertyListenerSupport.hookListener(bean, propertyName, listener);
		assertTrue("has listeners", bean.changeSupport
				.hasListeners(propertyName));
	}

	public void testAddPropertyChangeListenerWithoutPropertyName()
			throws Exception {
		GenericListenerBean bean = new GenericListenerBean();

		assertFalse(bean.changeSupport.hasListeners(propertyName));

		BeanPropertyListenerSupport.hookListener(bean, propertyName, listener);
		assertTrue("has listeners", bean.changeSupport
				.hasListeners(propertyName));
	}

	public void testLogStatusWhenAddPropertyChangeListenerMethodIsNotFound()
			throws Exception {
		class BeanStub {
		}

		class Log implements ILogger {
			int count;
			IStatus status;

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

	public void testLogStatusWhenRemovePropertyChangeListenerMethodIsNotFound()
			throws Exception {
		class InvalidBean {
		}

		class Log implements ILogger {
			int count;
			IStatus status;

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

		public void propertyChange(PropertyChangeEvent evt) {
			count++;
			this.event = evt;
		}
	}
}
