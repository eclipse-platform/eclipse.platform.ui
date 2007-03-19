/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.adaptable;

import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.expressions.ICountable;
import org.eclipse.core.expressions.IIterable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @since 3.3
 * 
 */
public class SelectionAdapterTest extends TestCase {

	public void testBasicSelectionEmpty() {
		ISelection empty = new ISelection() {

			public boolean isEmpty() {
				return true;
			}
		};
		ICountable countable = (ICountable) Platform.getAdapterManager()
				.getAdapter(empty, ICountable.class);
		assertEquals(0, countable.count());

		IIterable iterate = (IIterable) Platform.getAdapterManager()
				.getAdapter(empty, IIterable.class);
		assertFalse(iterate.iterator().hasNext());
	}

	public void testBasicSelection() {
		ISelection selection = new ISelection() {

			public boolean isEmpty() {
				return false;
			}
		};
		ICountable countable = (ICountable) Platform.getAdapterManager()
				.getAdapter(selection, ICountable.class);
		assertEquals(1, countable.count());

		IIterable iterate = (IIterable) Platform.getAdapterManager()
				.getAdapter(selection, IIterable.class);
		Iterator iterator = iterate.iterator();
		assertTrue(iterator.hasNext());
		Object o = iterator.next();
		assertTrue(o == selection);
	}

	public void testStructuredSelectionEmpty() {
		StructuredSelection selection = new StructuredSelection();
		ICountable countable = (ICountable) Platform.getAdapterManager()
				.getAdapter(selection, ICountable.class);
		assertEquals(0, countable.count());

		IIterable iterate = (IIterable) Platform.getAdapterManager()
				.getAdapter(selection, IIterable.class);
		assertFalse(iterate.iterator().hasNext());
	}

	public void testStructuredSelectionOne() {
		String obj = "me";
		StructuredSelection selection = new StructuredSelection(obj);
		ICountable countable = (ICountable) Platform.getAdapterManager()
				.getAdapter(selection, ICountable.class);
		assertEquals(1, countable.count());

		IIterable iterate = (IIterable) Platform.getAdapterManager()
				.getAdapter(selection, IIterable.class);
		Iterator iterator = iterate.iterator();
		assertTrue(iterator.hasNext());
		Object o = iterator.next();
		assertTrue(o == obj);
		assertFalse(iterator.hasNext());
	}

	public void testStructuredSelection() {
		String obj = "me";
		String obj2 = "you";
		StructuredSelection selection = new StructuredSelection(new Object[] {
				obj, obj2 });
		ICountable countable = (ICountable) Platform.getAdapterManager()
				.getAdapter(selection, ICountable.class);
		assertEquals(2, countable.count());

		IIterable iterate = (IIterable) Platform.getAdapterManager()
				.getAdapter(selection, IIterable.class);
		Iterator iterator = iterate.iterator();
		assertTrue(iterator.hasNext());
		Object o = iterator.next();
		assertTrue(o == obj);
		o = iterator.next();
		assertTrue(o == obj2);
		assertFalse(iterator.hasNext());
	}
}
