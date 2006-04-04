/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.alias;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.tests.resources.ResourceTest;

/**
 * Tests out of sync cases and refreshLocal in the presence of duplicate
 * resources.
 */
public class SyncAliasTest extends ResourceTest {
	public static Test suite() {
		return new TestSuite(SyncAliasTest.class);
	}

	public SyncAliasTest() {
		super();
	}

	public SyncAliasTest(String name) {
		super(name);
	}

	public void testSimple() {
		// TODO
	}
}
