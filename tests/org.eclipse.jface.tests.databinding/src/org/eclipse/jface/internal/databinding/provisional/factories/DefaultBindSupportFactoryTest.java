/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.factories;

import org.eclipse.jface.internal.databinding.provisional.factories.DefaultBindSupportFactory;

import junit.framework.TestCase;

/**
 * @since 3.2
 *
 */
public class DefaultBindSupportFactoryTest extends TestCase {
	/**
	 * Asserts that the instances of Boolean that are returned from
	 * {@link DefaultBindSupportFactory#isAssignableFromTo()} are not new
	 * instances of Boolean.
	 */
	public void test_isAssignableFromToBooleanInstances() {
		DefaultBindSupportFactory factory = new DefaultBindSupportFactory();
		Boolean b1 = factory.isAssignableFromTo(String.class, String.class);
		Boolean b2 = factory.isAssignableFromTo(String.class, String.class);
		
		assertNotNull(b1);
		assertNotNull(b2);
		assertTrue(b1.booleanValue());
		assertSame(b1, b2);
		
		b1 = factory.isAssignableFromTo(String.class, Integer.class);
		b2 = factory.isAssignableFromTo(String.class, Integer.class);
		
		assertNotNull(b1);
		assertNotNull(b2);
		assertFalse(b1.booleanValue());
		assertSame(b1, b2);
	}
}
