/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.scope;

import junit.framework.TestCase;

import org.eclipse.help.ui.internal.views.ScopeSet;
import org.eclipse.help.ui.internal.views.ScopeSetManager;

public class ScopeSetManagerTest extends TestCase {

	public void testAddScope() {
		String scopeName = "newScope1";
		ScopeSetManager manager = new ScopeSetManager();
		ScopeSet newScope = new ScopeSet(scopeName);
		manager.add(newScope);
		manager.save();
		manager = new ScopeSetManager();
		ScopeSet set = manager.findSet(scopeName);
		assertNotNull(set);
		assertEquals(scopeName, set.getName());
	}
	
	public void testAddAndRenameScope() {
		String oldScopeName = "newScope2";
		String newScopeName = "newScope3";
		ScopeSetManager manager = new ScopeSetManager();
		ScopeSet newScope = new ScopeSet(oldScopeName);
		manager.add(newScope);
		newScope.setName(newScopeName);
		manager.save();
		manager = new ScopeSetManager();
		ScopeSet set = manager.findSet(newScopeName);
		assertNotNull(set);
		assertEquals(newScopeName, set.getName());
		set = manager.findSet(oldScopeName);
		assertNull(set);
	}
	
	public void testAddAndDeleteScope() {
		String scopeName = "newScope4";
		ScopeSetManager manager = new ScopeSetManager();
		ScopeSet newScope = new ScopeSet(scopeName);
		manager.add(newScope);
		manager.save();
		
		manager = new ScopeSetManager();
		ScopeSet set = manager.findSet(scopeName);
		assertNotNull(set);
		assertEquals(scopeName, set.getName());
		manager.remove(set);
		
		manager = new ScopeSetManager();
		set = manager.findSet(scopeName);
		assertNull(set);
	}

}
