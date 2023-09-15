/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.search.core.tests;

import static org.junit.Assert.fail;

import org.junit.Test;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

/**
 */
public class ExtensionPointTest {
	private static final String EXTENSION_POINT_ID= "org.eclipse.search.textSearchEngine"; //$NON-NLS-1$

	private static final String EXAMPLE_EXTENSION= "sampleSearchEngine"; //$NON-NLS-1$

	private static final String ATTRIB_ID= "id"; //$NON-NLS-1$

	@Test
	public void testExtensionFound() throws Exception {
		IConfigurationElement[] extensions= Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
		for (IConfigurationElement curr : extensions) {
			if (EXAMPLE_EXTENSION.equals(curr.getAttribute(ATTRIB_ID))) {
				return;
			}
		}
		fail("Sample search engine extension not found");
	}
}
