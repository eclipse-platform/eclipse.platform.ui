/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.viewers;

import org.eclipse.jface.internal.databinding.viewers.ViewerElementWrapper;
import org.eclipse.jface.viewers.IElementComparer;

import junit.framework.TestCase;

/**
 * @since 3.2
 *
 */
public class ViewerElementWrapperTest extends TestCase {
	private ViewerElementWrapper wrapper;
	private Object element;
	private IElementComparer comparer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		element = new ElementStub(0);
		comparer = new IdentityElementComparer();
		wrapper = new ViewerElementWrapper(element, comparer);
	}

	public void testConstructor_NullComparer() {
		try {
			new ViewerElementWrapper(element, null);
			fail("Expected NullPointerException");
		} catch (NullPointerException expected) {
		}
	}

	public void testEquals() {
		assertFalse(wrapper.equals(null));
		assertTrue(wrapper.equals(wrapper));
		assertTrue(wrapper.equals(new ViewerElementWrapper(element, comparer)));
	}

	public void testHashCode() {
		int hash = 0;
		element = new ElementStub(hash);
		wrapper = new ViewerElementWrapper(element, comparer);
		assertEquals(System.identityHashCode(element), wrapper.hashCode());
		assertEquals(hash, element.hashCode());
	}

	static class ElementStub {
		private final int hash;

		public ElementStub(int hash) {
			this.hash = hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			if (obj.getClass() != getClass())
				return false;
			ElementStub that = (ElementStub) obj;
			return this.hash == that.hash;
		}

		@Override
		public int hashCode() {
			return hash;
		}
	}

	static class IdentityElementComparer implements IElementComparer {
		@Override
		public boolean equals(Object a, Object b) {
			return a == b;
		}

		@Override
		public int hashCode(Object element) {
			return System.identityHashCode(element);
		}
	}
}
