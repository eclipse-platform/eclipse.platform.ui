/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 221351)
 *     Brad Reynolds - through JavaBeanObservableArrayBasedListTest.java
 *     Matthew Hall - bug 213145, 244098, 246103
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.internal.databinding.beans.JavaBeanObservableSet;
import org.eclipse.jface.databinding.conformance.MutableObservableSetContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.SetChangeEventTracker;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Display;

/**
 * @since 1.1
 */
public class JavaBeanObservableArrayBasedSetTest extends
		AbstractDefaultRealmTestCase {
	private JavaBeanObservableSet set;

	private PropertyDescriptor propertyDescriptor;

	private Bean bean;

	private String propertyName;

	protected void setUp() throws Exception {
		super.setUp();

		propertyName = "array";
		propertyDescriptor = new PropertyDescriptor(propertyName, Bean.class);
		bean = new Bean(new HashSet());

		set = new JavaBeanObservableSet(SWTObservables.getRealm(Display
				.getDefault()), bean, propertyDescriptor, String.class);
	}

	public void testGetObserved() throws Exception {
		assertEquals(bean, set.getObserved());
	}

	public void testGetPropertyDescriptor() throws Exception {
		assertEquals(propertyDescriptor, set.getPropertyDescriptor());
	}

	public void testRegistersListenerOnCreation() throws Exception {
		assertTrue(bean.changeSupport.hasListeners(propertyName));
	}

	public void testRemovesListenerOnDisposal() throws Exception {
		set.dispose();
		assertFalse(bean.changeSupport.hasListeners(propertyName));
	}

	public void testSetBeanProperty_FiresSetChangeEvents() throws Exception {
		SetChangeEventTracker listener = SetChangeEventTracker.observe(set);

		assertEquals(0, listener.count);
		bean.setArray(new String[] { "element" });
		assertEquals(1, listener.count);
	}

	public void testAdd_AddsElement() throws Exception {
		assertEquals(0, set.size());

		String element = "1";
		set.add(element);

		assertEquals(1, set.size());
		assertEquals(element, bean.getArray()[0]);
	}

	public void testAdd_SetChangeEvent() throws Exception {
		SetChangeEventTracker listener = SetChangeEventTracker.observe(set);
		assertEquals(0, listener.count);

		String element = "1";
		set.add(element);

		assertEquals(1, listener.count);
		SetChangeEvent event = listener.event;

		assertSame(set, event.getObservableSet());
		assertEquals(Collections.singleton(element), event.diff.getAdditions());
		assertEquals(Collections.EMPTY_SET, event.diff.getRemovals());
	}

	public void testAdd_FiresPropertyChangeEvent() throws Exception {
		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				set.add("0");
			}
		});
	}

	public void testRemove() throws Exception {
		String element = "1";
		set.add(element);

		assertEquals(1, bean.getArray().length);
		set.remove(element);
		assertEquals(0, bean.getArray().length);
	}

	public void testRemove_SetChangeEvent() throws Exception {
		String element = "1";
		set.add(element);
		assertEquals(1, set.size());

		SetChangeEventTracker listener = SetChangeEventTracker.observe(set);
		assertEquals(0, listener.count);

		set.remove(element);

		assertEquals(1, listener.count);
		SetChangeEvent event = listener.event;
		assertEquals(set, event.getObservableSet());
		assertEquals(Collections.singleton(element), event.diff.getRemovals());
		assertEquals(Collections.EMPTY_SET, event.diff.getAdditions());
	}

	public void testRemovePropertyChangeEvent() throws Exception {
		set.add("0");

		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				set.remove("0");
			}
		});
	}

	public void testAddAll() throws Exception {
		Collection elements = Arrays.asList(new String[] { "1", "2" });
		assertEquals(0, set.size());

		set.addAll(elements);

		assertEquals(2, bean.getArray().length);
	}

	public void testAddAll_SetChangeEvent() throws Exception {
		Collection elements = Arrays.asList(new String[] { "1", "2" });
		assertEquals(0, set.size());

		SetChangeEventTracker listener = SetChangeEventTracker.observe(set);
		assertEquals(0, listener.count);

		set.addAll(elements);

		assertEquals(1, listener.count);
		SetChangeEvent event = listener.event;
		assertEquals(set, event.getObservableSet());

		assertEquals(new HashSet(elements), event.diff.getAdditions());
		assertEquals(Collections.EMPTY_SET, event.diff.getRemovals());
	}

	public void testAddAllPropertyChangeEvent() throws Exception {
		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				set.addAll(Arrays.asList(new String[] { "0", "1" }));
			}
		});
	}

	public void testRemoveAll() throws Exception {
		Collection elements = Arrays.asList(new String[] { "1", "2" });
		set.addAll(elements);

		assertEquals(2, bean.getArray().length);
		set.removeAll(elements);

		assertEquals(0, bean.getArray().length);
	}

	public void testRemoveAll_SetChangeEvent() throws Exception {
		Collection elements = Arrays.asList(new String[] { "1", "2" });
		set.addAll(elements);

		SetChangeEventTracker listener = SetChangeEventTracker.observe(set);
		assertEquals(0, listener.count);

		set.removeAll(elements);

		SetChangeEvent event = listener.event;
		assertEquals(set, event.getObservableSet());
		assertEquals(Collections.EMPTY_SET, event.diff.getAdditions());
		assertEquals(new HashSet(elements), event.diff.getRemovals());
	}

	public void testRemoveAllPropertyChangeEvent() throws Exception {
		set.add("0");
		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				set.removeAll(Arrays.asList(new String[] { "0" }));
			}
		});
	}

	public void testRetailAll() throws Exception {
		set.addAll(Arrays.asList(new String[] { "0", "1", "2", "3" }));

		assertEquals(4, bean.getArray().length);

		set.retainAll(Arrays.asList(new String[] { "0", "1" }));
		assertEquals(2, bean.getArray().length);

		assertTrue(set.containsAll(Arrays.asList(new String[] { "1", "0" })));
	}

	public void testRetainAll_SetChangeEvent() throws Exception {
		set.addAll(Arrays.asList(new String[] { "0", "1", "2", "3" }));

		SetChangeEventTracker listener = SetChangeEventTracker.observe(set);

		assertEquals(0, listener.count);
		set.retainAll(Arrays.asList(new String[] { "0", "1" }));

		assertEquals(1, listener.count);
		SetChangeEvent event = listener.event;
		assertEquals(set, event.getObservableSet());
		assertEquals(Collections.EMPTY_SET, event.diff.getAdditions());
		assertEquals(new HashSet(Arrays.asList(new String[] { "2", "3" })),
				event.diff.getRemovals());
	}

	public void testRetainAllPropertyChangeEvent() throws Exception {
		set.addAll(Arrays.asList(new String[] { "0", "1" }));

		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				set.retainAll(Arrays.asList(new String[] { "0" }));
			}
		});
	}

	public void testSetChangeEventFiresWhenNewSetIsSet() throws Exception {
		Bean[] elements = new Bean[] { new Bean(), new Bean() };

		SetChangeEventTracker listener = SetChangeEventTracker.observe(set);

		assertEquals(0, listener.count);
		bean.setArray(elements);
		assertEquals(1, listener.count);
	}

	public void testSetBeanProperty_CorrectForNullOldAndNewValues() {
		// The java bean spec allows the old and new values in a
		// PropertyChangeEvent to be null, which indicates that an unknown
		// change occured.

		// This test ensures that JavaBeanObservableValue fires the correct
		// value diff even if the bean implementor is lazy :-P

		Bean bean = new AnnoyingBean();
		bean.setArray(new Object[] { "old" });
		IObservableSet observable = BeansObservables.observeSet(
				new CurrentRealm(true), bean, "array");
		SetChangeEventTracker tracker = SetChangeEventTracker
				.observe(observable);
		bean.setArray(new Object[] { "new" });
		assertEquals(1, tracker.count);
		assertEquals(Collections.singleton("old"), tracker.event.diff
				.getRemovals());
		assertEquals(Collections.singleton("new"), tracker.event.diff
				.getAdditions());
	}

	private static void assertPropertyChangeEvent(Bean bean, Runnable runnable) {
		PropertyChangeTracker listener = new PropertyChangeTracker();
		bean.addPropertyChangeListener(listener);

		Object[] old = bean.getArray();
		assertEquals(0, listener.count);

		runnable.run();

		PropertyChangeEvent event = listener.evt;
		assertEquals("event did not fire", 1, listener.count);
		assertEquals("array", event.getPropertyName());
		assertTrue("old value", Arrays.equals(old, (Object[]) event
				.getOldValue()));
		assertTrue("new value", Arrays.equals(bean.getArray(), (Object[]) event.getNewValue()));
		assertFalse("sets are equal", Arrays.equals(bean.getArray(), old));
	}

	private static class PropertyChangeTracker implements
			PropertyChangeListener {
		int count;

		PropertyChangeEvent evt;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			count++;
			this.evt = evt;
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(JavaBeanObservableArrayBasedSetTest.class.getName());
		suite.addTestSuite(JavaBeanObservableArrayBasedSetTest.class);
		suite.addTest(MutableObservableSetContractTest.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			String propertyName = "array";
			PropertyDescriptor propertyDescriptor;
			try {
				propertyDescriptor = new PropertyDescriptor(propertyName,
						Bean.class);
			} catch (IntrospectionException e) {
				throw new RuntimeException(e);
			}
			Object bean = new Bean(new Object[0]);

			IObservableSet set = new JavaBeanObservableSet(realm, bean,
					propertyDescriptor, String.class);
			for (int i = 0; i < elementCount; i++)
				set.add(createElement(set));
			return set;
		}

		public Object createElement(IObservableCollection collection) {
			return new Object().toString();
		}

		public Object getElementType(IObservableCollection collection) {
			return String.class;
		}

		public void change(IObservable observable) {
			IObservableSet set = (IObservableSet) observable;
			set.add(createElement(set));
		}
	}
}
