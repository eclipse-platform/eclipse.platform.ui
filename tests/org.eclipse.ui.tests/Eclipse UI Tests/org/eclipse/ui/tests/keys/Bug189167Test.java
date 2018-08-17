/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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

package org.eclipse.ui.tests.keys;

import junit.framework.TestCase;

import org.eclipse.jface.bindings.Binding;

/**
 * Tests Bug 189167
 *
 * @since 3.4
 */
public class Bug189167Test extends TestCase {

	private Binding createTestBinding() {
		return new TestBinding("commandId", "schemeId", "contextId", "locale", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"platform", 0, null); //$NON-NLS-1$
	}

	public void testBindingsEqual() {
		Binding one = createTestBinding();
		Binding two = createTestBinding();
		assertEquals(one, two);
	}

	public void testHashCodeEquals(){
		Binding one = createTestBinding();
		Binding two = createTestBinding();
		Binding b3  = new TestBinding("commandID", "schemeID", "contextID", "locale", "platform", 1, null);
		assertEquals(one, two);
		assertEquals(one.hashCode(), two.hashCode());

		assertFalse(one.equals(b3));
	}
}
