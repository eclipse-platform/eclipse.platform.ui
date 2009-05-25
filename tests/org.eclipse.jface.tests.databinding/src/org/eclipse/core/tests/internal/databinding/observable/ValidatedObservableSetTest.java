/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 218269)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import java.util.Collections;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.validation.ValidatedObservableSet;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.conformance.MutableObservableSetContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class ValidatedObservableSetTest extends AbstractDefaultRealmTestCase {
	public static Test suite() {
		return MutableObservableSetContractTest.suite(new Delegate());
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		private Object elementType = new Object();

		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			IObservableSet target = new WritableSet(realm,
					Collections.EMPTY_SET, elementType);
			for (int i = 0; i < elementCount; i++)
				target.add(createElement(target));
			IObservableValue validationStatus = new WritableValue(realm,
					ValidationStatus.ok(), IStatus.class);
			return new ValidatedObservableSetStub(target, validationStatus);
		}

		public Object createElement(IObservableCollection collection) {
			return new Object();
		}

		public Object getElementType(IObservableCollection collection) {
			return elementType;
		}

		public void change(IObservable observable) {
			ValidatedObservableSetStub validated = (ValidatedObservableSetStub) observable;
			validated.target.add(createElement(validated));
		}

		public void setStale(IObservable observable, boolean stale) {
			ValidatedObservableSetStub validated = (ValidatedObservableSetStub) observable;
			if (stale) {
				validated.validationStatus.setValue(ValidationStatus
						.error("error"));
				validated.target.add(createElement(validated));
			} else {
				validated.validationStatus.setValue(ValidationStatus.ok());
			}
		}

	}

	static class ValidatedObservableSetStub extends ValidatedObservableSet {
		IObservableSet target;
		IObservableValue validationStatus;

		ValidatedObservableSetStub(IObservableSet target,
				IObservableValue validationStatus) {
			super(target, validationStatus);
			this.target = target;
			this.validationStatus = validationStatus;
		}
	}
}
