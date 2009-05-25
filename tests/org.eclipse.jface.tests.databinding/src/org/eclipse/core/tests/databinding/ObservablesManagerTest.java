/*******************************************************************************
 * Copyright (c) 2007, 2009 Bob Smith and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bob Smith - initial API and implementation (bug 198880)
 *     Matthew Hall - bugs 146397, 260329
 ******************************************************************************/

package org.eclipse.core.tests.databinding;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ObservablesManager;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 *
 */
public class ObservablesManagerTest extends AbstractDefaultRealmTestCase {
	private DataBindingContext dbc;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		dbc = new DataBindingContext();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		if (dbc != null) {
			dbc.dispose();
		}
		super.tearDown();
	}

	public void testOnlyModelIsDisposed() throws Exception {

		FlagOnDisposeObservableValue targetOv = new FlagOnDisposeObservableValue();
		FlagOnDisposeObservableValue modelOv = new FlagOnDisposeObservableValue();
		dbc.bindValue(targetOv, modelOv);

		ObservablesManager observablesManager = new ObservablesManager();

		observablesManager.addObservablesFromContext(dbc, false, true);
		observablesManager.dispose();

		assertFalse(targetOv.disposeCalled);
		assertTrue(modelOv.disposeCalled);
	}

	public void testOnlyTargetIsDisposed() throws Exception {

		FlagOnDisposeObservableValue targetOv = new FlagOnDisposeObservableValue();
		FlagOnDisposeObservableValue modelOv = new FlagOnDisposeObservableValue();
		dbc.bindValue(targetOv, modelOv);

		ObservablesManager observablesManager = new ObservablesManager();

		observablesManager.addObservablesFromContext(dbc, true, false);
		observablesManager.dispose();

		assertTrue(targetOv.disposeCalled);
		assertFalse(modelOv.disposeCalled);
	}

	public void testTargetAndModelIsDisposed() throws Exception {

		FlagOnDisposeObservableValue targetOv = new FlagOnDisposeObservableValue();
		FlagOnDisposeObservableValue modelOv = new FlagOnDisposeObservableValue();
		dbc.bindValue(targetOv, modelOv);

		ObservablesManager observablesManager = new ObservablesManager();

		observablesManager.addObservablesFromContext(dbc, true, true);
		observablesManager.dispose();

		assertTrue(targetOv.disposeCalled);
		assertTrue(modelOv.disposeCalled);
	}

	private static class FlagOnDisposeObservableValue implements
			IObservableValue {

		private boolean disposeCalled = false;

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.databinding.observable.value.IObservableValue#addValueChangeListener(org.eclipse.core.databinding.observable.value.IValueChangeListener)
		 */
		public void addValueChangeListener(IValueChangeListener listener) {
			// dummy
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValue()
		 */
		public Object getValue() {
			// dummy
			return null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.databinding.observable.value.IObservableValue#getValueType()
		 */
		public Object getValueType() {
			// dummy
			return null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.databinding.observable.value.IObservableValue#removeValueChangeListener(org.eclipse.core.databinding.observable.value.IValueChangeListener)
		 */
		public void removeValueChangeListener(IValueChangeListener listener) {
			// dummy

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.databinding.observable.value.IObservableValue#setValue(java.lang.Object)
		 */
		public void setValue(Object value) {
			// dummy

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.databinding.observable.IObservable#addChangeListener(org.eclipse.core.databinding.observable.IChangeListener)
		 */
		public void addChangeListener(IChangeListener listener) {
			// dummy

		}

		public void addDisposeListener(IDisposeListener listener) {
			// dummy
	
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.databinding.observable.IObservable#addStaleListener(org.eclipse.core.databinding.observable.IStaleListener)
		 */
		public void addStaleListener(IStaleListener listener) {
			// dummy

		}

		public boolean isDisposed() {
			return disposeCalled;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.databinding.observable.IObservable#dispose()
		 */
		public void dispose() {
			disposeCalled = true;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.databinding.observable.IObservable#getRealm()
		 */
		public Realm getRealm() {
			return Realm.getDefault();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.databinding.observable.IObservable#isStale()
		 */
		public boolean isStale() {
			// dummy
			return false;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.databinding.observable.IObservable#removeChangeListener(org.eclipse.core.databinding.observable.IChangeListener)
		 */
		public void removeChangeListener(IChangeListener listener) {
			// dummy

		}

		public void removeDisposeListener(IDisposeListener listener) {
			// dummy
			
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.core.databinding.observable.IObservable#removeStaleListener(org.eclipse.core.databinding.observable.IStaleListener)
		 */
		public void removeStaleListener(IStaleListener listener) {
			// dummy

		}

	}
}
