/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.junit.Test;

/**
 * Test to ensure that <code>WorkbenchPlugin.createExtension()</code> will only
 * throw <code>CoreException</code>s if there is a problem creating the
 * extension.
 *
 * @since 3.0
 */
public class Bug42616Test {

	@Test
	public void testErrorCondition() {
		CoreException e = assertThrows(CoreException.class, () -> WorkbenchPlugin.createExtension(null, null));
		// ensure that exception has a root cause.
		assertNotNull("Cause is null", e.getStatus().getException());
	}
}
