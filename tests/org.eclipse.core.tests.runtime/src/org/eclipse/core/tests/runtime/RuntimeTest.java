/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import org.eclipse.core.tests.harness.CoreTest;

/**
 * Common superclass for all runtime tests.
 */
public abstract class RuntimeTest extends CoreTest {
	public static final String PI_RUNTIME_TESTS = RuntimeTestsPlugin.PI_RUNTIME_TESTS;

	/**
	 * Constructor required by test framework.
	 */
	public RuntimeTest(String name) {
		super(name);
	}

	/**
	 * Constructor required by test framework.
	 */
	public RuntimeTest() {
		super();
	}

}
