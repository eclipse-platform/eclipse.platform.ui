/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.session;

import junit.framework.Test;
import junit.framework.TestCase;
import org.eclipse.core.tests.internal.registry.ExtensionRegistryStaticTest;
import org.eclipse.core.tests.runtime.RuntimeTest;
import org.eclipse.core.tests.session.SessionTestSuite;

/**
 * @since 3.1 
 */
public class AllTests extends TestCase {
	public static Test suite() {
		SessionTestSuite runtimeSessionTests = new SessionTestSuite(RuntimeTest.PI_RUNTIME_TESTS);
		runtimeSessionTests.addTest(ExtensionRegistryStaticTest.suite());
		return runtimeSessionTests;
	}
}
