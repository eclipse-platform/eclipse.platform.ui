/*******************************************************************************
 * Copyright (c) 2007, 2010 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 221351, 213145, 244098, 246103, 194734, 268688
 *     Ovidio Mallo - bugs 247741, 301774
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.beans.IBeanProperty;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.jface.databinding.conformance.MutableObservableSetContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.SetChangeEventTracker;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.3
 */
public class JavaBeanObservableSetTest extends AbstractDefaultRealmTestCase {
	private IObservableSet observableSet;
	private IBeanObservable beanObservable;
	private Bean bean;
	private PropertyDescriptor propertyDescriptor;
	private String propertyName;
	private SetChangeEventTracker listener;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		bean = new Bean();
		propertyName = "set";
		propertyDescriptor = ((IBeanProperty) BeanProperties.set(Bean.class,
				propertyName)).getPropertyDescriptor();

		observableSet = BeansObservables
				.observeSet(SWTObservables.getRealm(Display.getDefault()),
						bean, propertyName, Bean.class);
		beanObservable = (IBeanObservable) observableSet;
		listener = new SetChangeEventTracker();
	}

	public void testGetObserved() throws Exception {
		assertEquals(bean, beanObservable.getObserved());
	}

	public void testGetPropertyDescriptor() throws Exception {
		assertEquals(propertyDescriptor, beanObservable.getPropertyDescriptor());
	}

	public void testGetElementType() throws Exception {
		assertEquals(Bean.class, observableSet.getElementType());
	}

	public void testRegistersListenerAfterFirstListenerIsAdded()
			throws Exception {
		assertFalse(bean.changeSupport.hasListeners(propertyName));
		observableSet.addSetChangeListener(new SetChangeEventTracker());
		assertTrue(bean.changeSupport.hasListeners(propertyName));
	}

	public void testRemovesListenerAfterLastListenerIsRemoved()
			throws Exception {
		observableSet.addSetChangeListener(listener);

		assertTrue(bean.changeSupport.hasListeners(propertyName));
		observableSet.removeSetChangeListener(listener);
		assertFalse(bean.changeSupport.hasListeners(propertyName));
	}

	public void testFiresChangeEvents() throws Exception {
		observableSet.addSetChangeListener(listener);
		assertEquals(0, listener.count);
		bean.setSet(new HashSet(Arrays.asList(new String[] { "1" })));
		assertEquals(1, listener.count);
	}

	public void testConstructor_RegisterListeners() throws Exception {
		bean = new Bean();
		observableSet = BeansObservables.observeSet(new CurrentRealm(true),
				bean, propertyName);
		assertFalse(bean.hasListeners(propertyName));
		ChangeEventTracker.observe(observableSet);
		assertTrue(bean.hasListeners(propertyName));
	}

	public void testConstructor_SkipsRegisterListeners() throws Exception {
		bean = new Bean();

		observableSet = PojoObservables.observeSet(new CurrentRealm(true),
				bean, propertyName);
		assertFalse(bean.hasListeners(propertyName));
		ChangeEventTracker.observe(observableSet);
		assertFalse(bean.hasListeners(propertyName));
	}

	public void testSetBeanProperty_CorrectForNullOldAndNewValues() {
		// The java bean spec allows the old and new values in a
		// PropertyChangeEvent to be null, which indicates that an unknown
		// change occured.

		// This test ensures that JavaBeanObservableValue fires the correct
		// value diff even if the bean implementor is lazy :-P

		Bean bean = new AnnoyingBean();
		bean.setSet(Collections.singleton("old"));
		IObservableSet observable = BeansObservables.observeSet(
				new CurrentRealm(true), bean, "set");
		SetChangeEventTracker tracker = SetChangeEventTracker
				.observe(observable);
		bean.setSet(Collections.singleton("new"));
		assertEquals(1, tracker.count);
		assertEquals(Collections.singleton("old"), tracker.event.diff
				.getRemovals());
		assertEquals(Collections.singleton("new"), tracker.event.diff
				.getAdditions());
	}

	public void testModifyObservableSet_FiresSetChange() {
		Bean bean = new Bean(new HashSet());
		IObservableSet observable = BeansObservables.observeSet(bean, "set");
		SetChangeEventTracker tracker = SetChangeEventTracker
				.observe(observable);

		Object element = new Object();
		observable.add(element);

		assertEquals(1, tracker.count);
		assertDiff(tracker.event.diff, Collections.EMPTY_SET, Collections
				.singleton(element));
	}

	public void testSetBeanPropertyOutsideRealm_FiresEventInsideRealm() {
		Bean bean = new Bean(Collections.EMPTY_SET);
		CurrentRealm realm = new CurrentRealm(true);
		IObservableSet observable = BeansObservables.observeSet(realm, bean,
				"set");
		SetChangeEventTracker tracker = SetChangeEventTracker
				.observe(observable);

		realm.setCurrent(false);
		bean.setSet(Collections.singleton("element"));
		assertEquals(0, tracker.count);

		realm.setCurrent(true);
		assertEquals(1, tracker.count);
		assertDiff(tracker.event.diff, Collections.EMPTY_SET, Collections
				.singleton("element"));
	}

	/**
	 * Makes sure that the set set on the Bean model after changing the
	 * observable set is modifiable (see bugs 285307 and 301774).
	 */
	public void testUpdatedBeanSetIsModifiable() {
		Bean bean = new Bean(new ArrayList());
		IObservableSet observable = BeansObservables.observeSet(bean, "set");

		observable.add(new Object());
		bean.getSet().clear();
	}

	/**
	 * Makes sure that the set set on the Pojo model after changing the
	 * observable set is modifiable (see bugs 285307 and 301774).
	 */
	public void testUpdatedPojoSetIsModifiable() {
		Bean bean = new Bean(new ArrayList());
		IObservableSet observable = PojoObservables.observeSet(bean, "set");

		observable.add(new Object());
		bean.getSet().clear();
	}

	private static void assertDiff(SetDiff diff, Set oldSet, Set newSet) {
		oldSet = new HashSet(oldSet); // defensive copy in case arg is
		// unmodifiable
		diff.applyTo(oldSet);
		assertEquals("applying diff to list did not produce expected result",
				newSet, oldSet);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(JavaBeanObservableSetTest.class
				.getName());
		suite.addTestSuite(JavaBeanObservableSetTest.class);
		suite.addTest(MutableObservableSetContractTest.suite(new Delegate()));
		return suite;
	}

	private static class Delegate extends
			AbstractObservableCollectionContractDelegate {
		@Override
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			Bean bean = new Bean();
			String propertyName = "set";

			IObservableSet set = BeansObservables.observeSet(realm, bean,
					propertyName, String.class);
			for (int i = 0; i < elementCount; i++)
				set.add(createElement(set));
			return set;
		}

		@Override
		public Object createElement(IObservableCollection collection) {
			return new Object();
		}

		@Override
		public Object getElementType(IObservableCollection collection) {
			return String.class;
		}

		@Override
		public void change(IObservable observable) {
			IObservableSet set = (IObservableSet) observable;
			set.add(createElement(set));
		}
	}
}
