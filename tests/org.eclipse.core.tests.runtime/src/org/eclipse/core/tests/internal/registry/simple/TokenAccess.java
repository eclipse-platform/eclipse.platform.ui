/*******************************************************************************
 * Copyright (c) 2005, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.registry.simple;

import junit.framework.*;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

/**
 * Tests registry token-based access rules.
 * @since 3.2
 */
public class TokenAccess extends TestCase {

	public TokenAccess() {
		super();
	}

	public TokenAccess(String name) {
		super(name);
	}

	/**
	 * Tests token access to sensetive registry methods
	 */
	public void testControlledAccess() {
		Object tokenGood = new Object();
		Object tokenBad = new Object();

		// registry created with no token
		IExtensionRegistry registry = RegistryFactory.createRegistry(null, null, null);
		assertNotNull(registry);
		// and stopped with no token - should be no exception
		registry.stop(null);

		// registry created with no token
		registry = RegistryFactory.createRegistry(null, null, null);
		assertNotNull(registry);
		// and stopped with a bad - should be no exception
		registry.stop(tokenBad);

		// registry created with a good token
		registry = RegistryFactory.createRegistry(null, tokenGood, null);
		assertNotNull(registry);
		// and stopped with a good token - should be no exception
		registry.stop(tokenGood);

		// registry created with a good token
		registry = RegistryFactory.createRegistry(null, tokenGood, null);
		assertNotNull(registry);
		// and stopped with a bad token - should be an exception
		boolean bException = false;
		try {
			registry.stop(tokenBad);
		} catch (IllegalArgumentException e) {
			// this is good; this is expected
			bException = true;
		}
		assertTrue(bException);
	}

	public static Test suite() {
		return new TestSuite(TokenAccess.class);
	}

}
