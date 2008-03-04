/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.internal.beans;

import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.internal.databinding.internal.beans.JavaBeanObservableList;
import org.eclipse.jface.databinding.conformance.MutableObservableListContractTest;
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ListChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Display;

/**
 * @since 1.1
 */
public class JavaBeanObservableArrayBasedListTest extends
		AbstractDefaultRealmTestCase {
	private JavaBeanObservableList list;

	private PropertyDescriptor propertyDescriptor;

	private Bean bean;

	private String propertyName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		propertyName = "array";
		propertyDescriptor = new PropertyDescriptor(propertyName, Bean.class);
		bean = new Bean(new Object[0]);

		list = new JavaBeanObservableList(SWTObservables.getRealm(Display
				.getDefault()), bean, propertyDescriptor, Bean.class);
	}

	public void testGetObserved() throws Exception {
		assertSame(bean, list.getObserved());
	}

	public void testGetPropertyDescriptor() throws Exception {
		assertSame(propertyDescriptor, list.getPropertyDescriptor());
	}

	public void testRegistersListenerAfterFirstListenerIsAdded()
			throws Exception {
		assertFalse(bean.changeSupport.hasListeners(propertyName));
		list.addListChangeListener(new ListChangeEventTracker());
		assertTrue(bean.changeSupport.hasListeners(propertyName));
	}

	public void testRemovesListenerAfterLastListenerIsRemoved()
			throws Exception {
		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);

		assertTrue(bean.changeSupport.hasListeners(propertyName));
		list.removeListChangeListener(listener);
		assertFalse(bean.changeSupport.hasListeners(propertyName));
	}

	public void testFiresListChangeEvents() throws Exception {
		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);

		assertEquals(0, listener.count);
		bean.setArray(new Bean[] { new Bean() });
		assertEquals(1, listener.count);
	}

	public void testAddAddsElement() throws Exception {
		int count = list.size();
		String element = "1";

		assertEquals(0, count);
		list.add(element);
		assertEquals(count + 1, list.size());
		assertEquals(element, bean.getArray()[count]);
	}

	public void testAddListChangeEvent() throws Exception {
		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);

		assertEquals(0, listener.count);
		String element = "1";

		list.add(element);

		assertEquals(1, listener.count);
		ListChangeEvent event = listener.event;

		assertEquals(list, event.getObservableList());
		assertEntry(event.diff.getDifferences()[0], true, 0, element);
	}

	public void testAdd_FiresPropertyChangeEvent() throws Exception {
		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				list.add("0");
			}
		});
	}

	public void testAddWithIndex() throws Exception {
		String element = "1";
		assertEquals(0, list.size());

		list.add(0, element);
		assertEquals(element, bean.getArray()[0]);
	}

	public void testAddAtIndexListChangeEvent() throws Exception {
		String element = "1";
		assertEquals(0, list.size());

		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);

		list.add(0, element);

		ListChangeEvent event = listener.event;
		assertEntry(event.diff.getDifferences()[0], true, 0, element);
	}

	public void testAddAtIndexPropertyChangeEvent() throws Exception {
		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				list.add(0, "0");
			}
		});
	}

	public void testRemove() throws Exception {
		String element = "1";
		list.add(element);

		assertEquals(1, bean.getArray().length);
		list.remove(element);
		assertEquals(0, bean.getArray().length);
	}

	public void testRemoveListChangeEvent() throws Exception {
		String element = "1";
		list.add(element);

		assertEquals(1, list.size());
		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);

		list.remove(element);

		assertEquals(1, listener.count);
		ListChangeEvent event = listener.event;
		assertEquals(list, event.getObservableList());
		assertEntry(event.diff.getDifferences()[0], false, 0, element);
	}

	public void testRemovePropertyChangeEvent() throws Exception {
		list.add("0");

		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				list.remove("0");
			}
		});
	}

	public void testRemoveAtIndex() throws Exception {
		String element = "1";
		list.add(element);

		assertEquals(element, bean.getArray()[0]);

		list.remove(0);
		assertEquals(0, bean.getArray().length);
	}

	public void testRemoveAtIndexListChangeEvent() throws Exception {
		String element = "1";
		list.add(element);

		assertEquals(1, list.size());
		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);

		list.remove(0);

		assertEquals(1, listener.count);
		ListChangeEvent event = listener.event;
		assertEquals(list, event.getObservableList());
		assertEntry(event.diff.getDifferences()[0], false, 0, element);
	}

	public void testRemoveAtIndexPropertyChangeEvent() throws Exception {
		list.add("0");
		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				list.remove(0);
			}
		});
	}

	public void testAddAll() throws Exception {
		Collection elements = Arrays.asList(new String[] { "1", "2" });
		assertEquals(0, list.size());

		list.addAll(elements);

		assertEquals(2, bean.getArray().length);
	}

	public void testAddAllListChangEvent() throws Exception {
		List elements = Arrays.asList(new String[] { "1", "2" });
		assertEquals(0, list.size());

		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);
		assertEquals(0, listener.count);

		list.addAll(elements);

		assertEquals(1, listener.count);
		ListChangeEvent event = listener.event;
		assertEquals(list, event.getObservableList());

		assertEntry(event.diff.getDifferences()[0], true, 0, elements.get(0));
		assertEntry(event.diff.getDifferences()[1], true, 1, elements.get(1));
	}

	public void testAddAllPropertyChangeEvent() throws Exception {
		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				list.addAll(Arrays.asList(new String[] { "0", "1" }));
			}
		});
	}

	public void testAddAllAtIndex() throws Exception {
		List elements = Arrays.asList(new String[] { "1", "2" });
		list.addAll(elements);

		assertEquals(2, list.size());

		list.addAll(2, elements);

		assertEquals(4, bean.getArray().length);
		assertEquals(elements.get(0), bean.getArray()[0]);
		assertEquals(elements.get(1), bean.getArray()[1]);
	}

	public void testAddAllAtIndexListChangeEvent() throws Exception {
		List elements = Arrays.asList(new String[] { "1", "2" });
		list.addAll(elements);

		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);

		assertEquals(0, listener.count);

		list.addAll(2, elements);

		assertEquals(1, listener.count);
		ListChangeEvent event = listener.event;
		assertEquals(list, event.getObservableList());
		assertEntry(event.diff.getDifferences()[0], true, 2, elements.get(0));
		assertEntry(event.diff.getDifferences()[1], true, 3, elements.get(1));
	}

	public void testAddAllAtIndexPropertyChangeEvent() throws Exception {
		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				list.addAll(0, Arrays.asList(new String[] { "1", "2" }));
			}
		});
	}

	public void testRemoveAll() throws Exception {
		List elements = Arrays.asList(new String[] { "1", "2" });
		list.addAll(elements);
		list.addAll(elements);

		assertEquals(4, bean.getArray().length);
		list.removeAll(elements);

		assertEquals(2, bean.getArray().length);
		assertEquals(elements.get(0), bean.getArray()[0]);
		assertEquals(elements.get(1), bean.getArray()[1]);
	}

	public void testRemoveAllListChangeEvent() throws Exception {
		List elements = Arrays.asList(new String[] { "1", "2" });
		list.addAll(elements);
		list.addAll(elements);

		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);

		assertEquals(0, listener.count);
		list.removeAll(elements);

		ListChangeEvent event = listener.event;
		assertEquals(list, event.getObservableList());
		assertEntry(event.diff.getDifferences()[0], false, 0, elements.get(0));
		assertEntry(event.diff.getDifferences()[1], false, 0, elements.get(1));
	}

	public void testRemoveAllPropertyChangeEvent() throws Exception {
		list.add("0");
		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				list.removeAll(Arrays.asList(new String[] { "0" }));
			}
		});
	}

	public void testRetailAll() throws Exception {
		List elements = Arrays.asList(new String[] { "0", "1", "2", "3" });
		list.addAll(elements);

		assertEquals(4, bean.getArray().length);

		list.retainAll(elements.subList(0, 2));
		assertEquals(2, bean.getArray().length);

		assertEquals(elements.get(0), bean.getArray()[0]);
		assertEquals(elements.get(1), bean.getArray()[1]);
	}

	public void testRetainAllListChangeEvent() throws Exception {
		List elements = Arrays.asList(new String[] { "0", "1", "2", "3" });
		list.addAll(elements);

		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);

		assertEquals(0, listener.count);
		list.retainAll(elements.subList(0, 2));

		assertEquals(1, listener.count);
		ListChangeEvent event = listener.event;
		assertEquals(list, event.getObservableList());
		assertEntry(event.diff.getDifferences()[0], false, 2, elements.get(2));
		assertEntry(event.diff.getDifferences()[1], false, 2, elements.get(3));
	}

	public void testRetainAllPropertyChangeEvent() throws Exception {
		list.addAll(Arrays.asList(new String[] { "0", "1" }));

		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				list.retainAll(Arrays.asList(new String[] { "0" }));
			}
		});
	}

	public void testSet() throws Exception {
		String oldElement = "old";
		String newElement = "new";
		list.add(oldElement);

		assertEquals(oldElement, bean.getArray()[0]);

		list.set(0, newElement);
		assertEquals(newElement, bean.getArray()[0]);
	}

	public void testSetListChangeEvent() throws Exception {
		String oldElement = "old";
		String newElement = "new";
		list.add(oldElement);

		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);
		assertEquals(0, listener.count);

		list.set(0, newElement);

		assertEquals(1, listener.count);
		ListChangeEvent event = listener.event;
		assertEquals(list, event.getObservableList());
		assertEntry(event.diff.getDifferences()[0], false, 0, oldElement);
		assertEntry(event.diff.getDifferences()[1], true, 0, newElement);
	}

	public void testSetPropertyChangeEvent() throws Exception {
		list.add("0");
		assertPropertyChangeEvent(bean, new Runnable() {
			public void run() {
				list.set(0, "1");
			}
		});
	}

	public void testListChangeEventFiresWhenNewListIsSet() throws Exception {
		Bean[] elements = new Bean[] { new Bean(), new Bean() };

		ListChangeEventTracker listener = new ListChangeEventTracker();
		list.addListChangeListener(listener);

		assertEquals(0, listener.count);
		bean.setArray(elements);
		assertEquals(1, listener.count);
	}

	private static void assertEntry(ListDiffEntry entry, boolean addition,
			int position, Object element) {
		assertEquals("addition", addition, entry.isAddition());
		assertEquals("position", position, entry.getPosition());
		assertEquals("element", element, entry.getElement());
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
		assertTrue("old value", Arrays.equals(old, (Object[]) event.getOldValue()));
		assertTrue("new value", Arrays.equals(bean.getArray(), (Object[]) event.getNewValue()));
		assertFalse("lists are equal", Arrays.equals(bean.getArray(), old));
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
		return new SuiteBuilder()
				.addTests(JavaBeanObservableArrayBasedListTest.class)
				.addObservableContractTest(ObservableListContractTest.class,
						new Delegate())
				.addObservableContractTest(
						MutableObservableListContractTest.class, new Delegate())
				.build();
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

			IObservableList list = new JavaBeanObservableList(realm, bean,
					propertyDescriptor, String.class);
			for (int i = 0; i < elementCount; i++)
				list.add(createElement(list));
			return list;
		}

		public Object createElement(IObservableCollection collection) {
			return new Object();
		}

		public Object getElementType(IObservableCollection collection) {
			return String.class;
		}

		public void change(IObservable observable) {
			IObservableList list = (IObservableList) observable;
			list.add(createElement(list));
		}
	}
}
