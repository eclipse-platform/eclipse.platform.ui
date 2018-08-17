/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.adaptable;

import java.util.Iterator;

import org.eclipse.core.expressions.ICountable;
import org.eclipse.core.expressions.IIterable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import junit.framework.TestCase;

/**
 * @since 3.3
 *
 */
public class SelectionAdapterTest extends TestCase {

	public void testBasicSelectionEmpty() {
		ISelection empty = new ISelection() {

			@Override
			public boolean isEmpty() {
				return true;
			}
		};
		ICountable countable = Platform.getAdapterManager().getAdapter(empty, ICountable.class);
		assertEquals(0, countable.count());

		IIterable<?> iterate = Platform.getAdapterManager().getAdapter(empty, IIterable.class);
		assertFalse(iterate.iterator().hasNext());
	}

	public void testBasicSelection() {
		ISelection selection = new ISelection() {

			@Override
			public boolean isEmpty() {
				return false;
			}
		};
		ICountable countable = Platform.getAdapterManager().getAdapter(selection, ICountable.class);
		assertEquals(1, countable.count());

		IIterable<?> iterate = Platform.getAdapterManager().getAdapter(selection, IIterable.class);
		Iterator<?> iterator = iterate.iterator();
		assertTrue(iterator.hasNext());
		Object o = iterator.next();
		assertTrue(o == selection);
	}

	public void testStructuredSelectionEmpty() {
		StructuredSelection selection = new StructuredSelection();
		ICountable countable = Platform.getAdapterManager().getAdapter(selection, ICountable.class);
		assertEquals(0, countable.count());

		IIterable<?> iterate = Platform.getAdapterManager().getAdapter(selection, IIterable.class);
		assertFalse(iterate.iterator().hasNext());
	}

	public void testStructuredSelectionOne() {
		String obj = "me";
		StructuredSelection selection = new StructuredSelection(obj);
		ICountable countable = Platform.getAdapterManager().getAdapter(selection, ICountable.class);
		assertEquals(1, countable.count());

		IIterable<?> iterate = Platform.getAdapterManager().getAdapter(selection, IIterable.class);
		Iterator<?> iterator = iterate.iterator();
		assertTrue(iterator.hasNext());
		Object o = iterator.next();
		assertTrue(o == obj);
		assertFalse(iterator.hasNext());
	}

	public void testStructuredSelection() {
		String obj = "me";
		String obj2 = "you";
		StructuredSelection selection = new StructuredSelection(new Object[] { obj, obj2 });
		ICountable countable = Platform.getAdapterManager().getAdapter(selection, ICountable.class);
		assertEquals(2, countable.count());

		IIterable<?> iterate = Platform.getAdapterManager().getAdapter(selection, IIterable.class);
		Iterator<?> iterator = iterate.iterator();
		assertTrue(iterator.hasNext());
		Object o = iterator.next();
		assertTrue(o == obj);
		o = iterator.next();
		assertTrue(o == obj2);
		assertFalse(iterator.hasNext());
	}
}
