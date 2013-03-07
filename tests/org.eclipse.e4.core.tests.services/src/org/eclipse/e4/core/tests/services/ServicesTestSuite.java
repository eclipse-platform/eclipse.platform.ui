/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.tests.services;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Runs all e4 service tests.
 */
public class ServicesTestSuite extends TestSuite {
	public static Test suite() {
		return new ServicesTestSuite();
	}

	public ServicesTestSuite() {
		// no tests
	}
}
