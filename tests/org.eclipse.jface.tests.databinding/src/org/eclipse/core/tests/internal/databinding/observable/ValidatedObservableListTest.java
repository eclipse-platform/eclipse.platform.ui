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

import java.util.ArrayList;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.internal.databinding.validation.ValidatedObservableList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.conformance.MutableObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class ValidatedObservableListTest extends AbstractDefaultRealmTestCase {
	public static Test suite() {
		return MutableObservableListContractTest.suite(new Delegate());
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		private Object elementType = new Object();

		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			IObservableList target = new WritableList(realm, new ArrayList(),
					elementType);
			for (int i = 0; i < elementCount; i++)
				target.add(createElement(target));
			IObservableValue validationStatus = new WritableValue(realm,
					ValidationStatus.ok(), IStatus.class);
			return new ValidatedObservableListStub(target, validationStatus);
		}

		public Object createElement(IObservableCollection collection) {
			return new Object();
		}

		public Object getElementType(IObservableCollection collection) {
			return elementType;
		}

		public void change(IObservable observable) {
			ValidatedObservableListStub validated = (ValidatedObservableListStub) observable;
			validated.target.add(createElement(validated));
		}

		public void setStale(IObservable observable, boolean stale) {
			ValidatedObservableListStub validated = (ValidatedObservableListStub) observable;
			if (stale) {
				validated.validationStatus.setValue(ValidationStatus
						.error("error"));
				validated.target.add(createElement(validated));
			} else {
				validated.validationStatus.setValue(ValidationStatus.ok());
			}
		}

	}

	static class ValidatedObservableListStub extends ValidatedObservableList {
		IObservableList target;
		IObservableValue validationStatus;

		ValidatedObservableListStub(IObservableList target,
				IObservableValue validationStatus) {
			super(target, validationStatus);
			this.target = target;
			this.validationStatus = validationStatus;
		}
	}
}
