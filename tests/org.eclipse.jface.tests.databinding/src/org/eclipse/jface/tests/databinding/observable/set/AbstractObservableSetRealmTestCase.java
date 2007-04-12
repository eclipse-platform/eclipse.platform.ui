/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.observable.set;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.tests.databinding.util.RealmTester;
import org.eclipse.jface.tests.databinding.util.RealmTester.CurrentRealm;

/**
 * @since 1.1
 */
public abstract class AbstractObservableSetRealmTestCase extends TestCase {
	private IObservableSet set;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		RealmTester.setDefault(new CurrentRealm(true));
		set = doCreateSet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();

		RealmTester.setDefault(null);
	}

	/**
	 * Invoked when a test is to be ran and a new set is needed.
	 * 
	 * @return set
	 */
	protected abstract IObservableSet doCreateSet();

	/**
	 * Template method to provide access to a <code>getterCalled()</code>
	 * method. Default implementation returns the method if it is accessible.
	 * 
	 * @return method, <code>null</code> if not to be tested
	 */
	protected Method getMethodGetterCalled() {
		try {
			return set.getClass().getMethod("getterCalled", null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Template method to provide access to a
	 * <code>fireSetChange(SetDiff)</code> method. Default implementation
	 * returns the method if it is accessible.
	 * 
	 * @return method, <code>null</code> if not to be tested
	 */
	protected Method getMethodFireSetChange() {
		try {
			return set.getClass().getMethod("fireSetChange",
					new Class[] { SetDiff.class });
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Template method to provide access to a <code>setStale(boolean)</code>
	 * method. Default implementation returns the method if it is accessible.
	 * 
	 * @return method, <code>null</code> if not to be tested
	 */
	protected Method getMethodSetStale() {
		try {
			return set.getClass().getMethod("setStale",
					new Class[] { Boolean.TYPE });
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void testGetterCalledRealmCheck() throws Exception {
		final Method method = getMethodGetterCalled();

		if (method != null) {
			RealmTester.exerciseCurrent(new Runnable() {
				public void run() {
					invokeSafe(method, set, null);
				}
			});
		}
	}

	private void invokeSafe(Method method, Object instance, Object[] params) {
		try {
			method.invoke(instance, params);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof AssertionFailedException) {
				throw (AssertionFailedException) e.getTargetException();
			}
			throw new RuntimeException(e);
		}
	}

	public void testFireSetChangeRealmCheck() throws Exception {
		final Method method = getMethodFireSetChange();

		if (method != null) {
			RealmTester.exerciseCurrent(new Runnable() {
				public void run() {
					invokeSafe(method, set, new Object[] { Diffs.createSetDiff(
							new HashSet(), new HashSet()) });
				}
			});
		}
	}

	public void testIsStaleRealmCheck() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				set.isStale();
			}
		});
	}

	public void testSetStaleRealmCheck() throws Exception {
		final Method method = getMethodSetStale();

		if (method != null) {
			RealmTester.exerciseCurrent(new Runnable() {
				public void run() {
					invokeSafe(method, set, new Object[] { Boolean.TRUE });
				}
			});
		}
	}

	public void testIteratorGetterCalled() throws Exception {
		IObservable[] observables = ObservableTracker.runAndMonitor(
				new Runnable() {
					public void run() {
						set.iterator();
					}
				}, null, null);

		assertEquals("length", 1, observables.length);
		assertTrue("observable", Arrays.asList(observables).contains(set));
	}

	/**
	 * Returns <code>true</code> if the set is mutable and mutators should be
	 * tested.  Default is <code>false</code>.
	 * 
	 * @return <code>true</code> if mutable
	 */
	protected boolean isMutable() {
		return false;
	}

	public void testAdd() throws Exception {
		if (isMutable()) {
			RealmTester.exerciseCurrent(new Runnable() {
				public void run() {
					set.add("");
				}
			});
		}
	}

	public void testAddAll() throws Exception {
		if (isMutable()) {
			RealmTester.exerciseCurrent(new Runnable() {
				public void run() {
					set.addAll(Arrays.asList(new Object[] { "" }));
				}
			});
		}
	}

	public void testRemove() throws Exception {
		if (isMutable()) {
			RealmTester.exerciseCurrent(new Runnable() {
				public void run() {
					set.remove("");
				}
			});
		}
	}

	public void testRemoveAll() throws Exception {
		if (isMutable()) {
			RealmTester.exerciseCurrent(new Runnable() {
				public void run() {
					set.removeAll(Arrays.asList(new Object[] { "" }));
				}
			});
		}
	}

	public void testRetainAll() throws Exception {
		if (isMutable()) {
			RealmTester.exerciseCurrent(new Runnable() {
				public void run() {
					set.retainAll(Arrays.asList(new Object[] { "" }));
				}
			});
		}
	}

	public void testClear() throws Exception {
		if (isMutable()) {
			RealmTester.exerciseCurrent(new Runnable() {
				public void run() {
					set.clear();
				}
			});
		}
	}
}
