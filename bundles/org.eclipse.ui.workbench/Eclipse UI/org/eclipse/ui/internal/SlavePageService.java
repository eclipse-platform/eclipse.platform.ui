/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPageService;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.services.IDisposable;

/**
 * @since 3.4
 * 
 */
public class SlavePageService implements IPageService, IDisposable {

	private IPageService parent;
	private ListenerList pageListeners = new ListenerList(ListenerList.IDENTITY);
	private ListenerList perspectiveListeners = new ListenerList(
			ListenerList.IDENTITY);

	public SlavePageService(IPageService parent) {
		if (parent == null) {
			throw new IllegalArgumentException(
					"Parent IPageService cannot be null"); //$NON-NLS-1$
		}
		this.parent = parent;
	}

	public void addPageListener(IPageListener listener) {
		pageListeners.add(listener);
		parent.addPageListener(listener);
	}

	public void addPerspectiveListener(IPerspectiveListener listener) {
		perspectiveListeners.add(listener);
		parent.addPerspectiveListener(listener);
	}

	public IWorkbenchPage getActivePage() {
		return parent.getActivePage();
	}

	public void removePageListener(IPageListener listener) {
		pageListeners.remove(listener);
		parent.removePageListener(listener);
	}

	public void removePerspectiveListener(IPerspectiveListener listener) {
		perspectiveListeners.remove(listener);
		parent.removePerspectiveListener(listener);
	}

	public void dispose() {
		Object[] listeners = pageListeners.getListeners();
		
		for(int i = 0; i < listeners.length; i++) {
			parent.removePageListener((IPageListener) listeners[i]);
		}
		pageListeners.clear();
		
		listeners = perspectiveListeners.getListeners();
		for(int i = 0; i < listeners.length; i++) {
			parent.removePerspectiveListener((IPerspectiveListener) listeners[i]);
		}
		perspectiveListeners.clear();
	}

}
