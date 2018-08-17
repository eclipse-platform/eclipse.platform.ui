/*******************************************************************************
 * Copyright (c) 2006, 2007 Cerner Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableList;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

public class ObservablesTest extends AbstractDefaultRealmTestCase {
	@Test
	public void testUnmodifableObservableListExceptions() throws Exception {
		try {
			Observables.unmodifiableObservableList(null);
			fail("IllegalArgumentException should have been thrown.");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testUnmodifiableObservableList() throws Exception {
		IObservableList unmodifiable = Observables.unmodifiableObservableList(new ObservableListStub(
				new ArrayList<Object>(0), String.class));
		assertTrue(unmodifiable instanceof UnmodifiableObservableList);
	}

	private static class ObservableListStub extends ObservableList {
		/**
		 * @param wrappedList
		 * @param elementType
		 */
		protected ObservableListStub(List<Object> wrappedList, Object elementType) {
			super(wrappedList, elementType);
		}
	}
}
