/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.outline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * XMLCore.java
 */
public class XMLCore {
	
	private static XMLCore inst;
	
	public static XMLCore getDefault() {
		if (inst == null) {
			inst= new XMLCore();
		}
			
		return inst;
	}
	
	private List fModelChangeListeners= new ArrayList();
	
	private XMLCore() { }

	public void addAntModelListener(IAntModelListener listener) {
		synchronized (fModelChangeListeners) {
			fModelChangeListeners.add(listener);
		}
	}
	
	public void removeAntModelListener(IAntModelListener listener) {
		synchronized (fModelChangeListeners) {
			fModelChangeListeners.remove(listener);
		}
	}
	
	public void notifyAntModelListeners(AntModelChangeEvent event) {
		Iterator i;
		synchronized (fModelChangeListeners) {
			i= new ArrayList(fModelChangeListeners).iterator();
		}
		while (i.hasNext()) {
			((IAntModelListener)i.next()).antModelChanged(event);
		}
	}
}
