/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 283204)
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.viewers;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.junit.Test;

public class CheckableCheckedElementsObservableSetTest extends AbstractDefaultRealmTestCase {
	@Test
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

		IObservableSet<String> set = ViewerProperties.checkedElements(String.class).observe(checkable);

		set.add("Test1");
		set.add("Test2");
		assertEquals(2, set.size());

		// test
		set.clear();
	}
}
