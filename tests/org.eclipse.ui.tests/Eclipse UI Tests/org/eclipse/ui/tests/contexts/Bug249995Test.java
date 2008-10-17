/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.contexts;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.contexts.SlaveContextService;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.5
 * 
 */
public class Bug249995Test extends UITestCase {
	public Bug249995Test(String test) {
		super(test);
	}

	public void testMultipleSlaveActivations() throws Exception {
		IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
		IContextService cs = (IContextService) window
				.getService(IContextService.class);
		assertTrue(cs instanceof SlaveContextService);
		Field localActivationsField = SlaveContextService.class
				.getDeclaredField("fLocalActivations");
		localActivationsField.setAccessible(true);
		Field parentActivationsField = SlaveContextService.class
				.getDeclaredField("fParentActivations");
		parentActivationsField.setAccessible(true);

		Map local = (Map) localActivationsField.get(cs);
		int localStart = local.size();
		Set parent = (Set) parentActivationsField.get(cs);
		int parentStart = parent.size();
		for (int i = 0; i < 5; i++) {
			window.getActivePage().hideActionSet(
					"org.eclipse.ui.tests.contexts.Bug249995Test");
			assertEquals("on: " + i, localStart, local.size());
			assertEquals("on: " + i, parentStart, parent.size());
		}
	}
}
