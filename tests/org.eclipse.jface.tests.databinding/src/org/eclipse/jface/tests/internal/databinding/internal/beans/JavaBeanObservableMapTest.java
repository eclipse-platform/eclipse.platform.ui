/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableMap;
import org.eclipse.jface.tests.databinding.observable.ThreadRealm;

/**
 * @since 3.2
 * 
 */
public class JavaBeanObservableMapTest extends TestCase {
	private Model model1;

	private Model model2;

	private WritableSet set;

	private PropertyDescriptor propertyDescriptor;

	private JavaBeanObservableMap map;

	protected void setUp() throws Exception {
		ThreadRealm realm = new ThreadRealm();
		realm.init(Thread.currentThread());
		model1 = new Model("1");
		model2 = new Model("2");

		set = new WritableSet(realm, Model.class);
		set.add(model1);
		set.add(model2);

		propertyDescriptor = new PropertyDescriptor("value", Model.class);
		map = new JavaBeanObservableMap(set, propertyDescriptor);
	}

	public void testGetValue() throws Exception {
		assertEquals(
				"The 'value' from the map should be the value of the property of the model.",
				model1.getValue(), map.get(model1));
	}

	public void testSetValueNotifications() throws Exception {
		String oldValue = model1.getValue();
		String newValue = model1.getValue() + model1.getValue();
		MapChangeListener listener = new MapChangeListener();

		map.addMapChangeListener(listener);
		assertEquals(0, listener.count);
		model1.setValue(newValue);
		assertEquals(1, listener.count);
		assertTrue(listener.diff.getChangedKeys().contains(model1));
		assertEquals(newValue, listener.diff.getNewValue(model1));
		assertEquals(oldValue, listener.diff.getOldValue(model1));
		assertFalse(listener.diff.getAddedKeys().contains(model1));
		assertFalse(listener.diff.getRemovedKeys().contains(model1));
	}

	public void testPutValue() throws Exception {
		String oldValue = model1.getValue();
		String newValue = model1.getValue() + model1.getValue();
		MapChangeListener listener = new MapChangeListener();
		map.addMapChangeListener(listener);

		assertEquals(0, listener.count);
		map.put(model1, newValue);
		assertEquals(1, listener.count);
		assertEquals(newValue, model1.getValue());
		assertEquals(oldValue, listener.diff.getOldValue(model1));
		assertEquals(newValue, listener.diff.getNewValue(model1));
		assertFalse(listener.diff.getAddedKeys().contains(model1));
		assertTrue(listener.diff.getChangedKeys().contains(model1));
		assertFalse(listener.diff.getRemovedKeys().contains(model1));
	}

	public void testAddKey() throws Exception {
		MapChangeListener listener = new MapChangeListener();
		map.addMapChangeListener(listener);

		Model model3 = new Model("3");

		assertEquals(0, listener.count);
		set.add(model3);
		assertEquals(1, listener.count);
		assertTrue(listener.diff.getAddedKeys().contains(model3));
		assertEquals(model3.getValue(), map.get(model3));

		String newValue = model3.getValue() + model3.getValue();
		model3.setValue(newValue);
		assertEquals(2, listener.count);
		assertEquals(3, map.size());
	}

	public void testRemoveKey() throws Exception {
		MapChangeListener listener = new MapChangeListener();
		map.addMapChangeListener(listener);

		assertEquals(0, listener.count);
		set.remove(model1);
		assertEquals(1, listener.count);
		assertFalse(listener.diff.getAddedKeys().contains(model1));
		assertFalse(listener.diff.getChangedKeys().contains(model1));
		assertTrue(listener.diff.getRemovedKeys().contains(model1));
		assertEquals(1, map.size());
	}

	private static class MapChangeListener implements IMapChangeListener {
		int count;

		MapDiff diff;

		public void handleMapChange(MapChangeEvent event) {
			count++;
			this.diff = event.diff;
		}
	}

	public static class Model {
		private String value;

		private PropertyChangeSupport propertyChangeSupport;

		public Model(String value) {
			this.value = value;
			propertyChangeSupport = new PropertyChangeSupport(this);
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(listener);
		}

		/**
		 * @return Returns the value.
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @param value
		 *            The value to set.
		 */
		public void setValue(String value) {
			propertyChangeSupport.firePropertyChange("value", this.value,
					this.value = value);
		}
	}
}
