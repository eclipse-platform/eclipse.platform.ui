/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.cheatsheet.execution;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.cheatsheets.ICheatSheetManager;

/**
 * Mock object used in JUnit tests for command execution
 */

public class MockCheatSheetManager implements ICheatSheetManager {

	private Map dataStore = new HashMap();
	public String getCheatSheetID() {
		return "Mock";
	}

	public String getData(String key) {
		return (String)dataStore.get(key);
	}

	public void setData(String key, String data) {
		dataStore.put(key, data);
	}

}
