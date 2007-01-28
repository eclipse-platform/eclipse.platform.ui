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

import java.beans.PropertyDescriptor;
import java.util.HashSet;

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
	private Bean model1;

	private Bean model2;

	private WritableSet set;

	private PropertyDescriptor propertyDescriptor;

	private JavaBeanObservableMap map;

	protected void setUp() throws Exception {
		ThreadRealm realm = new ThreadRealm();
		realm.init(Thread.currentThread());
		model1 = new Bean("1");
		model2 = new Bean("2");

		set = new WritableSet(realm, new HashSet(), Bean.class);
		set.add(model1);
		set.add(model2);

		propertyDescriptor = new PropertyDescriptor("value", Bean.class);
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

		Bean model3 = new Bean("3");

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
	
	public void testGetObserved() throws Exception {
		assertEquals(set, map.getObserved());
	}
	
	public void testGetPropertyDescriptor() throws Exception {
		assertEquals(propertyDescriptor, map.getPropertyDescriptor());
	}

	private static class MapChangeListener implements IMapChangeListener {
		int count;

		MapDiff diff;

		public void handleMapChange(MapChangeEvent event) {
			count++;
			this.diff = event.diff;
		}
	}
}
