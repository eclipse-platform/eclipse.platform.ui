/*******************************************************************************
 * Copyright (c) 2006, 2007 Cerner Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableList;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class ObservablesTest extends AbstractDefaultRealmTestCase {	
	public void testUnmodifableObservableListExceptions() throws Exception {
		try {
			Observables.unmodifiableObservableList(null);
			fail("IllegalArgumentException should have been thrown.");
		} catch (IllegalArgumentException e) {
		}
	}
	
	public void testUnmodifiableObservableList() throws Exception {
		IObservableList unmodifiable = Observables.unmodifiableObservableList(new ObservableListStub(new ArrayList(0), String.class));
		assertTrue(unmodifiable instanceof UnmodifiableObservableList);
	}
	
	private static class ObservableListStub extends ObservableList {
		/**
		 * @param wrappedList
		 * @param elementType
		 */
		protected ObservableListStub(List wrappedList, Object elementType) {
			super(wrappedList, elementType);
		}		
	}
}
