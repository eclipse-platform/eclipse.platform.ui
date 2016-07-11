/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
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
	private ListenerList<IPageListener> pageListeners = new ListenerList<>(ListenerList.IDENTITY);
	private ListenerList<IPerspectiveListener> perspectiveListeners = new ListenerList<>(
			ListenerList.IDENTITY);

	public SlavePageService(IPageService parent) {
		if (parent == null) {
			throw new IllegalArgumentException(
					"Parent IPageService cannot be null"); //$NON-NLS-1$
		}
		this.parent = parent;
	}

	@Override
	public void addPageListener(IPageListener listener) {
		pageListeners.add(listener);
		parent.addPageListener(listener);
	}

	@Override
	public void addPerspectiveListener(IPerspectiveListener listener) {
		perspectiveListeners.add(listener);
		parent.addPerspectiveListener(listener);
	}

	@Override
	public IWorkbenchPage getActivePage() {
		return parent.getActivePage();
	}

	@Override
	public void removePageListener(IPageListener listener) {
		pageListeners.remove(listener);
		parent.removePageListener(listener);
	}

	@Override
	public void removePerspectiveListener(IPerspectiveListener listener) {
		perspectiveListeners.remove(listener);
		parent.removePerspectiveListener(listener);
	}

	@Override
	public void dispose() {

		for (IPageListener listener : pageListeners) {
			parent.removePageListener(listener);
		}
		pageListeners.clear();

		for (IPerspectiveListener listener : perspectiveListeners) {
			parent.removePerspectiveListener(listener);
		}
		perspectiveListeners.clear();
	}

}
