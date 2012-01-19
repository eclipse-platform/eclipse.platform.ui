/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.preferences;

import java.util.*;
import org.eclipse.core.runtime.preferences.AbstractPreferenceStorage;

public class TestNodeStorage extends AbstractPreferenceStorage {

	private Map<String, Properties> storage = new HashMap<String, Properties>();

	public Properties load(String nodePath) {
		return storage.get(nodePath);
	}

	public void save(String nodePath, Properties properties) {
		storage.put(nodePath, properties);
	}

	public String[] childrenNames(String nodePath) {
		return null;
	}

	public void removed(String nodePath) {
		storage.remove(nodePath);
	}

}
