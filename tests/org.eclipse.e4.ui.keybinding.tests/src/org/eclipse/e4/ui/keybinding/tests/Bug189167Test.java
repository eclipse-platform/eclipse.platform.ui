/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.keybinding.tests;

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
