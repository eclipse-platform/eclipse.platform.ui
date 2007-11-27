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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.services.IDisposable;

/**
 * @since 3.4
 * 
 */
public class SlaveSelectionService implements ISelectionService, IDisposable {

	private ListenerList postListeners = new ListenerList(ListenerList.IDENTITY);
	private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
	private Map listenersToPartId = new HashMap();
	private Map postListenersToPartId = new HashMap();

	private ISelectionService parentSelectionService;

	/**
	 * @param parentSelectionService
	 */
	public SlaveSelectionService(ISelectionService parentSelectionService) {
		if (parentSelectionService == null) {
			throw new IllegalArgumentException(
					"The parent selection service cannot be null"); //$NON-NLS-1$
		}
		this.parentSelectionService = parentSelectionService;
	}

	public void addPostSelectionListener(ISelectionListener listener) {
		postListeners.add(listener);
		parentSelectionService.addPostSelectionListener(listener);
	}

	public void addPostSelectionListener(String partId,
			ISelectionListener listener) {
		listenersToPartId.put(listener, partId);
		parentSelectionService.addPostSelectionListener(partId, listener);
	}

	public void addSelectionListener(ISelectionListener listener) {
		listeners.add(listener);
		parentSelectionService.addSelectionListener(listener);
	}

	public void addSelectionListener(String partId, ISelectionListener listener) {
		postListenersToPartId.put(listener, partId);
		parentSelectionService.addPostSelectionListener(partId, listener);
	}

	public ISelection getSelection() {
		return parentSelectionService.getSelection();
	}

	public ISelection getSelection(String partId) {
		return parentSelectionService.getSelection(partId);
	}

	public void removePostSelectionListener(ISelectionListener listener) {
		postListeners.remove(listener);
		parentSelectionService.removePostSelectionListener(listener);
	}

	public void removePostSelectionListener(String partId,
			ISelectionListener listener) {
		postListenersToPartId.remove(listener);
		parentSelectionService.removePostSelectionListener(partId, listener);
	}

	public void removeSelectionListener(ISelectionListener listener) {
		listeners.remove(listener);
		parentSelectionService.removeSelectionListener(listener);
	}

	public void removeSelectionListener(String partId,
			ISelectionListener listener) {
		listenersToPartId.remove(listener);
		parentSelectionService.removeSelectionListener(partId, listener);
	}

	public void dispose() {
		Object list[] = listeners.getListeners();

		for (int i = 0; i < list.length; i++) {
			parentSelectionService
					.removeSelectionListener((ISelectionListener) list[i]);
		}
		listeners.clear();

		list = postListeners.getListeners();
		for (int i = 0; i < list.length; i++) {
			parentSelectionService
					.removePostSelectionListener((ISelectionListener) list[i]);
		}
		postListeners.clear();

		Iterator i = listenersToPartId.keySet().iterator();
		while (i.hasNext()) {
			Object listener = i.next();
			parentSelectionService.removeSelectionListener(
					(String) listenersToPartId.get(listener),
					(ISelectionListener) listener);
		}
		listenersToPartId.clear();

		i = postListenersToPartId.keySet().iterator();
		while (i.hasNext()) {
			Object listener = i.next();
			parentSelectionService.removePostSelectionListener(
					(String) postListenersToPartId.get(listener),
					(ISelectionListener) listener);
		}
		postListenersToPartId.clear();
	}
}
