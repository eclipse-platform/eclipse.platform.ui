/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.preferences;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.internal.runtime.ListenerList;

/**
 * @since 3.1
 */
public class ListenerRegistry {

	static final Object[] EMPTY_LIST = new Object[0];
	Map registry = new HashMap();

	public synchronized Object[] getListeners(String path) {
		ListenerList list = (ListenerList) registry.get(path);
		return list == null ? EMPTY_LIST : list.getListeners();
	}

	public synchronized void add(String path, Object listener) {
		ListenerList list = (ListenerList) registry.get(path);
		if (list == null)
			list = new ListenerList(ListenerList.IDENTITY);
		list.add(listener);
		registry.put(path, list);
	}

	public synchronized void remove(String path, Object listener) {
		ListenerList list = (ListenerList) registry.get(path);
		if (list == null)
			return;
		list.remove(listener);
		if (list.isEmpty())
			registry.remove(path);
	}

}
