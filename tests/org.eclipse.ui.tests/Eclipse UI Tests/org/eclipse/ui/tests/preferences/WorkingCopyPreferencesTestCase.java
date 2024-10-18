/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
package org.eclipse.ui.tests.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.WorkingCopyManager;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

public class WorkingCopyPreferencesTestCase {

	/*
	 * See bug 94926 - WorkingCopyPreferences.remove(key) not working
	 */
	@Test
	public void testRemoveKey() throws BackingStoreException {

		// set the value in the real node
		String key = "key";
		String value = "value";
		IEclipsePreferences eNode = InstanceScope.INSTANCE.getNode("working.copy.tests.testRemoveKey");
		eNode.put(key, value);
		assertEquals("1.0", value, eNode.get(key, null));

		// create a working copy
		WorkingCopyManager manager = new WorkingCopyManager();
		IEclipsePreferences prefs = manager.getWorkingCopy(eNode);
		prefs.remove(key);

		// apply the changes
		manager.applyChanges();

		// see if our change was applied
		assertNull("3.0", eNode.get(key, null));
	}

	@Test
	public void testRemoveNode() throws BackingStoreException {
		// set the value in the real node
		String key = "key";
		String value = "value";
		IEclipsePreferences eNode = InstanceScope.INSTANCE.getNode("working.copy.tests.testRemoveKey");
		eNode.put(key, value);
		assertEquals("1.0", value, eNode.get(key, null));

		// create a working copy
		WorkingCopyManager manager = new WorkingCopyManager();
		IEclipsePreferences prefs = manager.getWorkingCopy(eNode);

		// remove the node
		prefs.removeNode();

		// apply the changes
		manager.applyChanges();
	}
}
