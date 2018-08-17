/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class DiffsTest {
	/**
	 * Asserts that the {@link SetDiff#toString()} implementation doesn't throw
	 * a NPE if any of its properties are <code>null</code>.
	 */
	@Test
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
	@Test
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
