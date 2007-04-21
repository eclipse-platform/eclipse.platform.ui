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

package org.eclipse.core.tests.databinding.validation;

import junit.framework.TestCase;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;

/**
 * @since 1.1
 */
public class ValidationStatusTest extends TestCase {
	public void testEqualsAndHashCode() throws Exception {
		String message = "error";
		Exception e = new IllegalArgumentException();
		IStatus status1 = ValidationStatus.error(message, e);
		IStatus status2 = ValidationStatus.error(message, e);
		
		assertEquals(status1, status2);
		assertEquals(status1.hashCode(), status2.hashCode());
	}
}
