/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.e4.core.internal.tests.di.extensions.InjectionMixedSuppliersTest;
import org.eclipse.e4.core.internal.tests.di.extensions.InjectionPreferencesTest;

public class CoreTestSuite extends TestSuite {
	public static Test suite() {
		return new CoreTestSuite();
	}

	public CoreTestSuite() {
		addTestSuite(InjectionPreferencesTest.class);
		addTestSuite(InjectionMixedSuppliersTest.class);
	}
}
