/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.WorkingCopyManager;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.osgi.service.prefs.BackingStoreException;

public class WorkingCopyPreferencesTestCase extends UITestCase {

	public WorkingCopyPreferencesTestCase(String name) {
		super(name);
	}

	/*
	 * See bug 94926 - WorkingCopyPreferences.remove(key) not working
	 */
	public void testRemoveKey() {

		// set the value in the real node
		String key = "key";
		String value = "value";
		IEclipsePreferences eNode = new InstanceScope().getNode("working.copy.tests.testRemoveKey");
		eNode.put(key, value);
		assertEquals("1.0", value, eNode.get(key, null));

		// create a working copy
		WorkingCopyManager manager = new WorkingCopyManager();
		IEclipsePreferences prefs = manager.getWorkingCopy(eNode);
		prefs.remove(key);

		// apply the changes
		try {
			manager.applyChanges();
		} catch (BackingStoreException e) {
			fail("2.99", e);
		}

		// see if our change was applied
		assertNull("3.0", eNode.get(key, null));
	}

	public void testRemoveNode() {
		// set the value in the real node
		String key = "key";
		String value = "value";
		IEclipsePreferences eNode = new InstanceScope().getNode("working.copy.tests.testRemoveKey");
		eNode.put(key, value);
		assertEquals("1.0", value, eNode.get(key, null));

		// create a working copy
		WorkingCopyManager manager = new WorkingCopyManager();
		IEclipsePreferences prefs = manager.getWorkingCopy(eNode);
		
		// remove the node
		try {
			prefs.removeNode();
		} catch (BackingStoreException e) {
			fail("2.99", e);
		}

		// apply the changes
		try {
			manager.applyChanges();
		} catch (BackingStoreException e) {
			fail("3.99", e);
		}
	}
}
