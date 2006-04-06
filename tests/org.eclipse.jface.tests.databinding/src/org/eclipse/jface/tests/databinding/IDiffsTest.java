/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding;

import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.jface.internal.databinding.provisional.observable.list.ListDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.list.ListDiffEntry;
import org.eclipse.jface.internal.databinding.provisional.observable.set.SetDiff;
import org.eclipse.jface.internal.databinding.provisional.observable.value.ValueDiff;

/**
 * @since 3.2
 *
 */
public class IDiffsTest extends TestCase {
	/**
	 * Asserts that the {@link SetDiff#toString()} implementation doesn't throw a NPE if any of its properties are <code>null</code>.
	 */
	public void test_SetDiff() {
		SetDiff diff = new SetDiff() {
			public Set getAdditions() {
				return null;
			}

			public Set getRemovals() {
				return null;
			}
		};
		
		try {
			diff.toString();
			assertTrue(true);
		} catch (NullPointerException e) {
			fail("NPE was thrown.");
		}
	}
	
	/**
	 * Asserts that the {@link LinkDiffEntry#toString()} implementation doesn't throw a NPE if any of its properties are <code>null</code>.
	 */
	public void test_ListDiffEntry() {
		ListDiffEntry entry = new ListDiffEntry() {
			public Object getElement() {
				return null;
			}

			public int getPosition() {
				return 0;
			}

			public boolean isAddition() {
				return false;
			}
		};
		
		try {
			entry.toString();
			assertTrue(true);
		} catch (NullPointerException e) {
			fail("NPE was thrown.");
		}
	}
	
	/**
	 * Asserts that the {@link ListDiff#toString()} implementation doesn't throw a NPE if any of its properties are <code>null</code>.
	 *
	 */
	public void test_ListDiff() {
		ListDiff diff = new ListDiff() {
			public ListDiffEntry[] getDifferences() {
				return null;
			}
		};
		
		try {
			diff.toString();
			assertTrue(true);
		} catch (NullPointerException e) {
			fail("NPE was thrown.");
		}
	}
	
	/**
	 * Asserts that if the {@link ListDiff#toString()} implementation doesn't throw a NEP if the differences contains a <code>null</code> item.
	 */
	public void test_ListDiff2() {
		ListDiff diff = new ListDiff() {
			public ListDiffEntry[] getDifferences() {
				return new ListDiffEntry[1];
			}
		};
		
		try {
			diff.toString();
			assertTrue(true);
		} catch (NullPointerException e) {
			fail("NPE was thrown.");
		}
	}
	
	/**
	 * Asserts that if the {@link ValueDiff#toString()} implementation doesn't throw a NPE if any of its properties are <code>null</code>.
	 *
	 */
	public void test_ValueDiff() {
		ValueDiff diff = new ValueDiff() {
			public Object getNewValue() {
				return null;
			}

			public Object getOldValue() {
				return null;
			}
		};
		
		try {
			diff.toString();
			assertTrue(true);
		} catch (NullPointerException e) {
			fail("NPE was thrown.");
		}
	}

}
