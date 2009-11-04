/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IFilterDescriptor;

/**
 * Tests for {@link org.eclipse.core.resources.IFileInfoFilterFactory}.
 */
public class IFilterDescriptorTest extends ResourceTest {
	public static Test suite() {
		return new TestSuite(IFilterDescriptorTest.class);
	}

	public void testGetFilter() {
		IFilterDescriptor descriptor = getWorkspace().getFilterDescriptor("");
		assertNull("1.0", descriptor);
		descriptor = getWorkspace().getFilterDescriptor("org.eclipse.core.resources.regexFilter");
		assertNotNull("1.1", descriptor);
	}
}