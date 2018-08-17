/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
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
		SessionTestSuite runtimeSessionTests = new SessionTestSuite(RuntimeTest.PI_RUNTIME_TESTS, AllTests.class.getName());
		runtimeSessionTests.addTest(ExtensionRegistryStaticTest.suite());
		return runtimeSessionTests;
	}
}
