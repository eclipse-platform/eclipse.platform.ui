/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 283204)
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.viewers;

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;

public class CheckableCheckedElementsObservableSetTest extends
		AbstractDefaultRealmTestCase {
	public void testClear() {
		// init
		ICheckable checkable = new ICheckable() {

			@Override
			public void addCheckStateListener(ICheckStateListener listener) {
			}

			@Override
			public boolean getChecked(Object element) {
				return false;
			}

			@Override
			public void removeCheckStateListener(ICheckStateListener listener) {

			}

			@Override
			public boolean setChecked(Object element, boolean state) {
				return false;
			}

		};

		// CheckableCheckedElementsObservableSet set = new
		// CheckableCheckedElementsObservableSet(Realm.getDefault(),
		// checkable, String.class);
		IObservableSet set = ViewersObservables.observeCheckedElements(
				checkable, String.class);
		set.add("Test1");
		set.add("Test2");
		assertEquals(2, set.size());

		// test
		set.clear();
	}
}
