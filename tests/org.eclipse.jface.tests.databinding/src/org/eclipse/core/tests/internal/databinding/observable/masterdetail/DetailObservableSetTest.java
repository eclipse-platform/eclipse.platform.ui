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

package org.eclipse.core.tests.internal.databinding.observable.masterdetail;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.masterdetail.DetailObservableSet;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 * 
 */
public class DetailObservableSetTest extends AbstractDefaultRealmTestCase {
	/**
	 * Asserts the use case of specifying null on construction for the detail
	 * type of the detail list.
	 * 
	 * @throws Exception
	 */
	public void testElementTypeNull() throws Exception {
		WritableValue observableValue = new WritableValue(new WritableSet(new HashSet(), Object.class),
				null);

		class Factory implements IObservableFactory {
			Object type;

			public IObservable createObservable(Object target) {
				return new WritableSet(new HashSet(), type);
			}
		}

		Factory factory = new Factory();
		DetailObservableSet detailObservable = new DetailObservableSet(factory,
				observableValue, null);
		assertNull(detailObservable.getElementType());

		factory.type = Object.class;
		observableValue.setValue(new WritableSet(Arrays
				.asList(new Object[] { new Object() }), String.class));
		assertNull("element type not null", detailObservable.getElementType());

		factory.type = String.class;
		// set the value again to ensure that the observable doesn't update the
		// element type with that of the new element type
		observableValue.setValue(new WritableSet(Arrays
				.asList(new String[] { "1" }), Object.class));
		assertNull("element type not null", detailObservable.getElementType());
	}

	/**
	 * Asserts that you can't change the type across multiple inner observables.
	 * 
	 * @throws Exception
	 */
	public void testElementTypeNotNull() throws Exception {
		WritableValue observableValue = new WritableValue(new WritableSet(new HashSet(), Object.class),
				null);

		class Factory implements IObservableFactory {
			Object type = Object.class;

			public IObservable createObservable(Object target) {
				return new WritableSet(new HashSet(), type);
			}
		}

		Factory factory = new Factory();
		DetailObservableSet detailObservable = new DetailObservableSet(factory,
				observableValue, Object.class);
		assertEquals(factory.type, detailObservable.getElementType());

		try {
			factory.type = String.class;
			observableValue.setValue(new WritableSet(Arrays
					.asList(new Object[] { new Object() }), String.class));
			fail("if an element type is set this cannot be changed");
		} catch (AssertionFailedException e) {
		}
	}
}
