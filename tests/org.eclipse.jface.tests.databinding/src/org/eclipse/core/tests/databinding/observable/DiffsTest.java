/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.ValueDiff;

/**
 * @since 3.2
 *
 */
public class DiffsTest extends TestCase {
	/**
	 * Asserts that the {@link SetDiff#toString()} implementation doesn't throw
	 * a NPE if any of its properties are <code>null</code>.
	 */
	public void test_SetDiff() {
		SetDiff diff = new SetDiff() {
			@Override
			public Set<?> getAdditions() {
				return null;
			}

			@Override
			public Set<?> getRemovals() {
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
	 * Asserts that if the {@link ValueDiff#toString()} implementation doesn't
	 * throw a NPE if any of its properties are <code>null</code>.
	 *
	 */
	public void test_ValueDiff() {
		ValueDiff diff = new ValueDiff() {
			@Override
			public Object getNewValue() {
				return null;
			}

			@Override
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
