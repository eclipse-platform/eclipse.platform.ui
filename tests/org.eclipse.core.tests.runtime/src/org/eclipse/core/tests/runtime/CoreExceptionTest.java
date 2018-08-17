/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.core.tests.runtime;

import org.eclipse.core.runtime.*;

/**
 * Test cases for the Path class.
 */
public class CoreExceptionTest extends RuntimeTest {
	/**
	 * Need a zero argument constructor to satisfy the test harness.
	 * This constructor should not do any real work nor should it be
	 * called by user code.
	 */
	public CoreExceptionTest() {
		super(null);
	}

	public CoreExceptionTest(String name) {
		super(name);
	}

	public void testCoreException() {
		final String MESSAGE_STRING = "An exception has occurred";
		IStatus status = new Status(IStatus.ERROR, "org.eclipse.core.tests.runtime", 31415, MESSAGE_STRING, new NumberFormatException());

		CoreException e = new CoreException(status);

		assertEquals("1.0", status, e.getStatus());
		assertEquals("1.1", MESSAGE_STRING, e.getMessage());
	}
}
