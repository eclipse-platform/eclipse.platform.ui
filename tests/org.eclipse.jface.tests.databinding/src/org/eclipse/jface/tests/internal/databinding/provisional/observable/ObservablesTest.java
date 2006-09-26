/*******************************************************************************
 * Copyright (c) 2006 Cerner Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.provisional.observable;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.observable.list.ObservableList;
import org.eclipse.jface.internal.databinding.internal.observable.UnmodifiableObservableList;
import org.eclipse.jface.internal.databinding.provisional.observable.Observables;

public class ObservablesTest extends TestCase {
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
