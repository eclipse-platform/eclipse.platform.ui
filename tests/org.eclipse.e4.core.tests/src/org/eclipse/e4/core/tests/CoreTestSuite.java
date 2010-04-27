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

import org.eclipse.e4.core.internal.tests.contexts.ComplexDisposalTest;
import org.eclipse.e4.core.internal.tests.contexts.ParentContextDisposalTest;
import org.eclipse.e4.core.internal.tests.di.AtInjectTest;
import org.eclipse.e4.core.internal.tests.di.DisposeClassLinkTest;
import org.eclipse.e4.core.internal.tests.di.InjectionOrderTest;
import org.eclipse.e4.core.internal.tests.di.InvokeTest;
import org.eclipse.e4.core.internal.tests.di.extensions.InjectionEventTest;
import org.eclipse.e4.core.internal.tests.di.extensions.InjectionMixedSuppliersTest;
import org.eclipse.e4.core.internal.tests.di.extensions.InjectionPreferencesTest;

public class CoreTestSuite extends TestSuite {
	public static Test suite() {
		return new CoreTestSuite();
	}

	public CoreTestSuite() {
		addTestSuite(InjectionPreferencesTest.class);
		addTestSuite(InjectionMixedSuppliersTest.class);
		addTestSuite(InjectionEventTest.class);
		
		// DI
		addTestSuite(InjectionOrderTest.class);
		addTestSuite(InvokeTest.class);
		addTest(AtInjectTest.suite());
		
		// Contexts
		addTestSuite(ParentContextDisposalTest.class);
		addTestSuite(ComplexDisposalTest.class);
		addTestSuite(DisposeClassLinkTest.class);
	}
}
