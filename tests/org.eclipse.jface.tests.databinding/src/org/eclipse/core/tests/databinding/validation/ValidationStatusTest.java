/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

package org.eclipse.core.tests.databinding.validation;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.junit.Test;

/**
 * @since 1.1
 */
public class ValidationStatusTest {
	@Test
	public void testEqualsAndHashCode() throws Exception {
		String message = "error";
		Exception e = new IllegalArgumentException();
		IStatus status1 = ValidationStatus.error(message, e);
		IStatus status2 = ValidationStatus.error(message, e);

		assertEquals(status1, status2);
		assertEquals(status1.hashCode(), status2.hashCode());
	}
}
