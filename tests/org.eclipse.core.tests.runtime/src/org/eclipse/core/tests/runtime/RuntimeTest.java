/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime;

import junit.framework.TestCase;
/**
 * Common superclass for all runtime tests.
 */
public abstract class RuntimeTest extends TestCase {
/**
 * Constructor required by test framework.
 */
public RuntimeTest(String name) {
	super(name);
}
/**
 * Fails the test due to the given exception.
 * @param message
 * @param e
 */
public void fail(String message, Exception e) {
	fail(message + ": " + e);
}
}

